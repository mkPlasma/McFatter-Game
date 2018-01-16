package engine.newscript.parser.simplifier;

import static engine.newscript.lexer.TokenType.*;

import java.util.ArrayList;
import java.util.Stack;

import engine.newscript.DScript;
import engine.newscript.lexer.Token;
import engine.newscript.lexer.TokenType;
import engine.newscript.parser.ParseUnit;

public class VariableReplacer{
	
	// Stack for each block
	private Stack<ArrayList<String>> variables;
	private Stack<ArrayList<String>> constVariables;
	
	// Variables used in tasks that need to be preserved
	private ArrayList<Integer> preserved;
	
	
	public VariableReplacer(){
		variables		= new Stack<ArrayList<String>>();
		constVariables	= new Stack<ArrayList<String>>();
		preserved		= new ArrayList<Integer>();
	}
	
	public void process(DScript script){

		variables.clear();
		constVariables.clear();
		preserved.clear();
		pushVarList();
		
		ArrayList<Object> parseTree = script.getParseTree();
		
		for(Object o:parseTree)
			process((ParseUnit)o);
	}
	
	private void process(ParseUnit p){
		
		boolean isBlock = p.getType().equals("s_block");
		
		// Push new list for each new block
		if(isBlock)
			pushVarList();
		
		else{
			// Check if used variables exist
			replaceVariables(p);
			
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
				Object[] pCont = parent.getContents();
				
				for(int i = 0; i < pCont.length; i++){
					if(pCont[i] == p){
						pCont[i] = contents[0];
						break;
					}
				}
				
				return;
				
				
			case "assignment":
				
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
		
		boolean preserve = p.isWithin("task_block");
		
		
		switch(p.getType()){
				
			case "new_var": case "for_cond":
				Token t = (Token)contents[0];
				
				addVariable(t.getValue(), preserve);
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
					addVariable(((Token)contents[1]).getValue(), preserve);
					return;
				}
				
				// Parameter list
				ParseUnit list = (ParseUnit)contents[1];
				contents = list.getContents();
				
				for(Object o:contents)
					addVariable(((Token)((ParseUnit)o).getContents()[0]).getValue(), p.getType().equals("task_def"));
				
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
		
		int n = scope == -1 ? getVariableNumber(t.getValue()) : getVariableNumberInScope(t.getValue(), scope);
		
		contents[index] = new Token(IDENTIFIER, Integer.toString(n), t.getFile(), t.getLineNum());
	}
	
	private void addVariable(String var, boolean preserve){
		
		int n = 0;
		
		for(ArrayList<String> vars:variables)
			n += vars.size();
		
		while(preserved.contains(n))
			n++;
		
		variables.peek().add(var + "," + n);
		
		if(preserve)
			preserved.add(n);
	}
	
	private void addConstVariable(String var, char type, String value){
		constVariables.peek().add(var + "," + type + value);
	}
	
	private int getVariableNumber(String var){
		
		for(int i = variables.size() - 1; i >= 0; i--){
			
			ArrayList<String> vars = variables.get(i);
			
			for(String v:vars)
				if(getName(v).equals(var))
					return getNum(v);
		}
		
		return -1;
	}
	
	private int getVariableNumberInScope(String var, int scope){
		
		int i = variables.size() - 1 - scope;
		
		ArrayList<String> vars = variables.get(i);
		
		for(String v:vars)
			if(getName(v).equals(var))
				return getNum(v);
		
		return -1;
	}
	
	private boolean isConstantVariable(String var){
		
		for(ArrayList<String> vars:constVariables)
			for(String v:vars)
				if(getName(v).equals(var))
					return true;
		
		return false;
	}
	
	private boolean isConstantVariableInScope(String var, int scope){
		
		int i = variables.size() - 1 - scope;
		
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
		
		int i = variables.size() - 1 - scope;
		
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
