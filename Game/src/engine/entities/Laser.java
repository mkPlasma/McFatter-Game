package engine.entities;

import engine.graphics.Renderer;

/*
 * 		Laser.java
 * 		
 * 		Purpose:	Solid laser object.
 * 		Notes:		Implement curvy lasers?
 * 		
 */

public class Laser extends Bullet{
	
	// Laser size
	private int length, width;
	private float scx, scy;
	
	public Laser(){
		super(null, 0, 0, 0, 0, null);
	}
	
	/*
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
	*/
	
	public void update(){
		super.update();
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
		deleted = true;
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
