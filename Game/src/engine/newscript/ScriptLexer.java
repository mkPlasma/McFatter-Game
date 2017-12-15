package engine.newscript;

public class ScriptLexer{
	
	// Regular expressions
	private static final String
	
	rgxIdentifier = "[a-zA-Z_]\\w*",
	
	rgxInteger = "\\d+";
	
	
	
	private DScript script;
	
	
	public void process(DScript script){
		this.script = script;
		
		
	}
}
