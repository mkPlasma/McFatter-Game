package engine.entities;

import engine.graphics.Sprite;

public class EntityFrame{
	
	// Stores presets for effects/bullets
	
	private final int type;
	
	private final Sprite sprite;
	
	// If true, sprite will rotate to the direction it's moving in
	private final boolean spriteAlign;
	
	// Rotation
	private final float spriteRotation;
	private final boolean spriteRotationBySpd;
	
	public EntityFrame(int type, Sprite sprite, boolean spriteAlign, float spriteRotation, boolean spriteRotationBySpd){
		this.type = type;
		this.sprite = sprite;
		this.spriteAlign = spriteAlign;
		this.spriteRotation = spriteRotation;
		this.spriteRotationBySpd = spriteRotationBySpd;
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
	
	public boolean getSpriteRotationBySpd(){
		return spriteRotationBySpd;
	}
}