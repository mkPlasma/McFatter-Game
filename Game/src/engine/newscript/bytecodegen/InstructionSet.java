package engine.newscript.bytecodegen;

public enum InstructionSet{
	
	// Initialize variable to zero
	init_zero,
	init_zero_l,

	// Initialize variable to top stack value (pops value)
	init_value,
	init_value_l,
	
	// Initialize variable to specified value
	init_int(true),
	init_int_l(true),
	init_float(true),
	init_float_l(true),
	init_false,
	init_false_l,
	init_true,
	init_true_l,
	
	// Store top stack value into specified variable (pops value)
	store_value(true),
	store_value_l(true),
	
	
	// Push a specified value onto the stack
	load_int(true),
	load_float(true),
	load_false,
	load_true,
	
	// Push value of a specified variable onto the stack
	load_var(true),
	load_var_l(true),
	
	// Push a specified predefined constant onto the stack (strings & arrays)
	load_const(true),
	
	
	// Pop and operate top two values of stack, then push result
	op_add,
	op_sub,
	op_mult,
	op_div,
	op_mod,
	op_exp,
	
	op_eq,
	op_lt,
	op_gt,
	op_lte,
	op_gte,
	op_neq,
	
	op_or,
	op_and,
	op_not,
	
	op_concat,
	
	// Increment/decrement specified variable by 1
	op_inc(true),
	op_inc_l(true),
	op_dec(true),
	op_dec_l(true),
	
	// Invert specified variable
	op_inv(true),
	op_inv_l(true),
	
	
	// Unconditional jump to specified bytecode index
	jump(true),
	
	// Jump to specified bytecode index if stack top value is true/false (pops value)
	jump_if_true(true),
	jump_if_false(true),
	
	
	// Uses stack top value to move another value to top of stack, operand is maximum, used for conditionals
	move_to_top(true),
	
	// Pop a specified number of values below top value, used for conditionals
	pop_count(true),
	
	
	// Creates an array, uses top stack value as length and takes all top values
	array_create,
	init_array(true),
	init_array_l(true),
	
	// Uses top stack value as index for next value, then pushes element
	array_elem,
	// Use specified variable as array instead of value on stack
	array_elem_v(true),
	array_elem_v_l(true),
	
	// Stores top stack value into specified array, second stack value is used as index
	store_array_elem(true),
	store_array_elem_l(true),
	
	// Copies top stack value down, used for augmented array element assignment
	copy_top,
	
	
	// Pops specified number of values, reverse, and place in local variables, used for functions/tasks
	init_params(true),
	
	// Jump to function, push value to return stack, and push local variable list
	jump_func(true),
	// Same as above, also allows function to return a value
	jump_func_r(true),

	// Same as above, also branches script
	jump_branch(true),
	jump_branch_r(true),
	
	// Pops value on return stack and returns from function
	return_void,
	
	// Returns value if called with jump_x_r, otherwise returns void
	return_value,
	;
	
	private final boolean operand;

	private InstructionSet(){
		operand = false;
	}
	
	private InstructionSet(boolean operand){
		this.operand = operand;
	}
	
	public boolean hasOperand(){
		return operand;
	}
	
	
	// For getting index/byte value of instruction
	private static InstructionSet[] instSet = InstructionSet.values();
	
	// Start at -128
	public static byte getOpcode(InstructionSet inst){
		for(int i = 0; i < instSet.length; i++)
			if(instSet[i] == inst)
				return (byte)(i - 128);
		
		return 127;
	}
	
	public static InstructionSet getName(int opcode){
		return instSet[opcode + 128];
	}
}
