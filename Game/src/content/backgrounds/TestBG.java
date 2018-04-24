package content.backgrounds;

import engine.entities.BGEntity;
import engine.entities.Background;
import engine.graphics.Sprite;
import engine.graphics.TextureCache;

public class TestBG extends Background{
	
	// Layer sprites
	private Sprite stars, stars2, clouds;
	
	// Scale of each layer
	private final float spd1 = 0.1f, spd2 = 0.2f, spd3 = 0.4f;
	
	
	public TestBG(TextureCache tc){
		super(tc);
	}
	
	public void init(){
		elements.clear();
		
		setFogRange(5000, 10000);
		setFogColor(1, 0, 0, 1);
		
		// Load sprites
		stars = new Sprite("bg.png", 0, 0, 768, 896);
		tc.loadSprite(stars);
		
		stars2 = new Sprite("bg.png", 0, 896, 768, 896);
		tc.loadSprite(stars2);
		
		clouds = new Sprite("bg.png", 768, 0, 768, 896);
		clouds.setAlpha(0.5f);
		tc.loadSprite(clouds);
		
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
	}
	
	public void update(){
		updateElements();
		
		for(BGEntity e:elements)
			if(e.getY() > 448)
				e.setY(e.getY() - 896);
	}
}
