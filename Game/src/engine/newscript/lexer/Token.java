package engine.newscript.lexer;

public class Token{
	
	private final TokenType type;
	private final String value;
	
	private final String file;
	private final int lineNum;
	
	public Token(TokenType type, String value, String file, int lineNum){
		this.type = type;
		this.value = value;
		this.file = file;
		this.lineNum = lineNum;
	}
	
	public TokenType getType(){
		return type;
	}
	
	public String getValue(){
		return value;
	}
	
	public String getFile(){
		return file;
	}
	
	public int getLineNum(){
		return lineNum;
	}
}
