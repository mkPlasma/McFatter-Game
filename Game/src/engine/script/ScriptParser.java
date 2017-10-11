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
	private ArrayList<ArrayList<Object>> expressions;
	
	// Temp bytecode expression storage for for loops
	// For loop -> Argument -> Expression bytecode
	private ArrayList<ArrayList<ArrayList<Long>>> tmpExp;
	private int tmpExpInd;
	
	// Stores variables by name while compiling
	private ArrayList<String> variables;
	private String createVar;
	private ArrayList<String> forVars;
	private boolean forLess;
	
	// Store functions by name
	private ArrayList<String> functions;
	
	// Function names when called
	private ArrayList<String> funcNames;
	
	// Function call bytecode holder when in expression
	private ArrayList<ArrayList<Long>> funcBc;
	
	// Number of parenthesis during function call
	private ArrayList<Integer> funcBrackets;
	
	// Count function parameters
	private ArrayList<Integer> funcParams;
	
	// For functions within expressions
	private int expDepth;
	private int funcCallNum;
	
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
		expressions	= new ArrayList<ArrayList<Object>>();
		tmpExp		= new ArrayList<ArrayList<ArrayList<Long>>>();
		states		= new Stack<String>();
		variables	= new ArrayList<String>();
		forVars		= new ArrayList<String>();
		functions	= new ArrayList<String>();
		funcNames	= new ArrayList<String>();
		funcBc		= new ArrayList<ArrayList<Long>>();
		funcBrackets= new ArrayList<Integer>();
		funcParams	= new ArrayList<Integer>();
		funcSkip	= new ArrayList<Integer>();
		
		tmpExpInd = -1;
		
		loopNum = 0;
		forLoopNum = -1;
		
		expDepth = 0;
		funcCallNum = -1;
		
		funcArgs = 0;
		fSkipNum = 0;
		
		// First variable reserved for register
		variables.add("");
		
		expressions.add(new ArrayList<Object>());
		
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

		// TODO: make read global variables before function
		// TODO: turn function arraylists into stacks
		// TODO: make function return stack in scriptrunner
		
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
							tmpExpInd++;
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
						expressions.get(expDepth).add(token);
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
							expressions.get(expDepth).add(statesPeek(1));
							// Add operation
							expressions.get(expDepth).add(token.replace("=", ""));
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
							if(statesPeek("exp")){
								if(statesPeek("for_args", 1)){
									if(tmpExp.get(tmpExpInd).size() > 2){
										compilationError("For loop has at most 3 arguments", lineNum);
										return;
									}
									
									if(tmpExp.get(tmpExpInd).size() >= 2){
										Object o = expressions.get(expDepth).get(0);
										
										// If counter is negative
										if((o instanceof Integer && ((int)o) < 0) || (o instanceof Float && ((float)o) < 0) ||
											(o instanceof String) && ((String)o).equals("-"))
											forLess = true;
									}
									
									// Store expression
									tmpExp.get(tmpExpInd).add(parseExpression(lineNum));
									
									states.add("exp");
									
									continue;
								}
								
								// Function call
								else if(statesPeek("func_args", 1)){
									
									if(!statesPeek("func_call", 2)){
										compilationError("Invalid function call definition", lineNum);
										return;
									}
									
									if(expDepth == 0){
										bytecode.addAll(parseExpression(lineNum));
										funcParams.set(0, funcParams.get(0) + 1);
									}
									else{
										funcBc.get(expDepth - 1).addAll(parseExpression(lineNum));
										funcParams.set(funcCallNum, funcParams.get(funcCallNum) + 1);
									}
									
									states.push("exp");
								}
							}
							
							// Function arguments
							else if(statesPeek("func_args")){
								// Define parameter
								variables.add(createVar);
								bytecode.add(getInstruction("create_var", lineNum, variables.size() - 1));
								bytecode.add(getInstruction("get_param", lineNum));
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
									if(tmpExp.get(tmpExpInd).size() == 3){
										ArrayList<Long> bc = tmpExp.get(tmpExpInd).get(1);
										
										bc.add(0, getInstruction("exp_val", VARIABLE, lineNum, forVarIndex));
										bc.add(bc.size() - 1, getInstruction("add", lineNum));
										bc.add(bc.size(), getInstruction("store", lineNum, forVarIndex));
										
										bytecode.addAll(bc);
									}
									else{
										bytecode.add(getInstruction("increment", lineNum, forVarIndex));
									}
									
									tmpExp.get(tmpExpInd).clear();
									
									// Add loop point
									bytecode.add(getInstruction("end_while", lineNum, loopNum));
									
									// Delete counter
									bytecode.add(getInstruction("delete_var", lineNum, forVarIndex));
									
									loopNum--;
									forLoopNum--;
									tmpExpInd--;
									tmpExp.remove(tmpExp.size() - 1);
									forVars.remove(forVars.size() - 1);
								}
								else{
									if(statesPeek("func_body")){
										inFunction = false;
										funcSkip.add(i + 1);
										states.pop();
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
										if(tmpExp.get(tmpExpInd).size() > 2){
											compilationError("For loop has at most 3 arguments", lineNum);
											return;
										}
										
										// Unless specified, loop for i < num
										if(tmpExp.get(tmpExpInd).size() != 2)
											forLess = true;
										
										// Store expression
										tmpExp.get(tmpExpInd).add(parseExpression(lineNum));
										
										// Create variable
										forVars.add(createVar);
										variables.add(createVar);
										bytecode.add(getInstruction("create_var", lineNum, variables.size() - 1));
										
										// Assign
										if(tmpExp.get(tmpExpInd).size() > 1){
											ArrayList<Long> bc = tmpExp.get(tmpExpInd).get(0);
											
											// Single value
											if(bc.size() == 2){
												bytecode.add(setOpcode(bc.get(0), "load"));
											}
											else{
												bytecode.addAll(tmpExp.get(tmpExpInd).get(0));
											}
											
											bytecode.add(getInstruction("store", lineNum, variables.size() - 1));
										}
										
										// Loop point
										bytecode.add(getInstruction("while", lineNum, loopNum));
										
										// Condition
										ArrayList<Long> bc = tmpExp.get(tmpExpInd).get(tmpExp.get(tmpExpInd).size() - 1);
										
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
								
								// Function call
								if(!open && funcBrackets.get(funcCallNum) == brackets[1] && statesPeek("func_args", 1) && statesPeek("func_call", 2)){
									
									// Check if no args
									if(!getData(tokens[i - 1]).equals("(")){
										if(expDepth == 0){
											bytecode.addAll(parseExpression(lineNum));
											funcParams.set(0, funcParams.get(0) + 1);
										}
										else{
											funcBc.get(expDepth - 1).addAll(parseExpression(lineNum));
											funcParams.set(funcCallNum, funcParams.get(funcCallNum) + 1);
										}
									}
									
									states.pop();
									states.pop();
									
									// Get function
									String func = funcNames.get(funcCallNum) + ":" + funcParams.get(funcCallNum);
									
									int funcIndex = -1;
									
									for(int j = 0; j < functions.size(); j++){
										// Variable
										String f = functions.get(j);
										int ind = f.indexOf(':') + 1;
										
										// Name + arg count
										String fn = f.substring(ind);
										
										// Scope
										int sc = Integer.parseInt(f.substring(0, ind - 1));
										
										if(func.equals(fn)){
											
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
											
											if(inScope){
												funcIndex = j;
												break;
											}
										}
									}
									
									if(expDepth == 0)
										bytecode.add(getInstruction("call_func", lineNum, funcIndex));
									else{
										funcBc.get(expDepth - 1).add(getInstruction("call_func", lineNum, funcIndex));
										expressions.remove(expDepth);
										expDepth--;
									}

									funcBrackets.remove(funcCallNum);
									funcParams.remove(funcCallNum);
									funcCallNum--;
									
									continue;
								}
								
								// Standard parenthesis
								expressions.get(expDepth).add(token);
								continue;
							}
							
							// Function definition arguments
							if(!open && brackets[1] == 0 && (statesPeek("var") || statesPeek("exp")) && statesPeek("func_args", 1)){
								
								// Pop "var" and "func_args"
								states.pop();
								states.pop();
								
								// Function declaration
								if(statesPeek("func_def")){
									
									// Check if args are empty
									if(!getData(tokens[i - 1]).equals("(")){
										// Define parameter
										variables.add(createVar);
										bytecode.add(getInstruction("create_var", lineNum, variables.size() - 1));
										bytecode.add(getInstruction("get_param", lineNum));
									}
									else
										funcArgs = 0;
									
									// Add number of args
									functions.set(functions.size() - 1, functions.get(functions.size() - 1) + ":" + funcArgs);
									
									// Check if function exists (exclude current function)
									for(int j = 0; j < functions.size() - 1; j++){
										// Variable
										String f = variables.get(j);
										int ind = f.indexOf(':') + 1;
										
										// Name + arg count
										String fn = f.substring(ind);
										
										// Scope
										int sc = Integer.parseInt(f.substring(0, ind - 1));
										
										if(functions.get(functions.size() - 1).equals(fn)){
											
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
											
											if(inScope){
												compilationError("Duplicate function", lineNum);
												return;
											}
										}
									}
									
									requireAfter = "{";
									states.pop();
									states.push("func_body");
								}
							}
							
							continue;
					}
					continue;
				
				
				
				// Function/task call
				case 'f':
					if(!stAccept && !statesPeek("func_def") && !statesPeek("exp")){
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
						// If in expression
						if(statesPeek("exp")){
							// Add placeholder in expression
							// Later replaced by function call bytecode
							expressions.get(expDepth).add("@" + expDepth);
							
							funcBc.add(new ArrayList<Long>());
							
							expDepth++;
							expressions.add(new ArrayList<Object>());
						}
						
						funcNames.add(token);
						
						funcBrackets.add(brackets[1]);
						
						funcParams.add(0);
						funcCallNum++;
						
						states.push("func_call");
						states.push("func_args");
						states.push("exp");
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
							// For loop variable
							if(statesPeek("for_args")){
								requireAfter = "in";
								token = (scope + 1) + ":" + token;
							}
							
							// Function definition parameter
							else if(statesPeek("func_args") && statesPeek("func_def", 1)){
								requireAfter = ",";
								token = (scope + 1) + ":" + token;
							}
							else{
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
						expressions.get(expDepth).add(var);
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
						if(type == 'i') expressions.get(expDepth).add(Integer.parseInt(token));
						if(type == 'l') expressions.get(expDepth).add(Float.parseFloat(token));
						if(type == 'b') expressions.get(expDepth).add(Boolean.parseBoolean(token));
						continue;
					}
					
					compilationErrorIV(token, lineNum);
					return;
				
				
				
				// String literals
				case 't':
					
					continue;
			}
		}
		
		//if(functionDef)
		//	BytecodePrinter.printBytecode(bytecode, "functions");
	}
	
	// Parse expression, convert to postfix
	private ArrayList<Long> parseExpression(int lineNum){
		
		if(expressions.get(expDepth).isEmpty()){
			compilationError("Invalid expression", lineNum);
			return null;
		}
		
		states.pop();	// Pop "exp"
		
		// Generated bytecode
		ArrayList<Long> bc = new ArrayList<Long>();
		
		// Check if single negative number
		if(expressions.get(expDepth).size() == 3){
			if(expressions.get(expDepth).get(0) instanceof Integer && (int)expressions.get(expDepth).get(0) == 0 &&
				expressions.get(expDepth).get(1) instanceof String && ((String)expressions.get(expDepth).get(1)).equals("-")){
				
				Object obj = expressions.get(expDepth).get(2);
				expressions.get(expDepth).clear();
				
				if(obj instanceof Integer)
					expressions.get(expDepth).add(-(int)obj);
				else
					expressions.get(expDepth).add(-(float)obj);
			}
		}
		
		// If single value
		if(expressions.get(expDepth).size() == 1){
			
			if(statesPeek("for_args"))
				bc.add(getValueInst(expressions.get(expDepth).get(0), lineNum));
			else
				bc.add(setOpcode(getValueInst(expressions.get(expDepth).get(0), lineNum), "load"));
			
			expressions.get(expDepth).clear();
			
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
				
				// Function call
				case "func_args":
					states.pop();
					
					if(!statesPeek("func_call", 1)){
						compilationError("Invalid function call expression", lineNum);
						bc.clear();
						return bc;
					}
					
					// If function call is in expression
					if(expDepth > 0)
						
					
					bc.add(getInstruction("set_param", lineNum));
					
					return bc;
			}
			
			bc.clear();
			return bc;
		}
		
		
		
		
		
		// Postfix stacks
		Stack<Object> output = new Stack<Object>();
		Stack<String> operators = new Stack<String>();
		
		// Convert to postfix
		for(int i = 0; i < expressions.get(expDepth).size(); i++){
			Object obj = expressions.get(expDepth).get(i);
			
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
			
			// Literals/variables/function calls
			output.push(obj);
		}
		
		expressions.get(expDepth).clear();
		
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
			
			// Function calls
			else if(obj instanceof String && ((String)obj).charAt(0) == '@'){
				int num = Integer.parseInt(((String)obj).substring(1));
				bc.addAll(funcBc.get(num));
			}
			
			// Literals/variables
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
			
			// Function call
			case "func_args":
				states.pop();
				
				if(!statesPeek("func_call", 1)){
					compilationError("Invalid function call definition", lineNum);
					bc.clear();
					return bc;
				}
				
				// If function call is in expression
				if(expDepth > 0)
					bc.add(getInstruction("exp_inc", lineNum));
					
				bc.add(getInstruction("set_param", lineNum));
				
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
