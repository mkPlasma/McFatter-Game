package engine.screens;

import content.FrameList;
import engine.entities.Player;
import engine.graphics.Renderer;
import engine.graphics.TextureCache;

/*
 * 		Mission.java
 * 		
 * 		Purpose:	Abstract game mission.
 * 		Notes:		Bullets, enemies, bosses, and gameplay
 * 					will be handled here.
 * 		
 */

public class Mission extends GameStage{
	
	private Player player;
	
	private FrameList frameList;
	
	public Mission(String scriptPath, MainScreen screen, Renderer r, TextureCache tc){
		super(scriptPath, screen, r, tc);
	}
	
	public void init(){
		super.init();
		
		frameList = new FrameList(tc);
		player = new Player(224, 450, frameList, screen);
		
		tc.loadSprite(player.getSprite());
		
		scriptController.setPlayer(player);
		scriptController.setFrameList(frameList);
	}
	
	public void update(){
		player.update();
		
		if(!scriptCompiler.failed())
			scriptController.run();
	}
	
	public void render(){
		
	}
	
	public Player getPlayer(){
		return player;
	}
}
