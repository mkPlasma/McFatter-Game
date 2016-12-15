package engine;

import java.awt.Graphics2D;

public abstract class GameEntity{
	
	protected double x, y;
	
	public void setX(double x){
		this.x = x;
	}
	
	public void setY(double y){
		this.y = y;
	}
	
	public void setPos(double x, double y){
		setX(x);
		setY(y);
	}
	
	public void setPos(double[] pos){
		setX(pos[0]);
		setY(pos[1]);
	}
	
	public double getX(){
		return x;
	}
	
	public double getY(){
		return y;
	}
	
	public double[] getPos(){
		return new double[]{x, y};
	}
	
	public abstract void update();
	public abstract void draw(Graphics2D g2d);
}
