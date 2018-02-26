package engine.entities;

import engine.graphics.Sprite;

/**
 * 
 * Base frame class for holding preset data for other objects.
 * 
 * @author Daniel
 *
 */

public class EntityFrame{
	
	private final int type;
	
	private final Sprite sprite;
	
	// If true, sprite will rotate to the direction it's moving in
	private final boolean spriteAlign;
	
	// Rotation
	private final float spriteRotation;
	
	public EntityFrame(int type, Sprite sprite, boolean spriteAlign, float spriteRotation){
		this.type = type;
		this.sprite = sprite;
		this.spriteAlign = spriteAlign;
		this.spriteRotation = spriteRotation;
	}
	
	public int getType(){
		return type;
	}
	
	public Sprite getSprite(){
		return sprite;
	}
	
	public boolean spriteAlign(){
		return spriteAlign;
	}
	
	public float getSpriteRotation(){
		return spriteRotation;
	}
}