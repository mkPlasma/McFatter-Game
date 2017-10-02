package engine.script;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static engine.script.ScriptFunctions.*;

public class ScriptRunner{
	
	// Stops script
	private boolean haltRun = false;
	
	// Variables
	private ArrayList<Object> variables;
	
	// Used to evaluate postfix expressions
	private ArrayList<Object> postfix;
	
	private long[] bytecode;
	
	private DScript script;
	
	public void run(DScript script){
		
		haltRun = false;
		this.script = script;
		
		// Initialize/reset variables/postfix
		variables = new ArrayList<Object>();
		postfix = new ArrayList<Object>();
		
		// Add register
		variables.add(0);
		
		
		// Load bytecode
		bytecode = script.getBytecode();
		
		// Loop through
		for(long inst:bytecode){
			
			// Instruction properties
			
			String opcode = opcodes[getOpcode(inst)];
			boolean isVar = isVariable(inst);
			byte type = getType(inst);
			int lineNum = getLineNum(inst);
			int data = getData(inst);
			
			// Postfix index
			int pfs = postfix.size();
			
			boolean doSwitch = true;
			
			if(isOperation(opcode)){
				opcode = getOperation(opcode);
				
				if(type == POSTFIX) postfixOperation(opcode, lineNum);
				else operation(opcode, isVar, type, data, lineNum);
				
				doSwitch = false;
			}
			
			if(doSwitch){
				switch(opcode){
					
					case "none":
						runtimeWarning("Opcode \"none\" found", lineNum);
						break;
						
					case "create_var":
						
						// Variables
						if(isVar)
							variables.add(variables.get(data));
						
						// Single values
						else{
							switch(getType(inst)){
								case INT:
									variables.add(data);
									break;
								case FLOAT:
									variables.add(Float.intBitsToFloat(data));
									break;
								case BOOLEAN:
									variables.add(data == 1);
							}
						}
						break;
						
					case "postfix_val":
						
						// Variables
						if(isVar)
							postfix.add(variables.get(data));
						
						// Single values
						else{
							switch(getType(inst)){
								case INT:
									postfix.add(data);
									break;
								case FLOAT:
									postfix.add(Float.intBitsToFloat(data));
									break;
								case BOOLEAN:
									postfix.add(data == 1);
							}
						}
						break;
					
					case "postfix_end":
						if(postfix.size() != 1)
							runtimeWarning("Postfix stack size " + postfix.size() + " on expression end", lineNum);
						
						// Save to register
						variables.set(0, postfix.get(0));
						break;
				}
			}
			
			if(haltRun)
				return;
		}
		
		System.out.println("\nResults of " + script.getFileName() + ":\n");
		
		for(int i = 0; i < variables.size(); i++)
			System.out.println((i == 0 ? "Register" : "Variable " + i) + ": " + variables.get(i));
		
	}
	
	// Single operation on register
	private void operation(String op, boolean isVar, byte type, int data, int lineNum){
		
		// Operands
		Object o1 = variables.get(0);
		Object o2 = data;
		
		if(!isVar){
			if(type == FLOAT)
				o2 = Float.intBitsToFloat(data);
			else if(type == BOOLEAN)
				o2 = data == 1;
		}
		else
			o2 = variables.get(data);
		
		// If each is number
		boolean isNumber1 = (o1 instanceof Integer) || (o1 instanceof Float);
		boolean isNumber2 = (o1 instanceof Integer) || (o1 instanceof Float);
		
		if(isNumber1 != isNumber2 || (!op.equals("==") && isNumberOp(op) != isNumber1))
			runtimeError("Type mismatch", lineNum);
		
		// If either is number/float
		boolean isNumber = isNumber1 || isNumber2;
		
		// Result stored if number, set immediately if boolean
		float result = 0;
		
		// Integers need to be cast to int first then float
		float n1 = o1 instanceof Float ? (float) o1 : (float)((int) o1);
		float n2 = o2 instanceof Float ? (float) o2 : (float)((int) o2);
		boolean b1 = (boolean) o1;
		boolean b2 = (boolean) o2;
		
		switch(op){
			case "+":	result = n1 + n2;			break;
			case "-":	result = n1 - n2;			break;
			case "*":	result = n1 * n2;			break;
			case "/":	result = n1 / n2;			break;
			case "%":	result = n1 % n2;			break;
			case "!":	variables.set(0, !b1);		return;
			case "||":	variables.set(0, b1 || b2);	return;
			case "&&":	variables.set(0, b1 && b2);	return;
			case "<":	variables.set(0, n1 < n2);	return;
			case ">":	variables.set(0, n1 > n2);	return;
			case "<=":	variables.set(0, n1 <= n2);	return;
			case ">=":	variables.set(0, n1 >= n2);	return;
			case "==":
				if(isNumber) variables.set(0, n1 == n2);
				else variables.set(0, b1 == b2);
				return;
		}
		
		// Treat as float if data is lost when casting to int
		if(result != (int)result)
			variables.set(0, result);
		else
			variables.set(0, (int)result);
	}
	
	// Postfix expression operation
	private void postfixOperation(String op, int lineNum){
		
		int pfs = postfix.size();
		
		boolean isSingleOp = op.equals("!");
		
		// Get operands
		Object o1 = postfix.get(pfs - (isSingleOp ? 1 : 2));
		Object o2 = postfix.get(pfs - 1);
		
		// Remove last two elements
		postfix.remove(pfs - 1);
		
		if(!isSingleOp)
			postfix.remove(pfs - 2);
		
		boolean isBoolean = o1 instanceof Boolean || o2 instanceof Boolean;
		
		// Number operation
		if(isNumberOp(op) && !isBoolean){
			
			if((!(o1 instanceof Integer) && !(o1 instanceof Float)) || (!(o2 instanceof Integer) && !(o2 instanceof Float)))
				runtimeError("Type mismatch", lineNum);
			
			// Result as number/boolean
			float result = 0;
			boolean resultBool = false;
			
			// Set result as boolean
			boolean useBool = false;

			// Integers need to be cast to int first then float
			float n1 = o1 instanceof Float ? (float) o1 : (float)((int) o1);
			float n2 = o2 instanceof Float ? (float) o2 : (float)((int) o2);
			
			// Operate
			switch(op){
				case "+":	result = n1 + n2;		break;
				case "-":	result = n1 - n2;		break;
				case "*":	result = n1 * n2;		break;
				case "/":	result = n1 / n2;		break;
				case "%":	result = n1 % n2;		break;
				case "<":	resultBool = n1 < n2;	useBool = true; break;
				case ">":	resultBool = n1 > n2;	useBool = true; break;
				case "==":	resultBool = n1 == n2;	useBool = true; break;
				case ">=":	resultBool = n1 <= n2;	useBool = true; break;
				case "<=":	resultBool = n1 >= n2;	useBool = true; break;
			}
			
			if(!useBool){
				// Treat as float if data is lost when casting to int
				if(result != (int)result)
					postfix.add(result);
				else
					postfix.add((int)result);
			}
			else
				postfix.add(resultBool);
			
			return;
		}
		
		// Boolean operation
		if(!isBoolean)
			runtimeError("Type mismatch", lineNum);
		
		// Cast
		boolean b1 = (boolean) o1;
		boolean b2 = (boolean) o2;
		
		boolean result = false;
		
		// Operate
		switch(op){
			case "!":	result = !b1;		break;
			case "||":	result = b1 || b2;	break;
			case "&&":	result = b1 && b2;	break;
			case "==":	result = b1 == b2;	break;
		}
		
		postfix.add(result);
	}
	
	// Create syntax error and halt compilation
	private void runtimeError(String type, int lineNum){
		try{
			System.err.println("DScript runtime error:\n" + type + " in " + script.getFileName() + " on line " + lineNum +
				":\n>> " + Files.readAllLines(Paths.get(script.getPath())).get(lineNum));
		}
		catch(IOException e){
			e.printStackTrace();
		}
		haltRun = true;
	}
	
	// Condition that should not occur, may produce incorrect results
	private void runtimeWarning(String type, int lineNum){
		System.err.println("DScript runtime warning:\n" + type + " in " + script.getFileName() + " on line " + lineNum);
	}
}
