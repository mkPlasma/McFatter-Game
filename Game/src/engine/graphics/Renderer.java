package engine.graphics;

import java.util.ArrayList;

import engine.entities.Bullet;
import engine.entities.Effect;
import engine.entities.Player;
import engine.screens.MainScreen;

import static engine.graphics.RenderBatch.*;

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
		basicShader = new ShaderProgram("quad", "basic", "quad");
		basicShader.bindAttrib(0, "position");
		basicShader.bindAttrib(1, "size");
		basicShader.bindAttrib(2, "texCoords");
		basicShader.bindAttrib(3, "transforms");
		basicShader.bindAttrib(4, "alpha");
		basicShader.link();
		
		illumiShader = new ShaderProgram("quad", "basic", "quad");
		illumiShader.bindAttrib(0, "position");
		illumiShader.bindAttrib(1, "size");
		illumiShader.bindAttrib(2, "texCoords");
		illumiShader.bindAttrib(3, "transforms");
		illumiShader.bindAttrib(4, "alpha");
		illumiShader.link();
		
		renderBatches = new ArrayList<RenderBatch>();
		
		basicShader.use();
	}
	
	// Initialize rendering for MainScreen
	public void initMainScreen(int playerTex, int bulletTex1, int bulletTex2, int effectTex){
		
		// Add in order of rendering
		
		// Player bullets
		renderBatches.add(new RenderBatch(MainScreen.MAX_PLAYER_BULLETS, 32, bulletTex1, UPDATE_ALL, false));
		
		// Player
		renderBatches.add(new RenderBatch(1, 64, playerTex, UPDATE_VBO, false));
		
		// Enemy bullets
		renderBatches.add(new RenderBatch(MainScreen.MAX_ENEMY_BULLETS, 32, bulletTex1, UPDATE_ALL, false));
		renderBatches.add(new RenderBatch(MainScreen.MAX_ENEMY_BULLETS, 32, bulletTex1, UPDATE_ALL, true));
		renderBatches.add(new RenderBatch(MainScreen.MAX_ENEMY_BULLETS, 32, bulletTex2, UPDATE_ALL, false));
		renderBatches.add(new RenderBatch(MainScreen.MAX_ENEMY_BULLETS, 32, bulletTex2, UPDATE_ALL, true));
		
		// Effects
		renderBatches.add(new RenderBatch(MainScreen.MAX_EFFECTS, 32, effectTex, UPDATE_ALL, true));
		
		// Border
		Sprite border = new Sprite("border.png", 0, 0, 1280, 960);
		border.load();
		
		RenderBatch borderBatch = new RenderBatch(1, border.getTexture().getID(), 1280, 960, UPDATE_NONE, false);
		borderBatch.updateManual(320, 240, 1280, 960, border.getTextureCoords());
		
		renderBatches.add(borderBatch);
	}
	
	public void updatePlayer(Player player, int time){
		renderBatches.get(1).updateWithEntity(player, time);
	}
	
	public void updateEnemyBullets(ArrayList<Bullet> bullets, int time){
		
		// Separate by texture/additive
		ArrayList<Bullet> b1	= new ArrayList<Bullet>();
		ArrayList<Bullet> b1a	= new ArrayList<Bullet>();
		ArrayList<Bullet> b2	= new ArrayList<Bullet>();
		ArrayList<Bullet> b2a	= new ArrayList<Bullet>();
		
		for(Bullet b:bullets){
			switch(b.getSprite().getTexture().getPath()){
				
				case "bullets/01.png":
					if(!b.getSprite().isAdditive())
						b1.add(b);
					else
						b1a.add(b);
					break;
					
				case "bullets/02.png":
					if(!b.getSprite().isAdditive())
						b2.add(b);
					else
						b2a.add(b);
					break;
			}
		}

		renderBatches.get(2).updateWithEntities(b1, time);
		renderBatches.get(3).updateWithEntities(b1a, time);
		renderBatches.get(4).updateWithEntities(b2, time);
		renderBatches.get(5).updateWithEntities(b2a, time);
	}
	
	public void updatePlayerBullets(ArrayList<Bullet> bullets, int time){
		renderBatches.get(0).updateWithEntities(bullets, time);
	}
	
	public void updateEffects(ArrayList<Effect> effects, int time){
		renderBatches.get(6).updateWithEntities(effects, time);
	}
	
	public void render(){
		for(int i = 0; i < renderBatches.size(); i++){
			
			RenderBatch rb = renderBatches.get(i);
			
			// Don't bind texture again if last batch had same texture
			if(i == 0 || rb.getTextureID() != renderBatches.get(i - 1).getTextureID())
				rb.bindTexture();
			
			rb.render();
		}
	}
	
	// Delete vao/buffers
	public void cleanup(){
		basicShader.destroy();
		illumiShader.destroy();
		
		for(RenderBatch sb:renderBatches)
			sb.cleanup();
	}
}
