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

	// 0	Player
	// 2-3	Enemy bullets
	// 3-4	Player bullets
	private SpriteBatch[] spriteBatches = new SpriteBatch[3];
	
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
		
		// Init batches
		
		// Player
		spriteBatches[0] = new SpriteBatch(12, false);

		// Enemy bullets
		spriteBatches[1] = new SpriteBatch(MainScreen.MAX_ENEMY_BULLETS*12, false);
		
		// Player bullets
		spriteBatches[2] = new SpriteBatch(MainScreen.MAX_PLAYER_BULLETS*12, false);
	}
	
	// Render functions
	public void renderPlayer(Player p, int time){
		basicShader.use();
		
		ArrayList<GameEntity> pl = new ArrayList<GameEntity>();
		pl.add(p);

		spriteBatches[0].setTextureID(p.getSprite().getTextureID());
		spriteBatches[0].update(pl, time);
		spriteBatches[0].draw();
	}
	
	public void renderEnemyBullets(ArrayList<Bullet> bl, int time){
		
		if(bl == null || bl.isEmpty())
			return;
		
		illumiShader.use();
		spriteBatches[1].setTextureID(bl.get(0).getSprite().getTextureID());
		spriteBatches[1].update(bl, time);
		spriteBatches[1].draw();
	}
	
	public void renderPlayerBullets(ArrayList<Bullet> bl, int time){
		if(bl == null || bl.isEmpty())
			return;
		
		illumiShader.use();
		spriteBatches[2].setTextureID(bl.get(0).getSprite().getTextureID());
		spriteBatches[2].update(bl, time);
		spriteBatches[2].draw();
		
	}
	
	public void renderEnemies(ArrayList<Enemy> el, int time){
		//basicShader.use();
		//renderEntities(el, time, false);
	}
	
	// Delete vao/buffers
	public void cleanup(){
		basicShader.destroy();
		illumiShader.destroy();
		
		for(SpriteBatch sb:spriteBatches)
			sb.cleanup();
	}
}
