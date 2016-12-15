package engine;

import java.util.ArrayList;

public class InstructionSet{
	
	private Instruction[] inst;
	private int time, index;
	
	// Type of instruction
	public static final int
		INST_DEFAULT = 0,
		INST_BULLET = 1,
		INST_PATTERN = 2;
	
	private final int type;
	
	// Pattern Instruction bullet list
	//private ArrayList<Bullet> bullets;
	
	public InstructionSet(InstructionSet instset){
		inst = instset.getInstructionArray();
		type = instset.getType();
		
		switch(type){
			case INST_BULLET:
				for(int i = 0; i < inst.length; i++){
					inst[i] = new BulletInstruction((BulletInstruction)inst[i]);
				}
				break;
		}
	}
	
	public InstructionSet(int type){
		this.type = type;
	}
	
	// Bullet Instructions
	public InstructionSet(BulletInstruction[] inst){
		this.inst = inst;
		type = INST_BULLET;
	}

	public InstructionSet(BulletInstruction inst){
		this.inst = new BulletInstruction[]{inst};
		type = INST_BULLET;
	}
	
	public void setBullet(Bullet bullet){
		for(int i = 0; i < inst.length; i++){
			BulletInstruction bi = (BulletInstruction)inst[i];
			bi.setBullet(bullet);
		}
	}
	
	
	// Pattern Instructions
	// Removed for now
	/*
	public InstructionSet(PatternInstruction[] inst){
		this.inst = inst;
		bullets = new ArrayList<Bullet>();
		type = INST_PATTERN;
	}

	public InstructionSet(PatternInstruction inst){
		this.inst = new PatternInstruction[]{inst};
		bullets = new ArrayList<Bullet>();
		type = INST_PATTERN;
	}
	
	public ArrayList<Bullet> getBullets(){
		// Returns bullets then clears the array list
		// This is because the bullets on the frame should only be fired once
		// If not cleared, all previous bullets would keep firing forever
		
		if(bullets == null || bullets.size() < 1)
			return null;

		ArrayList<Bullet> temp = new ArrayList<Bullet>(bullets);
		bullets.clear();
		
		return temp;
	}*/
	
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
	}
	
	public void init(int count){
		for(int i = 0; i < count; i++){
			if(inst[i] != null)
				inst[i].run(0);
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
			
			//addBullets();
			
			// Increment instruction index
			index++;
			
			// Exit if last instruction is reached
			if(index == inst.length)
				return true;
			
			ran = true;
		}
		
		//addBullets();
		
		// Increment time
		time++;
		return ran;
	}
	
	/*
	private void addBullets(){
		// Add bullets for Pattern Instructions
		if(type == INST_PATTERN && inst[index] != null){
			PatternInstruction pi = (PatternInstruction)inst[index];
			ArrayList<Bullet> b = pi.getBullets();
			
			if(bullets == null)
				bullets = new ArrayList<Bullet>();
			
			if(b != null){
				for(int i = 0; i < b.size(); i++){
					if(b.get(i) != null)
						bullets.add(b.get(i));
				}
			}
		}
	}*/
}
