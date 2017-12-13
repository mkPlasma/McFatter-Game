package engine.screens;

import content.FrameList;
import engine.entities.Player;
import engine.entities.Text;
import engine.graphics.Renderer;
import engine.graphics.TextureCache;
import engine.script.DScript;
import engine.script.ScriptCompiler;
import engine.script.ScriptController;

/*
 * 		GameStage.java
 * 		
 * 		Purpose:	Abstract game mission.
 * 		Notes:		Bullets, enemies, bosses, and gameplay
 * 					will be handled here.
 * 		
 */

public class GameStage{
	
	protected final MainScreen screen;
	
	protected String scriptPath;
	protected int time;
	
	protected ScriptCompiler scriptCompiler;
	protected ScriptController scriptController;
	protected DScript script;
	
	protected Text errorText;
	
	private Player player;
	
	protected Renderer r;
	
	public GameStage(String scriptPath, Player player, MainScreen screen, Renderer r){
		this.scriptPath = scriptPath;
		this.player = player;
		this.screen = screen;
		this.r = r;
	}
	
	public void init(){
		script = new DScript(scriptPath);
		scriptCompiler = new ScriptCompiler();
		scriptController = new ScriptController(script, screen);
		
		compileScript();
		scriptController.init();
		scriptController.setPlayer(player);
	}
	
	public void update(){
		if(!scriptCompiler.failed())
			scriptController.run();
	}
	
	// Recompile script
	public void reloadScript(){
		compileScript();
		scriptController.reload();
		player.resetDeaths();
	}
	
	private void compileScript(){
		scriptCompiler.compile(script);
		
		if(errorText != null)
			errorText.delete();
		
		if(scriptCompiler.failed()){
			errorText = new Text(scriptCompiler.getErrorText(), 40, 24, 800, 0.8f, -1, screen.getTextureCache());
			screen.addText(errorText);
		}
	}
	
	public void deleteErrorText(){
		
		if(errorText == null)
			return;
		
		errorText.delete();
		errorText = null;
	}
}
