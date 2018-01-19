package engine.newscript.bytecodegen;

import static engine.newscript.bytecodegen.InstructionSet.*;
import static engine.newscript.bytecodegen.CompilerUtil.*;
import static engine.newscript.parser.ParseUtil.*;
import static engine.newscript.lexer.TokenType.*;

import java.util.ArrayList;

import engine.newscript.DScript;
import engine.newscript.ScriptPrinter;
import engine.newscript.lexer.Token;
import engine.newscript.parser.ParseUnit;

public class BytecodeCompiler{
	
	private final DataGenerator dataGen;
	
	
	private ArrayList<Instruction> bytecode;
	
	
	
	public BytecodeCompiler(){
		
		dataGen = new DataGenerator();
		
		bytecode = new ArrayList<Instruction>();
	}
	
	public void process(DScript script){
		
		dataGen.process(script);
		
		
		ArrayList<Object> parseTree = script.getParseTree();
		script.clearParseTree();
		
		bytecode.clear();
		
		for(Object o:parseTree)
			compile((ParseUnit)o);
		
		
		script.setBytecode(bytecode.toArray(new Instruction[0]));
		
		ScriptPrinter.printBytecode(bytecode);
	}
	
	private void compile(ParseUnit p){
		
		Object[] contents = p.getContents();
		
		switch(p.getType()){
				
				
			case "statements":
				for(Object o:contents)
					compile((ParseUnit)o);
				return;
				
				
			case "statement":
				compile((ParseUnit)contents[0]);
				return;
				
				
			case "expression":
				compileExpression(p);
				return;
				
				
			case "new_var_def":
				// Add expression
				compileExpression((ParseUnit)contents[2]);
				
				// Get variable number
				int var = Integer.parseInt(((Token)((ParseUnit)contents[0]).getContents()[0]).getValue());
				
				// Create variable
				add(inst(store_value, var));
				
				return;
		}
	}
	
	
	private void compileExpression(ParseUnit p){
		
		Object[] contents = p.getContents();
		
		// Value
		if(contents.length == 1){
			
			// Check if literal value
			Object o = getValue(p);
			
			// If literal, add instruction to load
			if(o instanceof Integer){
				add(inst(load_int, (int)o));
				return;
			}
			
			if(o instanceof Float){
				add(inst(load_float, Float.floatToIntBits((float)o)));
				return;
			}
			
			if(o instanceof Boolean){
				add(inst((Boolean)o ? load_true : load_false));
				return;
			}
			
			// Other values
			o = contents[0];
			
			// Variable
			if(o instanceof Token && ((Token)o).getType() == IDENTIFIER){
				add(inst(load_var, Integer.parseInt(((Token)o).getValue())));
				return;
			}
		}
		
		// Standard expression
		if(contents.length == 3){
			
			// Add values
			compileExpression((ParseUnit)contents[0]);
			compileExpression((ParseUnit)contents[2]);
			
			// Add operation
			add(inst(getOperationOpcode(((Token)contents[1]).getValue())));
			
			return;
		}
	}
	
	
	
	private void add(Instruction i){
		bytecode.add(i);
	}
	
	private void add(ArrayList<Instruction> bc){
		bytecode.addAll(bc);
	}
}
