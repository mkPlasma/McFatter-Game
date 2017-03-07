package engine.graphics;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class Sprite{
	
	// Path of spritesheet
	private final String path;
	
	// Number of current sprite users
	private int numUsers;
	
	// Area of spritesheet to use
	private int x, y, width, height;
	
	private float rotation = 0;
	private float scaleX = 1, scaleY = 1;
	private float alpha = 1;
	
	// Stores source image (not cropped)
	private BufferedImage srcImg;
	
	// Stores sprite
	private BufferedImage img;
	
	// True if sprite has been loaded
	private boolean loaded, srcLoaded;

	// Sprite animation
	private Animation[] anim;
	
	public Sprite(Sprite spr){
		path = spr.getPath();
		x = spr.getX();
		y = spr.getY();
		width = spr.getWidth();
		height = spr.getHeight();
		
		rotation = spr.getRotation();
		scaleX = spr.getScaleX();
		scaleY = spr.getScaleY();
		alpha = spr.getAlpha();
		
		anim = spr.getAnimations();
		
		srcImg = spr.getSrcImg();
		img = spr.getImg();
		srcLoaded = true;
		loaded = true;
	}
	
	public Sprite(String path, int x, int y, int width, int height){
		this.path = path;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public Sprite(String path, int x, int y, int width, int height, Animation anim){
		this.path = path;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.anim = new Animation[]{anim};
	}
	
	public Sprite(String path, int x, int y, int width, int height, Animation[] anim){
		this.path = path;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.anim = anim;
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
	
	// Returns the image, loads it if necessary
	public BufferedImage getImg(){
		if(loaded)
			return img;
		
		load();
		return img;
	}
	
	// Returns a modified sprite for the animation at the given time
	public Sprite animate(int time){
		
		if(anim == null)
			return this;
		
		Sprite spr = new Sprite(this);
		
		for(Animation a:spr.getAnimations()){
			a.setSprite(spr);
			a.update(time);
		}
		
		return spr;
	}
	
	// Sets sprite image again if it has been updated by the animation
	public void reload(){
		img = srcImg.getSubimage(x, y, width, height);
	}
	
	// Return true if two sprites are the same, used in SpriteCache
	public boolean isEqual(Sprite sprite){
		
		if(sprite == null)
			return false;
		
		return sprite.path.equals(path) &&
				sprite.x == x &&
				sprite.y == y &&
				sprite.width == width &&
				sprite.height == height &&
				//sprite.rotation == rotation && // Commented out due to animations, may be changed later
				//sprite.scaleX == scaleX &&
				//sprite.scaleY == scaleY &&
				//sprite.alpha == alpha &&
				sprite.anim.equals(anim);
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
	
	public void addUser(){
		numUsers++;
	}
	
	public void removeUser(){
		numUsers--;
	}
	
	public int getNumUsers(){
		return numUsers;
	}
	
	public void setX(int x){
		this.x = x;
	}
	
	public int getX(){
		return x;
	}
	
	public void setY(int y){
		this.y = y;
	}
	
	public int getY(){
		return y;
	}
	
	public void setPos(int[] pos){
		x = pos[0];
		y = pos[1];
	}
	
	public void setPos(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public int[] getPos(){
		return new int[]{x, y};
	}
	
	public void setWidth(int width){
		this.width = width;
	}
	
	public int getWidth(){
		return width;
	}
	
	public void setHeight(int height){
		this.height = height;
	}
	
	public int getHeight(){
		return height;
	}
	
	public void setDimensions(int[] dim){
		width = dim[0];
		height = dim[1];
	}
	
	public int[] getDimensions(){
		return new int[]{width, height};
	}
	
	public void setRotation(float rotation){
		this.rotation = rotation;
	}
	
	public void rotate(float amount){
		rotation += amount;
	}
	
	public float getRotation(){
		return rotation;
	}
	
	public void setScaleX(float scaleX){
		this.scaleX = scaleX;
	}
	
	public float getScaleX(){
		return scaleX;
	}
	
	public void setScaleY(float scaleY){
		this.scaleY = scaleY;
	}
	
	public float getScaleY(){
		return scaleY;
	}
	
	public void setAlpha(float alpha){
		this.alpha = alpha;
	}
	
	public float getAlpha(){
		return alpha;
	}
	
	public void setAnimations(Animation anim){
		this.anim = new Animation[]{anim};
	}
	
	public void setAnimations(Animation[] anim){
		this.anim = anim;
	}
	
	public Animation[] getAnimations(){
		return anim;
	}
}