package engine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

public class MainScreen extends GameScreen{
	
	private KeyboardListener keyListener;
	private Player player;
	
	ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	
	public void init(KeyboardListener keyListener){
		this.keyListener = keyListener;
		player = new Player(400, 400, keyListener);
		enemy = new Enemy();
	}
	
	// Test stuff (temporary)
	private int timer = 0;
	private int counter = 0;
	
	private Random random = new Random();
	
	private Enemy enemy;
	
	public void update(){
		player.update();
		
		enemy.update();
		
		updateBullets();
		checkCollisions();
		
		
		// Bullet test

		InstructionSet inst = new InstructionSet();
		inst.add(new BulletInstruction(null, 0, BulletInstruction.SET_POS, new float[]{400, 150}));
		inst.add(new BulletInstruction(null, 0, BulletInstruction.CONST_SPD_DIR, new float[]{0, 2}));
		
		Bullet b = new Bullet(inst, Bullet.TYPE_ORB_L, 0);
		
		InstructionSet pset = new InstructionSet();
		pset.add(new PatternInstruction(0, PatternInstruction.PATTERN_CIRCLE, new float[]{32, 0}, b));
		
		pset.run();
		addBullets(pset.getBullets());
		
		/*
		timer++;
		if(timer > 15){
			timer = 0;
			counter++;
			
			int c = 16;
			int r = random.nextInt(360);
			
			for(int i = 0; i < c*0.75; i++){
				float dir = i*(360/(float)c) + r;
				
				InstructionSet inst = new InstructionSet();
				inst.add(new BulletInstruction(null, 0, BulletInstruction.SET_POS, new float[]{400, 150}));
				inst.add(new BulletInstruction(null, 0, BulletInstruction.CONST_SPD_DIR, new float[]{dir, 2}));
				inst.add(new BulletInstruction(null, 0, BulletInstruction.SET_BOUNCES, new float[]{3, Bullet.BOUNCE_SIDES_TOP}));
				
				Bullet b = new Bullet(inst, Bullet.TYPE_ORB_L, 0);
				
				bullets.add(b);
			}
		}
		*/
	}
	
	private void updateBullets(){
		
		// Collision and edge deletion
		for(int i = 0; i < bullets.size(); i++){
			
			if(bullets.get(i).remove()){
				bullets.remove(i);
			}
			else{// Update bullets
				bullets.get(i).update();
			}
		}
	}
	
	private void checkCollisions(){
		final float[] ppos = player.getPos();
		
		for(int i = 0; i < bullets.size(); i++){
			final Bullet b = bullets.get(i);
			final float bpos[] = b.getPos();
			
			// Hitbox size is slightly inaccurate, so it should be reduced
			if(Math.hypot(ppos[0] - bpos[0], ppos[1] - bpos[1]) < player.getHitboxSize() + b.getHitboxSize() - 2){
				//player.death();
				bullets.remove(i);
			}
		}
	}
	
	private void addBullets(ArrayList<Bullet> bullets){
		if(bullets.size() < 1)
			return;
		
		for(int i = 0; i < bullets.size(); i++){
			if(bullets.get(i) != null)
				this.bullets.add(bullets.get(i));
		}
	}
	
	
	public void draw(Graphics2D g2d){
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, 800, 600);
		
		drawBullets(g2d);
		player.draw(g2d);
	}
	
	private void drawBullets(Graphics2D g2d){
		
		for(int i = 0; i < bullets.size(); i++){
			bullets.get(i).draw(g2d);
		}
	}
}
