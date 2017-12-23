package engine.newscript;

public class Rule{
	
	private final String name;
	private final Object[][] patterns;
	
	public Rule(String name, Object[][] patterns){
		this.name = name;
		this.patterns = patterns;
	}
	
	public String getName(){
		return name;
	}
	
	public Object[][] getPatterns(){
		return patterns;
	}
}
