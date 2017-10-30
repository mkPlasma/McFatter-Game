package content;

import engine.entities.BulletFrame;
import engine.graphics.Animation;
import engine.graphics.Sprite;
import engine.graphics.TextureCache;

/*
 * 		BulletList.java
 * 		
 * 		Purpose:	Generates BulletFrame objects for bullet presets.
 * 		Notes:		
 * 		
 */

public class BulletList{
	
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
		TYPE_RING			= 9,
		
		TYPE_MISSILE		= 11,
		TYPE_MINE			= 12,
		TYPE_LASER			= 13,
		TYPE_LASER_DIST		= 14,
		TYPE_LASER_HELIX	= 15,
		
		TYPE_ATOM = 18;
	
	public static final String[] types = {
		"orb",
		"scale",
		"crystal",
		"rice",
		"star",
		"star4",
		"plus",
		"wall",
		"needle",
		"ring",
		"?",
		"missile",
		"mine",
		"laser",
		"laser_dist",
		"laser_helix",
		"??",
		"???",
		"atom",
		"????",
	};
	
	public static final byte
		COLOR_RED_D			= 16,
		COLOR_DARK_RED_D	= 17,
		COLOR_ORANGE_D		= 18,
		COLOR_YELLOW_D		= 19,
		COLOR_GOLD_D		= 20,
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
	
	private TextureCache tc;
	
	public BulletList(TextureCache tc){
		this.tc = tc;
	}
	
	public BulletFrame get(int type, int color){
		return new BulletFrame(type, color, getSprite(type, color), getHitboxSize(type), getHBLengthCrop(type),
			getSpriteAlign(type), getSpriteRotation(type), getSpriteRotationBySpd(type));
	}
	
	public Sprite getSprite(int type, int color){
		
		// Path, sprite size
		String path = "01.png";
		int size = 32;
		
		int sx = color*size;
		int sy = type*size;
		
		
		if(type >= 16 && type <= 19){
			path = "02.png";
			
			sx = (color%16)*size;
			sx += type % 2 == 0 ? 0 : 512;
			
			sy = type <= 17 ? 0 : 256;
		}
		
		
		// Create sprite
		Sprite sprite = new Sprite("bullets/" + path, sx, sy, size, size);
		
		
		// Animations
		float rotation = getSpriteRotation(type)*(color%2 == 0 ? 1 : -1);
		boolean spdRotation = getSpriteRotationBySpd(type);
		
		if(spdRotation)
			sprite.addAnimation(new Animation(Animation.ANIM_ROTATION_BY_SPD,  1, false, new float[]{rotation}));
		
		else if(rotation != 0)
			sprite.addAnimation(new Animation(Animation.ANIM_ROTATION, 1, false, new float[]{rotation}));
		
		if(type >= 16 && type <= 19){
			final int spd = 2;
			sprite.addAnimation(new Animation(Animation.ANIM_SET_SPRITE_FLIP, spd, false, new float[]{sx, sy, 0, 32, 8, sx, sy}));
			
			if(type == TYPE_ATOM)
				sprite.addAnimation(new Animation(Animation.ANIM_FLIP_X, 7*spd, false, null));
		}
		
		tc.loadSprite(sprite);
		
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
			case TYPE_ATOM:
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
			case TYPE_ATOM:
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
		
		return	type == TYPE_SCALE			||
				type == TYPE_CRYSTAL		||
				type == TYPE_RICE			||
				type == TYPE_NEEDLE			||
				type == TYPE_MISSILE		||
				type == TYPE_WALL			||
				type == TYPE_LASER			||
				type == TYPE_LASER_DIST		||
				type == TYPE_LASER_HELIX;
	}
	
	public float getSpriteRotation(int type){
		switch(type){
		
			case TYPE_STAR:
			case TYPE_STAR4:
			case TYPE_PLUS:
				return 2;
			
			case TYPE_MINE:
				return -0.5f;
			
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
