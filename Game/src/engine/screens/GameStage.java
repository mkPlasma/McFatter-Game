package engine.screens;

import engine.graphics.Renderer;
import engine.script.DScript;
import engine.script.ScriptCompiler;
import engine.script.ScriptController;

/*
 * 		GameStage.java
 * 		
 * 		Purpose:	Container for a stage, run in MainScreen.
 * 		Notes:		Either a mission or cutscene.
 * 		
 * 		Children:	Mission.java
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				10/17
 * 		Changes:			Added DScript
 */

public abstract class GameStage{
	
	// Currently used as dscript path
	protected String scriptPath;
	protected int time;
	
	protected ScriptController scriptController;
	protected DScript script;
	
	protected Renderer r;
	
	public GameStage(String scriptPath, Renderer r){
		this.scriptPath = scriptPath;
		this.r = r;
	}
	
	public void init(){
		script = new DScript(scriptPath);
		new ScriptCompiler().compile(script);
		
		scriptController = new ScriptController(script);
		scriptController.init();
	}
	
	
	public abstract void update();
	public abstract void render();
}
