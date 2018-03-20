package engine.script.parser;

import static engine.script.lexer.TokenType.*;

import engine.script.ScriptException;
import engine.script.lexer.Token;
import engine.script.lexer.TokenType;

/**
 * 
 * Static methods for various parse unit tasks.
 * 
 * @author Daniel
 * 
 */

public class ParseUtil{
	
	public static final int
		T_NUM			= 0b0001,
		T_BOOL			= 0b0010,
		T_STRING		= 0b0100,
		T_ARRAY		= 0b1000,
		T_NUM_STRING	= T_NUM | T_STRING,
		T_ANY			= T_NUM | T_BOOL | T_STRING | T_ARRAY;
	
	
	public static void replaceParseUnit(ParseUnit a, Object b){
		
		ParseUnit p = a.getParent();
		Object[] cont = p.getContents();
		
		for(int i = 0; i < cont.length; i++){
			if(cont[i] == a){
				cont[i] = b;
				break;
			}
		}
		
		p.setContents(cont);
	}
	
	public static void removeParseUnit(ParseUnit p) throws ScriptException{
		
		ParseUnit parent = p.getParent();
		
		if(parent == null)
			throw new ScriptException("Empty script after removing const variable", p.getFile(), p.getLineNum());
		
		Object[] pCont = parent.getContents();
		
		// Remove parent if empty
		if(pCont.length == 1){
			removeParseUnit(parent);
			return;
		}
		
		// Contents after removing
		Object[] cont = new Object[pCont.length - 1];
		
		int index = 0;
		
		for(int i = 0; i < cont.length; i++){
			if(pCont[i] == p){
				index = i;
				break;
			}
		}
		
		// Copy values
		System.arraycopy(pCont, 0, cont, 0, index);
		System.arraycopy(pCont, index + 1, cont, index, pCont.length - index - 1);
		
		// Set new contents
		parent.setContents(cont);
	}
	
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
		
		// Array
		if(p != null && p.getType().equals("array"))
			return p;
		if(p != null && paren && p2.getType().equals("array"))
			return p2;
		
		// Invalid
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
		
		// String concatenation
		if((t1 == T_STRING || t2 == T_STRING) && ot == T_NUM_STRING)
			return T_STRING;
		
		// Array concatenation
		if(op.equals("~"))
			return T_ARRAY;
		
		return (t1 == T_ANY || t2 == T_ANY || (t1 == T_NUM_STRING && t2 == T_NUM) || (t1 == T_NUM && t2 == T_NUM_STRING) || t1 == t2) && (ot & t1) > 0 && (ot & t2) > 0 ? getOperatorReturnType(op) : -1;
	}
	
	// Returns what operator takes
	public static int getOperatorType(String op){
		switch(op){
			case "-": case "*": case "/": case "%": case "^":
			case "<": case ">": case "<=": case ">=":
				return T_NUM;
			
			case "||": case "&&": case "!":
				return T_BOOL;
			
			case "+":
				return T_NUM_STRING;
				
			case "==": case "!=": case "~":
				return T_ANY;
		}
		
		return -1;
	}
	
	// Returns what operator returns
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
				
			case "~":
				return T_ARRAY;
		}
		
		return -1;
	}
}
