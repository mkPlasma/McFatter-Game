package engine.newscript;

public class Token{
	
	public static final int
		KEYWORD		= 0,
		IDENTIFIER	= 1,
		OPERATION	= 2,
		INT			= 3;
	
	private final int type;
	private final String value;
	
	public Token(int type, String value){
		this.type = type;
		this.value = value;
	}
	
	public int getType(){
		return type;
	}
	
	public String getValue(){
		return value;
	}
}
