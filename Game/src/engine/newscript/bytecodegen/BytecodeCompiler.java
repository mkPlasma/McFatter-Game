package engine.newscript.bytecodegen;

import static engine.newscript.bytecodegen.CompilerUtil.getFileIndex;
import static engine.newscript.bytecodegen.CompilerUtil.getLineNum;
import static engine.newscript.bytecodegen.CompilerUtil.getOperationOpcode;
import static engine.newscript.bytecodegen.InstructionSet.init_false;
import static engine.newscript.bytecodegen.InstructionSet.init_float;
import static engine.newscript.bytecodegen.InstructionSet.init_int;
import static engine.newscript.bytecodegen.InstructionSet.init_true;
import static engine.newscript.bytecodegen.InstructionSet.init_value;
import static engine.newscript.bytecodegen.InstructionSet.init_zero;
import static engine.newscript.bytecodegen.InstructionSet.jump;
import static engine.newscript.bytecodegen.InstructionSet.jump_if_false;
import static engine.newscript.bytecodegen.InstructionSet.load_false;
import static engine.newscript.bytecodegen.InstructionSet.load_float;
import static engine.newscript.bytecodegen.InstructionSet.load_int;
import static engine.newscript.bytecodegen.InstructionSet.load_true;
import static engine.newscript.bytecodegen.InstructionSet.load_var;
import static engine.newscript.bytecodegen.InstructionSet.store_value;
import static engine.newscript.lexer.TokenType.IDENTIFIER;
import static engine.newscript.parser.ParseUtil.getValue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import engine.newscript.DScript;
import engine.newscript.ScriptPrinter;
import engine.newscript.lexer.Token;
import engine.newscript.parser.ParseUnit;

public class BytecodeCompiler{
	
	private final DataGenerator dataGen;
	
	private DScript script;
	
	private ArrayList<Instruction> bytecode;
	
	
	
	public BytecodeCompiler(){
		
		dataGen = new DataGenerator();
		
		bytecode = new ArrayList<Instruction>();
	}
	
	public void process(DScript script){
		
		this.script = script;
		
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
				
			case "block": case "s_block": case "statement":
				compile((ParseUnit)contents[0]);
				return;
				
				
			case "expression":
				compileExpression(p);
				return;
				
				
			case "new_var":
				
				// Get variable number
				int var = Integer.parseInt(((Token)contents[0]).getValue());
				
				// Create variable
				add(inst(init_zero, var, (Token)contents[0]));
				
				return;
				
				
			case "new_var_def":
				
				// Get variable number
				var = Integer.parseInt(((Token)((ParseUnit)contents[0]).getContents()[0]).getValue());
				
				// Check if literal value
				Object o = getValue((ParseUnit)contents[2]);
				
				// If literal, add instruction to initialize
				if(o instanceof Integer){
					add(inst(init_int, (int)o, p));
					return;
				}
				
				if(o instanceof Float){
					add(inst(init_float, Float.floatToIntBits((float)o), p));
					return;
				}
				
				if(o instanceof Boolean){
					add(inst((Boolean)o ? init_true : init_false, p));
					return;
				}
				
				// Non-literals
				
				// Add expression
				compileExpression((ParseUnit)contents[2]);
				
				// Create variable
				add(inst(init_value, var, (ParseUnit)contents[0]));
				
				return;
				
				
			case "assign":
				// Add expression
				compileExpression((ParseUnit)contents[2]);
				
				// Get variable number
				var = Integer.parseInt(((Token)contents[0]).getValue());
				
				Token t = (Token)contents[1];
				String op = t.getValue();
				
				// Standard assignment
				if(op.equals("=")){
					add(inst(store_value, var, t));
					return;
				}
				
				// Augmented assign
				add(inst(load_var, var, (Token)contents[0]));
				add(inst(getOperationOpcode(op), t));
				add(inst(store_value, var, t));
				
				return;
				
				
			case "assign_u":
				// Get variable number
				var = Integer.parseInt(((Token)contents[0]).getValue());
				
				// Assignment
				add(inst(getOperationOpcode(((Token)contents[1]).getValue()), var, (Token)contents[1]));
				
				return;
				
				
			case "while_block":
				
				// Get loop point
				int index = bytecode.size();
				
				// Add condition to exit loop
				compileExpression((ParseUnit)((ParseUnit)contents[0]).getContents()[0]);
				
				// Condition jump index
				int jIndex = bytecode.size();
				
				// Add contents of block
				compile((ParseUnit)contents[1]);
				
				// Add loop
				add(inst(jump, index, p));
				
				// Add condition jump
				bytecode.add(jIndex, inst(jump_if_false, bytecode.size() + 1, p));
				
				return;
				
				
			case "if_block":
				
				// Add condition
				compileExpression((ParseUnit)((ParseUnit)contents[0]).getContents()[0]);
				
				// Condition jump index
				jIndex = bytecode.size();
				
				// Add contents of block
				compile((ParseUnit)contents[1]);
				
				// Add condition jump
				bytecode.add(jIndex, inst(jump_if_false, bytecode.size() + 1, p));
				
				return;
				
				
			case "if_else_chain":
				
				// cond
				// jump
				// cond
				// jump
				// if statements
				// jump end
				// else if statements
				// jump end
				// else statement
				// end
				
				// Jumps into each instruction group
				Queue<Integer> stJumps = new LinkedList<Integer>();
				
				// Add conditions
				for(Object o2:contents){
					ParseUnit p2 = (ParseUnit)o2;
					
					// End of chain
					if(p2.getType().equals("else"))
						break;
					
					// Add condition
					ParseUnit p3 = p2.getContents()[0]
				}
				
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
				add(inst(load_int, (int)o, p));
				return;
			}
			
			if(o instanceof Float){
				add(inst(load_float, Float.floatToIntBits((float)o), p));
				return;
			}
			
			if(o instanceof Boolean){
				add(inst((Boolean)o ? load_true : load_false, p));
				return;
			}
			
			// Other values
			o = contents[0];
			
			// Variable
			if(o instanceof Token && ((Token)o).getType() == IDENTIFIER){
				add(inst(load_var, Integer.parseInt(((Token)o).getValue()), (Token)o));
				return;
			}
		}
		
		// Standard expression
		if(contents.length == 3){
			
			// Add values
			compileExpression((ParseUnit)contents[0]);
			compileExpression((ParseUnit)contents[2]);
			
			// Add operation
			add(inst(getOperationOpcode(((Token)contents[1]).getValue()), (Token)contents[1]));
			
			return;
		}
		
		// Unary operation
		compileExpression((ParseUnit)contents[1]);
		add(inst(getOperationOpcode(((Token)contents[0]).getValue()), (Token)contents[0]));
	}
	
	
	
	private void add(Instruction i){
		bytecode.add(i);
	}
	
	public Instruction inst(InstructionSet i, Token t){
		return new Instruction(InstructionSet.getOpcode(i), getFileIndex(t, script), getLineNum(t));
	}
	
	public Instruction inst(InstructionSet i, ParseUnit p){
		return new Instruction(InstructionSet.getOpcode(i), getFileIndex(p, script), getLineNum(p));
	}
	
	public Instruction inst(InstructionSet i, int val, Token t){
		return new Instruction(InstructionSet.getOpcode(i), val, getFileIndex(t, script), getLineNum(t));
	}
	
	public Instruction inst(InstructionSet i, int val, ParseUnit p){
		return new Instruction(InstructionSet.getOpcode(i), val, getFileIndex(p, script), getLineNum(p));
	}
}
