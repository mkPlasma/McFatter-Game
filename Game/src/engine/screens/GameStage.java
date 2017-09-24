package engine.screens;

/*
 * 		GameStage.java
 * 		
 * 		Purpose:	Container for a stage, run in MainScreen.
 * 		Notes:		Either a mission or cutscene.
 * 		
 * 		Children:	Mission.java
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */

public abstract class GameStage{
	
	public static final int TYPE_MISSION = 0,
		TYPE_CUTSCENE = 1;
	
	protected final int type;
	
	protected final MainScreen screen;
	
	protected int time;
	
	public GameStage(int type, MainScreen screen){
		this.type = type;
		this.screen = screen;
	}
	
	public abstract void init();
	public abstract void update();
	public abstract void render();
	
	public int getType(){
		return type;
	}
}
