package content;

import engine.entities.BulletFrame;
import engine.entities.EffectFrame;
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
	
	private BulletList bulletList;
	private EffectList effectList;
	
	public FrameList(TextureCache tc){
		bulletList = new BulletList(tc);
		effectList = new EffectList(tc);
	}
	
	public BulletFrame getBullet(int type, int color){
		return bulletList.get(type, color);
	}
	
	public EffectFrame getEffect(int type, int color){
		return effectList.get(type, color);
	}
}
