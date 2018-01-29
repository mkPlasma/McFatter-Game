package engine.newscript.parser.simplifier;

import static engine.newscript.lexer.TokenType.*;
import static engine.newscript.parser.ParseUtil.*;

import java.util.ArrayList;
import java.util.Stack;

import engine.newscript.DScript;
import engine.newscript.ScriptException;
import engine.newscript.lexer.Token;
import engine.newscript.lexer.TokenType;
import engine.newscript.parser.ParseUnit;

public class VariableReplacer{
	
	// Stack for each block
	private Stack<ArrayList<String>> globalVariables;
	private Stack<Stack<ArrayList<String>>> localVariables;
	private Stack<ArrayList<String>> constVariables;
	
	
	public VariableReplacer(){
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
			replaceVariables(p);
			
			// Add new variables defined in current ParseUnit
			addVariables(p);
		}
		
		for(Object o:contents){
			
			if(o instanceof Token)
				continue;
			
			
			ParseUnit p2 = (ParseUnit)o;
			
			process(p2);
			
			// Remove const var definitions
			if(p2.getType().equals("const_var_def"))
				removeParseUnit(p2);
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
	
	private void replaceVariables(ParseUnit p){
		
		Object[] contents = p.getContents();
		
		
		switch(p.getType()){
				
			case "expression":
				
				if(contents.length > 1 || !(contents[0] instanceof Token))
					return;
				
				Token t = (Token)contents[0];
				
				if(t.getType() != IDENTIFIER)
					return;
				
				replaceVariable(p, 0, t, -1);
				return;
				
				
			case "id_scope":
				
				ParseUnit parent = p.getParent();
				
				if(parent.getType().equals("func_call_scope"))
					return;
				
				t = (Token)contents[0];
				replaceVariable(p, 0, t, Integer.parseInt(((Token)contents[2]).getValue()));
				
				// Replace id_scope
				replaceParseUnit(p, contents[0]);
				
				return;
				
				
			case "assign": case "assign_u":
				
				Object o = contents[0];
				
				if(o instanceof ParseUnit && ((ParseUnit)o).getType().equals("id_scope"))
					return;
				
				t = (Token)(o instanceof Token ? o : ((ParseUnit)o).getContents()[0]);

				replaceVariable(p, 0, t, -1);
				return;
		}
	}
	
	private void addVariables(ParseUnit p){
		
		Object[] contents = p.getContents();
		boolean local = p.isWithin("func_block") || p.isWithin("task_block");
		
		switch(p.getType()){
				
			case "new_var": case "for_cond":
				Token t = (Token)contents[0];
				
				addVariable(t.getValue(), local);
				replaceVariable(p, 0, t, 0);
				return;
				
				
			case "const_var_def":
				
				t = (Token)((ParseUnit)contents[2]).getContents()[0];
				TokenType tt = t.getType();
				
				char type = tt == INT ? 'i' : tt == FLOAT ? 'f' : tt == BOOLEAN ? 'b' : 's';
				
				addConstVariable(((Token)((ParseUnit)contents[0]).getContents()[0]).getValue(), type, t.getValue());
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
	
	private void replaceVariable(ParseUnit p, int index, Token t, int scope){
		
		Object[] contents = p.getContents();
		
		String var = t.getValue();
		
		// Replace constants
		if(scope == -1 ? isConstantVariable(var) : isConstantVariableInScope(var, scope)){
			
			String val = scope == -1 ? getConstantValue(var) : getConstantValueInScope(var, scope);
			
			char type = val.charAt(0);
			val = val.substring(1);
			
			TokenType tt = type == 'i' ? INT : type == 'f' ? FLOAT : type == 'b' ? BOOLEAN : STRING;
			
			contents[index] = new Token(tt, val, t.getFile(), t.getLineNum());
			return;
		}
		
		int n = scope == -1 ? getVariableNumber(var) : getVariableNumberInScope(var, scope);
		
		contents[index] = new Token(IDENTIFIER, (isLocalVariable(var) ? "l" : "") + n, t.getFile(), t.getLineNum());
	}
	
	private void addVariable(String var, boolean local){
		
		int n = 0;
		
		if(local){
			for(ArrayList<String> vars:localVariables.peek())
				n += vars.size();
			
			localVariables.peek().peek().add(var + "," + n);
			return;
		}
		
		for(ArrayList<String> vars:globalVariables)
			n += vars.size();
		
		globalVariables.peek().add(var + "," + n);
	}
	
	private void addConstVariable(String var, char type, String value){
		constVariables.peek().add(var + "," + type + value);
	}
	
	private int getVariableNumber(String var){
		
		if(!localVariables.isEmpty()){
			for(int i = localVariables.peek().size() - 1; i >= 0; i--){
				
				ArrayList<String> vars = localVariables.peek().get(i);
				
				for(String v:vars)
					if(getName(v).equals(var))
						return getNum(v);
			}
		}
		
		for(int i = globalVariables.size() - 1; i >= 0; i--){
			
			ArrayList<String> vars = globalVariables.get(i);
			
			for(String v:vars)
				if(getName(v).equals(var))
					return getNum(v);
		}
		
		return -1;
	}
	
	private int getVariableNumberInScope(String var, int scope){
		
		// Positive - local		Negative - global
		int i = localVariables.isEmpty() ? scope - 1 : localVariables.peek().size() - 1 - scope;
		
		ArrayList<String> vars = i < 0 ? globalVariables.get(globalVariables.size() + i) : localVariables.peek().get(i);
		
		for(String v:vars)
			if(getName(v).equals(var))
				return getNum(v);
		
		return -1;
	}
	
	private boolean isLocalVariable(String var){
		
		if(!localVariables.isEmpty())
			for(ArrayList<String> vars:localVariables.peek())
				for(String v:vars)
					if(getName(v).equals(var))
						return true;
		
		return false;
	}
	
	private boolean isConstantVariable(String var){
		
		for(ArrayList<String> vars:constVariables)
			for(String v:vars)
				if(getName(v).equals(var))
					return true;
		return false;
	}
	
	private boolean isConstantVariableInScope(String var, int scope){
		
		int i = constVariables.size() - 1 - scope;
		
		ArrayList<String> vars = constVariables.get(i);
		
		for(String v:vars)
			if(getName(v).equals(var))
				return true;
		
		return false;
	}
	
	private String getConstantValue(String var){
		
		for(int i = constVariables.size() - 1; i >= 0; i--){
			
			ArrayList<String> vars = constVariables.get(i);
			
			for(String v:vars)
				if(getName(v).equals(var))
					return getValue(v);
		}
		
		return "";
	}
	
	private String getConstantValueInScope(String var, int scope){
		
		int i = constVariables.size() - 1 - scope;
		
		ArrayList<String> vars = constVariables.get(i);
		
		for(String v:vars)
			if(getName(v).equals(var))
				return getValue(v);
		
		return "";
	}
	
	private String getName(String var){
		return var.substring(0, var.indexOf(','));
	}
	
	private int getNum(String var){
		return Integer.parseInt(getValue(var));
	}
	
	private String getValue(String var){
		return var.substring(var.indexOf(',') + 1);
	}
}
