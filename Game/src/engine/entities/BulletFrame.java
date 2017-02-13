package engine.entities;

import engine.graphics.Sprite;

public class BulletFrame{

	private final Sprite sprite;
	
	private final int hitboxSize;
	
	// If true, sprite will rotate to the direction it's moving in
	private final boolean spriteAlign;
	// Rotate sprite over time
	private final float spriteRotation;
	// Rotate according to speed
	private final boolean spriteRotationBySpd;
	
	
	public BulletFrame(Sprite sprite, int hitboxSize, boolean spriteAlign, float spriteRotation, boolean spriteRotationBySpd){
		this.sprite = sprite;
		this.hitboxSize = hitboxSize;
		this.spriteAlign = spriteAlign;
		this.spriteRotation = spriteRotation;
		this.spriteRotationBySpd = spriteRotationBySpd;
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
