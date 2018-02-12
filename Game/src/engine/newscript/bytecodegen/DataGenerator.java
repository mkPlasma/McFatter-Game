package engine.newscript.bytecodegen;

import java.util.ArrayList;

import engine.newscript.DScript;
import engine.newscript.parser.ParseUnit;

public class DataGenerator{
	
	private ArrayList<Object> constants;
	
	
	public void process(DScript script){
		
		constants.clear();
		
		ArrayList<Object> parseTree = script.getParseTree();
		
		for(Object o:parseTree)
			process((ParseUnit)o);
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
		
	}
}
