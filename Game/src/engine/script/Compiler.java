package engine.script;

import engine.script.bytecodegen.BytecodeCompiler;
import engine.script.lexer.Lexer;
import engine.script.parser.Parser;
import engine.script.preprocess.Preprocessor;

/**
 * 
 * Holds instances of all other DScript compiler classes.
 * Takes an .dscript file and generates its bytecode.
 * 
 * @author Daniel
 * 
 */

public class Compiler{
	
	private final Preprocessor preprocessor;
	private final Lexer lexer;
	private final Parser parser;
	private final BytecodeCompiler bytecodeCompiler;
	
	private DScript script;
	
	public Compiler(){
		preprocessor = new Preprocessor();
		lexer = new Lexer();
		parser = new Parser();
		bytecodeCompiler = new BytecodeCompiler();
	}
	
	public boolean compile(DScript script) throws ScriptException{
		
		this.script = script;
		
		preprocessor.process(script);
		lexer.process(script);
		parser.process(script);
		bytecodeCompiler.process(script);
		
		return true;
	}
}
