package engine.newscript.runner;

import static engine.newscript.bytecodegen.InstructionSet.*;

import java.util.ArrayList;
import java.util.Stack;

import engine.newscript.DScript;
import engine.newscript.ScriptException;
import engine.newscript.bytecodegen.Instruction;
import engine.newscript.bytecodegen.InstructionSet;

public class ScriptRunner{
	
	private DScript script;
	private Instruction[] bytecode;

	private ArrayList<Object> globalVariables;
	private Stack<ArrayList<Object>> localVariables;
	private Stack<Object> stack;
	
	private Instruction currentInstruction;
	private int instIndex;
	
	
	public void init(DScript script){
		this.script = script;
		
		bytecode = script.getBytecode();
		
		globalVariables	= new ArrayList<Object>();
		localVariables	= new Stack<ArrayList<Object>>();
		
		stack = new Stack<Object>();
	}
	
	public void run(){
		
		if(bytecode == null)
			return;
		
		try{
			for(instIndex = 0; instIndex < bytecode.length; instIndex++)
				runInstruction(bytecode[instIndex]);

			System.out.println("\nResult:");
			System.out.println("\nGlobal:");
			
			for(int i = 0; i < globalVariables.size(); i++)
				System.out.println(i + ": " + globalVariables.get(i));
			
			System.out.println("\nLocal:");
			for(int i = 0; i < localVariables.size(); i++)
				System.out.println(i + ": " + localVariables.get(i));
		}
		catch(ScriptException e){
			error(e);
		}
	}
	
	
	private void runInstruction(Instruction i) throws ScriptException{
		
		currentInstruction = i;
		
		InstructionSet name = getName(i.getOpcode());
		int op = i.getOperand();
		
		switch(name){
				
			case init_zero:
				globalVariables.add(0);
				return;
				
			case init_zero_l:
				localVariables.peek().add(0);
				return;

			case init_value:
				globalVariables.add(pop());
				return;
				
			case init_value_l:
				localVariables.peek().add(pop());
				return;

			case init_int:
				globalVariables.add(op);
				return;
				
			case init_int_l:
				localVariables.peek().add(op);
				return;

			case init_float:
				globalVariables.add(Float.intBitsToFloat(op));
				return;
				
			case init_float_l:
				localVariables.peek().add(Float.intBitsToFloat(op));
				return;

			case init_false:
				globalVariables.add(false);
				return;
				
			case init_false_l:
				localVariables.peek().add(false);
				return;

			case init_true:
				globalVariables.add(true);
				return;
				
			case init_true_l:
				localVariables.peek().add(true);
				return;
				
				

			case store_value:
				globalVariables.set(op, pop());
				return;
				
			case store_value_l:
				localVariables.peek().set(op, pop());
				return;
				
				
			case load_int:
				push(op);
				return;
				
			case load_float:
				push(Float.intBitsToFloat(op));
				return;
				
			case load_false:
				push(false);
				return;
				
			case load_true:
				push(true);
				return;
				
			case load_const:
				// TODO
				return;
				
			case load_var:
				push(globalVariables.get(op));
				return;
				
			case load_var_l:
				push(localVariables.peek().get(op));
				return;
				
				
				
			case op_add: case op_sub: case op_mult: case op_div: case op_mod: case op_exp:
			case op_eq: case op_lt: case op_gt: case op_lte: case op_gte: case op_neq:
				numOperation(name);
				return;
				
			case op_or:
				try{
					push((Boolean)pop() || (Boolean)pop());
				}catch(ClassCastException e){
					throw new ScriptException("Type mismatch, expected boolean", currentInstruction.getFileIndex(), currentInstruction.getLineNum());
				}
				return;
				
			case op_and:
				try{
					push((Boolean)pop() && (Boolean)pop());
				}catch(ClassCastException e){
					throw new ScriptException("Type mismatch, expected boolean", currentInstruction.getFileIndex(), currentInstruction.getLineNum());
				}
				return;
				
			case op_not:
				try{
					push(!(Boolean)pop());
				}catch(ClassCastException e){
					throw new ScriptException("Type mismatch, expected boolean", currentInstruction.getFileIndex(), currentInstruction.getLineNum());
				}
				return;

			case op_inc: case op_dec: case op_inv:
			case op_inc_l: case op_dec_l: case op_inv_l:
				unaryAssign(name, op);
				return;
				
				
				
			case jump:
				instIndex = op - 1;
				return;
				
			case jump_if_true:
				try{
					if((Boolean)pop())
						instIndex = op - 1;
				}catch(ClassCastException e){
					throw new ScriptException("Type mismatch, expected boolean", currentInstruction.getFileIndex(), currentInstruction.getLineNum());
				}
				return;
				
			case jump_if_false:
				try{
					if(!(Boolean)pop())
						instIndex = op - 1;
				}catch(ClassCastException e){
					throw new ScriptException("Type mismatch, expected boolean", currentInstruction.getFileIndex(), currentInstruction.getLineNum());
				}
				return;
				
				
				
			default:
				System.err.println("Unrecognized bytecode instruction '" + name + "'");
				return;
		}
	}
	
	// Numerical operation
	private void numOperation(InstructionSet op) throws ScriptException{
		
		Object o2 = pop();
		Object o1 = pop();
		
		if(!(o1 instanceof Float || o1 instanceof Integer) || !(o2 instanceof Float || o2 instanceof Integer))
			throw new ScriptException("Type mismatch, expected numbers", currentInstruction.getFileIndex(), currentInstruction.getLineNum());
		
		// Cast operands
		float a = o1 instanceof Float ? (float)o1 : (float)(int)o1;
		float b = o2 instanceof Float ? (float)o2 : (float)(int)o2;
		
		// Result
		float r = Float.NaN;
		
		// Operate
		switch(op){
			case op_add:	r = a + b;	break;
			case op_sub:	r = a - b;	break;
			case op_mult:	r = a * b;	break;
			case op_div:	r = a / b;	break;
			case op_mod:	r = a % b;	break;
			case op_exp:	r = (float)Math.pow(a, b);	break;
			
			// Push boolean results directly
			case op_lt:		push(a < b);	return;
			case op_gt:		push(a > b);	return;
			case op_lte:	push(a >= b);	return;
			case op_gte:	push(a <= b);	return;
			
			default:	return;
		}
		
		// If float and int are equal, use in
		if(r == (int)r)
			push((int)r);
		
		// Otherwise use float value
		else
			push(r);
	}
	
	private void unaryAssign(InstructionSet i, int op) throws ScriptException{
		
		boolean local = i == op_inc_l || i == op_dec_l || i == op_inc_l;
		
		Object v = local ? localVariables.peek().get(op) : globalVariables.get(op);
		
		if(v instanceof Float){
			float f = (float)v;
			
			switch(i){
				case op_inc:	f++;		break;
				case op_dec:	f--;		break;
				case op_inv:	f = -f;		break;
				
				default:	return;
			}
			
			if(local)
				localVariables.peek().set(op, f);
			else
				globalVariables.set(op, f);
			
			return;
		}

		if(v instanceof Integer){
			int n = (int)v;
			
			switch(i){
				case op_inc:	n++;		break;
				case op_dec:	n--;		break;
				case op_inv:	n = -n;		break;
				
				default:	return;
			}
			
			if(local)
				localVariables.peek().set(op, n);
			else
				globalVariables.set(op, n);
			
			return;
		}
		
		if(!(v instanceof Boolean) || i == op_inc || i == op_dec)
			throw new ScriptException("Type mismatch, expected boolean", currentInstruction.getFileIndex(), currentInstruction.getLineNum());
		
		if(local)
			localVariables.peek().set(op, !(Boolean)v);
		else
			globalVariables.set(op, !(Boolean)v);
	}
	
	// Shorthand functions
	private void push(Object o){
		stack.push(o);
	}
	
	private Object pop(){
		return stack.pop();
	}
	
	
	private void error(ScriptException e){
		
		int file = e.getFileIndex();
		int line = e.getLine();
		
		System.err.println(
			"Runtime error in " + (file == 0 ? script.getFileName(): script.getFileName(file)) +
			" on line " + line + ":\n" + e.getMessage() +
			(line > 0 ? "\n" + line + ": " + script.getLine(file, line) : "")
		);
	}
}
