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
	private ArrayList<String> varList;

	private DScript script;
	
	public void compile(DScript script){
		
		haltCompiler = false;
		this.script = script;
		
		// Initialize/reset lists
		bytecode = new ArrayList<Long>();
		varList = new ArrayList<String>();
		
		
		// First variable reserved for register
		varList.add("");
		
		int lineNum = 1;
		
		// Load file and read line by line
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
	
	// Takes a line and returns opcode
	private void processCode(String code, int lineNum){
		
		// Earliest delimiter index
		int delimiterIndex = getDelimiterIndex(code);
		
		// Code up to/after delimiter
		String partFirst = code.substring(0, delimiterIndex);
		String partSecond = code.substring(delimiterIndex);
		
		// Formatting code, check syntax errors
		String regex1;
		String regex2;
		Pattern pattern;
		Matcher matcher;
		boolean first;
		
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
				matcher = pattern.matcher(code);
				matcher.find();
				
				// Variable name
				String var = matcher.group(1);
				
				// If variable name is a reserved word
				if(isReservedWord(var) || Character.isDigit(var.charAt(0))){
					compilationError("Invalid variable name", code, lineNum);
					return;
				}
				
				// If variable already exists
				if(varList.contains(var)){
					compilationError("Duplicate variable", code, lineNum);
					return;
				}
				
				// Store variable during compilation
				varList.add(var);
				
				// Initialize empty variable
				if(first){
					bytecode.add(getInstruction(getOpcode("create_var"), VALUE, INT, lineNum, 0));
					return;
				}
				
				// Expression after equals
				String exp = matcher.group(2);
				long inst = processExpression(exp, lineNum);
				
				// If single value then create var
				if(getOpcode(inst) == getOpcode("none"))
					bytecode.add(setOpcode(inst, getOpcode("create_var")));
				
				// Otherwise (if postfix) add instruction directly then create var from register
				else{
					bytecode.add(inst);
					
					// End postfix expression
					bytecode.add(getInstruction(getOpcode("postfix_end"), VALUE, ZERO, lineNum, 0));
					
					// Create variable
					bytecode.add(getInstruction(getOpcode("create_var"), VARIABLE, ZERO, lineNum, 0));
				}
				
				return;
		}
		
		compilationError("Undefined compilation error", code, lineNum);
	}

	// Processes single values/expressions
	// opcode = none for single values
	private long processExpression(String code, int lineNum){
		
		String sFalse = "false", sTrue = "true", sInt = "-?\\d+", sFloat = "-?\\d+\\.\\d+";
		
		// Single values
		
		// Put function call check here
		
		
		// Booleans
		if(code.equals(sFalse))
			return getInstruction(getOpcode("none"), VALUE, BOOLEAN, lineNum, 0);
		else if(code.equals(sTrue))
			return getInstruction(getOpcode("none"), VALUE, BOOLEAN, lineNum, 1);
		
		// Int
		else if(code.matches(sInt))
			return getInstruction(getOpcode("none"), VALUE, INT, lineNum, Integer.parseInt(code));
		
		// Float
		else if(code.matches(sFloat))
			return getInstruction(getOpcode("none"), VALUE, FLOAT, lineNum, Float.floatToIntBits(Float.parseFloat(code)));
		
		
		
		// Expressions
		
		String regex = "((" + sFalse +  ")|(" + sTrue + ")|(" + sFloat + ")|(" + sInt +
			 ")|(\\w+)|(\\()|(\\))|\\+|\\-|\\*|/|%|!|(\\\\|\\\\|)|(&&)|<|>|(==)|(<=)|(>=))\\s*(.*)";
		
		// Group 1 - First token
		// Group 14 - Other tokens
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(code);
		boolean found = matcher.find();
		
		// Apply shunting-yard algorithm
		Stack<String> output = new Stack<String>();
		Stack<String> operations = new Stack<String>();
		
		while(found){
			
			// Current token
			String t = matcher.group(1);
			
			// If value/variable
			if(t.equals(sFalse) || t.equals(sTrue) || t.matches(sInt) || t.matches(sFloat) || t.matches("\\w+"))
				output.push(t);
			
			else if(isOperation(t)){
				
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
						compilationError("Mismatched parenthesis", code, lineNum);
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
		
		if(operations.peek().equals("(") || operations.peek().equals(")")){
			compilationError("Mismatched parenthesis", code, lineNum);
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
			
			if(i == postfix.size() - 1)
				return inst;
			else
				bytecode.add(inst);
		}
		
		compilationError("Undefined compilation error", code, lineNum);
		return 0;
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
		System.err.println("DScript compilation error:\n" + type + " in " + script.getFileName() + " on line " + lineNum + ":\n>> " + line);
		haltCompiler = true;
	}
}
