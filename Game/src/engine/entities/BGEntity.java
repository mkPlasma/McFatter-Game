package engine.entities;

import engine.graphics.Sprite;

/**
 * 
 * 3D game entity, used for background elements and camera
 * 
 * @author Daniel
 *
 */

public class BGEntity extends GameEntity{
	
	private float z;
	
	// Velocity
	private float velX, velY, velZ;
	
	// Rotation
	private float rotX, rotY, rotZ;
	
	private Sprite sprite;
	
	
	public BGEntity(Sprite sprite, float x, float y, float z){
		super(x, y);
		this.z = z;
		this.sprite = sprite;
	}
	
	public void update(){
		x += velX;
		y += velY;
		z += velZ;
	}
	
	
	
	public void setZ(float z){
		this.z = z;
	}
	
	public float getZ(){
		return z;
	}
	
	public void setVelX(float velX){
		this.velX = velX;
	}
	
	public float getVelX(){
		return velX;
	}
	
	public void setVelY(float velY){
		this.velY = velY;
	}
	
	public float getVelY(){
		return velY;
	}
	
	public void setVelZ(float velZ){
		this.velZ = velZ;
	}
	
	public float getVelZ(){
		return velZ;
	}
	
	
	public void setRotX(float rotX){
		this.rotX = rotX;
	}
	
	public float getRotX(){
		return rotX;
	}
	
	public void setRotY(float rotY){
		this.rotY = rotY;
	}
	
	public float getRotY(){
		return rotY;
	}
	
	public void setRotZ(float rotZ){
		this.rotZ = rotZ;
	}
	
	public float getRotZ(){
		return rotZ;
	}
	
	public void setSprite(Sprite sprite){
		this.sprite = sprite;
	}
	
	public Sprite getSprite(){
		return sprite;
	}
}
