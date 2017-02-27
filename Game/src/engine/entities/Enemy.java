package engine.entities;

import java.awt.Color;

import engine.graphics.Renderer;

public class Enemy extends MovableEntity{
	
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
		updateMovements();
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
