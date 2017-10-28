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
 * 		Notes:		Sets up script for parsing and checks most syntax errors.
 * 		
 */

public class ScriptLexer{
	
	private DScript script;
	
	// Stops compilation
	private boolean haltCompiler = false;
	
	private ArrayList<String> tokens;
	
	// In multi-line comment
	boolean inComment = false;
	
	// Regexes
	
	public static final String
	
	// Operators
	rOperators = "\\+|-|\\*|/|%|\\^|!|(\\|\\|)|(&&)|<|>|(==)|(<=)|(>=)",
	
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
				if(!inComment) tokens.add(lineNum + (token.contains(".") ? "l:" : "i:") + matcher.group(1));
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
					if(token.equals("//"))
						return;
					if(token.equals("/*")){
						inComment = true;
						return;
					}
					if(token.equals("*/")){
						
						if(!inComment){
							compilationError("Invalid end of comment", line, lineNum);
							return;
						}
						
						inComment = false;
						return;
					}
					
					// If match fails with extra character, revert
					if(!token.matches(rOperators) && !token.matches(rAssignments)){
						token = token.substring(0, token.length() - 1);
						i--;
					}
				}
				
				if(!inComment) tokens.add(lineNum + (token.matches(rOperators) ? "o" : "a") + ":" + token);
				token = "";
				
				skipWhitespace = true;
			}
			
			// Add separators directly
			else if(token.matches(rSeparators)){
				if(!inComment) tokens.add(lineNum + "s:" + token);
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
				
				if(!inComment) tokens.add(lineNum + "t:" + s);
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
				
				if(!inComment){
					// Check if reserved word
					if(isReservedWord(token2)){
						if(token2.equals("true") || token2.equals("false"))
							tokens.add(lineNum + "b:" + token2);
						else
							tokens.add(lineNum + "k:" + token2);
					}
					
					// Add function
					else if(p2.contains("("))
						tokens.add(lineNum + "f:" + token2);
					
					// Add variable
					else
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
					compilationError("Invalid syntax (missing semicolon?)", line, lineNum);
				
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
		
		// Accept at beginning of file
		boolean lastExpectedStrict = true;
		
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
				String s1 = getTypeName(s.charAt(0));
				
				if(s1 != null)
					s = s1;
				
				compilationError("Expected " + s + " after", getData(lToken), lineNum);
				return;
			}
			
			nextExpected = null;
			
			// Set expected values
			switch(type){
				// Keyword
				case 'k':
					lastExpected = new String[]{";", "{", "}"};
					lastExpectedStrict = false;
					
					switch(token){
						case "set":
							nextExpected = new String[]{"v", "const"};
							break;
						case "const":
							lastExpected = new String[]{"set"};
							nextExpected = new String[]{"v"};
							lastExpectedStrict = true;
							break;
						case "global":
							lastExpected = new String[]{";", "{", "}", "(", "[", ",", "o", "a"};
							nextExpected = new String[]{"v"};
							lastExpectedStrict = true;
							break;
						case "if":
							lastExpected = new String[]{";", "{", "}", "else"};
							nextExpected = new String[]{"("};
							break;
						case "else":
							lastExpected = new String[]{"}"};
							nextExpected = new String[]{"{", "if"};
							lastExpectedStrict = true;
							break;
						case "break":
							nextExpected = new String[]{";"};
							break;
						case "while": case "for":
							nextExpected = new String[]{"("};
							break;
						case "in":
							lastExpected = new String[]{"v"};
							nextExpected = new String[]{"i", "l", "v", "f", "-"};
							lastExpectedStrict = true;
							break;
						case "function": case "task":
							nextExpected = new String[]{"f"};
							break;
						case "return":
							nextExpected = new String[]{"v", "f", "i", "l", "b", "t", ";", "!", "-", ".", "(", "{"};
							break;
						case "wait":
							nextExpected = new String[]{"i", "l", "v", "f", ";", "-", "."};
							break;
					}
					break;
				
				// Operator
				case 'o':
					lastExpected = new String[]{"v", "f", "i", "l", ")", "]", "}"};
					nextExpected = new String[]{"v", "f", "i", "l", "(", "{", "global", "-", "."};
					
					switch(token){
						case "!":
							lastExpected = new String[]{"||", "&&", "==", "(", "{", "=", ","};
							nextExpected = new String[]{"v", "f", "b", "(", "{", "global"};
							break;
						
						case "||": case "&&":
							lastExpected = new String[]{"v", "f", "i", "l", "b", ")", "]", "}"};
							nextExpected = new String[]{"v", "f", "i", "l", "b", "(", "{", "!", "-", ".", "global"};
							break;
						
						case "==":
							lastExpected = new String[]{"v", "f", "i", "l", "b", "t", ")", "]", "}"};
							nextExpected = new String[]{"v", "f", "i", "l", "b", "t", "(", "{", "!", "-", ".", "global"};
							break;
					
						case "+":
							lastExpected = new String[]{"v", "f", "i", "l", "t", ")", "]", "}"};
							nextExpected = new String[]{"v", "f", "i", "l", "t", "(", "{", "-", ".", "global"};
							break;
					
						case "-":
							lastExpected = new String[]{"v", "o", "a", "f", "i", "l", ",", "(", ")", "[", "]", "}", "return", "wait", "in"};
							nextExpected = new String[]{"v", "f", "i", "l", "(", "{", "-", ".", "global"};
						break;
					}
					break;
				
				// Assignment operator
				case 'a':
					lastExpected = new String[]{"v", "]"};
					nextExpected = new String[]{"v", "f", "i", "l", "b", ")", "-", ".", "global"};
					
					switch(token){
						case "=":
							nextExpected = new String[]{"v", "f", "i", "l", "b", "t", "{", "(", "!", "-", ".", "global"};
							break;
					
						case "+=":
							nextExpected = new String[]{"v", "f", "i", "l", "b", "t", "(", "-", ".", "global"};
							break;
					
						case "++": case "--":
							nextExpected = new String[]{";"};
							break;
					}
					
					break;
				
				// Separator
				case 's':
					switch(token){
						case "(":
							lastExpected = new String[]{"o", "a", "f", "(", "{", "if", "while", "for", "return"};
							nextExpected = new String[]{"v", "f", "i", "l", "b", "t", "{", "(", ")", "!", "-", ".", "global"};
							break;
						case ")":
							lastExpected = new String[]{"v", "i", "l", "b", "t", "}", "(", ")", "]"};
							nextExpected = new String[]{"o", "{", ";", ",", ")", "[", "]"};
							break;
						case "{":
							lastExpected = new String[]{")", "(", "{", "o", "a", "else", "return", "in"};
							break;
						case "[":
							lastExpected = new String[]{"v", "}", ")"};
							nextExpected = new String[]{"v", "f", "i", "-", ".", "global"};
							break;
						case "]":
							lastExpected = new String[]{"v", "i", ")", "]"};
							nextExpected = new String[]{"a", "o", ")", "]", ",", ".", ";"};
							break;
						case ",":
							lastExpected = new String[]{"v", "i", "l", "b", "t", ")", "]", "}"};
							nextExpected = new String[]{"v", "f", "i", "l", "b", "t", "!", "-", ".", "global"};
							break;
						case ".":
							lastExpected = new String[]{"v", ")", "]", "o", "a", ",", "{", "(", "[", "in", "return", "wait"};
							nextExpected = new String[]{"f", "i"};
							break;
						case ";":
							lastExpected = new String[]{"v", "i", "l", "b", "t", "}", ")", "]", "++", "--", "break", "return", "wait"};
							break;
					}
					break;
				
				// Function/task
				case 'f':
					lastExpected = new String[]{"function", "task", "o", "a", ";", ",", "{", "}", "(", "[", ".", "in", "return", "wait"};
					nextExpected = new String[]{"("};
					lastExpectedStrict = false;
					break;
				
				// Variable
				case 'v':
					lastExpected = new String[]{"o", "a", ";", ",", "{", "}", "(", "[", "set", "const", "global", "in", "return", "wait"};
					nextExpected = new String[]{"o", "a", ";", ",", "}", ")", "[", "]", ".", "in"};
					break;
				
				// Value literals
				case 'i': case 'l': case 'b':
					lastExpected = new String[]{"o", "a", ",", "{", "(", "[", "in", "return", "wait"};
					nextExpected = new String[]{"o", ";", ",", "}", ")", "]"};
					break;
				
				// String
				case 't':
					lastExpected = new String[]{"=", "+", "==", "+=", "{", "(", ",", "return"};
					nextExpected = new String[]{"+", ",", "==", ";", "}", ")"};
					break;
			}
			
			
			
			
			// Matched string in lastExpected
			if(i != 0){
				matched = false;
				String lData = getData(lToken);
				char lType = getType(lToken);
				
				if(lastExpected != null){
					for(String s:lastExpected){
						if(lData.equals(s) || (s.length() == 1 && lType == s.charAt(0))){
							matched = true;
							break;
						}
					}
				}
				else matched = true;
				
				if(!matched){
					String s = lastExpected[0];
					String s1 = getTypeName(s.charAt(0));
					
					if(s1 != null)
						s = s1;
					
					compilationError("Expected " + s + " before", token, lineNum);
					return;
				}
				
				// Change to single negative number
				if(getData(token).equals("-")){
					if((i < tokens.size() + 1 && (getType(tokens.get(i + 1)) == 'i' || getType(tokens.get(i + 1)) == 'l' ||
						getType(tokens.get(i + 1)) == 'v' || getType(tokens.get(i + 1)) == 'f' || getData(tokens.get(i + 1)).equals("("))) &&
						(lType == 'o' || lType == 'a' || lData.equals(",") || lData.equals("(") ||
						lData.equals("[") || lData.equals("return") || lData.equals("wait") || lData.equals("in"))){
						
						char type2 = getType(tokens.get(i + 1));
						
						// Ints and floats
						if(type2 == 'i' || type2 == 'l'){
							// Remove -
							tokens.remove(i);
							
							// Set number to negative
							String t = tokens.get(i);
							String t2 = t.substring(0, t.indexOf(':') + 1);
							t = getData(t);
							
							tokens.set(i, t2 + '-' + t);
							
							nextExpected = null;
							i--;
						}
						
						// Other
						else{
							// Add 0 in front of -, skip it
							tokens.add(i, lineNum + "i:0");
							i++;
						}
					}
				}
				
				// Float values without leading zero
				else if(getData(token).equals(".")){
					if(i < tokens.size() + 1 && getType(tokens.get(i + 1)) == 'i' && lType != 'v' && !lData.equals(")") && !lData.equals("]")){
						
						// Remove .
						tokens.remove(i);
						
						// Set value
						String t = tokens.get(i);
						String t2 = t.substring(0, t.indexOf(':') + 1).replace("i:", "l:");
						t = getData(t);
						
						tokens.set(i, t2 + '.' + t);
						
						nextExpected = null;
						i--;
					}
				}
				
				lastExpected = null;
				lastExpectedStrict = true;
			}
			else if(lastExpectedStrict){
				String s = lastExpected[0];
				String s1 = getTypeName(s.charAt(0));
				
				if(s1 != null)
					s = s1;
				
				compilationError("Expected " + s + " before", token, lineNum);
				return;
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
			else bracketsChanged = false;
			
			// Check for extra close brackets
			if(bracketsChanged){
				for(int j:brackets){
					if(j < 0){
						compilationError("Extra close bracket", token, lineNum);
						return;
					}
				}
			}
			
			// Check for unclosed brackets
			if(i == tokens.size() - 1){
				for(int j:brackets){
					if(j > 0){
						compilationError("Unclosed bracket" + (j != 1 ? "s" : ""), token, lineNum);
						return;
					}
				}
			}
		}
	}
	
	// Return type name
	private String getTypeName(char type){
		switch(type){
			case 'k':	return "keyword";
			case 'o':	return "operator";
			case 'a':	return "assignment";
			case 's':	return "separator";
			case 'f':	return "function/task";
			case 'v':	return "variable";
			case 'i':	return "int";
			case 'l':	return "float";
			case 'b':	return "boolean";
			case 't':	return "string";
		}
		
		return null;
	}
	
	// Create syntax error and halt compilation
	private void compilationError(String type, String line, int lineNum){
		System.err.println("\nDScript compilation error (lexer):\n" + type + " in " + script.getFileName() + " on line " + lineNum + ":\n>> " + line);
		haltCompiler = true;
	}
	
	// Print tokens (debug)
	@SuppressWarnings("unused")
	private void printTokens(String[] tokens){
		
		System.out.println("\nPrinting lexical tokens of " + script.getFileName() + ":\n");
		
		for(String t:tokens){
			String type = getTypeName(getType(t));
			
			String lineNum = Integer.toString(getLineNum(t)) + ":\t";
			
			if(lineNum.length() == 3)
				lineNum += "\t";
			
			if(type.length() < 8)
				type += "\t";
			
			System.out.println("Line " + lineNum + type + "\t" + getData(t));
		}
	}
}
