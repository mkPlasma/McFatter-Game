package engine.newscript.lexer;

public enum TokenType{
	
	SET				("(set)\\b",		true),
	CONST			("(const)\\b",		true),
	IF				("(if)\\b",			true),
	ELSE			("(else)\\b",		true),
	WHILE			("(while)\\b",		true),
	UNTIL			("(until)\\b",		true),
	FOR				("(for)\\b",		true),
	IN				("(in)\\b",			true),
	BREAK			("(break)\\b"),
	FUNCTION		("(function)\\b",	true),
	TASK			("(task)\\b",		true),
	RETURN			("(return)\\b"),
	RETURNIF		("(returnif)\\b",	true),
	WAIT			("(wait)\\b"),
	WAITS			("(waits)\\b"),
	
	BRACE_L			("(\\{)",	true),
	BRACE_R			("(\\})",	true),
	PAREN_L			("(\\()",	true),
	PAREN_R			("(\\))",	true),
	BRACKET_L		("(\\[)",	true),
	BRACKET_R		("(\\])",	true),
	COMMA			("(,)",		true),
	SEMICOLON		("(;)",		true),
	
	FLOAT			("(\\d*+\\.\\d++)"),
	INT				("(\\d++)"),
	BOOLEAN			("(true|false)\\b"),
	STRING			("((\".*?[^\\\\]\")|(\"\"))"),
	
	DOT				("(\\.)",	true),
	
	UNARY_ASSIGN	("([\\+\\-\\!]{2})"),
	AUG_ASSIGN		("(\\+=|\\-=|\\*=|/=|%=|\\^=)"),
	OPERATOR1		("(\\^)"),
	OPERATOR2		("([\\*/%])"),
	OPERATOR3		("([\\+])"),
	MINUS			("(\\-)"),
	OPERATOR4		("(==|<=|>=|\\!=)"),
	LESS_THAN		("(<)"),
	GREATER_THAN	("(>)"),
	OPERATOR5		("([\\|&]{2})"),
	BOOL_UNARY		("(\\!)"),
	EQUALS			("(=)"),
	
	IDENTIFIER		("([a-zA-Z_]\\w*+)\\b"),
	
	CONSTANT;
	
	
	// Regex defining token type
	private final String regex;
	
	// Dispose token during parsing
	private final boolean dispose;
	
	private TokenType(){
		this(null, false);
	}
	
	private TokenType(String regex){
		this(regex, false);
	}
	
	private TokenType(String regex, boolean dispose){
		this.regex = regex;
		this.dispose = dispose;
	}
	
	public String regex(){
		return regex;
	}
	
	public boolean dispose(){
		return dispose;
	}
}
