package engine.newscript.parser.checker;

import static engine.newscript.lexer.TokenType.*;

import java.util.ArrayList;
import java.util.Stack;

import engine.newscript.DScript;
import engine.newscript.ScriptException;
import engine.newscript.lexer.Token;
import engine.newscript.parser.ParseUnit;

public class ParseTreeVariableChecker{
	
	// Stack for each block
	private Stack<ArrayList<String>> variables;
	private Stack<ArrayList<String>> constVariables;
	
	public ParseTreeVariableChecker(){
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
		
		else{
			// Check if used variables exist
			checkVariables(p);
			
			// Add new variables defined in current ParseUnit
			addVariables(p);
		}
		
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
				
				if(contents.length > 1 || !(contents[0] instanceof Token))
					return;
				
				Token t = (Token)contents[0];
				
				if(t.getType() != IDENTIFIER)
					return;
				
				String var = t.getValue();
				
				if(!variableExists(var))
					throwUndefinedVarException(var, t);
				
				break;
				
				
			case "new_var": case "new_const_var": case "for_cond":
				
				t = (Token)contents[0];
				var = t.getValue();
				
				if(variableExistsInScope(var))
					throwVarExistsException(var, t);
				
				break;
				
				
			case "assignment":

				t = (Token)contents[0];
				var = t.getValue();
				
				if(isConstantVariable(var))
					throw new ScriptException("Constant variables cannot be modified", t.getFile(), t.getLineNum());
				
				break;
		}
	}
	
	private void addVariables(ParseUnit p){
		
		Object[] contents = p.getContents();
		
		
		switch(p.getType()){
				
			case "new_var": case "for_cond":
				addVariable(((Token)contents[0]).getValue());
				break;
				
				
			case "new_const_var":
				addConstVariable(((Token)contents[0]).getValue());
				break;
				
				
			case "func_def": case "task_def":
				
				if(contents.length < 2)
					return;
				
				ParseUnit list = (ParseUnit)contents[1];
				contents = list.getContents();
				
				for(Object o:contents)
					addVariable(((Token)((ParseUnit)o).getContents()[0]).getValue());
				
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
		
		return isConstantVariable(var);
	}
	
	private boolean isConstantVariable(String var){
		
		for(ArrayList<String> vars:constVariables)
			for(String v:vars)
				if(v.equals(var))
					return true;
		
		return false;
	}
	
	private boolean variableExistsInScope(String var){
		
		ArrayList<String> vars = variables.peek();
		
		for(String v:vars)
			if(v.equals(var))
				return true;
		
		
		vars = constVariables.peek();
		
		for(String v:vars)
			if(v.equals(var))
				return true;
		
		return false;
	}
	
	private void throwUndefinedVarException(String var, Token t) throws ScriptException{
		throw new ScriptException("Variable '" + var + "' is not defined", t.getFile(), t.getLineNum());
	}
	
	private void throwVarExistsException(String var, Token t) throws ScriptException{
		throw new ScriptException("Duplicate variable '" + var + "'", t.getFile(), t.getLineNum());
	}
}
