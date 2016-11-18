package engine;

import java.awt.Graphics2D;

public abstract class GameEntity{
	
	protected int x, y;
	
	public GameEntity(int x, int y){
		this.x = x;
		this.y = y;
	}
	
	public void setX(int x){
		this.x = x;
	}
	
	public void setY(int y){
		this.y = y;
	}
	
	public void setPos(int x, int y){
		setX(x);
		setY(y);
	}
	
	public void setPos(int[] pos){
		setX(pos[0]);
		setY(pos[1]);
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
	
	public abstract void update();
	public abstract void draw(Graphics2D g2d);
}
