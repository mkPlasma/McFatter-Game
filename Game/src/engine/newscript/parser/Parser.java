package engine.newscript.parser;

import engine.newscript.DScript;
import engine.newscript.ScriptException;
import engine.newscript.ScriptPrinter;
import engine.newscript.parser.checker.ParseTreeChecker;
import engine.newscript.parser.simplifier.ParseTreeSimplifier;

public class Parser{
	
	private final Grammar grammar;
	
	private final ParseTreeGenerator treeGen;
	private final ParseTreeChecker treeCheck;
	private final ParseTreeSimplifier treeSimplify;
	
	public Parser(){
		grammar = new Grammar();
		
		treeGen			= new ParseTreeGenerator(grammar);
		treeCheck		= new ParseTreeChecker(grammar);
		treeSimplify	= new ParseTreeSimplifier();
	}
	
	public void process(DScript script) throws ScriptException{
		treeGen.process(script);
		treeCheck.process(script);
		treeSimplify.process(script);
	}
}
