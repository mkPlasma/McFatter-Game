package engine.entities;

import engine.graphics.Sprite;
import engine.graphics.TextureCache;

/*
 * 		Text.java
 * 		
 * 		Purpose:	Single character, drawn to screen
 * 		Notes:		
 * 		
 */

public class Text extends GameEntity{
	
	private int lifetime;
	
	public Text(float x, float y, char c, int lifetime, TextureCache tc){
		super(null, x, y);
		this.lifetime = lifetime;
		
		c -= 32;
		
		Sprite sprite = new Sprite("font.png", (c % 16)*16, (c/16)*32, 16, 32);
		tc.loadSprite(sprite);
		
		frame = new EntityFrame(0, sprite, false, 0);
	}
	
	public void update(){
		time++;
		
		if(lifetime > 0 && time >= lifetime)
			deleted = true;
	}
}
