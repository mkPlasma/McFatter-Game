package engine.entities;

import java.util.ArrayList;

import engine.graphics.TextureCache;

/**
 * 
 * Container for a drawable string object, made of TextChar objects.
 * 
 * @author Daniel
 *
 */

public class Text extends GameEntity{
	
	private String text;
	private int wrap;
	private float scale;
	
	private int lifetime;
	
	private int width, height;
	
	private ArrayList<TextChar> chars;
	
	private final TextureCache tc;
	
	public Text(String text, float x, float y, float scale, TextureCache tc){
		this(text, x, y, -1, scale, -1, tc);
	}
	
	public Text(String text, float x, float y, int wrap, float scale, int lifetime, TextureCache tc){
		super(x, y);
		this.text = text;
		this.lifetime = lifetime;
		this.wrap = wrap;
		this.scale = scale;
		this.tc = tc;
		
		chars = new ArrayList<TextChar>();
		genChars();
	}
	
	private void genChars(){
		
		chars.clear();
		
		width = 0;
		height = 0;
		
		// Find where to wrap text
		ArrayList<Integer> wrapPoints = new ArrayList<Integer>();
		
		int xl = 0;
		int lastWord = 0;
		
		if(wrap >= 0){
			for(int i = 0; i < text.length(); i++){
				
				char c = text.charAt(i);
				
				// Reset on line break
				if(c == '\n'){
					xl = 0;
					continue;
				}
				
				// Wrap at last space
				else if(c == ' '){
					lastWord = i + 1;
				}
				
				xl++;
				
				// Set wrap point
				if(xl > wrap && !wrapPoints.contains(lastWord) && lastWord > 0){
					wrapPoints.add(lastWord);
					xl = 0;
				}
			}
		}
		
		int line = 0;
		xl = 0;
		
		for(int i = 0; i < text.length(); i++){
			
			char c = text.charAt(i);
			
			if(c == ' '){
				xl++;
				continue;
			}
			
			// Newline/wrap point
			if(c == '\n' || wrapPoints.contains(i)){
				width = (int)Math.max(xl*(7*scale), width);
				
				xl = 0;
				line++;
				
				if(c == '\n')
					continue;
			}
			
			// Create text object
			TextChar t = new TextChar(x + xl*(int)(7*scale), y + line*(int)(24*scale), c, scale, tc);
			chars.add(t);
			xl++;
		}
		
		if(width == 0)
			width = (int)Math.max(xl*(7*scale), width);
		
		height = (int)(line*(24*scale));
	}
	
	public void update(){
		time++;
		
		if(lifetime > 0 && time >= lifetime)
			deleted = true;
	}
	
	public void setText(String text){
		this.text = text;
		genChars();
	}
	
	public String getText(){
		return text;
	}
	
	public void setX(float x){
		float dx = x - this.x;
		this.x = x;
		
		for(TextChar c:chars)
			c.setX(c.getX() + dx);
	}
	
	public void setY(float y){
		float dy = y - this.y;
		this.y = y;
		
		for(TextChar c:chars)
			c.setY(c.getY() + dy);
	}
	
	public int getWidth(){
		return width;
	}
	
	public int getHeight(){
		return height;
	}
	
	public ArrayList<TextChar> getChars(){
		return chars;
	}
}
