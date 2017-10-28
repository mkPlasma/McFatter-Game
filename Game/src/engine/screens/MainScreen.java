package engine.screens;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import content.BulletList;
import engine.KeyboardListener;
import engine.entities.Bullet;
import engine.entities.Effect;
import engine.entities.Enemy;
import engine.entities.Laser;
import engine.entities.Player;
import engine.graphics.Renderer;
import engine.graphics.TextureCache;
import engine.graphics.Texture;

/*
 * 		MainScreen.java
 * 		
 * 		Purpose:	Controls the main game screen.
 * 		Notes:		Any gameplay or cutscenes will be
 * 					processed by this class.
 * 		
 */

public class MainScreen extends GameScreen{
	
	public static final int MAX_ENEMY_BULLETS = 1024,
							MAX_PLAYER_BULLETS = 128,
							MAX_ENEMIES = 64,
							MAX_EFFECTS = 1024;
	
	private GameStage stage;
	
	private ArrayList<Bullet> enemyBullets, playerBullets;
	private ArrayList<Enemy> enemies;
	private ArrayList<Effect> effects;
	
	private Player player;
	
	private int time, rTime;
	
	private boolean paused;
	private int pauseTime = -30;
	private boolean tickFrame;
	
	public MainScreen(Renderer r, TextureCache tc){
		super(r, tc);
	}
	
	public void init(){
		
		enemyBullets = new ArrayList<Bullet>();
		playerBullets = new ArrayList<Bullet>();
		enemies = new ArrayList<Enemy>();
		effects = new ArrayList<Effect>();
		
		time = 0;
		rTime = 0;
		paused = false;

		// Load textures
		int tPlayer = tc.cache("player.png").getID();
		int tBullets1 = tc.cache("bullets/01.png").getID();
		int tBullets2 = tc.cache("bullets/02.png").getID();
		int tEffects = tc.cache("effects.png").getID();
		
		r.initMainScreen(tPlayer, tBullets1, tBullets2, tEffects);
		
		// Temporary test
		stage = new Mission("Game/res/script/test.dscript", r, tc);
		stage.init();
		
		if(stage instanceof Mission)
			player = ((Mission)stage).getPlayer();
	}
	
	public void cleanup(){
		r.cleanup();
	}
	
	public void render(){
		
		r.updatePlayer(player, rTime);
		r.updateEnemyBullets(enemyBullets, rTime);
		r.updatePlayerBullets(playerBullets, rTime);
		r.updateEffects(effects, rTime);
		
		r.render();
		
		if(!paused || tickFrame)
			rTime++;
	}
	
	public void update(){
		
		// Pause
		if(KeyboardListener.isKeyDown(GLFW.GLFW_KEY_ESCAPE) && time > pauseTime + 30){
			paused = !paused;
			pauseTime = time;
		}
		
		// Tick frame with P
		if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_P))
			tickFrame = true;
		
		if(!paused || tickFrame){
			updateGameStage();
			tickFrame = false;
		}
		
		
		time++;
	}
	
	private void updateGameStage(){
		
		stage.update();
		
		if(stage instanceof Mission){
			Mission ms = (Mission)stage;

			updateEffects();
			updateBullets();
			updateEnemies();

			addEnemies(ms.getEnemies());
			addEnemyBullets(ms.getBullets());
			addPlayerBullets(ms.getPlayerBullets());
			
			checkCollisions();
		}
		
		// Reload script with Alt+R
		if(KeyboardListener.isKeyDown(GLFW.GLFW_KEY_LEFT_ALT) && KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_R)){
			stage.reloadScript();
			
			for(Bullet b:enemyBullets)
				b.onDestroy();
		}
		
		// Clear bullets with Alt+C
		if(KeyboardListener.isKeyDown(GLFW.GLFW_KEY_LEFT_ALT) && (KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_C))){
			System.out.println("Cleared screen!");
			
			for(Bullet b:enemyBullets)
				b.onDestroy();
		}
	}
	
	private void updateBullets(){
		
		for(int i = 0; i < enemyBullets.size(); i++){
			
			Effect e = enemyBullets.get(i).getEffect();
			
			if(e != null)
				effects.add(e);
			
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
	
	public void addEffect(Effect effect){
		
		if(effect == null || effects.size() >= MAX_EFFECTS)
			return;
		
		effects.add(effect);
		
		if(effects.size() >= MAX_EFFECTS)
			effects.remove(effects.size() - 1);
	}
}
