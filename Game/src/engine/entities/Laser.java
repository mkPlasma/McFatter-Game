package engine.entities;

import content.FrameList;
import engine.screens.MainScreen;

/*
 * 		Laser.java
 * 		
 * 		Purpose:	Solid laser object.
 * 		Notes:		Implement curvy lasers?
 * 		
 */

public class Laser extends Bullet{
	
	// Laser size
	private int length, width;
	private float scx, scy;
	
	private float hbLengthCrop;
	
	public Laser(BulletFrame frame, float x, float y, float dir, int length, int width, int delay, FrameList frameList, MainScreen screen){
		super(frame, x, y, dir, delay, frameList, screen);

		this.length = length;
		this.width = width;
		
		onCreate();
	}
	
	private void onCreate(){
		initFrameProperties();
		
		collisions = true;
		
		updateScale(2);
	}
	
	public void initFrameProperties(){
		super.initFrameProperties();
		hbLengthCrop = frame.getHBLengthCrop();
	}
	
	public void update(){
		updateMovements();
		time++;
	}
	
	public void onDestroy(){
		deleted = true;
	}
	
	// 0 - x	1 - y	2 - both
	private void updateScale(int s){
		if(s == 0 || s == 2) scx = (float)(width)/sprite.getHeight();
		if(s == 1 || s == 2) scy = (float)(length*2)/sprite.getWidth();
	}
	
	
	public void setLength(int length){
		this.length = length;
		updateScale(0);
	}
	
	public int getLength(){
		return length;
	}
	
	public float getScaleX(){
		return scx;
	}
	
	public void setWidth(int width){
		this.width = width;
		updateScale(1);
	}
	
	public int getWidth(){
		return width;
	}
	
	public float getScaleY(){
		return scy;
	}
	
	public int getHitboxSize(){
		return (int)(scx*hitboxSize);
	}
	
	public int getHBLengthCrop(){
		
		// Absolute if whole number
		if(hbLengthCrop % 1 == 0) return (int)hbLengthCrop;
		
		// Depends on length otherwise
		return (int)(length*hbLengthCrop);
	}
}
