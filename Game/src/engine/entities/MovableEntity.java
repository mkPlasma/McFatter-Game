package engine.entities;

public abstract class MovableEntity extends GameEntity{
	
	// Instruction set will control movements
	protected InstructionSet inst;
	
	protected float dir, angVel, spd, accel, spdMin, spdMax;
	
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
	
	
	public float getDir(){
		return dir;
	}
	
	public void setDir(float dir){
		this.dir = dir;
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
	
	public float getAngVel(){
		return angVel;
	}
	
	public void setAngVel(float angVel){
		this.angVel = angVel;
	}
}
