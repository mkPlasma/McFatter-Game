package engine.newscript.bytecodegen;

public class Instruction{
	
	private final byte opcode;
	private final int operand;
	
	private final int fileIndex, lineNum;
	
	public Instruction(byte opcode, int fileIndex, int lineNum){
		this.opcode = opcode;
		operand = 0;
		this.fileIndex = fileIndex;
		this.lineNum = lineNum;
	}
	
	public Instruction(byte opcode, int operand, int fileIndex, int lineNum){
		this.opcode = opcode;
		this.operand = operand;
		this.fileIndex = fileIndex;
		this.lineNum = lineNum;
	}
	
	public byte getOpcode(){
		return opcode;
	}
	
	public int getOperand(){
		return operand;
	}
	
	public int getFileIndex(){
		return fileIndex;
	}
	
	public int getLineNum(){
		return lineNum;
	}
}
