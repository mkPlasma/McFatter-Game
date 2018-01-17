package engine.newscript.parser.simplifier;

import static engine.newscript.lexer.TokenType.*;

import java.util.ArrayList;

import engine.newscript.DScript;
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
		
		for(Object o:contents){
			
			if(!(o instanceof ParseUnit))
				continue;
			
			process((ParseUnit)o);
		}
	}
	
	private void simplify(ParseUnit p){
		
		Object[] contents = p.getContents();
		
		switch(p.getType()){
			
			// Invert until loops into while loops
			case "until_block":
				
				// Invert condition
				Object[] cont = ((ParseUnit)contents[0]).getContents();
				
				cont[0] = new ParseUnit("expression", new Object[]{
					
					new Token(BOOL_UNARY, "!", p.getFile(), p.getLineNum()),
					
					new ParseUnit("expression", new Object[]{
						new ParseUnit("expression_p", cont.clone())
					})
				});
				
				contents[0] = new ParseUnit("while_cond", cont);
				p = new ParseUnit("while_block", contents);
				
				break;
				
			// Invert until loops into while loops
			case "wait_until":
				
				// Invert condition
				cont = ((ParseUnit)contents[1]).getContents();
				
				contents[1] = new ParseUnit("expression", new Object[]{
					
					new Token(BOOL_UNARY, "!", p.getFile(), p.getLineNum()),
					
					new ParseUnit("expression", new Object[]{
						new ParseUnit("expression_p", cont.clone())
					})
				});
				
				p = new ParseUnit("wait_while", contents);
				break;
		}
	}
}
