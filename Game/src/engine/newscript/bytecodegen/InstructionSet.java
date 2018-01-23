package engine.newscript.bytecodegen;

public enum InstructionSet{

	// Stores an integer zero in specified value (default initialization)
	store_zero(true),
	
	// Store top stack value into specified variable (pops value)
	store_value(true),
	
	
	// Push a specified value onto the stack
	load_int(true),
	load_float(true),
	load_false,
	load_true,
	
	// Push value of a specified variable onto the stack
	load_var(true),
	
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
	
	// Increment/decrement specified variable by 1
	op_inc(true),
	op_dec(true),
	
	// Invert specified variable
	op_inv(true),
	
	
	// Unconditional jump to specified bytecode index
	jump(true),
	
	// Jump to specified bytecode index if stack top value is true/false (pops value)
	jump_if_true(true),
	jump_if_false(true),
	
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
