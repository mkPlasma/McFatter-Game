package engine.newscript;

import engine.newscript.lexer.Lexer;
import engine.newscript.parser.Parser;
import engine.newscript.preprocess.Preprocessor;

public class Compiler{
	
	private final Preprocessor preprocessor;
	private final Lexer lexer;
	private final Parser parser;
	
	private DScript script;
	
	public Compiler(){
		preprocessor = new Preprocessor();
		lexer = new Lexer();
		parser = new Parser();
	}
	
	public boolean compile(DScript script){
		
		this.script = script;
		
		try{
			preprocessor.process(script);
			lexer.process(script);
			parser.process(script);
		}
		catch(ScriptException e){
			error(e, false);
			return false;
		}
		
		return true;
	}
	
	private void error(ScriptException e, boolean runtime){
		
		String file = e.getFile();
		int line = e.getLine();
		
		System.err.println(
			(runtime ? "Runtime" : "Compilation") + " error in " + (file == null ? script.getFileName(): file.substring(16)) +
			" on line " + line + ":\n" + e.getMessage() +
			(line > 0 ? "\n" + line + ": " + script.getLine(file, line) : "")
		);
	}
}
