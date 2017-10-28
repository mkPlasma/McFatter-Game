package engine.entities;

import engine.graphics.Sprite;
import engine.graphics.TextureCache;

/*
 * 		EnemyFrame.java
 * 		
 * 		Purpose:	Holds enemy sprite/hitbox size data. Used for presets.
 * 		Notes:		
 * 		
 */
public class EnemyFrame extends EntityFrame{
	
	public EnemyFrame(){
		super(0, null, false, 0);
	}
	
	public EnemyFrame(int type, Sprite sprite, boolean spriteAlign, float spriteRotation){
		super(type, sprite, spriteAlign, spriteRotation);
	}
	
}
