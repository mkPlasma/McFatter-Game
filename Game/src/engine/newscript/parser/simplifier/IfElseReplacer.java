package engine.newscript.parser.simplifier;

import java.util.ArrayList;

import engine.newscript.DScript;
import engine.newscript.parser.ParseUnit;

public class IfElseReplacer{
	
	public void process(DScript script){
		
		ArrayList<Object> parseTree = script.getParseTree();
		
		for(Object o:parseTree)
			process((ParseUnit)o);
	}
	
	private void process(ParseUnit p){
		
		Object[] contents = p.getContents();
		
		// Replace if/else statements
		if(!p.getType().equals("if_else_chain")){
			for(int i = 0; i < contents.length; i++){
				
				Object o = contents[i];
				
				if(!(o instanceof ParseUnit))
					continue;
				
				
				ParseUnit p2 = (ParseUnit)o;
				
				// Search for if statements
				if(p2.getType().equals("if_block")){
					
					int endIndex = i;
					
					// Find following else if/else statements
					for(int j = i + 1; j < contents.length; j++){
						
						Object o2 = contents[j];
						
						if(!(o2 instanceof ParseUnit))
							break;
						
						String type = ((ParseUnit)o2).getType();
						
						if(!type.equals("else_if_block") && !type.equals("else_block"))
							break;
						
						endIndex = j;
					}
					
					// Replace if/else statements
					if(endIndex > i){
						
						Object[] cont = new Object[endIndex - i + 1];
						Object[] newCont = new Object[contents.length - cont.length + 1];
						
						
						System.arraycopy(contents, 0, newCont, 0, i);
						System.arraycopy(contents, endIndex + 1, newCont, i + 1, contents.length - endIndex - 1);
						
						System.arraycopy(contents, i, cont, 0, cont.length);
						
						newCont[i] = new ParseUnit("if_else_chain", cont);
						contents = newCont;
						
						p.setContents(newCont);
					}
				}
			}
		}
		
		
		for(Object o:contents){
			
			if(!(o instanceof ParseUnit))
				continue;
			
			process((ParseUnit)o);
		}
	}
}
