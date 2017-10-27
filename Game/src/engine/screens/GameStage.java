package engine.screens;

import org.lwjgl.glfw.GLFW;

import engine.KeyboardListener;
import engine.graphics.Renderer;
import engine.graphics.TextureCache;
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
	
	protected String scriptPath;
	protected int time;
	
	protected ScriptCompiler scriptCompiler;
	protected ScriptController scriptController;
	protected DScript script;
	
	protected Renderer r;
	protected TextureCache tc;
	
	public GameStage(String scriptPath, Renderer r, TextureCache sc){
		this.scriptPath = scriptPath;
		this.r = r;
		this.tc = sc;
	}
	
	public void init(){
		script = new DScript(scriptPath);
		
		scriptCompiler = new ScriptCompiler();
		scriptCompiler.compile(script);
		
		scriptController = new ScriptController(script);
		scriptController.init();
	}
	
	// Recompile script
	public void reloadScript(){
		scriptCompiler.compile(script);
		scriptController.reload();
	}
	
	public abstract void update();
	public abstract void render();
}
