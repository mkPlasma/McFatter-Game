package engine.newscript.parser.checker;

import static engine.newscript.lexer.TokenType.*;

import java.util.ArrayList;
import java.util.Stack;

import engine.newscript.DScript;
import engine.newscript.ScriptException;
import engine.newscript.lexer.Token;
import engine.newscript.parser.ParseUnit;

public class VariableChecker{
	
	// Stack for each block
	private Stack<ArrayList<String>> variables;
	private Stack<ArrayList<String>> constVariables;
	
	public VariableChecker(){
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
				
				
			case "id_scope":
				
				if(p.getParent().getType().equals("func_call_scope"))
					return;
				
				t = (Token)contents[0];
				var = t.getValue();
				
				int scope = Integer.parseInt(((Token)contents[2]).getValue());
				
				if(!variableExistsInScope(var, scope))
					throw new ScriptException("Variable '" + var + "' is not defined in scope " + scope, t.getFile(), t.getLineNum());
				
				break;
				
				
			case "new_var": case "new_const_var": case "for_cond":
				
				t = (Token)contents[0];
				var = t.getValue();
				
				if(variableExistsInScope(var, 0))
					throwVarExistsException(var, t);
				
				break;
				
				
			case "assignment":
				
				t = (Token)(contents[0] instanceof Token ? contents[0] : ((ParseUnit)contents[0]).getContents()[0]);
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
				return;
				
				
			case "new_const_var":
				addConstVariable(((Token)contents[0]).getValue());
				return;
				
				
			case "func_def": case "task_def":
				
				if(contents.length < 2)
					return;
				
				// Single parameter
				if(contents[1] instanceof Token){
					addVariable(((Token)contents[1]).getValue());
					return;
				}
				
				// Parameter list
				ParseUnit list = (ParseUnit)contents[1];
				contents = list.getContents();
				
				for(Object o:contents)
					addVariable(((Token)((ParseUnit)o).getContents()[0]).getValue());
				
				return;
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
	
	private boolean variableExistsInScope(String var, int scope){
		
		int i = variables.size() - 1 - scope;
		
		if(i < 0 || i >= variables.size())
			return false;
		
		ArrayList<String> vars = variables.get(i);
		
		for(String v:vars)
			if(v.equals(var))
				return true;
		
		
		vars = constVariables.get(i);
		
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
