package engine.script.parser.checker;

import static engine.script.lexer.TokenType.*;

import java.util.ArrayList;

import engine.script.DScript;
import engine.script.ScriptException;
import engine.script.lexer.Token;
import engine.script.parser.ParseUnit;

/**
 * 
 * Checks list parse units in for loops and function definitions.
 * 
 * @author Daniel
 * 
 */

public class ListChecker{
	
	public void process(DScript script) throws ScriptException{
		
		ArrayList<Object> parseTree = script.getParseTree();
		
		for(Object o:parseTree)
			findLists((ParseUnit)o);
	}
	
	private void findLists(ParseUnit p) throws ScriptException{
		
		Object[] contents = p.getContents();
		
		for(Object o:contents){
			
			if(o instanceof Token)
				continue;
			
			ParseUnit p2 = (ParseUnit)o;
			
			if(p2.getType().equals("list"))
				checkList(p2);
			else
				findLists(p2);
		}
	}
	
	private void checkList(ParseUnit p) throws ScriptException{
		
		String parentType = p.getParent().getType();
		
		// For loops can only have 3 parameters max
		if(parentType.equals("for_cond") && p.getContents().length > 3)
			throw new ScriptException("For loops must have at most 3 parameters", p.getFile(), p.getLineNum());
		
		// Function/tasks definitions must only have variable names
		else if(parentType.equals("func_def") || parentType.equals("task_def")){
			
			Object[] contents = p.getContents();
			
			for(Object o:contents){
				
				Object[] cont = ((ParseUnit)o).getContents();
				
				if(cont.length > 1 || !(cont[0] instanceof Token) || ((Token)cont[0]).getType() != IDENTIFIER)
					throw new ScriptException("Invalid function/task parameters", p.getFile(), p.getLineNum());
			}
		}
	}
}
