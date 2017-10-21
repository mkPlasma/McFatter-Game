package engine.entities;

import engine.graphics.Sprite;
import engine.graphics.SpriteCache;

/*
 * 		EnemyFrame.java
 * 		
 * 		Purpose:	Holds enemy sprite/hitbox size data. Used for presets.
 * 		Notes:		Currently unfinished.
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */
public class EnemyFrame extends EntityFrame{
	
	//static Sprite s = new Sprite("Game/res/img/bullets/01.png", 416, 0, 32, 32, 2);
	
	public EnemyFrame(){
		super(0, null, false, 0, false);
		//s = SpriteCache.cache(s);
	}
	
	public EnemyFrame(int type, Sprite sprite, boolean spriteAlign, float spriteRotation, boolean spriteRotationBySpd){
		super(type, sprite, spriteAlign, spriteRotation, spriteRotationBySpd);
	}
	
}
