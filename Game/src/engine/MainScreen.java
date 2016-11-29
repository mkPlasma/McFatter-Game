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
	}
	
	private int timer = 0;
	private int counter = 0;
	
	private Random random = new Random();
	
	public void update(){
		player.update();
		
		updateBullets();
		checkCollisions();
		
		// Bullet test
		timer++;
		if(timer > 60){
			timer = 0;
			counter++;
			
			int c = 64;
			
			for(int i = 0; i < c; i++){
				float dir = i*(360/(float)c);
				bullets.add(new Bullet(400, 100, random.nextInt(360), random.nextInt(4) + 3, Bullet.TYPE_ORB_L, 0));
			}
		}
	}
	
	private void updateBullets(){
		
		// Collision and edge deletion
		for(int i = 0; i < bullets.size(); i++){
			
			float x = bullets.get(i).getX();
			float y = bullets.get(i).getY();
			
			// Delete offscreen bullets
			if(x < -64 || x > 864 || y < -64 || y > 664){
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
			
			if(Math.hypot(ppos[0] - bpos[0], ppos[1] - bpos[1]) <= player.getHitboxSize() + b.getHitboxSize()){
				player.death();
				bullets.remove(i);
			}
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
