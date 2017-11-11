package engine.screens;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import engine.KeyboardListener;
import engine.entities.Bullet;
import engine.entities.Effect;
import engine.entities.Enemy;
import engine.entities.Laser;
import engine.entities.Player;
import engine.graphics.Renderer;
import engine.graphics.TextureCache;

/*
 * 		MainScreen.java
 * 		
 * 		Purpose:	Controls the main game screen.
 * 		Notes:		Any gameplay or cutscenes will be
 * 					processed by this class.
 * 		
 */

public class MainScreen extends GameScreen{
	
	public static final int MAX_ENEMY_BULLETS = 2048,
							MAX_PLAYER_BULLETS = 128,
							MAX_ENEMIES = 64,
							MAX_EFFECTS = 2048;
	
	private GameStage stage;
	
	private ArrayList<Bullet> enemyBullets, playerBullets;
	private ArrayList<Enemy> enemies;
	private ArrayList<Effect> effects;
	
	private Player player;
	
	private int time, rTime;
	
	private boolean paused;
	private int pauseTime = -30;
	private boolean tickFrame;
	
	private int clearScreen;
	private boolean slowMode;
	
	public MainScreen(Renderer r, TextureCache tc){
		super(r, tc);
	}
	
	public void init(){
		
		enemyBullets	= new ArrayList<Bullet>(MAX_ENEMY_BULLETS);
		playerBullets	= new ArrayList<Bullet>(MAX_PLAYER_BULLETS);
		enemies			= new ArrayList<Enemy>(MAX_ENEMIES);
		effects			= new ArrayList<Effect>(MAX_EFFECTS);
		
		time = 0;
		rTime = 0;
		paused = false;

		// Load textures
		
		r.initMainScreen(
			tc.cache("player.png").getID(),
			tc.cache("bullets/01.png").getID(),
			tc.cache("bullets/02.png").getID(),
			tc.cache("enemies.png").getID(),
			tc.cache("effects.png").getID()
		);
		
		// Temporary test
		stage = new Mission("Game/res/script/test.dscript", this, r, tc);
		stage.init();
		
		if(stage instanceof Mission)
			player = ((Mission)stage).getPlayer();
	}
	
	public void cleanup(){
		r.cleanup();
	}
	
	public void render(){
		
		r.setTime(rTime);
		r.updatePlayer(player);
		r.updateEnemyBullets(enemyBullets);
		r.updatePlayerBullets(playerBullets);
		r.updateEnemies(enemies);
		r.updateEffects(effects);
		r.updateHitboxes(enemyBullets, enemies, player);
		
		r.render();
		
		if((!paused || tickFrame) && (!slowMode || (slowMode && time % 2 == 0))){
			rTime++;
			tickFrame = false;
		}
	}
	
	public void update(){
		
		// Pause
		if(KeyboardListener.isKeyDown(GLFW.GLFW_KEY_ESCAPE) && time > pauseTime + 30){
			paused = !paused;
			pauseTime = time;
		}
		
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
			stage.reloadScript();
			clearScreen = 2;
		}
		
		// Clear bullets with Alt+C
		if(KeyboardListener.isKeyDown(GLFW.GLFW_KEY_LEFT_ALT) && (KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_C))){
			System.out.println("Cleared screen!");
			clearScreen = 1;
		}
	}
	
	private void updateGameStage(){
		
		if(clearScreen > 0){
			
			for(Bullet b:enemyBullets){
				if(clearScreen == 1)
					b.onDestroy(true);
				if(clearScreen == 2)
					b.delete();
			}
			
			if(clearScreen == 2)
				for(Effect e:effects)
					e.delete();
			
			clearScreen = 0;
		}
		
		stage.update();
		
		if(stage instanceof Mission){
			updateEffects();
			updateBullets();
			updateEnemies();
			
			checkCollisions();
		}
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
	
	private void checkCollisions(){
		float[] ppos = player.getPos();
		int pHitbox = player.getHitboxSize();
		
		// Enemy bullets
		for(int i = 0; i < enemyBullets.size(); i++){
			Bullet b = enemyBullets.get(i);
			
			if(b.collisionsEnabled()){
				float[] bpos = b.getPos();
				
				// Bullet collisions
				if(!(b instanceof Laser)){
					if(Math.hypot(ppos[0] - bpos[0], ppos[1] - bpos[1]) < pHitbox + b.getHitboxSize()){
						//player.death();
						b.onDestroy(false);
					}
				}
				
				// Laser collisions
				else{
					Laser l = (Laser)b;
					
					// Angle between laser and player
					double ang = (Math.atan2(bpos[1] - ppos[1], bpos[0] - ppos[0]) - Math.toRadians(l.getDir()));
					
					// Distance between laser base and player
					double d = Math.hypot(ppos[0] - bpos[0], ppos[1] - bpos[1]);
					
					// Perpendicular distance (actual distance to check)
					double d2 = (Math.abs(d*Math.sin(ang)));
					
					// Distance outwards from laser, used to 'crop' hitbox
					double d3 = (-d*Math.cos(ang));
					
					int crop = l.getHBLengthCrop();
					
					// Check collision
					if(d2 < pHitbox + l.getHitboxSize() && d3 > crop && d3 < l.getLength() - crop){
						//player.death();
						b.onDestroy(false);
					}
				}
			}
		}
		
		// Player bullets
		for(int i = 0; i < enemies.size(); i++){
			Enemy e = enemies.get(i);
			
			if(e.collisionsEnabled()){
				float[] epos = e.getPos();
				
				for(int j = 0; j < playerBullets.size(); j++){
					Bullet b = playerBullets.get(j);
					
					if(b.collisionsEnabled()){
						float[] bpos = b.getPos();
						
						if(Math.hypot(epos[0] - bpos[0], epos[1] - bpos[1]) < e.getHitboxSize() + b.getHitboxSize()){
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
}
