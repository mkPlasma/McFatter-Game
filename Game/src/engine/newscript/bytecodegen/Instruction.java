package engine.newscript.bytecodegen;

public class Instruction{
	
	private final byte opcode;
	private final int operand;
	
	public Instruction(byte opcode){
		this.opcode = opcode;
		operand = 0;
	}
	
	public Instruction(byte opcode, int operand){
		this.opcode = opcode;
		this.operand = operand;
	}
	
	public byte getOpcode(){
		return opcode;
	}
	
	public int getOperand(){
		return operand;
	}
}
