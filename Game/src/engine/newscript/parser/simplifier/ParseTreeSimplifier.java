package engine.newscript.parser.simplifier;

import engine.newscript.DScript;
import engine.newscript.ScriptPrinter;

public class ParseTreeSimplifier{
	
	private final VariableReplacer variableReplacer;
	private final FunctionReplacer functionReplacer;
	private final BlockSimplifier blockSimplifier;
	private final ExpressionSimplifier expressionSimplifier;
	
	public ParseTreeSimplifier(){
		variableReplacer		= new VariableReplacer();
		functionReplacer		= new FunctionReplacer();
		blockSimplifier		= new BlockSimplifier();
		expressionSimplifier	= new ExpressionSimplifier();
	}
	
	public void process(DScript script){
		variableReplacer.process(script);
		functionReplacer.process(script);
		blockSimplifier.process(script);
		expressionSimplifier.process(script);
		
		ScriptPrinter.printParseTree(script.getParseTree().toArray(new Object[0]));
	}
}
