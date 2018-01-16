package engine.newscript.parser;

import engine.newscript.lexer.Token;
import engine.newscript.lexer.TokenType;

import static engine.newscript.lexer.TokenType.*;

public class ParseUtil{
	
	public static final int
	T_NUM			= 0b001,
	T_BOOL			= 0b010,
	T_STRING		= 0b100,
	T_NUM_STRING	= T_NUM | T_STRING,
	T_ANY			= T_NUM | T_BOOL | T_STRING;
	
	
	public static Object getValue(Object o){
		
		if(!(o instanceof ParseUnit))
			return null;
		
		Object[] contents = ((ParseUnit)o).getContents();
		
		if(contents.length != 1)
			return null;
		
		// Parenthesized expression
		ParseUnit p = contents[0] instanceof ParseUnit ? (ParseUnit)contents[0] : null;
		ParseUnit p2 = p != null && p.getType().equals("expression_p") &&
				p.getContents()[0] instanceof ParseUnit ? (ParseUnit)p.getContents()[0] : null;
		
		boolean paren = p2 != null && p2.getContents().length == 1 && p2.getContents()[0] instanceof Token;
		
		if(!paren && !(contents[0] instanceof Token))
			return null;
		
		Token token = paren ? (Token)p2.getContents()[0] : (Token)contents[0];
		
		TokenType t = token.getType();
		String val = token.getValue();

		if(t == INT)		return Integer.parseInt(val);
		if(t == FLOAT)		return Float.parseFloat(val);
		if(t == BOOLEAN)	return Boolean.parseBoolean(val);
		if(t == STRING)		return val;
		
		return null;
	}
	
	public static int getReturnType(int t1, int t2, String op){
		
		int ot = getOperatorType(op);
		
		// Unary
		if(op.equals("!"))
			return t1 == T_BOOL ? T_BOOL : -1;
		
		// Equality comparison
		if(ot == T_ANY)
			return T_BOOL;
		
		// String concatenation
		if((t1 == T_STRING || t2 == T_STRING) && ot == T_NUM_STRING)
			return T_STRING;
		
		
		return (t1 == T_ANY || t2 == T_ANY || t1 == t2) && (ot & t1) > 0 ? getOperatorReturnType(op) : -1;
	}
	
	public static int getOperatorType(String op){
		switch(op){
			case "-": case "*": case "/": case "%": case "^":
			case "<": case ">": case "<=": case ">=":
				return T_NUM;
			
			case "||": case "&&": case "!":
				return T_BOOL;
			
			case "+":
				return T_NUM_STRING;
			
			case "==": case "!=":
				return T_ANY;
		}
		
		return -1;
	}
	
	public static int getOperatorReturnType(String op){
		switch(op){
			case "-": case "*": case "/": case "%": case "^":
				return T_NUM;
			
			case "||": case "&&": case "!":
			case "<": case ">": case "<=": case ">=":
			case "==": case "!=":
				return T_BOOL;
			
			case "+":
				return T_NUM_STRING;
		}
		
		return -1;
	}
}
