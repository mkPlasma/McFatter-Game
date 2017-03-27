package engine.screens;

public abstract class GameStage{
	
	public static final int TYPE_MISSION = 0,
		TYPE_CUTSCENE = 1;
	
	protected final int type;
	
	protected int time;
	
	public GameStage(int type){
		this.type = type;
	}
	
	public abstract void init();
	public abstract void update();
	public abstract void render();
	
	public int getType(){
		return type;
	}
}
