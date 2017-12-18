package engine.newscript;

public class Compiler{
	
	private final Preprocessor preprocessor;
	private final Lexer lexer;
	
	public Compiler(){
		preprocessor = new Preprocessor(this);
		lexer = new Lexer(this);
	}
	
	public void compile(DScript script){
		preprocessor.process(script);
		lexer.process(script);
	}
	
	public void error(String message){
		System.err.println(message);
	}
}
