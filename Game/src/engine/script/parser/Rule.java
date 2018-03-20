package engine.script.parser;

/**
 * 
 * Single context-free grammar rule.
 * Defined in Grammar.
 * 
 * @author Daniel
 * 
 */

public class Rule{
	
	private final String name;
	private final boolean finalValid;
	private final Object[][] patterns;
	
	public Rule(String name, Object[][] patterns){
		this(name, false, patterns);
	}
	
	public Rule(String name, boolean finalValid, Object[][] patterns){
		this.name = name;
		this.finalValid = finalValid;
		this.patterns = patterns;
	}
	
	public String getName(){
		return name;
	}
	
	public boolean isFinalValid(){
		return finalValid;
	}
	
	public Object[][] getPatterns(){
		return patterns;
	}
}
