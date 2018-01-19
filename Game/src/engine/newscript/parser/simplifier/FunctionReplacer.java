package engine.newscript.parser.simplifier;

import static engine.newscript.lexer.TokenType.*;
import static engine.newscript.parser.ParseUtil.replaceParseUnit;

import java.util.ArrayList;
import java.util.Stack;

import engine.newscript.DScript;
import engine.newscript.lexer.Token;
import engine.newscript.parser.ParseUnit;

public class FunctionReplacer{

	private Stack<ArrayList<String>> functions;
	private int funcNum;
	
	public FunctionReplacer(){
		functions	= new Stack<ArrayList<String>>();
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
		
		boolean isBlock = p.getType().equals("s_block");
		
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
			
			if(p2.getType().equals("s_block"))
				addFunction((ParseUnit)o);
			else
				addFunctions((ParseUnit)o);
		}
	}
	
	private void addFunction(ParseUnit p){
		
		if(p.getType().equals("s_block")){
			
			p = (ParseUnit)p.getContents()[0];
			String type = p.getType();
			
			// Check type
			if(!type.equals("func_block") && !type.equals("task_block"))
				return;
			
			// func_def/task_def
			p = (ParseUnit)p.getContents()[0];
			Object[] cont = p.getContents();
			int params = cont.length == 1 ? 0 : cont[1] instanceof Token ? 1 : ((ParseUnit)cont[1]).getContents().length;
			
			Token t = (Token)cont[0];
			
			addFunction(t.getValue(), params);
		}
	}
	
	private void replaceFunctions(ParseUnit p){
		
		switch(p.getType()){
			
			case "func_call":
				Object[] contents = p.getContents();
				
				Token t = (Token)contents[0];
				
				int params = contents.length == 1 ? 0 : ((ParseUnit)contents[1]).getContents().length;
				
				if(p.getParent() != null && p.getParent().getType().equals("dot_func_call"))
					params++;
				
				contents[0] = new Token(IDENTIFIER, getFunctionNumber(t.getValue(), params), t.getFile(), t.getLineNum());
				break;
				
				
			case "func_call_scope":
				contents = p.getContents();
				
				ParseUnit p2 = (ParseUnit)contents[0];
				
				t = (Token)p2.getContents()[0];
				params = contents.length == 1 ? 0 : ((ParseUnit)contents[1]).getContents().length;
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
		functions.peek().add(name + "," + params + "," + funcNum);
		funcNum++;
	}
	
	private String getFunctionNumber(String func, int params){
		
		func += "," + params;
		
		for(int i = functions.size() - 1; i >= 0; i--){
			
			ArrayList<String> funcs = functions.get(i);
			
			for(String f:funcs)
				if(getName(f).equals(func))
					return getValue(f);
		}
		
		return "";
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
