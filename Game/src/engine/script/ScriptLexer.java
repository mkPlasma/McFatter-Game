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

	// Operators
	String rOperators = "\\+|-|\\*|/|%|!|(\\|\\|)|(&&)|<|>|(==)|(<=)|(>=)";
	
	// Assignment operators
	String rAssignments = "=|(\\+\\+)|(--)|(\\+=)|(-=)|(\\*=)|(/=)|(%=)";
	
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
	
	
	/* 	Types
	 * 	
	 * 	k - keyword
	 * 	o - operator
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
				if(token.matches(rOperators)){
					if(i < line.length() - 1){
						token += line.charAt(++i);
						
						// Comments defined here singe / is an operator
						// Skip single line comments
						if(token.matches("//"))
							return;
						
						if(!token.matches(rOperators) && !token.matches(rAssignments)){
							token = token.substring(0, token.length() - 1);
							i--;
						}
					}
				}
				
				tokens.add(lineNum + "o:" + token);
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
		String last = "";
		
		for(int i = 0; i < tokens.size(); i++){
			
			// Get token
			String token = tokens.get(i);
			char type = getType(token);
			int lineNum = getLineNum(token);
			
			// Get data only
			token = getData(token);
			
			if(i != 0){
				last = tokens.get(i - 1);
				
				char lType = getType(last);
				String lToken = getData(last);				
				
				// Syntax errors if certain tokens were expected
				
				// All keywords should be preceded by ; { or }
				if(type == 'k'){
					if(token.equals("if")){
						if(!lToken.equals("else")){
							compilationError("Expected \"else\"", token, lineNum);
							return;
						}
						continue;
					}
					if(token.equals("const")){
						if(!lToken.equals("set")){
							compilationError("Expected \"set\"", token, lineNum);
							return;
						}
						continue;
					}
					if(token.equals("global")){
						if(!lToken.equals("set")){
							compilationError("Expected \"set\"", token, lineNum);
							return;
						}
						continue;
					}
					if(token.equals("in")){
						if(lType != 'v'){
							compilationError("Expected variable", token, lineNum);
							return;
						}
						continue;
					}
					if(!lToken.equals(";") && !lToken.equals("{") && !lToken.equals("}")){
						compilationError("Expected ; { or } on previous line", token, lineNum);
						return;
					}
				}
				
				// Variables/functions/literals should not come directly after one another
				else if((type == 'v' || type == 'f' || type == 'i' || type == 'l' || type == 'b' || type == 't') &&
					(lType == 'v' || lType == 'f' || lType == 'i' || lType == 'l' || lType == 'b' || lType == 't')){
					compilationError("Expected operation", token, lineNum);
					return;
				}
				
				// Operators require a value before them
				else if(type == 'o' && !token.equals("++") && !token.equals("--") && !token.equals("!") && lType != 'v' &&
					lType != 'f' && lType != 'i' && lType != 'l' && lType != 'b' && lType != 't' && !lToken.equals(")")){
						compilationError("Expected value", token, lineNum);
						return;
				}
				else if(type == 'o' && (token.equals("++") || token.equals("--")) && lType != 'v'){
					compilationError("Expected variable", token, lineNum);
					return;
				}
				
				// Operators require a value after them
				if(lType == 'o' && !lToken.equals("++") && !lToken.equals("--") && type != 'v' && type != 'f' &&
					type != 'i' && type != 'l' && type != 'b' && type != 't' && !token.equals("(") && !token.equals("!") &&
					!(lToken.contains("=") && !lToken.equals("==") && token.equals("{"))){
						compilationError("Expected value", token, lineNum);
						return;
				}
				
				// Keywords
				if(lType == 'k'){
					switch(lToken){
						case "set":
							if(type != 'v' && !token.equals("const")){
								compilationError("Expected identifier or const", token, lineNum);
								return;
							}
							continue;
						
						case "const": case "global":
							if(type != 'v'){
								compilationError("Expected identifier", token, lineNum);
								return;
							}
							continue;
						
						case "else":
							if(!token.equals("if") && !token.equals("{")){
								compilationError("Expected \"if\" or {", token, lineNum);
								return;
							}
							continue;
						
						case "break":
							if(!token.equals(";")){
								compilationError("Expected ;", token, lineNum);
								return;
							}
							continue;
						
						case "if": case "while": case "for":
							if(!token.equals("(")){
								compilationError("Expected (", token, lineNum);
								return;
							}
							continue;
						
						case "in":
							if(type != 'v' && type != 'f' && type != 'i' && type != 'l'){
								compilationError("Expected numerical value", token, lineNum);
								return;
							}
							continue;
						
						case "function": case "task":
							if(type != 'f'){
								compilationError("Expected " + last + " declaration", token, lineNum);
								return;
							}
							continue;
						
						case "return":
							if(!token.equals(";") && type != 'v' && type != 'f'){
								if(type != 'v' && type != 'f'){
									compilationError("Expected value", token, lineNum);
									return;
								}
								
								compilationError("Expected ;", token, lineNum);
								return;
							}
							continue;
						
						case "wait":
							if(type != 'i' && type != 'l' && type != 'v' && type != 'f'){
								compilationError("Expected value", token, lineNum);
								return;
							}
							continue;
					}
				}
			}
			
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
		System.err.println("\nDScript compilation error:\n" + type + " in " + script.getFileName() + " on line " + lineNum + ":\n>> " + line);
		haltCompiler = true;
	}
	
	// Print tokens (debug)
	private void printTokens(String[] tokens){
		
		System.out.println("\nPrinting lexical tokens of " + script.getFileName() + ":\n");
		
		for(String t:tokens){
			String type = "keyword";
			
			switch(getType(t)){
				case 'o':	type = "operator";		break;
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
