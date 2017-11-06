package engine.script;

import java.util.ArrayList;

import engine.entities.Bullet;
import engine.entities.GameEntity;
import engine.entities.MovableEntity;

/*
 * 		ScriptFunctions.java
 * 		
 * 		Purpose:	DScript reference and bytecode functions.
 * 		Notes:		
 * 		
 */

public class ScriptFunctions{
	
	// Note: Register is variable #0
	
	public static final String[] opcodes = {
		"none",				// No operation
		
		// Memory
		"load",				// Load a value into register
		"load_r",			// Load return value into register
		"store",			// Store register into variable
		"create_var",		// Creates a new variable
		"delete_var",		// Deletes variable
		
		// Expressions
		"exp_val",			// Declare expression value
		"exp_val_r",		// Expression value from function return
		"exp_end",			// End expression and save result in register
		"exp_inc",			// Create new expression, used by function calls in expressions
		"exp_dec",			// Remove expression
		
		// Arrays
		"array_val",		// Add register value into array
		"array_end",		// End array, store in register
		"array_load",		// Load array to get element
		"array_elem",		// Get array element of index in register
		"array_elem_s",		// Set array element to value of register
		"array_elem_a",		// Add
		"array_elem_u",		// Subtract
		"array_elem_m",		// Multiply
		"array_elem_d",		// Divide
		"array_elem_o",		// Modulo
		
		// Arithmetic
		"add",				// 
		"subtract",			// 
		"multiply",			// 
		"divide",			// 
		"modulo",			// 
		"exponent",			// 
		"increment",		// Increments register/variable
		"decrement",		// Decrements register/variable
		
		// Logical
		"not",				// Inverts register/variable
		"and",				// Ands register with value
		"or",				// Ors register with value
		
		// Comparisons
		"less",				// Compares register with value, sets register
		"greater",			//
		"equals",			//
		"not_eq",			//
		"less_eq",			//
		"greater_eq",		//
		
		// Control
		"end",				// Close current block
		"if",				// Branches into block if register is true
		"else",				// Branches into block if previous if did not run
		"else_if",			// Else if based on register
		"else_ahead",		// Store doElse value until else_if is reached
		"end_else_if",		// Same as end, also skips following else_if/else blocks
		"while",			// Return point for end_while, branches with if
		"end_while",		// Loops back to while
		"break",			// Breaks out of loop
		
		// Functions
		"function",			// Function jump location
		"task",				// Jump location and creates branch
		"call_func",		// Calls function with set parameters
		"call_func_b",		// Calls built-in function
		"end_func",			// Ends function, returns void
		"set_param",		// Set function parameter
		"get_param",		// Create variable and assign parameter to it
		"param_inc",		// Use new parameter queue
		"return",			// Returns register value
		"return_void",		// Return no value
		
		// Misc
		"dot",				// Dot separator
		"wait",				// Wait number of frames in register
	};
	
	public static final String[] reservedWords = {
		"set",
		"const",
		"global",
		
		"if",
		"else",
		"break",

		"while",
		"for",
		"forG",
		"forE",
		"forNE",
		"forLE",
		"forGE",
		"in",
		
		"function",
		"task",
		"return",
		
		"true",
		"false",
		
		"wait",
	};
	
	public static final String[] operations = {
		"+", "-", "*", "/", "%", "^", "<", ">", "==", "!=", "<=", ">=", "!", "||", "&&",
		
		"add", "subtract", "multiply", "divide", "modulo", "exponent",
		"less", "greater", "equals", "not_eq", "less_eq", "greater_eq", "not", "or", "and",
	};
	
	public static final byte INT = 0, FLOAT = 1, BOOLEAN = 2, STRING = 3,
							 VALUE = 0, VARIABLE = 1,
							 ZERO = 0;
	
	public static final String[] builtInFunctions = {
		
		// General
		"print:1",
		
		"scriptTime:0",
		
		"int:1",
		
		"centerPos:0",
		
		"playerX:0",
		"playerY:0",
		"playerPos:0",
		
		"angleToPlayer:1",
		"angleToPlayer:2",
		"angleToLocation:2",
		"angleToLocation:4",

		"rand:2",
		"randFloat:2",
		"randBool:0",
		
		// Math
		"pi:0",
		"abs:1",
		"round:1",
		"trunc:1",
		"floor:1",
		"ceil:1",
		"sqrt:1",
		"log:1",
		"log10:1",
		"degrees:1",
		"radians:1",
		
		"sin:1",
		"cos:1",
		"tan:1",
		"ain:1",
		"acos:1",
		"atan:1",
		
		"atan2:2",
		"min:2",
		"max:2",
		
		// Array
		"length:d0",
		"add:d1",
		"remove:d0",
		"remove:d1",
		
		// Bullet
		"bullet:7",
		"laser:8",
		
		"delete:d0",
		"setX:d1",
		"setY:d1",
		"setPos:d1",
		"setSpd:d1",
		"setDir:d1",
		"setAngVel:d1",
		"setAccel:d1",
		"setMinSpd:d1",
		"setMaxSpd:d1",
		"setType:d1",
		"setColor:d1",
		"setFrame:d1",
		"setFrame:d2",
		"setAdditive:d0",
		"setAdditive:d1",
		
		"getX:d0",
		"getY:d0",
		"getPos:d0",
		"getTime:d0",
		"isDeleted:d0",
		"getSpd:d0",
		"getDir:d0",
		"getAngVel:d0",
		"getAccel:d0",
		"getMinSpd:d0",
		"getMaxSpd:d0",
		"getType:d0",
		"getColor:d0",
		"getFrame:d0",
		"isAdditive:d0",
	};
	
	
	// Built in functions
	
	// Get index of name
	public static int getBuiltInFunctionIndex(String func){
		for(int i = 0; i < builtInFunctions.length; i++)
			if(builtInFunctions[i].replace(":d", ":").equals(func))
				return i;
		return -1;
	}
	
	// Get name
	public static String getBuiltInFunctionName(int index){
		String name = builtInFunctions[index];
		return name.substring(0, name.indexOf(':'));
	}
	
	public static int getBuiltInFunctionParameterCount(int index){
		String f = builtInFunctions[index].replaceAll(":d", ":");
		return Integer.parseInt(f.substring(f.indexOf(':') + 1));
	}
	
	// Check for type mismatch
	public static boolean builtInFunctionTypeMatch(int index, Object[] params){

		Object o1 = params.length > 0 ? params[0] : null;
		Object o2 = params.length > 1 ? params[1] : null;
		Object o3 = params.length > 2 ? params[2] : null;
		Object o4 = params.length > 3 ? params[3] : null;
		
		int p = getBuiltInFunctionParameterCount(index);
		
		switch(getBuiltInFunctionName(index)){
			case "int":
				return o1 instanceof Integer || o1 instanceof Float;
			
			case "angleToPlayer":
				if(p == 1)
					return o1 instanceof ArrayList;
				return (o1 instanceof Integer || o1 instanceof Float) && (o2 instanceof Integer || o2 instanceof Float);
			
			case "angleToLocation":
				
				if(p == 2)
					return o1 instanceof ArrayList && o2 instanceof ArrayList;
				
				return (o1 instanceof Integer || o1 instanceof Float) && (o2 instanceof Integer || o2 instanceof Float) &&
					   (o3 instanceof Integer || o3 instanceof Float) && (o4 instanceof Integer || o4 instanceof Float);
			
			case "abs": case "round": case "trunc": case "floor": case "ceil": case "sqrt": case "log": case "log10": case "degrees": case "radians":
			case "sin": case "cos": case "tan": case "asin": case "acos": case "atan":
				return o1 instanceof Integer || o1 instanceof Float;
			
			case "rand": case "randFloat": case "atan2": case "min": case "max":
				return (o1 instanceof Integer || o1 instanceof Float) && (o2 instanceof Integer || o2 instanceof Float);
			
			case "length":
				return o1 instanceof ArrayList || o1 instanceof String;
			
			case "add":
				return o1 instanceof ArrayList && !(o2 instanceof ArrayList);
			
			case "remove":
				return o1 instanceof ArrayList && (p == 0 || o2 instanceof Integer);
				
			case "bullet":{
				Object o5 = params[4];
				Object o6 = params[5];
				Object o7 = params[6];
				
				return o1 instanceof Integer && o2 instanceof Integer &&
					  (o3 instanceof Integer || o3 instanceof Float) &&
					  (o4 instanceof Integer || o4 instanceof Float) &&
					  (o5 instanceof Integer || o5 instanceof Float) &&
					  (o6 instanceof Integer || o6 instanceof Float) &&
					  (o7 instanceof Integer || o7 instanceof Float);
			}
			
			case "laser":{
				Object o5 = params[4];
				Object o6 = params[5];
				Object o7 = params[6];
				Object o8 = params[7];
				
				return o1 instanceof Integer && o2 instanceof Integer &&
					  (o3 instanceof Integer || o3 instanceof Float) &&
					  (o4 instanceof Integer || o4 instanceof Float) &&
					  (o5 instanceof Integer || o5 instanceof Float) &&
					  (o6 instanceof Integer || o6 instanceof Float) &&
					  (o7 instanceof Integer || o7 instanceof Float) &&
					  (o8 instanceof Integer || o8 instanceof Float);
			}
			
			case "setX": case "setY":
				return o1 instanceof GameEntity && (o2 instanceof Integer || o2 instanceof Float);
			
			case "setSpd": case "setDir":
			case "setAngVel": case "setAccel": case "setMinSpd": case "setMaxSpd":
				return o1 instanceof MovableEntity && (o2 instanceof Integer || o2 instanceof Float);
			
			case "setPos":
				return o1 instanceof GameEntity && o2 instanceof ArrayList;

			case "setType": case "setColor":
				return o1 instanceof Bullet && o2 instanceof Integer;
				
			case "setFrame":
				return o1 instanceof Bullet && ((p == 2 && o2 instanceof Integer && o3 instanceof Integer) || (p == 1 && o2 instanceof ArrayList));
				
			case "setAdditive":
				return o1 instanceof Bullet && (p == 0 || (p == 1 && o2 instanceof Boolean));
			
			case "delete": case "getX": case "getY": case "getPos": case "getTime": case "isDeleted":
				return o1 instanceof GameEntity;
			
			case "getSpd": case "getDir":
			case "getAngVel": case "getAccel": case "getMinSpd": case "getMaxSpd":
				return o1 instanceof MovableEntity;
			
			case "getType": case "getColor": case "getFrame": case "isAdditive":
				return o1 instanceof Bullet;
		}
		
		return true;
	}
	
	// Function requires dot separator
	public static boolean builtInFunctionDot(int index){
		return builtInFunctions[index].contains(":d");
	}
	
	
	
	// Token functions
	
	// Get token line num
	public static int getLineNum(String token){
		return Integer.parseInt(token.substring(0, token.indexOf(':') - 1));
	}
	
	// Get token type
	public static char getType(String token){
		return token.charAt(token.indexOf(':') - 1);
	}
	
	// Get token data
	public static String getData(String token){
		return token.substring(token.indexOf(':') + 1);
	}
	
	
	
	// Return index of bytecode
	public static byte getOpcode(String opcode){
		
		// As name
		for(short i = 0; i < opcodes.length; i++)
			if(opcodes[i].equals(opcode))
				return (byte)i;
		
		// As operation
		return getOpcode(getOperation(opcode));
	}
	
	public static boolean isReservedWord(String s){
		for(String r:reservedWords)
			if(s.equals(r))
				return true;
		return false;
	}
	
	
	// String functions
	
	// Convert string to longs
	public static ArrayList<Long> convertString(String s){
		
		ArrayList<Long> list = new ArrayList<Long>();
		
		long l = 0;
		
		for(int i = 0; i < s.length(); i++){
			
			l |= s.charAt(i);
			
			if((i + 1) % 8 == 0){
				list.add(l);
				l = 0;
			}
			else
				l <<= 8;
			
			if(i == s.length() - 1){
				l <<= (6 - i)*8;
			}
		}
		
		list.add(l);
		
		return list;
	}
	
	// Convert longs to string
	public static String convertString(ArrayList<Long> list){
		
		String s = "";
		
		for(int i = 0; i < list.size(); i++){
			
			long l = list.get(i);
			
			for(int j = 0; j < 8; j++){
				char c = (char)(255 & (l >>> ((7 - j)*8)));
				
				if(c == 0)
					return s;
				
				s += c;
			}
		}
		
		return s;
	}
	
	
	
	// Creates an instruction
	public static long getInstruction(String opcode, byte variable, byte type, int lineNum, int data){
		long inst = 0;
		
		// Set operand
		inst |= toLong(getOpcode(opcode));

		// Shift, add value/variable switch
		inst <<= 1;
		inst |= toLong(variable);
		
		// Shift, add type
		inst <<= 2;
		inst |= toLong(type);
		
		// Shift 5 to finish byte, then 2 bytes, assign line number
		inst <<= 21;
		inst |= toLong(lineNum);
		
		// Assign data
		inst <<= 32;
		inst |= toLong(data);
		
		return inst;
	}
	
	// Shorter forms
	public static long getInstruction(String opcode, byte variable, int lineNum, int data){
		return getInstruction(opcode, variable, ZERO, lineNum, data);
	}
	
	public static long getInstruction(String opcode, int lineNum, int data){
		return getInstruction(opcode, ZERO, ZERO, lineNum, data);
	}
	
	public static long getInstruction(String opcode, int lineNum){
		return getInstruction(opcode, ZERO, ZERO, lineNum, 0);
	}
	
	// Return instruction data
	
	public static byte getOpcode(long inst){
		return (byte)(inst >>> 56);
	}
	
	public static String getOpcodeName(long inst){
		byte op = getOpcode(inst);
		
		if(op < 0 || op > opcodes.length)
			return "stringval";
			
		return opcodes[getOpcode(inst)];
	}
	
	public static boolean isVariable(long inst){
		return (byte)(inst >>> 55 & 0b00000001) == 1;
	}
	
	public static byte getType(long inst){
		return (byte)(inst >>> 53 & 0b00000011);
	}
	
	public static short getLineNum(long inst){
		return (short)(inst >>> 32);
	}
	
	public static int getData(long inst){
		return (int)(inst << 32 >>> 32);
	}
	
	
	
	
	// Modify instruction data
	
	public static long setOpcode(long inst, String opcode){
		
		// Clear first byte
		inst <<= 8;
		inst >>>= 8;
		
		// Set first byte
		inst |= ((long)getOpcode(opcode)) << 56;
		
		return inst;
	}
	
	
	
	// Bit handling methods
	
	private static long toLong(byte b){
		return (long)b << 56 >>> 56;
	}
	
	private static long toLong(int i){
		return (long)i << 32 >>> 32;
	}
	
	
	
	
	// Operations
	
	public static boolean isNumberOp(String op){
		for(int i = 0; i < 12; i++)
			if(op.equals(operations[i]))
				return true;
		return false;
	}
	
	public static boolean isOperation(String text){
		for(String s:operations)
			if(text.equals(s))
				return true;
		return false;
	}
	
	public static String getOperation(String op){
		switch(op){
			case "add":			return "+";
			case "subtract":	return "-";
			case "multiply":	return "*";
			case "divide":		return "/";
			case "modulo":		return "%";
			case "exponent":	return "^";
			case "not":			return "!";
			case "or":			return "||";
			case "and":			return "&&";
			case "less":		return "<";
			case "greater":		return ">";
			case "equals":		return "==";
			case "not_eq":		return "!=";
			case "less_eq":		return "<=";
			case "greater_eq":	return ">=";
			
			case "+":	return "add";
			case "-":	return "subtract";
			case "*":	return "multiply";
			case "/":	return "divide";
			case "%":	return "modulo";
			case "^":	return "exponent";
			case "!":	return "not";
			case "||":	return "or";
			case "&&":	return "and";
			case "<":	return "less";
			case ">":	return "greater";
			case "==":	return "equals";
			case "!=":	return "not_eq";
			case "<=":	return "less_eq";
			case ">=":	return "greater_eq";
		}
		
		return "none";
	}
	
	public static int getPrecedence(String op){
		switch(op){
			case "^":
				return 4;
			case "*": case "/":
				return 3;
			case "+": case "-": case "%":
				return 2;
			case "!":
				return 1;
			case "<": case ">": case "==": case "!=": case "<=": case ">=": 
				return 0;
		}
		
		return -1;
	}
}
