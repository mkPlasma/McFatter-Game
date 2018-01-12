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
	
	private void process(ParseUnit p){
		
		boolean isBlock = p.getType().equals("s_block");
		
		// Push new list for each new block
		if(isBlock){
			pushFunctionList();
			addFunctions(p);
		}
		
		Object[] contents = p.getContents();
		
		for(Object o:contents){
			
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
			
			if(p2.getType().equals("s_block")){
				
				p2 = (ParseUnit)p2.getContents()[0];
				String type = p2.getType();
				
				if(!type.equals("func_block") && !type.equals("task_block"))
					continue;
				
				// func_def/task_def
				p2 = (ParseUnit)p2.getContents()[0];
				Object[] cont = p2.getContents();
				
				String name = ((Token)((ParseUnit)cont[0]).getContents()[0]).getValue();
				int params = cont.length == 1 ? 0 : ((ParseUnit)cont[1]).getContents().length;
				
				addFunction(name, params);
			}
		}
	}
	
	private void checkFunctions(ParseUnit p){
		
	}
	
	private void pushFunctionList(){
		functions.push(new ArrayList<String>());
	}
	
	private void popFunctionList(){
		functions.pop();
	}
	
	private void addFunction(String name, int paramCount){
		functions.peek().add(name + "," + paramCount);
	}
}
