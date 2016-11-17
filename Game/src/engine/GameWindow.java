package engine;

import javax.swing.JFrame;

public class GameWindow extends JFrame{
	
	// TODO: Add key listener, mouse listener
	private final GameThread gameThread;
	
	public GameWindow(String name){
		super(name);
		// Init window
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.setSize(800, 600);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		
		// Init other stuff
		
		gameThread = new GameThread();
		this.add(gameThread);
		
		this.setVisible(true);
	}
	
}
