package engine;

public class InstructionSet{
	
	private Instruction[] inst;
	private int time, index;
	
	// Type of instruction
	public static final int
		INST_DEFAULT = 0,
		INST_BULLET = 1,
		INST_PATTERN = 2;
	
	private final int type;
	
	
	public InstructionSet(InstructionSet instset){
		inst = instset.getInstructionArray();
		type = instset.getType();
		
		switch(type){
			case INST_BULLET:
				for(int i = 0; i < inst.length; i++){
					inst[i] = new MovementInstruction((MovementInstruction)inst[i]);
				}
				break;
		}
	}
	
	public InstructionSet(int type){
		this.type = type;
	}
	
	// Bullet Instructions
	public InstructionSet(MovementInstruction[] inst){
		this.inst = inst;
		type = INST_BULLET;
	}

	public InstructionSet(MovementInstruction inst){
		this.inst = new MovementInstruction[]{inst};
		type = INST_BULLET;
	}
	
	public void setEntity(MovableEntity entity){
		for(int i = 0; i < inst.length; i++){
			MovementInstruction bi = (MovementInstruction)inst[i];
			bi.setEntity(entity);
		}
	}
	
	// General functions
	public int length(){
		return inst.length;
	}
	
	public int getType(){
		return type;
	}
	
	public Instruction[] getInstructionArray(){
		return inst;
	}
	
	public Instruction get(int index){
		return inst[index];
	}
	
	public Instruction getCurrentInstruction(){
		if(index == inst.length)
			return inst[index - 1];
		
		return inst[index];
	}
	
	public void set(Instruction inst, int index){
		this.inst[index] = inst;
	}
	
	public void add(Instruction inst){
		
		Instruction[] newInst;
		int len = 0;
		
		if(this.inst == null)
			newInst = new Instruction[1];
		else{
			len = this.inst.length;
			newInst = new Instruction[len + 1];
		}
		
		for(int i = 0; i < len; i++)
			newInst[i] = this.inst[i];
		
		newInst[newInst.length - 1] = inst;
		this.inst = newInst;
	}
	
	public void add(Instruction inst, int index){
		
		Instruction[] newInst;
		int len = 0;
		
		if(this.inst == null)
			newInst = new Instruction[1];
		else{
			len = this.inst.length;
			newInst = new Instruction[len + 1];
		}
		
		for(int i = 0; i < len; i++){
			if(i < index)
				newInst[i] = this.inst[i];
			else
				newInst[i + 1] = this.inst[i];
		}
		
		newInst[index] = inst;
		this.inst = newInst;
	}
	
	
	// Runs the first instructions at time 0
	// Use to set initial position of bullets for example
	public void init(){
		inst[0].run(0);
		index++;
	}
	
	public void init(int count){
		for(int i = 0; i < count; i++){
			if(inst[i] != null){
				inst[i].run(0);
				index++;
			}
		}
	}
	
	// Runs the instruction set
	// Returns true when finished, false otherwise
	public boolean run(){
		
		if(index == inst.length)
			return true;
		
		// If time is set to -1, loop the instruction set
		if(inst[index].getTime() == -1){
			
			index = 0;
			time = 0;
			return true;
		}
		
		boolean ran = false;
		
		// Runs all instructions with that time value
		while(inst[index].run(time) && inst[index].getTime() != -1){
			
			// Increment instruction index
			index++;
			
			// Exit if last instruction is reached
			if(index == inst.length)
				return true;
			
			time = 0;
			ran = true;
		}
		
		// Increment time
		time++;
		return ran;
	}
}
