package engine.script;

import java.util.ArrayList;

import static engine.script.ScriptFunctions.*;

/*
 * 		BytecodePrinter.java
 * 		
 * 		Purpose:	Prints compiled DScript bytecode.
 * 		Notes:		WIP
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				10/2
 * 		Changes:			
 */

public class BytecodePrinter{
	
	public static void printBytecode(ArrayList<Long> bytecode, String fileName){
		
		System.out.println("\nPrinting bytecode of " + fileName + "(" + (bytecode.size()*8) + " bytes):\n");
		
		for(int i = 0; i < bytecode.size(); i++){
			
			long inst = bytecode.get(i);
			
			String opcode = opcodes[getOpcode(inst)];
			
			System.out.println(i + "\t" + getLineNum(inst) + "\t" + opcode + (opcode.length() < 8 ? "\t\t" : "\t") + getInfo(inst, opcode));
		}
	}
	
	private static String getInfo(long inst, String opcode){
		
		boolean var = isVariable(inst);
		byte type = getType(inst);
		
		// Get first 4 bytes
		int data = getData(inst);
		
		String info = "";
		//case "load": case "store": case "create_var": case "delete_var": case "postfix_val": case "increment": case "decrement":
		
		// Variable number
		if(var || opcode.equals("store") || opcode.equals("create_var") || opcode.equals("delete_var")
			|| opcode.equals("increment") || opcode.equals("decrement")){
			
			if(data == 0)
				info += "register";
			else{
				info += "variable #" + data;
				if(type == 4)
					info += " (object)";
			}
		}
		
		// While loop
		else if(opcode.equals("while") || opcode.equals("end_while"))
			info += "loop #" + data;
		
		// Literal value
		else if(opcode.equals("load") || opcode.equals("exp_val")){
			switch(type){
				case 0: // int
					info += data;
					break;
				case 1: // float
					info += Float.intBitsToFloat(data);
					break;
				case 2: // boolean
					info += data == 1 ? "true" : "false";
			}
		}
		
		return info;
	}
}