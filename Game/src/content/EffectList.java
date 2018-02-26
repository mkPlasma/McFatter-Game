package content;

import engine.entities.EffectFrame;
import engine.graphics.Animation;
import engine.graphics.Sprite;
import engine.graphics.TextureCache;

/**
 * 
 * Contains list of all effect types and properties.
 * Also generates EffectFrame objects.
 * 
 * @author Daniel
 *
 */

public class EffectList{

	public static final byte
		TYPE_FLARE	= 0,
		TYPE_CLOUD	= 1;
	
	private TextureCache tc;
	
	public EffectList(TextureCache tc){
		this.tc = tc;
	}
	
	public EffectFrame get(int type, int color, int animSpd){
		return new EffectFrame(type, getSprite(type, color, animSpd), false, 0, animSpd*8);
	}
	
	private Sprite getSprite(int type, int color, int animSpd){
		// Path, sprite size
		String path = "effects.png";
		int size = 32;
		
		int sx = color*size;
		int sy = type*(size*8);
		
		// Create sprite
		Sprite sprite = new Sprite(path, sx, sy, size, size);
		
		if(type == TYPE_CLOUD){
			sprite.setScale(2);
			sprite.addAnimation(new Animation(Animation.ANIM_SET_SPRITE, animSpd, false, new float[]{sx, sy, 0, 32, 8, sx, sy}));
			sprite.addAnimation(new Animation(Animation.ANIM_SCALE, 1, false, 0.1f/animSpd, 1, 5));
		}
		
		tc.loadSprite(sprite);
		
		return sprite;
	}
}
