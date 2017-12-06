package engine.screens;

import java.io.File;
import java.util.ArrayList;

import org.lwjgl.glfw.GLFW;

import content.FrameList;
import engine.KeyboardListener;
import engine.entities.Bullet;
import engine.entities.Effect;
import engine.entities.Enemy;
import engine.entities.Laser;
import engine.entities.Player;
import engine.entities.Text;
import engine.graphics.Renderer;
import engine.graphics.TextureCache;

/*
 * 		MainScreen.java
 * 		
 * 		Purpose:	Controls the main game screen.
 * 		Notes:		Any gameplay will be processed by this class.
 * 		
 */

public class MainScreen extends GameScreen{
	
	public static final int MAX_ENEMY_BULLETS = 204800,
							MAX_PLAYER_BULLETS = 128,
							MAX_ENEMIES = 64,
							MAX_EFFECTS = 2048,
							MAX_TEXTS = 1024;
	
	private GameStage stage;
	
	private ArrayList<Bullet> enemyBullets, playerBullets;
	private ArrayList<Enemy> enemies;
	private ArrayList<Effect> effects;
	private ArrayList<Text> texts;
	private Text fpsText;
	
	private Player player;
	
	private int time, rTime;
	
	private boolean paused;
	private int pauseTime = -30;
	private boolean tickFrame;
	
	private int clearScreen;
	private boolean slowMode;
	
	// temp
	private Text scriptText;
	private ArrayList<String> scriptNames = new ArrayList<String>();
	private Text currentPath;
	private Text scriptCursor;
	private boolean scriptSelect;
	private int scriptCursorIndex;
	private String currentDir;
	
	public MainScreen(Renderer r, TextureCache tc){
		super(r, tc);
	}
	
	public void init(){
		
		enemyBullets	= new ArrayList<Bullet>(MAX_ENEMY_BULLETS);
		playerBullets	= new ArrayList<Bullet>(MAX_PLAYER_BULLETS);
		enemies			= new ArrayList<Enemy>(MAX_ENEMIES);
		effects			= new ArrayList<Effect>(MAX_EFFECTS);
		texts			= new ArrayList<Text>(MAX_TEXTS);

		fpsText = new Text("", 0, 470, 1, tc);
		addText(fpsText);
		
		time = 0;
		rTime = 0;
		paused = false;

		// Load textures
		
		r.initMainScreen();
		
		// Temporary test
		player = new Player(0, 0, new FrameList(tc), this);
		tc.loadSprite(player.getSprite());
		scriptSelectInit();
	}
	
	// temp
	private void scriptSelectInit(){
		
		paused = false;
		
		if(stage != null){
			stage.deleteErrorText();
			updateText();
		}
		
		scriptNames.clear();
		
		if(scriptText != null)
			scriptText.delete();
		
		if(currentPath != null)
			currentPath.delete();
		
		scriptText = new Text("", 50, 50, 0.75f, tc);
		currentPath = new Text("", 50, 32, 0.75f, tc);
		addText(scriptText);
		addText(currentPath);
		
		addFiles("");
		
		if(scriptCursor != null)
			scriptCursor.delete();
		
		scriptCursor = new Text(">", 40, 68, 0.75f, tc);
		addText(scriptCursor);
		
		scriptCursorIndex = 0;
		
		scriptSelect = true;
	}
	
	private void addFiles(String directory){
		
		File[] files = new File("Game/res/script/" + directory).listFiles();
		
		if(files == null)
			return;
		
		currentDir = directory;
		currentPath.setText("script/" + directory);
		
		scriptNames.clear();
		scriptText.setText("");
		
		// Add folders first
		for(File file:files){
			if(file.isDirectory() && !file.getName().equals(".ref")){
				scriptText.setText(scriptText.getText() + "\n" + file.getName() + "/");
				scriptNames.add(directory + file.getName() + "/");
			}
		}
		
		// Add files after
		for(File file:files){
			if(file.isFile() && file.getName().endsWith(".dscript")){

				scriptText.setText(scriptText.getText() + "\n" + file.getName());
				scriptNames.add(directory + file.getName());
			}
		}
	}

	public void setFPS(int fps){
		fpsText.setText(Integer.toString(fps));
		fpsText.setX(632 - fpsText.getWidth());
	}
	
	public void cleanup(){
		r.cleanup();
	}
	
	public void render(){
		
		r.setTime(rTime);
		r.updatePlayer(player);
		r.updateEnemyBullets(enemyBullets);
		r.updatePlayerBullets(playerBullets);
		r.updateEnemies(enemies);
		r.updateEffects(effects);
		r.updateHitboxes(enemyBullets, enemies, player);
		r.updateText(texts);
		
		r.render();
		
		if((!paused || tickFrame) && (!slowMode || (slowMode && time % 2 == 0))){
			rTime++;
			tickFrame = false;
		}
	}
	
	public void update(){
		
		// Re-select script with ~
		if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_GRAVE_ACCENT)){
			
			scriptSelect = !scriptSelect;
			
			if(stage == null){
				scriptSelect = true;
				return;
			}
			else if(scriptSelect){
				scriptSelectInit();
				
				for(Bullet b:enemyBullets)
					b.delete();
				for(Enemy e:enemies)
					e.delete();
				for(Effect e:effects)
					e.delete();
			}
			else{
				scriptText.delete();
				currentPath.delete();
				scriptNames.clear();
				scriptCursor.delete();
			}
		}
		
		if(scriptSelect){
			
			// Cursor up
			if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_UP)){
				if(scriptCursorIndex > 0)
					scriptCursorIndex--;
				else
					scriptCursorIndex = scriptNames.size() - 1;
			}
			
			// Cursor down
			if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_DOWN)){
				if(scriptCursorIndex < scriptNames.size() - 1)
					scriptCursorIndex++;
				else
					scriptCursorIndex = 0;
			}
			
			scriptCursor.setY(68 + scriptCursorIndex*18);
			
			if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_Z)){
				
				String script = scriptNames.get(scriptCursorIndex);
				
				if(script.endsWith("/")){
					scriptCursorIndex = 0;
					addFiles(script);
					return;
				}
				
				script = "Game/res/script/" + script;
				
				stage = new Mission(script, this, r, tc);
				stage.init();
				
				if(stage instanceof Mission)
					player = ((Mission)stage).getPlayer();
				
				scriptSelect = false;
				
				scriptText.delete();
				currentPath.delete();
				scriptNames.clear();
				scriptCursor.delete();
				
				return;
			}
			
			if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_X)){
				
				scriptCursorIndex = 0;
				
				if(currentDir.isEmpty())
					return;
				
				scriptCursorIndex = 0;
				
				String dir = currentDir.substring(0, currentDir.length() - 1);
				
				int i = dir.lastIndexOf('/');
				
				if(i > 0)
					dir = dir.substring(0, i);
				else
					dir = "";
				
				addFiles(dir);
			}
			
			return;
		}
		
		// Pause
		if(KeyboardListener.isKeyDown(GLFW.GLFW_KEY_ESCAPE) && time > pauseTime + 30){
			paused = !paused;
			pauseTime = time;
		}
		
		debugKeys();
		
		if((!paused || tickFrame) && (!slowMode || (slowMode && time % 2 == 0)))
			updateGameStage();
		
		time++;
	}
	
	private void debugKeys(){
		
		// Frame step with P
		if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_P))
			tickFrame = true;

		// Toggle hitboxes with H
		if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_H))
			r.toggleRenderHitboxes();

		// Slow down with D
		if(!paused && KeyboardListener.isKeyDown(GLFW.GLFW_KEY_D))
			slowMode = true;
		else
			slowMode = false;
		
		// Fast forward with F
		if(!paused && KeyboardListener.isKeyDown(GLFW.GLFW_KEY_F)){
			updateGameStage();
			updateGameStage();
			rTime += 2;
		}
		
		// Reload script with Alt+R
		if(KeyboardListener.isKeyDown(GLFW.GLFW_KEY_LEFT_ALT) && KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_R)){
			stage.reloadScript();
			clearScreen = 2;
		}
		
		// Clear bullets with Alt+C
		if(KeyboardListener.isKeyDown(GLFW.GLFW_KEY_LEFT_ALT) && (KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_C)))
			clearScreen = 1;
	}
	
	private void updateGameStage(){
		
		if(clearScreen > 0){

			for(Bullet b:enemyBullets){
				if(clearScreen == 1)
					b.onDestroy(true);
				if(clearScreen == 2)
					b.delete();
			}
			
			for(Enemy e:enemies){
				if(clearScreen == 1)
					e.onDestroy();
				if(clearScreen == 2)
					e.delete();
			}
			
			if(clearScreen == 2)
				for(Effect e:effects)
					e.delete();
			
			clearScreen = 0;
		}
		
		stage.update();
		
		if(stage instanceof Mission){
			updateEffects();
			updateBullets();
			updateEnemies();
			updateText();
			
			checkCollisions();
		}
	}
	
	private void updateBullets(){
		
		for(int i = 0; i < enemyBullets.size(); i++){
			
			if(enemyBullets.get(i).isDeleted()){
				enemyBullets.remove(i);
				
				// Update index, prevents objects from updating twice when another object is removed
				i--;
			}
			else// Update bullets
				enemyBullets.get(i).update();
		}
		
		for(int i = 0; i < playerBullets.size(); i++){
			
			if(playerBullets.get(i).isDeleted()){
				playerBullets.remove(i);
				i--;
			}
			else// Update bullets
				playerBullets.get(i).update();
		}
	}
	
	private void updateEnemies(){
		for(int i = 0; i < enemies.size(); i++){
			if(enemies.get(i).isDeleted()){
				enemies.remove(i);
				i--;
			}
			else
				enemies.get(i).update();
		}
	}
	
	private void updateEffects(){
		for(int i = 0; i < effects.size(); i++){
			if(effects.get(i).isDeleted()){
				effects.remove(i);
				i--;
			}
			else
				effects.get(i).update();
		}
	}
	
	private void updateText(){
		for(int i = 0; i < texts.size(); i++){
			if(texts.get(i).isDeleted()){
				texts.remove(i);
				i--;
			}
			else
				texts.get(i).update();
		}
	}
	
	private void checkCollisions(){
		float[] ppos = player.getPos();
		int pHitbox = player.getHitboxSize();
		
		// Enemy bullets
		for(Bullet b:enemyBullets){
			
			if(b.collisionsEnabled()){
				float[] bpos = b.getPos();
				
				// Bullet collisions
				if(!(b instanceof Laser)){
					if(Math.hypot(ppos[0] - bpos[0], ppos[1] - bpos[1]) < pHitbox + b.getHitboxSize()){
						player.death();
						b.onDestroy(false);
					}
				}
				
				// Laser collisions
				else{
					Laser l = (Laser)b;
					
					// Angle between laser and player
					double ang = (Math.atan2(bpos[1] - ppos[1], bpos[0] - ppos[0]) - Math.toRadians(l.getDir()));
					
					// Distance between laser base and player
					double d = Math.hypot(ppos[0] - bpos[0], ppos[1] - bpos[1]);
					
					// Perpendicular distance (actual distance to check)
					double d2 = (Math.abs(d*Math.sin(ang)));
					
					// Distance outwards from laser, used to 'crop' hitbox
					double d3 = (-d*Math.cos(ang));
					
					int crop = l.getHBLengthCrop();
					
					// Check collision
					if(d2 < pHitbox + l.getHitboxSize() && d3 > crop && d3 < l.getLength() - crop){
						player.death();
						b.onDestroy(false);
					}
				}
			}
		}
		
		// Enemy collisions
		for(Enemy e:enemies){
			
			if(e.collisionsEnabled()){
				float[] epos = e.getPos();
				
				if(Math.hypot(ppos[0] - epos[0], ppos[1] - epos[1]) < pHitbox + e.getHitboxSize()){
					player.death();
					e.damage(50);
					continue;
				}
				
				for(Bullet b:playerBullets){
					
					if(b.collisionsEnabled()){
						float[] bpos = b.getPos();
						
						if(Math.hypot(epos[0] - bpos[0], epos[1] - bpos[1]) < e.getHitboxSize() + b.getHitboxSize()){
							e.damage((int)b.getDamage());
							b.onDestroy(false);
						}
					}
				}
			}
		}
	}
	
	public void addEnemy(Enemy enemy){
		
		if(enemy == null || enemies.size() >= MAX_ENEMIES)
			return;
		
		enemies.add(enemy);
	}
	
	public void addEnemyBullet(Bullet bullet){
		
		if(bullet == null || enemyBullets.size() >= MAX_ENEMY_BULLETS)
			return;
		
		enemyBullets.add(bullet);
	}
	
	public void addPlayerBullet(Bullet bullet){
		
		if(bullet == null || playerBullets.size() >= MAX_PLAYER_BULLETS)
			return;
		
		playerBullets.add(bullet);
	}
	
	public void addEffect(Effect effect){
		
		if(effect == null || effects.size() >= MAX_EFFECTS)
			return;
		
		effects.add(effect);
	}
	
	public void addText(Text text){

		if(text == null || texts.size() >= MAX_TEXTS)
			return;
		
		texts.add(text);
	}
	
	public void clearEnemyBullets(){
		for(Bullet b:enemyBullets)
			b.onDestroy(true);
	}
	
	public TextureCache getTextureCache(){
		return tc;
	}
}
