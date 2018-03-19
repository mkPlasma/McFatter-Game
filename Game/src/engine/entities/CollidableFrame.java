package engine.entities;

import engine.graphics.Sprite;

/**
 * 
 * Entity frame for bullets and enemies.
 * 
 * @author Daniel
 *
 */

public class CollidableFrame extends EntityFrame{
	
	// Shape of hitbox
	protected final int hitboxType;
	
	// Hitbox dimensions
	protected final int hitboxWidth, hitboxLength;
	
	// Hitbox y-axis offset
	protected final int hitboxOffset;
	
	public CollidableFrame(int type, Sprite sprite, int hitboxType, int hitboxWidth, int hitboxLength, int hitboxOffset, boolean spriteAlign, float spriteRotation){
		super(type, sprite, spriteAlign, spriteRotation);
		this.hitboxType = hitboxType;
		this.hitboxWidth = hitboxWidth;
		this.hitboxLength = hitboxLength;
		this.hitboxOffset = hitboxOffset;
	}
	
	public int getHitboxType(){
		return hitboxType;
	}
	
	public int getHitboxWidth(){
		return hitboxWidth;
	}
	
	public int getHitboxLength(){
		return hitboxLength;
	}
	
	public int getHitboxOffset(){
		return hitboxOffset;
	}
}
