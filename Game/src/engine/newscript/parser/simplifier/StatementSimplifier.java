package engine.newscript.parser.simplifier;

import static engine.newscript.lexer.TokenType.*;
import static engine.newscript.parser.ParseUtil.*;

import java.util.ArrayList;

import engine.newscript.DScript;
import engine.newscript.lexer.Token;
import engine.newscript.parser.ParseUnit;

/**
 * 
 * Miscellaneous simplifier for converting until/for loops into while loops, waits into wait, etc.
 * 
 * @author Daniel
 * 
 */

public class StatementSimplifier{
	
	public void process(DScript script){
		
		ArrayList<Object> parseTree = script.getParseTree();
		
		for(Object o:parseTree)
			process(((ParseUnit)o));
	}
	
	private void process(ParseUnit p){
		
		simplify(p);
		
		Object[] contents = p.getContents();
		
		for(int i = 0; i < contents.length; i++){
			
			Object o = contents[i];
			
			if(!(o instanceof ParseUnit))
				continue;
			
			ParseUnit p2 = (ParseUnit)o;
			
			// Replace s_block and else_if_cond with its contents
			if(p2.getType().equals("s_block") || p2.getType().equals("else_if_cond")){
				contents[i] = p2.getContents()[0];
				p2 = (ParseUnit)contents[i];
				
				p.setContents(contents);
			}
			
			process(p2);
		}
	}
	
	private void simplify(ParseUnit p){
		switch(p.getType()){
				
			// Invert until loops into while loops
			case "until_block":
				replaceUntilLoop(p);
				break;
				
			case "wait_until":
				replaceWaitUntil(p);
				break;
				
			// Replace waits by wait
			case "waits":
				replaceWaitS(p);
				break;
				
			// Change for loops into while loops
			case "for_block":
				replaceForLoop(p);
				break;
				
			// Replace dot function calls
			case "dot_func_call":
				replaceDotFuncCall(p);
				break;
		}
	}
	
	private void replaceUntilLoop(ParseUnit p){
		
		Object[] contents = p.getContents();
		
		// Invert condition
		Object[] cont = ((ParseUnit)contents[0]).getContents();
		
		cont[0] = new ParseUnit("expression", new Object[]{
			
			new Token(BOOL_UNARY, "!", p.getFile(), p.getLineNum()),
			
			new ParseUnit("expression", new Object[]{
				new ParseUnit("expression_p", cont.clone())
			})
		});
		
		contents[0] = new ParseUnit("while_cond", cont);
		
		replaceParseUnit(p, new ParseUnit("while_block", contents));
	}
	
	private void replaceWaitUntil(ParseUnit p){
		
		Object[] contents = p.getContents();
		
		// Invert condition
		Object[] cont = ((ParseUnit)contents[1]).getContents();
		
		contents[1] = new ParseUnit("expression", new Object[]{
			
			new Token(BOOL_UNARY, "!", p.getFile(), p.getLineNum()),
			
			new ParseUnit("expression", new Object[]{
				new ParseUnit("expression_p", new Object[]{
					new ParseUnit("expression", cont.clone())
				})
			})
		});
		
		replaceParseUnit(p, new ParseUnit("wait_while", contents));
	}
	
	private void replaceWaitS(ParseUnit p){
		
		Object[] contents = p.getContents();
		
		// Single second
		if(contents.length == 1){
			replaceParseUnit(p, new ParseUnit("wait", new Object[]{
				new Token(WAIT, "wait", p.getFile(), p.getLineNum()),
				new ParseUnit("expression", new Object[]{new Token(INT, "60", p.getFile(), p.getLineNum())})
			}));
			return;
		}
		
		// Expression
		replaceParseUnit(p, new ParseUnit("wait", new Object[]{
			new Token(WAIT, "wait", p.getFile(), p.getLineNum()),
			new ParseUnit("expression", new Object[]{
				contents[1],
				new Token(OPERATOR2, "*", p.getFile(), p.getLineNum()),
				new ParseUnit("expression", new Object[]{new Token(INT, "60", p.getFile(), p.getLineNum())})
			}),
		}));
	}
	
	private void replaceForLoop(ParseUnit p){
		
		Object[] contents = p.getContents();
		
		Object[] forPre = ((ParseUnit)((ParseUnit)contents[0]).getContents()[0]).getContents();
		
		// Variable
		Token id = (Token)forPre[0];
		
		// Expressions
		Object[] list = ((ParseUnit)((ParseUnit)contents[0]).getContents()[1]).getContents();
		int len = list.length;
		
		Object exp1 = list[0];
		ParseUnit exp2 = len >= 2 ? (ParseUnit)list[1] : null;
		ParseUnit exp3 = len >= 3 ? (ParseUnit)list[2] : null;
		
		
		// Replace condition
		Object exp = len == 1 ? exp1 : len == 2 ? exp2 : exp3;
		
		contents[0] = new ParseUnit("while_cond", new Object[]{
			new ParseUnit("expression", new Object[]{
				new ParseUnit("expression", new Object[]{id}),
				forPre[1],
				
				(len == 1 ?
					new ParseUnit("expression", new Object[]{exp}) :
					exp),
			})
		});
		
		
		// Add increment to block
		
		// Get statements
		boolean isBlock = ((ParseUnit)contents[1]).getType().equals("block");
		ParseUnit st = isBlock ? (ParseUnit)((ParseUnit)contents[1]).getContents()[0] : (ParseUnit)contents[1];
		Object[] stCont = st.getContents();
		
		Object[] cont = new Object[stCont.length + 1];
		System.arraycopy(stCont, 0, cont, 0, stCont.length);
		
		// Add statement
		if(!isBlock)
			cont[0] = new ParseUnit("statement", new Object[]{cont[0]});
		
		exp = len == 1 ? id : exp2;
		
		cont[cont.length - 1] = new ParseUnit("statement", new Object[]{
			len == 3 ?
				
				// List length 3
				new ParseUnit("assign", new Object[]{
					id,
					new Token(AUG_ASSIGN, "+=", exp2.getFile(), exp2.getLineNum()),
					exp2
				}) :
				
				// List length 1 or 2
				new ParseUnit("assign_u", new Object[]{
					id,
					new Token(UNARY_ASSIGN, "++",
						(exp instanceof Token ? ((Token)exp).getFile() : ((ParseUnit)exp).getFile()),
						(exp instanceof Token ? ((Token)exp).getLineNum() : ((ParseUnit)exp).getLineNum())
					)
				})
		});
		
		// Replace statements
		if(isBlock)
			replaceParseUnit(st, new ParseUnit("statements", cont));
		else
			replaceParseUnit(st, new ParseUnit("block", new Object[]{new ParseUnit("statements", cont)}));
		
		
		// Create new block
		cont = new Object[]{new ParseUnit("statements", new Object[]{
			
			new ParseUnit("statement", new Object[]{
				len >= 2 ?
				// List length 2 or 3
				new ParseUnit("new_var_def", new Object[]{
					new ParseUnit("new_var", new Object[]{id}),
					new Token(EQUALS, "=",
						(exp1 instanceof Token ? ((Token)exp1).getFile() : ((ParseUnit)exp1).getFile()),
						(exp1 instanceof Token ? ((Token)exp1).getLineNum() : ((ParseUnit)exp1).getLineNum())),
					exp1
				}) :
				
				// List Length 1
				new ParseUnit("new_var", new Object[]{id})
			}),
			
			new ParseUnit("while_block", contents)
		})};
		
		// Replace for_block
		replaceParseUnit(p, new ParseUnit("block", cont));
	}
	
	private void replaceDotFuncCall(ParseUnit p){
		
		Object[] contents = p.getContents();
		
		// func_call
		ParseUnit p2 = (ParseUnit)contents[1];
		Object[] cont = p2.getContents();
		
		// No parameters originally
		if(cont.length == 1){
			
			// Copy function name
			Object[] newCont = new Object[2];
			newCont[0] = cont[0];
			
			// Add parameter
			newCont[1] = contents[0];
			
			// Replace dot_func_call
			replaceParseUnit(p, new ParseUnit(p2.getType(), newCont));
		}
		
		// Single parameter
		else if(cont.length == 2){
			
			// Copy function name
			Object[] newCont = new Object[2];
			newCont[0] = cont[0];
			
			// Add parameter list
			newCont[1] = new ParseUnit("list", new Object[]{
				contents[0],
				cont[1]
			});
			
			// Replace dot_func_call
			replaceParseUnit(p, new ParseUnit(p2.getType(), newCont));
		}
		
		// Parameter list
		else{
			// List
			ParseUnit list = (ParseUnit)cont[1];
			
			// Create new params
			Object[] params = list.getContents();
			Object[] newParams = new Object[params.length + 1];
			
			// Copy parameters
			System.arraycopy(params, 0, newParams, 1, params.length);
			
			// Add expression
			newParams[0] = contents[0];
			
			// Replace dot_func_call
			replaceParseUnit(p, p2);
		}
	}
}
