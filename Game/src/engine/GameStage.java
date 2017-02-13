package engine;

import engine.graphics.Renderer;

public abstract class GameStage{
	
	public static final int TYPE_MISSION = 0,
		TYPE_CUTSCENE = 1;
	
	protected final Renderer r;
	protected final int type;
	
	protected int time;
	
	public GameStage(Renderer r, int type){
		this.r = r;
		this.type = type;
	}
	
	public abstract void init();
	public abstract void update();
	public abstract void draw();
	
	public int getType(){
		return type;
	}
}
