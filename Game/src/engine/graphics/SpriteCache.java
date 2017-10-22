package engine.graphics;

import java.util.ArrayList;

/*
 * 		SpriteCache.java
 * 		
 * 		Purpose:	Caches sprites.
 * 		Notes:		Be sure to always cache your sprites!
 * 					Not caching sprites could result in
 * 					ridiculous lag and memory usage.
 * 					
 * 					Use cache(s), which automatically returns
 * 					a loaded (or previously cached) sprite object.
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */

public class SpriteCache{
	
	private short maxSize = 512;
	
	private ArrayList<Sprite> sprites = new ArrayList<Sprite>();
	
	// Caches sprite or returns cache in sprite
	// Use when loading sprites
	
	public Sprite cache(Sprite spr){
		
		int i = getSpriteIndex(spr);
		
		// Return sprite in cache if it exists
		if(i > -1)
			return sprites.get(i);
		
		i = getTextureIndex(spr);
		
		// Sets the texture if it is the same
		if(i > -1){
			Sprite s = sprites.get(i);
			
			spr.setTexture(s.getTexture());
			spr.setTextureID(s.getTextureID());
			spr.setTexWidth(s.getTexWidth());
			spr.setTexHeight(s.getTexHeight());
			spr.setComp(s.getComp());
			
			spr.genTextureCoords();
		}
		else if(!spr.isLoaded())// Load it otherwise, if neccessary
			spr.load();
		
		// Cache sprite
		sprites.add(spr);
		
		if(sprites.size() > maxSize)
			throw new RuntimeException("Sprite count exceeded cache size of " + maxSize);
		
		// Return modified sprite
		return spr;
	}
	
	// Returns index of sprite in cache
	// -1 if not cached
	private int getSpriteIndex(Sprite spr){
		for(int i = 0; i < sprites.size(); i++)
			if(sprites.get(i).equals(spr))
				return i;
		return -1;
	}

	// Returns index of sprite texture in cache
	// -1 if not cached
	private int getTextureIndex(Sprite spr){
		for(int i = 0; i < sprites.size(); i++)
			if(sprites.get(i).getPath().equals(spr.getPath()))
				return i;
		return -1;
	}
}