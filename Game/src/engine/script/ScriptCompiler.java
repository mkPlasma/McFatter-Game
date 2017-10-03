package engine.script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static engine.script.ScriptFunctions.*;

/*
 * 		ScriptCompiler.java
 * 		
 * 		Purpose:	Compiles a DScript object into bytecode.
 * 		Notes:		WIP
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				10/2
 * 		Changes:			
 */

public class ScriptCompiler{
	
	private final char[] delimiters = {' ', '('};
	
	
	// Stops compilation
	private boolean haltCompiler = false;
	
	// Current bytecode
	ArrayList<Long> bytecode;
	
	// Stores variables by name while compiling
	private ArrayList<String> variables;
	
	// Keep track of scope (brackets)
	int scope = 0;
	
	private DScript script;
	
	public void compile(DScript script){
		
		haltCompiler = false;
		this.script = script;
		
		// Initialize/reset lists
		bytecode = new ArrayList<Long>();
		variables = new ArrayList<String>();
		
		// First variable reserved for register
		variables.add("");
		
		int lineNum = 1;
		
		// Load file and read line by line
		// First readthrough to define functions and tasks
		try(BufferedReader br = new BufferedReader(new FileReader(script.getPath()))){
			for(String line; (line = br.readLine()) != null;){
				line = removeComments(line.trim());
				
				if(!line.isEmpty())
		    		processFunctions(line, lineNum);
				
				if(haltCompiler)
					return;
				
				lineNum++;
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}

		// Read again for all other operations
		try(BufferedReader br = new BufferedReader(new FileReader(script.getPath()))){
			for(String line; (line = br.readLine()) != null;){
				line = removeComments(line.trim());
				
				if(!line.isEmpty())
		    		processCode(line, lineNum);
				
				if(haltCompiler)
					return;
				
				lineNum++;
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		// Print bytecode (debug)
		BytecodePrinter.printBytecode(bytecode, script.getFileName());
		
		// Set DScript object bytecode
		long[] bytecodeArray = new long[bytecode.size()];
		
		for(int i = 0; i < bytecode.size(); i++)
			bytecodeArray[i] = bytecode.get(i);
		
		script.setBytecode(bytecodeArray);
	}
	
	// Removes comments from line
	private String removeComments(String line){
		int i = line.indexOf("//");
		
		if(i == -1)
			return line;
		
		String sub = line;
		
		// Check if // is in quotes
		int q = 0;
		
		int removed = 0;
		
		while(true){
			Matcher matcher = Pattern.compile("[^\\\\]\"").matcher(sub.substring(0, i));
			
			while(matcher.find())
				q++;
			
			if(q == 0)
				break;
			
			removed += sub.substring(0, i + 2).length();
			sub = sub.substring(i + 2);
			i = sub.indexOf("//");
			
			if(q%2 == 1 && i == -1)
				return line;
			else break;
		}
		
		return line.substring(0, removed + i).trim();
	}
	
	// Define functions
	private void processFunctions(String code, int lineNum){
		
		// Earliest delimiter index
		int delimiterIndex = getDelimiterIndex(code);
		
		// Code up to/after delimiter
		String partFirst = code.substring(0, delimiterIndex);
		
		// Only take functions and tasks
		if(!partFirst.equals("function") && !partFirst.equals("task"))
			return;
		
		String partSecond = code.substring(delimiterIndex);
		
		// Formatting code, check syntax errors
		String regex;
		Pattern pattern;
		Matcher matcher;
		
		// Check keywords
		if(partFirst.equals("function")){
			// Function regex
			regex = "\\s+(\\w+)\\((.*)\\)\\s*\\{";
			
			if(!partSecond.matches(regex)){
				compilationError("Invalid function declaration", code, lineNum);
				return;
			}
			
			pattern = Pattern.compile(regex);
			matcher = pattern.matcher(partSecond);
			matcher.find();
			
			String func = matcher.group(1);
			
			System.out.println(func);
			
			return;
		}
		
		compilationError("Undefined compilation error", code, lineNum);
	}
	
	// Processes a single line
	private void processCode(String code, int lineNum){
		
		// Check brackets to see whether still in function
		if(scope > 0){
			if(code.contains("{"))
				scope++;
			else if(code.contains("}"))
				scope--;
			
			// Mismatched
			if(scope < 0){
				compilationError("Mismatched brackets", code, lineNum);
				return;
			}
			else if(scope > 0)
				return;
		}
		
		// Earliest delimiter index
		int delimiterIndex = getDelimiterIndex(code);
		
		// Code up to/after delimiter
		String partFirst = code.substring(0, delimiterIndex);
		
		// Do not take functions/tasks
		if(partFirst.equals("function") || partFirst.equals("task"))
			scope++;
		
		String partSecond = code.substring(delimiterIndex);
		
		// Formatting code, check syntax errors
		String regex1;
		String regex2;
		Pattern pattern;
		Matcher matcher;
		boolean first;
		
		
		// Check for variables/function call first
		
		// Regex for variable with operation
		regex1 = "(\\w+)\\s*(=|(\\+\\+)|(\\-\\-)|(\\+=)|(-=)|(\\*=)|(/=)|(%=))\\s*(.*)";
		
		if(code.matches(regex1)){
			pattern = Pattern.compile(regex1);
			matcher = pattern.matcher(code);
			matcher.find();
			
			String var = matcher.group(1);
			int i = variables.indexOf(var);
			
			if(i == -1){
				compilationError("Undefined variable", code, lineNum);
				return;
			}
			
			// Operation
			String op = matcher.group(2);
			
			// Increment and decrement
			if(op.equals("++")){
				bytecode.add(getInstruction(getOpcode("increment"), lineNum, i));
				return;
			}
			else if(op.equals("--")){
				bytecode.add(getInstruction(getOpcode("decrement"), lineNum, i));
				return;
			}
			
			
			// Expression
			String exp = matcher.group(10);
			
			// Add variable and operation to expression
			// a *= 2 + 2 becomes a = a*(2 + 2)
			if(!op.equals("="))
				exp = var + op.replace("=", "") + "(" + exp + ")";
			
			// Add expression
			bytecode.add(processExpression(exp, lineNum));
			
			// Store register value in variable
			bytecode.add(getInstruction(getOpcode("store"), lineNum, i));
			
			return;
		}
		
		// Check keywords
		switch(partFirst){
			case "set":
				regex1 = "\\s+(\\w+)";					// set x
				regex2 = "\\s+(\\w+)\\s*=\\s*(.+)";		// set x = 0
				
				// Set pattern/create error
				if(partSecond.matches(regex1)){
					pattern = Pattern.compile(regex1);
					first = true;
				}
				else if(partSecond.matches(regex2)){
					pattern = Pattern.compile(regex2);
					first = false;
				}
				else{
					compilationError("Syntax error", code, lineNum);
					return;
				}
				
				// Get regex match
				matcher = pattern.matcher(partSecond);
				matcher.find();
				
				// Variable name
				String var = matcher.group(1);
				
				// If variable name is a reserved word
				if(isReservedWord(var) || Character.isDigit(var.charAt(0))){
					compilationError("Invalid variable name", code, lineNum);
					return;
				}
				
				// If variable already exists
				if(variables.contains(var)){
					compilationError("Duplicate variable", code, lineNum);
					return;
				}
				
				// Store variable during compilation
				variables.add(var);
				
				// Initialize empty variable
				if(first){
					bytecode.add(getInstruction(getOpcode("create_var"), VALUE, INT, lineNum, 0));
					return;
				}
				
				// Expression after equals
				String exp = matcher.group(2);
				long inst = processExpression(exp, lineNum);
				
				// If single value then create var
				if(getOpcode(inst) == ZERO)
					bytecode.add(setOpcode(inst, getOpcode("create_var")));
				
				// Otherwise (if postfix) add instruction directly then create var from register
				else{
					// End postfix expression
					bytecode.add(inst);
					
					// Create variable
					bytecode.add(getInstruction(getOpcode("create_var"), VARIABLE, ZERO, lineNum, 0));
				}
				
				return;
			
			case "function":
				// Function regex
				regex1 = "\\s+(\\w+)\\((.*)\\)\\s*\\{";
				
				if(!partSecond.matches(regex1)){
					compilationError("Invalid function declaration", code, lineNum);
					return;
				}
				
				pattern = Pattern.compile(regex1);
				matcher = pattern.matcher(partSecond);
				matcher.find();
				
				String func = matcher.group(1);
				
				System.out.println(func);
				
				break;
		}
		
		compilationError("Undefined compilation error", code, lineNum);
	}

	// Processes single values/expressions
	// opcode = none for single values
	private long processExpression(String code, int lineNum){
		
		String sFalse = "false", sTrue = "true", sInt = "-?\\d+", sFloat = "-?\\d+\\.\\d+";
		
		// Single values
		
		// Put function call check here
		
		// Variables
		if(code.matches("\\w+") && !Character.isDigit(code.charAt(0))){
			int i = variables.indexOf(code);
			
			// Check variable exists
			if(i == -1){
				compilationError("Undefined variable", code, lineNum);
				return 0;
			}
			
			return getInstruction(ZERO, VARIABLE, ZERO, lineNum, i);
		}
		
		// Booleans
		else if(code.equals(sFalse))
			return getInstruction(ZERO, VALUE, BOOLEAN, lineNum, 0);
		else if(code.equals(sTrue))
			return getInstruction(ZERO, VALUE, BOOLEAN, lineNum, 1);
		
		// Int
		else if(code.matches(sInt))
			return getInstruction(ZERO, VALUE, INT, lineNum, Integer.parseInt(code));
		
		// Float
		else if(code.matches(sFloat))
			return getInstruction(ZERO, VALUE, FLOAT, lineNum, Float.floatToIntBits(Float.parseFloat(code)));
		
		
		
		// Expressions
		
		// Regex for values, variables, operations
		String regex = "((" + sFalse +  ")|(" + sTrue + ")|(" + sFloat + ")|(" + sInt +
			 ")|(\\w+)|(\\()|(\\))|\\+|-|\\*|/|%|!|(\\|\\|)|(&&)|<|>|(==)|(<=)|(>=))\\s*(.*)";
		
		// Group 1 - First token
		// Group 14 - Other tokens
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(code);
		boolean found = matcher.find();
		
		// Apply shunting-yard algorithm
		Stack<String> output = new Stack<String>();
		Stack<String> operations = new Stack<String>();
		
		// Original code for diplaying errors
		String codeOriginal = code;
		
		// Previous type
		// 0 - none		1 - value	2 - op
		int prev = 0;
		
		while(found){
			
			// Current token
			String t = matcher.group(1);
			
			// If value/variable
			if(t.equals(sFalse) || t.equals(sTrue) || t.matches(sInt) || t.matches(sFloat) || t.matches("\\w+")){
				output.push(t);
				prev = 1;
			}
			
			else if(isOperation(t)){
				
				// Check for 2 operations in a row
				if(prev == 2){
					compilationError("Invalid expression", codeOriginal, lineNum);
					return 0;
				}
				
				prev = 2;
				
				// Top operation is greater/equal precedence
				while(!operations.isEmpty() && getPrecedence(operations.peek()) >= getPrecedence(t))
					output.push(operations.pop());
				
				operations.push(t);
			}
			
			// If left parenthesis
			else if(t.equals("("))
				operations.push("(");
			
			// If right parenthesis
			else if(t.equals(")")){
				while(!operations.peek().equals("(")){
					
					if(operations.isEmpty()){
						compilationError("Mismatched parenthesis", codeOriginal, lineNum);
						return 0;
					}
					
					output.push(operations.pop());
				}
				
				// Pop left parenthesis
				operations.pop();
			}
			
			// Remove processed section, match again until finished
			code = matcher.group(14);
			matcher = pattern.matcher(code);
			found = matcher.find();
			
			if(code.isEmpty())
				found = false;
		}
		
		if(operations.isEmpty()){
			compilationError("Invalid expression", codeOriginal, lineNum);
			return 0;
		}
			
		
		if(operations.peek().equals("(") || operations.peek().equals(")")){
			compilationError("Mismatched parenthesis", codeOriginal, lineNum);
			return 0;
		}
		
		// Push operators onto output stack
		while(!operations.isEmpty()){
			output.push(operations.pop());
		}
		
		// Convert to ArrayList
		ArrayList<String> postfix = new ArrayList<String>();
		
		while(!output.isEmpty())
			postfix.add(output.pop());
		
		Collections.reverse(postfix);
		
		for(int i = 0; i < postfix.size(); i++){
			
			String t = postfix.get(i);
			long inst = 0;
			
			// Set postfix operation/value
			if(isOperation(t))
				inst = getInstruction(getOpcode(t), ZERO, POSTFIX, lineNum, 0);
			else
				inst = setOpcode(processExpression(t, lineNum), getOpcode("postfix_val"));
			
			bytecode.add(inst);
		}
		
		// End postfix expression
		return getInstruction(getOpcode("postfix_end"), VALUE, ZERO, lineNum, 0);
	}
	
	// Returns earliest delimiter index
	private int getDelimiterIndex(String code){
		
		// Earliest delimiter index
		int delimiterIndex = code.length();
		
		for(char c:delimiters){
			int i = code.indexOf(c);
			
			if(i != -1 && i < delimiterIndex)
				delimiterIndex = i;
		}
		
		return delimiterIndex;
	}
	
	// Create syntax error and halt compilation
	private void compilationError(String type, String line, int lineNum){
		System.err.println("\nDScript compilation error:\n" + type + " in " + script.getFileName() + " on line " + lineNum + ":\n>> " + line);
		haltCompiler = true;
	}
}
