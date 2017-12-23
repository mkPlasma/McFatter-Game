package engine.newscript;

import java.util.Stack;

public class Parser extends CompilerUnit{
	
	private final Grammar grammar;
	private final Rule[] rules;
	
	private Token[] tokens;
	private int tokenIndex;
	
	private Stack<Object> stack;
	
	private DScript script;
	
	public Parser(Compiler compiler){
		super(compiler);
		
		grammar = new Grammar();
		rules = grammar.getRules();
		
		stack = new Stack<Object>();
	}
	
	public void process(DScript script){
		this.script = script;
		
		tokens = script.getTokens();
		tokenIndex = 0;
		
		script.clearTokens();
		
		boolean invalid = false;
		
		while(true){
			
			// Simplify first
			if(!stack.isEmpty())
				while(simplify());
			
			if(tokenIndex >= tokens.length)
				break;
			
			// Otherwise, add tokens
			stack.push(tokens[tokenIndex]);
			tokenIndex++;
		}
		
		if(invalid){
			compiler.error("parse error or some");
		}
		
		ScriptPrinter.printParseTree(stack.toArray(new Object[0]));
	}
	
	// Attempt to simplify objects on stack
	private boolean simplify(){
		
		for(Rule rule:rules){
			
			Object[][] patterns = rule.getPatterns();
			
			// Check every pattern
			for(Object[] pattern:patterns){
				
				int len = pattern.length;
				
				// If too few elements on stack to match pattern
				if(stack.size() < len)
					continue;
				
				Object[] contents = new Object[len];
				boolean match = true;
				
				// Check against topmost elements
				for(int i = 0; i < len; i++){
					
					contents[i] = stack.get(stack.size() - len + i);
					
					if(
						(contents[i] instanceof Token && ((Token)contents[i]).getType() != pattern[i]) ||
						(contents[i] instanceof ParseUnit && !((ParseUnit)contents[i]).getType().equals(pattern[i]))
						){
						match = false;
						break;
					}
				}
				
				// Add new element if matched
				if(match){
					// Pop top elements
					for(int i = 0; i < len; i++)
						stack.pop();
					
					// Add new element
					stack.push(new ParseUnit(rule.getName(), contents));
					
					return true;
				}
			}
		}
		
		return false;
	}
}
