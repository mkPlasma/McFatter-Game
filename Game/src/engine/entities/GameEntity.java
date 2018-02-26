package engine.entities;

import engine.graphics.Sprite;

/**
 * 
 * Generic abstract game entity.
 * 
 * @author Daniel
 *
 */

public abstract class GameEntity{
	
	// Position
	protected float x, y;
	
	// Time in frames since entity's spawn
	// Must be ticked in the update method
	protected int time;
	
	// If true, the entity will be deleted
	protected boolean deleted;
	
	// Whether entity is drawn or not
	protected boolean visible;
	
	// Stores a template of sprites and animations
	protected EntityFrame frame;
	
	public GameEntity(EntityFrame frame, float x, float y){
		this.frame = frame;
		this.x = x;
		this.y = y;
		
		visible = true;
	}
	
	public abstract void update();
	
	
	public EntityFrame getFrame(){
		return frame;
	}
	
	public Sprite getSprite(){
		return frame.getSprite();
	}
	
	public void delete(){
		deleted = true;
	}
	
	public void setX(float x){
		this.x = x;
	}
	
	public void setY(float y){
		this.y = y;
	}
	
	public void setPos(float x, float y){
		setX(x);
		setY(y);
	}
	
	public void setPos(float[] pos){
		setX(pos[0]);
		setY(pos[1]);
	}
	
	public float getX(){
		return x;
	}
	
	public float getY(){
		return y;
	}
	
	public float[] getPos(){
		return new float[]{x, y};
	}
	
	public int getTime(){
		return time;
	}
	
	public boolean isDeleted(){
		return deleted;
	}
	
	public void setVisible(boolean visible){
		this.visible = visible;
	}
	
	public boolean isVisible(){
		return visible;
	}
}
