package engine.entities;

import engine.graphics.Sprite;

/**
 * 
 * Abstract movable game entity.
 * Contains properties such as speed, direction, acceleration, and angular velocity.
 * 
 * @author Daniel
 *
 */

public abstract class MovableEntity extends GameEntity{
	
	// Stores a template of sprites and animations
	protected EntityFrame frame;
	
	
	// Movement properties
	protected float	dir, dirRad, angVel,
					spd, accel,
					minSpd, maxSpd;
	
	// Limit speed if true
	protected boolean useMinSpd, useMaxSpd;
	
	
	public MovableEntity(EntityFrame frame, float x, float y){
		super(x, y);
		
		this.frame = frame;
	}
	
	public MovableEntity(EntityFrame frame, float x, float y, float dir, float spd){
		super(x, y);
		
		this.frame = frame;
		this.dir = dir;
		this.spd = spd;
	}
	
	public MovableEntity(EntityFrame frame, float x, float y, float dir, float spd, float minSpd, float maxSpd, float accel){
		super(x, y);
		
		this.dir = dir;
		this.spd = spd;
		this.minSpd = minSpd;
		this.maxSpd = maxSpd;
		this.accel = accel;
		
		useMinSpd = true;
		useMaxSpd = true;
	}
	
	public void update(){
		updateMovements();
		time++;
	}
	
	protected void updateMovements(){
		
		// Acceleration
		spd += accel;
		
		// Keep speed within range
		
		if(useMaxSpd && spd > maxSpd)
			spd = maxSpd;
		else if(useMinSpd && spd < minSpd)
			spd = minSpd;
		
		// Angular velocity
		dir += angVel;
		
		dirRad = (float)Math.toRadians(dir);
		
		x += spd*Math.cos(dirRad);
		y += spd*Math.sin(dirRad);
	}
	
	
	public EntityFrame getFrame(){
		return frame;
	}
	
	public Sprite getSprite(){
		return frame.getSprite();
	}
	
	public float getDir(){
		return dir;
	}
	
	public void setDir(float dir){
		this.dir = dir;
	}
	
	public float getAngVel(){
		return angVel;
	}
	
	public void setAngVel(float angVel){
		this.angVel = angVel;
	}
	
	public float getSpd(){
		return spd;
	}
	
	public void setSpd(float spd){
		this.spd = spd;
	}
	
	public float getAccel(){
		return accel;
	}
	
	public void setAccel(float accel){
		this.accel = accel;
	}
	
	public float getMaxSpd(){
		return maxSpd;
	}
	
	public void setMaxSpd(float spdMax){
		this.maxSpd = spdMax;
		useMaxSpd = true;
	}
	
	public float getMinSpd(){
		return minSpd;
	}
	
	public void setMinSpd(float spdMin){
		this.minSpd = spdMin;
		useMinSpd = true;
	}
}
