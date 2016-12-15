package engine;

import java.awt.Color;
import java.awt.Graphics2D;

public class Bullet extends GameEntity{
	
	public static final int
		TYPE_ORB_S = 0,
		TYPE_ORB_M = 1,
		TYPE_ORB_L = 2;
	
	public static final byte
		BOUNCE_LEFT =		0b0000001,
		BOUNCE_RIGHT =		0b0000010,
		BOUNCE_TOP =		0b0000100,
		BOUNCE_BOTTOM =		0b0001000,
		BOUNCE_SIDES =		BOUNCE_LEFT | BOUNCE_RIGHT,
		BOUNCE_SIDES_TOP =	BOUNCE_SIDES | BOUNCE_TOP,
		BOUNCE_SPRITE_SIZE = 0b1000000;
	
	// Instruction set will control bullet movements
	private InstructionSet inst;
	
	// Bullet attributes
	// 0 - Use min spd
	// 1 - Use max spd
	// 2 - Num bounces
	// 3 - Bounce borders
	private int attr[] = new int[4];
	
	private double dir, angVel, spd, accel, spdMin, spdMax;
	
	private int type, color;
	private int hitboxSize;
	
	// If true, the bullet will be deleted
	private boolean remove;
	
	// If true, bullet will not be updated
	private boolean paused;
	
	public Bullet(Bullet b){
		inst = new InstructionSet(b.getInstructionSet());
		
		attr = b.getAttributes();
		
		dir = b.getDir();
		angVel = b.getAngVel();
		spd = b.getSpd();
		accel = b.getAccel();
		spdMin = b.getSpdMin();
		spdMax = b.getSpdMax();
		
		type = b.getType();
		color = b.getColor();
		initHitboxSize();
		
		paused = b.isPaused();
	}
	
	public Bullet(BulletInstruction inst, int type, int color){
		
		inst.setBullet(this);
		this.inst = new InstructionSet(inst);
		this.inst.init();
		
		this.type = type;
		this.color = color;
		initHitboxSize();
	}
	
	public Bullet(InstructionSet inst, int type, int color){
		
		inst.setBullet(this);
		this.inst = inst;
		inst.init();
		
		this.type = type;
		this.color = color;
		initHitboxSize();
	}
	
	public Bullet(double x, double y, double dir, double spd, int type, int color){
		setX(x);
		setY(y);
		
		BulletInstruction bi = new BulletInstruction(this, 0, BulletInstruction.CONST_DIR_SPD, new double[]{x, y, dir, spd});
		inst = new InstructionSet(bi);
		inst.init();
		
		this.type = type;
		this.color = color;
		initHitboxSize();
	}
	
	private void initHitboxSize(){
		switch(type){
			case 0:
				hitboxSize = 2;
				return;
			case 1:
				hitboxSize = 5;
				return;
			case 2:
				hitboxSize = 8;
				return;
		}
		
		return;
	}
	
	
	public InstructionSet getInstructionSet(){
		return inst;
	}
	
	public void setInstructionSet(InstructionSet inst){
		this.inst = inst;
	}
	public void setInstructionSet(BulletInstruction inst){
		this.inst = new InstructionSet(inst);
	}
	
	public void setAttributes(int[] attr){
		this.attr = attr;
	}
	
	public int[] getAttributes(){
		return attr;
	}
	
	public double getDir(){
		return dir;
	}
	
	public void setDir(double dir){
		this.dir = dir;
	}
	
	public double getSpd(){
		return spd;
	}
	
	public void setSpd(double spd){
		this.spd = spd;
	}
	
	public double getAccel(){
		return accel;
	}
	
	public void setAccel(double accel){
		this.accel = accel;
	}
	
	public double getSpdMax(){
		return spdMax;
	}
	
	public void setSpdMax(double spdMax){
		this.spdMax = spdMax;
	}
	
	public double getSpdMin(){
		return spdMin;
	}
	
	public void setSpdMin(double spdMin){
		this.spdMin = spdMin;
	}
	
	public double getAngVel(){
		return angVel;
	}
	
	public void setAngVel(double angVel){
		this.angVel = angVel;
	}
	
	public int getHitboxSize(){
		return hitboxSize;
	}
	
	public void setHitboxSize(int hitboxSize){
		this.hitboxSize = hitboxSize;
	}
	
	public int getType(){
		return type;
	}
	
	public void setType(int type){
		this.type = type;
	}
	
	public int getColor(){
		return color;
	}
	
	public void setColor(int color){
		this.color = color;
	}
	
	
	public boolean remove(){
		return remove;
	}
	
	public void setPaused(boolean paused){
		this.paused = paused;
	}
	
	public boolean isPaused(){
		return paused;
	}
	
	public void update(){
		if(paused)
			return;
		
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
		
		// Bullet movement
		x += spd*Math.cos(Math.toRadians(dir));
		y += spd*Math.sin(Math.toRadians(dir));
		
		// Delete at borders
		if(x < -64 || x > 864 || y < -64 || y > 664)
			remove = true;
		
		// Bouncing
		if(attr[2] > 0){
			if(((attr[3] & BOUNCE_LEFT) == BOUNCE_LEFT && x <= 0) || ((attr[3] & BOUNCE_LEFT) == BOUNCE_LEFT && x >= 800)){
				dir = -dir + 180;
				attr[2]--;
			}
			if(((attr[3] & BOUNCE_TOP) == BOUNCE_TOP && y <= 0) || ((attr[3] & BOUNCE_BOTTOM) == BOUNCE_BOTTOM && y >= 600)){
				dir = -dir;
				attr[2]--;
			}
		}
	}
	
	public void draw(Graphics2D g2d){
		float size = hitboxSize;
		
		g2d.setColor(Color.BLUE);
		g2d.fillOval((int)(x - (size/2)), (int)(y - (size/2)), (int)size, (int)size);
	}
}
