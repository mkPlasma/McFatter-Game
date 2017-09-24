package content;

import engine.entities.EntityFrame;
import engine.graphics.Animation;
import engine.graphics.Sprite;
import engine.graphics.SpriteCache;

/*
 * 		EffectList.java
 * 		
 * 		Purpose:	Generates EntityFrame objects for creating effects. Currently unused.
 * 		Notes:		Will be replaced by external definitions.
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */

public class EffectList{
	
	// Generates and caches EntityFrame objects for effects
	
	public static final byte
		TYPE_ORB_M =		0;
	
	
	// Cache and index of current bullet frame cache
	private static EntityFrame[] cache = new EntityFrame[256];
	private static int index;
	
	public static EntityFrame get(byte type, byte color){
		
		for(int i = 0; i < index; i++)
			if(cache[i].getType() == type)
				return cache[i];
		
		cache[index] = new EntityFrame(type, getSprite(type, color), getSpriteAlign(type), getSpriteRotation(type), getSpriteRotationBySpd(type));
		index++;
		
		return cache[index - 1];
	}
	
	public static Sprite getSprite(byte type, byte color){
		String path = "Game/res/img/effects/01.png";
		int size = 32;
		
		/*
		if(type <= 7)
			path += "01.png";
		else if(type <= 15){
			path += "01b.png";
			type -= 8;
		}
		else if(type <= 32){
			path += "02.png";
			type -= 16;
		}
		*/
		
		// Standard sprite rotation will be stored in an animation
		// Rotation by speed will be handled by the bullet
		float rotation = getSpriteRotation(type);
		
		Sprite sprite;
		
		if(rotation == 0)
			sprite = new Sprite(path, color*size, type*size, size, size);
		else
			sprite = new Sprite(path, color*size, type*size, size, size, new Animation(Animation.ANIM_ROTATION, 1, false, new float[]{rotation}));
		
		sprite = SpriteCache.cache(sprite);
		
		return sprite;
	}
	
	public static boolean getSpriteAlign(byte type){
		return false;
		
		//return	type == TYPE_SCALE			||
		//		type == TYPE_CRYSTAL;
	}
	
	public static float getSpriteRotation(byte type){
		return 0;
		/*switch(type){
			case TYPE_STAR4: case TYPE_PLUS: case TYPE_STAR4_DARK: case TYPE_PLUS_DARK:
				return 2;
			case TYPE_MINE: case TYPE_MINE_DARK:
				return -0.1f;
			default:
				return 0;
		}*/
	}
	
	public static boolean getSpriteRotationBySpd(byte type){
		return false;
		//return	type == TYPE_MINE		||
		//		type == TYPE_MINE_DARK;
	}
}
