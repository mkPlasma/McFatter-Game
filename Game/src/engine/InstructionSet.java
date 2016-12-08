package engine;

import java.util.ArrayList;

public class InstructionSet{
	
	private Instruction[] inst;
	private int time, index;
	
	// Type of instruction
	// 0 - Generic (default)
	// 1 - Bullet
	// 2 - Pattern
	private final int type;
	
	// Pattern Instruction bullet list
	private ArrayList<Bullet> bullets;

	public InstructionSet(){
		type = 0;
	}
	
	public InstructionSet(Instruction[] inst){
		this.inst = inst;
		type = 0;
	}
	
	public InstructionSet(Instruction inst){
		this.inst = new Instruction[]{inst};
		type = 0;
	}
	
	// Bullet Instructions
	public InstructionSet(BulletInstruction[] inst){
		this.inst = inst;
		type = 1;
	}

	public InstructionSet(BulletInstruction inst){
		this.inst = new BulletInstruction[]{inst};
		type = 1;
	}
	
	public void setBullet(Bullet bullet){
		for(int i = 0; i < inst.length; i++){
			BulletInstruction bi = (BulletInstruction)inst[i];
			bi.setBullet(bullet);
		}
	}
	
	
	// Pattern Instructions
	public InstructionSet(PatternInstruction[] inst){
		this.inst = inst;
		bullets = new ArrayList<Bullet>();
		type = 2;
	}

	public InstructionSet(PatternInstruction inst){
		this.inst = new PatternInstruction[]{inst};
		bullets = new ArrayList<Bullet>();
		type = 2;
	}
	
	public ArrayList<Bullet> getBullets(){
		// Returns bullets then clears the array list
		// This is because the bullets on the frame should only be fired once
		// If not cleared, all previous bullets would keep firing forever
		
		if(bullets.size() < 1)
			return null;
		
		ArrayList<Bullet> temp = bullets;
		bullets.removeAll(bullets);
		
		return temp;
	}
	
	// General functions
	public int length(){
		return inst.length;
	}
	
	
	public Instruction get(int index){
		return inst[index];
	}
	
	public Instruction getCurrentInstruction(){
		if(index == inst.length)
			return inst[index - 1];
		
		return inst[index];
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
	
	
	// Runs the first instruction at time 0
	// Use to set initial position of bullets for example
	public void init(){
		inst[0].run(0);
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
		
		while(inst[index].run(time)){
			index++;
			
			// Add bullets for Pattern Instructions
			
			if(type == 2){
				PatternInstruction pi = (PatternInstruction)inst[index];
				ArrayList<Bullet> b = pi.getBullets();
				
				for(int i = 0; i < b.size(); i++){
					bullets.add(b.get(i));
				}
			}
			
			if(index == inst.length)
				return true;
			
			ran = true;
		}
		
		time++;
		return ran;
	}
}
