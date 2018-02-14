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
	private Stack<ArrayList<String>> globalVariables;
	private Stack<Stack<ArrayList<String>>> localVariables;
	private Stack<ArrayList<String>> constVariables;
	
	
	public VariableChecker(){
		globalVariables	= new Stack<ArrayList<String>>();
		localVariables	= new Stack<Stack<ArrayList<String>>>();
		constVariables	= new Stack<ArrayList<String>>();
	}
	
	public void process(DScript script) throws ScriptException{
		
		globalVariables.clear();
		localVariables.clear();
		constVariables.clear();
		
		pushGlobalVarList();
		
		ArrayList<Object> parseTree = script.getParseTree();
		
		for(Object o:parseTree)
			process((ParseUnit)o);
	}
	
	private void process(ParseUnit p) throws ScriptException{
		
		boolean isBlock = p.getType().equals("s_block");
		boolean func = false;
		boolean local = false;
		
		Object[] contents = p.getContents();
		
		// Push new list for each new block
		if(isBlock){
			// Local variables
			ParseUnit p2 = (ParseUnit)contents[0];
			func = p2.getType().equals("func_block") || p2.getType().equals("task_block");
			local = p.isWithin("func_block") || p.isWithin("task_block");
			
			if(func)
				pushLocalVarStack();
			else if(local)
				pushLocalVarList();
			else
				pushGlobalVarList();
		}
		
		else{
			// Check if used variables exist
			checkVariables(p);
			
			// Add new variables defined in current ParseUnit
			addVariables(p);
		}
		
		for(Object o:contents){
			
			if(o instanceof Token)
				continue;
			
			process((ParseUnit)o);
		}
		
		// Remove block variables
		if(isBlock){
			if(func)
				popLocalVarStack();
			else if(local)
				popLocalVarList();
			else
				popGlobalVarList();
		}
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
				
				if(!variableExists(t.getValue()))
					throwUndefinedVarException(t.getValue(), t);
				
				return;
				
				
			case "id_scope":
				
				ParseUnit parent = p.getParent();
				
				if(parent.getType().equals("func_call_scope"))
					return;
				
				t = (Token)contents[0];
				
				if(!variableExistsInScope(t.getValue(), Integer.parseInt(((Token)contents[2]).getValue())))
					throwUndefinedVarException(t.getValue(), t);
				
				return;
				
				
			case "array_elem":
				
				if(!(contents[0] instanceof Token))
					return;
				
				t = (Token)contents[0];
				
				if(!variableExists(t.getValue()))
					throwUndefinedVarException(t.getValue(), t);
				
				return;
				
				
			case "assign": case "assign_u":
				
				Object o = contents[0];
				
				if(o instanceof ParseUnit && ((ParseUnit)o).getType().equals("id_scope"))
					return;
				
				t = (Token)(o instanceof Token ? o : ((ParseUnit)o).getContents()[0]);
				
				if(isConstantVariable(t.getValue()))
					throw new ScriptException("Constant variables cannot be modified", t.getFile(), t.getLineNum());
				
				if(!variableExists(t.getValue()))
					throwUndefinedVarException(t.getValue(), t);
				
				return;
				
				
			case "new_var": case "new_const_var": case "for_pre":
				
				t = (Token)contents[0];
				
				if(variableExistsInScope(t.getValue(), 0))
					throwVarExistsException(t.getValue(), t);
				
				break;
		}
	}
	
	private void addVariables(ParseUnit p){
		
		Object[] contents = p.getContents();
		boolean local = p.isWithin("func_block") || p.isWithin("task_block");
		
		switch(p.getType()){
				
			case "new_var": case "for_pre":
				Token t = (Token)contents[0];
				
				addVariable(t.getValue(), local);
				return;
				
				
			case "const_var_def":
				addConstVariable(((Token)((ParseUnit)contents[0]).getContents()[0]).getValue());
				return;
				
				
			case "func_def": case "task_def":
				
				if(contents.length < 2)
					return;
				
				// Single parameter
				if(contents[1] instanceof Token){
					addVariable(((Token)contents[1]).getValue(), local);
					return;
				}
				
				// Parameter list
				ParseUnit list = (ParseUnit)contents[1];
				contents = list.getContents();
				
				for(Object o:contents)
					addVariable(((Token)((ParseUnit)o).getContents()[0]).getValue(), local);
				
				return;
		}
	}
	
	private void pushGlobalVarList(){
		globalVariables.push(new ArrayList<String>());
		constVariables.push(new ArrayList<String>());
	}
	
	private void popGlobalVarList(){
		globalVariables.pop();
		constVariables.pop();
	}
	
	private void pushLocalVarStack(){
		localVariables.push(new Stack<ArrayList<String>>());
		pushLocalVarList();
	}
	
	private void popLocalVarStack(){
		localVariables.pop();
		constVariables.pop();
	}
	
	private void pushLocalVarList(){
		localVariables.peek().push(new ArrayList<String>());
		constVariables.push(new ArrayList<String>());
	}
	
	private void popLocalVarList(){
		localVariables.peek().pop();
		constVariables.pop();
	}
	
	private void addVariable(String var, boolean local){
		
		if(local)
			localVariables.peek().peek().add(var);
		else
			globalVariables.peek().add(var);
	}
	
	private void addConstVariable(String var){
		constVariables.peek().add(var);
	}
	
	private boolean variableExists(String var){
		
		if(!localVariables.isEmpty()){
			for(int i = localVariables.peek().size() - 1; i >= 0; i--){
				
				ArrayList<String> vars = localVariables.peek().get(i);
				
				for(String v:vars)
					if(v.equals(var))
						return true;
			}
		}
		
		for(int i = globalVariables.size() - 1; i >= 0; i--){
			
			ArrayList<String> vars = globalVariables.get(i);
			
			for(String v:vars)
				if(v.equals(var))
					return true;
		}
		
		for(int i = constVariables.size() - 1; i >= 0; i--){
			
			ArrayList<String> vars = constVariables.get(i);
			
			for(String v:vars)
				if(v.equals(var))
					return true;
		}
		
		return false;
	}
	
	private boolean variableExistsInScope(String var, int scope){
		
		// Positive - local		Negative - global
		int i = localVariables.isEmpty() ? scope - 1 : localVariables.peek().size() - 1 - scope;
		
		ArrayList<String> vars = i < 0 ? globalVariables.get(globalVariables.size() + i) : localVariables.peek().get(i);
		
		for(String v:vars)
			if(v.equals(var))
				return true;
		
		return false;
	}
	
	private boolean isConstantVariable(String var){
		
		for(ArrayList<String> vars:constVariables)
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
