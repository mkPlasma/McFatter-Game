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
import engine.graphics.SpriteCache;

/*
 * 		MainScreen.java
 * 		
 * 		Purpose:	Controls the main game screen.
 * 		Notes:		Any gameplay or cutscenes will be
 * 					processed by this class.
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				
 * 		Changes:			
 */

public class MainScreen extends GameScreen{
	
	public static final int MAX_ENEMY_BULLETS = 2048,
							MAX_PLAYER_BULLETS = 128,
							MAX_ENEMIES = 64;
	
	private GameStage stage;
	
	private ArrayList<Bullet> enemyBullets, playerBullets;
	private ArrayList<Enemy> enemies;
	private ArrayList<Effect> effects;
	
	private Player player;
	
	private int time, rTime;
	
	private boolean paused;
	private int pauseTime = -30;
	
	private boolean clearingBullets = false;
	
	public MainScreen(Renderer r, SpriteCache sc){
		super(r, sc);
	}
	
	public void init(){
		
		enemyBullets = new ArrayList<Bullet>();
		playerBullets = new ArrayList<Bullet>();
		enemies = new ArrayList<Enemy>();
		effects = new ArrayList<Effect>();
		
		time = 0;
		rTime = 0;
		paused = false;
		
		// Temporary test
		stage = new Mission("Game/res/script/test.dscript", r, sc);
		stage.init();
		
		if(stage instanceof Mission)
			player = ((Mission)stage).getPlayer();
	}
	
	public void cleanup(){
		r.cleanup();
	}
	
	public void render(){
		
		r.renderPlayerBullets(playerBullets, rTime);
		r.renderPlayer(player, rTime);
		
		//stage.render();
		
		r.renderEnemyBullets(enemyBullets, rTime);
		
		//r.renderEnemies(enemies, rTime);
		
		if(!paused)
			rTime++;
	}
	
	public void update(){
		
		if(KeyboardListener.isKeyDown(GLFW.GLFW_KEY_ESCAPE) && time > pauseTime + 30){
			paused = !paused;
			pauseTime = time;
		}
		
		if(!paused)
			updateGameStage();
		
		time++;
	}
	
	private void updateGameStage(){
		
		stage.update();
		
		if(stage instanceof Mission){
			Mission ms = (Mission)stage;
			
			updateBullets();
			updateEnemies();
			updateEffects();

			addEnemies(ms.getEnemies());
			addEnemyBullets(ms.getBullets());
			addPlayerBullets(ms.getPlayerBullets());
			
			checkCollisions();
			
			// Clear bullets with Alt+C
			if(KeyboardListener.isKeyDown(GLFW.GLFW_KEY_LEFT_ALT) && KeyboardListener.isKeyDown(GLFW.GLFW_KEY_C)){
				if(!clearingBullets){
					
					System.out.println("Cleared screen!");
					clearingBullets = true;
					
					for(Bullet b:enemyBullets)
						b.onDestroy();
				}
			}
			else
				clearingBullets = false;
		}
	}
	
	private void updateBullets(){
		
		for(int i = 0; i < enemyBullets.size(); i++){
			
			if(enemyBullets.get(i).remove()){
				enemyBullets.remove(i);
				
				// Update index, prevents objects from updating twice when another object is removed
				i--;
			}
			else// Update bullets
				enemyBullets.get(i).update();
		}
		
		for(int i = 0; i < playerBullets.size(); i++){
			
			if(playerBullets.get(i).remove()){
				playerBullets.remove(i);
				i--;
			}
			else// Update bullets
				playerBullets.get(i).update();
		}
	}
	
	private void updateEnemies(){
		for(int i = 0; i < enemies.size(); i++){
			if(enemies.get(i).remove()){
				enemies.remove(i);
				i--;
			}
			else
				enemies.get(i).update();
		}
	}
	
	private void updateEffects(){
		for(int i = 0; i < effects.size(); i++){
			if(effects.get(i).remove()){
				effects.remove(i);
				i--;
			}
			else
				effects.get(i).update();
		}
	}
	
	private void checkCollisions(){
		final float[] ppos = player.getPos();
		final int pHitbox = player.getHitboxSize();
		
		// Enemy bullets
		for(int i = 0; i < enemyBullets.size(); i++){
			final Bullet b = enemyBullets.get(i);
			
			if(b.collisionsEnabled()){
				final float bpos[] = b.getPos();
				
				// Bullet collisions
				if(!(b instanceof Laser)){
					if(Math.hypot(ppos[0] - bpos[0], ppos[1] - bpos[1]) < pHitbox + b.getHitboxSize()){
						//player.death();
						b.onDestroy();
					}
				}
				
				// Laser collisions
				else{
					
					// Angle between laser and player
					float ang = (float)(Math.atan2(bpos[1] - ppos[1], bpos[0] - ppos[0]) - Math.toRadians(b.getDir()));
					
					// Distance between laser base and player
					float d = (float)Math.hypot(ppos[0] - bpos[0], ppos[1] - bpos[1]);
					
					// Perpendicular distance (actual distance to check)
					float d2 = (float)(Math.abs(d*Math.sin(ang)));
					
					// Distance outwards from laser, used to 'crop' hitbox
					float d3 = -(float)(d*Math.cos(ang));
					
					Laser l = (Laser)b;
					int crop = l.getHBLengthCrop();
					
					// Check collision
					if(d3 > crop && d3 < l.getLength() - crop && d2 < pHitbox + l.getHitboxSize()){
						//player.death();
						b.onDestroy();
					}
				}
			}
		}
		
		// Player bullets
		for(int i = 0; i < enemies.size(); i++){
			final Enemy e = enemies.get(i);
			
			if(e.collisionsEnabled()){
				final float[] epos = e.getPos();
				
				for(int j = 0; j < playerBullets.size(); j++){
					final Bullet b = playerBullets.get(j);
					
					if(b.collisionsEnabled()){
						final float bpos[] = b.getPos();
						
						if(Math.hypot(epos[0] - bpos[0], epos[1] - bpos[1]) < e.getHitboxSize() + b.getHitboxSize()){
							e.damage(b.getDamage());
							b.onDestroy();
						}
					}
				}
			}
		}
	}
	
	
	// Used to add player shots to the screen
	private void addPlayerBullets(ArrayList<Bullet> bullets){
		
		if(bullets == null || bullets.size() < 1 || playerBullets.size() >= MAX_PLAYER_BULLETS)
			return;
		
		playerBullets.addAll(bullets);
		
		while(playerBullets.size() >= MAX_PLAYER_BULLETS)
			playerBullets.remove(playerBullets.size() - 1);
	}
	
	// Used to add enemy shots to the screen
	private void addEnemyBullets(ArrayList<Bullet> bullets){
		
		if(bullets == null || bullets.size() < 1 || enemyBullets.size() >= MAX_ENEMY_BULLETS)
			return;
		
		enemyBullets.addAll(bullets);
		
		while(enemyBullets.size() >= MAX_ENEMY_BULLETS)
			enemyBullets.remove(enemyBullets.size() - 1);
	}
	
	private void addEnemies(ArrayList<Enemy> enemyList){
		
		if(enemyList == null || enemyList.size() < 1 || enemies.size() >= MAX_ENEMIES)
			return;
		
		enemies.addAll(enemyList);
		
		while(enemies.size() >= MAX_ENEMIES)
			enemies.remove(enemies.size() - 1);
	}
}
