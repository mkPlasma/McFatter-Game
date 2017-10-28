package engine.script;

import java.util.ArrayList;

import static engine.script.ScriptFunctions.*;

/*
 * 		BytecodePrinter.java
 * 		
 * 		Purpose:	Prints compiled DScript bytecode.
 * 		Notes:		For debug use
 * 		
 */

public class BytecodePrinter{
	
	public static void printBytecode(ArrayList<Long> bytecode, String fileName){
		
		System.out.println("\nPrinting bytecode of " + fileName + "(" + (bytecode.size()*8) + " bytes):\n");
		
		for(int i = 0; i < bytecode.size(); i++){
			
			long inst = bytecode.get(i);
			
			String opcode = getOpcodeName(inst);
			
			System.out.println(i + "\t" + getLineNum(inst) + "\t" + opcode + (opcode.length() < 8 ? "\t\t" : "\t") + getInfo(bytecode, i));
			
			// Skip next longs if string
			if(getType(inst) == STRING)
				i += getData(inst) + 1;
		}
		
		System.out.println();
	}
	
	private static String getInfo(ArrayList<Long> bytecode, int i){
		
		long inst = bytecode.get(i);
		String opcode = getOpcodeName(inst);
		
		boolean var = isVariable(inst);
		byte type = getType(inst);
		
		// Get first 4 bytes
		int data = getData(inst);
		
		// Variable number
		if(var || opcode.equals("store") || opcode.equals("create_var") || opcode.equals("delete_var")
			|| opcode.equals("increment") || opcode.equals("decrement") || opcode.equals("get_param")){
			
			if(data == 0)
				return "register";
			else
				return "variable #" + data;
		}
		
		switch(opcode){
			// While loop
			case "while": case "end_while":
				return "loop #" + data;
			
			// Function
			case "function": case "task": case "call_func":
				return "function #" + data;
			
			case "call_func_b":
				return getBuiltInFunctionName(data);
			
			// Literal value
			case "load": case "exp_val":
				switch(type){
					case INT:
						return Integer.toString(data);
					case FLOAT:
						return Float.toString(Float.intBitsToFloat(data));
					case BOOLEAN:
						return data == 1 ? "true" : "false";
					case STRING:
						ArrayList<Long> list = new ArrayList<Long>();
						for(int j = i + 1; j <= i + 1 + data; j++)
							list.add(bytecode.get(j));
						
						return '"' + convertString(list) + '"';
				}
			}
		
		return "";
	}
}