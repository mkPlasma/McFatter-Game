package engine.newscript.bytecodegen;

import static engine.newscript.bytecodegen.InstructionSet.*;

import java.util.ArrayList;

public class CompilerUtil{
	
	public static InstructionSet getOperationOpcode(String op){
		
		switch(op){
			case "+":	return op_add;
			case "-":	return op_sub;
			case "*":	return op_mult;
			case "/":	return op_div;
			case "%":	return op_mod;
			case "^":	return op_exp;
			
			case "==":	return op_eq;
			case "<":	return op_lt;
			case ">":	return op_gt;
			case "<=":	return op_lte;
			case ">=":	return op_gte;
			case "!=":	return op_neq;
			
			case "||":	return op_or;
			case "&&":	return op_and;
			case "!":	return op_not;
		}
		
		return null;
	}
	
	
	// Shorthand functions
	
	public static Instruction inst(InstructionSet i){
		return new Instruction(InstructionSet.getOpcode(i));
	}
	
	public static Instruction inst(InstructionSet i, int val){
		return new Instruction(InstructionSet.getOpcode(i), val);
	}
	
	public static ArrayList<Instruction> inst2(InstructionSet i){
		ArrayList<Instruction> a = new ArrayList<Instruction>();
		a.add(inst(i));
		return a;
	}

	public static ArrayList<Instruction> inst2(InstructionSet i, int val){
		ArrayList<Instruction> a = new ArrayList<Instruction>();
		a.add(inst(i, val));
		return a;
	}
}
