package engine.newscript.parser.simplifier;

import engine.newscript.DScript;
import engine.newscript.ScriptPrinter;

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
		variableReplacer.process(script);
		functionReplacer.process(script);
		statementSimplifier.process(script);
		ScriptPrinter.printParseTree(script.getParseTree().toArray(new Object[0]));
		expressionSimplifier.process(script);
		
		ScriptPrinter.printParseTree(script.getParseTree().toArray(new Object[0]));
	}
}
