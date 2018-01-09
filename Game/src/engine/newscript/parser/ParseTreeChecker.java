package engine.newscript.parser;

import java.util.ArrayList;

import engine.newscript.DScript;
import engine.newscript.ScriptException;
import engine.newscript.lexer.Token;

public class ParseTreeChecker{
	
	private final Errors errors;
	
	private final Rule[] finalRules;
	private final Rule[] errorRules;
	
	private ArrayList<Object> tree;
	
	
	private final ParseTreeExpressionChecker expressionChecker;
	private final ParseTreeContextChecker contextChecker;
	private final ParseTreeListChecker listChecker;
	
	public ParseTreeChecker(Grammar grammar){
		
		errors = new Errors();
		
		finalRules = grammar.getFinalValid();
		errorRules = errors.getRules();

		expressionChecker	= new ParseTreeExpressionChecker();
		contextChecker		= new ParseTreeContextChecker();
		listChecker			= new ParseTreeListChecker();
	}
	
	public void process(DScript script) throws ScriptException{
		
		tree = script.getParseTree();
		
		// Check specific errors first
		for(Rule rule:errorRules)
			check(rule);
		
		// Non-specific after
		for(Object o:tree){
			if(o instanceof ParseUnit){
				ParseUnit p = (ParseUnit)o;
				
				boolean found = false;
				
				for(Rule rule:finalRules){
					if(p.getType().equals(rule.getName())){
						found = true;
						break;
					}
				}
				
				if(!found)
					throw new ScriptException("Syntax error", p.getFile(), p.getLineNum());
			}
		}
		
		// Check for tokens
		for(Object o:tree)
			if(o instanceof Token)
				throw new ScriptException("Syntax error", ((Token)o).getFile(), ((Token)o).getLineNum());
		
		expressionChecker.process(script);
		contextChecker.process(script);
		listChecker.process(script);
	}
	
	private void check(Rule rule) throws ScriptException{
		
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
				
				// Throw exception if matches
				if(matched){
					
					Object o = contents[0];
					String file = o instanceof Token ? ((Token)o).getFile()		: ((ParseUnit)o).getFile();
					int lineNum = o instanceof Token ? ((Token)o).getLineNum()	: ((ParseUnit)o).getLineNum();
					
					throw new ScriptException(rule.getName(), file, lineNum);
				}
			}
		}
	}
}
