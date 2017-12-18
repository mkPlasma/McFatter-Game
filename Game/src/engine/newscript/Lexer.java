package engine.newscript;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import engine.newscript.Token.TokenType;
import static engine.newscript.Token.TokenType.*;

public class Lexer extends CompilerUnit{
	
	private final Map<TokenType, String> lexmap;
	
	private ArrayList<Token> tokens;
	
	public Lexer(Compiler compiler){
		super(compiler);
		
		lexmap = new TreeMap<TokenType, String>();
		tokens = new ArrayList<Token>();
		
		initMap();
	}
	
	// Link token types to regex
	private void initMap(){
		lexmap.put(SET,				"(set)\\b");
		lexmap.put(CONST,			"(const)\\b");
		lexmap.put(GLOBAL,			"(global)\\b");
		lexmap.put(IF,				"(if)\\b");
		lexmap.put(ELSE,			"(else)\\b");
		lexmap.put(BREAK,			"(break)\\b");
		lexmap.put(WHILE,			"(while)\\b");
		lexmap.put(FOR,				"(for)\\b");
		lexmap.put(IN,				"(in)\\b");
		lexmap.put(FUNCTION,		"(function)\\b");
		lexmap.put(TASK,			"(task)\\b");
		lexmap.put(RETURN,			"(return)\\b");
		lexmap.put(WAIT,			"(wait)\\b");
		
		lexmap.put(IDENTIFIER, 		"([a-zA-Z_]\\w*+)\\b");
		
		lexmap.put(BRACE_L,			"(\\{)");
		lexmap.put(BRACE_R,			"(\\})");
		lexmap.put(PAREN_L,			"(\\()");
		lexmap.put(PAREN_R,			"(\\))");
		lexmap.put(BRACKET_L,		"(\\[)");
		lexmap.put(BRACKET_R,		"(\\])");
		lexmap.put(COMMA,			"(,)");
		lexmap.put(DOT,				"(\\.)");
		lexmap.put(SEMICOLON,		"(;)");
		
		lexmap.put(NUM_OP,			"([\\+\\-\\*/%\\^]{1})");
		lexmap.put(COMPARISON,		"([<>(==)(<=)(>=)(\\!=)])");
		lexmap.put(BOOL_OP,			"([\\|&]{2})");
		lexmap.put(BOOL_UNARY,		"(\\!)");
		lexmap.put(ASSIGNMENT,		"([=(\\+=)(-=)(\\*=)(/=)(%=)(\\^=)])");
		lexmap.put(UNARY_ASSIGN,	"([\\+\\-\\!]{2})");
		
		lexmap.put(INT,				"(\\d++)");
		lexmap.put(FLOAT,			"(\\d++\\.\\d++)");
		lexmap.put(TRUE,			"(true)\\b");
		lexmap.put(FALSE,			"(false)\\b");
		lexmap.put(STRING,			"((\".*?[^\\\\]\")|(\"\"))");
	}
	
	public void reset(){
		tokens.clear();
	}
	
	public void process(DScript script){
		
		String[] file = script.getFile();
		
		// Build tokens for each lien
		for(String line:file){
			
			if(line.isEmpty() || line.startsWith("$"))
				continue;
			
			buildTokens(line);
		}
	}
	
	private void buildTokens(String line){
		Pattern pattern;
		Matcher matcher;
		
		// Token found
		boolean found = false;
		boolean invalid;
		
		do{
			invalid = true;
			
			// Check each regex
			for(Map.Entry<TokenType, String> e:lexmap.entrySet()){
				pattern = Pattern.compile("^" + e.getValue() + ".*?");
				matcher = pattern.matcher(line);
				
				found = matcher.find();
				
				// If matches
				if(found){
					String token = matcher.group(1);
					
					// Cut token out of line
					line = line.substring(token.length()).trim();
					
					// Add token
					tokens.add(new Token(e.getKey(), token));
					
					invalid = false;
					break;
				}
			}
			
			// Token did not match any regex
			if(invalid)
				compiler.error("invalid token");
			
			// If line is empty, finish
			if(line.isEmpty())
				break;
			
		}while(found);
	}
}
