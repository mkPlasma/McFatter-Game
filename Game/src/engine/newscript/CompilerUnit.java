package engine.newscript;

public abstract class CompilerUnit{
	
	protected final Compiler compiler;
	
	public CompilerUnit(Compiler compiler){
		this.compiler = compiler;
	}
	
	public abstract void process(DScript script);
}
