package engine.script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static engine.script.ScriptFunctions.*;

public class ScriptLexer{
	
	private DScript script;
	
	// Stops compilation
	private boolean haltCompiler = false;
	
	private ArrayList<String> tokens;
	
	
	// Regexes

	// Operators
	String rOperators = "\\+|-|\\*|/|%|!|(\\|\\|)|(&&)|<|>|(==)|(<=)|(>=)";
	
	// Assignment operators
	String rAssignments = "=|(\\+\\+)|(\\-\\-)|(\\+=)|(-=)|(\\*=)|(/=)|(%=)";
	
	// Identifiers
	String rIdentifiers = "\\w+";
	
	// Numbers
	String rInt = "\\d+";
	String rFloat = "\\d+\\.\\d+";
	
	// Separators
	String rSeparators = "\\(|\\)|\\{|\\}|\\[|\\]|,|\\.|;";
	
	// Delimiters
	String rDelimiters = rOperators + "|" + rAssignments + "|(\\s+" + rIdentifiers + ")|" + rSeparators;
	
	// Word + delimiters
	String rWordDelim = "(" + rIdentifiers + ")\\s*?(" + rDelimiters + ")";
	
	// Number literal + delimiters (remove period delimiter for floats)
	String rNumDelim = "((" + rInt + ")|(" + rFloat + "))\\s*?(" + rDelimiters.replace("|\\.", "") + ")";
	
	
	/* 	Format
	 * 	
	 * 	i - int literal
	 * 	f - float literal
	 * 	s - string literal
	 * 	
	 * 	f - function/task
	 * 	v - variable
	 */
	
	public String[] lex(DScript script){
		
		this.script = script;
		
		// Initialize/reset tokens arraylist
		tokens = new ArrayList<String>();
		
		haltCompiler = false;
		
		int lineNum = 1;
		
		// Load file and read line by line
		// First readthrough to define functions and tasks
		try(BufferedReader br = new BufferedReader(new FileReader(script.getPath()))){
			for(String line; (line = br.readLine()) != null;){
				
				line = line.trim();
				
				if(!line.isEmpty())
		    			process(line, lineNum);
				
				if(haltCompiler)
					return null;
				
				lineNum++;
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		/*
		System.out.println();
		
		for(int i = 0; i < tokens.size(); i++)
			System.out.println(tokens.get(i));
		*/
		
		// Check for syntax errors
		checkTokens();
		
		// Convert to array
		String[] tokensArray = new String[tokens.size()];
		
		for(int i = 0; i < tokens.size(); i++)
			tokensArray[i] = tokens.get(i);
		
		return tokensArray;
	}
	
	// Process line into tokens
	private void process(String line, int lineNum){
		
		// Current token
		String token = "";
		
		Pattern pattern;
		Matcher matcher;
		
		// Skip whitespace after token
		boolean skipWhitespace = false;
		
		// Loop through line
		for(int i = 0; i < line.length(); i++){
			
			token += line.charAt(i);
			
			// Add number literals directly
			if(token.matches(rNumDelim)){
				pattern = Pattern.compile(rNumDelim);
				matcher = pattern.matcher(token);
				matcher.find();
				
				// Add token
				tokens.add(lineNum + (token.contains(".") ? "f:" : "i:") + matcher.group(1));
				token = "";
				
				// Push back i as not to miss data
				i -= matcher.group(4).trim().length();
			}
			
			// Add operators, assignments, and separators directly
			else if(token.matches(rOperators) || token.matches(rAssignments) || token.matches(rSeparators)){
				tokens.add(lineNum + ":" + token);
				token = "";
				
				skipWhitespace = true;
			}
			
			// String literal
			else if(token.charAt(0) == '"'){
				
				// String
				String s = "";
				
				// Move off of starting quote
				i++;
				
				while(i < line.length()){
					
					char c = line.charAt(i);
					
					// If not escaped quotation marks
					if(c == '"'){
						
						// Count backslashes to determine if quote is escaped
						int b = 0;
						for(int j = s.length() - 1; j > 0; j--)
							if(s.charAt(j) == '\\')
								b++;
							else
								break;
						
						// If backslashes are odd, end
						if(b == 0 || b % 2 == 1)
							break;
					}
					
					// Otherwise add to string
					else
						s += c;
					
					i++;
				}
				
				tokens.add(lineNum + "s:" + s);
				token = "";
				
				skipWhitespace = true;
			}
			
			// Check if matches keyword/identifier
			else if(token.matches(rWordDelim) && !Character.isDigit(token.charAt(0))){
				pattern = Pattern.compile(rWordDelim);
				matcher = pattern.matcher(token);
				matcher.find();
				
				// Temporary second token and delimiter
				String token2 = matcher.group(1);
				String p2 = matcher.group(2);
				
				if(Character.isDigit(token2.charAt(0))){
					compilationError("Invalid identifer", token2, lineNum);
					return;
				}
				
				// Check if reserved word
				if(isReservedWord(token2)){
					tokens.add(lineNum + ":" + token2);
				}
				
				// Add function
				else if(p2.contains("(")){
					tokens.add(lineNum + "f:" + token2);
				}
				
				// Add variable
				else {
					tokens.add(lineNum + "v:" + token2);
				}
				
				// Clear token
				token = "";
				
				// Push back i as not to miss data
				i -= p2.trim().length();
			}
			
			// Finish if at end of line
			if(i >= line.length() - 1)
				return;
			
			if(skipWhitespace){
				while(Character.isWhitespace(line.charAt(++i)))
					if(i >= line.length() - 1)
						return;
				i--;
				
				skipWhitespace = false;
			}
		}
	}
	
	// Check for syntax errors
	public void checkTokens(){
		
		// Keep track of brackets
		// ( { [
		int[] brackets = new int[3];
		boolean bracketsChanged;
		
		// Last token
		String last = "";
		
		for(int i = 0; i < tokens.size(); i++){
			
			// Get token
			String token = tokens.get(i);
			
			int in = token.indexOf(':');
			
			// Current type
			char type = token.charAt(in - 1);
			if(Character.isDigit(type)) type = '0';

			// Line number string
			String ln = token.substring(0, in);
			ln = Character.isLetter(ln.charAt(ln.length() - 1)) ? ln.substring(0, ln.length() - 1) : ln;
			
			// Token
			token = token.substring(in + 1);
			
			int lineNum = Integer.parseInt(ln);
			
			// Syntax errors if certain tokens were expected
			switch(last){
				case "set":
					if(type != 'v'){
						compilationError("Expected variable", token, lineNum);
						return;
					}
					break;
				case "else":
					if(!token.equals("if") && !token.equals("{")){
						compilationError("Expected \"if\" or {", token, lineNum);
						return;
					}
					break;
				case "break":
					if(!token.equals(";")){
						compilationError("Expected ;", token, lineNum);
						return;
					}
			}
			
			last = token;
			
			// Check brackets
			bracketsChanged = true;
			if(token.equals("("))	brackets[0]++;
			if(token.equals(")"))	brackets[0]--;
			if(token.equals("{"))	brackets[1]++;
			if(token.equals("}"))	brackets[1]--;
			if(token.equals("["))	brackets[2]++;
			if(token.equals("]"))	brackets[2]--;
			else	 bracketsChanged = false;
			
			if(bracketsChanged){
				for(int j:brackets){
					// If closed before opened or
					// Unclosed at end
					if(j < 0)
						compilationError("Extra close bracket", token, lineNum);
					else if(i == tokens.size() - 1 && j > 0)
						compilationError("Unclosed bracket", token, lineNum);
				}
			}
			
			/*
			switch(token){
				case "set":		nextExpected = "variable";	continue;
				case "else":		nextExpected = "if | {";		continue;
				case "break"	:	nextExpected = ";";			continue;
				
				case "if": case"while": case "for":	nextExpected = "(";	continue;
				case "function": case "task":	nextExpected = "function";		continue;
				case "return":	nextExpected = "; | variable | function";
				
				case "wait":		nextExpected = "; | int | variable";
			}
			*/
		}
	}
	
	// Create syntax error and halt compilation
	private void compilationError(String type, String line, int lineNum){
		System.err.println("\nDScript compilation error:\n" + type + " in " + script.getFileName() + " on line " + lineNum + ":\n>> " + line);
		haltCompiler = true;
	}
}
