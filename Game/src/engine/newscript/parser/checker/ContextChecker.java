package engine.newscript.parser.checker;

import static engine.newscript.parser.ParseUtil.*;

import java.util.ArrayList;

import engine.newscript.DScript;
import engine.newscript.ScriptException;
import engine.newscript.parser.ParseUnit;

public class ContextChecker{
	
	public void process(DScript script) throws ScriptException{
		
		ArrayList<Object> parseTree = script.getParseTree();
		
		for(Object o:parseTree)
			check((ParseUnit)o, null);
	}
	
	private void check(ParseUnit p, ParseUnit prev) throws ScriptException{
		
		Object[] contents = p.getContents();
		
		switch(p.getType()){
			
			case "break":
				if(!p.isWithin("while_block") && !p.isWithin("until_block") && !p.isWithin("for_block"))
					throw new ScriptException("Break statement must be in a loop", p.getFile(), p.getLineNum());
				
				break;

			case "return":
				
				boolean withinTask = p.isWithin("task_block");
				
				if(!p.isWithin("func_block") && !withinTask)
					throw new ScriptException("Return statement must be in a function or task", p.getFile(), p.getLineNum());
				
				if(withinTask && contents.length > 1)
					throw new ScriptException("Task cannot return a value", p.getFile(), p.getLineNum());
				
				break;
				
			case "returnif":
				
				if(!p.isWithin("func_block") && !p.isWithin("task_block"))
					throw new ScriptException("Returnif statement must be in a function or task", p.getFile(), p.getLineNum());
				
				break;
			
			case "function_block": case "task_block":
				
				if(p.isWithin("if_block")		||
					p.isWithin("if_else_block")	||
					p.isWithin("else_block")	||
					p.isWithin("while_block")	||
					p.isWithin("until_block")	||
					p.isWithin("for_block"))
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
}
