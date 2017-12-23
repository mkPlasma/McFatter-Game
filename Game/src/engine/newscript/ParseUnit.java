package engine.newscript;

public class ParseUnit{
	
	private final String type;
	
	private final Object[] contents;
	
	public ParseUnit(String type, Object[] contents){
		this.type = type;
		this.contents = contents;
	}
	
	public String getType(){
		return type;
	}
	
	public Object[] getContents(){
		return contents;
	}
}
