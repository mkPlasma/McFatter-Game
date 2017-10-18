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
	private Stack<ArrayList<Object>> expressions;
	
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
	private Stack<String> funcNames;
	
	// Bytecode holder for functions/arrays in expression
	private Stack<ArrayList<Long>> tempBc;
	
	// Number of parenthesis during function call
	private Stack<Integer> funcBrackets;
	
	// Count function parameters
	private Stack<Integer> funcParams;
	
	// Number of arguments in function definition
	private int funcArgs;
	
	// Keep track of loops
	int loopTotal;
	
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
		expressions	= new Stack<ArrayList<Object>>();
		tmpExp		= new ArrayList<ArrayList<ArrayList<Long>>>();
		states		= new Stack<String>();
		variables	= new ArrayList<String>();
		forVars		= new ArrayList<String>();
		functions	= new ArrayList<String>();
		funcNames	= new Stack<String>();
		tempBc		= new Stack<ArrayList<Long>>();
		funcBrackets= new Stack<Integer>();
		funcParams	= new Stack<Integer>();
		
		tmpExpInd = -1;
		
		loopNum = -1;
		forLoopNum = -1;
		loopTotal = -1;
		
		funcArgs = 0;
		
		expressions.add(new ArrayList<Object>());
		
		haltCompiler = false;

		// First variable reserved for register
		variables.add("");
		
		// Define functions
		process(true);
		
		if(haltCompiler)
			return;
		
		// Remove bytecode and variables from function definition
		bytecode.clear();
		variables.clear();
		variables.add("");
		
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
		
		// Error if required token is not found
		String[] requireAfter = null;
		
		// Allow else keyword after if/else if statement
		boolean allowElse = false;
		boolean resetAllowElse = false;
		
		boolean allowSquareBracket = false;
		boolean resetAllowSquareBracket = false;
		
		boolean allowDot = false;
		boolean resetAllowDot = false;
		
		// Variable being added to expression
		String expVar = null;
		
		// Index of function definition
		int funcNum = 0;
		
		// Scope assigned to variables
		int scope = 0;
		int scopeDepth = 0;
		
		// Used for calculating current scope
		ArrayList<Integer> scopePrev = new ArrayList<Integer>();
		int scopeHighest = 0;
		
		// Keep track of which scope is in which
		ArrayList<Integer> scopeParent = new ArrayList<Integer>();
		
		// Swich array element bytecode placeholder
		Stack<Boolean> arrayElemSwitch = new Stack<Boolean>();
		
		for(int i = 0; i < tokens.length; i++){
			
			// Current token
			String tCur = tokens[i];
			
			int lineNum = getLineNum(tCur);
			int type = getType(tCur);
			
			// Data
			String token = getData(tCur);
			
			// Check if required token was found
			if(requireAfter != null){
				boolean matched = false;
				
				for(String s:requireAfter){
					if(token.equals(s))
						matched = true;
				}
				
				if(!matched){
					compilationError("Expected " + requireAfter[0], lineNum);
					return;
				}
			}
			
			requireAfter = null;

			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println(token);
			System.out.println(states);
			for(int j = 0; j < expressions.size(); j++)
				System.out.print(expressions.get(j));
			
			if(!tempBc.isEmpty())
				BytecodePrinter.printBytecode(tempBc.peek(), "tempbc " + tempBc.size());
			
			// Check if not array item
			if(expVar != null && !token.equals("[") && !token.equals(".") && type != 'a'){
				expressions.peek().add(expVar);
				expVar = null;
			}
			
			if(resetAllowElse) allowElse = false;
			resetAllowElse = allowElse;
			
			if(resetAllowSquareBracket) allowSquareBracket = false;
			resetAllowSquareBracket = allowSquareBracket;
			
			if(resetAllowDot) allowDot = false;
			resetAllowDot = allowDot;
			
			// Within () or []
			boolean inBrackets = brackets[1] != 0 || brackets[2] != 0;
			
			// Most conditions should only be accepted if true
			boolean stAccept = !inBrackets && (statesPeek("") || statesPeek("else") || statesPeek("if_body") ||
				statesPeek("while_body") || statesPeek("for_body") || statesPeek("func_body"));
			
			switch(type){
				
				
				
				// Keywords
				case 'k':
					if(!stAccept && !token.equals("global") && !token.equals("in")){
						compilationErrorIT(token, lineNum);
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
							compilationErrorIT(token, lineNum);
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
								compilationErrorIT(token, lineNum);
								return;
							}
							
							// For argument expression
							states.push("exp");
							continue;
						
						case "function":
							states.push("func_def");
							funcArgs = 0;
							continue;
						
						case "return":
							if(!states.contains("func_body")){
								compilationError("Return must be in function", lineNum);
								return;
							}
							
							states.push("return");
							states.push("exp");
							
							continue;
					}
					
					continue;
				
				
				
				// Operators
				case 'o':
					
					// Add to expression
					if(statesPeek("exp")){
						expressions.peek().add(token);
						continue;
					}
					compilationErrorIT(token, lineNum);
					return;
				
				
				
				// Assignment operators
				case 'a':
					if(inBrackets || statesPeek("exp") || (!statesPeek("assign") && !statesPeek("create"))){
						compilationErrorIT(token, lineNum);
						return;
					}
					
					expVar = null;
					
					if(token.contains("=")){
						
						// For +=, /=, etc.
						if(!token.equals("=")){
							
							if(statesPeek("create")){
								compilationErrorIT(token, lineNum);
								return;
							}
							
							// Array assign
							if(statesPeek("assign_array_pre", 2))
								states.add(states.size() - 3, token.replaceAll("=", ""));
							
							// Variable assign
							else{
								// Add var name
								expressions.peek().add(statesPeek(1));
								// Add operation
								expressions.peek().add(token.replace("=", ""));
							}
						}
						
						if(statesPeek("assign_array_pre", 2)){
							states.pop();
							states.pop();
							states.pop();
							states.push("assign_array");
							states.push("assign");
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
								compilationErrorIT(token, lineNum);
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
								
								// Void return statement
								if(statesPeek("return", 1) && expressions.peek().isEmpty()){
									bytecode.add(getInstruction("return_void", lineNum));
									
									// Pop "exp" and "return"
									states.pop();
									states.pop();
									continue;
								}
								
								bytecode.addAll(parseExpression(lineNum));
								continue;
							}
							continue;
						
						case ",":
							if(statesPeek("exp")){
								
								// For loop argument
								if(statesPeek("for_args", 1)){
									if(tmpExp.get(tmpExpInd).size() > 2){
										compilationError("For loop has at most 3 arguments", lineNum);
										return;
									}
									
									if(tmpExp.get(tmpExpInd).size() >= 2){
										Object o = expressions.peek().get(0);
										
										// If counter is negative
										if((o instanceof Integer && ((int)o) < 0) || (o instanceof Float && ((float)o) < 0) ||
											(o instanceof String) && o.equals("-"))
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
									
									if(expressions.size() == 1)
										bytecode.addAll(parseExpression(lineNum));
									else{
										// Must do this otherwise funcBc.peek() will be the wrong item
										ArrayList<Long> bc = parseExpression(lineNum);
										tempBc.peek().addAll(bc);
									}
									
									funcParams.push(funcParams.pop() + 1);
									
									states.push("exp");
									
									continue;
								}
								
								// Array item
								else if(statesPeek("array", 1)){
									tempBc.peek().addAll(parseExpression(lineNum));
									states.push("exp");
									continue;
								}
							}
							
							// Function arguments
							else if(statesPeek("func_args")){
								// Define parameter
								variables.add(createVar);
								bytecode.add(getInstruction("create_var", lineNum, variables.size() - 1));
								bytecode.add(getInstruction("get_param", lineNum, variables.size() - 1));
								funcArgs++;
								
								states.push("var");
								
								continue;
							}
							
							compilationErrorIT(token, lineNum);
							return;
						
						case ".":
							
							if(!allowDot){
								compilationErrorIT(token, lineNum);
								return;
							}
							
							if(statesPeek("exp")){
								expressions.peek().add("@");
								tempBc.push(new ArrayList<Long>());
							}
							
							// Dot after variable
							if(expVar != null){
								
								// Pop "assign" and var name
								if(statesPeek("assign")){
									states.pop();
									states.pop();
								}
								
								long inst = getInstruction("load", VARIABLE, lineNum, variables.indexOf(expVar));
								
								if(statesPeek("exp"))
									tempBc.peek().add(inst);
								else
									bytecode.add(inst);
								
								expVar = null;
							}
							
							// Dot after function
							else{
								long inst = getInstruction("load_r", lineNum);

								if(statesPeek("exp"))
									tempBc.peek().add(inst);
								else
									bytecode.add(inst);
							}
							
							long inst = getInstruction("dot", lineNum);
							
							if(statesPeek("exp"))
								tempBc.peek().add(inst);
							else
								bytecode.add(inst);
							
							states.push("dot");
							
							BytecodePrinter.printBytecode(tempBc.peek(), "dot " + tempBc.size());
							
							continue;
						
						case "{": case "}":
							boolean open = token.equals("{");

							if(open) brackets[0]++;
							else	 brackets[0]--;
							
							
							// Set scope/close block
							if(open){
								// Array
								if(statesPeek("exp")){
									
									if(statesPeek("array", 1)){
										compilationError("Cannot add array to array", lineNum);
										return;
									}
									
									// Placeholder for bytecode
									expressions.peek().add("#");
									
									// Use new expression for paramters
									expressions.add(new ArrayList<Object>());
									tempBc.push(new ArrayList<Long>());
									
									states.add("array");
									states.add("exp");
									continue;
								}
								
								// Else
								else if(statesPeek("else")){
									states.pop();
									bytecode.add(getInstruction("else", lineNum));
								}
								else if(!statesPeek("") && !statesPeek("else") && !statesPeek("if_body") &&
									!statesPeek("while_body") && !statesPeek("for_body") && !statesPeek("func_body")){
									compilationErrorIT(token, lineNum);
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
								
								// Array end
								if(statesPeek("exp") && statesPeek("array", 1)){
									
									// Parse values if not empty
									if(!expressions.peek().isEmpty())
										tempBc.peek().addAll(parseExpression(lineNum));
									
									// Pop "exp"
									else
										states.pop();
									
									tempBc.peek().add(getInstruction("array_end", lineNum));
									expressions.pop();
									states.pop();
									
									allowSquareBracket = true;
									continue;
								}
								
								// Delete variables created in this scope
								for(int j = 1; j < variables.size(); j++){
									String v = variables.get(j);
									int ind = v.indexOf(':') + 1;
									int sc = Integer.parseInt(v.substring(0, ind - 1).replace("c", ""));
									
									if(sc == scope && (forVars.isEmpty() || (!forVars.isEmpty() && !v.equals(forVars.get(forLoopNum)))))
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
									if(statesPeek("func_body"))
										states.pop();
									
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
								if(statesPeek("func_call", 1))
									states.push("exp");
								else
									states.push("var");
								continue;
							}
							
							if(statesPeek("exp")){
								
								// End of if(), for(), etc
								if(!open && brackets[1] == 0){
									
									// End if/while condition
									if((statesPeek("if_cond", 1) || statesPeek("while_cond", 1))){
										boolean cIf = statesPeek("if_cond", 1);
										boolean elseIf = statesPeek("else", 2);
										
										bytecode.addAll(parseExpression(lineNum));
										requireAfter = new String[]{"{"};
										
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
										
										requireAfter = new String[]{"{"};
										
										states.pop();
										states.push("for_body");
										
										continue;
									}
								}
								
								// Function call
								if(!open && (!funcBrackets.isEmpty() && funcBrackets.peek() == brackets[1]) &&
									statesPeek("func_args", 1) && statesPeek("func_call", 2)){

									// No args
									if(expressions.peek().isEmpty()){
										// Pop "exp"
										states.pop();
									}
									else{
										if(expressions.size() == 1 && !statesPeek("dot"))
											bytecode.addAll(parseExpression(lineNum));
										else{
											// Must do this otherwise funcBc.peek() will be the wrong item
											ArrayList<Long> bc = parseExpression(lineNum);
											tempBc.peek().addAll(bc);
										}
										
										funcParams.push(funcParams.pop() + 1);
									}
									
									// Pop "func_args" and "func_call"
									states.pop();
									states.pop();
									
									// Get function
									String func = funcNames.pop() + ":" + funcParams.pop();
									
									// Check if is built in function
									int funcIndex = getBuiltInFunctionIndex(func);
									boolean builtIn = funcIndex != -1;
									
									System.out.println(func);
									
									// If not, check defined functions
									if(funcIndex == -1){
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
												boolean inScope = sc == scope || sc == 0;
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
									}
									
									// Do not check if functions are being defined
									if(funcIndex == -1 && !functionDef){
										compilationError("Undefined function (wrong parameter count/scope?)", lineNum);
										return;
									}
									
									// After dot separator
									if(statesPeek("dot")){
										tempBc.peek().add(getInstruction("call_func_b", lineNum, funcIndex));
										
										ArrayList<Long> bc = tempBc.pop();
										tempBc.peek().addAll(bc);

										expressions.pop();
										states.pop();
									}
									
									else if(expressions.size() == 1)
										bytecode.add(getInstruction(builtIn ? "call_func_b" : "call_func", lineNum, funcIndex));
									else{
										tempBc.peek().add(getInstruction(builtIn ? "call_func_b" : "call_func", lineNum, funcIndex));
										expressions.pop();
									}
									
									funcBrackets.pop();
									allowSquareBracket = true;
									allowDot = true;
									
									continue;
								}
								
								// Standard parenthesis
								expressions.peek().add(token);
								continue;
							}
							
							// Function definition arguments
							if(!open && brackets[1] == 0 && (statesPeek("func_args") || (statesPeek("var") && statesPeek("func_args", 1)))){
								
								// If no args
								boolean empty = statesPeek("var");
								
								// Pop "var"
								if(empty)
									states.pop();
								
								// Pop "func_args"
								states.pop();
								
								if(!statesPeek("func_def")){
									compilationError("Invalid function definition", lineNum);
									return;
								}
								
								// Pop "func_def"
								states.pop();
								
								// No args
								if(empty)
									funcArgs = 0;
								else{
									// Define parameter
									variables.add(createVar);
									bytecode.add(getInstruction("create_var", lineNum, variables.size() - 1));
									bytecode.add(getInstruction("get_param", lineNum, variables.size() - 1));
									funcArgs++;
								}
								
								requireAfter = new String[]{"{"};
								states.push("func_body");

								// Add to functions list only if defining
								if(!functionDef)
									continue;
								
								// Add number of args
								String func = functions.get(functions.size() - 1) + ":" + funcArgs;
								functions.set(functions.size() - 1, func);
								
								func = func.substring(func.indexOf(':') + 1);
								
								if(getBuiltInFunctionIndex(func) != -1){
									compilationError("Duplicate built-in function", lineNum);
									return;
								}
								
								// Check if function exists (exclude current function)
								for(int j = 0; j < functions.size() - 1; j++){
									// Variable
									String f = functions.get(j);
									int ind = f.indexOf(':') + 1;
									
									// Name + arg count
									String fn = f.substring(ind);
									
									// Scope
									int sc = Integer.parseInt(f.substring(0, ind - 1));
									
									if(func.equals(fn)){
										
										// Check if in scope
										boolean inScope = sc == scope || sc == 0;
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
							}
							
							continue;
						
						case "[": case "]":
							
							open = token.equals("[");
							
							if(open){
								if(!allowSquareBracket){
									compilationErrorIT(token, lineNum);
									return;
								}
								
								// Add new temp bytecode holder
								tempBc.push(new ArrayList<Long>());
								
								// Array element in expression
								if(statesPeek("exp")){
									// Array variable
									if(expVar != null){
										tempBc.peek().add(getInstruction("array_load", lineNum, variables.indexOf(expVar)));
										arrayElemSwitch.push(false);
									}
									
									// Array direct/from function return
									else{
										// From function
										if(getData(tokens[i - 1]).equals(")"))
											tempBc.peek().add(getInstruction("load_r", lineNum));
										
										tempBc.peek().add(getInstruction("array_load", lineNum, 0));
										
										arrayElemSwitch.push(true);
									}
									
									// Add placeholder
									expressions.peek().add(arrayElemSwitch.peek() ? "$" :"#");
									expressions.add(new ArrayList<Object>());
								}
								
								// Assign array element
								else{
									// Two required, one for element as a whole, second for expression inside
									tempBc.push(new ArrayList<Long>());
									
									// Pop "assign"
									states.pop();
									
									// Load variable array
									tempBc.peek().add(getInstruction("array_load", VARIABLE, lineNum, variables.indexOf(states.pop())));
									
									states.push("assign_array_pre");
									states.push("assign");
									
									expressions.add(new ArrayList<Object>());
									
									arrayElemSwitch.push(false);
								}
								
								// Reset variable
								expVar = null;
								
								states.push("array_elem");
								states.push("exp");
								
								continue;
							}
							// End expression and parse
							else{
								// Must do this otherwise funcBc.peek() will be the wrong item
								ArrayList<Long> bc = parseExpression(lineNum);
								tempBc.peek().addAll(bc);
								
								// Switch top 2 bytecode holders if referencing array directly or function call
								if(arrayElemSwitch.pop()){
									bc = tempBc.pop();
									tempBc.add(tempBc.size() - 1, bc);
								}
								
								expressions.pop();
							}
							
							continue;
						
					}
					continue;
				
				
				
				// Function/task call
				case 'f':
					if(!stAccept && !statesPeek("func_def") && !statesPeek("exp") && !statesPeek("dot")){
						compilationErrorIT(token, lineNum);
						return;
					}
					
					// Add function
					if(statesPeek("func_def")){
						if(functionDef)
							functions.add(scope + ":" + token);
						
						bytecode.add(getInstruction("function", lineNum, funcNum));
						funcNum++;
						
						states.push("func_args");
						continue;
					}
					
					// Function call
					
					// If in expression
					if(statesPeek("exp") || statesPeek("dot")){
						// Add placeholder in expression
						// Later replaced by function call bytecode
						if(statesPeek("exp"))
							expressions.peek().add("@");
						
						tempBc.push(new ArrayList<Long>());
						
						// Use new expression for paramters
						expressions.add(new ArrayList<Object>());
					}
					
					funcNames.push(token);
					
					funcBrackets.push(brackets[1]);
					
					funcParams.push(0);
					
					states.push("func_call");
					states.push("func_args");
					
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
								
								if(scope == 0 && !statesPeek("for_args", 1) && !statesPeek("func_args", 1))
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
								requireAfter = new String[]{"in"};
								token = (scopeHighest + 1) + ":" + token;
							}
							
							// Function definition parameter
							else if(statesPeek("func_args") && statesPeek("func_def", 1)){
								requireAfter = new String[]{",", ")"};
								token = (scopeHighest + 1) + ":" + token;
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
						// Store variable before adding to check for array item
						allowSquareBracket = true;
						allowDot = true;
						expVar = var;
						continue;
					}
					
					if(!states.isEmpty() && !statesPeek("if_body") && !statesPeek("while_body") && !statesPeek("for_body") && !statesPeek("func_body")){
						compilationErrorIT(token, lineNum);
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
					
					allowSquareBracket = true;
					allowDot = true;
					expVar = var;
					
					states.push(var);
					states.push("assign");
					
					continue;
				
				
				
				// Value literals
				case 'i': case 'l': case 'b':
					
					// Set variable
					if(statesPeek("exp")){
						if(type == 'i') expressions.peek().add(Integer.parseInt(token));
						if(type == 'l') expressions.peek().add(Float.parseFloat(token));
						if(type == 'b') expressions.peek().add(Boolean.parseBoolean(token));
						continue;
					}
					
					compilationErrorIT(token, lineNum);
					return;
				
				
				
				// String literals
				case 't':
				
			}
		}
		
		if(states.size() > 0)
			System.err.println("\nDScript parser warning:\nStates stack size of " + states.size() + " after parsing");
	}
	
	// Parse expression, convert to postfix
	private ArrayList<Long> parseExpression(int lineNum){
		
		ArrayList<Object> exp = expressions.peek();
		
		// Generated bytecode
		ArrayList<Long> bc = new ArrayList<Long>();
		
		if(exp.isEmpty()){
			compilationError("Empty expression", lineNum);
			return bc;
		}
		
		states.pop();	// Pop "exp"
		
		
		// Check if single negative number
		if(exp.size() == 3){
			if(exp.get(0) instanceof Integer && (int)exp.get(0) == 0 &&
				exp.get(1) instanceof String && exp.get(1).equals("-")){
				
				Object obj = exp.get(2);
				exp.clear();
				
				if(obj instanceof Integer)
					exp.add(-(int)obj);
				else
					exp.add(-(float)obj);
			}
		}
		
		// If single value (exclude placeholders)
		if(exp.size() == 1){
			
			Object obj = exp.get(0);
			
			boolean getValue = true;
			
			// Placeholders
			if(obj instanceof String){
				char c = ((String)obj).charAt(0);
				
				if(c == '@' || c == '#'){
					bc.addAll(tempBc.pop());
					
					if(c == '@'){
						if(statesPeek("for_args"))
							bc.add(getInstruction("exp_val_r", lineNum));
						else
							bc.add(getInstruction("load_r", lineNum));
					}
					
					getValue = false;
				}
			}
			
			if(getValue){
				if(statesPeek("for_args"))
					bc.add(getValueInst(obj, lineNum));
				else
					bc.add(setOpcode(getValueInst(obj, lineNum), "load"));
			}
			
			exp.clear();
			
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
					
					String st = states.pop();
					
					// Assign array element
					if(st.equals("assign_array")){
						// Store value of assigned expression
						bc.add(getInstruction("array_val", lineNum));
						
						// Add bytecode to set array element
						bc.addAll(tempBc.pop());
						
						// If operation replace last instruction
						if(isOperation(statesPeek())){
							String op = states.pop();
							bc.remove(bc.size() - 1);
							
							switch(op){
								case "+":
									bc.add(getInstruction("array_elem_a", lineNum));
									break;
								case "-":
									bc.add(getInstruction("array_elem_u", lineNum));
									break;
								case "*":
									bc.add(getInstruction("array_elem_m", lineNum));
									break;
								case "/":
									bc.add(getInstruction("array_elem_d", lineNum));
									break;
								case "%":
									bc.add(getInstruction("array_elem_o", lineNum));
									break;
							}
						}
					}
					
					// Get variable and pop
					else{
						int i = variables.indexOf(st);
						
						// Store variable
						bc.add(getInstruction("store", lineNum, i));
					}
					
					return bc;
				
				// Function call
				case "func_args":
					
					if(!statesPeek("func_call", 1)){
						compilationError("Invalid function call expression", lineNum);
						bc.clear();
						return bc;
					}
					
					bc.add(getInstruction("set_param", lineNum));
					
					return bc;
				
				// Return value
				case "return":
					bc.add(getInstruction("return", lineNum));
					states.pop();
					return bc;
				
				// Array item
				case "array":
					bc.add(getInstruction("array_val", lineNum));
					return bc;
				
				// Get array element
				case "array_elem":
					
					states.pop();
					
					// Assign array element
					if(statesPeek("assign")){
						bc.addAll(tempBc.pop());
						bc.add(getInstruction("array_elem_s", lineNum));
						
						states.pop();
						states.push("assign_array");
						states.push("assign");
					}
					
					// Array element in expression
					else
						bc.add(getInstruction("array_elem", lineNum));
					
					return bc;
			}
			
			bc.clear();
			return bc;
		}
		
		
		
		
		
		// Postfix stacks
		Stack<Object> output = new Stack<Object>();
		Stack<String> operators = new Stack<String>();
		
		// Convert to postfix
		for(int i = 0; i < exp.size(); i++){
			Object obj = exp.get(i);
			
			// Operations
			if(obj instanceof String && isOperation((String)obj)){
				while(!operators.isEmpty() && getPrecedence(operators.peek()) >= getPrecedence((String)obj)){
					output.push(operators.pop());
				}
				operators.push((String)obj);
				continue;
			}
			
			// Parenthesis
			if(obj instanceof String && (obj.equals("(") || obj.equals(")"))){
				
				if(obj.equals("("))
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
		
		if(!operators.isEmpty() && (operators.peek().equals("(") || operators.peek().equals(")"))){
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
			if(obj instanceof String && isOperation((String)obj)){
				bc.add(getInstruction((String)obj, lineNum));
				continue;
			}
				
			// Bytecode placeholder
			if(obj instanceof String){
				char c = ((String)obj).charAt(0);
				
				if(c == '@' || c == '#' || c == '$'){
					
					// Add bytecode
					if(c != '$')
						bc.addAll(tempBc.pop());
					
					// Return value
					if(c == '@')
						bc.add(getInstruction("exp_val_r", lineNum));
					
					// Array value
					else if(c == '#')
						bc.add(getInstruction("exp_val", VARIABLE, lineNum, 0));
					
					// Array element after array or function call
					else if(c == '$'){
						// Need to remove exp_val
						bc.remove(bc.size() - 1);
						
						// Add bytecode
						bc.addAll(tempBc.pop());
						bc.add(getInstruction("exp_val", VARIABLE, lineNum, 0));
					}
					
					continue;
				}
			}
			
			// Literals/variables
			bc.add(getValueInst(obj, lineNum));
		}
		
		// End expression
		bc.add(getInstruction("exp_end", lineNum));
		
		exp.clear();
		
		
		
		
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
				
				String st = states.pop();
				
				// Assign array element
				if(st.equals("assign_array")){
					// Store value of assigned expression
					bc.add(getInstruction("array_val", lineNum));
					
					// Add bytecode to set array element
					bc.addAll(tempBc.pop());
					
					// If operation replace last instruction
					if(isOperation(statesPeek())){
						String op = states.pop();
						bc.remove(bc.size() - 1);
						
						switch(op){
							case "+":
								bc.add(getInstruction("array_elem_a", lineNum));
								break;
							case "-":
								bc.add(getInstruction("array_elem_u", lineNum));
								break;
							case "*":
								bc.add(getInstruction("array_elem_m", lineNum));
								break;
							case "/":
								bc.add(getInstruction("array_elem_d", lineNum));
								break;
							case "%":
								bc.add(getInstruction("array_elem_o", lineNum));
								break;
						}
					}
				}
				
				// Get variable and pop
				else{
					int i = variables.indexOf(st);
					
					// Store variable
					bc.add(getInstruction("store", lineNum, i));
				}
				
				return bc;
			
			
			// Function call
			case "func_args":
				
				if(!statesPeek("func_call", 1)){
					compilationError("Invalid function call definition", lineNum);
					bc.clear();
					return bc;
				}
				
				// If function call is in expression
				if(expressions.size() > 1){
					bc.add(0, getInstruction("exp_inc", lineNum));
					bc.add(getInstruction("exp_dec", lineNum));
				}
				
				bc.add(getInstruction("set_param", lineNum));
				
				return bc;
			
			// Return value
			case "return":
				bc.add(getInstruction("return", lineNum));
				states.pop();
				return bc;
			
			// Array item
			case "array":
				// If array is in expression
				if(expressions.size() > 1){
					bc.add(0, getInstruction("exp_inc", lineNum));
					bc.add(getInstruction("exp_dec", lineNum));
				}
				
				bc.add(getInstruction("array_val", lineNum));
				return bc;
				
			// Get array element
			case "array_elem":
				
				states.pop();
				
				// Assign array element
				if(statesPeek("assign")){
					bc.addAll(tempBc.pop());
					bc.add(getInstruction("array_elem_s", lineNum));
					
					states.pop();
					states.push("assign_array");
					states.push("assign");
				}
				
				// Array element in expression
				else
					bc.add(getInstruction("array_elem", lineNum));
				
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
	
	private void compilationErrorIT(String token, int lineNum){
		compilationError("Invalid token \"" + token + "\"", lineNum);
	}
}
