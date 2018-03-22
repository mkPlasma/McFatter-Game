package engine.script.parser;

import engine.script.DScript;
import engine.script.ScriptException;
import engine.script.ScriptPrinter;
import engine.script.parser.checker.ParseTreeChecker;
import engine.script.parser.simplifier.ParseTreeSimplifier;

/**
 * 
 * Contains instances of other parser classes.
 * 
 * @author Daniel
 * 
 */

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
		
		//ScriptPrinter.printParseTree(script.getParseTree().toArray(new Object[0]));
	}
}
