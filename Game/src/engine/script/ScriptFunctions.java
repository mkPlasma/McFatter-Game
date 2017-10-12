package engine.script;

/*
 * 		ScriptFunctions.java
 * 		
 * 		Purpose:	DScript reference and bytecode functions.
 * 		Notes:		WIP
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				10/9
 * 		Changes:			
 */

public class ScriptFunctions{
	
	// Note: Registers are variables 0 and 1
	// All operations are postfix if set to type object
	
	public static final String[] opcodes = {
		"none",				// No oeration
		
		// Memory
		"load",				// Load a value into register
		"store",			// Store register into variable
		"create_var",		// Creates a new variable
		"delete_var",		// Deletes variable
		"exp_val",			// Declare expression value
		"exp_end",			// End expression and save result in register
		"exp_inc",			// Create new expression, used by function calls in expressions
		
		// Arithmetic
		"add",				// Adds value to register
		"subtract",			// Subtracts value from register
		"multiply",			// Multiplies register by value
		"divide",			// Divides register by value
		"modulo",			// Sets register remainder
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
		"call_func",		// Call function with set parameters
		"set_param",		// Set function parameter
		"get_param",		// Store function parameter into register
		"return",		// Returns register value
		"return_void",	// Return no value
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
		"print(a)",
	};
	
	public static final String[] operations = {
		"+", "-", "*", "/", "%", "<", ">", "==", "<=", ">=", "!", "||", "&&",
		
		"add", "subtract", "multiply", "divide", "modulo", "not", "or", "and",
		"less", "greater", "equals", "less_eq", "greater_eq",
	};
	
	public static final byte INT = 0, FLOAT = 1, BOOLEAN = 2, OBECT = 3,
							 VALUE = 0, VARIABLE = 1,
							 REG1 = 0, REG2 = 1,
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
				return toByte(i);
		
		// As operation
		return getOpcode(getOperation(opcode));
	}
	
	public static boolean isReservedWord(String s){
		for(String r:reservedWords)
			if(s.equals(r))
				return true;
		return false;
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

	public static long getInstruction(String opcode, byte type, int lineNum){
		return getInstruction(opcode, ZERO, type, lineNum, 0);
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
	
	public static long setVariable(long inst, byte variable){
		
		// Clear bit
		inst &= 0b1111111101111111111111111111111111111111111111111111111111111111l;
		
		// Set variable
		inst |= ((toLong(variable) & 0b1) << 55);
		
		return inst;
	}
	
	public static long setType(long inst, byte type){
		
		// Clear bits
		inst &= 0b1111111110011111111111111111111111111111111111111111111111111111l;
		
		// Set type
		inst |= ((toLong(type) & 0b11) << 53);
		
		return inst;
	}
	
	
	
	
	// Bit handling methods
	
	private static byte toByte(short s){
		return (byte)(((byte)0)|s);
	}
	
	private static long toLong(byte b){
		return 0l | ((long)b) << 56 >>> 56;
	}
	
	private static long toLong(int i){
		return 0l | ((long)i << 32 >>> 32);
	}
	
	
	
	
	// Operations
	
	public static boolean isNumber(long inst){
		byte type = getType(inst);
		return type == INT || type == FLOAT;
	}
	
	public static boolean isNumberOp(String op){
		for(int i = 0; i < 10; i++)
			if(op.equals(operations[i]))
				return true;
		return false;
	}
	
	public static boolean opReturnsNumber(String op){
		for(int i = 0; i < 5; i++)
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
