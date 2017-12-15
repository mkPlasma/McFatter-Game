package engine.newscript;

public class ScriptCompiler{
	
	private final ScriptLexer lexer;
	
	public ScriptCompiler(){
		lexer = new ScriptLexer();
	}
	
	public void compile(DScript script){
		lexer.process(script);
	}
}
