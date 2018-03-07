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

/**
 * 
 * Generates bytecode from parse tree.
 * 
 * @author Daniel
 *
 */

public class BytecodeCompiler{
	
	private final DataGenerator dataGen;
	
	private DScript script;
	
	private ArrayList<Instruction> bytecode;
	
	// Compiling functions
	private boolean functionsOnly, withinFunction;
	private ArrayList<Integer> functions;
	private ArrayList<Boolean> task;
	
	// Store break statements while compiling loop
	private Stack<ArrayList<Integer>> breakStatements;
	private Stack<ArrayList<ParseUnit>> breakUnits;
	
	// Store bytecode for returnif comparisons
	private ArrayList<Instruction> returnIfBytecode;
	
	
	
	public BytecodeCompiler(){
		
		dataGen = new DataGenerator();
		
		bytecode = new ArrayList<Instruction>();
		

		functions	= new ArrayList<Integer>();
		task		= new ArrayList<Boolean>();
		
		breakStatements = new Stack<ArrayList<Integer>>();
		breakUnits = new Stack<ArrayList<ParseUnit>>();
		
		returnIfBytecode = new ArrayList<Instruction>();
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
		
		// Compile parse units
		for(Object o:parseTree)
			compile((ParseUnit)o);
		
		
		// Replace function calls
		for(int i = 0; i < bytecode.size(); i++){
			
			Instruction inst = bytecode.get(i);
			InstructionSet name = InstructionSet.getName(inst.getOpcode());
			
			if(name == jump_func || name == jump_func_r){
				
				int funcNum = inst.getOperand();
				boolean exp = name == jump_func_r;
				
				bytecode.set(i, inst(task.get(funcNum) ? (exp ? jump_branch_r : jump_branch) : (exp ? jump_func_r : jump_func), functions.get(funcNum), inst.getFileIndex(), inst.getLineNum()));
			}
		}
		
		script.setBytecode(bytecode.toArray(new Instruction[0]));
		
		ScriptPrinter.printBytecode(bytecode);
	}
	
	private void compile(ParseUnit p){
		
		Object[] contents = p.getContents();
		
		switch(p.getType()){
			
			case "statements":
				for(Object o:contents)
					if(!functionsOnly || (functionsOnly && withinFunction) || (o instanceof ParseUnit && (((ParseUnit)o).getType().equals("func_block")) || ((ParseUnit)o).getType().equals("task_block")))
						compile((ParseUnit)o);
				return;
				
			case "statement":
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
				add(isLocalVar(v) ? store_zero_l : store_zero, var, v);
				
				return;
			}
				
			case "new_var_def":{
				
				// Get variable number
				Token v = (Token)((ParseUnit)contents[0]).getContents()[0];
				int var = getVarNum(v);
				boolean local = isLocalVar(v);
				
				// Add expression
				compileExpression((ParseUnit)contents[2]);
				
				// Create variable
				add(local ? store_value_l : store_value, var, (ParseUnit)contents[0]);
				
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
					add(local ? load_var_l : load_var, var, (Token)contents[0]);
				
				// Add expression
				compileExpression((ParseUnit)contents[2]);
				
				// Augmented assign operation
				if(aug)
					add(getOperationOpcode(op), t);
				
				// Store
				add(local ? store_value_l : store_value, var, t);
				
				return;
			}
				
			case "assign_u":{
				// Get variable number
				Token v = (Token)contents[0];
				int var = getVarNum(v);
				
				// Assignment
				add(getUnaryOperationOpcode(((Token)contents[1]).getValue(), isLocalVar(v)), var, (Token)contents[1]);
				
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
				add(load_int, list.length, p);
				
				// Create array
				add(array_create, p);
				
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
					add(array_elem, p);
				}
				
				// Array in variable
				
				// Add index
				compileExpression((ParseUnit)contents[1]);
				
				// Get element
				add(isLocalVar((Token)contents[0]) ? array_elem_v_l : array_elem_v, p);
				
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
					add(copy_top, (ParseUnit)eCont[1]);
					
					// Get element
					add(local ? array_elem_v_l : array_elem_v, var, (ParseUnit)eCont[1]);
				}
				
				// Add expression
				compileExpression((ParseUnit)contents[2]);
				
				// Augmented assign operation
				if(aug)
					add(getOperationOpcode(op), t);
				
				// Store
				add(local ? store_array_elem_l : store_array_elem, var, t);
				
				return;
			}
			
			case "array_elem_assign_u":{
				
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
					add(copy_top, (ParseUnit)eCont[1]);
					
					// Get element
					add(local ? array_elem_v_l : array_elem_v, var, (ParseUnit)eCont[1]);
				}
				
				// Add expression
				compileExpression((ParseUnit)contents[2]);
				
				// Augmented assign operation
				if(aug)
					add(getOperationOpcode(op), t);
				
				// Store
				add(local ? store_array_elem_l : store_array_elem, var, t);
				
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
				add(move_to_top, list.length - 1, p);
				
				// Remove other values
				add(pop_count, list.length - 1, p);
				
				return;
			}
				
			case "func_call": case "func_call_bi":{
				
				if(contents.length > 1){
					// Add parameters
					ParseUnit p2 = (ParseUnit)contents[1];
					
					// Single parameter
					if(p2.getType().equals("expression"))
						compileExpression(p2);
					
					// Multiple parameters
					else{
						Object[] list = p2.getContents();
						
						for(Object o:list)
							compileExpression((ParseUnit)o);
					}
				}
				
				// Whether function call is in an expression/should accept a return value
				boolean exp = p.isWithin("expression");
				
				// Standard function call
				if(p.getType().equals("func_call")){
					// Function number
					int funcNum = Integer.parseInt(((Token)contents[0]).getValue());
					
					// Add placeholder jump to replace later
					add(exp ? jump_func_r : jump_func, funcNum, p);
					return;
				}
				
				// Built-in function call
				add(exp ? func_bi_r : func_bi, Integer.parseInt(((Token)contents[0]).getValue()), p);
				
				return;
			}
				
			case "break":
				// Placeholder jump statement, replaced in while_block compile
				breakStatements.peek().add(bytecode.size());
				breakUnits.peek().add(p);
				add(jump, 0, p);
				return;
				
				
			case "return":
				
				// Return void
				if(contents.length == 1){
					add(return_void, p);
					return;
				}
				
				// Return value
				
				// Add value
				compileExpression((ParseUnit)contents[1]);
				add(return_value, p);
				
				return;
				
				
			case "returnif":
				
				// Get size before adding expression
				int s = bytecode.size();
				
				// Compile expression
				compileExpression((ParseUnit)contents[0]);
				
				returnIfBytecode.clear();
				
				// Get added expression bytecode
				int s2 = bytecode.size();
				
				for(int i = s; i < s2; i++)
					returnIfBytecode.add(bytecode.remove(s));
				
				// Add return
				returnIfBytecode.add(inst(return_if_true, p));
				
				return;
				
				
			case "wait":
				
				// Single frame
				if(contents.length == 1){
					add(wait, 1, p);
					return;
				}
				
				// No expression
				Object v = getValue(contents[1]);
				
				if(v instanceof Integer || v instanceof Float){
					add(wait, v instanceof Integer ? (int)v : (int)(float)v, p);
					
					// Add returnif check if necessary
					if(!returnIfBytecode.isEmpty())
						bytecode.addAll(returnIfBytecode);
					
					return;
				}
				
				// Expression
				compileExpression((ParseUnit)contents[1]);
				add(wait_value, p);
				
				// Add returnif check if necessary
				if(!returnIfBytecode.isEmpty())
					bytecode.addAll(returnIfBytecode);
				
				return;
				
				
			case "func_block": case "task_block":{
				
				if(!functionsOnly || (functionsOnly && withinFunction))
					return;
				
				// Add other functions within current one first
				if(((ParseUnit)contents[1]).getType().equals("block")){
					Object[] statements = ((ParseUnit)((ParseUnit)contents[1]).getContents()[0]).getContents();
					
					for(Object o:statements)
						if(o instanceof ParseUnit && (((ParseUnit)o).getType().equals("func_block") || ((ParseUnit)o).getType().equals("task_block")))
							compile((ParseUnit)o);
				}
					
				withinFunction = true;
				
				Object[] dCont = ((ParseUnit)contents[0]).getContents();
				int params = dCont.length == 1 ? 0 : dCont[1] instanceof Token ? 1 : ((ParseUnit)dCont[1]).getContents().length;
				
				int funcNum = Integer.parseInt(((Token)dCont[0]).getValue());
				
				// Add function bytecode index
				while(functions.size() < funcNum + 1)
					functions.add(0);
				
				while(task.size() < funcNum + 1)
					task.add(false);
				
				functions.set(funcNum, bytecode.size());
				task.set(funcNum, p.getType().equals("task_block"));
				
				// Initialize parameters
				if(params > 0)
					add(init_params, params, p);
				
				// Compile contents of block
				compile((ParseUnit)contents[1]);
				
				// Add void return if necessary
				Instruction i = bytecode.get(bytecode.size() - 1);
				
				if(i.getOpcode() != InstructionSet.getOpcode(return_void) && i.getOpcode() != InstructionSet.getOpcode(return_value))
					add(return_void, p);
				
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
				add(jump_if_false, 0, p);
				
				// Add list for break statements inside loop
				breakStatements.push(new ArrayList<Integer>());
				breakUnits.push(new ArrayList<ParseUnit>());
				
				// Add contents of block
				compile((ParseUnit)contents[1]);
				
				// Add loop
				add(jump, index, p);
				
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
				add(jump_if_false, 0, p);
				
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
						add(jump, 0, p2);
						break;
					}
					
					// Add condition
					compileExpression((ParseUnit)((ParseUnit)p2.getContents()[0]).getContents()[0]);
					
					// Add jump
					add(jump_if_true, 0, p2);
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
						add(jump, 0, p);
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
		
		// Parenthesized
		if(contents[0] instanceof ParseUnit && ((ParseUnit)contents[0]).getType().equals("expression_p"))
			contents = ((ParseUnit)((ParseUnit)contents[0]).getContents()[0]).getContents();
		
		// Value
		if(contents.length == 1){
			
			// If literal, add instruction to load
			if(compileValue(p))
				return;
			
			// Other values
			Object o = contents[0];
			
			if(o instanceof Token){
				Token t = (Token)o;
				
				// Variables
				if(t.getType() == IDENTIFIER)
					add(isLocalVar(t) ? load_var_l : load_var, getVarNum(t), t);
				
				else if(t.getType() == CONST)
					add(load_const, Integer.parseInt(t.getValue()), t);
				
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
			add(getOperationOpcode(((Token)contents[1]).getValue()), (Token)contents[1]);
			
			return;
		}
		
		// Unary operation
		compileExpression((ParseUnit)contents[1]);
		add(getOperationOpcode(((Token)contents[0]).getValue()), (Token)contents[0]);
	}
	
	private boolean compileValue(ParseUnit p){
		
		Object o = getValue(p);
		
		if(o instanceof Integer){
			add(load_int, (int)o, p);
			return true;
		}
		
		if(o instanceof Float){
			add(load_float, Float.floatToIntBits((float)o), p);
			return true;
		}
		
		if(o instanceof Boolean){
			add((Boolean)o ? load_true : load_false, p);
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
	
	
	private void add(InstructionSet i, Token t){
		bytecode.add(inst(i, t));
	}
	
	public void add(InstructionSet i, ParseUnit p){
		bytecode.add(inst(i, p));
	}
	
	public void add(InstructionSet i, int val, Token t){
		bytecode.add(inst(i, val, t));
	}
	
	public void add(InstructionSet i, int val, ParseUnit p){
		bytecode.add(inst(i, val, p));
	}
	
	public void add(byte i, int val, ParseUnit p){
		bytecode.add(inst(i, val, p));
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
	
	public Instruction inst(InstructionSet i, int val, int fileIndex, int lineNum){
		return new Instruction(InstructionSet.getOpcode(i), val, fileIndex, lineNum);
	}
	
	public Instruction inst(byte i, int val, ParseUnit p){
		return new Instruction(i, val, getFileIndex(p, script), getLineNum(p));
	}
}
