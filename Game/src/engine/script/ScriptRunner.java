package engine.script;

import static engine.script.ScriptFunctions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

/*
 * 		ScriptRunner.java
 * 		
 * 		Purpose:	Runs DScript bytecode.
 * 		Notes:		WIP
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				current
 * 		Changes:			
 */

public class ScriptRunner{
	
	// Stops script
	private boolean haltRun = false;
	
	// Used for undefined/deleted variables
	// Switch to (byte)0 to save memory
	private final Object NULL = null;
	
	// Variables
	private Object[] variables;
	
	// Used to evaluate postfix expressions
	private Stack<ArrayList<Object>> expressions;
	
	// Function points
	private ArrayList<Integer> functions;
	
	// Function parameters
	private Stack<Queue<Object>> funcParams;
	
	// Store return points for function calls
	private Stack<Integer> returnPoints;
	
	// Return value for functions
	private Object returnValue;
	
	private long[] bytecode;
	
	private DScript script;
	
	public void run(DScript script){
		this.script = script;

		// Load bytecode
		bytecode = script.getBytecode();
		
		if(bytecode == null){
			System.err.println("\n" + script.getFileName() + " not compiled, not running");
			return;
		}
		
		haltRun = false;
		
		// Initialize/reset exoression
		expressions = new Stack<ArrayList<Object>>();
		expressions.push(new ArrayList<Object>());

		functions = new ArrayList<Integer>();
		funcParams = new Stack<Queue<Object>>();
		returnPoints = new Stack<Integer>();
		
		// Account for register
		int varCount = 1;
		
		// Add variables, find functions
		for(int i = 0; i < bytecode.length; i++){
			String op = opcodes[getOpcode(bytecode[i])];
			
			if(op.equals("create_var"))
				varCount++;
			else if(op.equals("function"))
				functions.add(i);
		}
		
		variables = new Object[varCount];
		
		for(int i = 0; i < variables.length; i++)
			variables[i] = NULL;
		
		// Go into else block
		boolean doElse = false;
		boolean resetDoElse = false;
		boolean elseAhead = false;
		
		// Loop through
		for(int i = 0; i < bytecode.length; i++){
			
			// Instruction properties
			long inst = bytecode[i];
			String opcode = opcodes[getOpcode(inst)];
			boolean isVar = isVariable(inst);
			int lineNum = getLineNum(inst);
			int data = getData(inst);
			
			// Set doElse to false after one loop
			// Preserve if elseAhead
			if(!elseAhead){
				if(resetDoElse)
					doElse = false;
				resetDoElse = doElse;
			}
			else
				resetDoElse = false;
			
			// Check operations first
			if(isOperation(opcode)){
				opcode = getOperation(opcode);
				
				//if(type == POSTFIX)
				operate(opcode, lineNum);
				//else operation(opcode, isVar, type, data, lineNum);
				
				if(haltRun)
					return;
				
				continue;
			}
			
			// Other checks
			switch(opcode){
				
				
				
				case "none":
					runtimeWarning("Opcode \"none\" found", lineNum);
					continue;
				
				
				
				case "load":
					if(isVar)
						variables[0] = variables[data];
					
					else{
						switch(getType(inst)){
							case INT:
								variables[0] = data;
								continue;
							case FLOAT:
								variables[0] = Float.intBitsToFloat(data);
								continue;
							case BOOLEAN:
								variables[0] = data == 1;
								continue;
						}
					}
					continue;
				
				
				
				case "store":
					variables[data] = variables[0];
					continue;
				
				
				
				case "create_var":
					variables[data] = 0;
					continue;
				
				
				
				case "delete_var":
					//variables[data] = NULL;
					continue;
				
				
				
				case "exp_val":
					
					// Variables
					if(isVar)
						expressions.peek().add(variables[data]);
					
					// Single values
					else{
						switch(getType(inst)){
							case INT:
								expressions.peek().add(data);
								continue;
							case FLOAT:
								expressions.peek().add(Float.intBitsToFloat(data));
								continue;
							case BOOLEAN:
								expressions.peek().add(data == 1);
								continue;
						}
					}
					continue;
				
				
				
				case "exp_val_r":
					if(returnValue == null){
						runtimeError("Null or void return value", lineNum);
						return;
					}
					
					expressions.peek().add(returnValue);
					continue;
				
				
				
				case "exp_inc":
					expressions.push(new ArrayList<Object>());
					continue;
				
				
				
				case "exp_dec":
					expressions.pop();
					continue;
				
				
				
				case "exp_end":
					if(expressions.peek().size() != 1){
						runtimeWarning("Expression stack size " + expressions.peek().size() + " on expression end", lineNum);
						return;
					}
					
					// Save to register
					variables[0] = expressions.peek().get(0);
					
					// Clear expression
					expressions.peek().clear();
					
					continue;
				
				
				
				case "increment":{// Brackets required as not to leak o and n
					Object o = variables[data];
					
					// Check type
					if(!(o instanceof Integer) && !(o instanceof Float)){
						runtimeError("Type mismatch", lineNum);
						return;
					}
					
					// Cast
					float n = o instanceof Float ? (float) o : (float)((int) o);
					
					// Set data
					if(o instanceof Float)
						variables[data] = n + 1;
					else
						variables[data] = (int)n + 1;
					
					continue;
				}
				
				
				
				case "decrement":{// Brackets required as not to leak o and n
					Object o = variables[data];
					
					// Check type
					if(!(o instanceof Integer) && !(o instanceof Float)){
						runtimeError("Type mismatch", lineNum);
						return;
					}
					
					// Cast
					float n = o instanceof Float ? (float) o : (float)((int) o);
					
					// Set data
					if(o instanceof Float)
						variables[data] = n - 1;
					else
						variables[data] = (int)n - 1;
					
					continue;
				}
				
				
				
				case "if": case "else_if":
					if(!(variables[0] instanceof Boolean)){
						runtimeError("Type mismatch", lineNum);
						return;
					}
					
					boolean elseIf = opcode.equals("else_if");
					
					// Reset elseAhead
					if(elseIf)
						elseAhead = false;
					
					// Continue if true, skip to end if false
					if(!(boolean)variables[0] || (elseIf && !doElse)){
						i = skipToEnd(i, false);
						doElse = true;
					}
					
					continue;
				
				case "else":
					if(!doElse)
						i = skipToEnd(i, false);
					continue;
				
				case "else_ahead":
					elseAhead = true;
					continue;
				
				case "end_else_if":
					// Skip past else/else if statements
					i = skipToEnd(i, true);
					continue;
				
				case "end_while":{
					
					int whileNum = data;
					int whileNumC = 0;
					String op2 = "";
					
					// Loop back to while
					while(!op2.equals("while") || whileNumC != whileNum){
						i--;
						op2 = opcodes[getOpcode(bytecode[i])];
						whileNumC = getData(bytecode[i]);
					}
					
					continue;
				}
				
				// Skip function definitions
				case "function":
					i = skipToEnd(i, false);
					continue;
				
				case "call_func":
					// Use new expression
					expressions.push(new ArrayList<Object>());
					
					// Add return point
					returnPoints.push(i);
					
					// Jump to function
					i = functions.get(data);
					
					continue;
				
				case "set_param":
					// New parameters
					if(funcParams.isEmpty())
						funcParams.push(new LinkedList<Object>());
					
					funcParams.peek().add(variables[0]);
					continue;
				
				case "get_param":
					variables[data] = funcParams.peek().remove();
					
					// Remove once all parameters are taken
					if(funcParams.peek().isEmpty())
						funcParams.pop();
					
					continue;
				
				// End instruction should be reached only at the end of a function
				// It is skipped by if/else if statements
				case "end": case "return_void": case "return":
					// Remove expression
					expressions.pop();
					
					// Return
					i = returnPoints.pop();
					
					if(opcode.equals("return"))
						returnValue = variables[0];
					else
						returnValue = null;
					
					continue;
			}
		}
		
		System.out.println("\nResults of " + script.getFileName() + ":\n");
		
		for(int i = 0; i < variables.length; i++)
			System.out.println((i == 0 ? "Register" : "Variable " + i) + ": " + variables[i]);
		
	}
	
	// Skip to end of block, returns new i
	private int skipToEnd(int i, boolean skipElseIf){
		int depth = 1;
		
		while(depth > 0 && i < bytecode.length - 1){
			i++;
			String opcode = opcodes[getOpcode(bytecode[i])];
			
			if(opcode.equals("if") || opcode.equals("else") || opcode.equals("else_if") ||
			   opcode.equals("function"))
				depth++;
			else if(opcode.equals("end") || opcode.equals("end_while")){
				depth--;
				
				// Skip other else if/else statements
				if(skipElseIf && i < bytecode.length - 1){
					String op2 = opcodes[getOpcode(bytecode[i + 1])];
					
					if(op2.equals("else")){
						i += 2;
						depth++;
					}
					else if(op2.equals("else_ahead")){
						while(!opcodes[getOpcode(bytecode[++i])].equals("else_if"));
						depth++;
					}
				}
			}
		}
		
		return i;
	}
	
	// Postfix expression operation
	private void operate(String op, int lineNum){
		
		int expSize = expressions.peek().size();
		
		boolean isSingleOp = op.equals("!");
		
		// Get operands
		Object o1 = expressions.peek().get(expSize - (isSingleOp ? 1 : 2));
		Object o2 = expressions.peek().get(expSize - 1);
		
		// Remove last two elements
		expressions.peek().remove(expSize - 1);
		
		if(!isSingleOp)
			expressions.peek().remove(expSize - 2);
		
		boolean isBoolean = o1 instanceof Boolean || o2 instanceof Boolean;
		
		// Number operation
		if(isNumberOp(op) && !isBoolean){
			
			if((!(o1 instanceof Integer) && !(o1 instanceof Float)) || (!(o2 instanceof Integer) && !(o2 instanceof Float))){
				runtimeError("Type mismatch", lineNum);
				return;
			}
			
			// Result as number/boolean
			float result = 0;
			boolean resultBool = false;
			
			// Set result as boolean
			boolean useBool = false;

			// Integers need to be cast to int first then float
			float n1 = o1 instanceof Float ? (float) o1 : (float)((int) o1);
			float n2 = o2 instanceof Float ? (float) o2 : (float)((int) o2);
			
			// Operate
			switch(op){
				case "+":	result = n1 + n2;		break;
				case "-":	result = n1 - n2;		break;
				case "*":	result = n1 * n2;		break;
				case "/":	result = n1 / n2;		break;
				case "%":	result = n1 % n2;		break;
				case "<":	resultBool = n1 < n2;	useBool = true; break;
				case ">":	resultBool = n1 > n2;	useBool = true; break;
				case "==":	resultBool = n1 == n2;	useBool = true; break;
				case ">=":	resultBool = n1 <= n2;	useBool = true; break;
				case "<=":	resultBool = n1 >= n2;	useBool = true; break;
			}
			
			if(!useBool){
				// Treat as float if data is lost when casting to int
				if(result != (int)result)
					expressions.peek().add(result);
				else
					expressions.peek().add((int)result);
			}
			else
				expressions.peek().add(resultBool);
			
			return;
		}
		
		// Boolean operation
		if(!isBoolean || isNumberOp(op)){
			runtimeError("Type mismatch", lineNum);
			return;
		}
		
		// Cast
		boolean b1 = (boolean) o1;
		boolean b2 = (boolean) o2;
		
		boolean result = false;
		
		// Operate
		switch(op){
			case "!":	result = !b1;		break;
			case "||":	result = b1 || b2;	break;
			case "&&":	result = b1 && b2;	break;
			case "==":	result = b1 == b2;	break;
		}
		
		expressions.peek().add(result);
	}
	
	// Create syntax error and halt compilation
	private void runtimeError(String type, int lineNum){
		try{
			System.err.println("\nDScript runtime error:\n" + type + " in " + script.getFileName() + " on line " + lineNum +
				":\n>> " + Files.readAllLines(Paths.get(script.getPath())).get(lineNum - 1).trim());
		}
		catch(IOException e){
			e.printStackTrace();
		}
		haltRun = true;
	}
	
	// Condition that should not occur, may produce incorrect results
	private void runtimeWarning(String type, int lineNum){
		System.err.println("\nDScript runtime warning:\n" + type + " in " + script.getFileName() + " on line " + lineNum);
	}
}
