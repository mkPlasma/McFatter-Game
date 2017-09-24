package engine.entities;

/*
 * 		Instruction.java
 * 		
 * 		Purpose:	Holds an instruction for what an entity should do.
 * 		Notes:		Currently only used for movement. Probably will not
 * 					be used for anything else.
 * 		
 * 		Children: MovementInstruction.java
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */

public abstract class Instruction{
	
	// Time will control when the instruction is run
	// Time in frames from start of instruction set execution
	
	// Not final, allows an instruction to repeat itself
	protected int time;
	
	// Type controls what kind of action will be performed
	protected final int type;
	
	// Args will control how the action is done
	protected final float[] args;
	
	public Instruction(int time, int type, float args[]){
		this.time = time;
		this.type = type;
		this.args = args;
	}
	
	// Run the instruction
	
	// Some instructions will require change over time
	// In this case, this function should return false when running
	// Return true when finished
	public abstract boolean run(int time);
	
	public int getTime(){
		return time;
	}
	
	public int getType(){
		return type;
	}
	
	public float[] getArgs(){
		return args;
	}
}
