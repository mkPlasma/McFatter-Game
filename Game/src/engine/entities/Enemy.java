package engine.entities;

import java.awt.Color;

import engine.graphics.Renderer;

public class Enemy extends MovableEntity{
	
	// Movement attributes
	// 0 - Use min spd
	// 1 - Use max spd
	protected byte attr[] = new byte[2];
	
	private final int hitboxSize;
	
	// Whether entity can collide
	protected boolean collisions = true;
	
	private int health;
	private int hpmax;
	
	public Enemy(InstructionSet inst){
		super();
		
		setVisible(true);
		
		inst.setEntity(this);
		this.inst = inst;
		inst.init();
		
		hitboxSize = 8;
		
		hpmax = 50000;
		health = hpmax;
		
		onCreate();
	}
	
	
	public void update(){
		
		// Set up bullet movements
		inst.run();
		
		// Acceleration
		spd += accel;
		
		// Keep speed within range
		
		if(attr[1] == 1 && spd > spdMax)
			spd = spdMax;
		else if(attr[0] == 1 && spd < spdMin)
			spd = spdMin;
		
		// Angular velocity
		dir += angVel;
		
		// Movement
		x += spd*Math.cos(Math.toRadians(dir));
		y += spd*Math.sin(Math.toRadians(dir));
		
		time++;
	}
	
	public void draw(Renderer r){
		
		if(!visible)
			return;
		
		r.drawCircle((int)x - hitboxSize, (int)y - hitboxSize, hitboxSize*2, hitboxSize*2, Color.GREEN);
		
		// Health bar (temporary)
		r.drawRectangle(10, 10, 780, 10, Color.DARK_GRAY);
		r.drawRectangle(10, 10, (int)(780*((double)health/(double)hpmax)), 10, Color.GREEN);
	}
	
	public void onCreate(){
		
	}
	
	public void onDestroy(){
		
	}

	public void setAttributes(byte[] attr){
		this.attr = attr;
	}
	
	public byte[] getAttributes(){
		return attr;
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
	
	public void setCollisions(boolean collisions){
		this.collisions = collisions;
	}
	
	public boolean collisionsEnabled(){
		return collisions;
	}
}
