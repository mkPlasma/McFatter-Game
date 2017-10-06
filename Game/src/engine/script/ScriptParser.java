package engine.script;

import static engine.script.ScriptFunctions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Stack;

/*
 * 		ScriptParser.java
 * 		
 * 		Purpose:	Parses DScript tokens into bytecode.
 * 		Notes:		WIP
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				current
 * 		Changes:			
 */

public class ScriptParser{
	
	// Lexical tokens
	private String[] tokens;
	
	// Current bytecode
	private ArrayList<Long> bytecode;
	
	// Store current states
	private Stack<String> states;
	
	// Store expression while processing
	private ArrayList<Object> expression;
	
	// Stores variables by name while compiling
	private ArrayList<String> variables;
	
	// Stops compilation
	private boolean haltCompiler = false;
	
	private DScript script;
	
	public void parse(DScript script){
		
		this.script = script;
		tokens = script.getTokens();
		
		if(tokens == null){
			System.err.println("\n" + script.getFileName() + " not lexed, not parsing");
			return;
		}
		
		// Initialize/reset lists
		bytecode = new ArrayList<Long>();
		expression = new ArrayList<Object>();
		states = new Stack<String>();
		variables = new ArrayList<String>();
		
		// First variable reserved for register
		variables.add("");
		
		haltCompiler = false;
		
		// Process into bytecode
		process();
		
		if(haltCompiler)
			return;
		
		// Print bytecode (debug)
		BytecodePrinter.printBytecode(bytecode, script.getFileName());
		
		// Clear tokens after
		script.clearTokens();
		
		// Convert to array
		long[] bytecodeArray = new long[bytecode.size()];
		
		for(int i = 0; i < bytecode.size(); i++)
			bytecodeArray[i] = bytecode.get(i);
		
		script.setBytecode(bytecodeArray);
	}
	
	// Process tokens into bytecode
	private void process(){
		
		// Error if next expected doesn't match
		String[] nextExpected = null;
		
		// Keep track of brackets
		// { ( [
		int[] brackets = new int[3];
		
		// Scope assigned to variables
		int scope = 0;
		
		for(int i = 0; i < tokens.length; i++){
			
			// Is first/last token
			//boolean tFirst = i == 0;
			//boolean tLast = i == tokens.length - 1;
			//String tPrev = !tFirst ? tokens[i - 1] : null;
			//String tNext = !tLast ? tokens[i + 1] : null;
			
			// Current token
			String tCur = tokens[i];
			
			int lineNum = getLineNum(tCur);
			int type = getType(tCur);
			
			// Data
			String token = getData(tCur);
			
			// Matched string in nextExpected
			boolean matched = false;
			
			if(nextExpected != null){
				for(String s:nextExpected){
					if(token.equals(s) || (s.length() == 1 && type == s.charAt(0))){
						matched = true;
						break;
					}
				}
			}
			else matched = true;
			
			if(!matched){
				String s = nextExpected[0];

				if(s.equals("k"))		s = "keyword";
				else if(s.equals("o"))	s = "operator";
				else if(s.equals("s"))	s = "separator";
				else if(s.equals("f"))	s = "function/task";
				else if(s.equals("v"))	s = "variable";
				else if(s.equals("i"))	s = "int";
				else if(s.equals("l"))	s = "float";
				else if(s.equals("b"))	s = "boolean";
				else if(s.equals("t"))	s = "string";
				
				compilationError("Expected " + s, lineNum);
			}

			nextExpected = null;
			
			switch(type){
				// Keywords
				case 'k':
					switch(token){
						case "set":
							states.push("set");	// create_var
							states.push("var");	// Add to variables arraylist
							continue;
						
						case "const":
							if(statesPeek("var")){
								nextExpected = new String[]{"v"};
								
								// Add const before var
								String t = states.pop();
								states.push("const");
								states.push(t);
								continue;
							}
							compilationError("Invalid token \"const\"", lineNum);
							return;
						
						case "global":
							nextExpected = new String[]{"v"};
							states.push("global");
							break;
					}
					
					continue;
				
				// Operators
				case 'o':
					
					// Assignment
					switch(token){
						case "=":
							states.push("exp"); // Expression
							continue;
					}
					
					// Add opeerator to expression
					if(statesPeek("exp")){
						expression.add(token);
						continue;
					}
					
					continue;
				
				// Separators
				case 's':
					switch(token){
						case ";":
							// Default variable init
							if(statesPeek("set")){
								bytecode.add(getInstruction(getOpcode("create_var"), VALUE, INT, lineNum, ZERO));
								states.pop();
								continue;
							}
							
							// If at the end of an expression, parse
							if(statesPeek("exp")){
								parseExpression(lineNum);
								continue;
							}
							continue;
						case "(": case ")":
							if(statesPeek("exp")){
								expression.add(token);
								continue;
							}
							
							
							continue;
					}
					continue;
				
				// Functions/tasks
				case 'f':
					
					break;
				
				// Variables
				case 'v':
					// Variable + scope + is constant
					String var = null;
					
					// Variable name only
					String varName = null;
					
					boolean existsInScope = false;
					
					// Get variable (exclude register)
					for(int j = 1; j < variables.size(); j++){
						// Variable
						String v = variables.get(j);
						int ind = v.indexOf(':') + 1;
						
						// Name
						String vn = v.substring(ind);
						
						// Scope
						int sc = Integer.parseInt(v.substring(0, ind - 1).replace("c", ""));
						
						if(token.equals(vn)){
							// Otherwise if correct scope
							if(sc == scope){
								var = v;
								varName = vn;
								existsInScope = true;
								
								if(statesPeek("global")){
									if(sc != 0){
										compilationError("Global variable " + vn + " is not defined", lineNum);
										return;
									}
									else
										states.pop();
								}
								
								break;
							}
							// Otherwise if global
							else if(sc == 0){
								var = v;
								varName = vn;
								
								// Break if global was requested
								if(statesPeek("global")){
									states.pop();
									break;
								}
							}
						}
					}
					
					// Add to variables arraylist
					if(statesPeek("var")){
						
						if(existsInScope){
							compilationError("Duplicate variable " + token, lineNum);
							return;
						}
						
						// Pop "var"
						states.pop();
						
						// Make constant
						if(statesPeek("const")){
							token = scope + "c:" + token;
							states.pop();
						}
						else
							token = scope + ":" + token;
						
						nextExpected = new String[]{"=", ";"};
						
						variables.add(token);
						continue;
					}
					
					// If not created
					if(var == null){
						compilationError("Variable " + token + " is not defined", lineNum);
						return;
					}
					
					// Is variable a constant
					boolean constant = var.charAt(var.indexOf(':') - 1) == 'c';
					
					// Assignment
					if(states.isEmpty()){
						// If constant
						if(constant){
							compilationError("Variable " + varName + " is constant", lineNum);
							return;
						}
					}
					
					// Add to expression
					if(statesPeek("exp")){
						expression.add(var);
						continue;
					}
					
					continue;
				
				// Value literals
				case 'i': case 'l': case 'b':
					
					// Set variable
					if(statesPeek("exp")){
						if(type == 'i') expression.add(Integer.parseInt(token));
						if(type == 'l') expression.add(Float.parseFloat(token));
						if(type == 'b') expression.add(Boolean.parseBoolean(token));
						continue;
					}
					continue;
				
				// String literals
				case 't':
					
					continue;
			}
		}
	}
	
	// Parse expression, convert to postfix
	private void parseExpression(int lineNum){
		
		if(expression.isEmpty()){
			compilationError("Invalid expression", lineNum);
			return;
		}
		
		// If single value
		if(expression.size() == 1){
			
			long inst = getValueInst(expression.get(0), lineNum);
			
			switch(statesPeek()){
				case "exp":
					inst = setOpcode(inst, getOpcode("create_var"));
				break;
			}
			
			states.pop();	// Pop "exp"
			expression.clear();
			bytecode.add(inst);
			return;
		}
		
		// Postfix stacks
		Stack<Object> output = new Stack<Object>();
		Stack<String> operators = new Stack<String>();
		
		// Convert to postfix
		for(int i = 0; i < expression.size(); i++){
			Object obj = expression.get(i);
			
			// Operations
			if(obj instanceof String && isOperation((String)obj)){
				while(!operators.isEmpty() && getPrecedence(operators.peek()) >= getPrecedence((String)obj)){
					output.push(operators.pop());
				}
				operators.push((String)obj);
				continue;
			}
			
			// Parenthesis
			if(obj instanceof String && (((String)obj).equals("(") || ((String)obj).equals(")"))){
				
				if(((String)obj).equals("("))
					operators.push((String)obj);
				else{
					while(!operators.isEmpty() && !operators.peek().equals("(")){
						output.push(operators.pop());
					}
					if(operators.isEmpty()){
						compilationError("Mismatched parenthesis", lineNum);
						return;
					}
					
					operators.pop();
				}
				
				continue;
			}
			
			// Literals/variables
			output.push(obj);
		}
		
		expression.clear();
		
		if(operators.peek().equals("(") || operators.peek().equals(")")){
			compilationError("Mismatched parenthesis", lineNum);
			return;
		}
		
		while(!operators.isEmpty())
			output.push(operators.pop());

		// Convert to bytecode
		Object[] expression = output.toArray();
		
		for(Object obj:expression){
			// Operation
			if(obj instanceof String && isOperation((String)obj))
				bytecode.add(getInstruction(getOpcode((String)obj), ZERO, POSTFIX, lineNum, 0));
			else
				bytecode.add(getValueInst(obj, lineNum));
		}
		
		// End expression
		bytecode.add(getInstruction(getOpcode("postfix_end"), ZERO, ZERO, lineNum, 0));
		
		states.pop();	// Pop "exp"
		
		switch(statesPeek()){
			case "set":
				// Create variable from register
				bytecode.add(getInstruction(getOpcode("create_var"), VARIABLE, ZERO, lineNum, 0));
				return;
		}
	}
	
	// Returns instruction for single value
	private long getValueInst(Object obj, int lineNum){
		long inst = 0;
		
		// Value literals
		if(obj instanceof Integer)		inst = getInstruction(getOpcode("postfix_val"), VALUE, INT, lineNum, (int)obj);
		else if(obj instanceof Float)	inst = getInstruction(getOpcode("postfix_val"), VALUE, FLOAT, lineNum, Float.floatToIntBits((float)obj));
		else if(obj instanceof Boolean)	inst = getInstruction(getOpcode("postfix_val"), VALUE, BOOLEAN, lineNum, (boolean)obj ? 1 : 0);
		
		// Variable
		else if(obj instanceof String)	inst = getInstruction(getOpcode("postfix_val"), VARIABLE, INT, lineNum, variables.indexOf(obj));
		
		return inst;
	}
	
	// Peek states stack
	private boolean statesPeek(String state){
		return statesPeek().equals(state);
	}
	
	private String statesPeek(){
		return states.isEmpty() ? "" : states.peek();
	}
	
	// States stack first
	private boolean statesFirst(String state){
		return statesFirst().equals(state);
	}
	
	private String statesFirst(){
		return states.isEmpty() ? "" : states.firstElement();
	}
	
	// Create syntax error and halt compilation
	private void compilationError(String type, int lineNum){
		try{
			System.err.println("\nDScript compilation error (parser):\n" + type + " in " + script.getFileName() + " on line " + lineNum +
				":\n>> " + Files.readAllLines(Paths.get(script.getPath())).get(lineNum - 1));
		}
		catch(IOException e){
			e.printStackTrace();
		}
		haltCompiler = true;
	}
}
