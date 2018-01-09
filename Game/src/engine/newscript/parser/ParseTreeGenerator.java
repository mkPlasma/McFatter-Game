package engine.newscript.parser;

import java.util.ArrayList;
import java.util.Arrays;

import engine.newscript.DScript;
import engine.newscript.ScriptPrinter;
import engine.newscript.lexer.Token;

public class ParseTreeGenerator{
	
	private final Rule[] rules;
	
	private ArrayList<Object> tree;
	
	public ParseTreeGenerator(Grammar grammar){
		rules = grammar.getRules();
	}
	
	public void process(DScript script){
		
		// Copy tokens
		tree = new ArrayList<Object>(Arrays.asList(script.getTokens()));
		script.clearTokens();
		
		// Simplify, prioritize top rules
		for(int i = 0; i < rules.length; i++){
			Rule rule = rules[i];
			
			// Attempt to simplify once, try next rule if failed
			if(!simplify(rule))
				continue;
			
			// Continue simplifying
			while(simplify(rule));
			
			// Loop back to first rule
			i = -1;
		}
		
		ScriptPrinter.printParseTree(tree.toArray(new Object[0]));
		
		script.setParseTree(tree);
	}
	
	// Attempt to simplify objects on stack
	private boolean simplify(Rule rule){
		
		Object[][] patterns = rule.getPatterns();
		
		// Check every pattern
		for(Object[] pattern:patterns){
			
			boolean concat = pattern[pattern.length - 1] == null;
			int len = pattern.length - (concat ? 1 : 0);
			
			// If too few elements to match pattern
			if(tree.size() < len)
				continue;
			
			// Check against all elements
			for(int i = 0; i <= tree.size() - len; i++){

				Object[] contents = new Object[len];
				boolean matched = true;
				
				for(int j = 0; j < len; j++){
					contents[j] = tree.get(i + j);
					
					if(
						(contents[j] instanceof Token && ((Token)contents[j]).getType() != pattern[j]) ||
						(contents[j] instanceof ParseUnit && !((ParseUnit)contents[j]).getType().equals(pattern[j]))
						){
						matched = false;
						break;
					}
				}
				
				// Add new element if matched
				if(matched){
					
					// Remove elements
					for(int j = 0; j < len; j++)
						tree.remove(i);
					
					// Simplify ParseRules
					for(int j = 0; j < contents.length; j++){
						Object o = contents[j];
						
						if(!(o instanceof ParseUnit))
							continue;
						
						ParseUnit p = (ParseUnit)o;
						Object[] cont = p.getContents();
						
						// Single content
						/*if(cont.length == 1){
							contents[j] = cont[0];
							continue;
						}*/
						
						// Concatenation
						if(concat && rule.getName().equals(p.getType())){
							
							// Expand array, copy values
							Object[] contCopy = contents;
							contents = new Object[contents.length + cont.length - 1];
							int ind = j + cont.length;
							
							System.arraycopy(contCopy, 0, contents, 0, j);
							System.arraycopy(cont, 0, contents, j, cont.length);
							System.arraycopy(contCopy, j + 1, contents, ind, contCopy.length - j - 1);
						}
					}
					
					// Dispose tokens
					int disp = 0;
					
					for(Object o:contents)
						if(o instanceof Token && ((Token)o).getType().dispose())
							disp++;
					
					if(disp > 0){
						Object[] contCopy = contents;
						contents = new Object[contents.length - disp];
						
						disp = 0;
						
						for(Object o:contCopy)
							if(!(o instanceof Token) || (o instanceof Token && !((Token)o).getType().dispose()))
								contents[disp++] = o;
					}
					
					
					// Set parents
					ParseUnit added = new ParseUnit(rule.getName(), contents);
					
					for(Object o:contents)
						if(o instanceof ParseUnit)
							((ParseUnit)o).setParent(added);
					
					tree.add(i, added);
					
					return true;
				}
			}
		}
		
		return false;
	}
}
