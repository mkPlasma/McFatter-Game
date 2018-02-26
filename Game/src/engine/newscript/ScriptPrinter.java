package engine.newscript;

import java.util.ArrayList;

import engine.newscript.bytecodegen.Instruction;
import engine.newscript.bytecodegen.InstructionSet;
import engine.newscript.lexer.Token;
import engine.newscript.parser.ParseUnit;

/**
 * 
 * Static methods for printing DScript lexical tokens, parse tree, and bytecode.
 * 
 * @author Daniel
 * 
 */

public class ScriptPrinter{
	
	public static void printTokens(Token[] tokens){
		for(Token t:tokens)
			printToken(t);
	}
	
	private static void printToken(Token t){
		String name = t.getType().name();
		System.out.println(name + (name.length() >= 8 ? "" : "\t") + "\t" + t.getValue());
	}
	
	
	public static void printParseTree(Object[] elements){
		printParseTree(elements, 0);
	}
	
	private static void printParseTree(Object[] elements, int tabs){
		
		if(tabs > 0)
			System.out.println("{");
		
		for(Object e:elements){
			
			printTabs(tabs);
			
			if(e instanceof Token){
				printToken2((Token)e);
				continue;
			}
			
			ParseUnit u = (ParseUnit)e;
			
			System.out.print(u.getType());
			printParseTree(u.getContents(), tabs + 1);
		}

		if(tabs > 0){
			printTabs(tabs - 1);
			System.out.println("}");
		}
	}
	
	private static void printToken2(Token t){
		String name = t.getType().name();
		System.out.println(name + " [" + t.getValue() + "]");
	}
	
	private static void printTabs(int tabs){
		for(int i = 0; i < tabs; i++)
			System.out.print("\t");
	}
	
	public static void printBytecode(ArrayList<Instruction> bytecode){
		
		for(int i = 0; i < bytecode.size(); i++){
			
			Instruction inst = bytecode.get(i);
			InstructionSet instS = InstructionSet.getName(inst.getOpcode());
			String num = Integer.toString(i);
			String name = instS.name();
			
			System.out.println(num + "\t" + name + "\t" + (name.length() < 8 ? "\t" : "") + (instS.hasOperand() ? inst.getOperand() : ""));
		}
	}
}
