package engine.script;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

import org.lwjgl.glfw.GLFW;

import engine.KeyboardListener;
import engine.entities.Text;
import engine.screens.MainScreen;

/**
 * 
 * Script selection menu.
 * 
 * @author Daniel
 *
 */

public class ScriptSelector{
	
	private final MainScreen screen;
	private final ScriptHandler scriptHandler;
	
	private boolean selecting;
	
	// Current path
	private String directory;
	
	// Full file paths for current directory
	private ArrayList<String> files;
	
	// Cursor position for each directory
	private Stack<Integer> cursorPosStack;
	private int cursorPos;
	
	// Text objects
	private Text directoryText;
	private Text fileList;
	private Text cursor;
	
	
	public ScriptSelector(MainScreen screen, ScriptHandler scriptHandler){
		this.screen = screen;
		this.scriptHandler = scriptHandler;
	}
	
	public void init(){
		files = new ArrayList<String>();
		cursorPosStack = new Stack<Integer>();
		cursorPos = 0;
		
		directoryText	= new Text("", 60, 40, 0.75f, screen.getTextureCache());
		fileList		= new Text("", 60, 60, 0.75f, screen.getTextureCache());
		cursor			= new Text(">", 45, 0, 1, screen.getTextureCache());
		
		screen.addText(directoryText);
		screen.addText(fileList);
		screen.addText(cursor);
		
		selecting = true;
		createFileList("");
		
		setTextVisible(true);
		updateCursor();
	}
	
	public void createFileList(String directory){
		this.directory = directory;
		
		directoryText.setText("script/" + directory);
		files.clear();
		
		File[] fileArr = new File("Game/res/script/" + directory).listFiles();
		
		// Empty folder
		if(fileArr == null){
			fileList.setText("\n(empty)");
			return;
		}
		
		fileList.setText("");
		
		// Add folders first
		for(File file:fileArr){
			if(file.isDirectory() && !file.getName().equals(".ref")){
				
				fileList.setText(fileList.getText() + "\n" + file.getName() + "/");
				files.add(directory + file.getName() + "/");
			}
		}
		
		// Add files after
		for(File file:fileArr){
			if(file.isFile() && file.getName().endsWith(".dscript")){
				
				fileList.setText(fileList.getText() + "\n" + file.getName());
				files.add(directory + file.getName());
			}
		}
	}
	
	public void update(){
		
		// Select script or cancel with ~
		if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_GRAVE_ACCENT)){
			selecting = !selecting;
			setTextVisible(selecting);
			scriptHandler.hideErrorText();
			
			if(selecting)
				screen.pauseBGM();
			else
				screen.playBGM();
		}
		
		// Menu only
		if(!selecting)
			return;
		
		
		if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_DOWN)){
			cursorPos++;
			
			// Loop to top
			if(cursorPos >= files.size())
				cursorPos = 0;

			updateCursor();
		}
		else if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_UP)){
			cursorPos--;
			
			// Loop to bottom
			if(cursorPos < 0)
				cursorPos = files.size() - 1;
			
			updateCursor();
		}
		
		// Select
		if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_Z)){
			
			if(files.isEmpty())
				return;
			
			String file = files.get(cursorPos);
			
			// If folder
			if(file.endsWith("/")){
				
				createFileList(file);
				cursorPosStack.push(cursorPos);
				
				cursorPos = 0;
				updateCursor();
				
				return;
			}
			
			// If script
			scriptHandler.init(file);
			screen.resetPlayer();
			screen.clearAll();
			screen.unpause();
			
			selecting = false;
			setTextVisible(false);
			
			return;
		}
		
		// Up directory
		else if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_X)){
			
			if(directory.isEmpty())
				return;
			
			// Get upper directory
			String dir = directory.substring(0, directory.length() - 1);
			
			int i = dir.lastIndexOf('/');
			
			if(i > 0)
				dir = dir.substring(0, i + 1);
			else
				dir = "";
			
			createFileList(dir);
			
			// Set cursor position
			cursorPos = cursorPosStack.pop();
			
			while(cursorPos >= files.size())
				cursorPos--;
			
			updateCursor();
		}
	}
	
	private void setTextVisible(boolean visible){
		directoryText.setVisible(visible);
		fileList.setVisible(visible);
		cursor.setVisible(visible);
	}
	
	
	private void updateCursor(){
		cursor.setY(78 + cursorPos*18);
	}
	
	
	public boolean selecting(){
		return selecting;
	}
}
