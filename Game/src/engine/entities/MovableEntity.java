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
	
	// Instruction set will control movements
	protected InstructionSet inst;
	
	protected float	dir, dirPast,
					velX, velY, angVel,
					spd, spdPast, accel,
					spdMin, spdMax;
	
	protected boolean useSpdMin, useSpdMax;
	
	public MovableEntity(){
		super();
	}
	
	public MovableEntity(float x, float y){
		super(x, y);
	}
	
	public MovableEntity(float[] pos){
		super(pos);
	}
	
	public InstructionSet getInstructionSet(){
		return inst;
	}
	
	public void setInstructionSet(InstructionSet inst){
		this.inst = inst;
	}
	
	public void setInstructionSet(MovementInstruction inst){
		this.inst = new InstructionSet(inst);
	}
	
	protected void updateMovements(){
		
		// Set up movements
		inst.run();
		
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
	protected void updateVelocity(){
		if(dir != dirPast){
			velX = (float)(spd*Math.cos(Math.toRadians(dir)));
			velY = (float)(spd*Math.sin(Math.toRadians(dir)));
			
			dirPast = dir;
			spdPast = spd;
		}
		else if(spd != spdPast){
			float sr = spd/spdPast;
			velX *= sr;
			velY *= sr;
			
			spdPast = spd;
		}
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
