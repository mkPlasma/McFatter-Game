package engine.script;

import static engine.script.ScriptFunctions.BOOLEAN;
import static engine.script.ScriptFunctions.FLOAT;
import static engine.script.ScriptFunctions.INT;
import static engine.script.ScriptFunctions.STRING;
import static engine.script.ScriptFunctions.builtInFunctionDot;
import static engine.script.ScriptFunctions.builtInFunctionTypeMatch;
import static engine.script.ScriptFunctions.convertString;
import static engine.script.ScriptFunctions.getBuiltInFunctionName;
import static engine.script.ScriptFunctions.getBuiltInFunctionParameterCount;
import static engine.script.ScriptFunctions.getData;
import static engine.script.ScriptFunctions.getLineNum;
import static engine.script.ScriptFunctions.getOpcodeName;
import static engine.script.ScriptFunctions.getOperation;
import static engine.script.ScriptFunctions.getType;
import static engine.script.ScriptFunctions.isNumberOp;
import static engine.script.ScriptFunctions.isOperation;
import static engine.script.ScriptFunctions.isVariable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;

import content.FrameList;
import engine.entities.Bullet;
import engine.entities.GameEntity;
import engine.entities.MovableEntity;
import engine.entities.Player;
import engine.screens.MainScreen;

/*
 * 		ScriptRunner.java
 * 		
 * 		Purpose:	Runs DScript bytecode.
 * 		Notes:		Also provides functionality of built-in functions.
 * 		
 */

public class ScriptRunner{
	
	// Stops script
	private boolean haltRun = false;
	
	// Finished running
	private boolean finished = false;
	
	// Used for deleted variables
	private final Object EMPTY = (byte)0;
	
	
	// Stored in ScriptState
	private Object[] variables;
	private Stack<Integer> returnPoints;
	
	
	// Loop points
	private ArrayList<Integer> loops;
	
	// Function points
	private ArrayList<Integer> functions;
	
	// Current array
	private ArrayList<Object> array;
	
	// Element taken from array
	private ArrayList<Object> arrayRef;
	
	// Used to evaluate postfix expressions
	private Stack<ArrayList<Object>> expressions;
	
	// Function parameters
	private Stack<Queue<Object>> funcParams;
	
	// Return value for functions
	private Object returnValue;
	
	// Holds value before dot separator
	private Stack<Object> dotValues;
	
	private long[] bytecode;
	
	private DScript script;
	
	// Branched states
	private ArrayList<ScriptBranch> branches;
	
	private MainScreen screen;
	private Player player;
	
	private FrameList frameList;
	
	// RNG
	private Random random;
	
	private int time;
	
	public ScriptRunner(DScript script, MainScreen screen){
		this.script = script;
		this.screen = screen;
	}
	
	// Initializes and returns primary branch
	public ScriptBranch init(){
		// Load bytecode
		bytecode = script.getBytecode();
		
		if(bytecode == null){
			System.err.println("\n" + script.getFileName() + " not compiled, not running");
			haltRun = true;
			return null;
		}
		
		haltRun = false;

		// Initialize/reset lists
		loops 			= new ArrayList<Integer>();
		functions		= new ArrayList<Integer>();
		array			= new ArrayList<Object>();
		arrayRef		= new ArrayList<Object>();
		expressions		= new Stack<ArrayList<Object>>();
		funcParams		= new Stack<Queue<Object>>();
		branches		= new ArrayList<ScriptBranch>();
		dotValues		= new Stack<Object>();
		
		expressions.push(new ArrayList<Object>());
		funcParams.add(new LinkedList<Object>());
		
		random = new Random();
		
		// Account for register and default vars
		int varCount = 53;
		
		// Add variables, find functions
		for(int i = 0; i < bytecode.length; i++){
			long inst = bytecode[i];
			String op = getOpcodeName(inst);
			
			if(getType(inst) == STRING)
				i += getData(inst) + 1;
			else if(op.equals("create_var"))
				varCount++;
			else if(op.equals("function") || op.equals("task"))
				functions.add(i);
			else if(op.equals("while"))
				loops.add(i);
		}
		
		Object[] variables = new Object[varCount];
		
		addDefaultVars(variables);
		
		return new ScriptBranch(0, variables, true);
	}
	
	// Adds vars for bullet types, etc.
	private void addDefaultVars(Object[] variables){
		
		int c = 1;
		
		// Bullet types
		for(int i = 0; i < 20; i++)
			variables[c++] = i;
		
		// Bullet colors
		for(int i = 0; i < 32; i++)
			variables[c++] = i;
	}
	
	public void setPlayer(Player player){
		this.player = player;
	}
	
	public void setFrameList(FrameList frameList){
		this.frameList = frameList;
	}
	
	public void setTime(int time){
		this.time = time;
	}
	
	public boolean haltRun(){
		return haltRun;
	}
	
	public boolean isFinished(){
		return finished;
	}
	
	public ArrayList<ScriptBranch> getBranches(){
		ArrayList<ScriptBranch> temp = new ArrayList<ScriptBranch>(branches);
		branches.clear();
		return temp;
	}
	
	// Run bytecode
	@SuppressWarnings("unchecked")
	public void run(ScriptBranch branch){
		
		if(haltRun)
			return;
		
		// Return if waiting
		if(!branch.tickWaitTime())
			return;

		branch.syncWithParent();
		variables = branch.getVariables();
		returnPoints = branch.getReturnPoints();
		
		// Go into else block
		boolean doElse = false;
		boolean resetDoElse = false;
		boolean elseAhead = false;
		
		// Loop through
		for(int i = branch.getBytecodeIndex(); i < bytecode.length; i++){
			
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
							case STRING:
								ArrayList<Long> list = new ArrayList<Long>();
								
								int c = i + data + 1;
								i++;
								
								for(; i <= c; i++)
									list.add(bytecode[i]);
								
								i--;
								
								variables[0] = convertString(list);
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
					variables[data] = EMPTY;
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
							case STRING:
								ArrayList<Long> list = new ArrayList<Long>();
								
								int c = i + data + 1;
								i++;
								
								for(; i <= c; i++)
									list.add(bytecode[i]);
								
								i--;
								
								expressions.peek().add(convertString(list));
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
						default:	op = "";	break;
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
					
					if(o == null){
						runtimeError("Variable is not defined", lineNum);
						return;
					}
					
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
					
					if(o == null){
						runtimeError("Variable is not defined", lineNum);
						return;
					}
					
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
				
				case "end_while":
					i = loops.get(data);
					continue;
				
				
					
				case "break":
					while(!getOpcodeName(bytecode[++i]).equals("end_while"));
					continue;
				
				
				// Skip function definitions
				case "function": case "task":
					i = skipToEnd(i, false);
					continue;
				
				
				
				case "call_func":
					
					int index = i;
					
					// Jump to function
					i = functions.get(data);
					
					// Branch if task
					if(getOpcodeName(bytecode[i]).equals("task")){
						// New branch continues outside of task
						ScriptBranch newBranch = new ScriptBranch(index + 1, variables, branch.isPrimary());
						branches.add(newBranch);
						
						// Current branch enters task
						branch.setPrimary(false);
						branch.setParent(newBranch);
					}
					
					// For functions add return point and use new expression
					else{
						// Remove if in other function call
						if(funcParams.size() > 1)
							funcParams.pop();
						
						returnPoints.push(index);
						expressions.push(new ArrayList<Object>());
					}
					
					continue;
				
				
				
				case "call_func_b":
					builtInFunction(data, lineNum);
					
					// Remove if in other function call
					if(funcParams.size() > 1)
						funcParams.pop();
					
					continue;
				
				
				
				case "set_param":
					funcParams.peek().add(variables[0]);
					continue;
				
				
				
				case "get_param":
					variables[data] = funcParams.peek().remove();
					
					// Remove if empty
					if(funcParams.size() > 1 && funcParams.peek().isEmpty())
						funcParams.pop();
					
					continue;
				
				
				
				case "param_inc":
					funcParams.add(new LinkedList<Object>());
					continue;
				
				
				case "end_func": case "return_void": case "return":
					
					// End branch
					if(!branch.isPrimary() && returnPoints.isEmpty()){
						branch.setVariables(variables);
						branch.syncToParent();
						branch.remove();
						return;
					}
					
					// Remove expression
					expressions.pop();
					
					// Return
					i = returnPoints.pop();
					
					if(opcode.equals("return"))
						returnValue = variables[0];
					else{
						returnValue = null;
					}
					
					continue;
				
				
				case "dot":
					if(variables[0] == null){
						runtimeError("Null value before dot separator", lineNum);
						return;
					}
					
					dotValues.push(variables[0]);
					continue;
				
				case "wait":{
					Object obj = variables[0];
					
					// Check type
					if(!(obj instanceof Integer) && !(obj instanceof Float)){
						runtimeError("Type mismatch", lineNum);
						return;
					}
					
					// Cast to int
					obj = obj instanceof Float ? (int)(float)obj : obj;
					
					// Set wait time
					branch.setWaitTime((int)obj);
					
					// Set bytecode index
					branch.setBytecodeIndex(i + 1);
					
					// Update variables/return points
					branch.setVariables(variables);
					branch.setReturnPoints(returnPoints);
					branch.syncToParent();
					
					// Return to continue running
					return;
				}
			}
		}
		
		// Script finished
		if(branch.isPrimary()){
			branch.remove();
			finished = true;
		}
	}
	
	// Skip to end of block, returns new i
	private int skipToEnd(int i, boolean skipElseIf){
		int depth = 1;
		
		while(depth > 0 && i < bytecode.length - 1){
			i++;
			String opcode = getOpcodeName(bytecode[i]);
			
			if(getType(bytecode[i]) == STRING)
				i += getData(bytecode[i]) + 1;
			
			else if(opcode.equals("if") || opcode.equals("else") || opcode.equals("else_if") ||
			   opcode.equals("function") || opcode.equals("task"))
				depth++;
			
			else if(opcode.equals("end") || opcode.equals("end_while") || opcode.equals("end_func")){
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
		
		// Check if initialized
		if(o1 == null || (!isSingleOp && o2 == null)){
			runtimeError("Variable is not defined", lineNum);
			return;
		}
		
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

		if(o1 == null || (!op.equals("!") && o2 == null)){
			runtimeError("Variable is not defined", lineNum);
			return null;
		}
		
		boolean isString = o1 instanceof String || o2 instanceof String;
		
		// Check strings
		if(isString && !(op.equals("+") || op.equals("=="))){
			runtimeError("Type mismatch", lineNum);
			return null;
		}
		
		// String operation
		if(isString){
			if(op.equals("+"))
				return o1.toString() + o2.toString();
			else
				return o1.equals(o2);
		}
		
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
				case "/":
					if(n2 == 0){
						runtimeError("Divide by zero", lineNum);
						return null;
					}
					result = n1 / n2;
				break;
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
	@SuppressWarnings("unchecked")
	private void builtInFunction(int index, int lineNum){
		
		boolean dot = builtInFunctionDot(index);
		
		// Function requirement/dot must match
		if(dot && dotValues.isEmpty()){
			runtimeError("Function must be used with dot separator", lineNum);
			return;
		}
		
		Queue<Object> params = funcParams.peek();
		
		// Add dot value to list
		if(dot){
			Queue<Object> tmp = new LinkedList<Object>();
			
			// Move all values to temp array
			while(!params.isEmpty())
				tmp.add(params.remove());
			
			// Add dot value
			params.add(dotValues.pop());
			
			// Place values back
			while(!tmp.isEmpty())
				params.add(tmp.remove());
		}
		
		// Check for type mismatch
		if(!builtInFunctionTypeMatch(index, params.toArray())){
			runtimeError("Type mismatch", lineNum);
			return;
		}

		// Parameter count
		int paramCount = getBuiltInFunctionParameterCount(index);
		paramCount += dot ? 1 : 0;
		
		// Parameters
		Object o1 = null;
		Object o2 = null;
		
		if(paramCount > 0 && params.size() > 0){
			o1 = params.remove();
			
			if(paramCount > 1 && params.size() > 0)
				o2 = params.remove();
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
		
		// As entities
		GameEntity ge = o1 instanceof GameEntity ? (GameEntity)o1 : null;
		MovableEntity me = o1 instanceof MovableEntity ? (MovableEntity)o1 : null;
		Bullet bl = o1 instanceof Bullet ? (Bullet)o1 : null;
		
		// No return value by default
		returnValue = null;
		
		String func = getBuiltInFunctionName(index);
		
		switch(func){
			
			// General functions
			case "print":
				System.out.println(script.getFileName() + ": " + o1);
				return;
			
			case "scriptTime":
				returnValue = time;
				return;

			case "centerX":
				returnValue = 224;
				return;
			
			case "centerY":
				returnValue = 240;
				return;
			
			case "centerPos":{
				ArrayList<Object> ar = new ArrayList<Object>();
				ar.add(224);
				ar.add(240);
				returnValue = ar;
				return;
			}
			
			case "leftX":
				returnValue = 32;
				return;
			
			case "rightX":
				returnValue = 416;
				return;
			
			case "topY":
				returnValue = 16;
				return;
			
			case "bottomY":
				returnValue = 464;
				return;
			
			case "playerX":
				returnValue = player.getX();
				return;
				
			case "playerY":
				returnValue = player.getY();
				return;
			
			case "angleToPlayer":
				// Pos array
				if(paramCount == 1){
					float[] pos = convertArray((ArrayList<Object>)o1, func, lineNum);
					returnValue = (float)Math.toDegrees(Math.atan2(player.getY() - pos[1], player.getX() - pos[0]));
					return;
				}

				// x, y
				
				if(!isFloat){
					f1 = (float)i1;
					f2 = (float)i2;
				}
				
				returnValue = (float)Math.toDegrees(Math.atan2(player.getY() - f2, player.getX() - f1));
				return;
			
			case "angleToLocation":
				// Pos array
				if(paramCount == 2){
					float[] pos1 = convertArray((ArrayList<Object>)o1, func, lineNum);
					float[] pos2 = convertArray((ArrayList<Object>)o2, func, lineNum);
					returnValue = (float)Math.toDegrees(Math.atan2(pos2[1] - pos1[1], pos2[0] - pos1[0]));
				}
				
				// x1, y1, x2, y2
				
				if(!isFloat){
					f1 = (float)i1;
					f2 = (float)i2;
				}
				
				Object o3 = params.remove();
				Object o4 = params.remove();

				float f3 = o3 instanceof Float ? (float)o3 : (float)(int)o3;
				float f4 = o4 instanceof Float ? (float)o4 : (float)(int)o4;

				returnValue = (float)Math.toDegrees(Math.atan2(f4 - f2, f3 - f1));
				return;

			case "rand":
				if(isFloat){
					i1 = (int)f1;
					i2 = (int)f2;
				}
				
				returnValue = i1 + random.nextInt(i2 - i1);
				return;
			
			case "randFloat":
				returnValue = f1 + random.nextFloat()*(f2 - f1);
				return;
			
			case "randBool":
				returnValue = random.nextBoolean();
				return;
			
			
			
			
			
			// Math functions
			case "pi":
				returnValue = (float)Math.PI;
				return;
			
			case "abs":
				if(isFloat) returnValue = Math.abs(f1);
				else		returnValue = Math.abs(i1);
				return;
			
			case "round":
				if(isFloat) returnValue = (int)Math.round(f1);
				else		returnValue = (int)Math.round(i1);
				return;
			
			case "trunc":
				if(isFloat) returnValue = (int)(f1 > 0 ? Math.floor(f1) : Math.ceil(f1));
				else		returnValue = (int)(i1 > 0 ? Math.floor(i1) : Math.ceil(i1));
				return;
			
			case "floor":
				if(isFloat) returnValue = (int)Math.floor(f1);
				else		returnValue = (int)Math.floor(i1);
				return;
			
			case "ceil":
				if(isFloat) returnValue = (int)Math.ceil(f1);
				else		returnValue = (int)Math.ceil(i1);
				return;
			
			case "sqrt":
				if(isFloat) returnValue = (float)Math.sqrt(f1);
				else		returnValue = (float)Math.sqrt(i1);
				return;
			
			case "log":
				if(isFloat) returnValue = (float)Math.log(f1);
				else		returnValue = (float)Math.log(i1);
				return;
			
			case "log10":
				if(isFloat) returnValue = (float)Math.log10(f1);
				else		returnValue = (float)Math.log10(i1);
				return;
			
			case "degrees":
				if(isFloat) returnValue = (float)Math.toDegrees(f1);
				else		returnValue = (float)Math.toDegrees(i1);
				return;
			
			case "radians":
				if(isFloat) returnValue = (float)Math.toRadians(f1);
				else		returnValue = (float)Math.toRadians(i1);
				return;
			
			case "sin":
				if(isFloat) returnValue = (float)Math.sin(Math.toRadians(f1));
				else		returnValue = (float)Math.sin(Math.toRadians(i1));
				return;
				
			case "cos":
				if(isFloat) returnValue = (float)Math.cos(Math.toRadians(f1));
				else		returnValue = (float)Math.cos(Math.toRadians(i1));
				return;
				
			case "tan":
				if(isFloat) returnValue = (float)Math.tan(Math.toRadians(f1));
				else		returnValue = (float)Math.tan(Math.toRadians(i1));
				return;
				
			case "asin":
				if(isFloat) returnValue = (float)Math.toDegrees(Math.asin(f1));
				else		returnValue = (float)Math.toDegrees(Math.asin(i1));
				return;
				
			case "acos":
				if(isFloat) returnValue = (float)Math.toDegrees(Math.acos(f1));
				else		returnValue = (float)Math.toDegrees(Math.acos(i1));
				return;
				
			case "atan":
				if(isFloat) returnValue = (float)Math.toDegrees(Math.atan(f1));
				else		returnValue = (float)Math.toDegrees(Math.atan(i1));
				return;
				
			case "atan2":
				if(isFloat) returnValue = (float)Math.toDegrees(Math.atan2(i1, i2));
				else		returnValue = (float)Math.toDegrees(Math.atan2(f1, f2));
				return;
				
			case "min":
				if(isFloat) returnValue = Math.min(f1, f2);
				else		returnValue = Math.min(i1, i2);
				return;
				
			case "max":
				if(isFloat) returnValue = Math.max(f1, f2);
				else		returnValue = Math.max(i1, i2);
				return;
			
			
			// Array functions
			case "length":
				if(o1 instanceof ArrayList)
					returnValue = ((ArrayList<Object>)o1).size();
				else
					returnValue = ((String)o1).length();
				return;
			
			case "add":
				((ArrayList<Object>)o1).add(o2);
				return;
			
			case "remove":{
				ArrayList<Object> ar = (ArrayList<Object>)o1;
				
				if(paramCount == 0){
					returnValue = ar.remove(ar.size() - 1);
					return;
				}
				
				int l = Math.min(i2, ar.size());
				
				for(int i = 0; i < l; i++)
					ar.remove(ar.size() - 1);
				
				return;
			}
			
			
			// Bullet functions
			case "bullet":{
				Object ox = params.remove();
				Object oy = params.remove();
				Object oDir = params.remove();
				Object oSpd = params.remove();
				Object oDel = params.remove();

				float x = ox instanceof Float ? (float)ox : (float)(int)ox;
				float y = oy instanceof Float ? (float)oy : (float)(int)oy;
				float dir = oDir instanceof Float ? (float)oDir : (float)(int)oDir;
				float spd = oSpd instanceof Float ? (float)oSpd : (float)(int)oSpd;
				int del = (int)oDel;
				
				returnValue = new Bullet(frameList.getBullet((byte)i1, (byte)i2), x, y, dir, spd, del, frameList, screen);
				screen.addEnemyBullet((Bullet)returnValue);
				return;
			}
			
			case "delete":
				bl.onDestroy();
				return;
			
			case "setX":
				if(isFloat) ge.setX(f2);
				else		ge.setX(i2);
				return;
			
			case "setY":
				if(isFloat) ge.setY(f2);
				else		ge.setY(i2);
				return;
			
			case "setPos":
				ge.setPos(convertArray((ArrayList<Object>)o2, func, lineNum));
				return;
			
			case "setDir":
				if(isFloat) me.setDir(f2);
				else		me.setDir(i2);
				return;
			
			case "setAngVel":
				if(isFloat) me.setAngVel(f2);
				else		me.setAngVel(i2);
				return;
			
			case "setSpd":
				if(isFloat) me.setSpd(f2);
				else		me.setSpd(i2);
				return;
			
			case "setAccel":
				if(isFloat) me.setAccel(f2);
				else		me.setAccel(i2);
				return;
			
			case "setMinSpd":
				if(isFloat) me.setMinSpd(f2);
				else		me.setMinSpd(i2);
				return;
			
			case "setMaxSpd":
				if(isFloat) me.setMaxSpd(f2);
				else		me.setMaxSpd(i2);
				return;
			
			case "setType":
				bl.setType(i2);
				bl.refreshSprite();
				return;
			
			case "setColor":
				bl.setColor(i2);
				bl.refreshSprite();
				return;
			
			case "setFrame":{
				
				// Type, color
				if(paramCount == 3){
					int i3 = (int)params.remove();
					bl.setFrame(frameList.getBullet(i2, i3));
					return;
				}
				
				// Array
				ArrayList<Object> ar = (ArrayList<Object>)o2;
				
				if(ar.size() < 2){
					runtimeError("setFrame requires an array of length 2", lineNum);
					return;
				}
				
				if(!(ar.get(0) instanceof Integer) || !(ar.get(1) instanceof Integer)){
					runtimeError("Type mismatch", lineNum);
					return;
				}
				
				bl.setFrame(frameList.getBullet((int)ar.get(0), (int)ar.get(1)));
				bl.initFrameProperties();
				return;
			}
			
			case "setAdditive":{
				boolean add = paramCount == 1 ? true : (boolean)o2;
				
				bl.getSprite().setAdditive(add);
				return;
			}
			
			
			case "getX":
				returnValue = ge.getX();
				return;
			
			case "getY":
				returnValue = ge.getY();
				return;
			
			case "getPlayerPos": case "getPos":{
				float[] pos = func.equals("getPlayerPos") ? player.getPos() : ge.getPos();
				ArrayList<Float> pos1 = new ArrayList<Float>();
				
				pos1.add(pos[0]);
				pos1.add(pos[1]);
				
				returnValue = pos1;
				return;
			}
			
			case "getTime":
				returnValue = ge.getTime();
				return;
			
			case "isDeleted":
				returnValue = ge.isDeleted();
				return;
			
			case "getDir":
				returnValue = me.getDir();
				return;
			
			case "getAngVel":
				returnValue = me.getAngVel();
				return;
			
			case "getSpd":
				returnValue = me.getSpd();
				return;
			
			case "getAccel":
				returnValue = me.getAccel();
				return;
			
			case "getMinSpd":
				returnValue = me.getMinSpd();
				return;
			
			case "getMaxSpd":
				returnValue = me.getMaxSpd();
				return;

			case "getType":
				returnValue = bl.getFrame().getType();
				return;
			
			case "getColor":
				returnValue = bl.getFrame().getColor();
				return;

			case "getFrame":{
				ArrayList<Integer> frame = new ArrayList<Integer>();
				
				frame.add(bl.getFrame().getType());
				frame.add(bl.getFrame().getColor());
				
				returnValue = frame;
				return;
			}
			
			case "isAdditive":
				returnValue = bl.getSprite().isAdditive();
				return;
		}
	}
	
	// Converts ArrayList to float[] for certain functions
	private float[] convertArray(ArrayList<Object> list, String funcName, int lineNum){
		// Check size
		if(list.size() < 2){
			runtimeError(funcName + " requires an array of length 2", lineNum);
			return new float[]{0, 0};
		}
		
		// Get numbers
		Object o1 = list.get(0);
		Object o2 = list.get(1);
		
		float f1 = o1 instanceof Float ? (float)o1 : o1 instanceof Integer ? (float)(int)o1 : Float.NaN;
		float f2 = o2 instanceof Float ? (float)o2 : o2 instanceof Integer ? (float)(int)o2 : Float.NaN;
		
		// Check type
		if(Float.isNaN(f1) || Float.isNaN(f2)){
			runtimeError(funcName + " requires an array of numbers", lineNum);
			return new float[]{0, 0};
		}
		
		return new float[]{f1, f2};
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
