package engine.script;

/*
 * 		ScriptCompiler.java
 * 		
 * 		Purpose:	Compiles a DScript object into bytecode.
 * 		Notes:		
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				10/6
 * 		Changes:			Created
 */

public class ScriptCompiler{
	
	private ScriptLexer lexer;
	private ScriptParser parser;
	
	public ScriptCompiler(){
		lexer = new ScriptLexer();
		parser = new ScriptParser();
	}
	
	public void compile(DScript script){
		lexer.analyze(script);
		parser.parse(script);
	}
}
