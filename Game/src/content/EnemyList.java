package content;

import engine.entities.EnemyFrame;
import engine.graphics.Animation;
import engine.graphics.Sprite;
import engine.graphics.TextureCache;

import static engine.entities.CollidableEntity.*;

/**
 * 
 * Contains list of all enemy types and properties.
 * Also generates EnemyFrame objects.
 * 
 * @author Daniel
 *
 */

public class EnemyList{

	public static final byte
		TYPE_FIGHTER		= 0,
		TYPE_RADIAL		= 1;
	
	
	public static final String[] types = {
		"fighter",
		"radial",
	};
	
	private TextureCache tc;
	
	public EnemyList(TextureCache tc){
		this.tc = tc;
	}
	
	public EnemyFrame get(int type){
		return new EnemyFrame(type, getSprite(type), HITBOX_CIRCLE, 16, 16, 0, type == TYPE_FIGHTER, 0);
	}
	
	private Sprite getSprite(int type){
		
		// Path, sprite size
		String path = "enemies.png";
		int size = 64;
		
		int sx = 0;
		int sy = 0;
		
		switch(type){
			case TYPE_RADIAL:
				sy = 64;
				break;
		}
		
		
		// Create sprite
		Sprite sprite = new Sprite(path, sx, sy, size, size);
		
		if(type == TYPE_RADIAL)
			sprite.addAnimation(new Animation(Animation.ANIM_ROTATION, 1, false, new float[]{15}));
		
		
		tc.loadSprite(sprite);		
		return sprite;
	}
}
