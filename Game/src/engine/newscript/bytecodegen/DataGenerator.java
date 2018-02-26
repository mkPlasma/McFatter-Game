package engine.newscript.bytecodegen;

import static engine.newscript.lexer.TokenType.*;
import static engine.newscript.parser.ParseUtil.*;

import java.util.ArrayList;

import engine.newscript.DScript;
import engine.newscript.lexer.Token;
import engine.newscript.parser.ParseUnit;

/**
 * 
 * Generates list of constants in parse tree and replaces them with constant tokens.
 * 
 * @author Daniel
 *
 */

public class DataGenerator{
	
	private ArrayList<Object> constants;
	
	
	public DataGenerator(){
		constants = new ArrayList<Object>();
	}
	
	public void process(DScript script){
		
		constants.clear();
		
		ArrayList<Object> parseTree = script.getParseTree();
		
		for(Object o:parseTree)
			process((ParseUnit)o);
		
		
		script.setConstants(constants.toArray(new Object[0]));
	}
	
	private void process(ParseUnit p){
		
		addConstants(p);
		
		Object[] contents = p.getContents();
		
		for(Object o:contents){
			
			if(!(o instanceof ParseUnit))
				continue;
			
			process((ParseUnit)o);
		}
	}
	
	private void addConstants(ParseUnit p){
		
		Object[] contents = p.getContents();
		
		// Array
		if(p.getType().equals("array")){
			
			// Check if constant
			boolean constant = true;
			
			for(Object o:contents){
				
				ParseUnit exp = (ParseUnit)o;
				
				if(exp.getContents().length != 1 || !(exp.getContents()[0] instanceof Token)){
					constant = false;
					break;
				}
			}
			
			// Add if constant
			if(constant){
				
				// Create array
				ArrayList<Object> array = new ArrayList<Object>();
				
				for(Object o:contents)
					array.add(getValue(o));
				
				
				// Check if exists
				int ind = constants.indexOf(array);
				
				if(ind != -1){
					replaceParseUnit(p, new Token(CONST, Integer.toString(ind), p.getFile(), p.getLineNum()));
					return;
				}
				
				// Replace array
				replaceParseUnit(p, new Token(CONST, Integer.toString(constants.size()), p.getFile(), p.getLineNum()));
				
				// Add to list
				constants.add(array);
				return;
			}
		}
		
		
		for(int i = 0; i < contents.length; i++){
			
			Object o = contents[i];
			
			if(!(o instanceof Token))
				continue;
			
			Token t = (Token)o;
			
			// Strings
			if(t.getType() == STRING){
				
				// Check if exists
				int ind = constants.indexOf(t.getValue());
				
				if(ind != -1){
					contents[i] = new Token(CONST, Integer.toString(ind), t.getFile(), t.getLineNum());
					continue;
				}
				
				// Replace token
				contents[i] = new Token(CONST, Integer.toString(constants.size()), t.getFile(), t.getLineNum());
				
				// Add to list
				constants.add(t.getValue());
				continue;
			}
		}
	}
}
