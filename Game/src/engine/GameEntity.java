package engine;

import java.awt.Graphics2D;

public abstract class GameEntity{
	
	protected float x, y;
	
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
	
	public abstract void update();
	public abstract void draw(Graphics2D g2d);
}
