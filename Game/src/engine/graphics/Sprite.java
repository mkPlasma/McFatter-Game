package engine.graphics;

import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import engine.IOFunctions;
import engine.entities.GameEntity;

/*
 * 		Sprite.java
 * 		
 * 		Purpose:	Holds an image and certain properties, such as scale or alpha
 * 		Notes:		Several entities may share one sprite to reduce memory use.
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */

public class Sprite{
	
	/* 
	 * Do not load sprites directly. Instead use:
	 * 
	 * sprite = SpriteCache.cache(sprite);
	 * 
	 * This will place the sprite in the cache and return a loaded instance of the sprite.
	*/
	
	// Path of spritesheet
	private final String path;
	
	// Number of current sprite users
	private int numUsers;
	
	// Full image dimensions
	private int texWidth, texHeight;
	private int comp;
	
	// Area of spritesheet to use
	private int x, y, width, height;

	// Normalized texture coordinates of texture for rendering
	// 0 - Top left
	// 1 - Top right
	// 2 - Bottom right
	// 3 - Bottom left
	private float[] texCoords;
	
	private float rotation = 0;
	private float scaleX = 1, scaleY = 1;
	private float alpha = 1;
	
	// Stores texture
	private ByteBuffer texture;
	private int texID;
	
	// True if sprite has been loaded
	private boolean loaded;

	// Sprite animation
	private Animation[] anim;
	
	
	public Sprite(Sprite spr){
		path = spr.getPath();
		
		x = spr.getX();
		y = spr.getY();
		width = spr.getWidth();
		height = spr.getHeight();
		
		texHeight = spr.getTexHeight();
		texWidth = spr.getTexWidth();
		
		genTextureCoords();
		
		rotation = spr.getRotation();
		scaleX = spr.getScaleX();
		scaleY = spr.getScaleY();
		alpha = spr.getAlpha();
		
		anim = spr.getAnimations();
		
		texture = spr.getTexture();
		texID = spr.getTextureID();
		
		loaded = true;
	}

	public Sprite(String path, int x, int y, int width, int height){
		this.path = path;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public Sprite(String path, int x, int y, int width, int height, float scale){
		this.path = path;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		scaleX = scale;
		scaleY = scale;
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
	
	// Load sprite to texture
	public void load(){
		
		if(loaded)
			return;
		
		IntBuffer w = BufferUtils.createIntBuffer(1);
		IntBuffer h = BufferUtils.createIntBuffer(1);
		IntBuffer comp = BufferUtils.createIntBuffer(1);
		
		ByteBuffer t = null;
		
		try{
			t = stbi_load_from_memory(IOFunctions.readToByteBuffer("Game/res/img/" + path), w, h, comp, 0);
			loaded = true;
		}catch(IOException e){
			e.printStackTrace();
		}
		
		if(t == null)
			throw new RuntimeException("Failed to load image: " + STBImage.stbi_failure_reason());
		
		texWidth = w.get(0);
		texHeight = h.get(0);
		this.comp = comp.get(0);
		
		texture = t;
		
		texID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texID);
		
		/*
		if(this.comp == 3){
			if((texWidth & 3) != 0)
				glPixelStorei(GL_UNPACK_ALIGNMENT, 2 - (texWidth & 1));
			
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, texWidth, texHeight, 0, GL_RGB, GL_UNSIGNED_BYTE, texture);
		}
		else*/
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, texWidth, texHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, texture);
		
		genTextureCoords();
	}
	
	// Creates texture coordinates
	public void genTextureCoords(){
		float left =	(float)x/(float)texWidth;
		float right =	((float)x + (float)width)/(float)texWidth;
		float top =		(float)y/(float)texHeight;
		float bottom =	((float)y + (float)height)/(float)texHeight;
		
		texCoords = new float[]{
			left,  top,
			right, top,
			right, bottom,
			left,  bottom
		};
	}
	
	// Returns a modified sprite for the animation at the given time
	public Sprite animate(int time, int syncTime, GameEntity e){
		
		if(anim == null)
			return this;
		
		Sprite spr = new Sprite(this);
		
		for(Animation a:spr.getAnimations()){
			a.setSprite(spr);
			a.setEntity(e);
			a.update(a.sync() ? syncTime : time);
		}
		
		return spr;
	}
	
	
	// Adds an animation to the list
	public void addAnimation(Animation a){
		
		if(anim != null){
			Animation[] temp = new Animation[anim.length + 1];
			
			for(int i = 0; i < anim.length; i++){
				temp[i] = anim[i];
			}
			
			temp[temp.length - 1] = a;
			
			anim = temp.clone();
		}else{
			anim = new Animation[]{a};
		}
	}
	
	// Return true if two sprites are the same, used in SpriteCache
	public boolean isEqual(Sprite sprite){
		
		if(sprite == null)
			return false;
		
		if(sprite.path.equalsIgnoreCase(path) &&
			sprite.x == x &&
			sprite.y == y &&
			sprite.width == width &&
			sprite.height == height &&
			sprite.rotation == rotation &&
			sprite.scaleX == scaleX &&
			sprite.scaleY == scaleY &&
			sprite.alpha == alpha){
		
			// Check if all animations are equal
			Animation[] a = sprite.getAnimations();
			
			// Return false if one is null
			if((a == null && anim != null) || (a != null && anim == null))
				return false;
			
			if(a != null && anim != null){
				// Return false if they don't have the same amount
				if(a.length != anim.length)
					return false;
				
				// Check if equal
				for(int i = 0; i < sprite.getAnimations().length; i++)
					if(a[i].isEqual(anim[i]))
						return false;
			}
			
			return true;
		}
		
		return false;
	}
	
	public float[] getTextureCoords(){
		return texCoords;
	}
	
	public void setTexture(ByteBuffer texture){
		this.texture = texture;
	}

	public ByteBuffer getTexture(){
		return texture;
	}
	
	public void setTextureID(int texID){
		this.texID = texID;
	}
	
	public int getTextureID(){
		return texID;
	}
	
	public void setTexWidth(int texWidth){
		this.texWidth = texWidth;
	}
	
	public int getTexWidth(){
		return texWidth;
	}
	
	public void setTexHeight(int texHeight){
		this.texHeight = texHeight;
	}
	
	public int getTexHeight(){
		return texHeight;
	}
	
	public void setComp(int comp){
		this.comp = comp;
	}
	
	public int getComp(){
		return comp;
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
	
	
	public boolean isLoaded(){
		return loaded;
	}
	
	public String getPath(){
		return path;
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
	
	public float getScaledWidth(){
		return width*scaleX;
	}
	
	public void setHeight(int height){
		this.height = height;
	}
	
	public int getHeight(){
		return height;
	}
	
	public float getScaledHeight(){
		return height*scaleY;
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
	
	public void setScale(float scale){
		scaleX = scale;
		scaleY = scale;
	}
	
	public void setScale(float scaleX, float scaleY){
		this.scaleX = scaleX;
		this.scaleY = scaleY;
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