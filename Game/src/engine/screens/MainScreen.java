package engine.screens;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import engine.KeyboardListener;
import engine.entities.Bullet;
import engine.entities.Effect;
import engine.entities.EffectGenerator;
import engine.entities.Enemy;
import engine.entities.Laser;
import engine.graphics.Renderer;

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
	
	private GameStage stage;
	
	private Renderer r;
	
	private ArrayList<Bullet> enemyBullets, playerBullets;
	private ArrayList<Enemy> enemies;
	private ArrayList<Effect> effects;
	
	private int time, rTime;
	
	private boolean paused;
	private int pauseTime;
	
	public void init(){
		enemyBullets = new ArrayList<Bullet>();
		playerBullets = new ArrayList<Bullet>();
		enemies = new ArrayList<Enemy>();
		effects = new ArrayList<Effect>();
		
		time = 0;
		rTime = 0;
		paused = false;
		
		r = new Renderer();
		r.init();
		
		// Temporary test
		stage = new Mission("Game/res/script/test.dscript");
		stage.init();
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
			
			addEffects(EffectGenerator.getEffects());
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
		final float[] ppos = ((Mission)stage).getPlayer().getPos();
		
		// Enemy bullets
		for(int i = 0; i < enemyBullets.size(); i++){
			final Bullet b = enemyBullets.get(i);
			
			if(b.collisionsEnabled()){
				final float bpos[] = b.getPos();
				
				if(b instanceof Laser){
					
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
					
					if(d3 > crop && d3 < l.getLength() - crop && d2 < ((Mission)stage).getPlayer().getHitboxSize() + l.getHitboxSize()){
						//player.death();
						b.onDestroy();
					}
				}
				else{
					if(Math.hypot(ppos[0] - bpos[0], ppos[1] - bpos[1]) < ((Mission)stage).getPlayer().getHitboxSize() + b.getHitboxSize()){
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
		if(bullets == null || bullets.size() < 1)
			return;
		
		for(int i = 0; i < bullets.size(); i++){
			if(bullets.get(i) != null)
				playerBullets.add(bullets.get(i));
		}
	}
	
	// Used to add enemy shots to the screen
	private void addEnemyBullets(ArrayList<Bullet> bullets){
		if(bullets == null || bullets.size() < 1)
			return;
		
		for(int i = 0; i < bullets.size(); i++){
			if(bullets.get(i) != null)
				enemyBullets.add(bullets.get(i));
		}
	}
	
	private void addEnemies(ArrayList<Enemy> enemies){
		if(enemies == null || enemies.size() < 1)
			return;
		
		for(int i = 0; i < enemies.size(); i++){
			if(enemies.get(i) != null)
				this.enemies.add(enemies.get(i));
		}
	}
	
	private void addEffects(ArrayList<Effect> effects){
		if(effects == null || effects.size() < 1)
			return;
		
		for(int i = 0; i < effects.size(); i++){
			if(effects.get(i) != null)
				this.effects.add(effects.get(i));
		}
	}
	
	
	int j = 0;
	public void render(){
		
		drawGameStage();
		
		drawEnemies();
		drawBullets();
		//drawEffects();
		
		if(!paused)
			rTime++;
	}
	
	private void drawGameStage(){
		stage.render();
	}
	
	private void drawBullets(){
		r.renderBullets(playerBullets, rTime);
		r.renderBullets(enemyBullets, rTime);
	}

	private void drawEnemies(){
		r.renderEnemies(enemies, rTime);
	}
	
	private void drawEffects(){
		for(int i = 0; i < effects.size(); i++)
			effects.get(i).render();
	}
	
	
	
	public ArrayList<Enemy> getEnemies(){
		return enemies;
	}
}
