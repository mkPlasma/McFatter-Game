package engine.newscript;

public class Token{
	
	public enum TokenType{
		SET,
		CONST,
		GLOBAL,
		IF,
		ELSE,
		BREAK,
		WHILE,
		FOR,
		IN,
		FUNCTION,
		TASK,
		RETURN,
		WAIT,
		
		IDENTIFIER,
		
		BRACE_L,
		BRACE_R,
		PAREN_L,
		PAREN_R,
		BRACKET_L,
		BRACKET_R,
		COMMA,
		DOT,
		SEMICOLON,
		
		NUM_OP,
		COMPARISON,
		BOOL_OP,
		BOOL_UNARY,
		ASSIGNMENT,
		UNARY_ASSIGN,
		
		INT,
		FLOAT,
		TRUE,
		FALSE,
		STRING,
		
		LINE,
		FILE;
	}
	
	private final TokenType type;
	private final String value;
	
	
	public Token(TokenType type){
		this(type, null);
	}
	
	public Token(TokenType type, String value){
		this.type = type;
		this.value = value;
	}
	
	public TokenType getType(){
		return type;
	}
	
	public String getValue(){
		return value;
	}
}
