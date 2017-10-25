package engine.graphics;

import java.util.ArrayList;

import engine.entities.Bullet;
import engine.entities.Enemy;
import engine.entities.GameEntity;
import engine.entities.Player;
import engine.screens.MainScreen;

/*
 * 		Renderer.java
 * 		
 * 		Purpose:	Renders game objects.
 * 		Notes:		
 * 		
 */

public class Renderer{
	
	private ShaderProgram basicShader;
	private ShaderProgram illumiShader;
	
	private ArrayList<RenderBatch> renderBatches;
	
	public void init(){
		// Init shaders
		basicShader = new ShaderProgram("basic", "basic");
		basicShader.bindAttrib(0, "position");
		basicShader.bindAttrib(1, "texCoords");
		basicShader.link();
		
		illumiShader = new ShaderProgram("basic", "illumi");
		illumiShader.bindAttrib(0, "position");
		illumiShader.bindAttrib(1, "texCoords");
		illumiShader.link();
		
		renderBatches = new ArrayList<RenderBatch>();
		
		basicShader.use();
	}
	
	// Initialize rendering for MainScreen
	public void initMainScreen(int playerTexID, int bulletTexID){
		
		// Add in order of rendering
		
		// Player bullets
		renderBatches.add(new RenderBatch(MainScreen.MAX_PLAYER_BULLETS*12, bulletTexID, false, false));
		
		// Player
		renderBatches.add(new RenderBatch(12, playerTexID, true, false));
		
		// Enemy bullets
		renderBatches.add(new RenderBatch(MainScreen.MAX_ENEMY_BULLETS*12, bulletTexID, false, false));
		
		// Border
		Sprite border = new Sprite("border.png", 0, 0, 1280, 960);
		border.load();
		
		RenderBatch borderBatch = new RenderBatch(12, border.getTextureID(), true, false);
		borderBatch.updateManual(320, 240, 1280, 960, border.getTextureCoords());
		
		renderBatches.add(borderBatch);
	}
	
	public void updatePlayer(Player player, int time){
		renderBatches.get(1).updateWithEntity(player, time);
	}
	
	public void updateEnemyBullets(ArrayList<Bullet> bullets, int time){
		renderBatches.get(2).updateWithEntities(bullets, time);
	}
	
	public void updatePlayerBullets(ArrayList<Bullet> bullets, int time){
		renderBatches.get(0).updateWithEntities(bullets, time);
	}
	
	public void render(){
		for(RenderBatch rb:renderBatches)
			rb.render();
	}
	
	// Delete vao/buffers
	public void cleanup(){
		basicShader.destroy();
		illumiShader.destroy();
		
		for(RenderBatch sb:renderBatches)
			sb.cleanup();
	}
}
