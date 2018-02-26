package engine.graphics;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import engine.entities.GameEntity;

/**
 * 
 * Container for a Texture object and its properties.
 * Several sprites may share a single Texture object, with each sprite being a different part of the texture.
 * 
 * @author Daniel
 *
 */

public class Sprite{
	
	/* 
	 * Do not load sprites directly. Instead use:
	 * 
	 * sprite = SpriteCache.cache(sprite);
	 * 
	 * This will place the sprite in the cache and return a loaded instance of the sprite.
	*/
	
	// Image texture
	private Texture texture;
	
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
	
	// Render additively
	private boolean additive = false;
	
	// Sprite animation
	private ArrayList<Animation> anim;
	
	
	public Sprite(Sprite spr){
		
		texture = spr.getTexture();
		
		x		= spr.getX();
		y		= spr.getY();
		width	= spr.getWidth();
		height	= spr.getHeight();
		
		texCoords = spr.getTextureCoords();
		
		rotation	= spr.getRotation();
		scaleX		= spr.getScaleX();
		scaleY		= spr.getScaleY();
		alpha		= spr.getAlpha();
		
		anim = spr.getAnimations();
	}

	public Sprite(String path, int x, int y, int width, int height){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		anim = new ArrayList<Animation>();
		texture = new Texture(path);
	}
	
	public Sprite(String path, int x, int y, int width, int height, float scale){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		scaleX = scale;
		scaleY = scale;
		
		anim = new ArrayList<Animation>();
		texture = new Texture(path);
	}
	
	// Load sprite to texture
	public void load(){
		texture.load();
		genTextureCoords();
	}
	
	// Creates texture coordinates
	public void genTextureCoords(){
		
		float w = texture.getWidth();
		float h = texture.getHeight();
		
		float left		= x/w;
		float right		= (x + width)/w;
		float top		= y/h;
		float bottom	= (y + height)/h;
		
		// Correct texture bleeding
		left	+= 0.5/w;
		right	-= 0.5/w;
		top		+= 0.5/h;
		bottom	-= 0.5/h;
		
		texCoords = new float[]{
			left, right, top, bottom
		};
	}
	
	// Returns a modified sprite for the animation at the given time
	public Sprite animate(int time, int syncTime, GameEntity e){
		
		if(anim.size() == 0)
			return this;
		
		Sprite spr = new Sprite(this);
		
		for(Animation a:spr.getAnimations()){
			a.setSprite(spr);
			a.setEntity(e);
			a.update(a.sync() ? syncTime : time);
			
			// Update texture coords
			if(a.getType() == Animation.ANIM_SET_SPRITE || a.getType() == Animation.ANIM_SET_SPRITE_FLIP)
				spr.genTextureCoords();
		}
		
		// Remove finished animations
		for(int i = 0; i < anim.size(); i++){
			if(anim.get(i).isFinished()){
				
				// Set finished scale/alpha
				scaleX = spr.getScaleX();
				scaleY = spr.getScaleY();
				alpha = spr.getAlpha();
				
				anim.remove(i);
				i--;
			}
		}
		
		return spr;
	}
	
	
	// Adds an animation to the list
	public void addAnimation(Animation a){
		anim.add(a);
	}
	
	// Return true if two sprites are the same, used in SpriteCache
	public boolean equals(Sprite sprite){
		
		if(sprite == null)
			return false;
		
		if(sprite.getTexture().getPath().equalsIgnoreCase(texture.getPath()) &&
			sprite.getX() == x &&
			sprite.getY() == y &&
			sprite.getWidth() == width &&
			sprite.getHeight() == height &&
			sprite.getRotation() == rotation &&
			sprite.getScaleX() == scaleX &&
			sprite.getScaleY() == scaleY &&
			sprite.getAlpha() == alpha){
		
			// Check if all animations are equal
			ArrayList<Animation> a = sprite.getAnimations();
			
			// Return false if one is null
			if((a == null && anim != null) || (a != null && anim == null))
				return false;
			
			if(a != null && anim != null){
				// Return false if they don't have the same amount
				if(a.size() != anim.size())
					return false;
				
				// Check if equal
				for(int i = 0; i < a.size(); i++)
					if(a.get(i).equals(anim.get(i)))
						return false;
			}
			
			return true;
		}
		
		return false;
	}
	
	public void setTextureCoords(float[] texCoords){
		this.texCoords = texCoords.clone();
	}
	
	public float[] getTextureCoords(){
		return texCoords;
	}
	
	public void setTexture(Texture texture){
		this.texture = texture;
	}

	public Texture getTexture(){
		return texture;
	}
	
	public ByteBuffer getImage(){
		return texture.getImage();
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
	
	public void setAdditive(boolean additive){
		this.additive = additive;
	}
	
	public boolean isAdditive(){
		return additive;
	}
	
	//public void setAnimations(Animation anim){
	//	this.anim = new Animation[]{anim};
	//}
	
	//public void setAnimations(Animation[] anim){
	//	this.anim = anim;
	//}
	
	public ArrayList<Animation> getAnimations(){
		return anim;
	}
}