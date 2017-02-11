package engine;

import java.awt.image.BufferedImage;

public class SpriteCache{
	
	// Size of sprite and image caches
	private static final int cacheSize = 128;
	
	// Index of next sprite (how many sprites are cached)
	private static int index = 0;
	
	// Caches sprite objects and images
	private static Sprite[] sprites = new Sprite[cacheSize];
	private static BufferedImage[] images = new BufferedImage[cacheSize];
	
	// Cache/load sprite
	public static Sprite loadSprite(Sprite sprite){
		
		int i = spriteExists(sprite);
		
		if(i == -1){// If not cached
			sprites[index] = sprite;
			images[index] = sprite.getImg();
			
			index++;
			
			return sprite;
		}
		if(!sprites[i].isLoaded())// If cached
			images[index] = sprite.getImg();
		
		return sprites[i];
	}
	
	// Loads sprite if necessary, then returns it
	public static BufferedImage getImage(Sprite sprite){
		
		int i = spriteExists(sprite);
		
		if(i > -1){// If cached
			if(!sprites[i].isLoaded())
				images[i] = sprites[i].getImg();
			
			return images[i];
		}
		
		sprites[index] = sprite;
		images[index] = sprite.getImg();
		
		index++;
		
		return images[index - 1];
	}
	
	// Return index of sprite if sprite object is already cached, -1 otherwise
	private static int spriteExists(Sprite sprite){
		for(int i = 0; i < sprites.length; i++)
			if(sprite.isEqual(sprites[i]))
				return i;
		
		return -1;
	}
}