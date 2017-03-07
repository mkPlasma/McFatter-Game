package engine.graphics;

import java.awt.image.BufferedImage;

public class SpriteCache{
	
	// Size of sprite and image caches
	private static final short cacheSize = 256;
	private static final short srcCacheSize = 64;
	
	// Index of next sprite (how many sprites are cached)
	private static int index = 0;
	
	// Index of next source image
	private static int srcIndex = 0;
	
	// Caches for sprite objects and images
	private static Sprite[] sprites = new Sprite[cacheSize];
	//private static BufferedImage[] images = new BufferedImage[cacheSize];
	
	// Caches for source images (sprite sheets)
	private static String[] srcImgPaths = new String[srcCacheSize];
	private static BufferedImage[] srcImages = new BufferedImage[srcCacheSize];
	
	// Caches and loads sprite and image
	// Make sure never to return the original sprite, always return sprite from cache
	
	public static Sprite cacheSprite(Sprite sprite){
		
		int i = spriteExists(sprite);
		
		if(i == -1){// If not cached or loaded
			sprites[index] = sprite;
			loadSprite(sprites[index]);
			
			index++;
			
			return sprites[index - 1];
		}
		
		if(!sprites[i].isLoaded())// If cached and not loaded
			loadSprite(sprites[i]);
		
		return sprites[i];
	}
	
	// Loads the sprite image and caches the source image
	private static void loadSprite(Sprite sprite){
		
		if(sprite.isSrcLoaded()){// If source image is loaded
			sprite.load();
			return;
		}
		
		int i = srcImgExists(sprite);
		
		if(i > -1){// If source image is cached
			sprite.setSrcImg(srcImages[i]);
			sprite.load();
			
			return;
		}
		
		// If source image is not cached
		
		sprite.load();
		
		srcImgPaths[srcIndex] = sprite.getPath();
		srcImages[srcIndex] = sprite.getSrcImg();
		
		srcIndex++;
	}
	
	// Loads sprite if necessary, then returns it
	// Currently unused
	/*
	public static BufferedImage getImage(Sprite sprite){
		
		int i = spriteExists(sprite);
		
		if(i > -1){// If cached
			if(!sprites[i].isLoaded()){
				System.out.println("Loaded");
				images[i] = sprites[i].getImg();
			}
			
			return images[i];
		}
		
		sprites[index] = sprite;
		images[index] = sprite.getImg();
		
		index++;
		
		return images[index - 1];
	}
	*/
	
	// Returns index of sprite if sprite object is already cached, -1 otherwise
	private static int spriteExists(Sprite sprite){
		for(int i = 0; i < index; i++)
			if(sprite.isEqual(sprites[i]))
				return i;
		
		return -1;
	}

	// Returns index of source image if already cached, -1 otherwise
	private static int srcImgExists(Sprite sprite){
		for(int i = 0; i < srcIndex; i++)
			if(srcImgPaths[i].equals(sprite.getPath()))
				return i;
		
		return -1;
	}
}