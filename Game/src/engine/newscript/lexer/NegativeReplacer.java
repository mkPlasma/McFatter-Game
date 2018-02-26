package engine.newscript.lexer;

import static engine.newscript.lexer.TokenType.*;

import java.util.ArrayList;
import java.util.Arrays;

import engine.newscript.DScript;

/**
 * 
 * Replaces minus and number tokens with negative number tokens where appropriate.
 * 
 * @author Daniel
 * 
 */

public class NegativeReplacer{
	
	private ArrayList<Token> tokens;
	
	public void process(DScript script){
		tokens = new ArrayList<Token>(Arrays.asList(script.getTokens()));
		
		for(int i = 0; i < tokens.size() - 1; i++){
			
			Token t1 = tokens.get(i);
			Token t2 = tokens.get(i + 1);
			
			if(t1.getType() == MINUS && (t2.getType() == INT || t2.getType() == FLOAT)){
				
				if(i == 0){
					replace(0);
					continue;
				}
				
				// Previous token
				TokenType t3 = tokens.get(i - 1).getType();
				
				// Variable scope exception
				if(t3 == GREATER_THAN){
					if(i <= 3)
						replace(i);
					else if(tokens.get(i - 2).getType() != INT && tokens.get(i - 3).getType() != LESS_THAN && tokens.get(i - 4).getType() != IDENTIFIER)
						replace(i);
					
					continue;
				}
				
				if(t3 != INT && t3 != FLOAT && t3 != IDENTIFIER && t3 != BRACE_R && t3 != PAREN_R && t3 != BRACKET_R)
					replace(i);
			}
		}
		
		script.setTokens(tokens.toArray(new Token[0]));
	}
	
	private void replace(int index){
		
		// Remove negative
		tokens.remove(index);
		
		// Set value
		Token t = tokens.get(index);
		tokens.set(index, new Token(t.getType(), "-" + t.getValue(), t.getFile(), t.getLineNum()));
	}
}
