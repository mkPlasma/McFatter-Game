package engine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import com.sun.glass.events.KeyEvent;

import content.TestMission;
import engine.entities.Bullet;
import engine.entities.Enemy;
import engine.entities.Player;
import engine.graphics.Renderer;

public class MainScreen extends GameScreen{
	
	private GameStage gs;
	
	private ArrayList<Bullet> enemyBullets, playerBullets;
	private ArrayList<Enemy> enemies;
	
	private int time;
	
	private boolean paused;
	private int pauseTime;
	
	public void init(Graphics2D g2d){
		
		enemyBullets = new ArrayList<Bullet>();
		playerBullets = new ArrayList<Bullet>();
		enemies = new ArrayList<Enemy>();
		
		r = new Renderer(g2d);
		
		time = 0;
		paused = false;
		
		// Temporary test
		gs = new TestMission(r);
		gs.init();
		
		//enemies.add(new Enemy(400, 200));
		
		// Obsolete pattern instruction test
		/*
		InstructionSet inst = new InstructionSet(1);
		inst.add(new BulletInstruction(null, 0, BulletInstruction.SET_POS, new double[]{400, 150}));
		inst.add(new BulletInstruction(null, 0, BulletInstruction.CONST_DIR_SPD, new double[]{0, 2}));
		base = new Bullet(inst, Bullet.TYPE_ORB_L, 0);
		
		pset = new InstructionSet(InstructionSet.INST_PATTERN);
		pset.add(new PatternInstruction(0, PatternInstruction.PATTERN_SPIRAL_STACKED, new double[]{24, 5, 0, 15, -1, 5, 1}, base));
		*/
	}
	
	
	public void update(){
		
		if(KeyboardListener.isKeyPressed(KeyEvent.VK_ESCAPE) && time > pauseTime + 30){
			paused = !paused;
			pauseTime = time;
		}
		
		if(!paused){
			updateGameStage();
			
			if(gs.getType() == GameStage.TYPE_MISSION){
				updateBullets();
				updateEnemies();
				checkCollisions();
			}
		}
		
		time++;
		
		// Obsolete pattern instruction test
		//pset.run();
		//addBullets(pset.getBullets());
		
		//base.getInstructionSet().set(new BulletInstruction(base, 0, BulletInstruction.SET_DIR, new double[]{Math.atan2(player.getY() - 150, player.getX() - 400)}), 0);
		//base.getInstructionSet().init();
		
		// Bullet test
		
	}
	
	private void updateGameStage(){
		gs.update();
		
		if(gs.getType() == GameStage.TYPE_MISSION){
			Mission ms = (Mission)gs;
			
			addEnemies(ms.getEnemies());
			addEnemyBullets(ms.getBullets());
			addPlayerBullets(ms.updatePlayer());
		}
	}
	
	private void updateBullets(){
		
		// Collision and edge deletion
		for(int i = 0; i < enemyBullets.size(); i++){
			
			if(enemyBullets.get(i).remove()){
				enemyBullets.remove(i);
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
			if(enemies.get(i).remove())
				enemies.remove(i);
			else{
				enemies.get(i).update();
			}
		}
	}
	
	private void checkCollisions(){
		final float[] ppos = ((Mission)gs).getPlayer().getPos();
		
		// Enemy bullets
		for(int i = 0; i < enemyBullets.size(); i++){
			final Bullet b = enemyBullets.get(i);
			
			if(b.collisionsEnabled()){
				final float bpos[] = b.getPos();
				
				if(Math.hypot(ppos[0] - bpos[0], ppos[1] - bpos[1]) < ((Mission)gs).getPlayer().getHitboxSize() + b.getBulletFrame().getHitboxSize()){
					//player.death();
					b.onDestroy();
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
	
	// Used to add enemies
	private void addEnemies(ArrayList<Enemy> enemies){
		if(enemies == null || enemies.size() < 1)
			return;
		
		for(int i = 0; i < enemies.size(); i++){
			if(enemies.get(i) != null)
				this.enemies.add(enemies.get(i));
		}
	}
	
	
	public void draw(){
		if(paused)
			return;

		r.drawRectangle(0, 0, 800, 600, Color.BLACK);
		
		drawGameStage();
		
		drawEnemies();
		drawBullets();
	}
	
	private void drawGameStage(){
		gs.draw();
	}
	
	private void drawBullets(){
		for(int i = 0; i < enemyBullets.size(); i++)
			enemyBullets.get(i).draw(r);
		
		for(int i = 0; i < playerBullets.size(); i++)
			playerBullets.get(i).draw(r);
	}
	
	private void drawEnemies(){
		for(int i = 0; i < enemies.size(); i++)
			enemies.get(i).draw(r);
	}
}
