package engine;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class GameThread extends JPanel implements Runnable{
	
	public void run(){
		
	}

	public void paint(Graphics g){
		super.paint(g);
		
		Graphics2D  g2d = (Graphics2D)g;
		
		g2d.fillRect(200, 200, 400, 400);
		
		repaint();
	}
}
