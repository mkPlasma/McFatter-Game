package engine.newscript.parser;

import static engine.newscript.lexer.TokenType.*;

import java.util.ArrayList;
import java.util.Stack;

import engine.newscript.DScript;
import engine.newscript.ScriptException;
import engine.newscript.lexer.Token;

public class ParseTreeScopeChecker{
	
	// Stack for each block
	private Stack<ArrayList<String>> variables;
	private Stack<ArrayList<String>> constVariables;
	
	public ParseTreeScopeChecker(){
		variables		= new Stack<ArrayList<String>>();
		constVariables	= new Stack<ArrayList<String>>();
	}
	
	public void process(DScript script) throws ScriptException{

		variables.clear();
		constVariables.clear();
		pushVarList();
		
		ArrayList<Object> parseTree = script.getParseTree();
		
		for(Object o:parseTree)
			process((ParseUnit)o);
	}
	
	private void process(ParseUnit p) throws ScriptException{
		
		boolean isBlock = p.getType().equals("s_block");
		
		// Push new list for each new block
		if(isBlock)
			pushVarList();
		
		// Check if used variables exist
		checkVariables(p);
		
		// Add new variables in current ParseUnit
		addVariables(p);
		
		
		Object[] contents = p.getContents();
		
		for(Object o:contents){
			
			if(o instanceof Token)
				continue;
			
			process((ParseUnit)o);
		}
		
		// Remove block variables
		if(isBlock)
			popVarList();
	}
	
	private void checkVariables(ParseUnit p) throws ScriptException{
		
		Object[] contents = p.getContents();
		
		
		switch(p.getType()){
				
			case "expression":
				
				for(Object o:contents){
					
					if(o instanceof ParseUnit || (o instanceof Token && ((Token)o).getType() != IDENTIFIER))
						continue;
					
					Token t = (Token)o;
					String var = t.getValue();
					
					if(!variableExists(var))
						throwUndefinedVar(var, t);
				}
				
				break;
		}
	}
	
	private void addVariables(ParseUnit p){
		
		Object[] contents = p.getContents();
		
		
		switch(p.getType()){
				
			case "func_def": case "task_def":
				
				if(contents.length < 2)
					return;
				
				ParseUnit list = (ParseUnit)contents[1];
				contents = list.getContents();
				
				for(Object o:contents)
					addVariable(((Token)o).getValue());
				
				break;
				
				
			case "for_cond": case "new_var":
				addVariable(((Token)contents[0]).getValue());
				break;
				
				
			case "new_const_var":
				addConstVariable(((Token)contents[0]).getValue());
				break;
		}
	}
	
	private void pushVarList(){
		variables.push(new ArrayList<String>());
		constVariables.push(new ArrayList<String>());
	}
	
	private void popVarList(){
		variables.pop();
		constVariables.pop();
	}

	private void addVariable(String var){
		variables.peek().add(var);
	}
	
	private void addConstVariable(String var){
		constVariables.peek().add(var);
	}
	
	private boolean variableExists(String var){
		
		for(ArrayList<String> vars:variables)
			for(String v:vars)
				if(v.equals(var))
					return true;
		
		return false;
	}
	
	private void throwUndefinedVar(String var, Token t) throws ScriptException{
		throw new ScriptException("Variable '" + var + "' is not defined", t.getFile(), t.getLineNum());
	}
}
