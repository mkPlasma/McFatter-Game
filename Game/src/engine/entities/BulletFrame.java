package engine.entities;

import engine.graphics.Sprite;

/*
 * 		BulletFrame.java
 * 		
 * 		Purpose:	Holds bullet data, such as sprite, hitboxSize, etc.
 * 					Essentially, stores a bullet preset.
 * 		Notes:		Used by Lasers as well.
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */

public class BulletFrame extends EntityFrame{
	
	// Bullet color
	private final int color;
	
	private final int hitboxSize;
	
	// Crops the hitbox at the ends of lasers, value should be >0 and <1
	private final float hbLengthCrop;
	
	public BulletFrame(int type, int color, Sprite sprite, int hitboxSize, float hbLengthCrop, boolean spriteAlign, float spriteRotation, boolean spriteRotationBySpd){
		super(type, sprite, spriteAlign, spriteRotation, spriteRotationBySpd);
		this.color = color;
		this.hitboxSize = hitboxSize;
		this.hbLengthCrop = hbLengthCrop;
	}
	
	public float[] getRGB(){
		switch(color){
			case 0:
				return new float[]{1, 0, 0};
			case 1:
				return new float[]{.25f, 0, 0};
			case 2:
				return new float[]{1, .5f, 0};
			case 3:
				return new float[]{1, 1, 0};
			case 4:
				return new float[]{.25f, .25f, 0};
			case 5:
				return new float[]{0, 1, 0};
			case 6:
				return new float[]{0, .25f, 0};
			case 7:
				return new float[]{0, 1, 1};
			case 8:
				return new float[]{0, .5f, 1};
			case 9:
				return new float[]{0, 0, 1};
			case 10:
				return new float[]{0, 0, .25f};
			case 11:
				return new float[]{.5f, 0, 1};
			case 12:
				return new float[]{1, 0, 1};
			case 13:
				return new float[]{1, 1, 1};
			case 14:
				return new float[]{.215f, .215f, .215f};
			case 15:
				return new float[]{0, 0, 0};
			default:
				return new float[]{0, 0, 0};
		}
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
