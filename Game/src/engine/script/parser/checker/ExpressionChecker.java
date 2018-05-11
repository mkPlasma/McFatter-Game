package engine.script.parser.checker;

import static engine.script.parser.ParseUtil.*;
import static engine.script.lexer.TokenType.*;

import java.util.ArrayList;

import engine.script.DScript;
import engine.script.ScriptException;
import engine.script.lexer.Token;
import engine.script.parser.ParseUnit;

/**
 * 
 * Checks for valid expressions.
 * 
 * @author Daniel
 * 
 */

public class ExpressionChecker{
	
	private ArrayList<Object> parseTree;
	
	public void process(DScript script) throws ScriptException{
		parseTree = script.getParseTree();
		checkExpressions();
	}
	
	private void checkExpressions() throws ScriptException{
		for(Object o:parseTree)
			checkExpressions((ParseUnit)o);
	}
	
	private void checkExpressions(ParseUnit p) throws ScriptException{
		
		Object[] contents = p.getContents();
		
		for(Object o:contents){
			
			if(!(o instanceof ParseUnit))
				continue;
			
			ParseUnit p2 = (ParseUnit)o;
			
			if(p2.getType().equals("expression")){
				int t = checkExpression(p2);
				
				// Check required types
				switch(p.getType()){
					case "if_cond": case "if_else_cond": case "while_cond": case "until_cond": case "returnif": case "wait_while": case "wait_until":
						
						if((t & T_BOOL) <= 0)
							throw new ScriptException("Expression must output boolean", p.getFile(), p.getLineNum());
						
						break;
						
					case "wait": case "waits":
						
						if((t & T_NUM) <= 0)
							throw new ScriptException("Expression must output number", p.getFile(), p.getLineNum());
						
						break;
						
						
					case "list":
						
						if(!p.getParent().getType().equals("for_cond"))
							break;
						
						if((t & T_NUM) <= 0)
							throw new ScriptException("Expression must output number", p.getFile(), p.getLineNum());
						
						break;
						
						
					case "const_var_def":
						
						if(t == T_ANY)
							throw new ScriptException("Constant variable must be defined in terms of literals only", p.getFile(), p.getLineNum());
						
						break;
				}
			}
			else
				checkExpressions(p2);
		}
	}
	
	private int checkExpression(ParseUnit p) throws ScriptException{
		
		Object[] contents = p.getContents();
		
		if(contents.length == 1){
			
			int t = getValueType(getValue(p));
			
			// Single value
			if(t != -1)
				return t;
			
			// Parenthesized expression
			Object o = contents[0];
			
			if(o instanceof ParseUnit && ((ParseUnit)o).getType().equals("expression_p"))
				t = checkExpression((ParseUnit)((ParseUnit)o).getContents()[0]);
			
			if(t != -1)
				return t;
			
			// Single non-value
			checkExpressions(p);
			return T_ANY;
		}
		
		// Binary operator
		if(contents.length == 3){
			
			Object c1 = contents[0];
			Object c2 = contents[1];
			Object c3 = contents[2];
			
			Object val1 = getValue(c1);
			Object val2 = getValue(c3);
			
			int t1 = getValueType(val1);
			int t2 = getValueType(val2);
			
			// Simplify function calls, etc.
			if(val1 == null){
				if(c1 instanceof ParseUnit){
					ParseUnit p1 = (ParseUnit)c1;
					
					if(p1.getType().equals("expression"))
						t1 = checkExpression(p1);
				}
			}
			if(val2 == null){
				if(c3 instanceof ParseUnit){
					ParseUnit p3 = (ParseUnit)c3;
					
					if(p3.getType().equals("expression"))
						t2 = checkExpression(p3);
				}
			}
			
			if(!(c2 instanceof Token))
				return -1;
			
			
			Token tk = (Token)c2;
			
			// Variable/function call
			if(t1 == -1 || t2 == -1)
				return getOperatorReturnType(tk.getValue());
			
			int rt = getReturnType(t1, t2, tk.getValue());
			
			if(rt == -1)
				throw new ScriptException("Type mismatch", tk.getFile(), tk.getLineNum());
			
			return rt;
		}
		
		// Unary operator
		Object c1 = contents[0];
		Object c2 = contents[1];
		
		Object val = getValue(c2);

		int t = getValueType(val);
		
		// Simplify function calls, etc.
		if(val == null){
			if(c1 instanceof ParseUnit){
				ParseUnit p2 = (ParseUnit)c2;
				
				if(p2.getType().equals("expression"))
					t = checkExpression(p2);
			}
		}
		
		if(!(c1 instanceof Token))
			return -1;
		
		Token tk = (Token)c1;
		
		// Variable/function call
		if(t == -1)
			return tk.getType() == BOOL_UNARY ? T_BOOL : T_NUM;
		
		int rt = getReturnType(t, -1, tk.getValue());
		
		if(rt == -1)
			throw new ScriptException("Type mismatch", tk.getFile(), tk.getLineNum());
		
		return rt;
	}
	
	private int getValueType(Object v){
		return v == null ? -1 : v instanceof Integer || v instanceof Float ? T_NUM :
			v instanceof Boolean ? T_BOOL : v instanceof String ? T_STRING : -1;
	}
}
