package engine.newscript.parser;

import java.util.ArrayList;
import java.util.Arrays;

import engine.newscript.DScript;
import engine.newscript.lexer.Token;
import engine.newscript.lexer.TokenType;

/**
 * 
 * Generates parse tree from list of tokens.
 * 
 * @author Daniel
 * 
 */

public class ParseTreeGenerator{
	
	private final Rule[] rules;
	private final Object[][] replacements;
	
	private ArrayList<Object> parseTree;
	
	public ParseTreeGenerator(Grammar grammar){
		rules = grammar.getRules();
		replacements = grammar.getReplacements();
	}
	
	public void process(DScript script){
		
		// Copy tokens
		parseTree = new ArrayList<Object>(Arrays.asList(script.getTokens()));
		script.clearTokens();
		
		// Simplify, prioritize top rules
		for(int i = 0; i < rules.length; i++){
			Rule rule = rules[i];
			
			// Attempt to simplify once, try next rule if failed
			if(!simplify(rule))
				continue;
			
			// Loop back to first rule if successful
			i = -1;
		}
		
		//ScriptPrinter.printParseTree(parseTree.toArray(new Object[0]));
		
		script.setParseTree(parseTree);
	}
	
	// Attempt to simplify objects on stack
	private boolean simplify(Rule rule){
		
		Object[][] patterns = rule.getPatterns();
		
		// Check every pattern
		for(Object[] pattern:patterns){
			
			boolean concat = pattern[pattern.length - 1] == null;
			int len = pattern.length - (concat ? 1 : 0);
			
			// If too few elements to match pattern
			if(parseTree.size() < len)
				continue;
			
			// Check against all elements
			check:
			for(int i = 0; i <= parseTree.size() - len; i++){
				
				Object[] contents = new Object[len];
				
				// Check elements
				for(int j = 0; j < len; j++){
					
					boolean matched = false;
					
					// Check replacements
					for(int k = 0; k < replacements.length + 1; k++){
						contents[j] = parseTree.get(i + j);
						
						// Compared objects
						Object a = contents[j];
						Object b = pattern[j];
						
						// Replace
						Object[] rep = k == 0 ? null : replacements[k - 1];
						
						if(k > 0 && ((b instanceof String && rep[1] instanceof String && b.equals(rep[1])) ||
									 (b instanceof TokenType && rep[1] instanceof TokenType && b == rep[1])))
							b = rep[0];
						
						// Check match
						matched = (a instanceof Token && ((Token)a).getType() == b) ||
								  (a instanceof ParseUnit && ((ParseUnit)a).getType().equals(b));
						
						// Matched one token/unit, continue to next
						if(matched){
							// If replacement was a ParseUnit, place content inside
							if(k > 0 && rep[1] instanceof String)
								contents[j] = new ParseUnit((String)rep[1], new Object[]{a});
							
							break;
						}
					}
					
					// Did not match, shift pattern
					if(!matched)
						continue check;
				}
				
				// Add new elements if matched
				
				// Remove elements
				for(int j = 0; j < len; j++)
					parseTree.remove(i);
				
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
				
				parseTree.add(i, new ParseUnit(rule.getName(), contents));
				return true;
			}
		}
		
		return false;
	}
}
