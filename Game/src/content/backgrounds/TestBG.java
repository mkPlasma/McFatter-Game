package content.backgrounds;

import engine.entities.BGEntity;
import engine.entities.Background;
import engine.graphics.Sprite;
import engine.graphics.TextureCache;

public class TestBG extends Background{
	
	public TestBG(TextureCache tc){
		super(tc);
	}
	
	public void init(){

		Sprite s = new Sprite("bg.png", 0, 0, 768, 896);
		tc.loadSprite(s);
		
		elements.add(new BGEntity(s, 224, 240, 100));
	}
	
	public void update(){
		
		updateElements();
	}
}
