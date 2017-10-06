package engine.script;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static engine.script.ScriptFunctions.*;

/*
 * 		ScriptLexer.java
 * 		
 * 		Purpose:	Parses DScript script into lexical tokens.
 * 		Notes:		WIP
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				10/4
 * 		Changes:			
 */

public class ScriptLexer{
	
	private DScript script;
	
	// Stops compilation
	private boolean haltCompiler = false;
	
	private ArrayList<String> tokens;
	
	
	// Regexes
	
	public static final String
	
	// Operators
	rOperators = "\\+|-|\\*|/|%|!|(\\|\\|)|(&&)|<|>|(==)|(<=)|(>=)",
	
	// Assignment operators
	rAssignments = "=|(\\+\\+)|(--)|(\\+=)|(-=)|(\\*=)|(/=)|(%=)",
	
	// Identifiers
	rIdentifiers = "\\w+",
	
	// Numbers
	rInt = "\\d+",
	rFloat = "\\d+\\.\\d+",
	
	// Separators
	rSeparators = "\\(|\\)|\\{|\\}|\\[|\\]|,|\\.|;",
	
	// Delimiters
	rDelimiters = rOperators + "|" + rAssignments + "|(\\s+" + rIdentifiers + ")|" + rSeparators,
	
	// Word + delimiters
	rWordDelim = "(" + rIdentifiers + ")\\s*?(" + rDelimiters + ")",
	
	// Number literal + delimiters (remove period delimiter for floats)
	rNumDelim = "((" + rInt + ")|(" + rFloat + "))\\s*?(" + rDelimiters.replace("|\\.", "") + ")";
	
	
	/* 	Types
	 * 	
	 * 	k - keyword
	 * 	o - operator
	 *  a - assignment operator
	 * 	s - separator
	 * 	
	 * 	f - function/task
	 * 	v - variable
	 * 	
	 * 	i - int literal
	 * 	l - float literal
	 * 	b - boolean literal
	 * 	t - string literal
	 */
	
	public void analyze(DScript script){
		
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
					return;
				
				lineNum++;
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		// Check for syntax errors
		checkTokens();
		
		if(haltCompiler)
			return;
		
		// Convert to array
		String[] tokensArray = new String[tokens.size()];
		
		for(int i = 0; i < tokens.size(); i++)
			tokensArray[i] = tokens.get(i);
		
		
		// Print tokens (debug)
		//printTokens(tokensArray);
		
		// Set tokens
		script.setTokens(tokensArray);
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
			
			// Add next characters until a match
			token += line.charAt(i);
			
			// Add number literals directly
			if(token.matches(rNumDelim)){
				pattern = Pattern.compile(rNumDelim);
				matcher = pattern.matcher(token);
				matcher.find();
				
				// Add token
				tokens.add(lineNum + (token.contains(".") ? "l:" : "i:") + matcher.group(1));
				token = "";
				
				// Push back i as not to miss data
				i -= matcher.group(4).trim().length();
			}
			
			// Add operators, assignments, and separators directly
			else if(token.matches(rOperators) || token.matches(rAssignments)){
				
				// Check for assignment operators
				if(i < line.length() - 1){
					// Try adding extra character
					token += line.charAt(++i);
					
					// Comments defined here because single / is an operator
					if(token.matches("//"))
						return;
					
					// If match fails with extra character, revert
					if(!token.matches(rOperators) && !token.matches(rAssignments)){
						token = token.substring(0, token.length() - 1);
						i--;
					}
				}
				
				tokens.add(lineNum + (token.matches(rOperators) ? 'o' : 'a') + ":" + token);
				token = "";
				
				skipWhitespace = true;
			}
			
			// Add separators directly
			else if(token.matches(rSeparators)){
				tokens.add(lineNum + "s:" + token);
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
						
						for(int j = s.length() - 1; j > 0; j--){
							if(s.charAt(j) == '\\')
								b++;
							else
								break;
						}
						
						// If backslashes are even, end
						if(b % 2 == 0)
							break;
						else
							s += c;
					}
					
					// Otherwise add to string
					else
						s += c;
					
					i++;
				}
				
				tokens.add(lineNum + "t:" + s);
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
					if(token2.equals("true") || token2.equals("false"))
						tokens.add(lineNum + "b:" + token2);
					else
						tokens.add(lineNum + "k:" + token2);
				}
				
				// Add function
				else if(p2.contains("(")){
					tokens.add(lineNum + "f:" + token2);
				}
				
				// Add variable
				else{
					tokens.add(lineNum + "v:" + token2);
				}
				
				// Clear token
				token = "";
				
				// Push back i as not to miss data
				i -= p2.trim().length();
			}
			
			// Finish if at end of line
			if(i >= line.length() - 1){
				
				// Token should be empty after completing line
				if(!token.isEmpty())
					compilationError("Invalid syntax", line, lineNum);
				
				return;
			}
			
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
		// { ( [
		int[] brackets = new int[3];
		boolean bracketsChanged;
		
		// Last token
		String lToken = null;
		
		// Require certain tokens
		String[] lastExpected = null;
		String[] nextExpected = null;
		
		for(int i = 0; i < tokens.size(); i++){
			
			// Get token
			String token = tokens.get(i);
			char type = getType(token);
			int lineNum = getLineNum(token);
			
			// Get data only
			token = getData(token);
			
			// Matched string in nextExpected
			boolean matched = false;
			
			if(nextExpected != null){
				for(String s:nextExpected){
					if(token.equals(s) || (s.length() == 1 && type == s.charAt(0))){
						matched = true;
						break;
					}
				}
			}
			else matched = true;
			
			if(!matched){
				String s = nextExpected[0];
				
				if(s.equals("k"))		s = "keyword";
				else if(s.equals("o"))	s = "operator";
				else if(s.equals("s"))	s = "separator";
				else if(s.equals("f"))	s = "function/task";
				else if(s.equals("v"))	s = "variable";
				else if(s.equals("i"))	s = "int";
				else if(s.equals("l"))	s = "float";
				else if(s.equals("b"))	s = "boolean";
				else if(s.equals("t"))	s = "string";
				
				compilationError("Expected " + s + " after", token, lineNum);
			}
			
			nextExpected = null;
			
			// Set expected values
			switch(type){
				// Keyword
				case 'k':
					lastExpected = new String[]{";", "{", "}", ""};
					
					switch(token){
						case "set":
							nextExpected = new String[]{"v", "const"};
							break;
						case "const":
							lastExpected = new String[]{"set"};
							nextExpected = new String[]{"v"};
							break;
						case "global":
							lastExpected = new String[]{";", "{", "}", "(", ",", "o"};
							nextExpected = new String[]{"v"};
							break;
						case "if":
							lastExpected = new String[]{";", "{", "}", "else"};
							nextExpected = new String[]{"("};
							break;
						case "else":
							nextExpected = new String[]{"{", "if"};
							break;
						case "break":
							nextExpected = new String[]{";"};
							break;
						case "while": case "for":
							nextExpected = new String[]{"("};
							break;
						case "in":
							lastExpected = new String[]{"v"};
							nextExpected = new String[]{"i", "l", "v", "f"};
							break;
						case "function": case "task":
							nextExpected = new String[]{"f"};
							break;
						case "return":
							nextExpected = new String[]{"v", ";", "f", "i", "l", "b", "t"};
							break;
						case "wait":
							nextExpected = new String[]{"i", "l", "v", "f", ";"};
							break;
					}
					break;
				
				// Operator
				case 'o':
					break;
				
				// Assignment operator
				case 'a':
					break;
				
				// Separator
				case 's':
					break;
				
				// Function/task
				case 'f':
					break;
				
				// Variable
				case 'v':
					break;
				
				// Value literals
				case 'i': case 'l': case 'b':
					break;
				
				// String
				case 't':
					break;
			}
			
			
			
			
			// Matched string in lastExpected
			if(i != 0){
				matched = false;
				
				if(lastExpected != null){
					for(String s:lastExpected){
						if(getData(lToken).equals(s) || (s.length() == 1 && type == s.charAt(0))){
							matched = true;
							break;
						}
					}
				}
				else matched = true;
				
				if(!matched){
					String s = lastExpected[0];
					
					if(s.equals("k"))		s = "keyword";
					else if(s.equals("o"))	s = "operator";
					else if(s.equals("s"))	s = "separator";
					else if(s.equals("f"))	s = "function/task";
					else if(s.equals("v"))	s = "variable";
					else if(s.equals("i"))	s = "int";
					else if(s.equals("l"))	s = "float";
					else if(s.equals("b"))	s = "boolean";
					else if(s.equals("t"))	s = "string";
					
					compilationError("Expected " + s + " before", token, lineNum);
				}
				
				lastExpected = null;
			}
			
			lToken = tokens.get(i);
			
			
			// Check brackets
			bracketsChanged = true;
				 if(token.equals("{"))	brackets[0]++;
			else if(token.equals("}"))	brackets[0]--;
			else if(token.equals("("))	brackets[1]++;
			else if(token.equals(")"))	brackets[1]--;
			else if(token.equals("["))	brackets[2]++;
			else if(token.equals("]"))	brackets[2]--;
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
		}
	}
	
	// Create syntax error and halt compilation
	private void compilationError(String type, String line, int lineNum){
		System.err.println("\nDScript compilation error (lexer):\n" + type + " in " + script.getFileName() + " on line " + lineNum + ":\n>> " + line);
		haltCompiler = true;
	}
	
	// Print tokens (debug)
	private void printTokens(String[] tokens){
		
		System.out.println("\nPrinting lexical tokens of " + script.getFileName() + ":\n");
		
		for(String t:tokens){
			String type = "keyword";
			
			switch(getType(t)){
				case 'o':	type = "operator";		break;
				case 'a':	type = "assignment";		break;
				case 's':	type = "separator";		break;
				case 'f':	type = "function/task";	break;
				case 'v':	type = "variable";		break;
				case 'i':	type = "int";			break;
				case 'l':	type = "float";			break;
				case 'b':	type = "boolean";		break;
				case 't':	type = "string";		break;
			}
			
			String lineNum = Integer.toString(getLineNum(t)) + ":\t";
			
			if(lineNum.length() == 3)
				lineNum += "\t";
			
			if(type.length() < 8)
				type += "\t";
			
			System.out.println("Line " + lineNum + type + "\t" + getData(t));
		}
	}
}
