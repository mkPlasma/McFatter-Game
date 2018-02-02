package engine.newscript.runner;

import static engine.newscript.bytecodegen.InstructionSet.*;

import java.util.ArrayList;
import java.util.Collections;
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
			case op_lt: case op_gt: case op_lte: case op_gte:
				numOperation(name);
				return;
				
			case op_eq:
				push(pop().equals(pop()));
				return;
				
			case op_neq:
				push(!pop().equals(pop()));
				return;
				
			case op_or:
				try{
					push((boolean)pop() || (boolean)pop());
				}catch(ClassCastException e){
					throwException("Type mismatch, expected boolean");
				}
				return;
				
			case op_and:
				try{
					push((boolean)pop() && (boolean)pop());
				}catch(ClassCastException e){
					throwException("Type mismatch, expected boolean");
				}
				return;
				
			case op_not:
				try{
					push(!(boolean)pop());
				}catch(ClassCastException e){
					throwException("Type mismatch, expected boolean");
				}
				return;
				
			case op_concat:
				concat();
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
					if((boolean)pop())
						instIndex = op - 1;
				}catch(ClassCastException e){
					throwException("Type mismatch, expected boolean");
				}
				return;
				
			case jump_if_false:
				try{
					if(!(boolean)pop())
						instIndex = op - 1;
				}catch(ClassCastException e){
					throwException("Type mismatch, expected boolean");
				}
				return;
				
				
			case move_to_top:{
				Object o = pop();
				int ind = 0;
				
				if(o instanceof Integer)		ind = (int)o;
				else if(o instanceof Float)		ind = (int)(float)o;
				else if(o instanceof Boolean)	ind = (Boolean)o ? 0 : 1;
				else throwException("Type mismatch, expected number or boolean");
				
				ind = Math.max(Math.min(ind, op), 0) + 1;
				o = stack.remove(stack.size() - ind);
				push(o);
				
				return;
			}
			
			case pop_count:{
				Object top = pop();
				
				for(int j = 0; j < op; j++)
					pop();
				
				push(top);
				return;
			}
				
				
			case array_create:
				push(createArray());
				return;
				
			case init_array:
				globalVariables.set(op, createArray());
				return;
				
			case init_array_l:
				localVariables.peek().set(op, createArray());
				return;

				
			case array_elem:
				arrayElem(pop(), pop());
				return;
				
			case array_elem_v:
				arrayElem(pop(), globalVariables.get(op));
				return;
				
			case array_elem_v_l:
				arrayElem(pop(), localVariables.peek().get(op));
				return;
				
				
			case store_array_elem:
				storeArrayElem(globalVariables.get(op));
				return;
				
			case store_array_elem_l:
				storeArrayElem(localVariables.peek().get(op));
				return;
				
			case copy_top:
				push(stack.peek());
				return;
				
				
			default:
				System.err.println("Unrecognized bytecode instruction '" + name + "'");
				return;
		}
	}
	
	// Numerical operation
	@SuppressWarnings("unchecked")
	private void numOperation(InstructionSet op) throws ScriptException{
		
		// Pop in reverse order
		Object o2 = pop();
		Object o1 = pop();
		
		// Array operations
		if(o1 instanceof ArrayList || o2 instanceof ArrayList){
			
			ArrayList<Object> result = new ArrayList<Object>();
			ArrayList<Object> a1 = o1 instanceof ArrayList ? (ArrayList<Object>)o1 : null;
			ArrayList<Object> a2 = o2 instanceof ArrayList ? (ArrayList<Object>)o2 : null;
			
			// If one is an array
			if(o1 instanceof ArrayList ^ o2 instanceof ArrayList){
				
				// Copy objects
				if(o1 instanceof ArrayList){
					for(Object o:a1)
						result.add(operate(o, o2, op));
				}
				else
					for(Object o:a2)
						result.add(operate(o, o1, op));
				
				push(result);
				return;
			}
			
			int minSize = Math.min(a1.size(), a2.size());
			int maxSize = Math.max(a1.size(), a2.size());
			
			// Always make result larger if different sizes
			for(int i = 0; i < maxSize; i++){
				
				if(i < minSize)
					result.add(operate(a1.get(i), a2.get(i), op));
				else
					result.add(a1.size() > a2.size() ? a1.get(i) : a2.get(i));
			}
			
			push(result);
			return;
		}
		
		// Standard operations
		push(operate(o1, o2, op));
	}
	
	// Perform operation and return value
	@SuppressWarnings("incomplete-switch")
	private Object operate(Object o1, Object o2, InstructionSet op) throws ScriptException{
		
		if(!(o1 instanceof Float || o1 instanceof Integer) || !(o2 instanceof Float || o2 instanceof Integer))
			throwException("Type mismatch, expected numbers in operation");
		
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
			
			// Return boolean results directly
			case op_lt:	return a < b;
			case op_gt:	return a > b;
			case op_lte:	return a <= b;
			case op_gte:	return a >= b;
		}
		
		// If float and int are equal, use int
		if(r == (int)r)
			return (int)r;
		
		// Otherwise use float value
		return r;
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
		
		if(i != op_inv)
			throwException("Type mismatch, expected number in assignment");
		
		
		if(!(v instanceof Boolean))
			throwException("Type mismatch, expected boolean in assignment");
		
		if(local)
			localVariables.peek().set(op, !(boolean)v);
		else
			globalVariables.set(op, !(boolean)v);
	}
	
	// Concatenation operation
	@SuppressWarnings("unchecked")
	private void concat(){

		Object o2 = pop();
		Object o1 = pop();
		
		// Both are arrays
		if(o1 instanceof ArrayList && o2 instanceof ArrayList){
			((ArrayList<Object>)o1).addAll((ArrayList<Object>)o2);
			push(o1);
			return;
		}
		
		// First is array
		if(o1 instanceof ArrayList && !(o2 instanceof ArrayList)){
			((ArrayList<Object>)o1).add(o2);
			push(o1);
			return;
		}
		
		// Second is array
		if(!(o1 instanceof ArrayList) && o2 instanceof ArrayList){
			((ArrayList<Object>)o2).add(0, o1);
			push(o2);
			return;
		}
		
		// Both not arrays
		ArrayList<Object> result = new ArrayList<Object>();
		result.add(o1);
		result.add(o2);
		
		push(result);
	}
	
	// Create an array value
	private ArrayList<Object> createArray(){
		ArrayList<Object> array = new ArrayList<Object>();
		int len = (int)pop();
		
		for(int j = 0; j < len; j++)
			array.add(pop());
		
		Collections.reverse(array);
		return array;
	}
	
	// Get an array element
	@SuppressWarnings("unchecked")
	private void arrayElem(Object index, Object array) throws ScriptException{
		
		if(!(index instanceof Integer) && !(index instanceof Float))
			throwException("Type mismatch, expected number in array index");
		
		int ind = index instanceof Integer ? (int)index : (int)(float)index;
		
		try{
			push(((ArrayList<Object>)array).get(ind));
		}
		catch(ClassCastException e){
			throwException("Type mismatch, expected array");
		}
		catch(IndexOutOfBoundsException e){
			throwException("Array index out of bounds (index " + ind + ", size " + ((ArrayList<Object>)array).size() + ")");
		}
	}
	
	// Store an array element
	@SuppressWarnings("unchecked")
	private void storeArrayElem(Object array) throws ScriptException{
		
		Object value = pop();
		Object index = pop();
		
		if(!(index instanceof Integer) && !(index instanceof Float))
			throwException("Type mismatch, expected number in array index");
		
		int ind = index instanceof Integer ? (int)index : (int)(float)index;
		
		try{
			((ArrayList<Object>)array).set(ind, value);
		}
		catch(ClassCastException e){
			throwException("Type mismatch, expected array");
		}
		catch(IndexOutOfBoundsException e){
			throwException("Array index out of bounds (index " + ind + ", size " + ((ArrayList<Object>)array).size() + ")");
		}
	}
	
	
	// Shorthand functions
	private void push(Object o){
		stack.push(o);
	}
	
	private Object pop(){
		return stack.pop();
	}
	
	
	private void throwException(String message) throws ScriptException{
		throw new ScriptException(message, currentInstruction.getFileIndex(), currentInstruction.getLineNum());
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
