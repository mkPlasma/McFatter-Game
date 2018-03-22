package engine.screens;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import content.EffectList;
import content.FrameList;
import engine.KeyboardListener;
import engine.entities.Bullet;
import engine.entities.CollidableEntity;
import engine.entities.Effect;
import engine.entities.Enemy;
import engine.entities.Player;
import engine.entities.Text;
import engine.graphics.Renderer;
import engine.graphics.TextureCache;
import engine.script.ScriptHandler;
import engine.script.ScriptSelector;

/**
 * 
 * Handles main game screen where gameplay takes place.
 * 
 * @author Daniel
 *
 */

public class MainScreen extends GameScreen{
	
	public static final int MAX_ENEMY_BULLETS = 4096,
							MAX_PLAYER_BULLETS = 128,
							MAX_ENEMIES = 64,
							MAX_EFFECTS = 204800,
							MAX_TEXTS = 1024;
	
	private ScriptHandler scriptHandler;
	private ScriptSelector scriptSelector;
	
	private FrameList frameList;
	
	private ArrayList<Bullet> enemyBullets, playerBullets;
	private ArrayList<Enemy> enemies;
	private ArrayList<Effect> effects;
	private ArrayList<Text> texts;
	private Text fpsText;
	private Text pauseText;
	
	private Player player;
	
	private int time, rTime;
	
	private boolean paused;
	private boolean tickFrame;
	
	private int clearScreen;
	private boolean slowMode;
	
	
	public MainScreen(Renderer r, TextureCache tc){
		super(r, tc);
	}
	
	public void init(){
		
		frameList = new FrameList(tc);
		
		enemyBullets	= new ArrayList<Bullet>(MAX_ENEMY_BULLETS);
		playerBullets	= new ArrayList<Bullet>(MAX_PLAYER_BULLETS);
		enemies			= new ArrayList<Enemy>(MAX_ENEMIES);
		effects			= new ArrayList<Effect>(MAX_EFFECTS);
		texts			= new ArrayList<Text>(MAX_TEXTS);
		
		fpsText = new Text("", 0, 470, 1, tc);
		addText(fpsText);
		
		pauseText = new Text("Paused!", 430, 120, 1, tc);
		addText(pauseText);
		
		time = 0;
		rTime = 0;
		paused = false;

		// Init renderer
		r.initMainScreen();
		
		// Temporary test
		player = new Player(224, 432, this);
		tc.loadSprite(player.getSprite());
		
		
		scriptHandler = new ScriptHandler(this);
		scriptSelector = new ScriptSelector(this, scriptHandler);
		scriptSelector.init();
	}
	
	public void setFPS(int fps){
		fpsText.setText(Integer.toString(fps));
		fpsText.setX(632 - fpsText.getWidth());
	}
	
	public void cleanup(){
		r.cleanup();
	}
	
	public void render(){
		
		pauseText.setVisible(paused);
		
		r.setTime(rTime);
		r.updatePlayer(player);
		r.updateEnemyBullets(enemyBullets);
		r.updatePlayerBullets(playerBullets);
		r.updateEnemies(enemies);
		r.updateEffects(effects);
		r.updateHitboxes(enemyBullets, enemies, player);
		r.updateText(texts);
		
		r.renderObjects(!scriptSelector.selecting());
		
		r.render();
		
		if((!paused || tickFrame) && (!slowMode || (slowMode && time % 2 == 0))){
			rTime++;
			tickFrame = false;
		}
	}
	
	public void update(){
		
		scriptSelector.update();
		
		if(scriptSelector.selecting())
			return;
		
		
		// Pause
		if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_ESCAPE))
			paused = !paused;
		
		debugKeys();
		
		if((!paused || tickFrame) && (!slowMode || (slowMode && time % 2 == 0)))
			updateGameStage();
		
		time++;
	}
	
	private void debugKeys(){
		
		// Frame step with P
		if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_P))
			tickFrame = true;

		// Toggle hitboxes with H
		if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_H))
			r.toggleRenderHitboxes();

		// Slow down with D
		if(!paused && KeyboardListener.isKeyDown(GLFW.GLFW_KEY_D))
			slowMode = true;
		else
			slowMode = false;
		
		// Fast forward with F
		if(!paused && KeyboardListener.isKeyDown(GLFW.GLFW_KEY_F)){
			updateGameStage();
			updateGameStage();
			rTime += 2;
		}
		
		// Reload script with Alt+R
		if(KeyboardListener.isKeyDown(GLFW.GLFW_KEY_LEFT_ALT) && KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_R)){
			scriptHandler.reload();
			
			resetPlayer();
			clearAll();
			paused = false;
		}
		
		// Clear bullets with Alt+C
		if(KeyboardListener.isKeyDown(GLFW.GLFW_KEY_LEFT_ALT) && (KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_C)))
			clearScreen = 1;
	}
	
	private void updateGameStage(){
		
		clearScreen();
		
		player.update();
		
		scriptHandler.run();
		
		updateEffects();
		updateBullets();
		updateEnemies();
		updateText();
		
		checkCollisions();
	}
	
	public void resetPlayer(){
		player.setPos(224, 432);
		player.resetDeaths();
	}
	
	private void clearScreen(){
		
		if(clearScreen == 0)
			return;
		
		for(Bullet b:enemyBullets){
			if(clearScreen == 1)
				b.onDestroy(true);
			if(clearScreen == 2)
				b.delete();
		}
		
		for(Enemy e:enemies){
			if(clearScreen == 1)
				e.onDestroy();
			if(clearScreen == 2)
				e.delete();
		}
		
		if(clearScreen == 2)
			for(Effect e:effects)
				e.delete();
		
		clearScreen = 0;
	}
	
	public void clearAll(){
		
		for(Bullet b:enemyBullets)
			b.delete();
		
		for(Bullet b:playerBullets)
			b.delete();
		
		for(Enemy e:enemies)
			e.delete();
		
		for(Effect e:effects)
			e.delete();
	}
	
	public void clearEnemyBullets(){
		
		for(Bullet b:enemyBullets)
			b.onDestroy(true);
	}
	
	public void unpause(){
		paused = false;
	}
	
	private void updateBullets(){
		
		for(int i = 0; i < enemyBullets.size(); i++){
			
			if(enemyBullets.get(i).isDeleted()){
				enemyBullets.remove(i);
				
				// Update index, prevents objects from updating twice when another object is removed
				i--;
			}
			else// Update bullets
				enemyBullets.get(i).update();
		}
		
		for(int i = 0; i < playerBullets.size(); i++){
			
			if(playerBullets.get(i).isDeleted()){
				playerBullets.remove(i);
				i--;
			}
			else// Update bullets
				playerBullets.get(i).update();
		}
	}
	
	private void updateEnemies(){
		for(int i = 0; i < enemies.size(); i++){
			if(enemies.get(i).isDeleted()){
				enemies.remove(i);
				i--;
			}
			else
				enemies.get(i).update();
		}
	}
	
	private void updateEffects(){
		for(int i = 0; i < effects.size(); i++){
			if(effects.get(i).isDeleted()){
				effects.remove(i);
				i--;
			}
			else
				effects.get(i).update();
		}
	}
	
	private void updateText(){
		for(int i = 0; i < texts.size(); i++){
			if(texts.get(i).isDeleted()){
				texts.remove(i);
				i--;
			}
			else
				texts.get(i).update();
		}
	}
	
	private void checkCollisions(){
		float[] ppos = player.getPos();
		int pHitbox = player.getHitboxSize();
		
		// Player/Enemy bullets
		for(Bullet b:enemyBullets){
			
			if(b.collisionsEnabled()){
				float[] bpos = b.getPos();
				int offset = b.getHitboxOffset();
				float dir = (float)(Math.toRadians(b.getDir())%(2*Math.PI));
				
				if(offset != 0){
					bpos[0] += (float)(offset*Math.cos(dir));
					bpos[1] += (float)(offset*Math.sin(dir));
				}
				
				// Distance between bullet and player
				int dist = (int)Math.hypot(ppos[0] - bpos[0], ppos[1] - bpos[1]);
				
				int hitboxType = b.getHitboxType();
				int w = b.getHitboxWidth();
				
				// Circle/circle collisions
				if(hitboxType == CollidableEntity.HITBOX_CIRCLE){
					if(dist < pHitbox + w){
						player.death();
						b.onDestroy(false);
					}
					
					break;
				}
				
				int l = b.getHitboxLength();
				
				// Direction from bullet to player
				float ang = (float)(Math.atan2(ppos[1] - bpos[1], ppos[0] - bpos[0]));
				
				// Radius at angle
				int r = 0;
				
				// Circle/ellipse collisions
				if(hitboxType == CollidableEntity.HITBOX_ELLIPSE)
					r = (int)((w*l)/Math.sqrt(Math.pow(l*Math.cos(ang), 2) + Math.pow(w*Math.sin(ang), 2)));
				
				// Circle/rectangle collisions
				else{
					// Angle to corners of rectangle
					float cAng = (float)((Math.atan2(l, w) - dir - Math.PI/2)%(2*Math.PI));
					
					// Find radius based on which side player is closest to
					if(ang > Math.PI - cAng || ang < -Math.PI + cAng)
						r = (int)(-w/(Math.cos(ang)));
					
					else if(ang > cAng)
						r = (int)(l/(Math.sin(ang)));
					
					else if(ang > -cAng)
						r = (int)(w/(Math.cos(ang)));
					
					else
						r = (int)(-l/(Math.sin(ang)));
				}
				
				if(dist < pHitbox + r){
					player.death();
					b.onDestroy(false);
				}
				
				/*
				final int res = 5;
				
				for(int x = 32; x < 416; x += res){
					for(int y = 16; y < 464; y += res){
						
						// Direction from bullet to player
						ang = (float)(Math.atan2(y - bpos[1], x - bpos[0]) + dir);
						
						// Angle to corners of rectangle
						float cAng = (float)(Math.atan2(w, l) + dir);
						
						int col = 0;
						
						// Find radius based on which side player is closest to
						r = 0;
						if(ang > -cAng + 3*dir && ang < cAng + dir)
							r = 100000000;

						//else if(ang > -cAng + dir + Math.PI || (ang > -cAng + dir - Math.PI && ang < cAng + dir - Math.PI)){
						//	r = 100000000;
						//	col = 5;
						//}
						/*
						else if(ang > cAng){
							r = (int)(l/(Math.sin(ang)));
							col = 5;
						}
						
						else if(ang > -cAng){
							r = (int)(w/(Math.cos(ang)));
							col = 8;
						}
						
						else{
							r = (int)(-l/(Math.sin(ang)));
							col = 11;
						}
						
						dist = (int)Math.hypot(x - bpos[0], y - bpos[1]);

						if(dist < pHitbox + r){
							Effect e = new Effect(frameList.getEffect(EffectList.TYPE_FLARE, col, 0), x, y);
							e.getSprite().setScale(0.5f);
							e.setLifetime(1);
							addEffect(e);
						}
					}
				}
				*/
			}
		}
		
		// Enemy collisions
		for(Enemy e:enemies){
			
			if(e.collisionsEnabled()){
				float[] epos = e.getPos();
				
				if(Math.hypot(ppos[0] - epos[0], ppos[1] - epos[1]) < pHitbox + e.getHitboxWidth()){
					player.death();
					e.damage(50);
					continue;
				}
				
				for(Bullet b:playerBullets){
					
					if(b.collisionsEnabled()){
						float[] bpos = b.getPos();
						
						if(Math.hypot(epos[0] - bpos[0], epos[1] - bpos[1]) < e.getHitboxWidth() + b.getHitboxWidth()){
							e.damage((int)b.getDamage());
							b.onDestroy(false);
						}
					}
				}
			}
		}
	}
	
	public void addEnemy(Enemy enemy){
		
		if(enemy == null || enemies.size() >= MAX_ENEMIES)
			return;
		
		enemies.add(enemy);
	}
	
	public void addEnemyBullet(Bullet bullet){
		
		if(bullet == null || enemyBullets.size() >= MAX_ENEMY_BULLETS)
			return;
		
		enemyBullets.add(bullet);
	}
	
	public void addPlayerBullet(Bullet bullet){
		
		if(bullet == null || playerBullets.size() >= MAX_PLAYER_BULLETS)
			return;
		
		playerBullets.add(bullet);
	}
	
	public void addEffect(Effect effect){
		
		if(effect == null || effects.size() >= MAX_EFFECTS)
			return;
		
		effects.add(effect);
	}
	
	public void addText(Text text){

		if(text == null || texts.size() >= MAX_TEXTS)
			return;
		
		texts.add(text);
	}
	
	public TextureCache getTextureCache(){
		return tc;
	}
	
	public FrameList getFrameList(){
		return frameList;
	}
	
	public Player getPlayer(){
		return player;
	}
}
