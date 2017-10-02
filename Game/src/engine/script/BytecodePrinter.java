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
		
		System.out.println("\nPrinting bytecode of " + fileName + ":\n");
		
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
		
		switch(opcode){
			
			// Add value/variable index
			case "load": case "create_var": case "postfix_val": case "add": case "subtract": case "multiply": case "divide":
			case "modulo": case "and": case "or": case "less": case "greater": case "equals": case "less_eq": case "greater_eq":
				
				if(isOperation(opcode) && type == POSTFIX){
					info += "postfix";
					break;
				}
				
				if(var){
					if(data == 0)
						info += "register";
					else{
						info += "variable #" + data;
						if(type == 4)
							info += " (object)";
					}
					break;
				}
				else{
					switch(type){
						case 0: // int
							info += data;
							break;
						case 1: // float
							info += Float.intBitsToFloat(data);
							break;
						case 2: // boolean
							info += data == 1 ? "true" : "false";
							break;
					}
				}
				
				break;
		}
		
		return info;
	}
}