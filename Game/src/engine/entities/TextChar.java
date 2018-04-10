package engine.entities;

import engine.graphics.Sprite;
import engine.graphics.TextureCache;

/**
 * 
 * Single drawable character entity.
 * 
 * @author Daniel
 *
 */

public class TextChar extends GameEntity{
	
	private final Sprite sprite;
	
	public TextChar(float x, float y, char c, float scale, TextureCache tc){
		super(x, y);
		
		c -= 32;
		
		sprite = new Sprite("font.png", (c % 16)*16, (c/16)*32, 16, 32);
		sprite.setScale(scale);
		tc.loadSprite(sprite);
	}
	
	public void update(){
		
	}
	
	public Sprite getSprite(){
		return sprite;
	}
}
