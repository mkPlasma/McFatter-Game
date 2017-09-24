package content;

import engine.entities.BulletFrame;
import engine.graphics.Animation;
import engine.graphics.Sprite;
import engine.graphics.SpriteCache;

/*
 * 		BulletList.java
 * 		
 * 		Purpose:	Generates BulletFrame objects for creating bullet patterns.
 * 		Notes:		Will be replaced by external definitions.
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */

public class BulletList{
	
	// Generates and caches BulletFrame objects
	
	public static final byte
		TYPE_ORB_M =			0,
		TYPE_SCALE =			1,
		TYPE_STAR4 =			2,
		TYPE_CRYSTAL =		3,
		TYPE_MISSILE =		4,
		TYPE_MINE =			5,
		TYPE_PLUS =			6,
		TYPE_WALL =			7,
		TYPE_ORB_M_DARK =	8,
		TYPE_SCALE_DARK =	9,
		TYPE_STAR4_DARK =	10,
		TYPE_CRYSTAL_DARK =	11,
		TYPE_MISSILE_DARK =	12,
		TYPE_MINE_DARK =		13,
		TYPE_PLUS_DARK =		14,
		TYPE_WALL_DARK =		15,
		TYPE_LASER =			16;
	
	public static final byte
		COLOR_RED =			0,
		COLOR_DARK_RED =		1,
		COLOR_ORANGE =		2,
		COLOR_YELLOW =		3,
		COLOR_DARK_YELLOW =	4,
		COLOR_GREEN =		5,
		COLOR_DARK_GREEN =	6,
		COLOR_CYAN =			7,
		COLOR_LIGHT_BLUE =	8,
		COLOR_BLUE =			9,
		COLOR_DARK_BLUE =	10,
		COLOR_PURPLE =		11,
		COLOR_PINK =			12,
		COLOR_WHITE =		13,
		COLOR_GRAY =			14,
		COLOR_BLACK =		15;
	
	
	// Cache and index of current bullet frame cache
	private static BulletFrame[] cache = new BulletFrame[256];
	private static int index;
	
	public static BulletFrame get(byte type, byte color){
		
		for(int i = 0; i < index; i++)
			if(cache[i].getType() == type && cache[i].getColor() == color)
				return cache[i];
		
		cache[index] = new BulletFrame(type, color, getSprite(type, color), getHitboxSize(type), getHBLengthCrop(type), getSpriteAlign(type), getSpriteRotation(type), getSpriteRotationBySpd(type));
		index++;
		
		return cache[index - 1];
	}
	
	public static Sprite getSprite(byte type, byte color){
		String path = "01.png";
		int size = 32;
		
		path = "Game/res/img/bullets/" + path;
		
		if(type >= 8 && type < 16){
			color += 16;
			type -= 8;
		}
		
		// Standard sprite rotation will be stored in an animation
		float rotation = getSpriteRotation(type)*(color%2 == 0 ? 1 : -1);
		boolean spdRotation = getSpriteRotationBySpd(type);
		
		Sprite sprite;
		
		if(rotation == 0)
			sprite = new Sprite(path, color*size, type*size, size, size);
		else if(spdRotation)
			sprite = new Sprite(path, color*size, type*size, size, size, new Animation(Animation.ANIM_ROTATION_BY_SPD,  1, false, new float[]{rotation}));
		else
			sprite = new Sprite(path, color*size, type*size, size, size, new Animation(Animation.ANIM_ROTATION, 1, false, new float[]{rotation}));
		
		sprite = SpriteCache.cache(sprite);
		
		return sprite;
	}
	
	public static int getHitboxSize(byte type){
		switch(type){
			case TYPE_SCALE: case TYPE_STAR4: case TYPE_CRYSTAL: case TYPE_MISSILE: case TYPE_PLUS: case TYPE_WALL:
			case TYPE_SCALE_DARK: case TYPE_STAR4_DARK: case TYPE_CRYSTAL_DARK: case TYPE_MISSILE_DARK: case TYPE_PLUS_DARK: case TYPE_WALL_DARK:
				return 3;
			case TYPE_ORB_M: case TYPE_ORB_M_DARK:
				return 4;
			case TYPE_MINE: case TYPE_MINE_DARK: case TYPE_LASER:
				return 5;
			default:
				return 0;
		}
	}
	
	public static float getHBLengthCrop(byte type){
		switch(type){
			case TYPE_SCALE: case TYPE_STAR4: case TYPE_CRYSTAL: case TYPE_MISSILE: case TYPE_PLUS: case TYPE_WALL:
			case TYPE_SCALE_DARK: case TYPE_STAR4_DARK: case TYPE_CRYSTAL_DARK: case TYPE_MISSILE_DARK: case TYPE_PLUS_DARK: case TYPE_WALL_DARK:
				return .8f;
			case TYPE_ORB_M: case TYPE_MINE: case TYPE_ORB_M_DARK: case TYPE_MINE_DARK: case TYPE_LASER:
				return .9f;
			default:
				return 0;
		}
	}
	
	
	public static boolean getSpriteAlign(byte type){
		return	type == TYPE_SCALE			||
				type == TYPE_CRYSTAL			||
				type == TYPE_MISSILE			||
				type == TYPE_WALL			||
				type == TYPE_SCALE_DARK		||
				type == TYPE_CRYSTAL_DARK	||
				type == TYPE_MISSILE_DARK	||
				type == TYPE_WALL_DARK		||
				type == TYPE_LASER;
	}
	
	public static float getSpriteRotation(byte type){
		switch(type){
			case TYPE_STAR4: case TYPE_PLUS: case TYPE_STAR4_DARK: case TYPE_PLUS_DARK:
				return 2;
			case TYPE_MINE: case TYPE_MINE_DARK:
				return -0.05f;
			default:
				return 0;
		}
	}
	
	public static boolean getSpriteRotationBySpd(byte type){
		switch(type){
			case TYPE_MINE: case TYPE_MINE_DARK:
				return true;
			default:
				return false;
		}
	}
}
