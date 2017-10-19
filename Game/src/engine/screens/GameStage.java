package engine.screens;

import engine.script.DScript;
import engine.script.ScriptCompiler;
import engine.script.ScriptController;
import engine.script.ScriptRunner;

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
	protected String xmlPath;
	protected int time;
	
	protected ScriptController scriptController;
	protected DScript script;
	
	public GameStage(String xmlPath){
		this.xmlPath = xmlPath;
	}
	
	public void init(){
		script = new DScript(xmlPath);
		new ScriptCompiler().compile(script);
		
		scriptController = new ScriptController(script);
		scriptController.init();
	}
	
	
	public abstract void update();
	public abstract void render();
}
