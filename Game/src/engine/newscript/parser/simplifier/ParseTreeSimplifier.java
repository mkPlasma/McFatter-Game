package engine.newscript.parser.simplifier;

import engine.newscript.DScript;

public class ParseTreeSimplifier{
	
	private final VariableReplacer variableReplacer;
	private final FunctionReplacer functionReplacer;
	private final StatementSimplifier statementSimplifier;
	private final ExpressionSimplifier expressionSimplifier;
	
	public ParseTreeSimplifier(){
		variableReplacer		= new VariableReplacer();
		functionReplacer		= new FunctionReplacer();
		statementSimplifier	= new StatementSimplifier();
		expressionSimplifier	= new ExpressionSimplifier();
	}
	
	public void process(DScript script){
		expressionSimplifier.process(script);
		variableReplacer.process(script);
		functionReplacer.process(script);
		statementSimplifier.process(script);
		
		// Simplify expressions again after constant variables have been replaced
		expressionSimplifier.process(script);
	}
}
