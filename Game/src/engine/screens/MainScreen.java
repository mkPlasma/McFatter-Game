package engine.screens;

import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import content.TestMission;
import engine.KeyboardListener;
import engine.entities.Bullet;
import engine.entities.Effect;
import engine.entities.EffectGenerator;
import engine.entities.Enemy;
import engine.entities.Laser;

public class MainScreen extends GameScreen{
	
	private GameStage gs;
	
	private ArrayList<Bullet> enemyBullets, playerBullets;
	private ArrayList<Enemy> enemies;
	private ArrayList<Effect> effects;
	
	private int time;
	
	private boolean paused;
	private int pauseTime;
	
	public void init(){
		enemyBullets = new ArrayList<Bullet>();
		playerBullets = new ArrayList<Bullet>();
		enemies = new ArrayList<Enemy>();
		effects = new ArrayList<Effect>();
		
		time = 0;
		paused = false;
		
		// Temporary test
		gs = new TestMission();
		gs.init();
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
		gs.update();
		
		if(gs.getType() == GameStage.TYPE_MISSION){
			Mission ms = (Mission)gs;
			
			updateBullets();
			updateEnemies();
			ms.updatePlayer();
			updateEffects();
			
			checkCollisions();
			
			addEnemies(ms.getEnemies());
			addEnemyBullets(ms.getBullets());
			addPlayerBullets(ms.getPlayerBullets());
			
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
		final float[] ppos = ((Mission)gs).getPlayer().getPos();
		
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
					
					if(d3 > crop && d3 < l.getLength() - crop && d2 < ((Mission)gs).getPlayer().getHitboxSize() + l.getHitboxSize()){
						//player.death();
						b.onDestroy();
					}
				}
				else{
					if(Math.hypot(ppos[0] - bpos[0], ppos[1] - bpos[1]) < ((Mission)gs).getPlayer().getHitboxSize() + b.getBulletFrame().getHitboxSize()){
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
						
						if(Math.hypot(epos[0] - bpos[0], epos[1] - bpos[1]) < e.getHitboxSize() + b.getBulletFrame().getHitboxSize()){
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
	
	
	
	public void render(){
		
		drawGameStage();
		
		drawEnemies();
		drawBullets();
		drawEffects();
	}
	
	private void drawGameStage(){
		gs.render();
	}
	
	private void drawBullets(){
		for(int i = 0; i < enemyBullets.size(); i++){
			enemyBullets.get(i).render();
			
			// Laser collision debug
			/*
			if(enemyBullets.get(i) instanceof Laser){
				final float[] ppos = ((Mission)gs).getPlayer().getPos();
				final float bpos[] = enemyBullets.get(i).getPos();
				
				// angle between laser/player
				float ang = (float)(Math.atan2(bpos[1] - ppos[1], bpos[0] - ppos[0]) - Math.toRadians(enemyBullets.get(i).getDir()));
				
				// distance between laser/player
				float d = (float)Math.hypot(ppos[0] - bpos[0], ppos[1] - bpos[1]);
				
				// perpendicular distance
				float d2 = -(float)(d*Math.cos(ang));
				
				if(d2 < 0)
					d2 = 0;
				if(d2 > ((Laser)enemyBullets.get(i)).getLength())
					d2 = ((Laser)enemyBullets.get(i)).getLength();
				
				// angle to laser
				float ang2 = (float)Math.toRadians(enemyBullets.get(i).getDir());

				final float[] p = {(float)(bpos[0] + d2*Math.cos(ang2)), (float)(bpos[1] + d2*Math.sin(ang2))};
				//final float[] p = {(float)(ppos[0] + d2*Math.cos(ang2)), (float)(ppos[1] + d2*Math.sin(ang2))};
				
				r.drawLine((int)ppos[0], (int)ppos[1], (int)bpos[0], (int)bpos[1], Color.RED);
				r.drawLine((int)ppos[0], (int)ppos[1], (int)p[0], (int)p[1], Color.GREEN);
				r.drawLine((int)bpos[0], (int)bpos[1], (int)p[0], (int)p[1], Color.BLUE);
			}*/
		}
		
		for(int i = 0; i < playerBullets.size(); i++)
			playerBullets.get(i).render();
	}

	private void drawEnemies(){
		for(int i = 0; i < enemies.size(); i++)
			enemies.get(i).render();
	}
	
	private void drawEffects(){
		for(int i = 0; i < effects.size(); i++)
			effects.get(i).render();
	}
}
