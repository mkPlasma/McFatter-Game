package engine.newscript;

public class ScriptTest{
	public static void main(String[] args){
		DScript script = new DScript(".ref/");
		
		new ScriptCompiler().compile(script);
	}
}
