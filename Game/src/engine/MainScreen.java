package engine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Random;

public class MainScreen extends GameScreen{
	
	private KeyboardListener keyListener;
	private Player player;
	
	private ArrayList<Bullet> bullets = new ArrayList<Bullet>();
	
	// Temporary test stuff
	private InstructionSet pset;
	private Bullet base;
	
	private int timer = 0;
	private int counter = 0;
	
	private Random random = new Random();
	
	private Enemy enemy;
	
	public void init(KeyboardListener keyListener){
		this.keyListener = keyListener;
		player = new Player(400, 400, keyListener);
		enemy = new Enemy();

		InstructionSet inst = new InstructionSet(1);
		inst.add(new BulletInstruction(null, 0, BulletInstruction.SET_POS, new double[]{400, 150}));
		inst.add(new BulletInstruction(null, 0, BulletInstruction.CONST_DIR_SPD, new double[]{0, 2}));
		
		base = new Bullet(inst, Bullet.TYPE_ORB_L, 0);
		
		pset = new InstructionSet(InstructionSet.INST_PATTERN);
		pset.add(new PatternInstruction(0, PatternInstruction.PATTERN_SPIRAL_STACKED, new double[]{24, 5, 0, 15, -1, 5, 1}, base));
	}
	
	
	public void update(){
		player.update();
		
		enemy.update();
		
		updateBullets();
		checkCollisions();
		
		
		//pset.run();
		//addBullets(pset.getBullets());
		
		//base.getInstructionSet().set(new BulletInstruction(base, 0, BulletInstruction.SET_DIR, new double[]{Math.atan2(player.getY() - 150, player.getX() - 400)}), 0);
		//base.getInstructionSet().init();

		// Bullet test
		
		timer++;
		if(timer > 15){
			timer = 0;
			counter++;
			
			int c = 16;
			int r = random.nextInt(30);
			
			for(int i = 0; i < c*0.75; i++){
				float dir = 90 + i*(360/(float)c) + r;
				
				InstructionSet inst = new InstructionSet(InstructionSet.INST_BULLET);
				inst.add(new BulletInstruction(null, 0, BulletInstruction.SET_POS, new double[]{400, 150}));
				inst.add(new BulletInstruction(null, 0, BulletInstruction.CONST_DIR_SPD, new double[]{dir, 2}));
				inst.add(new BulletInstruction(null, 0, BulletInstruction.SET_BOUNCES, new double[]{3, Bullet.BOUNCE_SIDES_TOP}));
				
				Bullet b = new Bullet(inst, Bullet.TYPE_ORB_L, 0);
				
				bullets.add(b);
			}
		}
		
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
		final double[] ppos = player.getPos();
		
		for(int i = 0; i < bullets.size(); i++){
			final Bullet b = bullets.get(i);
			final double bpos[] = b.getPos();
			
			// Hitbox size is slightly inaccurate, so it should be reduced
			if(Math.hypot(ppos[0] - bpos[0], ppos[1] - bpos[1]) < player.getHitboxSize() + b.getHitboxSize() - 2){
				//player.death();
				bullets.remove(i);
			}
		}
	}
	
	private void addBullets(ArrayList<Bullet> bullets){
		if(bullets == null || bullets.size() < 1)
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
