package engine.newscript.bytecodegen;

import static engine.newscript.bytecodegen.CompilerUtil.*;
import static engine.newscript.bytecodegen.InstructionSet.*;
import static engine.newscript.lexer.TokenType.*;
import static engine.newscript.parser.ParseUtil.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import engine.newscript.DScript;
import engine.newscript.ScriptPrinter;
import engine.newscript.lexer.Token;
import engine.newscript.parser.ParseUnit;

public class BytecodeCompiler{
	
	private final DataGenerator dataGen;
	
	private DScript script;
	
	private ArrayList<Instruction> bytecode;
	
	// Compiling function
	private boolean functionsOnly, withinFunction;
	

	private Stack<ArrayList<Integer>> breakStatements;
	private Stack<ArrayList<ParseUnit>> breakUnits;
	
	
	
	public BytecodeCompiler(){
		
		dataGen = new DataGenerator();
		
		bytecode = new ArrayList<Instruction>();
		
		breakStatements = new Stack<ArrayList<Integer>>();
		breakUnits = new Stack<ArrayList<ParseUnit>>();
	}
	
	public void process(DScript script){
		
		this.script = script;
		
		dataGen.process(script);
		
		
		ArrayList<Object> parseTree = script.getParseTree();
		script.clearParseTree();
		
		bytecode.clear();
		
		// Compile functions first
		functionsOnly = true;
		withinFunction = false;
		
		for(Object o:parseTree)
			compile((ParseUnit)o);
		
		functionsOnly = false;
		withinFunction = false;
		
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
				if(!functionsOnly || (functionsOnly && withinFunction))
					compile((ParseUnit)contents[0]);
				return;
				
			case "block":
				compile((ParseUnit)contents[0]);
				return;
				
				
			case "expression":
				compileExpression(p);
				return;
				
				
			case "new_var":{
				
				// Get variable number
				Token v = (Token)contents[0];
				int var = getVarNum(v);
				
				// Create variable
				add(inst(isLocalVar(v) ? init_zero_l : init_zero, var, v));
				
				return;
			}
				
			case "new_var_def":{
				
				// Get variable number
				Token v = (Token)((ParseUnit)contents[0]).getContents()[0];
				int var = getVarNum(v);
				boolean local = isLocalVar(v);
				
				// Check if literal value
				Object o = getValue((ParseUnit)contents[2]);
				
				// If literal, add instruction to initialize
				if(o instanceof Integer){
					add(inst(local ? init_int_l : init_int, (int)o, p));
					return;
				}
				
				if(o instanceof Float){
					add(inst(local ? init_float_l : init_float, Float.floatToIntBits((float)o), p));
					return;
				}
				
				if(o instanceof Boolean){
					add(inst((Boolean)o ? (local ? init_true_l : init_true) : (local ? init_false_l : init_false), p));
					return;
				}
				
				// Non-literals
				
				// Add expression
				compileExpression((ParseUnit)contents[2]);
				
				// Create variable
				add(inst(local ? init_value_l : init_value, var, (ParseUnit)contents[0]));
				
				return;
			}
				
			case "assign":{
				
				Token t = (Token)contents[1];
				String op = t.getValue();
				
				boolean aug = !op.equals("=");
				
				// Get variable number
				Token v = (Token)contents[0];
				int var = getVarNum(v);
				boolean local = isLocalVar(v);
				
				// Augmented assign variable
				if(aug)
					add(inst(local ? load_var_l : load_var, var, (Token)contents[0]));
				
				// Add expression
				compileExpression((ParseUnit)contents[2]);
				
				// Augmented assign operation
				if(aug)
					add(inst(getOperationOpcode(op), t));
				
				// Store
				add(inst(local ? store_value_l : store_value, var, t));
				
				return;
			}
				
			case "assign_u":{
				// Get variable number
				Token v = (Token)contents[0];
				int var = getVarNum(v);
				
				// Assignment
				add(inst(getUnaryOperationOpcode(((Token)contents[1]).getValue(), isLocalVar(v)), var, (Token)contents[1]));
				
				return;
			}
				
			case "array":{
				
				ParseUnit pList = (ParseUnit)contents[0];
				Object[] list = pList.getContents();
				
				// Add elements
				
				// Single value
				if(list.length == 1)
					compile(pList);
				
				// List
				else
					for(Object o:list)
						compile((ParseUnit)o);
				
				// Add array length
				add(inst(load_int, list.length, p));
				
				// Create array
				add(inst(array_create, p));
				
				return;
			}
			
			case "array_elem":{
				
				// Defined array
				if(!(contents[0] instanceof Token)){
					
					// Add array
					compile((ParseUnit)contents[0]);
					
					// Add index
					compileExpression((ParseUnit)contents[1]);
					
					// Get element
					add(inst(array_elem, p));
				}
				
				// Array in variable
				
				// Add index
				compileExpression((ParseUnit)contents[1]);
				
				// Get element
				add(inst(isLocalVar((Token)contents[0]) ? array_elem_v_l : array_elem_v, p));
				
				return;
			}
				
			case "array_elem_assign":{
				
				// array_elem contents
				Object[] eCont = ((ParseUnit)contents[0]).getContents();
				
				Token t = (Token)contents[1];
				String op = t.getValue();
				
				boolean aug = !op.equals("=");
				
				// Get variable number
				Token v = (Token)eCont[0];
				int var = getVarNum(v);
				boolean local = isLocalVar(v);
				
				
				// Augmented assign variable
				if(aug){
					// Add index
					compileExpression((ParseUnit)eCont[1]);
					
					// Copy index to use again
					add(inst(copy_top, (ParseUnit)eCont[1]));
					
					// Get element
					add(inst(local ? array_elem_v_l : array_elem_v, var, (ParseUnit)eCont[1]));
				}
				
				// Add expression
				compileExpression((ParseUnit)contents[2]);
				
				// Augmented assign operation
				if(aug)
					add(inst(getOperationOpcode(op), t));
				
				// Store
				add(inst(local ? store_array_elem_l : store_array_elem, var, t));
				
				return;
			}
				
			case "conditional":{
				
				// Get value list
				Object[] list = ((ParseUnit)contents[3]).getContents();
				
				// Add values, backwards
				for(int i = list.length - 1; i >= 0; i--)
					compileExpression((ParseUnit)list[i]);
				
				// Add expresion
				compileExpression((ParseUnit)contents[0]);
				
				// Retrieve value
				add(inst(move_to_top, list.length - 1, p));
				
				// Remove other values
				add(inst(pop_count, list.length - 1, p));
				
				return;
			}
				
			case "break":
				// Placeholder jump statement, replaced in while_block compile
				breakStatements.peek().add(bytecode.size());
				breakUnits.peek().add(p);
				add(inst(jump, 0, p));
				return;
				
				
				
			case "func_block":{
				
				if(!functionsOnly)
					return;
				
				withinFunction = true;
				
				Object[] dCont = ((ParseUnit)contents[0]).getContents();
				int params = dCont[1] instanceof Token ? 1 : ((ParseUnit)dCont[1]).getContents().length;
				
				// Initialize parameters
				add(inst(init_params, params, p));
				
				// Compile contents of block
				compile((ParseUnit)contents[1]);
				
				// Set entry point for start of script
				script.setEntryPoint(bytecode.size());
				
				withinFunction = false;
				return;
			}
				
			case "while_block":
				
				// Get loop point
				int index = bytecode.size();
				
				// Add condition to exit loop
				compileExpression((ParseUnit)((ParseUnit)contents[0]).getContents()[0]);
				
				// Condition jump index
				int jIndex = bytecode.size();
				// Placeholder jump
				add(inst(jump_if_false, 0, p));
				
				// Add list for break statements inside loop
				breakStatements.push(new ArrayList<Integer>());
				breakUnits.push(new ArrayList<ParseUnit>());
				
				// Add contents of block
				compile((ParseUnit)contents[1]);
				
				// Add loop
				add(inst(jump, index, p));
				
				// Add condition jump
				bytecode.set(jIndex, inst(jump_if_false, bytecode.size(), p));
				
				// Set jump locations for break statements
				ArrayList<Integer> breaks = breakStatements.pop();
				ArrayList<ParseUnit> bUnits = breakUnits.pop();
				
				for(int i = 0; i < breaks.size(); i++)
					bytecode.set(breaks.get(i), inst(jump, bytecode.size(), bUnits.get(i)));
				
				return;
				
				
			case "if_block":
				
				// Add condition
				compileExpression((ParseUnit)((ParseUnit)contents[0]).getContents()[0]);
				
				// Condition jump index
				jIndex = bytecode.size();
				add(inst(jump_if_false, 0, p));
				
				// Add contents of block
				compile((ParseUnit)contents[1]);
				
				// Add condition jump
				bytecode.set(jIndex, inst(jump_if_false, bytecode.size(), p));
				
				return;
				
				
			case "if_else_chain":
				
				// Jumps into each instruction group
				Queue<Integer> stJumps = new LinkedList<Integer>();
				
				// Add conditions
				for(Object o2:contents){
					ParseUnit p2 = (ParseUnit)o2;
					
					// No condition for else
					if(p2.getType().equals("else_block")){
						stJumps.add(bytecode.size());
						bytecode.add(inst(jump, 0, p2));
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
						endJumps.add(bytecode.size());
						bytecode.add(inst(jump, 0, p));
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
			
			// If literal, add instruction to load
			if(compileValue(p))
				return;
			
			// Other values
			Object o = contents[0];
			
			// Variable
			if(o instanceof Token && ((Token)o).getType() == IDENTIFIER){
				Token v = (Token)o;
				add(inst(isLocalVar(v) ? load_var_l : load_var, getVarNum(v), (Token)o));
				return;
			}
			
			// Other (function calls, conditionals)
			compile((ParseUnit)o);
			return;
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
	
	private boolean compileValue(ParseUnit p){
		
		Object o = getValue(p);
		
		if(o instanceof Integer){
			add(inst(load_int, (int)o, p));
			return true;
		}
		
		if(o instanceof Float){
			add(inst(load_float, Float.floatToIntBits((float)o), p));
			return true;
		}
		
		if(o instanceof Boolean){
			add(inst((Boolean)o ? load_true : load_false, p));
			return true;
		}
		
		return false;
	}
	
	private int getVarNum(Token t){
		String v = t.getValue();
		return Integer.parseInt(v.substring(v.charAt(0) == 'l' ? 1 : 0));
	}
	
	private boolean isLocalVar(Token t){
		return t.getValue().charAt(0) == 'l';
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
