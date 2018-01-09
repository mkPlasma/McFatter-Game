package engine.newscript.parser;

import static engine.newscript.lexer.TokenType.*;
import static engine.newscript.parser.ParseUtil.*;

import java.util.ArrayList;

import engine.newscript.DScript;
import engine.newscript.ScriptException;
import engine.newscript.lexer.Token;

public class ParseTreeSimplifier{
	
	private ArrayList<Object> parseTree;
	
	public void process(DScript script) throws ScriptException{
		
		parseTree = script.getParseTree();
		
		simplifyExpressions();
	}
	
	private void simplifyExpressions() throws ScriptException{
		for(Object o:parseTree)
			simplifyExpressions(((ParseUnit)o).getContents());
	}
	
	private void simplifyExpressions(Object[] contents) throws ScriptException{
		for(Object o:contents){
			
			if(!(o instanceof ParseUnit))
				continue;
			
			ParseUnit p = (ParseUnit)o;
			
			if(p.getType().equals("expression"))
				simplifyExpression(p);
			else
				simplifyExpressions(p.getContents());
		}
	}
	
	private void simplifyExpression(ParseUnit p) throws ScriptException{
		
		Object[] contents = p.getContents();
		
		// Single non-value
		if(contents.length == 1){
			simplifyExpressions(contents);
			return;
		}
		
		// Binary operator
		if(contents.length == 3){
			
			Object c1 = contents[0];
			Object c2 = contents[1];
			Object c3 = contents[2];
			
			Object val1 = getValue(c1);
			Object val2 = getValue(c3);
			
			// Simplify function calls, etc.
			if(val1 == null){
				if(c1 instanceof ParseUnit){
					ParseUnit p1 = (ParseUnit)c1;
					
					if(p1.getType().equals("expression"))
						simplifyExpression(p1);
					else
						simplifyExpressions(p1.getContents());
				}
			}
			if(val2 == null){
				if(c3 instanceof ParseUnit){
					ParseUnit p3 = (ParseUnit)c3;
					
					if(p3.getType().equals("expression"))
						simplifyExpression(p3);
					else
						simplifyExpressions(p3.getContents());
				}
			}
			
			val1 = getValue(c1);
			val2 = getValue(c3);
			
			// Check if able to simplify
			if(val1 == null || val2 == null || !(c2 instanceof Token))
				return;
			
			Token t = (Token)c2;
			p.setContents(operate(val1, val2, t));
			
			return;
		}
		
		// Unary operator
	}
	
	private Object[] operate(Object o1, Object o2, Token op) throws ScriptException{
		
		// Number operation
		if(o1 instanceof Integer || o1 instanceof Float){

			float n1 = o1 instanceof Float ? (float)o1 : (float)(int)o1;
			float n2 = o2 instanceof Float ? (float)o2 : (float)(int)o2;
			
			Object r = 0;
			
			switch(op.getValue()){
				case "+":	r = n1 + n2;	break;
				case "-":	r = n1 - n2;	break;
				case "*":	r = n1 * n2;	break;
				case "/":	r = n1 / n2;	break;
				case "%":	r = n1 % n2;	break;
				case "^":	r = (float)Math.pow(n1, n2);	break;
				case "<":	r = n1 < n2;	break;
				case ">":	r = n1 > n2;	break;
				case "==":	r = n1 == n2;	break;
				case "<=":	r = n1 <= n2;	break;
				case ">=":	r = n1 >= n2;	break;
				case "!=":	r = n1 != n2;	break;
			}
			
			String file = op.getFile();
			int lineNum = op.getLineNum();
			
			// Comparison operation
			if(r instanceof Boolean)
				return new Object[]{new Token(BOOLEAN, r.toString(), file, lineNum)};
			
			float rf = (Float)r;
			
			// Check type
			if(rf == (int)rf)
				return new Object[]{new Token(INT, Integer.toString((int)rf), file, lineNum)};
			
			return new Object[]{new Token(FLOAT, Float.toString(rf), file, lineNum)};
		}
		
		return null;
	}
}
