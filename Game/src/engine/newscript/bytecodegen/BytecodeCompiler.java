package engine.newscript.bytecodegen;

import static engine.newscript.bytecodegen.CompilerUtil.*;
import static engine.newscript.bytecodegen.InstructionSet.*;
import static engine.newscript.lexer.TokenType.*;
import static engine.newscript.parser.ParseUtil.*;

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
				
				Token t = (Token)contents[1];
				String op = t.getValue();
				
				boolean aug = !op.equals("=");
				
				// Get variable number
				var = Integer.parseInt(((Token)contents[0]).getValue());
				
				// Augmented assign variable
				if(aug)
					add(inst(load_var, var, (Token)contents[0]));
				
				// Add expression
				compileExpression((ParseUnit)contents[2]);
				
				// Augmented assign operation
				if(aug)
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
					
					// No condition for else
					if(p2.getType().equals("else_block")){
						bytecode.add(inst(jump, 0, p2));
						stJumps.add(bytecode.size() - 1);
						break;
					}
					
					// Add condition
					compileExpression((ParseUnit)((ParseUnit)p2.getContents()[0]).getContents()[0]);
					
					// Add jump
					bytecode.add(inst(jump_if_true, 0, p2));
					stJumps.add(bytecode.size() - 1);
				}
				
				// Jump to end of chain
				ArrayList<Integer> endJumps = new ArrayList<Integer>();
				
				// Add contents of block
				for(Object o2:contents){
					ParseUnit p2 = (ParseUnit)o2;
					
					boolean isElse = p2.getType().equals("else_block");
					
					// Set initial jump into statements
					int i = stJumps.remove();
					bytecode.set(i, inst(bytecode.get(i).getOpcode(), bytecode.size(), p2));
					
					// Add contents
					compile((ParseUnit)p2.getContents()[isElse ? 0 : 1]);
					
					 // Jump to end
					if(!isElse){
						bytecode.add(inst(jump, 0, p));
						endJumps.add(bytecode.size() - 1);
					}
				}
				
				// Add end jumps
				for(int i:endJumps)
					bytecode.set(i, inst(jump, bytecode.size(), p));
				
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
	
	public Instruction inst(byte i, int val, ParseUnit p){
		return new Instruction(i, val, getFileIndex(p, script), getLineNum(p));
	}
}
