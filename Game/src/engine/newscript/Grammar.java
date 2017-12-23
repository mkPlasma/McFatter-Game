package engine.newscript;

import static engine.newscript.Token.TokenType.*;

public class Grammar{
	
	private final Rule[] rules;
	
	public Grammar(){
		rules = new Rule[]{
			
			new Rule("statement", new Object[][]{
				{"new_var", SEMICOLON},
				{"new_var_def"},
				{"assignment"},
				{"func_call", SEMICOLON},
			}),
			
			
			
			new Rule("new_var", new Object[][]{
				{SET, IDENTIFIER},
			}),
			
			new Rule("new_var_def", new Object[][]{
				{"new_var", EQUALS, "expression", SEMICOLON},
			}),
			
			new Rule("assignment", new Object[][]{
				{IDENTIFIER, EQUALS, "expression"},
				{IDENTIFIER, AUG_ASSIGN, "expression"},
				{IDENTIFIER, UNARY_ASSIGN},
			}),
			
			
			
			new Rule("func_call", new Object[][]{
				{IDENTIFIER, PAREN_L, PAREN_R},
				{IDENTIFIER, PAREN_L, "expression", PAREN_R},
				{IDENTIFIER, PAREN_L, "list", PAREN_R},
			}),
			
			
			new Rule("expression", new Object[][]{
				{IDENTIFIER},
				{"func_call"},
				{INT},
				{FLOAT},
				{BOOLEAN},
				{STRING},
				{"expression_p"},
				{"expression", OPERATOR, "expression"},
				{BOOL_UNARY, "expression"},
			}),
			
			new Rule("expression_p", new Object[][]{
				{PAREN_L, "expression", PAREN_R},
			}),
			
			
			new Rule("list", new Object[][]{
				{"expression", COMMA, "expression"},
				{"list", COMMA, "list"},
				{"list", COMMA, "expression"},
				{"expression", COMMA, "list"},
			}),
		};
	}
	
	public Rule[] getRules(){
		return rules;
	}
}
