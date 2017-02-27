package engine.entities;

import engine.graphics.Sprite;

public class BulletFrame{
	
	// Stores information about the bullet including sprite and hitbox size
	// Used to create bullet presets
	// Ultimately, all the settings depend on the sprite type
	
	// Sprite type and color
	private final int type, color;
	
	private final Sprite sprite;
	
	private final int hitboxSize;
	
	// If true, sprite will rotate to the direction it's moving in
	private final boolean spriteAlign;
	// Rotate sprite over time
	private final float spriteRotation;
	// Rotate according to speed
	private final boolean spriteRotationBySpd;
	
	
	public BulletFrame(int type, int color, Sprite sprite, int hitboxSize, boolean spriteAlign, float spriteRotation, boolean spriteRotationBySpd){
		this.type = type;
		this.color = color;
		this.sprite = sprite;
		this.hitboxSize = hitboxSize;
		this.spriteAlign = spriteAlign;
		this.spriteRotation = spriteRotation;
		this.spriteRotationBySpd = spriteRotationBySpd;
	}
	
	public int getType(){
		return type;
	}
	
	public int getColor(){
		return color;
	}
	
	public Sprite getSprite(){
		return sprite;
	}
	
	public int getHitboxSize(){
		return hitboxSize;
	}
	
	public boolean spriteAlign(){
		return spriteAlign;
	}
	
	public float getSpriteRotation(){
		return spriteRotation;
	}
	
	public boolean spriteRotationBySpd(){
		return spriteRotationBySpd;
	}
}
