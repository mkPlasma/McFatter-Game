package engine.entities;

/*
 * 		MovableEntity.java
 * 		
 * 		Purpose:	Abstract movable entity.
 * 		Notes:		Extensive movement properties, including
 * 					speed, direction, acceleration, and angular velocity.
 * 		
 * 		Children: Bullet.java, Enemy.java, Effect.java
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */

public abstract class MovableEntity extends GameEntity{
	
	protected float	dir, dirPast,
					velX, velY, angVel,
					spd, spdPast, accel,
					spdMin, spdMax;
	
	protected boolean useSpdMin, useSpdMax;
	
	public MovableEntity(){
		super();
	}
	
	public MovableEntity(EntityFrame frame, float x, float y){
		super(frame, x, y);
	}
	
	public MovableEntity(EntityFrame frame, float x, float y, float dir, float spd){
		super(frame, x, y);
		
		this.dir = dir;
		this.spd = spd;
		
		dirPast = dir;
		spdPast = spd;
		
		setVelocities();
	}
	
	
	protected void updateMovements(){
		
		// Acceleration
		spd += accel;
		
		// Keep speed within range
		
		if(useSpdMax && spd > spdMax)
			spd = spdMax;
		else if(useSpdMin && spd < spdMin)
			spd = spdMin;
		
		// Angular velocity
		dir += angVel;
		
		updateVelocity();
		
		// Movement
		x += velX;
		y += velY;
	}
	
	// Updates velocity if direction has changed
	private void updateVelocity(){
		if(dir != dirPast){
			setVelocities();
			dirPast = dir;
			spdPast = spd;
			return;
		}
		
		if(spd != spdPast){
			
			if(spdPast == 0){
				setVelocities();
				return;
			}
			
			float sr = spd/spdPast;
			
			if(Float.isInfinite(sr)){
				setVelocities();
				return;
			}
			
			velX *= sr;
			velY *= sr;
			
			spdPast = spd;
		}
	}
	
	// Set velocities based on direction
	private void setVelocities(){
		velX = (float)(spd*Math.cos(Math.toRadians(dir)));
		velY = (float)(spd*Math.sin(Math.toRadians(dir)));
	}
	
	
	public float getDir(){
		return dir;
	}
	
	public void setDir(float dir){
		this.dir = dir;
	}
	
	public float getVelX(){
		return velX;
	}
	
	public void setVelX(float velX){
		this.velX = velX;
	}
	
	public float getVelY(){
		return velY;
	}
	
	public void setVelY(float velY){
		this.velY = velY;
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
	
	public float getSpdMax(){
		return spdMax;
	}
	
	public void setSpdMax(float spdMax){
		this.spdMax = spdMax;
	}
	
	public float getSpdMin(){
		return spdMin;
	}
	
	public void setSpdMin(float spdMin){
		this.spdMin = spdMin;
	}
	
	public boolean useSpdMin(){
		return useSpdMin;
	}
	
	public void setUseSpdMin(boolean useSpdMin){
		this.useSpdMin = useSpdMin;
	}
	
	public boolean useSpdMax(){
		return useSpdMax;
	}
	
	public void setUseSpdMax(boolean useSpdMax){
		this.useSpdMax = useSpdMax;
	}
}
