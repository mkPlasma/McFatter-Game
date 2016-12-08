package engine;

import java.util.ArrayList;

public class PatternInstruction extends Instruction{
	
	// Generates bullet patterns
	
	// args reference
	// PATTERN_CIRCLE	[num, radius]
	
	public static final short
		PATTERN_CIRCLE =	1,
		PATTERN_ARC =		2,
		PATTERN_SPIRAL =	3;
	
	// Bullets that should be added
	ArrayList<Bullet> bullets;
	
	// Base bullet
	Bullet base;
	
	public PatternInstruction(int time, int type, float[] args, Bullet base){
		super(time, type, args);
		bullets = new ArrayList<Bullet>();
		this.base = base;
	}
	
	public ArrayList<Bullet> getBullets(){
		// Returns bullets then clears the array list
		// This is because the bullets on the frame should only be fired once
		// If not cleared, all previous bullets would keep firing forever
		
		ArrayList<Bullet> temp = bullets;
		bullets.removeAll(bullets);
		
		return temp;
	}
	
	public boolean run(int time){
		// Return false if not time for instruction to be run yet
		if(time < this.time)
			return false;
		
		switch(type){
			case 0:
				patternCircle();
				break;
			case 1:
				patternArc();
		}
		return true;
	}
	
	private void patternCircle(){
		for(int i = 0; i < args[0]; i++){
			Bullet b = base;
			
			// Init to set pos/dir
			base.getInstructionSet().init();
			
			float dir = base.getDir() + i*(360/args[0]);
			
			// New bullet pos/dir will override original
			b.getInstructionSet().add(new BulletInstruction(b, 0, BulletInstruction.SET_POS, new float[]{(float)(base.getX() + args[1]*Math.cos(dir)), (float)(base.getY() + args[1]*Math.sin(dir))}), 1);
			b.getInstructionSet().add(new BulletInstruction(b, 0, BulletInstruction.SET_POS, new float[]{dir}), 2);
		}
	}
	
	private void patternArc(){
		
	}
}
