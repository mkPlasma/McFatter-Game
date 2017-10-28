package engine.screens;

import java.util.ArrayList;

import content.BulletList;
import content.FrameList;
import engine.entities.Bullet;
import engine.entities.Enemy;
import engine.entities.Player;
import engine.graphics.Renderer;
import engine.graphics.TextureCache;

/*
 * 		Mission.java
 * 		
 * 		Purpose:	Abstract game mission.
 * 		Notes:		Bullets, enemies, bosses, and gameplay
 * 					will be handled here.
 * 		
 */

public class Mission extends GameStage{
	
	private ArrayList<Bullet> bullets;
	private ArrayList<Enemy> enemies;
	private Player player;
	
	private FrameList frameList;
	
	public Mission(String scriptPath, Renderer r, TextureCache tc){
		super(scriptPath, r, tc);
	}
	
	public void init(){
		super.init();
		
		frameList = new FrameList(tc);
		player = new Player(224, 450, frameList.getBullet(BulletList.TYPE_CRYSTAL, FrameList.COLOR_LIGHT_BLUE), frameList);
		
		tc.loadSprite(player.getSprite());
		
		scriptController.setPlayer(player);
		scriptController.setFrameList(frameList);
		
		bullets = new ArrayList<Bullet>();		
		enemies = new ArrayList<Enemy>();
	}
	
	public void update(){
		player.update();
		
		scriptController.run();
		
		bullets.addAll(scriptController.getBullets());
	}
	
	public void render(){
		
	}
	
	public ArrayList<Bullet> getPlayerBullets(){
		return player.getBullets();
	}
	
	public Player getPlayer(){
		return player;
	}
	
	public ArrayList<Enemy> getEnemies(){
		if(enemies == null || enemies.size() < 1)
			return null;
		
		ArrayList<Enemy> temp = new ArrayList<Enemy>(enemies);
		enemies.clear();
		
		return temp;
	}
	
	public ArrayList<Bullet> getBullets(){
		// Returns bullets then clears the array list
		// This is because the bullets on the frame should only be fired once
		// If not cleared, all previous bullets would keep firing forever
		
		if(bullets == null || bullets.size() < 1)
			return null;
		
		ArrayList<Bullet> temp = new ArrayList<Bullet>(bullets);
		bullets.clear();
		
		return temp;
	}
}
