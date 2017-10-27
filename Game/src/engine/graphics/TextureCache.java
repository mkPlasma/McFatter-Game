package engine.graphics;

import java.util.ArrayList;

/*
 * 		Texture.java
 * 		
 * 		Purpose:	Caches sprite textures.
 * 		Notes:		Be sure to always cache your sprites!
 * 					Not caching textures could result in
 * 					ridiculous lag and memory usage.
 * 					
 * 					Use cache(spr) which loads the sprite
 * 					or uses a cached texture.
 * 			
 */

public class TextureCache{
	
	private static final short MAX_TEXTURES = 16;
	
	private ArrayList<Texture> textures;
	
	public TextureCache(){
		textures = new ArrayList<Texture>();
	}
	
	// Loads and caches a texture, for manual use
	public Texture cache(String path){
		
		Texture texture = new Texture(path);
		texture.load();
		textures.add(texture);

		if(textures.size() > MAX_TEXTURES){
			System.err.println("Texture cache exceeded " + MAX_TEXTURES);
			textures.remove(textures.size() - 1);
		}
		
		return texture;
	}
	
	// Use when loading sprites
	public void loadSprite(Sprite spr){
		
		Texture t = getCachedTexture(spr.getTexture());
		
		if(t != null){
			spr.setTexture(t);
			spr.genTextureCoords();
		}
		else
			spr.load();
	}
	
	// Returns cached texture if exists
	private Texture getCachedTexture(Texture tex){
		
		for(Texture t:textures)
			if(t.getPath().equalsIgnoreCase(tex.getPath()))
				return t;
		
		return null;
	}
}