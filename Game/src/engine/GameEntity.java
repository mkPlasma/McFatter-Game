package engine;

public abstract class GameEntity{
	
	protected float x, y;
	protected int time = 0;
	
	public GameEntity(){
		
	}
	
	public GameEntity(float x, float y){
		this.x = x;
		this.y = y;
	}
	
	public GameEntity(float[] pos){
		x = pos[0];
		y = pos[1];
	}
	
	public abstract void onCreate();
	public abstract void onDestroy();
	public abstract void update();
	public abstract void draw(Renderer r);
	
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
}
