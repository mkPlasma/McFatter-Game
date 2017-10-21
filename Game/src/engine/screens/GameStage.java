package engine.screens;

import org.lwjgl.glfw.GLFW;

import engine.KeyboardListener;
import engine.graphics.Renderer;
import engine.graphics.SpriteCache;
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
	
	private boolean reloadingScript = false;
	
	protected Renderer r;
	protected SpriteCache sc;
	
	public GameStage(String scriptPath, Renderer r, SpriteCache sc){
		this.scriptPath = scriptPath;
		this.r = r;
		this.sc = sc;
	}
	
	public void init(){
		script = new DScript(scriptPath);
		
		scriptCompiler = new ScriptCompiler();
		scriptCompiler.compile(script);
		
		scriptController = new ScriptController(script);
		scriptController.init();
	}
	
	// Check if script needs to be reloaded
	public void reloadScript(){
		if(KeyboardListener.isKeyDown(GLFW.GLFW_KEY_LEFT_ALT) && KeyboardListener.isKeyDown(GLFW.GLFW_KEY_R)){
			if(!reloadingScript){
				reloadingScript = true;
				
				scriptCompiler.compile(script);
				scriptController.reload();
			}
		}
		else
			reloadingScript = false;
	}
	
	public abstract void update();
	public abstract void render();
}
