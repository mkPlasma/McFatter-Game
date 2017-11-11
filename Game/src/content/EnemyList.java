package content;

import engine.entities.EnemyFrame;
import engine.graphics.Sprite;
import engine.graphics.TextureCache;

/*
 * 		EffectList.java
 * 		
 * 		Purpose:	Holds presets for effects.
 * 		Notes:		
 * 		
 */

public class EnemyList{

	public static final byte
		TYPE_STANDARD	= 0;
	
	private TextureCache tc;
	
	public EnemyList(TextureCache tc){
		this.tc = tc;
	}
	
	public EnemyFrame get(int type){
		return new EnemyFrame(type, getSprite(type), false, 0);
	}
	
	private Sprite getSprite(int type){
		
		// Path, sprite size
		String path = "enemies.png";
		int size = 64;
		
		int sx = 0;
		int sy = 0;
		
		// Create sprite
		Sprite sprite = new Sprite(path, sx, sy, size, size);
		
		tc.loadSprite(sprite);
		
		return sprite;
	}
}
