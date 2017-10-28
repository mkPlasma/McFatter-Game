package content;

import engine.entities.EffectFrame;
import engine.graphics.Animation;
import engine.graphics.Sprite;
import engine.graphics.TextureCache;

/*
 * 		EffectList.java
 * 		
 * 		Purpose:	Holds presets for effects.
 * 		Notes:		
 * 		
 */

public class EffectList{

	public static final byte
		TYPE_CLOUD			= 0;
	
	private static final int animSpd = 1;
	
	private TextureCache tc;
	
	public EffectList(TextureCache tc){
		this.tc = tc;
	}
	
	public EffectFrame get(int type, int color){
		return new EffectFrame(type, getSprite(type, color), false, 0, animSpd*8);
	}
	
	private Sprite getSprite(int type, int color){
		// Path, sprite size
		String path = "effects.png";
		int size = 32;
		
		int sx = color*size + (type % 2 == 0 ? 0 : 512);
		int sy = (type/2)*(size*8);
		
		
		// Create sprite
		Sprite sprite = new Sprite(path, sx, sy, size, size);
		sprite.setScale(2);
		
		// Animations
		sprite.addAnimation(new Animation(Animation.ANIM_SET_SPRITE, animSpd, false, new float[]{sx, sy, 0, 32, 8, sx, sy}));
		sprite.addAnimation(new Animation(Animation.ANIM_ALPHA, 1, false, new float[]{-1f, 0, 1}));
		
		tc.loadSprite(sprite);
		
		return sprite;
	}
}
