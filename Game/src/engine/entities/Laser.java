package engine.entities;

import content.BulletList;
import content.EffectList;
import engine.screens.MainScreen;

/**
 * 
 * Laser object, similar to a bullet but uses a rectangular hitbox and can be scaled.
 * 
 * @author Daniel
 *
 */

public class Laser extends Bullet{
	
	// Laser size
	private int length, width;
	private int aWidth;
	private float scx, scy;
	
	// Effects
	private int iDelay;
	private int deleteTime;
	
	private boolean segmented;
	
	private Effect baseFlare;
	
	public Laser(BulletFrame frame, float x, float y, float dir, int length, int width, int delay, MainScreen screen){
		super(frame, x, y, dir, delay, screen);
		
		this.length = length;
		this.width = width;
		
		onCreate();
	}
	
	private void onCreate(){
		initFrameProperties();
		
		iDelay = Math.min(delay, 15);
		aWidth = width;
		resistant = true;
		
		segmented = type == BulletList.TYPE_LASER_DIST || type == BulletList.TYPE_LASER_HELIX;
		
		if(delay > 0){
			collisions = false;
			width = 2;
		}
		
		updateScale(2);
		
		baseFlare = new Effect(screen.getFrameList().getEffect(EffectList.TYPE_FLARE, color % 16), x, y);
		baseFlare.setLifetime(0);
		screen.addEffect(baseFlare);
	}
	
	public void initFrameProperties(){
		super.initFrameProperties();
	}
	
	public void update(){
		
		// Delay effect
		if(delay > 0){
			
			if(delay < 16){
				width = aWidth - (int)((delay - 1)*((aWidth - 2)/(float)iDelay));
				updateScale(0);
			}
			
			delay--;
			
			if(delay == 0)
				collisions = true;
		}
		else{
			// Despawn effect
			if(deleteTime > 0){
				
				if(deleteTime > 10){
					width = 2 + (int)((deleteTime - 10)*((aWidth - 2)/15f));
					updateScale(0);
				}
				
				deleteTime--;
				
				if(deleteTime == 0){
					deleted = true;
					baseFlare.delete();
				}
			}
		}
		updateMovements();
		
		baseFlare.setPos(x, y);
		baseFlare.getSprite().setScale(width/6f);
		
		time++;
	}
	
	public void onDestroy(boolean force){
		
		if((!force && resistant) || deleteTime > 0)
			return;
		
		deleteTime = 25;
		collisions = false;
	}
	
	// 0 - x	1 - y	2 - both
	private void updateScale(int s){
		if(s == 0 || s == 2) scx = (float)width/sprite.getHeight()*2;
		if(s == 1 || s == 2) scy = (float)length/sprite.getWidth()*2;
	}
	
	
	public void setLength(int length){
		this.length = length;
		updateScale(1);
	}
	
	public int getLength(){
		return length;
	}
	
	public float getScaleX(){
		return scx;
	}
	
	public void setWidth(int width){
		this.width = width;
		aWidth = width;
		updateScale(0);
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getActualWidth(){
		return aWidth;
	}
	
	public float getScaleY(){
		return scy;
	}
	
	public int getHitboxWidth(){
		return (int)(super.getHitboxWidth()*scx);
	}
	
	public int getHitboxLength(){
		return (int)(super.getHitboxLength()*scy);
	}
	
	public void setSegmented(boolean segmented){
		this.segmented = segmented;
	}
	
	public boolean isSegmented(){
		return segmented;
	}
}
