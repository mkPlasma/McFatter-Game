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
		
		// Print bytecode (debug)
		BytecodePrinter.printBytecode(bytecode, script.getFileName());
		
		// Clear tokens after
		script.clearTokens();
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
			int typeCurrent = getType(tCur);
			
			// Matched string in nextExpected
			boolean matched = false;
			
			if(nextExpected != null){
				for(String s:nextExpected){
					if(getData(tCur).equals(s) || (s.length() == 1 && getType(tCur) == s.charAt(0))){
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
			
			tCur = getData(tCur);
			
			switch(typeCurrent){
				// Keywords
				case 'k':
					switch(tCur){
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
					switch(tCur){
						case "=":
							nextExpected = null;
							states.push("exp"); // Expression
							continue;
					}
					
					// Add opeerator to expression
					if(statesPeek("exp")){
						expression.add(tCur);
						continue;
					}
					
					continue;
				
				// Separators
				case 's':
					switch(tCur){
						case ";":
							nextExpected = null;
							
							// Default variable init
							if(statesPeek("set")){
								bytecode.add(getInstruction(getOpcode("create_var"), VALUE, INT, lineNum, ZERO));
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
								expression.add(tCur);
								states.pop();
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
					// Add to variables arraylist
					if(statesPeek("var")){
						nextExpected = null;
						states.pop();
						
						// Make constant
						if(statesPeek("const")){
							tCur = scope + "c:" + tCur;
							states.pop();
						}
						else
							tCur = scope + ":" + tCur;
						
						variables.add(tCur);
						continue;
					}

					// Variable + scope + is constant
					String var = null;
					
					// Variable name only
					String varName = null;
					
					// Get variable (exclude register)
					for(int j = 1; j < variables.size(); j++){
						// Variable
						String v = variables.get(j);
						int ind = v.indexOf(':') + 1;
						
						// Name
						String vn = v.substring(0, ind);
						// Scope
						
						int sc = Integer.parseInt(v.substring(0, ind - 1).replace("c", ""));
						
						if(v.equals(tCur)){
							// If global
							if(sc == 0){
								var = v;
								varName = vn;
								
								// Break if global was requested
								if(statesPeek("global")){
									states.pop();
									break;
								}
							}
							// Otherwise if correct scope
							else if(sc == scope){
								var = v;
								varName = vn;
								break;
							}
						}
					}
					
					// If not created
					if(var == null){
						compilationError("Variable " + tCur + " is not defined", lineNum);
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
						if(typeCurrent == 'i') expression.add(Integer.parseInt(tCur));
						if(typeCurrent == 'l') expression.add(Float.parseFloat(tCur));
						if(typeCurrent == 'b') expression.add(Boolean.parseBoolean(tCur));
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
					while(!operators.isEmpty() && operators.peek() != "("){
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
		
		if(operators.peek().equals("(") || operators.peek().equals(")")){
			compilationError("Mismatched parenthesis", lineNum);
			return;
		}
		
		Object[] expression = output.toArray();
		
		// Reverse
		/*
		for(int i = 0; i < expression.length/2; i++){
			Object tmp = expression[i];
			expression[i] = expression[expression.length - i - 1];
			expression[expression.length - i - 1] = tmp;
		}
		*/
		
		for(Object obj:expression){
			// Operation
			if(obj instanceof String && isOperation((String)obj))
				bytecode.add(getInstruction(getOpcode((String)obj), ZERO, POSTFIX, lineNum, 0));
			else
				bytecode.add(getValueInst(obj, lineNum));
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
		else if(obj instanceof String)	inst = getInstruction(getOpcode("postfix_val"), VALUE, INT, lineNum, (int)obj);
		
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
			System.err.println("\nDScript compilation error:\n" + type + " in " + script.getFileName() + " on line " + lineNum +
				":\n>> " + Files.readAllLines(Paths.get(script.getPath())).get(lineNum - 1));
		}
		catch(IOException e){
			e.printStackTrace();
		}
		haltCompiler = true;
	}
}
