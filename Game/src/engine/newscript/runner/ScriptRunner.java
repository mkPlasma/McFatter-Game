package engine.newscript.runner;

import static engine.newscript.bytecodegen.InstructionSet.*;

import java.util.Stack;

import engine.newscript.DScript;
import engine.newscript.bytecodegen.Instruction;
import engine.newscript.bytecodegen.InstructionSet;

public class ScriptRunner{
	
	private Instruction[] bytecode;
	
	private Object[] variables;
	
	private Stack<Object> stack;
	
	
	public void init(DScript script){
		bytecode = script.getBytecode();
		variables = new Object[script.getNumVariables()];
		
		stack = new Stack<Object>();
	}
	
	public void run(){
		
		for(Instruction i:bytecode)
			runInstruction(i);
		
		System.out.println("\nResult:");
		
		for(int i = 0; i < variables.length; i++)
			System.out.println(i + ": " + variables[i]);
	}
	
	
	private void runInstruction(Instruction i){
		
		InstructionSet name = getName(i.getOpcode());
		int op = i.getOperand();
		
		switch(name){
			
			case store_zero:
				variables[op] = 0;
				return;
				
			case store_value:
				variables[op] = pop();
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
				push(variables[op]);
				return;
				
				
			case op_add: case op_sub: case op_mult: case op_div: case op_mod: case op_exp:
			case op_eq: case op_lt: case op_gt: case op_lte: case op_gte: case op_neq:
				numOperation(name);
				return;
				
			case op_or:
				push((Boolean)pop() || (Boolean)pop());
				return;
				
			case op_and:
				push((Boolean)pop() && (Boolean)pop());
				return;
				
			case op_not:
				push(!(Boolean)pop());
				return;
				
			default:
				System.err.println("Unrecognized bytecode instruction '" + name + "'");
				return;
		}
	}
	
	// Numerical operation
	private void numOperation(InstructionSet i){
		
		Object o1 = pop();
		Object o2 = pop();
		
		// Cast operands
		float a = o1 instanceof Float ? (float)o1 : (float)(int)o1;
		float b = o2 instanceof Float ? (float)o2 : (float)(int)o2;
		
		// Result
		float r = Float.NaN;
		
		// Operate
		switch(i){
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
	
	// Shorthand functions
	private void push(Object o){
		stack.push(o);
	}
	
	private Object pop(){
		return stack.pop();
	}
}
