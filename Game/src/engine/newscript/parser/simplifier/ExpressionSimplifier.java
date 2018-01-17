package engine.newscript.parser.simplifier;

import static engine.newscript.lexer.TokenType.*;
import static engine.newscript.parser.ParseUtil.*;

import java.util.ArrayList;

import engine.newscript.DScript;
import engine.newscript.lexer.Token;
import engine.newscript.lexer.TokenType;
import engine.newscript.parser.ParseUnit;

public class ExpressionSimplifier{
	
	public void process(DScript script){
		
		ArrayList<Object> parseTree = script.getParseTree();
		
		for(Object o:parseTree)
			simplifyExpressions(((ParseUnit)o).getContents());
	}
	
	private void simplifyExpressions(Object[] contents){
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
	
	private void simplifyExpression(ParseUnit p){
		
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
		Object c1 = contents[0];
		Object c2 = contents[1];
		
		Object val = getValue(c2);
		
		// Simplify function calls, etc.
		if(val == null){
			if(c2 instanceof ParseUnit){
				ParseUnit p2 = (ParseUnit)c2;
				
				if(p2.getType().equals("expression"))
					simplifyExpression(p2);
				else
					simplifyExpressions(p2.getContents());
			}
		}
		
		val = getValue(c2);
		
		// Check if able to simplify, invert comparisons if possible
		if(val == null || !(c1 instanceof Token)){
			
			if(!(c2 instanceof ParseUnit))
				return;
			
			// Get parenthesized expression
			ParseUnit p2 = (ParseUnit)((ParseUnit)((ParseUnit)c2).getContents()[0]).getContents()[0];
			Object[] cont = p2.getContents();
			
			// Check length
			if(cont.length != 3)
				return;
			
			String op = "";
			TokenType type = OPERATOR4;
			
			Token t = (Token)cont[1];
			
			// Invert operator
			switch(t.getValue()){
				case "==":	op = "!=";	break;
				case "!=":	op = "==";	break;
				case "<":	op = ">=";	break;
				case ">":	op = "<=";	break;
				case "<=":	op = ">";	type = GREATER_THAN;	break;
				case ">=":	op = "<";	type = LESS_THAN;		break;
			}
			
			// Replace operator
			cont[1] = new Token(type, op, t.getFile(), t.getLineNum());
			
			// Replace expression
			((ParseUnit)c2).getParent().setContents(cont);
			return;
		}
		
		Token t = (Token)c1;
		p.setContents(operate(val, null, t));
		
		return;
	}
	
	private Object[] operate(Object o1, Object o2, Token op){
		
		// Unary operation (oly binary op is !)
		if(o2 == null)
			return new Object[]{new Token(BOOLEAN, Boolean.toString(!(Boolean)o1), op.getFile(), op.getLineNum())};
		
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