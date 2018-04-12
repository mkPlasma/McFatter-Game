package content.backgrounds;

import engine.entities.BGEntity;
import engine.entities.Background;
import engine.graphics.Sprite;
import engine.graphics.TextureCache;

public class TestBG extends Background{
	
	private BGEntity test;
	
	public TestBG(TextureCache tc){
		super(tc);
	}
	
	public void init(){
		
		Sprite s = new Sprite("bg.png", 0, 0, 768, 896);
		tc.loadSprite(s);
		
		test = new BGEntity(s, 224, 240, 1);
		//test.setVelZ(0.01f);
		
		elements.add(test);
	}
	
	public void update(){
		
		test.setRotX(test.getRotX() + 0.001f);
		
		updateElements();
	}
}
