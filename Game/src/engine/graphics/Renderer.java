package engine.graphics;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL20.*;

import engine.entities.Bullet;
import engine.entities.Effect;
import engine.entities.Enemy;
import engine.entities.GameEntity;
import engine.entities.Laser;
import engine.entities.Player;
import engine.entities.Text;

import static engine.graphics.RenderBatch.*;
import static engine.screens.MainScreen.*;

/*
 * 		Renderer.java
 * 		
 * 		Purpose:	Renders game objects.
 * 		Notes:		
 * 		
 */

public class Renderer{
	
	private ShaderProgram basicShader,
						  laserShader,
						  hitboxShader,
						  laserHitboxShader;
	
	private ArrayList<RenderBatch> renderBatches;
	
	private TextureCache tc;
	
	private int time;
	
	private boolean renderHitboxes;
	
	public Renderer(TextureCache tc){
		this.tc = tc;
	}
	
	public void init(){
		// Init shaders
		basicShader = new ShaderProgram("quad", "quad", "basic");
		basicShader.bindAttrib(0, "position");
		basicShader.bindAttrib(1, "size");
		basicShader.bindAttrib(2, "texCoords");
		basicShader.bindAttrib(3, "transforms");
		basicShader.bindAttrib(4, "alpha");
		basicShader.link();
		
		laserShader = new ShaderProgram("laser", "laser", "basic");
		laserShader.bindAttrib(0, "position");
		laserShader.bindAttrib(1, "size");
		laserShader.bindAttrib(2, "texCoords");
		laserShader.bindAttrib(3, "transforms");
		laserShader.bindAttrib(4, "alpha");
		laserShader.bindAttrib(5, "segments");
		laserShader.link();
		
		hitboxShader = new ShaderProgram("circle", "circle", "hitbox");
		hitboxShader.bindAttrib(0, "position");
		hitboxShader.bindAttrib(1, "radius");
		hitboxShader.link();
		
		laserHitboxShader = new ShaderProgram("quad", "quad", "hitbox");
		laserHitboxShader.bindAttrib(0, "position");
		laserHitboxShader.bindAttrib(1, "size");
		laserHitboxShader.bindAttrib(2, "texCoords");
		laserHitboxShader.bindAttrib(3, "transforms");
		laserHitboxShader.bindAttrib(4, "alpha");
		laserHitboxShader.link();
		
		renderBatches = new ArrayList<RenderBatch>();
	}
	
	// Initialize rendering for MainScreen
	public void initMainScreen(){

		int bulletTex1 = tc.cache("bullets/01.png").getID();
		int bulletTex2 = tc.cache("bullets/02.png").getID();
		
		// Add in order of rendering
		
		// Player bullets
		renderBatches.add(new RenderBatch(0, MAX_PLAYER_BULLETS, 32, bulletTex1, UPDATE_ALL_BUT_SIZE, false));
		
		// Enemies
		renderBatches.add(new RenderBatch(0, MAX_ENEMIES, 64, tc.cache("enemies.png").getID(), UPDATE_ALL_BUT_SIZE, false));
		
		// Player
		renderBatches.add(new RenderBatch(0, 1, 64, tc.cache("player.png").getID(), UPDATE_VBO, false));
		
		// Enemy bullets
		renderBatches.add(new RenderBatch(0, MAX_ENEMY_BULLETS, 32, bulletTex1, UPDATE_ALL_BUT_SIZE, false));
		renderBatches.add(new RenderBatch(0, MAX_ENEMY_BULLETS, 32, bulletTex1, UPDATE_ALL_BUT_SIZE, true));
		renderBatches.add(new RenderBatch(0, MAX_ENEMY_BULLETS, 32, bulletTex2, UPDATE_ALL_BUT_SIZE, false));
		renderBatches.add(new RenderBatch(0, MAX_ENEMY_BULLETS, 32, bulletTex2, UPDATE_ALL_BUT_SIZE, true));
		renderBatches.add(new RenderBatch(1, MAX_ENEMY_BULLETS, 32, bulletTex1, UPDATE_ALL_BUT_SIZE, false));
		renderBatches.add(new RenderBatch(1, MAX_ENEMY_BULLETS, 32, bulletTex1, UPDATE_ALL_BUT_SIZE, true));
		
		// Effects
		renderBatches.add(new RenderBatch(0, MAX_EFFECTS, 32, tc.cache("effects.png").getID(), UPDATE_ALL_BUT_SIZE, true));
		
		// Hitboxes
		renderBatches.add(new RenderBatch(2, MAX_ENEMY_BULLETS + MAX_ENEMIES + 1, UPDATE_HITBOX));
		
		// Laser hitboxes
		renderBatches.add(new RenderBatch(3, MAX_ENEMY_BULLETS, UPDATE_LASER_HITBOX));
		
		// Border
		Sprite border = new Sprite("border.png", 0, 0, 1280, 960);
		tc.loadSprite(border);
		
		RenderBatch borderBatch = new RenderBatch(1, 1280, 960, border.getTexture().getID(), UPDATE_NONE);
		borderBatch.updateManual(320, 240, border.getTextureCoords());
		
		renderBatches.add(borderBatch);
		
		// Text
		renderBatches.add(new RenderBatch(MAX_TEXT, 16, 32, tc.cache("font.png").getID(), UPDATE_TEXT));
	}
	
	public void setTime(int time){
		this.time = time;
	}
	
	public void updatePlayer(Player player){
		renderBatches.get(2).updateWithEntity(player, time);
	}
	
	public void updateEnemies(ArrayList<Enemy> enemies){
		renderBatches.get(1).updateWithEntities(enemies, time);
	}
	
	public void updateEnemyBullets(ArrayList<Bullet> bullets){
		
		// Separate by texture/additive
		ArrayList<Bullet> b1	= new ArrayList<Bullet>();
		ArrayList<Bullet> b1a	= new ArrayList<Bullet>();
		ArrayList<Bullet> b2	= new ArrayList<Bullet>();
		ArrayList<Bullet> b2a	= new ArrayList<Bullet>();
		ArrayList<Bullet> l		= new ArrayList<Bullet>();
		ArrayList<Bullet> la	= new ArrayList<Bullet>();
		
		for(Bullet b:bullets){
			switch(b.getSprite().getTexture().getPath()){
				
				case "bullets/01.png":
					if(b instanceof Laser && ((Laser)b).isSegmented()){
						if(!b.getSprite().isAdditive())
							l.add(b);
						else
							la.add(b);
					}
					else{
						if(!b.getSprite().isAdditive())
							b1.add(b);
						else
							b1a.add(b);
					}
					break;
					
				case "bullets/02.png":
					if(!b.getSprite().isAdditive())
						b2.add(b);
					else
						b2a.add(b);
					break;
			}
		}
		
		renderBatches.get(3).updateWithEntities(b1, time);
		renderBatches.get(4).updateWithEntities(b1a, time);
		renderBatches.get(5).updateWithEntities(b2, time);
		renderBatches.get(6).updateWithEntities(b2a, time);
		renderBatches.get(7).updateWithEntities(l, time);
		renderBatches.get(8).updateWithEntities(la, time);
	}
	
	public void updatePlayerBullets(ArrayList<Bullet> bullets){
		renderBatches.get(0).updateWithEntities(bullets, time);
	}
	
	public void updateEffects(ArrayList<Effect> effects){
		renderBatches.get(9).updateWithEntities(effects, time);
	}
	
	public void updateHitboxes(ArrayList<Bullet> enemyBullets, ArrayList<Enemy> enemies, Player player){
		
		if(!renderHitboxes)
			return;
		
		ArrayList<GameEntity> el = new ArrayList<GameEntity>();
		
		el.addAll(enemyBullets);
		el.addAll(enemies);
		el.add(player);
		
		ArrayList<GameEntity> ll = new ArrayList<GameEntity>();
		
		for(int i = 0; i < el.size(); i++){
			GameEntity e = el.get(i);
			
			if(e instanceof Laser){
				el.remove(i);
				ll.add(e);
				
				i--;
			}
		}
		
		renderBatches.get(10).updateHitboxes(el);
		renderBatches.get(11).updateWithEntities(ll, 0);
	}
	
	public void updateText(ArrayList<Text> text){
		renderBatches.get(13).updateWithEntities(text, 0);
	}
	
	public void render(){
		for(int i = 0; i < renderBatches.size(); i++){
			
			RenderBatch rb = renderBatches.get(i);
			
			if(rb.getShader() == 0)
				basicShader.use();
			else if(rb.getShader() == 1){
				laserShader.use();
				glUniform1i(0, time);
			}
			else{
				if(!renderHitboxes)
					continue;
				
				if(rb.getShader() == 2)
					hitboxShader.use();
				else
					laserHitboxShader.use();
			}
			
			// Don't bind texture again if last batch had same texture
			if(rb.getTextureID() != -1 && (i == 0 || rb.getTextureID() != renderBatches.get(i - 1).getTextureID()))
				rb.bindTexture();
			
			rb.render();
		}
	}
	
	public void toggleRenderHitboxes(){
		renderHitboxes = !renderHitboxes;
	}
	
	// Delete vao/buffers
	public void cleanup(){
		basicShader.destroy();
		hitboxShader.destroy();
		
		for(RenderBatch sb:renderBatches)
			sb.cleanup();
	}
}
