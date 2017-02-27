package content;

import engine.entities.BulletFrame;
import engine.graphics.Sprite;
import engine.graphics.SpriteCache;

public class BulletSheet{
	
	// Generates and caches Bullet Frame objects
	
	public static final byte
		TYPE_ORB_M =		0,
		TYPE_SCALE =		1,
		TYPE_STAR4 =		2,
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
		TYPE_MINE_DARK =	13,
		TYPE_PLUS_DARK =	14,
		TYPE_WALL_DARK =	15;
	
	public static final byte
		COLOR_RED =			0,
		COLOR_DARK_RED =	1,
		COLOR_ORANGE =		2,
		COLOR_YELLOW =		3,
		COLOR_DARK_YELLOW =	4,
		COLOR_GREEN =		5,
		COLOR_DARK_GREEN =	6,
		COLOR_CYAN =		7,
		COLOR_LIGHT_BLUE =	8,
		COLOR_BLUE =		9,
		COLOR_DARK_BLUE =	10,
		COLOR_PURPLE =		11,
		COLOR_PINK =		12,
		COLOR_WHITE =		13,
		COLOR_GRAY =		14,
		COLOR_BLACK =		15;
	
	
	// Cache and index of current bullet frame cache
	private static BulletFrame[] cache = new BulletFrame[256];
	private static int index;
	
	public static BulletFrame get(byte type, byte color){
		
		for(int i = 0; i < index; i++)
			if(cache[i].getType() == type && cache[i].getColor() == color)
				return cache[i];
		
		cache[index] = new BulletFrame(type, color, getSprite(type, color), getHitboxSize(type), getSpriteAlign(type), getSpriteRotation(type), getSpriteRotationBySpd(type));
		index++;
		
		return cache[index - 1];
	}
	
	public static Sprite getSprite(byte type, byte color){
		String path = "Game/res/img/bullets/";
		int size = 32;
		
		if(type <= 7)
			path += "01.png";
		else if(type <= 15){
			path += "01b.png";
			type -= 8;
		}
		
		Sprite sprite = new Sprite(path, color*size, type*size, size, size);
		sprite = SpriteCache.cacheSprite(sprite);
		
		return sprite;
	}
	
	public static int getHitboxSize(byte type){
		switch(type){
			case TYPE_SCALE: case TYPE_STAR4: case TYPE_CRYSTAL: case TYPE_MISSILE: case TYPE_PLUS: case TYPE_WALL:
			case TYPE_SCALE_DARK: case TYPE_STAR4_DARK: case TYPE_CRYSTAL_DARK: case TYPE_MISSILE_DARK: case TYPE_PLUS_DARK: case TYPE_WALL_DARK:
				return 3;
			case TYPE_ORB_M: case TYPE_MINE: case TYPE_ORB_M_DARK: case TYPE_MINE_DARK:
				return 5;
		}
		
		return 0;
	}
	
	public static boolean getSpriteAlign(byte type){
		return	type == TYPE_SCALE			||
				type == TYPE_CRYSTAL		||
				type == TYPE_MISSILE		||
				type == TYPE_WALL			||
				type == TYPE_SCALE_DARK		||
				type == TYPE_CRYSTAL_DARK	||
				type == TYPE_MISSILE_DARK	||
				type == TYPE_WALL_DARK;
	}
	
	public static float getSpriteRotation(byte type){
		switch(type){
			case TYPE_STAR4: case TYPE_PLUS: case TYPE_STAR4_DARK: case TYPE_PLUS_DARK:
				return 2;
			case TYPE_MINE: case TYPE_MINE_DARK:
				return -0.1f;
			default:
				return 0;
		}
	}
	
	public static boolean getSpriteRotationBySpd(byte type){
		return	type == TYPE_MINE		||
				type == TYPE_MINE_DARK;
	}
}
