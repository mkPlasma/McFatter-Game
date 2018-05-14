package content.backgrounds;

import java.util.ArrayList;
import java.util.Random;

import engine.entities.BGEntity;
import engine.entities.Background;
import engine.graphics.Sprite;
import engine.graphics.TextureCache;

public class BGStage1 extends Background{
	
	private static final int MAX_ASTEROIDS = 55;
	
	// Planet/moon entities
	private BGEntity planet, moon;
	
	// Asteroid sprites
	private Sprite[] asteroidSpr;
	
	private int asterTime;
	
	// Asteroid list
	private ArrayList<BGEntity> asteroids;
	
	// Scale of each layer
	private final float spd1 = 0.05f, spd2 = 0.15f, spd3 = 0.25f;
	
	private boolean slow;
	
	private final Random random;
	
	
	public BGStage1(TextureCache tc){
		super(tc);
		
		asteroids = new ArrayList<BGEntity>();
		random = new Random();
	}
	
	public void init(){
		elements.clear();
		asteroids.clear();
		
		setFogRange(5000, 10000);
		setFogColor(1, 0, 0, 1);
		
		time = 0;
		slow = false;
		
		// Layer sprites
		Sprite stars = new Sprite("bg.png", 0, 0, 768, 896);
		tc.loadSprite(stars);
		
		Sprite stars2 = new Sprite("bg.png", 0, 896, 768, 896);
		tc.loadSprite(stars2);
		
		Sprite clouds = new Sprite("bg.png", 768, 0, 768, 896);
		//clouds.setAlpha(0.5f);
		tc.loadSprite(clouds);
		
		// Create layers
		for(int i = 0; i < 3; i++){

			Sprite spr = i == 0 ? stars : i == 1 ? stars2 : clouds;
			float spd = i == 0 ? spd1 : i == 1 ? spd2 : spd3;
			
			for(int j = 0; j < 2; j++){
				BGEntity e = new BGEntity(spr, 0, j*448, 416);
				e.getSprite().setScaleY((j%2 == 0 ? -1 : 1)*(i == 2 ? -1 : 1));
				e.setVelY(spd);
				
				elements.add(e);
			}
		}
		
		
		// Planet/moon
		Sprite spr = new Sprite("bg.png", 768, 896, 512, 512);
		tc.loadSprite(spr);
		spr.setScale(1.5f);
		
		planet = new BGEntity(spr, -64, -420, 416);
		planet.setVelY(0.095f);
		elements.add(planet);
		
		
		spr = new Sprite("bg.png", 1280, 896, 192, 192);
		tc.loadSprite(spr);
		
		moon = new BGEntity(spr, 32, -480, 416);
		moon.setVelY(0.115f);
		elements.add(moon);
		
		
		
		// Load asteroid sprites≈
		asteroidSpr = new Sprite[6];
		
		for(int x = 0; x < 2; x++){
			for(int y = 0; y < 3; y++){
				Sprite s = new Sprite("bg.png", 768 + x*32, 1408, 32, 32);
				tc.loadSprite(s);
				asteroidSpr[y*2 + x] = s;
			}
		}
		
		sortElements();
	}
	
	public void update(){
		updateElements();
		
		// Loop back layers
		for(int i = 0; i < elements.size(); i++){
			
			BGEntity e = elements.get(i);
			
			if(e.getY() > 448){
				
				// Remove if asteroid
				if(asteroids.contains(e)){
					asteroids.remove(e);
					elements.remove(i--);
					continue;
				}
				
				e.setY(e.getY() - 896);
			}
		}
		
		// Spawn asteroids
		if(time > 1770){
			asterTime--;
			
			if(asteroids.size() < MAX_ASTEROIDS && asterTime <= 0){
				BGEntity e = new BGEntity(asteroidSpr[random.nextInt(6)], random.nextInt(416) - 224, -240, 418);
				e.setVelY(0.5f + random.nextFloat()*0.5f);
				
				if(slow)
					e.setVelY(e.getVelY()/8);
				
				e.getSprite().setScale(0.3f + random.nextFloat()*0.3f);
				e.setRotZ((float)(random.nextFloat()*Math.PI*2));
				
				asteroids.add(e);
				elements.add(e);
				
				asterTime = slow ? 40 + random.nextInt(120) : 5 + random.nextInt(15);
			}
		}
		
		// Slow down
		if(time == 4667){
			slow = true;
			
			planet.setVelY(0);
			moon.setVelY(0);
			
			for(BGEntity e:elements)
				e.setVelY(e.getVelY()/8);
		}
		
		time++;
		
		sortElements();
	}
}
