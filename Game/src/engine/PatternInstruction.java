package engine;

import java.util.ArrayList;

public class PatternInstruction extends Instruction{
	
	// Generates bullet patterns
	
	// args reference
	// PATTERN_CIRCLE	[num, radius, stackNum, spdInc]
	// PAATTERN_SPIRAL	[num, radius, rotation, delay, count, stackNum, spdInc]
	
	public static final short
		PATTERN_CIRCLE =			0,
		PATTERN_CIRCLE_STACKED =	1,
		PATTERN_ARC =				2,
		PATTERN_ARC_STACKED =		3,
		PATTERN_SPIRAL =			4,
		PATTERN_SPIRAL_STACKED =	5;
	
	// Bullets that should be added
	private ArrayList<Bullet> bullets;
	
	// Base bullet
	private final Bullet base;
	
	public PatternInstruction(int time, int type, double[] args, Bullet base){
		super(time, type, args);
		bullets = new ArrayList<Bullet>();
		
		this.base = base;
		
		// Init to set pos/dir
		if(base != null)
			base.getInstructionSet().init(2);
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
	}
	
	public boolean run(int time){
		// Return false if not time for instruction to be run yet
		if(time < this.time)
			return false;
		
		switch(type){
			case PATTERN_CIRCLE:
				return patternCircle(false);
			case PATTERN_CIRCLE_STACKED:
				return patternCircle(true);
			case PATTERN_ARC:
				return patternArc(false);
			case PATTERN_ARC_STACKED:
				return patternArc(true);
			case PATTERN_SPIRAL:
				return patternSpiral(false);
			case PATTERN_SPIRAL_STACKED:
				return patternSpiral(true);
		}
		return true;
	}
	
	private boolean patternCircle(boolean stack){
		for(int i = 0; i < args[0]; i++){
			
			int stackNum = stack ? (int)args[2] : 1;
			double spdInc = args[3];
			
			for(int j = 0; j < stackNum; j++){
				Bullet b = new Bullet(base);
				double dir = base.getDir() + i*(360/args[0]);
				
				// New bullet pos/dir will override original
				b.getInstructionSet().set(new BulletInstruction(b, 0, BulletInstruction.SET_POS, new double[]{(base.getX() + args[1]*Math.cos(Math.toRadians(dir))), (base.getY() + args[1]*Math.sin(Math.toRadians(dir)))}), 0);
				b.getInstructionSet().set(new BulletInstruction(b, 0, BulletInstruction.SET_DIR, new double[]{dir}), 1);
				
				if(stack)
					b.getInstructionSet().add(new BulletInstruction(b, 0, BulletInstruction.SET_SPD, new double[]{base.getSpd() + j*spdInc}), 2);
				
				// Make sure to set them at first frame
				b.getInstructionSet().setBullet(b);
				b.getInstructionSet().init(stack ? 3 : 2);
				
				bullets.add(b);
			}
		}
		
		return true;
	}
	
	private boolean patternArc(boolean stack){
		
		return true;
	}

	private boolean patternSpiral(boolean stack){
		for(int i = 0; i < args[0]; i++){
			
			int stackNum = stack ? (int)args[5] : 1;
			double spdInc = args[6];
			
			for(int j = 0; j < stackNum; j++){
				Bullet b = new Bullet(base);
				double dir = base.getDir() + i*(360/args[0]);
				
				if(j == stackNum - 1)
					base.setDir(base.getDir() + args[2]);
				
				// New bullet pos/dir will override original
				b.getInstructionSet().set(new BulletInstruction(b, 0, BulletInstruction.SET_POS, new double[]{(base.getX() + args[1]*Math.cos(Math.toRadians(dir))), (base.getY() + args[1]*Math.sin(Math.toRadians(dir)))}), 0);
				b.getInstructionSet().add(new BulletInstruction(b, 0, BulletInstruction.SET_DIR, new double[]{dir}), 1);
				
				if(stack)
					b.getInstructionSet().add(new BulletInstruction(b, 0, BulletInstruction.SET_SPD, new double[]{base.getSpd() + j*spdInc}));
				
				// Make sure to set them at first frame
				
				b.getInstructionSet().setBullet(b);
				b.getInstructionSet().init(stack ? 3 : 2);
				
				bullets.add(b);
			}
		}
		
		// Increase time value so spiral repeats
		time += args[3];
		
		// Decrement count
		// Loop forever if count = -1
		if(args[4] != -1)
			args[4] -= 1;

		// Return true (finished) if all of spiral was fired
		return args[4] == 0;
	}
}
