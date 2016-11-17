package engine;

import javax.swing.JFrame;

public class GameWindow extends JFrame{
	
	// TODO: Add key listener, mouse listener, game thread
	private final GamePanel gamePanel;
	
	public GameWindow(String name){
		super(name);
		// Init window
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.setSize(800, 600);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		
		// Init other stuff
		
		gamePanel = new GamePanel();
		this.add(gamePanel);
		
		
		this.setVisible(true);
	}
	
}
