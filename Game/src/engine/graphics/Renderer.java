package engine.graphics;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL20.*;

import engine.entities.BGEntity;
import engine.entities.Background;
import engine.entities.Bullet;
import engine.entities.CollidableEntity;
import engine.entities.Effect;
import engine.entities.Enemy;
import engine.entities.GameEntity;
import engine.entities.Laser;
import engine.entities.Player;
import engine.entities.Text;
import engine.entities.TextChar;
import engine.graphics.RenderBatch;

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
						  squareHitboxShader,
						  shader3D;
	
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
		rbSquareHitboxes,
		
		rbBackground,
		rbBackground3D,
		
		rbText;
	
	private TextureCache tc;
	
	private int time;
	
	private Background bg;
	
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
		basicShader.bindAttrib(3, "rotation");
		basicShader.bindAttrib(4, "alpha");
		basicShader.link();
		
		laserShader = new ShaderProgram("laser", "laser", "basic");
		laserShader.bindAttrib(0, "position");
		laserShader.bindAttrib(1, "size");
		laserShader.bindAttrib(2, "texCoords");
		laserShader.bindAttrib(3, "rotation");
		laserShader.bindAttrib(4, "alpha");
		laserShader.bindAttrib(5, "segments");
		laserShader.link();
		
		hitboxShader = new ShaderProgram("circle", "circle", "hitbox");
		hitboxShader.bindAttrib(0, "position");
		hitboxShader.bindAttrib(1, "size");
		hitboxShader.bindAttrib(3, "rotation");
		hitboxShader.link();
		
		squareHitboxShader = new ShaderProgram("quad", "quad", "hitbox");
		squareHitboxShader.bindAttrib(0, "position");
		squareHitboxShader.bindAttrib(1, "size");
		squareHitboxShader.bindAttrib(2, "texCoords");
		squareHitboxShader.bindAttrib(3, "rotation");
		squareHitboxShader.bindAttrib(4, "alpha");
		squareHitboxShader.link();
		
		shader3D = new ShaderProgram("quad3d", "quad3d", "basicFog");
		shader3D.bindAttrib(0, "position");
		shader3D.bindAttrib(1, "size");
		shader3D.bindAttrib(2, "texCoords");
		shader3D.bindAttrib(3, "rotation");
		shader3D.bindAttrib(4, "alpha");
		shader3D.link();
		
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
		rbPlayerBullets = new RenderBatch(0, MAX_PLAYER_BULLETS, 32, bulletTex1, UPDATE_ALL, true);

		// Enemy bullets
		rbEnemyBullets1		= new RenderBatch(SHADER_STANDARD, MAX_ENEMY_BULLETS, 32, bulletTex1, UPDATE_ALL, false);
		rbEnemyBullets1a		= new RenderBatch(SHADER_STANDARD, MAX_ENEMY_BULLETS, 32, bulletTex1, UPDATE_ALL, true);
		rbEnemyBullets2		= new RenderBatch(SHADER_STANDARD, MAX_ENEMY_BULLETS, 32, bulletTex2, UPDATE_ALL, false);
		rbEnemyBullets2a		= new RenderBatch(SHADER_STANDARD, MAX_ENEMY_BULLETS, 32, bulletTex2, UPDATE_ALL, true);
		rbEnemyBulletsL		= new RenderBatch(SHADER_LASER, MAX_ENEMY_BULLETS, 32, bulletTex1, UPDATE_LASER, false);
		rbEnemyBulletsLa		= new RenderBatch(SHADER_LASER, MAX_ENEMY_BULLETS, 32, bulletTex1, UPDATE_LASER, true);
		
		// Enemies
		rbEnemies = new RenderBatch(SHADER_STANDARD, MAX_ENEMIES, 64, tc.cache("enemies.png").getID(), UPDATE_ALL, false);
		
		// Effects
		rbEffects = new RenderBatch(SHADER_STANDARD, MAX_EFFECTS, 32, tc.cache("effects.png").getID(), UPDATE_ALL, true);
		
		// Hitboxes
		rbHitboxes = new RenderBatch(SHADER_HITBOX, MAX_ENEMY_BULLETS + MAX_ENEMIES + 1, UPDATE_HITBOX);
		rbSquareHitboxes = new RenderBatch(SHADER_S_HITBOX, MAX_ENEMY_BULLETS, UPDATE_HITBOX);
		
		// Text
		rbText = new RenderBatch(MAX_TEXTS, 16, 32, tc.cache("font.png").getID(), UPDATE_ALL);
		
		
		// Background
		rbBackground3D = new RenderBatch(SHADER_3D, 32, 0, tc.cache("bg.png").getID(), UPDATE_ALL, false);
		
		// Border
		Sprite border = new Sprite("border.png", 0, 0, 1280, 960);
		tc.loadSprite(border);
		
		RenderBatch rbBorder = new RenderBatch(1, 1280, 960, border.getTexture().getID(), UPDATE_NONE);
		rbBorder.updateManual(320, 240, border.getTextureCoords());
		
		
		// Add in order of rendering
		renderBatches.add(rbBackground3D);
		
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
		renderBatches.add(rbSquareHitboxes);
		
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
		objRenderBatches.add(rbSquareHitboxes);
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
		
		ArrayList<GameEntity> el2 = new ArrayList<GameEntity>();
		
		for(int i = 0; i < el.size(); i++){
			GameEntity e = el.get(i);
			
			if(e instanceof CollidableEntity && ((CollidableEntity)e).getHitboxType() == CollidableEntity.HITBOX_RECTANGLE){
				el.remove(i--);
				el2.add(e);
			}
		}
		
		rbHitboxes.updateHitboxes(el);
		rbSquareHitboxes.updateWithEntities(el2, 0);
	}
	
	public void updateText(ArrayList<Text> texts){
		
		ArrayList<TextChar> chars = new ArrayList<TextChar>();
		
		for(Text t:texts)
			if(t.isVisible())
				chars.addAll(t.getChars());
		
		rbText.updateWithEntities(chars, 0);
	}
	
	public void updateBG(Background bg){
		this.bg = bg;
		
		ArrayList<BGEntity> elements = bg.getElements();
		rbBackground3D.updateWithEntities(elements, time);
	}
	
	public void render(){
		for(int i = 0; i < renderBatches.size(); i++){
			
			RenderBatch rb = renderBatches.get(i);
			RenderBatch pr = i > 0 ? renderBatches.get(i - 1) : null;
			
			if(!renderObjects && objRenderBatches.contains(rb))
				continue;
			
			if(rb.getShader() == SHADER_STANDARD)
				basicShader.use();
			else if(rb.getShader() == SHADER_LASER){
				laserShader.use();
				
				if(pr.getShader() != SHADER_LASER)
					glUniform1i(0, time);
			}
			else if(rb.getShader() == SHADER_HITBOX || rb.getShader() == SHADER_S_HITBOX){
				if(!renderHitboxes)
					continue;
				
				if(rb.getShader() == SHADER_HITBOX)
					hitboxShader.use();
				else
					squareHitboxShader.use();
			}
			else if(rb.getShader() == SHADER_3D){
				shader3D.use();
				
				BGEntity cam = bg.getCamera();
				
				// Uniform camera position/rotation
				glUniform3f(shader3D.getUniformLocation("camPosition"), cam.getX(), cam.getY(), cam.getZ());
				glUniform3f(shader3D.getUniformLocation("camRotation"), (float)Math.toRadians(cam.getRotX()), (float)Math.toRadians(cam.getRotY()), (float)Math.toRadians(cam.getRotZ()));
				
				// Uniform fog range
				glUniform2f(shader3D.getUniformLocation("fogRange"), bg.getFogStart(), bg.getFogMax());
				
				// Uniform fog color
				glUniform4f(shader3D.getUniformLocation("fogColor"), bg.getFogR(), bg.getFogG(), bg.getFogB(), bg.getFogA());
			}
			
			
			// Don't bind texture again if last batch had same texture
			if(rb.getTextureID() != -1 && (pr == null || rb.getTextureID() != pr.getTextureID()))
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
