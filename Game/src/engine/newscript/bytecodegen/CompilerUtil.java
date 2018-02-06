package engine.newscript.bytecodegen;

import static engine.newscript.bytecodegen.InstructionSet.*;

import engine.newscript.DScript;
import engine.newscript.lexer.Token;
import engine.newscript.parser.ParseUnit;

public class CompilerUtil{
	
	public static InstructionSet getOperationOpcode(String op){
		
		switch(op){
			case "+":	case "+=":	return op_add;
			case "-":	case "-=":	return op_sub;
			case "*":	case "*=":	return op_mult;
			case "/":	case "/=":	return op_div;
			case "%":	case "%=":	return op_mod;
			case "^":	case "^=":	return op_exp;
			
			case "==":	return op_eq;
			case "<":	return op_lt;
			case ">":	return op_gt;
			case "<=":	return op_lte;
			case ">=":	return op_gte;
			case "!=":	return op_neq;
			
			case "||":	return op_or;
			case "&&":	return op_and;
			case "!":	return op_not;
			
			case "~": case "~=":	return op_concat;
		}
		
		return null;
	}
	
	public static InstructionSet getUnaryOperationOpcode(String op, boolean local){
		
		switch(op){
			case "++":	return local ? op_inc_l : op_inc;
			case "--":	return local ? op_dec_l : op_dec;
			case "!!":	return local ? op_inv_l : op_inv;
		}
		
		return null;
	}
	
	public static int getFileIndex(Object o, DScript script){
		return script.getFileIndex(o instanceof Token ? ((Token)o).getFile() : ((ParseUnit)o).getFile());
	}
	
	public static int getLineNum(Object o){
		return o instanceof Token ? ((Token)o).getLineNum() : ((ParseUnit)o).getLineNum();
	}
}
