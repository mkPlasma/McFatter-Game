package engine.newscript.parser;

import static engine.newscript.lexer.TokenType.*;

/**
 * 
 * List of specific rule-defined parse error messages.
 * 
 * @author Daniel
 * 
 */

public class Errors{
	
	private final Rule[] rules;
	
	public Errors(){
		rules = new Rule[]{
			new Rule("Missing semicolon",
			new Object[][]{
				{"new_var"},
				{"new_var_def"},
				{"const_var_def"},
				{"assign"},
				{"assign_u"},
				{"array_elem_assign"},
				{"array_elem_assign_u"},
				{"func_call"},
				{"dot_func_call"},
				{"func_call_scope"},
				{"break"},
				{"return"},
				{"returnif"},
				{"wait"},
				{"waits"},
				{"wait_while"},
				{"wait_until"},
			}),
			
			new Rule("Empty block",
			new Object[][]{
				{"func_def"},
				{"task_def"},
				{"if_cond"},
				{"else_if_cond"},
				{ELSE},
				{"while_cond"},
				{"until_cond"},
				{"for_cond"},
			}),
			
			new Rule("Constant variable must be defined",
			new Object[][]{
				{SET, CONST},
			}),
		};
	}
	
	public Rule[] getRules(){
		return rules;
	}
}
