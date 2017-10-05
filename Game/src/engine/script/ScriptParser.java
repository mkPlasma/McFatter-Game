package engine.script;

import static engine.script.ScriptFunctions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Stack;

/*
 * 		ScriptParser.java
 * 		
 * 		Purpose:	Parses DScript tokens into bytecode.
 * 		Notes:		WIP
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				current
 * 		Changes:			
 */

public class ScriptParser{
	
	// Lexical tokens
	private String[] tokens;
	
	// Current bytecode
	private ArrayList<Long> bytecode;
	
	// Store current states
	private Stack<String> states;
	
	// Store expression while processing
	private ArrayList<Object> expression;
	
	// Stores variables by name while compiling
	private ArrayList<String> variables;
	
	// Stops compilation
	private boolean haltCompiler = false;
	
	private DScript script;
	
	public void parse(DScript script){
		
		this.script = script;
		tokens = script.getTokens();
		
		// Initialize/reset lists
		bytecode = new ArrayList<Long>();
		expression = new ArrayList<Object>();
		states = new Stack<String>();
		variables = new ArrayList<String>();
		
		// First variable reserved for register
		variables.add("");
		
		haltCompiler = false;
		
		// Process into bytecode
		process();
		
		// Print bytecode (debug)
		BytecodePrinter.printBytecode(bytecode, script.getFileName());
		
		// Clear tokens after
		script.clearTokens();
	}
	
	// Process tokens into bytecode
	private void process(){
		for(int i = 0; i < tokens.length; i++){
			
			// Is first/last token
			//boolean tFirst = i == 0;
			//boolean tLast = i == tokens.length - 1;
			
			// Current, previous, next tokens
			String tCur = tokens[i];
			//String tPrev = !tFirst ? tokens[i - 1] : null;
			//String tNext = !tLast ? tokens[i + 1] : null;
			
			// Keep track of brackets
			// { ( [
			int[] brackets = new int[3];
			
			int lineNum = getLineNum(tCur);
			int typeCurrent = getType(tCur);
			
			tCur = getData(tCur);
			
			switch(typeCurrent){
				// Keywords
				case 'k':
					switch(tCur){
						case "set":
							states.push("set");	// create_var
							states.push("var");	// Add to variables arraylist
							continue;
					}
					
					break;
				
				// Operators
				case 'o':
					
					// Assignment
					switch(tCur){
						case "=":
							states.push("exp"); // Expression
							continue;
					}
					
					// Add opeerator to expression
					if(statesPeek("exp"))
						expression.add(tCur);
					
					break;
				
				// Separators
				case 's':
					switch(tCur){
						case ";":
							// Default variable init
							if(statesPeek("set")){
								bytecode.add(getInstruction(getOpcode("create_var"), VALUE, INT, lineNum, ZERO));
								continue;
							}
							
							// If at the end of an expression, parse
							if(statesPeek("exp")){
								parseExpression(lineNum);
							}
							continue;
						case "(": case ")":
							if(statesPeek("exp"))
								expression.add(tCur);
							
							
							continue;
					}
					break;
				
				// Functions/tasks
				case 'f':
					
					break;
				
				// Variables
				case 'v':
					// Add to variables arraylist
					if(statesPeek("var")){
						variables.add(tCur);
						states.pop();
					}
					else if(statesPeek("exp")){
						expression.add(tCur);
					}
					break;
				
				// Value literals
				case 'i': case 'l': case 'b':

					int lInt = Integer.parseInt(tCur);
					float lFloat = Float.parseFloat(tCur);
					boolean lBoolean = Boolean.parseBoolean(tCur);
					
					// Set variable
					if(statesPeek("exp")){
						if(typeCurrent == 'i') expression.add(lInt);
						if(typeCurrent == 'l') expression.add(lFloat);
						if(typeCurrent == 'b') expression.add(lBoolean);
					}
					break;
				
				// String literals
				case 't':
					
					break;
			}
		}
	}
	
	// Parse expression, convert to postfix
	private void parseExpression(int lineNum){
		
		if(expression.isEmpty()){
			compilationError("Invalid expression", lineNum);
			return;
		}
		
		for(int i = 0; i < expression.size(); i++){
			
			Object current = expression.get(i);
			long inst = 0;
			
			if(current instanceof Integer) inst = getInstruction(getOpcode("postfix_val"), VALUE, INT, lineNum, (int)current);
		}
	}
	
	// Peek states stack
	private boolean statesPeek(String state){
		return statesPeek().equals(state);
	}
	
	private String statesPeek(){
		return states.isEmpty() ? "" : states.peek();
	}
	
	// States stack first
	private boolean statesFirst(String state){
		return statesFirst().equals(state);
	}
	
	private String statesFirst(){
		return states.isEmpty() ? "" : states.firstElement();
	}
	
	// Create syntax error and halt compilation
	private void compilationError(String type, int lineNum){
		try{
			System.err.println("\nDScript compilation error:\n" + type + " in " + script.getFileName() + " on line " + lineNum +
				":\n>> " + Files.readAllLines(Paths.get(script.getPath())).get(lineNum - 1));
		}
		catch(IOException e){
			e.printStackTrace();
		}
		haltCompiler = true;
	}
}
