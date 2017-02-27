package engine.graphics;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class Sprite{
	
	// Path of spritesheet
	private final String path;
	
	// Area of spritesheet to use
	private final int x, y, width, height;
	
	// Stores source image (not cropped)
	private BufferedImage srcImg;
	
	// Stores sprite
	private BufferedImage img;
	
	// True if sprite has been loaded
	private boolean loaded, srcLoaded;
	
	public Sprite(String path, int x, int y, int width, int height){
		this.path = path;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	// Load sprite to img
	public void load(){
		
		if(!srcLoaded){
			File imgFile = new File(path);
			
			try{
				srcImg = ImageIO.read(imgFile);
				srcLoaded = true;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		img = srcImg.getSubimage(x, y, width, height);
		
		loaded = true;
	}
	
	public BufferedImage getImg(){
		if(loaded)
			return img;
		
		load();
		return img;
	}
	
	// Return true if two sprites are the same, used in SpriteCache
	public boolean isEqual(Sprite sprite){
		
		if(sprite == null)
			return false;
		
		return sprite.path.equals(path) &&
				sprite.x == x &&
				sprite.y == y &&
				sprite.width == width &&
				sprite.height == height;
	}
	
	public void setSrcImg(BufferedImage srcImg){
		this.srcImg = srcImg;
	}
	
	public BufferedImage getSrcImg(){
		return srcImg;
	}
	
	public boolean isLoaded(){
		return loaded;
	}
	
	public boolean isSrcLoaded(){
		return srcLoaded;
	}
	
	
	public String getPath(){
		return path;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}

	public int[] getPos(){
		return new int[]{x, y};
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}
	
	public int[] getDimensions(){
		return new int[]{width, height};
	}
}