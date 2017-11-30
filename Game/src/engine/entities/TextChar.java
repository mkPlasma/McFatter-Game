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

public class TextChar extends GameEntity{
	
	public TextChar(float x, float y, char c, float scale, TextureCache tc){
		super(null, x, y);
		
		c -= 32;
		
		Sprite sprite = new Sprite("font.png", (c % 16)*16, (c/16)*32, 16, 32);
		sprite.setScale(scale);
		tc.loadSprite(sprite);
		
		frame = new EntityFrame(0, sprite, false, 0);
	}
	
	public void update(){
		
	}
}
