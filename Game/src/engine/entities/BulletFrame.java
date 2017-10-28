package engine.entities;

import engine.graphics.Sprite;

/*
 * 		BulletFrame.java
 * 		
 * 		Purpose:	Holds bullet data, such as sprite, hitboxSize, etc.
 * 					Essentially, stores a bullet preset.
 * 		Notes:		Used by Lasers as well.
 * 		
 */

public class BulletFrame extends EntityFrame{
	
	private final boolean spriteRotationBySpd;
	
	// Bullet color
	private final int color;
	
	private final int hitboxSize;
	
	// Crops the hitbox at the ends of lasers, value should be >0 and <1
	private final float hbLengthCrop;
	
	public BulletFrame(int type, int color, Sprite sprite, int hitboxSize, float hbLengthCrop, boolean spriteAlign, float spriteRotation, boolean spriteRotationBySpd){
		super(type, sprite, spriteAlign, spriteRotation);
		this.color = color;
		this.hitboxSize = hitboxSize;
		this.hbLengthCrop = hbLengthCrop;
		this.spriteRotationBySpd = spriteRotationBySpd;
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
	
	public boolean getSpriteRotationBySpd(){
		return spriteRotationBySpd;
	}
}
