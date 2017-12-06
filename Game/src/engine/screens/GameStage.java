package engine.screens;

import engine.entities.Text;
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
 */

public abstract class GameStage{
	
	protected String scriptPath;
	protected int time;
	
	protected final MainScreen screen;
	protected ScriptCompiler scriptCompiler;
	protected ScriptController scriptController;
	protected DScript script;
	
	protected Text errorText;
	
	protected Renderer r;
	protected TextureCache tc;
	
	public GameStage(String scriptPath, MainScreen screen, Renderer r, TextureCache tc){
		this.scriptPath = scriptPath;
		this.screen = screen;
		this.r = r;
		this.tc = tc;
	}
	
	public void init(){
		script = new DScript(scriptPath);
		scriptCompiler = new ScriptCompiler();
		scriptController = new ScriptController(script, screen);
		
		compileScript();
		scriptController.init();
	}
	
	// Recompile script
	public void reloadScript(){
		compileScript();
		scriptController.reload();
	}
	
	private void compileScript(){
		scriptCompiler.compile(script);
		
		if(errorText != null)
			errorText.delete();
		
		if(scriptCompiler.failed()){
			errorText = new Text(scriptCompiler.getErrorText(), 40, 24, 800, 0.8f, -1, tc);
			screen.addText(errorText);
		}
	}
	
	public void deleteErrorText(){
		
		if(errorText == null)
			return;
		
		errorText.delete();
		errorText = null;
	}
	
	public abstract void update();
	public abstract void render();
}
