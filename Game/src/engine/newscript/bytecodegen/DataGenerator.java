package engine.newscript.bytecodegen;

import static engine.newscript.lexer.TokenType.*;

import java.util.ArrayList;

import engine.newscript.DScript;
import engine.newscript.lexer.Token;
import engine.newscript.parser.ParseUnit;

public class DataGenerator{
	
	private int numVariables;
	
	public void process(DScript script){
		
		ArrayList<Object> parseTree = script.getParseTree();
		
		for(Object o:parseTree)
			process((ParseUnit)o);
		
		
		script.setNumVariables(numVariables + 1);
	}
	
	private void process(ParseUnit p){
		
		getVariableCount(p);
		
		Object[] contents = p.getContents();
		
		for(Object o:contents){
			
			if(!(o instanceof ParseUnit))
				continue;
			
			process((ParseUnit)o);
		}
	}
	
	private void getVariableCount(ParseUnit p){
		
		Object[] contents = p.getContents();
		
		for(Object o:contents){
			
			if(!(o instanceof Token) || p.getType().equals("func_def") || p.getType().equals("task_def") || p.getType().equals("func_call"))
				continue;
			
			// Ignore identifiers in functions
			
			Token t = (Token)o;
			
			if(t.getType() == IDENTIFIER)
				numVariables = Math.max(numVariables, Integer.parseInt(t.getValue()));
		}
	}
}
