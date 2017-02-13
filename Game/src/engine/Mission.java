package engine;

import java.util.ArrayList;

import engine.entities.Bullet;
import engine.entities.Enemy;
import engine.entities.Player;
import engine.graphics.Renderer;

public abstract class Mission extends GameStage{
	
	protected ArrayList<Bullet> bullets;
	protected ArrayList<Enemy> enemies;
	protected Player player;
	
	
	public Mission(Renderer r){
		super(r, TYPE_MISSION);
	}
	
	
	public ArrayList<Bullet> updatePlayer(){
		player.update();
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
