package engine.newscript.runner;

import static engine.newscript.bytecodegen.InstructionSet.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import engine.newscript.BIFunc;
import engine.newscript.BuiltInFunctionList;
import engine.newscript.DScript;
import engine.newscript.ScriptException;
import engine.newscript.ScriptHandler;
import engine.newscript.bytecodegen.Instruction;
import engine.newscript.bytecodegen.InstructionSet;

/**
 * 
 * Executes DScript bytecode.
 * 
 * @author Daniel
 * 
 */

public class ScriptRunner{
	
	private final BuiltInFunctionList biFuncList;
	
	private final ScriptHandler handler;
	
	// Script data
	private Instruction[] bytecode;
	private Object[] constants;
	
	// Current script branch
	private Branch branch;
	
	// Yield branch and return to ScriptHandler loop
	private boolean yield;
	
	// Variables and work stack
	private ArrayList<Object> globalVariables;
	private Stack<ArrayList<Object>> localVariables;
	private Stack<Object> stack;
	
	// Return stack for functions
	private Stack<Integer> returnStack;
	private Stack<Boolean> returnValStack;
	
	// Current instruction index
	private int instIndex;
	
	
	public ScriptRunner(ScriptHandler handler){
		this.handler = handler;
		biFuncList = new BuiltInFunctionList(handler.getScreen());
	}
	
	public void init(DScript script){
		bytecode = script.getBytecode();
		constants = script.getConstants();
		
		globalVariables	= new ArrayList<Object>();
	}
	
	public void run(Branch branch) throws ScriptException{
		
		if(bytecode == null)
			return;
		
		// Tick wait time
		if(branch.tickWait())
			return;
		
		this.branch = branch;
		
		// Get branch variables and return stack
		stack			= branch.getWorkStack();
		localVariables	= branch.getLocalVariables();
		returnStack		= branch.getReturnStack();
		returnValStack	= branch.getReturnValStack();
		
		// Run bytecode
		for(instIndex = branch.getInstructionIndex(); instIndex < bytecode.length; instIndex++){
			runInstruction(bytecode[instIndex]);
			
			if(yield){
				yield = false;
				return;
			}
		}
		
		branch.finish();
	}
	
	
	private void runInstruction(Instruction i) throws ScriptException{
		
		InstructionSet name = getName(i.getOpcode());
		int op = i.getOperand();
		
		switch(name){
				
			case store_zero:
				setGlobalVar(op, 0);
				return;
				
			case store_zero_l:
				setLocalVar(op, 0);
				return;
				
			case store_value:
				setGlobalVar(op, pop());
				return;
				
			case store_value_l:
				setLocalVar(op, pop());
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
				push(constants[op]);
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
				
				
			case init_params:{
				
				Queue<Object> p = new LinkedList<Object>();
				
				for(int j = 0; j < op; j++)
					p.add(pop());
				
				localVariables.peek().add(p.remove());
				
				return;
			}
				
			case jump_func: case jump_func_r:
				
				localVariables.add(new ArrayList<Object>());
				
				returnStack.add(instIndex);
				returnValStack.add(name == jump_func_r);
				
				instIndex = op - 1;
				return;
				
				
			case jump_branch: case jump_branch_r:
				
				// Create new branch, does not jump to task
				Branch newBranch = new Branch(instIndex + 1, stack, localVariables, returnStack, returnValStack);
				
				// Return task branch if necessary
				if(name == jump_branch_r)
					newBranch.pushBranch(branch);
				
				handler.addBranch(newBranch);
				
				// Jump without adding return index
				localVariables.add(new ArrayList<Object>());
				instIndex = op - 1;
				
				return;
				
				
			case return_void:
				
				// End task
				if(returnStack.isEmpty()){
					branch.finish();
					yield();
					return;
				}
				
				localVariables.pop();
				instIndex = returnStack.pop();
				
				// If a return value is expected, there must be one
				if(returnValStack.pop())
					throwException("Expected return value");
				
				return;
				
				
			case return_value:
				localVariables.pop();
				instIndex = returnStack.pop();
				
				// Pop return value if it is not used
				if(!returnValStack.pop())
					pop();
				
				return;
				
				
			case func_bi: case func_bi_r:{
				BIFunc f = biFuncList.get(op);
				
				// Initialize parameter list
				Object[] params = new Object[f.getParamCount()];
				
				// Add parameters
				for(int j = params.length - 1; j >= 0; j--)
					params[j] = pop();
				
				// Call function and get return value
				Object r = f.call(i, params);
				
				// If return value was expected
				if(name == func_bi_r){
					
					// Error if no return value
					if(r == null)
						throwException(f.getName() + "() does not return a value");
					
					// Push value otherwise
					push(r);
				}
				
				return;
			}
				
				
			case wait:
				branch.setWait(Math.max(0, op));
				yield();
				return;
				
			case wait_s:
				branch.setWait(Math.max(0, (int)pop()));
				yield();
				return;
				
				
			default:
				System.err.println("Unrecognized bytecode instruction '" + name + "'");
				return;
		}
	}
	
	// Set global variable, add enough variables if necessary
	private void setGlobalVar(int index, Object value){
		
		try{
			globalVariables.set(index, value);
		}
		
		// Add more variables if necessary
		catch(IndexOutOfBoundsException e){
			
			while(globalVariables.size() < index + 1)
				globalVariables.add(null);
			
			globalVariables.set(index, value);
		}
	}
	
	private void setLocalVar(int index, Object value){
		
		try{
			localVariables.peek().set(index, value);
		}
		
		// Add more variables if necessary
		catch(IndexOutOfBoundsException e){
			
			while(localVariables.peek().size() < index + 1)
				localVariables.peek().add(null);
			
			localVariables.peek().set(index, value);
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
	
	// Perform numerical operation and return value
	@SuppressWarnings("incomplete-switch")
	private Object operate(Object o1, Object o2, InstructionSet op) throws ScriptException{
		
		// String concatenation
		if(o1 instanceof String || o2 instanceof String){
			
			if(op != op_add)
				throwException("Type mismatch, expected + for string concatenation");
			
			return o1.toString() + o2.toString();
		}
		
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
	
	// Update current branch and yield to next
	private void yield(){
		branch.setInstructionIndex(instIndex + 1);
		yield = true;
	}
	
	// Shorthand functions
	private void push(Object o){
		stack.push(o);
	}
	
	private Object pop(){
		return stack.pop();
	}
	
	
	private void throwException(String message) throws ScriptException{
		Instruction i = bytecode[instIndex];
		throw new ScriptException(message, i.getFileIndex(), i.getLineNum());
	}
}
