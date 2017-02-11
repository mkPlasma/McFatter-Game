package engine;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

import content.BulletSheet;

public class Enemy extends MovableEntity{

	// Bullet attributes
	// 0 - Use min spd
	// 1 - Use max spd
	protected int attr[] = new int[2];
	
	
	protected final int hitboxSize;
	
	protected int health;
	protected int hpmax;
	
	protected ArrayList<Bullet> bullets;
	
	public Enemy(float x, float y){
		super(x, y);
		hitboxSize = 8;
		bullets = new ArrayList<Bullet>();
		
		hpmax = 50000;
		health = hpmax;
		
		onCreate();
	}
	
	public void damage(int damage){
		health -= damage;
		
		if(health <= 0)
			remove = true;
	}
	
	public int getHealth(){
		return health;
	}
	
	
	public int getHitboxSize(){
		return hitboxSize;
	}
	
	// temp
	private int counter = 0;
	private Random random = new Random();
	private int r;
	float bx, by;
	
	public void update(){
		
		bx = (float)(x + 100*Math.cos(time*0.1));
		by = (float)(y + 100*Math.sin(time*0.1));
		
		/*
		if(time % 1 == 0){
			
			int c = 2;
			int r = random.nextInt(5);
			
			for(int i = 0; i < c; i++){
				double dir = i*(360d/c) + time*6;
				
				InstructionSet inst = new InstructionSet(InstructionSet.INST_BULLET);
				inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_BULLET, MovementInstruction.SET_POS, new double[]{bx, by}));
				inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_BULLET, MovementInstruction.SET_DIR, new double[]{dir}));
				inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_BULLET, MovementInstruction.CONST_ACCEL, new double[]{dir, 4, -0.1, 0, 10}));
				inst.add(new MovementInstruction(null, 120, MovementInstruction.ENT_BULLET, MovementInstruction.SET_DIR, new double[]{dir + 180}));
				inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_BULLET, MovementInstruction.SET_ACCEL, new double[]{0}));
				inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_BULLET, MovementInstruction.SET_SPD, new double[]{3}));
				
				Bullet b = new Bullet(inst, Bullet.TYPE_SCALE, i == 0 ? Bullet.COLOR_PINK : Bullet.COLOR_LIGHT_BLUE);
				bullets.add(b);
			}
		}
		*/
		
		int c = 64;
		
		int t = 120;
		
		if(time % t == 0){
			for(int i = 0; i < c; i++){
				float dir = i*(360f/c) + (180/c) + 90;
				
				InstructionSet inst = new InstructionSet(InstructionSet.INST_BULLET);
				inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_BULLET, MovementInstruction.SET_POS, new float[]{x, y}));
				inst.add(new MovementInstruction(null, 0, MovementInstruction.ENT_BULLET, MovementInstruction.CONST_ACCEL, new float[]{dir, 6, -0.1f, 0, 10}));
				inst.add(new MovementInstruction(null, t, MovementInstruction.ENT_BULLET, MovementInstruction.CONST_ACCEL, new float[]{dir, 0, 0.05f, 0, 10}));
				
				Bullet b = new Bullet(inst, BulletSheet.get((byte)counter, BulletSheet.COLOR_PURPLE));
				bullets.add(b);
			}
			
			counter++;
			
			if(counter == 8)
				counter = 0;
		}
		
		time++;
	}
	
	public void draw(Renderer r){
		r.drawCircle((int)x - hitboxSize, (int)y - hitboxSize, hitboxSize*2, hitboxSize*2, Color.GREEN);
		
		
		// Health bar (temporary)
		r.drawRectangle(10, 10, 750, 10, Color.DARK_GRAY);
		r.drawRectangle(10, 10, (int)(750d*((double)health/(double)hpmax)), 10, Color.GREEN);
	}
	
	public void onCreate(){
		
	}
	
	public void onDestroy(){
		
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
}
