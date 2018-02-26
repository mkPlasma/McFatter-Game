package engine.newscript.parser.checker;

import java.util.ArrayList;
import java.util.Stack;

import engine.newscript.BuiltInFunctionList;
import engine.newscript.DScript;
import engine.newscript.ScriptException;
import engine.newscript.lexer.Token;
import engine.newscript.parser.ParseUnit;

/**
 * 
 * Checks that function calls reference existing functions and duplicate functions.
 * 
 * @author Daniel
 * 
 */

public class FunctionChecker{

	private Stack<ArrayList<String>> functions;
	private BuiltInFunctionList biFunc;
	
	public FunctionChecker(){
		functions = new Stack<ArrayList<String>>();
		biFunc = new BuiltInFunctionList(null);
	}
	
	public void process(DScript script) throws ScriptException{
		
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
	
	private void process(ParseUnit p) throws ScriptException{
		
		boolean isBlock = p.getType().equals("s_block");
		
		if(isBlock){
			// Push new list for each new block
			pushFunctionList();
			
			// Add all functions defined within the block
			addFunctions(p);
		}
		else
			checkFunctions(p);
		
		Object[] contents = p.getContents();
		
		for(Object o:contents){
			
			if(!(o instanceof ParseUnit))
				continue;
			
			process((ParseUnit)o);
		}
		
		if(isBlock)
			popFunctionList();
	}
	
	private void addFunctions(ParseUnit p) throws ScriptException{
		
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
	
	private void addFunction(ParseUnit p) throws ScriptException{
		
		if(p.getType().equals("s_block")){
			
			p = (ParseUnit)p.getContents()[0];
			String type = p.getType();
			
			// Check type
			if(!type.equals("func_block") && !type.equals("task_block"))
				return;
			
			// func_def/task_def
			p = (ParseUnit)p.getContents()[0];
			Object[] cont = p.getContents();
			
			Token t = (Token)cont[0];
			
			String func = t.getValue();
			int params = cont.length == 1 ? 0 : cont[1] instanceof Token ? 1 : ((ParseUnit)cont[1]).getContents().length;
			
			if(functionExistsInScope(func, params, 0))
				throw new ScriptException("Duplicate function '" + func + "'", t.getFile(), t.getLineNum());
			
			if(biFunc.isBuiltInFunction(func + ',' + params))
				throw new ScriptException("Duplicate built-in function '" + func + "'", t.getFile(), t.getLineNum());
			
			addFunction(func, params);
		}
	}
	
	private void checkFunctions(ParseUnit p) throws ScriptException{
		
		switch(p.getType()){
			
			case "func_call":
				Object[] contents = p.getContents();
				
				Token t = (Token)contents[0];
				String func = t.getValue();
				
				int params = contents.length == 1 ? 0 : ((ParseUnit)contents[1]).getType().equals("list") ? ((ParseUnit)contents[1]).getContents().length : 1;
				
				if(p.getParent() != null && p.getParent().getType().equals("dot_func_call"))
					params++;
				
				if(!functionExists(func, params))
					throwUndefinedFunctionException(func, t);
				
				break;
				
				
			case "func_call_scope":
				contents = p.getContents();
				
				ParseUnit p2 = (ParseUnit)contents[0];
				
				t = (Token)p2.getContents()[0];
				func = t.getValue();

				params = contents.length == 1 ? 0 : ((ParseUnit)contents[1]).getType().equals("list") ? ((ParseUnit)contents[1]).getContents().length : 1;
				
				int scope = Integer.parseInt(((Token)p2.getContents()[2]).getValue());
				
				if(!functionExistsInScope(func, params, scope))
					throwUndefinedFunctionExceptionInScope(func, t, scope);
				
				break;
		}
	}
	
	private void pushFunctionList(){
		functions.push(new ArrayList<String>());
	}
	
	private void popFunctionList(){
		functions.pop();
	}
	
	private void addFunction(String name, int paramCount){
		functions.peek().add(name + ',' + paramCount);
	}
	
	private boolean functionExists(String func, int paramCount){
		
		func += "," + paramCount;
		
		for(ArrayList<String> funcs:functions)
			for(String f:funcs)
				if(f.equals(func))
					return true;
		
		return biFunc.isBuiltInFunction(func);
	}
	
	private boolean functionExistsInScope(String func, int paramCount, int scope){
		
		int i = functions.size() - 1 - scope;
		
		if(i < 0 || i >= functions.size())
			return false;
		
		func += "," + paramCount;
		
		ArrayList<String> funcs = functions.get(i);
		
		for(String f:funcs)
			if(f.equals(func))
				return true;
		
		return false;
	}
	
	private void throwUndefinedFunctionException(String func, Token t) throws ScriptException{
		
		for(ArrayList<String> funcs:functions)
			for(String f:funcs)
				if(f.substring(0, f.indexOf(',')).equals(func))
					throw new ScriptException("Incorrect parameter count for function/task '" + func + "'", t.getFile(), t.getLineNum());
		
		throw new ScriptException("Function/task '" + func + "' is not defined", t.getFile(), t.getLineNum());
	}
	
	private void throwUndefinedFunctionExceptionInScope(String func, Token t, int scope) throws ScriptException{
		
		int i = functions.size() - 1 - scope;
		
		if(i >= 0 && i < functions.size()){
			
			ArrayList<String> funcs = functions.get(i);
			
			for(String f:funcs)
				if(f.substring(0, f.indexOf(',')).equals(func))
					throw new ScriptException("Incorrect parameter count for function/task '" + func + "'" + " in scope " + scope, t.getFile(), t.getLineNum());
		}
		
		
		throw new ScriptException("Function/task '" + func + "' is not defined in scope " + scope, t.getFile(), t.getLineNum());
	}
}
