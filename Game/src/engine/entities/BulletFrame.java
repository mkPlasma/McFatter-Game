package engine.entities;

import engine.graphics.Sprite;

public class BulletFrame extends EntityFrame{
	
	// Stores information about the bullet including sprite and hitbox size
	// Used to create bullet presets
	// Ultimately, all the settings depend on the sprite type
	
	// Bullet color
	private final int color;
	
	private final int hitboxSize;
	
	// Crops the hitbox at the ends of lasers
	private final float hbLengthCrop;
	
	public BulletFrame(int type, int color, Sprite sprite, int hitboxSize, float hbLengthCrop, boolean spriteAlign, float spriteRotationBySpd){
		super(type, sprite, spriteAlign, spriteRotationBySpd);
		this.color = color;
		this.hitboxSize = hitboxSize;
		this.hbLengthCrop = hbLengthCrop;
	}
	
	public int getColor(){
		return color;
	}
	
	public int getHitboxSize(){
		return hitboxSize;
	}
	
	public float getHBLengthCrop(){
		return hbLengthCrop;
	}
}
