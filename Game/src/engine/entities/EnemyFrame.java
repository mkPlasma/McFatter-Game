package engine.entities;

import engine.graphics.Sprite;

/*
 * 		EnemyFrame.java
 * 		
 * 		Purpose:	Holds enemy sprite/hitbox size data. Used for presets.
 * 		Notes:		
 * 		
 */
public class EnemyFrame extends EntityFrame{
	
	private final int hitboxSize;
	
	public EnemyFrame(int type, Sprite sprite, int hitboxSize, boolean spriteAlign, float spriteRotation){
		super(type, sprite, spriteAlign, spriteRotation);
		this.hitboxSize = hitboxSize;
	}
	
	public int getHitboxSize(){
		return hitboxSize;
	}
}
