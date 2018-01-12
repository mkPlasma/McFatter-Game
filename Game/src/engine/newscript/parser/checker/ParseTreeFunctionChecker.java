package engine.newscript.parser.checker;

import java.util.ArrayList;
import java.util.Stack;

import engine.newscript.DScript;
import engine.newscript.ScriptException;
import engine.newscript.lexer.Token;
import engine.newscript.parser.ParseUnit;

public class ParseTreeFunctionChecker{

	private Stack<ArrayList<String>> functions;
	
	public ParseTreeFunctionChecker(){
		functions	= new Stack<ArrayList<String>>();
	}
	
	public void process(DScript script) throws ScriptException{
		
		functions.clear();
		pushFunctionList();
		
		ArrayList<Object> parseTree = script.getParseTree();
		
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
		
		addFunction(p, 1);
		
		Object[] contents = p.getContents();
		
		for(Object o:contents){
			
			if(!(o instanceof ParseUnit))
				continue;
			
			ParseUnit p2 = (ParseUnit)o;
			
			if(p2.getType().equals("s_block"))
				addFunction((ParseUnit)o, 0);
			else
				addFunctions((ParseUnit)o);
		}
	}
	
	private void addFunction(ParseUnit p, int scope) throws ScriptException{
		
		if(p.getType().equals("s_block")){
			
			p = (ParseUnit)p.getContents()[0];
			String type = p.getType();
			
			if(!type.equals("func_block") && !type.equals("task_block"))
				return;
			
			// func_def/task_def
			p = (ParseUnit)p.getContents()[0];
			Object[] cont = p.getContents();
			
			Token t = (Token)((ParseUnit)cont[0]).getContents()[0];
			
			String func = t.getValue();
			int params = cont.length == 1 ? 0 : ((ParseUnit)cont[1]).getContents().length;
			
			if(functionExists(func, params))
				throw new ScriptException("Duplicate function '" + func + "'", t.getFile(), t.getLineNum());
			
			addFunction(func, params, scope);
		}
	}
	
	private void checkFunctions(ParseUnit p) throws ScriptException{
		
		if(!p.getType().equals("func_call"))
			return;
		
		Object[] contents = p.getContents();
		
		Token t = (Token)((ParseUnit)contents[0]).getContents()[0];
		String func = t.getValue();
		
		int params = contents.length == 1 ? 0 : ((ParseUnit)contents[1]).getContents().length;
		
		if(!functionExists(func, params))
			throwUndefinedFunctionException(func, t);
	}
	
	private void pushFunctionList(){
		functions.push(new ArrayList<String>());
	}
	
	private void popFunctionList(){
		functions.pop();
	}
	
	private void addFunction(String name, int paramCount, int scope){
		functions.get(functions.size() - 1 - scope).add(name + "," + paramCount);
	}
	
	private boolean functionExists(String func, int paramCount){
		
		func += "," + paramCount;
		
		for(ArrayList<String> funcs:functions)
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
}
