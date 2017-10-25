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
		TYPE_ORB			= 0,
		TYPE_SCALE			= 1,
		TYPE_CRYSTAL		= 2,
		TYPE_RICE			= 3,
		TYPE_STAR			= 4,
		TYPE_STAR4			= 5,
		TYPE_PLUS			= 6,
		TYPE_WALL			= 7,
		TYPE_NEEDLE			= 8,
		TYPE_MISSILE		= 9,
		TYPE_MINE			= 10,
		TYPE_LASER			= 13,
		TYPE_LASER_DIST		= 14,
		TYPE_LASER_HELIX	= 15;
	
	public static final byte
		COLOR_RED			= 0,
		COLOR_DARK_RED		= 1,
		COLOR_ORANGE		= 2,
		COLOR_YELLOW		= 3,
		COLOR_DARK_YELLOW	= 4,
		COLOR_GREEN			= 5,
		COLOR_DARK_GREEN	= 6,
		COLOR_CYAN			= 7,
		COLOR_LIGHT_BLUE	= 8,
		COLOR_BLUE			= 9,
		COLOR_DARK_BLUE		= 10,
		COLOR_PURPLE		= 11,
		COLOR_PINK			= 12,
		COLOR_WHITE			= 13,
		COLOR_GRAY			= 14,
		COLOR_BLACK			= 15,
		COLOR_RED_D			= 16,
		COLOR_DARK_RED_D	= 17,
		COLOR_ORANGE_D		= 18,
		COLOR_YELLOW_D		= 19,
		COLOR_DARK_YELLOW_D	= 20,
		COLOR_GREEN_D		= 21,
		COLOR_DARK_GREEN_D	= 22,
		COLOR_CYAN_D		= 23,
		COLOR_LIGHT_BLUE_D	= 24,
		COLOR_BLUE_D		= 25,
		COLOR_DARK_BLUE_D	= 26,
		COLOR_PURPLE_D		= 27,
		COLOR_PINK_D		= 28,
		COLOR_WHITE_D		= 29,
		COLOR_GRAY_D		= 30,
		COLOR_BLACK_D		= 31;
	
	
	// Cache and index of current bullet frame cache
	private BulletFrame[] cache = new BulletFrame[256];
	private int index;
	
	private SpriteCache spriteCache;
	
	public BulletList(SpriteCache spriteCache){
		this.spriteCache = spriteCache;
	}
	
	public BulletFrame get(int type, int color){
		
		for(int i = 0; i < index; i++)
			if(cache[i].getType() == type && cache[i].getColor() == color)
				return cache[i];
		
		cache[index] = new BulletFrame(type, color, getSprite(type, color), getHitboxSize(type), getHBLengthCrop(type), getSpriteAlign(type), getSpriteRotation(type), getSpriteRotationBySpd(type));
		index++;
		
		return cache[index - 1];
	}
	
	public Sprite getSprite(int type, int color){
		String path = "bullets/b32.png";
		int size = 32;
		
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
		
		sprite = spriteCache.cache(sprite);
		
		return sprite;
	}
	
	public int getHitboxSize(int type){
		switch(type){
			
			case TYPE_ORB:
			case TYPE_SCALE:
			case TYPE_CRYSTAL:
			case TYPE_RICE:
			case TYPE_STAR:
			case TYPE_STAR4:
			case TYPE_PLUS:
			case TYPE_WALL:
			case TYPE_NEEDLE:
			case TYPE_MISSILE:
				return 3;
			
			case TYPE_MINE:
			case TYPE_LASER:
			case TYPE_LASER_DIST:
			case TYPE_LASER_HELIX:
				return 5;
			
			default:
				return 0;
		}
	}
	
	public float getHBLengthCrop(int type){
		switch(type){
			
			case TYPE_ORB:
			case TYPE_SCALE:
			case TYPE_CRYSTAL:
			case TYPE_RICE:
			case TYPE_STAR:
			case TYPE_STAR4:
			case TYPE_PLUS:
			case TYPE_WALL:
			case TYPE_NEEDLE:
			case TYPE_MISSILE:
				return .8f;
				
			case TYPE_MINE:
			case TYPE_LASER:
			case TYPE_LASER_DIST:
			case TYPE_LASER_HELIX:
				return .9f;
			
			default:
				return 0;
		}
	}
	
	
	public boolean getSpriteAlign(int type){
		
		return	type == TYPE_SCALE		||
				type == TYPE_CRYSTAL	||
				type == TYPE_RICE		||
				type == TYPE_NEEDLE		||
				type == TYPE_MISSILE	||
				type == TYPE_WALL		||
				type == TYPE_LASER		||
				type == TYPE_LASER_DIST	||
				type == TYPE_LASER_HELIX;
	}
	
	public float getSpriteRotation(int type){
		switch(type){
		
			case TYPE_STAR:
			case TYPE_STAR4:
			case TYPE_PLUS:
				return 2;
			
			case TYPE_MINE:
				return -0.05f;
			
			default:
				return 0;
		}
	}
	
	public boolean getSpriteRotationBySpd(int type){
		switch(type){
			
			case TYPE_MINE:
				return true;
			
			default:
				return false;
		}
	}
}
