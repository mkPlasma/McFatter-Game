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
import engine.entities.TextChar;

import static engine.graphics.RenderBatch.*;
import static engine.screens.MainScreen.*;

/**
 * 
 * Handles OpenGL rendering.
 * Sets up and stores render batches, caches textures, etc.
 * 
 * @author Daniel
 *
 */

public class Renderer{
	
	private ShaderProgram basicShader,
						  laserShader,
						  hitboxShader,
						  laserHitboxShader;
	
	private ArrayList<RenderBatch> renderBatches;
	private ArrayList<RenderBatch> objRenderBatches;
	
	private RenderBatch
		rbPlayer,
		
		rbEnemyBullets1,
		rbEnemyBullets1a,
		rbEnemyBullets2,
		rbEnemyBullets2a,
		rbEnemyBulletsL,
		rbEnemyBulletsLa,
		
		rbPlayerBullets,
		
		rbEnemies,
		rbEffects,
		
		rbHitboxes,
		rbLaserHitboxes,
		
		rbText;
	
	private TextureCache tc;
	
	private int time;
	
	private boolean renderObjects;
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

		renderBatches	= new ArrayList<RenderBatch>();
		objRenderBatches	= new ArrayList<RenderBatch>();
	}
	
	// Initialize rendering for MainScreen
	public void initMainScreen(){

		int bulletTex1 = tc.cache("bullets/01.png").getID();
		int bulletTex2 = tc.cache("bullets/02.png").getID();
		
		// Player
		rbPlayer = new RenderBatch(0, 1, 64, tc.cache("player.png").getID(), UPDATE_VBO, false);
		
		// Player bullets
		rbPlayerBullets = new RenderBatch(0, MAX_PLAYER_BULLETS, 32, bulletTex1, UPDATE_ALL, false);

		// Enemy bullets
		rbEnemyBullets1		= new RenderBatch(SHADER_STANDARD, MAX_ENEMY_BULLETS, 32, bulletTex1, UPDATE_ALL, false);
		rbEnemyBullets1a		= new RenderBatch(SHADER_STANDARD, MAX_ENEMY_BULLETS, 32, bulletTex1, UPDATE_ALL, true);
		rbEnemyBullets2		= new RenderBatch(SHADER_STANDARD, MAX_ENEMY_BULLETS, 32, bulletTex2, UPDATE_ALL, false);
		rbEnemyBullets2a		= new RenderBatch(SHADER_STANDARD, MAX_ENEMY_BULLETS, 32, bulletTex2, UPDATE_ALL, true);
		rbEnemyBulletsL		= new RenderBatch(SHADER_LASER, MAX_ENEMY_BULLETS, 32, bulletTex1, UPDATE_ALL, false);
		rbEnemyBulletsLa		= new RenderBatch(SHADER_LASER, MAX_ENEMY_BULLETS, 32, bulletTex1, UPDATE_ALL, true);
		
		// Enemies
		rbEnemies = new RenderBatch(SHADER_STANDARD, MAX_ENEMIES, 64, tc.cache("enemies.png").getID(), UPDATE_ALL, false);
		
		// Effects
		rbEffects = new RenderBatch(SHADER_STANDARD, MAX_EFFECTS, 32, tc.cache("effects.png").getID(), UPDATE_ALL, true);
		
		// Hitboxes
		rbHitboxes = new RenderBatch(SHADER_HITBOX, MAX_ENEMY_BULLETS + MAX_ENEMIES + 1, UPDATE_HITBOX);
		rbLaserHitboxes = new RenderBatch(SHADER_L_HITBOX, MAX_ENEMY_BULLETS, UPDATE_LASER_HITBOX);
		
		// Text
		rbText = new RenderBatch(MAX_TEXTS, 16, 32, tc.cache("font.png").getID(), UPDATE_ALL);
		
		
		// Background (temp)
		Sprite bg = new Sprite("bg.png", 0, 0, 768, 896);
		tc.loadSprite(bg);
		
		RenderBatch rbBackground = new RenderBatch(1, 768, 896, bg.getTexture().getID(), UPDATE_NONE);
		rbBackground.updateManual(224, 240, bg.getTextureCoords());
		
		// Border
		Sprite border = new Sprite("border.png", 0, 0, 1280, 960);
		tc.loadSprite(border);
		
		RenderBatch rbBorder = new RenderBatch(1, 1280, 960, border.getTexture().getID(), UPDATE_NONE);
		rbBorder.updateManual(320, 240, border.getTextureCoords());
		
		
		// Add in order of rendering
		renderBatches.add(rbBackground);
		
		renderBatches.add(rbEnemies);
		renderBatches.add(rbPlayerBullets);
		renderBatches.add(rbPlayer);
		
		renderBatches.add(rbEnemyBullets1);
		renderBatches.add(rbEnemyBullets1a);
		renderBatches.add(rbEnemyBullets2);
		renderBatches.add(rbEnemyBullets2a);
		renderBatches.add(rbEnemyBulletsL);
		renderBatches.add(rbEnemyBulletsLa);
		
		renderBatches.add(rbEffects);
		
		renderBatches.add(rbHitboxes);
		renderBatches.add(rbLaserHitboxes);
		
		renderBatches.add(rbBorder);
		
		renderBatches.add(rbText);
		
		// Add batches to objects-only list
		objRenderBatches.add(rbEnemies);
		objRenderBatches.add(rbPlayerBullets);
		objRenderBatches.add(rbPlayer);
		
		objRenderBatches.add(rbEnemyBullets1);
		objRenderBatches.add(rbEnemyBullets1a);
		objRenderBatches.add(rbEnemyBullets2);
		objRenderBatches.add(rbEnemyBullets2a);
		objRenderBatches.add(rbEnemyBulletsL);
		objRenderBatches.add(rbEnemyBulletsLa);
		
		objRenderBatches.add(rbEffects);

		objRenderBatches.add(rbHitboxes);
		objRenderBatches.add(rbLaserHitboxes);
	}
	
	public void setTime(int time){
		this.time = time;
	}
	
	public void updatePlayer(Player player){
		rbPlayer.updateWithEntity(player, time);
	}
	
	public void updateEnemies(ArrayList<Enemy> enemies){
		rbEnemies.updateWithEntities(enemies, time);
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
		
		rbEnemyBullets1.updateWithEntities(b1, time);
		rbEnemyBullets1a.updateWithEntities(b1a, time);
		rbEnemyBullets2.updateWithEntities(b2, time);
		rbEnemyBullets2a.updateWithEntities(b2a, time);
		rbEnemyBulletsL.updateWithEntities(l, time);
		rbEnemyBulletsLa.updateWithEntities(la, time);
	}
	
	public void updatePlayerBullets(ArrayList<Bullet> bullets){
		rbPlayerBullets.updateWithEntities(bullets, time);
	}
	
	public void updateEffects(ArrayList<Effect> effects){
		rbEffects.updateWithEntities(effects, time);
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
				el.remove(i--);
				ll.add(e);
			}
		}
		
		rbHitboxes.updateHitboxes(el);
		rbLaserHitboxes.updateWithEntities(ll, 0);
	}
	
	public void updateText(ArrayList<Text> texts){
		
		ArrayList<TextChar> chars = new ArrayList<TextChar>();
		
		for(Text t:texts)
			if(t.isVisible())
				chars.addAll(t.getChars());
		
		rbText.updateWithEntities(chars, 0);
	}
	
	public void render(){
		for(int i = 0; i < renderBatches.size(); i++){
			
			RenderBatch rb = renderBatches.get(i);
			
			if(!renderObjects && objRenderBatches.contains(rb))
				continue;
			
			if(rb.getShader() == SHADER_STANDARD)
				basicShader.use();
			else if(rb.getShader() == SHADER_LASER){
				laserShader.use();
				glUniform1i(0, time);
			}
			else{
				if(!renderHitboxes)
					continue;
				
				if(rb.getShader() == SHADER_HITBOX)
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
	
	public void renderObjects(boolean renderObjects){
		this.renderObjects = renderObjects;
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
