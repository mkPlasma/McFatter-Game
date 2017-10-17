package engine.script;

import static engine.script.ScriptFunctions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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
	
	// Used to evaluate postfix expressions
	private Stack<ArrayList<Object>> expressions;
	
	// Function parameters
	private Stack<Object> funcParams;
	
	// Return value for functions
	private Object returnValue;
	
	private long[] bytecode;
	
	private DScript script;
	
	@SuppressWarnings("unchecked")
	public void run(DScript script){
		this.script = script;

		// Load bytecode
		bytecode = script.getBytecode();
		
		if(bytecode == null){
			System.err.println("\n" + script.getFileName() + " not compiled, not running");
			return;
		}
		
		haltRun = false;
		
		// Variables
		Object[] variables;
		
		// Function points
		ArrayList<Integer> functions = new ArrayList<Integer>();
		
		// Store return points for function calls
		Stack<Integer> returnPoints = new Stack<Integer>();
		
		// Current array
		ArrayList<Object> array = new ArrayList<Object>();
		
		// Element taken from array
		ArrayList<Object> arrayRef = new ArrayList<Object>();
		
		// Initialize/reset lists
		expressions = new Stack<ArrayList<Object>>();
		expressions.push(new ArrayList<Object>());
		funcParams = new Stack<Object>();
		
		// Account for register
		int varCount = 1;
		
		// Add variables, find functions
		for(int i = 0; i < bytecode.length; i++){
			String op = getOpcodeName(bytecode[i]);
			
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
			String opcode = getOpcodeName(inst);
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
				
				operate(opcode, lineNum);
				
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
				
				
				
				case "load_r":
					if(returnValue == null){
						runtimeError("Null or void return value", lineNum);
						return;
					}
					
					variables[0] = returnValue;
					continue;
				
				
				
				case "store":
					variables[data] = variables[0];
					continue;
				
				
				
				case "create_var":
					variables[data] = 0;
					continue;
				
				
				
				case "delete_var":
					variables[data] = NULL;
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
				
				
				
				case "exp_inc":
					expressions.push(new ArrayList<Object>());
					continue;
				
				
				
				case "exp_dec":
					expressions.pop();
					continue;
				
				
				
				case "array_val":
					if(variables[0] instanceof ArrayList){
						runtimeError("Cannot add array to array", lineNum);
						return;
					}
					
					array.add(variables[0]);
					continue;
				
				
				
				case "array_end":
					variables[0] = array.clone();
					array.clear();
					continue;
				
				
				
				case "array_load":{
					Object o = variables[data];
					
					if(!(o instanceof ArrayList)){
						runtimeError("Variable is not an array", lineNum);
						return;
					}
					
					arrayRef = (ArrayList<Object>)o;
					continue;
				}
				
				
				
				case "array_elem":{
					Object o = variables[0];
					
					// Get item in index
					if(o instanceof Integer){
						int n = (int)o;
						
						// Negative numbers refer to elements from end
						if(n < 0)
							n = arrayRef.size() + n;
						
						if(n >= 0 && n < arrayRef.size()){
							variables[0] = arrayRef.get(n);
							continue;
						}
					}
					
					// Otherwise get index of item (or -1 if not in array)
					variables[0] = arrayRef.indexOf(o);
					
					continue;
				}
				
				
				
				case "array_elem_s": case "array_elem_a": case "array_elem_u": case "array_elem_m": case "array_elem_d": case "array_elem_o":{
					
					int ind = (int)variables[0];
					
					if(ind < 0 || ind >= arrayRef.size()){
						runtimeError("Array index out of range", lineNum);
						return;
					}
					
					char c = opcode.charAt(opcode.length() - 1);
					String op;
					
					switch(c){
						case 'a':	op = "+";	break;
						case 'u':	op = "-";	break;
						case 'm':	op = "*";	break;
						case 'd':	op = "/";	break;
						case 'o':	op = "%";	break;
						default:		op = "";		break;
					}
					
					Object obj = array.get(array.size() - 1);
					
					if(!op.isEmpty()){
						obj = operate(op, arrayRef.get(ind), obj, lineNum);
					}
					
					arrayRef.set(ind, obj);
					array.remove(array.size() - 1);
					
					continue;
				}
				
				
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
					
					int whileNumC = 0;
					String op2 = "";
					
					// Loop back to while
					while(!op2.equals("while") || whileNumC != data){
						i--;
						op2 = getOpcodeName(bytecode[i]);
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
				
				
				
				case "call_func_b":
					builtInFunction(data, lineNum);
					continue;
				
				
				
				case "set_param":
					funcParams.push(variables[0]);
					continue;
				
				
				
				case "get_param":
					variables[data] = funcParams.pop();
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
			}
		}
		
		//System.out.println("\nResults of " + script.getFileName() + ":\n");
		
		//for(int i = 0; i < variables.length; i++)
		//	System.out.println((i == 0 ? "Register" : "Variable " + i) + ": " + variables[i]);
		
	}
	
	// Skip to end of block, returns new i
	private int skipToEnd(int i, boolean skipElseIf){
		int depth = 1;
		
		while(depth > 0 && i < bytecode.length - 1){
			i++;
			String opcode = getOpcodeName(bytecode[i]);
			
			if(opcode.equals("if") || opcode.equals("else") || opcode.equals("else_if") ||
			   opcode.equals("function"))
				depth++;
			else if(opcode.equals("end") || opcode.equals("end_while")){
				depth--;
				
				// Skip other else if/else statements
				if(skipElseIf && i < bytecode.length - 1){
					String op2 = getOpcodeName(bytecode[i + 1]);
					
					if(op2.equals("else")){
						i += 2;
						depth++;
					}
					else if(op2.equals("else_ahead")){
						while(!getOpcodeName(bytecode[++i]).equals("else_if"));
						depth++;
					}
				}
			}
		}
		
		return i;
	}
	
	// Postfix expression operation
	@SuppressWarnings("unchecked")
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
		
		// Check if second is array
		if(!(o1 instanceof ArrayList) && o2 instanceof ArrayList){
			runtimeError("Type mismatch", lineNum);
			return;
		}
		
		boolean isArray = o1 instanceof ArrayList;
		
		// Regular operations
		if(!isArray){
			expressions.peek().add(operate(op, o1, o2, lineNum));
			return;
		}
		
		// Array operations
		
		// Is second object array
		boolean secondIsArray = o2 instanceof ArrayList;
		
		ArrayList<Object> a1 = (ArrayList<Object>)o1;
		ArrayList<Object> a2 = null;

		int firstSize = a1.size();
		int smallerSize = 0;
		
		if(secondIsArray){
			a2 = (ArrayList<Object>)o2;
			smallerSize = Math.min(a1.size(), a2.size());
		}
		
		ArrayList<Object> result = new ArrayList<Object>();
		
		// Operate on each item
		for(int i = 0; i < firstSize; i++){
			
			// Both arrays
			if(secondIsArray){
				if(i < smallerSize)
					result.add(operate(op, a1.get(i), a2.get(i), lineNum));
				
				// If first array was larger, copy remaining elements
				else
					result.add(a1.get(i));
			}
			
			// First only is array
			else
				result.add(operate(op, a1.get(i), o2, lineNum));
			
			if(haltRun)
				return;
		}
		
		expressions.peek().add(result);
	}
	
	// Single operation on two objects
	private Object operate(String op, Object o1, Object o2, int lineNum){
		
		boolean isBoolean = o1 instanceof Boolean || o2 instanceof Boolean;
		
		// Number operation
		if(isNumberOp(op) && !isBoolean){
			
			if((!(o1 instanceof Integer) && !(o1 instanceof Float)) || (!(o2 instanceof Integer) && !(o2 instanceof Float))){
				runtimeError("Type mismatch", lineNum);
				return null;
			}
			
			// Result as number/boolean
			float result = 0;
			boolean resultBool = false;
			
			boolean bothFloats = o1 instanceof Float || o2 instanceof Float;
			
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
				case "^":	result = (float)Math.pow(n1, n2);		break;
				case "<":	resultBool = n1 < n2;	useBool = true; break;
				case ">":	resultBool = n1 > n2;	useBool = true; break;
				case "==":	resultBool = n1 == n2;	useBool = true; break;
				case "<=":	resultBool = n1 <= n2;	useBool = true; break;
				case ">=":	resultBool = n1 >= n2;	useBool = true; break;
			}
			
			if(useBool)
				return resultBool;
			
			// Treat as float if data is lost when casting to int
			if(result != (int)result || bothFloats)
				return result;
			
			return (int)result;
		}
		
		// Boolean operation
		if(!isBoolean || isNumberOp(op)){
			runtimeError("Type mismatch", lineNum);
			return null;
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
		
		return result;
	}
	
	// Built in functions
	private void builtInFunction(int index, int lineNum){
		
		// Check for type mismatch
		if(!builtInFunctionTypeMatch(index, funcParams.toArray())){
			runtimeError("Type mismatch", lineNum);
			return;
		}
		
		// Parameters
		Object o1 = null;
		Object o2 = null;
		
		if(funcParams.size() > 0){
			o1 = funcParams.pop();
			
			if(funcParams.size() > 1)
				o2 = funcParams.pop();
		}
		
		// As ints/floats
		boolean isFloat = o1 instanceof Float || o2 instanceof Float;
		
		int i1 = o1 instanceof Integer ? (int)o1 : 0;
		int i2 = o2 instanceof Integer ? (int)o2 : 0;
		
		float f1 = o1 instanceof Float ? (float)o1 : 0;
		float f2 = o2 instanceof Float ? (float)o2 : 0;
		
		if(isFloat){
			f1 = o1 instanceof Integer ? i1 : f1;
			f2 = o2 instanceof Integer ? i2 : f2;
		}
		
		// No return value by default
		returnValue = null;
		
		switch(getBuiltInFunctionName(index)){
			case "print":
				System.out.println(script.getFileName() + ": " + o1);
				return;
				
			case "pi":
				returnValue = (float)Math.PI;
				return;
				
			case "abs":
				if(!isFloat) returnValue = Math.abs(i1);
				else		 returnValue = Math.abs(f1);
				return;
				
			case "degrees":
				if(!isFloat) returnValue = (float)Math.toDegrees(i1);
				else		 returnValue = (float)Math.toDegrees(f1);
				return;
			
			case "radians":
				if(!isFloat) returnValue = (float)Math.toRadians(i1);
				else		 returnValue = (float)Math.toRadians(f1);
				return;
			
			case "sin":
				if(!isFloat) returnValue = (float)Math.sin(Math.toRadians(i1));
				else		 returnValue = (float)Math.sin(Math.toRadians(f1));
				return;
				
			case "cos":
				if(!isFloat) returnValue = (float)Math.cos(Math.toRadians(i1));
				else		 returnValue = (float)Math.cos(Math.toRadians(f1));
				return;
				
			case "tan":
				if(!isFloat) returnValue = (float)Math.tan(Math.toRadians(i1));
				else		 returnValue = (float)Math.tan(Math.toRadians(f1));
				return;
				
			case "asin":
				if(!isFloat) returnValue = (float)Math.toDegrees(Math.asin(i1));
				else		 returnValue = (float)Math.toDegrees(Math.asin(f1));
				return;
				
			case "acos":
				if(!isFloat) returnValue = (float)Math.toDegrees(Math.acos(i1));
				else		 returnValue = (float)Math.toDegrees(Math.acos(f1));
				return;
				
			case "atan":
				if(!isFloat) returnValue = (float)Math.toDegrees(Math.atan(i1));
				else		 returnValue = (float)Math.toDegrees(Math.atan(f1));
				return;
				
			case "atan2":
				if(!isFloat) returnValue = (float)Math.toDegrees(Math.atan2(i1, i2));
				else		 returnValue = (float)Math.toDegrees(Math.atan2(f1, f2));
				return;
				
			case "pow":
				if(!isFloat) returnValue = (float)Math.pow(i1, i2);
				else		 returnValue = (float)Math.pow(f1, f2);
				return;
				
			case "min":
				if(!isFloat) returnValue = Math.min(i1, i2);
				else		 returnValue = Math.min(f1, f2);
				return;
				
			case "max":
				if(!isFloat) returnValue = Math.max(i1, i2);
				else		 returnValue = Math.max(f1, f2);
				return;
		}
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
