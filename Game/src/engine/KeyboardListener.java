package engine;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyboardListener implements KeyListener{
	
	// Holds booleans for key presses
	private boolean keys[] = new boolean[256];
	
	public void keyTyped(KeyEvent e){
		
	}
	
	public void keyPressed(KeyEvent e){
		keys[e.getKeyCode()] = true;
	}
	
	public void keyReleased(KeyEvent e){
		keys[e.getKeyCode()] = false;
	}
	
	public boolean isKeyPressed(int key){
		return keys[key];
	}

	public boolean isKeyReleased(int key){
		return !keys[key];
	}
}
