package engine.script;

import java.util.ArrayList;

/*
 * 		ScriptFunctions.java
 * 		
 * 		Purpose:	DScript reference and bytecode functions.
 * 		Notes:		WIP
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				current
 * 		Changes:			
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
		
		// Functions
		"function",			// Function jump location
		"call_func",		// Calls function with set parameters
		"call_func_b",		// Calls built-in function
		"set_param",		// Set function parameter
		"get_param",		// Store function parameter into register
		"param_inc",		// Use new parameter queue
		"return",			// Returns register value
		"return_void",		// Return no value
		"dot",				// Dot separator
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
		"in",
		
		"function",
		"task",
		"return",
		
		"true",
		"false",
		
		"wait",
	};
	
	public static final String[] builtInFunctions = {
		
		// General
		"print:1",
		
		// Math
		"pi:0",
		"abs:1",
		"degrees:1",
		"radians:1",
		"sin:1",
		"cos:1",
		"tan:1",
		"ain:1",
		"acos:1",
		"atan:1",
		"atan2:2",
		"pow:2",
		"min:2",
		"max:2",
		
		// Array
		"length:d0",
		"add:d1",
		"remove:d0",
		"remove:d1",
		
		// Bullet
		"bullet:6",
	};
	
	public static final String[] operations = {
		"+", "-", "*", "/", "%", "^", "<", ">", "==", "<=", ">=", "!", "||", "&&",
		
		"add", "subtract", "multiply", "divide", "modulo", "exponent", "not", "or", "and",
		"less", "greater", "equals", "less_eq", "greater_eq",
	};
	
	public static final byte INT = 0, FLOAT = 1, BOOLEAN = 2, STRING = 3,
							 VALUE = 0, VARIABLE = 1,
							 ZERO = 0;
	
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
		
		Object o1 = null;
		Object o2 = null;
		
		if(params.length > 0){
			o1 = params[0];
			
			if(params.length > 1)
				o2 = params[1];
		}
		
		int p = getBuiltInFunctionParameterCount(index);
		
		switch(getBuiltInFunctionName(index)){
			case "abs": case "degrees": case "radians": case "sin": case "cos": case "tan": case "asin": case "acos": case "atan":
				return o1 instanceof Integer || o1 instanceof Float;
			
			case "atan2": case "pow": case "min": case "max":
				return (o1 instanceof Integer || o1 instanceof Float) && (o2 instanceof Integer || o2 instanceof Float);
			
			case "length":
				return o1 instanceof ArrayList || o1 instanceof String;
			
			case "add":
				return o1 instanceof ArrayList && !(o2 instanceof ArrayList);
			
			case "remove":
				return o1 instanceof ArrayList && (p == 0 || o2 instanceof Integer);
			
			case "bullet":
				Object o3 = params[2];
				Object o4 = params[2];
				Object o5 = params[2];
				Object o6 = params[2];
				
				return o1 instanceof Integer && o2 instanceof Integer &&
					  (o3 instanceof Integer || o3 instanceof Float) &&
					  (o4 instanceof Integer || o4 instanceof Float) &&
					  (o5 instanceof Integer || o5 instanceof Float) &&
					  (o6 instanceof Integer || o6 instanceof Float);
		}
		
		return true;
	}
	
	// Function requires dot separator
	public static boolean builtInFunctionDot(int index){
		return builtInFunctions[index].contains(":d");
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
		for(int i = 0; i < 11; i++)
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
			case "exponent":		return "^";
			case "not":			return "!";
			case "or":			return "||";
			case "and":			return "&&";
			case "less":		return "<";
			case "greater":		return ">";
			case "equals":		return "==";
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
			case "<": case ">": case "=": case "<=": case ">=": 
				return 0;
		}
		
		return -1;
	}
}
