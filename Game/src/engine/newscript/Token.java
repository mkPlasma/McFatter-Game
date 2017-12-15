package engine.newscript;

public class Token{
	
	public static final int
		SET			= 0,
		CONST		= 1,
		GLOBAL		= 2,
		IF			= 3,
		ELSE			= 4,
		BREAK		= 5,
		WHILE		= 6,
		FOR			= 7,
		IN			= 8,
		FUNCTION		= 9,
		TASK			= 10,
		RETURN		= 11,
		WAIT			= 12,
		
		BRACE_L		= 13,
		BRACE_R		= 14,
		PAREN_L		= 15,
		PAREN_R		= 16,
		BRACKET_L	= 17,
		BRACKET_R	= 18,
		COMMA		= 19,
		DOT			= 20,
		SEMICOLON	= 21,
		
		
		ADD			= 22,
		SUBTRACT		= 23,
		MULTIPLY		= 24,
		DIVIDE		= 25,
		MODULO		= 26,
		EXPONENT		= 27,
		GREATER		= 28,
		LESS			= 29,
		EQUALS		= 30,
		INVERT		= 31,
		OR			= 32,
		AND			= 33,
		
		INT			= 34,
		FLOAT		= 35,
		TRUE			= 36,
		FALSE		= 37,
		STRING		= 38;
	
	
	private final int type;
	private final String value;
	
	
	public Token(int type){
		this(type, null);
	}
	
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
