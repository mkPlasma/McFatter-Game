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
	
	// Temp bytecode expression storage for loop/function call
	private ArrayList<ArrayList<ArrayList<Long>>> tmpExp;
	private boolean forLess;
	
	// Stores variables by name while compiling
	private ArrayList<String> variables;
	private String createVar;
	private ArrayList<String> forVars;
	
	// Store functions by name
	private ArrayList<String> functions;
	
	// Skip functions after defining
	private ArrayList<Integer> funcSkip;
	private int funcArgs;
	private int fSkipNum;
	
	// Loop index
	private int loopNum;
	private int forLoopNum;
	
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
		bytecode	= new ArrayList<Long>();
		expression	= new ArrayList<Object>();
		tmpExp		= new ArrayList<ArrayList<ArrayList<Long>>>();
		states		= new Stack<String>();
		variables	= new ArrayList<String>();
		forVars		= new ArrayList<String>();
		functions	= new ArrayList<String>();
		funcSkip	= new ArrayList<Integer>();
		
		loopNum = 0;
		forLoopNum = -1;
		
		funcArgs = 0;
		fSkipNum = 0;
		
		// First variable reserved for register
		variables.add("");
		
		haltCompiler = false;
		
		// Define functions
		process(true);
		// Process into bytecode
		process(false);
		
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
	private void process(boolean functionDef){
		
		// Keep track of brackets
		// { ( [
		int[] brackets = new int[3];
		
		// Scope assigned to variables
		int scope = 0;
		
		// Used for calculating current scope
		ArrayList<Integer> scopePrev = new ArrayList<Integer>();
		int scopeHighest = 0;
		int scopeDepth = 0;
		
		// Keep track of which scope is in which
		ArrayList<Integer> scopeParent = new ArrayList<Integer>();
		
		// Error if required token is not found
		String requireAfter = null;
		
		// Allow else keyword after if/else if statement
		boolean allowElse = false;
		boolean resetAllowElse = false;
		
		// Keep track of loops
		int loopTotal = 0;
		
		boolean inFunction = false;
		
		for(int i = 0; i < tokens.length; i++){
			
			// Skip defined functions
			if(funcSkip.size() > (fSkipNum*2)){
				if(!functionDef && i == funcSkip.get(fSkipNum*2)){
					while(++i != funcSkip.get((fSkipNum*2) + 1));
					fSkipNum++;
					
					if(i >= tokens.length)
						return;
				}
			}
			
			// Current token
			String tCur = tokens[i];
			
			int lineNum = getLineNum(tCur);
			int type = getType(tCur);
			
			// Data
			String token = getData(tCur);
			
			
			// Check only function definitions
			if(functionDef && !inFunction){
				if(token.equals("function"))
					inFunction = true;
				else
					continue;
			}
			
			if(requireAfter != null && !token.equals(requireAfter)){
				compilationError("Expected " + requireAfter, lineNum);
				return;
			}
			
			requireAfter = null;
			
			if(resetAllowElse)
				allowElse = false;
			resetAllowElse = allowElse;
			
			// Within () or []
			boolean inBrackets = brackets[1] != 0 || brackets[2] != 0;
			
			// Most conditions should only be accepted if true
			boolean stAccept = !inBrackets && (statesPeek("") || statesPeek("else") || statesPeek("if_body") ||
				statesPeek("while_body") || statesPeek("for_body") || statesPeek("func_body"));
			
			switch(type){
				
				
				
				// Keywords
				case 'k':
					if(!stAccept && !token.equals("global") && !token.equals("in")){
						compilationErrorIV(token, lineNum);
						return;
					}
					
					switch(token){
						case "set":
							states.push("create");	// create_var
							states.push("var");		// Add to variables arraylist
							continue;
						
						case "const":
							if(statesPeek("var")){
								// Add const before var
								String t = states.pop();
								states.push("const");
								states.push(t);
								continue;
							}
							compilationErrorIV(token, lineNum);
							return;
						
						case "global":
							states.push("global");
							continue;
						
						case "if":
							// Else if
							if(statesPeek("else"))
								bytecode.add(getInstruction("else_ahead", lineNum));
							
							states.push("if_cond");
							continue;
						
						case "else":
							if(!allowElse){
								compilationError("Else must follow if statement", lineNum);
								return;
							}
							
							states.push("else");
							continue;
						
						case "while":
							loopNum = loopTotal;
							loopTotal++;
							
							states.push("while_cond");
							bytecode.add(getInstruction("while", lineNum, loopNum));
							continue;
						
						case "for":
							loopNum = loopTotal;
							loopTotal++;
							
							forLoopNum++;
							tmpExp.add(new ArrayList<ArrayList<Long>>());
							
							states.push("for_args");
							continue;
						
						case "in":
							if(brackets[2] != 0){
								compilationErrorIV(token, lineNum);
								return;
							}
							
							// For argument expression
							states.push("exp");
							continue;
						
						case "function":
							states.push("func_def");
							funcArgs = 0;
							funcSkip.add(i);
							continue;
					}
					
					continue;
				
				
				
				// Operators
				case 'o':
					
					// Add to expression
					if(statesPeek("exp")){
						expression.add(token);
						continue;
					}
					compilationErrorIV(token, lineNum);
					return;
				
				
				
				// Assignment operators
				case 'a':
					if(inBrackets || statesPeek("exp") || (!statesPeek("assign") && !statesPeek("create"))){
						compilationErrorIV(token, lineNum);
						return;
					}
					
					if(token.contains("=")){
						
						// For +=, /=, etc.
						if(!token.equals("=")){
							// Add var name
							expression.add(statesPeek(1));
							// Add operation
							expression.add(token.replace("=", ""));
						}

						states.push("exp"); // Expression
						
						continue;
					}
					
					// Increment/Decrement
					states.pop();
					bytecode.add(getInstruction(token.equals("++") ? "increment" : "decrement", lineNum, variables.indexOf(states.pop())));
					
					continue;
				
				
				
				// Separators
				case 's':
					switch(token){
						case ";":
							if(inBrackets){
								compilationErrorIV(token, lineNum);
								return;
							}
							
							// Default variable init
							if(statesPeek("create")){
								variables.add(createVar);
								bytecode.add(getInstruction("create_var", lineNum, variables.size() - 1));
								states.pop();
								continue;
							}
							
							// If at the end of an expression, parse
							if(statesPeek("exp")){
								bytecode.addAll(parseExpression(lineNum));
								continue;
							}
							continue;
						
						case ",":
							// For loop argument
							if(statesPeek("exp") && statesPeek("for_args", 1)){
								if(tmpExp.get(forLoopNum).size() > 2){
									compilationError("For loop has at most 3 arguments", lineNum);
									return;
								}
								
								if(tmpExp.get(forLoopNum).size() >= 2){
									Object o = expression.get(0);
									
									// If counter is negative
									if((o instanceof Integer && ((int)o) < 0) || (o instanceof Float && ((float)o) < 0) ||
										(o instanceof String) && ((String)o).equals("-"))
										forLess = true;
								}
								
								// Store expression
								tmpExp.get(forLoopNum).add(parseExpression(lineNum));
								
								states.add("exp");
								
								continue;
							}
							
							// Function arguments
							else if(statesPeek("func_args")){
								// Define parameter
								variables.add(createVar);
								bytecode.add(getInstruction("get_param", lineNum, variables.size() - 1));
								funcArgs++;
							}
							
							compilationErrorIV(token, lineNum);
							
							return;
						
						case "{": case "}":
							boolean open = token.equals("{");

							if(open) brackets[0]++;
							else	 brackets[0]--;
							
							
							// Set scope/close block
							if(open){
								// Array
								if(statesPeek("create") || statesPeek("assign")){}
								
								// Else
								else if(statesPeek("else")){
									states.pop();
									bytecode.add(getInstruction("else", lineNum));
								}
								else if(!statesPeek("") && !statesPeek("else") && !statesPeek("if_body") &&
									!statesPeek("while_body") && !statesPeek("for_body") && !statesPeek("func_body")){
									compilationErrorIV(token, lineNum);
									return;
								}
								
								
								// Set current scope parent
								scopeParent.add(scope);
								
								// Set scope
								scopeHighest++;
								scope = scopeHighest;
								
								// Record scope
								if(scopePrev.size() < scopeDepth + 1)
									scopePrev.add(scope);
								else
									scopePrev.set(scopeDepth, scope);
								
								scopeDepth++;
							}
							else{
								// Delete variables created in this scope
								for(int j = 1; j < variables.size(); j++){
									String v = variables.get(j);
									int ind = v.indexOf(':') + 1;
									int sc = Integer.parseInt(v.substring(0, ind - 1).replace("c", ""));
									
									if(sc == scope && !forVars.isEmpty() && !v.equals(forVars.get(forLoopNum)))
										bytecode.add(getInstruction("delete_var", lineNum, j));
								}
								
								// Set scope back
								scopeDepth--;
								scope = scopeDepth == 0 ? 0 : scopePrev.get(scopeDepth - 1);
								
								// Clear upper list
								for(int j = scopeDepth; j < scopePrev.size(); j++)
									scopePrev.set(j, 0);
								
								// End block
								if(statesPeek("if_body")){
									states.pop();
									bytecode.add(getInstruction("end_else_if", lineNum));
									bytecode.add(getInstruction("end", lineNum));
									allowElse = true;
									continue;
								}
								else if(statesPeek("while_body")){
									states.pop();
									bytecode.add(getInstruction("end_while", lineNum, loopNum));
									loopNum--;
								}
								else if(statesPeek("for_body")){
									states.pop();
									
									int forVarIndex = variables.indexOf(forVars.get(forLoopNum));
									
									// Add variable expression
									if(tmpExp.get(forLoopNum).size() == 3){
										ArrayList<Long> bc = tmpExp.get(forLoopNum).get(1);
										
										bc.add(0, getInstruction("exp_val", VARIABLE, lineNum, forVarIndex));
										bc.add(bc.size() - 1, getInstruction("add", lineNum));
										bc.add(bc.size(), getInstruction("store", lineNum, forVarIndex));
										
										bytecode.addAll(bc);
									}
									else{
										bytecode.add(getInstruction("increment", lineNum, forVarIndex));
									}
									
									tmpExp.get(forLoopNum).clear();
									
									// Add loop point
									bytecode.add(getInstruction("end_while", lineNum, loopNum));
									
									// Delete counter
									bytecode.add(getInstruction("delete_var", lineNum, forVarIndex));
									
									loopNum--;
									forLoopNum--;
									tmpExp.remove(tmpExp.size() - 1);
									forVars.remove(forVars.size() - 1);
								}
								else{
									if(statesPeek("func_body")){
										inFunction = false;
										funcSkip.add(i + 1);
									}
									
									bytecode.add(getInstruction("end", lineNum));
								}
							}
							
							continue;
						
						case "(": case ")":
							open = token.equals("(");
							
							if(open) brackets[1]++;
							else	 brackets[1]--;
							
							// if/while condition
							if(open && (statesPeek("if_cond") || statesPeek("while_cond"))){
								states.push("exp");
								continue;
							}
							else if(open && (statesPeek("for_args") || statesPeek("func_args"))){
								states.push("var");
							}
							
							if(statesPeek("exp")){
								
								// End of if(), for(), etc
								if(!open && brackets[1] == 0){
									
									// End if/while condition
									if((statesPeek("if_cond", 1) || statesPeek("while_cond", 1))){
										boolean cIf = statesPeek("if_cond", 1);
										boolean elseIf = statesPeek("else", 2);
										
										bytecode.addAll(parseExpression(lineNum));
										requireAfter = "{";
										
										if(cIf)
											states.push("if_body");
										else
											states.push("while_body");
										
										bytecode.add(getInstruction(elseIf ? "else_if" : "if", lineNum));
										
										continue;
									}
									
									// For loop argument
									else if(statesPeek("for_args", 1)){
										if(tmpExp.get(forLoopNum).size() > 2){
											compilationError("For loop has at most 3 arguments", lineNum);
											return;
										}
										
										// Unless specified, loop for i < num
										if(tmpExp.get(forLoopNum).size() != 2)
											forLess = true;
										
										// Store expression
										tmpExp.get(forLoopNum).add(parseExpression(lineNum));
										
										// Create variable
										forVars.add(createVar);
										variables.add(createVar);
										bytecode.add(getInstruction("create_var", lineNum, variables.size() - 1));
										
										// Assign
										if(tmpExp.get(forLoopNum).size() > 1){
											ArrayList<Long> bc = tmpExp.get(forLoopNum).get(0);
											
											// Single value
											if(bc.size() == 2){
												bytecode.add(setOpcode(bc.get(0), "load"));
											}
											else{
												bytecode.addAll(tmpExp.get(forLoopNum).get(0));
											}
											
											bytecode.add(getInstruction("store", lineNum, variables.size() - 1));
										}
										
										// Loop point
										bytecode.add(getInstruction("while", lineNum, loopNum));
										
										// Condition
										ArrayList<Long> bc = tmpExp.get(forLoopNum).get(tmpExp.get(forLoopNum).size() - 1);
										
										// Add comparison to expression
										bc.add(0, getInstruction("exp_val", VARIABLE, lineNum, variables.size() - 1));
										bc.add(bc.size() - 1, getInstruction(forLess ? "less" : "greater", lineNum));
										
										// Add comparison
										bytecode.addAll(bc);
										
										// Add branch
										bytecode.add(getInstruction("if", lineNum));
										
										requireAfter = "{";
										
										states.pop();
										states.push("for_body");
										
										continue;
									}
								}
								
								expression.add(token);
								continue;
							}
							
							// Function arguments
							if(!open && brackets[1] == 0 && statesPeek("var") && statesPeek("func_args", 1)){
								
								// Check if args are empty
								if(!getData(tokens[i - 1]).equals("(")){
									// Define parameter
									variables.add(createVar);
									bytecode.add(getInstruction("get_param", lineNum, variables.size() - 1));
								}
								else
									funcArgs = 0;
								
								// Add number of args
								functions.set(functions.size() - 1, functions.get(functions.size() - 1) + ":" + funcArgs);
								
								requireAfter = "{";
								
								// Pop "var" and "func_args"
								states.pop();
								states.pop();
								
								// Function declaration
								if(statesPeek("func_def")){
									states.push("func_body");
								}
								
								// Function call
								else if(statesPeek("func_call")){
									
								}
								else{
									compilationError("Invalid function given", lineNum);
									return;
								}
							}
							
							continue;
					}
					continue;
				
				
				
				// Function/task call
				case 'f':
					if(!stAccept && !statesPeek("func_def")){
						compilationErrorIV(token, lineNum);
						return;
					}

					// Add function
					if(statesPeek("func_def")){
						functions.add(scope + ":" + token);
						bytecode.add(getInstruction("function", lineNum, functions.size() - 1));
						
						states.push("func_args");
					}
					
					// Function call
					else{
						states.push("func_call");
						states.push("func_args");
					}
					
					continue;
				
				
				
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
							
							// Check if in scope
							boolean inScope = sc == scope;
							int last = scope;
							
							// Check if in parent
							if(!inScope){
								int sc2 = last == 0 ? 0 : scopeParent.get(last - 1);
								
								while(sc2 != 0){
									sc2 = last == 0 ? 0 : scopeParent.get(last - 1);
									
									if(sc2 == sc){
										inScope = true;
										break;
									}
									last = sc2;
								}
							}

							// If global
							if(sc == 0){
								var = v;
								varName = vn;
								
								if(scope == 0 && !statesPeek("for_args", 1))
									existsInScope = true;
								
								// Break if global was requested
								if(statesPeek("global")){
									states.pop();
									break;
								}
							}
							
							// Otherwise if correct scope
							else if(inScope){
								var = v;
								varName = vn;
								existsInScope = true;
								
								if(statesPeek("global")){
									if(sc != 0){
										compilationError("Global variable " + vn + " is not defined", lineNum);
										return;
									}
									else{
										states.pop();
										break;
									}
								}
								
								break;
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
						else{
							if(statesPeek("for_args")){
								requireAfter = "in";
								token = (scope + 1) + ":" + token;
							}
							else{
								if(statesPeek("func_args"))
									requireAfter = ",";
								
								token = scope + ":" + token;
							}
						}
						
						// Add variable after expression so
						// set x = x; is invalid
						createVar = token;
						continue;
					}
					
					// If not created
					if(var == null){
						compilationError("Variable " + token + " is not defined", lineNum);
						return;
					}
					
					// Add to expression
					if(statesPeek("exp")){
						expression.add(var);
						continue;
					}
					
					if(!states.isEmpty() && !statesPeek("if_body") && !statesPeek("while_body") && !statesPeek("for_body") && !statesPeek("func_body")){
						compilationErrorIV(token, lineNum);
						return;
					}
					
					// Is variable a constant
					boolean constant = var.charAt(var.indexOf(':') - 1) == 'c';
					
					// Assignment
					// If constant
					if(constant){
						compilationError("Variable " + varName + " is constant", lineNum);
						return;
					}

					states.push(var);
					states.push("assign");
					
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
					
					compilationErrorIV(token, lineNum);
					return;
				
				
				
				// String literals
				case 't':
					
					continue;
			}
		}
		
		if(functionDef)
			BytecodePrinter.printBytecode(bytecode, "functions");
	}
	
	// Parse expression, convert to postfix
	private ArrayList<Long> parseExpression(int lineNum){
		
		if(expression.isEmpty()){
			compilationError("Invalid expression", lineNum);
			return null;
		}
		
		states.pop();	// Pop "exp"
		
		// Generated bytecode
		ArrayList<Long> bc = new ArrayList<Long>();
		
		// Check if single negative number
		if(expression.size() == 3){
			if(expression.get(0) instanceof Integer && (int)expression.get(0) == 0 &&
			   expression.get(1) instanceof String && ((String)expression.get(1)).equals("-")){
				
				Object obj = expression.get(2);
				expression.clear();
				
				if(obj instanceof Integer)
					expression.add(-(int)obj);
				else
					expression.add(-(float)obj);
			}
		}
		
		// If single value
		if(expression.size() == 1){
			
			if(statesPeek("for_args"))
				bc.add(getValueInst(expression.get(0), lineNum));
			else
				bc.add(setOpcode(getValueInst(expression.get(0), lineNum), "load"));
			
			expression.clear();
			
			switch(statesPeek()){
				
				// If statement condition
				case "if_cond":
					// Pop "if"
					states.pop();
					
					// Pop "else"
					if(statesPeek("else"))
						states.pop();
					
					return bc;
				
				// While loop condition
				case "while_cond":
					// Branch with if, loop back with while
					states.pop();
					return bc;
				
				// For loop argument
				case "for_args":
					bc.add(getInstruction("exp_end", lineNum));
					return bc;
				
				// Creating variable
				case "create":
					// Create variable and store value
					variables.add(createVar);
					
					bc.add(getInstruction("create_var", lineNum, variables.size() - 1));
					bc.add(getInstruction("store", lineNum, variables.size() - 1));
					states.pop();
					
					return bc;
				
				// Assignment
				case "assign":
					// Pop "assign"
					states.pop();

					// Get variable and pop
					int i = variables.indexOf(states.pop());
					
					// Store variable
					bc.add(getInstruction("store", lineNum, i));
					
					return bc;
			}
			
			bc.clear();
			return bc;
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
						bc.clear();
						return bc;
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
			bc.clear();
			return bc;
		}
		
		while(!operators.isEmpty())
			output.push(operators.pop());

		// Convert to bytecode
		Object[] expression = output.toArray();
		
		for(Object obj:expression){
			// Operation
			if(obj instanceof String && isOperation((String)obj))
				bc.add(getInstruction((String)obj, lineNum));
			else
				bc.add(getValueInst(obj, lineNum));
		}
		
		// End expression
		bc.add(getInstruction("exp_end", lineNum));
		
		
		
		
		switch(statesPeek()){
			
			// If statement condition
			case "if_cond":
				// Pop "if"
				states.pop();
				
				// Pop "else"
				if(statesPeek("else"))
					states.pop();
				
				return bc;
			
			// While loop condition
			case "while_cond":
				// Branch with if, loop back with while
				states.pop();
				return bc;
			
			// For loop argument
			case "for_args":
				return bc;
			
			// Creating variable
			case "create":
				// Create variable and store value
				variables.add(createVar);
				
				bc.add(getInstruction("create_var", lineNum, variables.size() - 1));
				bc.add(getInstruction("store", lineNum, variables.size() - 1));
				states.pop();
				
				return bc;
			
			// Assignment
			case "assign":
				// Pop "assign"
				states.pop();
				
				// Get variable and pop
				int i = variables.indexOf(states.pop());
				
				// Store variable
				bc.add(getInstruction("store", lineNum, i));
				
				return bc;
		}

		bc.clear();
		return bc;
	}
	
	// Returns instruction for single value
	private long getValueInst(Object obj, int lineNum){
		long inst = 0;
		
		// Value literals
		if(obj instanceof Integer)		inst = getInstruction("exp_val", VALUE, INT, lineNum, (int)obj);
		else if(obj instanceof Float)	inst = getInstruction("exp_val", VALUE, FLOAT, lineNum, Float.floatToIntBits((float)obj));
		else if(obj instanceof Boolean)	inst = getInstruction("exp_val", VALUE, BOOLEAN, lineNum, (boolean)obj ? 1 : 0);
		
		// Variable
		else if(obj instanceof String)	inst = getInstruction("exp_val", VARIABLE, INT, lineNum, variables.indexOf(obj));
		
		return inst;
	}
	
	// Peek states stack
	private String statesPeek(int depth){
		return states.size() - 1 < depth ? "" : states.get(states.size() - 1 - depth);
	}

	private String statesPeek(){
		return states.isEmpty() ? "" : states.peek();
	}

	private boolean statesPeek(String state){
		return statesPeek().equals(state);
	}
	
	private boolean statesPeek(String state, int depth){
		return statesPeek(depth).equals(state);
	}
	
	// Create syntax error and halt compilation
	private void compilationError(String type, int lineNum){
		try{
			System.err.println("\nDScript compilation error (parser):\n" + type + " in " + script.getFileName() + " on line " + lineNum +
				":\n>> " + Files.readAllLines(Paths.get(script.getPath())).get(lineNum - 1).trim());
		}
		catch(IOException e){
			e.printStackTrace();
		}
		haltCompiler = true;
	}
	
	private void compilationErrorIV(String token, int lineNum){
		compilationError("Invalid token \"" + token + "\"", lineNum);
	}
}
