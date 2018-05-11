package engine.script.parser.simplifier;

import engine.script.DScript;
import engine.script.ScriptException;
import engine.script.ScriptPrinter;

/**
 * 
 * Holds instances of all other parse tree simplifier and replacer classes.
 * 
 * @author Daniel
 * 
 */

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
		
		// Simplify expressions again after constant variables have been replaced
		expressionSimplifier.process(script);
		
		functionReplacer.process(script);
		statementSimplifier.process(script);
		ifElseReplacer.process(script);
	}
}
