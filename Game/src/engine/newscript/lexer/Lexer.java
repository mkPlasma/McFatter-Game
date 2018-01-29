package engine.newscript.lexer;

import static engine.newscript.lexer.TokenType.*;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import engine.newscript.DScript;
import engine.newscript.ScriptException;

public class Lexer{
	
	private ArrayList<Token> tokens;
	
	private String file;
	private int lineNum;
	
	private final NegativeReplacer negativeReplacer;
	
	
	public Lexer(){
		tokens = new ArrayList<Token>();
		negativeReplacer = new NegativeReplacer();
	}
	
	public void process(DScript script) throws ScriptException{
		
		tokens.clear();
		
		String[] file = script.getFile();
		script.clearFile();
		
		lineNum = 1;
		
		// Build tokens for each line
		for(String line:file){
			
			if(line.isEmpty()){
				lineNum++;
				continue;
			}
			
			if(line.startsWith("$line")){
				lineNum = Integer.parseInt(line.substring(6));
				continue;
			}
			else if(line.startsWith("$file")){
				this.file = line.substring(6);
				lineNum = 1;
				continue;
			}
			
			buildTokens(line);
			lineNum++;
		}
		
		script.setTokens(tokens.toArray(new Token[0]));
		
		negativeReplacer.process(script);
		//ScriptPrinter.printTokens(script.getTokens());
	}
	
	private void buildTokens(String line) throws ScriptException{
		Pattern pattern;
		Matcher matcher;
		
		// Token found
		boolean found = false;
		boolean invalid;
		
		do{
			
			invalid = true;
			
			// Check each regex
			for(TokenType type:TokenType.values()){
				pattern = Pattern.compile("^" + type.regex() + ".*?");
				matcher = pattern.matcher(line);
				
				found = matcher.find();
				
				// If matches
				if(found){
					String token = matcher.group(1);
					
					// Cut token out of line
					line = line.substring(token.length()).trim();
					
					// Remove space if negative number
					if(type == INT || type == FLOAT)
						token = token.replace(" ", "");
					
					// Remove quotes if string
					if(type == STRING)
						token = token.substring(0, token.length() - 1).substring(1);
					
					// Add token
					tokens.add(new Token(type, token, file, lineNum));
					
					invalid = false;
					break;
				}
			}
			
			// Token did not match any regex
			if(invalid)
				throw new ScriptException("Invalid token '" + line.charAt(0) + "'", file, lineNum);
			
			// If line is empty, finish
			if(line.isEmpty())
				break;
			
		}while(found);
	}
}
