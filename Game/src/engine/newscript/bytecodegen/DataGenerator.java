package engine.newscript.bytecodegen;

import java.util.ArrayList;

import engine.newscript.DScript;
import engine.newscript.parser.ParseUnit;

public class DataGenerator{
	
	public void process(DScript script){
		
		ArrayList<Object> parseTree = script.getParseTree();
		
		for(Object o:parseTree)
			process((ParseUnit)o);
	}
	
	private void process(ParseUnit p){
		
		Object[] contents = p.getContents();
		
		for(Object o:contents){
			
			if(!(o instanceof ParseUnit))
				continue;
			
			process((ParseUnit)o);
		}
	}
}
