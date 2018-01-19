package engine.newscript.parser;

import static engine.newscript.lexer.TokenType.*;

public class Grammar{
	
	private static final Object CONCAT = null;
	private static final boolean FINAL = true;
	
	private final Rule[] rules;
	private final Rule[] finalValid;
	private final Object[][] replacements;
	
	public Grammar(){
		rules = new Rule[]{
			
			// Define parts of blocks
			new Rule("func_def", new Object[][]{
				{FUNCTION, IDENTIFIER, PAREN_L, PAREN_R},
				{FUNCTION, IDENTIFIER, PAREN_L, IDENTIFIER, PAREN_R},
				{FUNCTION, IDENTIFIER, PAREN_L, "list", PAREN_R},
			}),
			
			new Rule("task_def", new Object[][]{
				{TASK, IDENTIFIER, PAREN_L, PAREN_R},
				{TASK, IDENTIFIER, PAREN_L, IDENTIFIER, PAREN_R},
				{TASK, IDENTIFIER, PAREN_L, "list", PAREN_R},
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
			
			new Rule("func_call", new Object[][]{
				{IDENTIFIER, PAREN_L, PAREN_R},
				{IDENTIFIER, PAREN_L, "expression", PAREN_R},
				{IDENTIFIER, PAREN_L, "list", PAREN_R},
			}),
			
			new Rule("func_call_scope", new Object[][]{
				{"id_scope", PAREN_L, PAREN_R},
				{"id_scope", PAREN_L, "expression", PAREN_R},
				{"id_scope", PAREN_L, "list", PAREN_R},
			}),
			
			new Rule("dot_func_call", new Object[][]{
				{IDENTIFIER, DOT, "func_call"},
			}),
			
			
			// Values
			new Rule("expression_p", new Object[][]{
				{PAREN_L, "expression", PAREN_R},
			}),
			
			new Rule("id_scope", new Object[][]{
				{IDENTIFIER, LESS_THAN, INT, GREATER_THAN},
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
			
			new Rule("conditional", new Object[][]{
				{"expression", MINUS, GREATER_THAN, "expression"},
				{"expression", MINUS, GREATER_THAN, "list"},
			}),
			
			
			// Expressions
			new Rule("expression", new Object[][]{
				{INT},
				{FLOAT},
				{BOOLEAN},
				{STRING},
				{"expression_p"},
				{"array"},
				{"conditional"},
				
				// Operator precedence
				{BOOL_UNARY, "expression"},
				{"expression", OPERATOR1, "expression"},
				{"expression", OPERATOR2, "expression"},
				{"expression", OPERATOR3, "expression"},
				{"expression", MINUS, "expression"},
				{"expression", OPERATOR4, "expression"},
				{"expression", LESS_THAN, "expression"},
				{"expression", GREATER_THAN, "expression"},
				{"expression", OPERATOR5, "expression"},
				{MINUS, "expression"},
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
				{"array_elem", EQUALS, "expression"},
				{"array_elem", AUG_ASSIGN, "expression"},
				{"array_elem", UNARY_ASSIGN},
				{"id_scope", EQUALS, "expression"},
				{"id_scope", AUG_ASSIGN, "expression"},
				{"id_scope", UNARY_ASSIGN},
			}),
			
			new Rule("break", new Object[][]{
				{BREAK},
			}),
			
			new Rule("return", new Object[][]{
				{RETURN, "expression"},
				{RETURN},
			}),
			
			new Rule("returnif", new Object[][]{
				{RETURNIF, "expression"},
			}),
			
			new Rule("wait_while", new Object[][]{
				{WAIT, WHILE, "expression"},
			}),
			
			new Rule("wait_until", new Object[][]{
				{WAIT, UNTIL, "expression"},
			}),
			
			new Rule("wait", new Object[][]{
				{WAIT, "expression"},
				{WAIT},
			}),
			
			new Rule("waits", new Object[][]{
				{WAITS, "expression"},
				{WAITS},
			}),
			
			
			// Finals
			new Rule("statement", new Object[][]{
				{"new_var",			SEMICOLON},
				{"new_var_def", 	SEMICOLON},
				{"const_var_def",	SEMICOLON},
				{"assignment",		SEMICOLON},
				{"func_call",		SEMICOLON},
				{"dot_func_call",	SEMICOLON},
				{"func_call_scope",	SEMICOLON},
				{"break",			SEMICOLON},
				{"return",			SEMICOLON},
				{"returnif",		SEMICOLON},
				{"wait",			SEMICOLON},
				{"waits",			SEMICOLON},
				{"wait_while",		SEMICOLON},
				{"wait_until",		SEMICOLON},
			}),
			
			new Rule("block", FINAL, new Object[][]{
				{BRACE_L, "statements", BRACE_R},
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
			
			new Rule("s_block", FINAL, new Object[][]{
				{"func_block"},
				{"task_block"},
				{"if_block"},
				{"if_else_block"},
				{"else_block"},
				{"while_block"},
				{"until_block"},
				{"for_block"},
				{"block"},
			}),
			
			// Statements below control blocks to allow single-statement blocks
			new Rule("statements", FINAL, new Object[][]{
				{"statement"},
				{"s_block"},
				{"statements", "statements", CONCAT},
			}),
		};
		
		
		// Add final rules
		int n = 0;
		
		for(Rule r:rules)
			if(r.isFinalValid())
				n++;
		
		finalValid = new Rule[n];
		n = 0;
		
		for(Rule r:rules)
			if(r.isFinalValid())
				finalValid[n++] = r;
		
		
		// Add replacements
		// Identifier may be used in place of expression unit
		replacements = new Object[][]{
			{IDENTIFIER,		"expression"},
			{"func_call",		"expression"},
			{"func_call_scope",	"expression"},
			{"dot_func_call",	"expression"},
			{"array_elem",		"expression"},
			{"id_scope",		"expression"},
		};
	}
	
	public Rule[] getRules(){
		return rules;
	}
	
	public Rule[] getFinalValid(){
		return finalValid;
	}
	
	public Object[][] getReplacements(){
		return replacements;
	}
}
