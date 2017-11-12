package engine.script;

/*
 * 		ScriptCompiler.java
 * 		
 * 		Purpose:	Compiles a DScript object into bytecode.
 * 		Notes:		
 * 		
 */

public class ScriptCompiler{
	
	private ScriptLexer lexer;
	private ScriptParser parser;
	
	private boolean failed;
	private String errorText;
	
	public ScriptCompiler(){
		lexer = new ScriptLexer();
		parser = new ScriptParser();
	}
	
	public void compile(DScript script){
		
		lexer.analyze(script);
		
		failed = lexer.failed();
		
		if(failed){
			errorText = lexer.getErrorText();
			return;
		}
		
		parser.parse(script);
		
		failed = parser.failed();
		
		if(failed){
			errorText = parser.getErrorText();
			return;
		}
	}
	
	public boolean failed(){
		return failed;
	}
	
	public String getErrorText(){
		return errorText;
	}
}
