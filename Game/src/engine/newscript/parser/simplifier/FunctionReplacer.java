package engine.newscript.parser.simplifier;

import static engine.newscript.lexer.TokenType.*;
import static engine.newscript.parser.ParseUtil.*;

import java.util.ArrayList;
import java.util.Stack;

import engine.newscript.BuiltInFunctionList;
import engine.newscript.DScript;
import engine.newscript.lexer.Token;
import engine.newscript.parser.ParseUnit;

/**
 * 
 * Replaces all function definitions and calls with an index number for compilation.
 * 
 * @author Daniel
 * 
 */

public class FunctionReplacer{

	private Stack<ArrayList<String>> functions;
	private int funcNum;
	
	private final BuiltInFunctionList biFunc;
	
	public FunctionReplacer(){
		functions = new Stack<ArrayList<String>>();
		biFunc = new BuiltInFunctionList(null);
	}
	
	public void process(DScript script){
		
		functions.clear();
		pushFunctionList();
		
		ArrayList<Object> parseTree = script.getParseTree();
		
		// Add top-level functions first
		for(Object o:parseTree)
			addFunctions((ParseUnit)o);
		
		// Process each item
		for(Object o:parseTree)
			process((ParseUnit)o);
	}
	
	private void process(ParseUnit p){
		
		boolean isBlock = p.getType().equals("func_block") || p.getType().equals("task_block");
		
		if(isBlock){
			// Push new list for each new block
			pushFunctionList();
			
			// Add all functions defined within the block
			addFunctions(p);
		}
		else
			replaceFunctions(p);
		
		Object[] contents = p.getContents();
		
		for(Object o:contents){
			
			if(!(o instanceof ParseUnit))
				continue;
			
			process((ParseUnit)o);
		}
		
		if(isBlock)
			popFunctionList();
	}
	
	private void addFunctions(ParseUnit p){
		
		Object[] contents = p.getContents();
		
		for(Object o:contents){
			
			if(!(o instanceof ParseUnit))
				continue;
			
			
			ParseUnit p2 = (ParseUnit)o;
			
			if(p2.getType().equals("func_block") || p2.getType().equals("task_block"))
				addFunction((ParseUnit)o);
			else
				addFunctions((ParseUnit)o);
		}
	}
	
	private void addFunction(ParseUnit p){
		
		p = (ParseUnit)p.getContents()[0];
		Object[] cont = p.getContents();
		int params = cont.length == 1 ? 0 : cont[1] instanceof Token ? 1 : ((ParseUnit)cont[1]).getContents().length;
		
		// Function names
		Token t = (Token)((ParseUnit)cont[0]).getContents()[0];
		cont[0] = new Token(IDENTIFIER, Integer.toString(funcNum), t.getFile(), t.getLineNum());
		
		addFunction(t.getValue(), params);
	}
	
	private void replaceFunctions(ParseUnit p){
		
		switch(p.getType()){
			
			case "func_call":
				Object[] contents = p.getContents();
				
				Token t = (Token)((ParseUnit)contents[0]).getContents()[0];

				int params = contents.length == 1 ? 0 : ((ParseUnit)contents[1]).getType().equals("list") ? ((ParseUnit)contents[1]).getContents().length : 1;
				
				if(p.getParent() != null && p.getParent().getType().equals("dot_func_call"))
					params++;
				
				contents[0] = new Token(IDENTIFIER, getFunctionNumber(t.getValue(), params), t.getFile(), t.getLineNum());
				
				// If built-in
				if(biFunc.isBuiltInFunction(t.getValue() + ',' + params))
					replaceParseUnit(p, new ParseUnit("func_call_bi", contents));
				
				break;
				
				
			case "func_call_scope":
				contents = p.getContents();
				
				ParseUnit p2 = (ParseUnit)contents[0];
				
				t = (Token)p2.getContents()[0];
				params = contents.length == 1 ? 0 : ((ParseUnit)contents[1]).getType().equals("list") ? ((ParseUnit)contents[1]).getContents().length : 1;
				int scope = Integer.parseInt(((Token)p2.getContents()[2]).getValue());

				Token n = new Token(IDENTIFIER, getFunctionNumberInScope(t.getValue(), params, scope), t.getFile(), t.getLineNum());
				
				// Replace id_scope
				contents[0] = n;
				
				// Replace func_call_scope
				replaceParseUnit(p, new ParseUnit("func_call", contents));
				
				break;
		}
	}
	
	private void pushFunctionList(){
		functions.push(new ArrayList<String>());
	}
	
	private void popFunctionList(){
		functions.pop();
	}
	
	private void addFunction(String name, int params){
		functions.peek().add(name + ',' + params + ',' + funcNum++);
	}
	
	private String getFunctionNumber(String func, int params){
		
		func += "," + params;
		
		for(int i = functions.size() - 1; i >= 0; i--){
			
			ArrayList<String> funcs = functions.get(i);
			
			for(String f:funcs)
				if(getName(f).equals(func))
					return getValue(f);
		}
		
		return Integer.toString(biFunc.getBuiltInFunctionIndex(func));
	}
	
	private String getFunctionNumberInScope(String func, int params, int scope){
		
		func += "," + params;
		
		int i = functions.size() - 1 - scope;
		
		ArrayList<String> funcs = functions.get(i);
		
		for(String f:funcs)
			if(getName(f).equals(func))
				return getValue(f);
		
		return "";
	}
	
	private String getName(String var){
		return var.substring(0, var.lastIndexOf(','));
	}
	
	private String getValue(String var){
		return var.substring(var.lastIndexOf(',') + 1);
	}
}
