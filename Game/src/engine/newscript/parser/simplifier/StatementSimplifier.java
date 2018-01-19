package engine.newscript.parser.simplifier;

import static engine.newscript.lexer.TokenType.*;
import static engine.newscript.parser.ParseUtil.*;

import java.util.ArrayList;

import engine.newscript.DScript;
import engine.newscript.ScriptPrinter;
import engine.newscript.lexer.Token;
import engine.newscript.parser.ParseUnit;

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
			
			process((ParseUnit)o);
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
				
			// Change for loops into while loops
			case "for_block":
				replaceForLoop(p);
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
	
	private void replaceForLoop(ParseUnit p){
		
		Object[] contents = p.getContents();
		
		// Variable
		Token id = (Token)((ParseUnit)contents[0]).getContents()[0];
		
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
				new Token(LESS_THAN, "<",
					(exp instanceof Token ? ((Token)exp).getFile() : ((ParseUnit)exp).getFile()),
					(exp instanceof Token ? ((Token)exp).getLineNum() : ((ParseUnit)exp).getLineNum())),
				new ParseUnit("expression", new Object[]{exp})
			})
		});
		
		
		// Add increment to block
		
		// Get statements
		ParseUnit st = (ParseUnit)((ParseUnit)contents[1]).getContents()[0];
		Object[] stCont = st.getContents();
		
		Object[] cont = new Object[stCont.length + 1];
		System.arraycopy(stCont, 0, cont, 0, stCont.length);
		
		exp = len == 1 ? id : exp2;
		
		cont[cont.length - 1] = new ParseUnit("statement", new Object[]{
			len == 3 ?
				
				// List length 3
				new ParseUnit("assignment", new Object[]{
					id,
					new Token(AUG_ASSIGN, "+=", exp2.getFile(), exp2.getLineNum()),
					exp2
				}) :
				
				// List length 1 or 2
				new ParseUnit("assignment", new Object[]{
					id,
					new Token(UNARY_ASSIGN, "++",
						(exp instanceof Token ? ((Token)exp).getFile() : ((ParseUnit)exp).getFile()),
						(exp instanceof Token ? ((Token)exp).getLineNum() : ((ParseUnit)exp).getLineNum())
					)
				})
		});
		
		// Replace statements
		replaceParseUnit(st, new ParseUnit("statements", cont));
		
		
		// Create new block
		cont = new Object[]{new ParseUnit("statements", new Object[]{
			
			new ParseUnit("statement", new Object[]{
				len > 2 ?
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
			
			new ParseUnit("s_block", new Object[]{
				new ParseUnit("while_block", contents)
			})
		})};
		
		// Replace for_block
		replaceParseUnit(p, new ParseUnit("block", cont));
	}
}
