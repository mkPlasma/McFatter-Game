package engine.newscript;

import engine.newscript.runner.ScriptRunner;

public class ScriptTest{
	public static void main(String[] args){
		
		DScript script = new DScript("test.dscript");
		
		new Compiler().compile(script);
		
		ScriptRunner runner = new ScriptRunner();
		runner.init(script);
		runner.run();
	}
}
