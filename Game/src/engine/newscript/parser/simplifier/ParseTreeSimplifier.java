package engine.newscript.parser.simplifier;

import engine.newscript.DScript;
import engine.newscript.ScriptException;

public class ParseTreeSimplifier{
	
	private final ExpressionSimplifier expressionSimplifier;
	private final VariableReplacer variableReplacer;
	private final FunctionReplacer functionReplacer;
	private final StatementSimplifier statementSimplifier;
	private final IfElseReplacer ifElseReplacer;
	
	public ParseTreeSimplifier(){
		expressionSimplifier	= new ExpressionSimplifier();
		variableReplacer		= new VariableReplacer();
		functionReplacer		= new FunctionReplacer();
		statementSimplifier		= new StatementSimplifier();
		ifElseReplacer			= new IfElseReplacer();
	}
	
	public void process(DScript script) throws ScriptException{
		expressionSimplifier.process(script);
		variableReplacer.process(script);
		functionReplacer.process(script);
		statementSimplifier.process(script);
		ifElseReplacer.process(script);
		
		// Simplify expressions again after constant variables have been replaced
		expressionSimplifier.process(script);
	}
}
