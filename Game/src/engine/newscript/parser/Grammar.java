package engine.newscript.parser;

import static engine.newscript.lexer.TokenType.*;

public class Grammar{
	
	private static final Object CONCAT = null;
	private static final boolean FINAL = true;
	
	private final Rule[] rules;
	private final Rule[] finalValid;
	
	public Grammar(){
		rules = new Rule[]{
			
			// Define parts of blocks
			new Rule("func_name_def", new Object[][]{
				{FUNCTION, IDENTIFIER},
			}),
			
			new Rule("func_def", new Object[][]{
				{"func_name_def", PAREN_L, PAREN_R},
				{"func_name_def", PAREN_L, "list", PAREN_R},
			}),
			
			new Rule("task_name_def", new Object[][]{
				{TASK, IDENTIFIER},
			}),
			
			new Rule("task_def", new Object[][]{
				{"task_name_def", PAREN_L, PAREN_R},
				{"task_name_def", PAREN_L, "list", PAREN_R},
			}),
			
			new Rule("if_cond", new Object[][]{
				{IF, PAREN_L, "expression", PAREN_R},
			}),
			
			new Rule("if_else_cond", new Object[][]{
				{IF, ELSE, PAREN_L, "expression", PAREN_R},
			}),
			
			new Rule("while_cond", new Object[][]{
				{WHILE, PAREN_L, "expression", PAREN_R},
			}),
			
			new Rule("until_cond", new Object[][]{
				{UNTIL, PAREN_L, "expression", PAREN_R},
			}),
			
			new Rule("for_cond", new Object[][]{
				{FOR, PAREN_L, IDENTIFIER, IN, "expression", PAREN_R},
				{FOR, PAREN_L, IDENTIFIER, IN, "list", PAREN_R},
			}),
			

			// Prioritize certain statements above expression
			new Rule("func_call", new Object[][]{
				{IDENTIFIER, PAREN_L, PAREN_R},
				{IDENTIFIER, PAREN_L, "expression", PAREN_R},
				{IDENTIFIER, PAREN_L, "list", PAREN_R},
			}),
			
			new Rule("dot_func_call", new Object[][]{
				{IDENTIFIER, DOT, "func_call"},
			}),
			
			new Rule("statement", new Object[][]{
				{"func_call", SEMICOLON},
				{"dot_func_call", SEMICOLON},
			}),
			
			new Rule("assignment", new Object[][]{
				{"array_elem", EQUALS, "expression"},
				{"array_elem", AUG_ASSIGN, "expression"},
				{"array_elem", UNARY_ASSIGN},
			}),
			
			
			// Values
			new Rule("expression_p", new Object[][]{
				{PAREN_L, "expression", PAREN_R},
			}),
			
			new Rule("list", new Object[][]{
				{"expression", COMMA, "expression"},
				{"list", COMMA, "list", CONCAT},
				{"list", COMMA, "expression", CONCAT},
				{"expression", COMMA, "list", CONCAT},
			}),
			
			new Rule("array", new Object[][]{
				{BRACE_L, BRACE_R},
				{BRACE_L, "expression", BRACE_R},
				{BRACE_L, IDENTIFIER, BRACE_R},
				{BRACE_L, "list", BRACE_R},
			}),
			
			new Rule("array_elem", new Object[][]{
				{IDENTIFIER, BRACKET_L, "expression", BRACKET_R},
				{"array", BRACKET_L, "expression", BRACKET_R},
			}),
			
			
			// Statements
			new Rule("new_var", new Object[][]{
				{SET, IDENTIFIER},
			}),
			
			new Rule("new_const_var", new Object[][]{
				{SET, CONST, IDENTIFIER},
			}),
			
			new Rule("new_var_def", new Object[][]{
				{"new_var", EQUALS, "expression"},
			}),
			
			new Rule("const_var_def", new Object[][]{
				{"new_const_var", EQUALS, "expression"},
			}),
			
			new Rule("assignment", new Object[][]{
				{IDENTIFIER, EQUALS, "expression"},
				{IDENTIFIER, AUG_ASSIGN, "expression"},
				{IDENTIFIER, UNARY_ASSIGN},
			}),
			
			new Rule("break", new Object[][]{
				{BREAK},
			}),
			
			new Rule("return", new Object[][]{
				{RETURN, "expression"},
				{RETURN},
			}),
			
			new Rule("wait", new Object[][]{
				{WAIT, "expression"},
				{WAIT},
			}),
			
			new Rule("waits", new Object[][]{
				{WAITS, "expression"},
				{WAITS},
			}),
			
			new Rule("wait_while", new Object[][]{
				{WAIT, WHILE, "expression"},
			}),
			
			new Rule("wait_until", new Object[][]{
				{WAIT, UNTIL, "expression"},
			}),
			
			
			// Expressions
			new Rule("expression", new Object[][]{
				{INT},
				{FLOAT},
				{BOOLEAN},
				{STRING},
				{IDENTIFIER},
				{"func_call"},
				{"dot_func_call"},
				{"array"},
				{"array_elem"},
				{"expression_p"},
				
				// Operator precedence
				{BOOL_UNARY, "expression"},
				{"expression", OPERATOR1, "expression"},
				{"expression", OPERATOR2, "expression"},
				{"expression", OPERATOR3, "expression"},
				{"expression", OPERATOR4, "expression"},
				{"expression", OPERATOR5, "expression"},
			}),
			
			
			// Finals
			new Rule("statement", new Object[][]{
				{"new_var",			SEMICOLON},
				{"new_var_def", 	SEMICOLON},
				{"const_var_def",	SEMICOLON},
				{"assignment",		SEMICOLON},
				{"break",			SEMICOLON},
				{"return",			SEMICOLON},
				{"wait",			SEMICOLON},
				{"waits",			SEMICOLON},
				{"wait_while",		SEMICOLON},
				{"wait_until",		SEMICOLON},
			}),
			
			new Rule("block", FINAL, new Object[][]{
				{BRACE_L, "statements", BRACE_R},
			}),
			
			new Rule("s_block", FINAL, new Object[][]{
				{"func_block"},
				{"task_block"},
				{"if_block"},
				{"if_else_block"},
				{"else_block"},
				{"while_block"},
				{"until_block"},
				{"for_block"},
			}),
			
			
			// Block types
			new Rule("func_block", FINAL, new Object[][]{
				{"func_def", "statement"},
				{"func_def", "block"},
			}),
			
			new Rule("task_block", FINAL, new Object[][]{
				{"task_def", "statement"},
				{"task_def", "block"},
			}),
			
			new Rule("if_block", FINAL, new Object[][]{
				{"if_cond", "statement"},
				{"if_cond", "block"},
			}),
			
			new Rule("if_else_block", FINAL, new Object[][]{
				{"if_cond", "statement"},
				{"if_cond", "block"},
			}),
			
			new Rule("else_block", FINAL, new Object[][]{
				{ELSE, "statement"},
				{ELSE, "block"},
			}),
			
			new Rule("while_block", FINAL, new Object[][]{
				{"while_cond", "statement"},
				{"while_cond", "block"},
				{"while_cond", SEMICOLON},
			}),
			
			new Rule("until_block", FINAL, new Object[][]{
				{"until_cond", "statement"},
				{"until_cond", "block"},
				{"until_cond", SEMICOLON},
			}),
			
			new Rule("for_block", FINAL, new Object[][]{
				{"for_cond", "statement"},
				{"for_cond", "block"},
			}),
			
			// Statements below control blocks to allow single-statement blocks
			new Rule("statements", FINAL, new Object[][]{
				{"statement"},
				{"s_block"},
				{"statements", "statements", CONCAT},
			}),
		};
		
		
		int n = 0;
		
		for(Rule r:rules)
			if(r.isFinalValid())
				n++;
		
		finalValid = new Rule[n];
		n = 0;
		
		for(Rule r:rules)
			if(r.isFinalValid())
				finalValid[n++] = r;
	}
	
	public Rule[] getRules(){
		return rules;
	}
	
	public Rule[] getFinalValid(){
		return finalValid;
	}
}
