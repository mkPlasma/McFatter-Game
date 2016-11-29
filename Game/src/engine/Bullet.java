package engine;

import java.awt.Color;
import java.awt.Graphics2D;

public class Bullet extends GameEntity{
	
	public static final int
	TYPE_ORB_S = 0,
	TYPE_ORB_M = 1,
	TYPE_ORB_L = 2;
	
	
	private int hitboxSize;
	private float dir, spd;
	
	public Bullet(float x, float y, float dir, float spd, int type, int color){
		super(x, y);
		this.dir = dir;
		this.spd = spd;
		
		hitboxSize = initHitboxSize(type);
	}
	
	private int initHitboxSize(int type){
		switch(type){
			case 0:
				return 2;
			case 1:
				return 5;
			case 2:
				return 8;
		}
		
		return 0;
	}
	
	public float getDir(){
		return dir;
	}
	
	public void setDir(float dir){
		this.dir = dir;
	}
	
	public float getSpd(){
		return spd;
	}
	
	public void setSpd(float spd){
		this.spd = spd;
	}
	
	public int getHitboxSize(){
		return hitboxSize;
	}
	
	public void setHitboxSize(int hitboxSize){
		this.hitboxSize = hitboxSize;
	}
	
	
	public void update(){
		x += spd*Math.cos(Math.toRadians(dir));
		y += spd*Math.sin(Math.toRadians(dir));
	}
	
	public void draw(Graphics2D g2d){
		g2d.setColor(Color.BLUE);
		
		float size = hitboxSize;
		
		g2d.fillOval((int)(x - (size/2)), (int)(y - (size/2)), (int)size, (int)size);
	}
}
