package content;

import engine.entities.BulletFrame;
import engine.entities.EffectFrame;
import engine.entities.EnemyFrame;
import engine.graphics.TextureCache;

/*
 * 		FrameList.java
 * 		
 * 		Purpose:	Holds instances of other frame lists.
 * 		Notes:		
 * 		
 */

public class FrameList{

	public static final byte
		COLOR_RED			= 0,
		COLOR_DARK_RED		= 1,
		COLOR_ORANGE		= 2,
		COLOR_YELLOW		= 3,
		COLOR_GOLD			= 4,
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
		COLOR_BLACK			= 15;
	
	public static final String[] colors = {
		"red",
		"dark_red",
		"orange",
		"yellow",
		"gold",
		"green",
		"dark_green",
		"cyan",
		"light_blue",
		"blue",
		"dark_blue",
		"purple",
		"pink",
		"white",
		"gray",
		"black"
	};
	
	public final BulletList bulletList;
	public final EffectList effectList;
	public final EnemyList enemyList;
	
	public FrameList(TextureCache tc){
		bulletList = new BulletList(tc);
		effectList = new EffectList(tc);
		enemyList = new EnemyList(tc);
	}
	
	public BulletFrame getBullet(int type, int color){
		return bulletList.get(type, color);
	}
	
	public EffectFrame getEffect(int type, int color){
		return effectList.get(type, color, 1);
	}
	
	public EffectFrame getEffect(int type, int color, int animSpd){
		return effectList.get(type, color, animSpd);
	}
	
	public EnemyFrame getEnemy(int type){
		return enemyList.get(type);
	}
	
	public static int getVarNum(String var){
		
		// Bullet types
		for(int i = 0; i < BulletList.types.length; i++)
			if(var.equals("_" + BulletList.types[i]))
				return i;
		
		// Colors
		for(int i = 0; i < colors.length; i++){
			
			// Standard
			if(var.equals("_" + colors[i]))
				return i;
			
			// Dark
			if(var.equals("_" + colors[i] + "_d"))
				return i + 16;
		}
		
		return -1;
	}
}
