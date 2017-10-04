package engine.script;

import static engine.script.ScriptFunctions.*;

import java.util.ArrayList;

public class ScriptParser{
	
	// Lexical tokens
	String[] tokens;
	
	// Current bytecode
	ArrayList<Long> bytecode;
	
	// Stores variables by name while compiling
	private ArrayList<String> variables;

	// Stops compilation
	private boolean haltCompiler = false;
	
	// Keep track of scope (brackets)
	int scope = 0;
	
	private DScript script;
	
	public void parse(DScript script){
		
		this.script = script;
		tokens = script.getTokens();

		// Initialize/reset lists
		bytecode = new ArrayList<Long>();
		variables = new ArrayList<String>();
		
		// First variable reserved for register
		variables.add("");
		
		haltCompiler = false;
		
		process();
		
		// Clear tokens after
		script.clearTokens();
	}
	
	// Process tokens into bytecode
	private void process(){
		for(int i = 0; i < tokens.length; i++){
			
			// Is first/last token
			boolean tFirst = i == 0;
			boolean tLast = i == tokens.length - 1;
			
			// Current, previous, next tokens
			String tCur = tokens[i];
			String tPrev = !tFirst ? tokens[i - 1] : null;
			String tNext = !tLast ? tokens[i + 1] : null;
			
			int typeCurrent = getType(tCur);
			
			// Keywords
			if(typeCurrent == 'k'){
				switch(tCur){
					case "set":
						break;
				}
			}
		}
	}
	
	// Create syntax error and halt compilation
	private void compilationError(String type, String line, int lineNum){
		System.err.println("\nDScript compilation error:\n" + type + " in " + script.getFileName() + " on line " + lineNum + ":\n>> " + line);
		haltCompiler = true;
	}
}
