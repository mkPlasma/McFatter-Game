package engine.entities;

import engine.graphics.Renderer;

/*
 * 		Laser.java
 * 		
 * 		Purpose:	Solid laser object.
 * 		Notes:		Implement curvy lasers?
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */

public class Laser extends Bullet{
	
	// Laser size
	private int length, width;
	private float scx, scy;
	
	public Laser(BulletFrame frame, MovementInstruction inst){
		super(frame, inst);
		
		visible = true;
		
		this.frame = frame;
		
		inst.setEntity(this);
		this.inst = new InstructionSet(inst);
		
		onCreate();
	}
	
	public Laser(BulletFrame frame, InstructionSet inst){
		super(frame, inst);
		
		visible = true;
		
		this.frame = frame;
		
		inst.setEntity(this);
		this.inst = inst;
		
		onCreate();
	}
	
	public Laser(BulletFrame frame, float x, float y, float dir, float spd, int length, int width){
		super(frame, x, y, dir, spd);
		
		visible = true;

		this.frame = frame;
		
		inst = new InstructionSet(InstructionSet.INST_MOVABLE);
		inst.add(new MovementInstruction(this, 0, MovementInstruction.ENT_LASER, MovementInstruction.SET_POS, new float[]{x, y}));
		inst.add(new MovementInstruction(this, 0, MovementInstruction.ENT_LASER, MovementInstruction.CONST_DIR_SPD, new float[]{dir, spd}));
		
		onCreate();
	}
	
	public Laser(BulletFrame frame, float x, float y, float dir, float spd, int length, int width, int damage){
		super(frame, x, y, dir, spd, damage, 0);
		
		visible = true;
		
		this.frame = frame;
		
		this.damage = damage;
		
		inst = new InstructionSet(InstructionSet.INST_MOVABLE);
		inst.add(new MovementInstruction(this, 0, MovementInstruction.ENT_LASER, MovementInstruction.SET_POS, new float[]{x, y}));
		inst.add(new MovementInstruction(this, 0, MovementInstruction.ENT_LASER, MovementInstruction.CONST_DIR_SPD, new float[]{dir, spd}));
		
		onCreate();
	}
	
	public void update(){
		if(paused)
			return;
		
		updateMovements();
		
		// Delete at borders
		if(x < -64 || x > 864 || y < -64 || y > 664)
			remove = true;
		
		time++;
	}
	
	public void draw(Renderer r){
		
		if(!visible)
			return;

		scx = ((float)width)/((float)frame.getSprite().getWidth());
		scy = ((float)length)/((float)frame.getSprite().getHeight());
		
		//r.render(frame.getSprite(), time, (int)(x + (length/2)*Math.cos(Math.toRadians(dir))), (int)(y + (length/2)*Math.sin(Math.toRadians(dir))), 1, dir + 90, scx, scy);
	}
	
	public void onCreate(){
		
	}
	
	public void onDestroy(){
		remove = true;
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
	
	public void setLength(int length){
		this.length = length;
	}
	
	public int getLength(){
		return length;
	}
	
	public void setWidth(int width){
		this.width = width;
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getHitboxSize(){
		return (int)(scx*((BulletFrame)frame).getHitboxSize());
	}
	
	public int getHBLengthCrop(){
		return (int)(length - length*((BulletFrame)frame).getHBLengthCrop());
	}
}
