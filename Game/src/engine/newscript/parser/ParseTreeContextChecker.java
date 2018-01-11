package engine.newscript.parser;

import java.util.ArrayList;

import engine.newscript.DScript;
import engine.newscript.ScriptException;

public class ParseTreeContextChecker{
	
	public void process(DScript script) throws ScriptException{
		
		ArrayList<Object> parseTree = script.getParseTree();
		
		for(Object o:parseTree)
			check((ParseUnit)o, null);
	}
	
	private void check(ParseUnit p, ParseUnit prev) throws ScriptException{
		
		Object[] contents = p.getContents();
		
		switch(p.getType()){
			
			case "break":
				if(!isWithin(p, "while_block") && !isWithin(p, "until_block") && !isWithin(p, "for_block"))
					throw new ScriptException("Break statement must be in a loop", p.getFile(), p.getLineNum());
				
				break;
				
			case "return":
				
				boolean withinTask = isWithin(p, "task_block");
				
				if(!isWithin(p, "func_block") && !withinTask)
					throw new ScriptException("Return statement must be in a function or task", p.getFile(), p.getLineNum());
				
				if(withinTask && contents.length > 1)
					throw new ScriptException("Task cannot return a value", p.getFile(), p.getLineNum());
				
				break;
			
			case "function_block": case "task_block":
				
				if(isWithin(p, "if_block") ||
					isWithin(p, "if_else_block") ||
					isWithin(p, "else_block") ||
					isWithin(p, "while_block") ||
					isWithin(p, "until_block") ||
					isWithin(p, "for_block"))
					throw new ScriptException("Function/task cannot be defined inside control block", p.getFile(), p.getLineNum());
				
				break;
		}
		
		
		for(int i = 0; i < contents.length; i++){
			
			Object o1 = contents[i];
			
			Object o2 = i > 0 ? contents[i - 1] : null;
			o2 = o2 instanceof ParseUnit ? (ParseUnit)o2 : null;
			
			if(o1 instanceof ParseUnit)
				check((ParseUnit)o1, (ParseUnit)o2);
		}
	}
	
	private boolean isWithin(ParseUnit p, String type){
		
		ParseUnit parent = p.getParent();
		
		if(parent == null)
			return false;
		
		if(parent.getType().equals(type))
			return true;
		
		return isWithin(parent, type);
	}
}
