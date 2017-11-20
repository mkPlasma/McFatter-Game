package engine.screens;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

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
 * 		Notes:		Any gameplay or cutscenes will be
 * 					processed by this class.
 * 		
 */

public class MainScreen extends GameScreen{
	
	public static final int MAX_ENEMY_BULLETS = 2048,
							MAX_PLAYER_BULLETS = 128,
							MAX_ENEMIES = 64,
							MAX_EFFECTS = 2048,
							MAX_TEXT = 1024;
	
	private GameStage stage;
	
	private ArrayList<Bullet> enemyBullets, playerBullets;
	private ArrayList<Enemy> enemies;
	private ArrayList<Effect> effects;
	private ArrayList<Text> text;
	private ArrayList<Text> fpsText;
	
	private Player player;
	
	private int time, rTime;
	
	private boolean paused;
	private int pauseTime = -30;
	private boolean tickFrame;
	
	private int clearScreen;
	private boolean slowMode;

	// temp
	private Stack<ArrayList<Text>> scriptTexts = new Stack<ArrayList<Text>>();
	private Stack<ArrayList<String>> scriptNames = new Stack<ArrayList<String>>();
	private Stack<Integer> numScripts = new Stack<Integer>();
	private Stack<ArrayList<Text>> currentPath = new Stack<ArrayList<Text>>();
	private Text scriptCursor;
	private boolean scriptSelect;
	private int scriptCursorIndex;
	
	public MainScreen(Renderer r, TextureCache tc){
		super(r, tc);
	}
	
	public void init(){
		
		enemyBullets	= new ArrayList<Bullet>(MAX_ENEMY_BULLETS);
		playerBullets	= new ArrayList<Bullet>(MAX_PLAYER_BULLETS);
		enemies			= new ArrayList<Enemy>(MAX_ENEMIES);
		effects			= new ArrayList<Effect>(MAX_EFFECTS);
		text			= new ArrayList<Text>(MAX_TEXT);
		fpsText			= new ArrayList<Text>(2);
		
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
		
		text.clear();
		scriptTexts.clear();
		scriptNames.clear();
		numScripts.clear();
		currentPath.clear();
		
		addFiles("");
		
		scriptCursor = addText(">", 40, 50, -1, 0.75f, 0).get(0);
		scriptCursorIndex = 0;
		
		scriptSelect = true;
	}
	
	private void addFiles(String directory){
		
		File[] files = new File("Game/res/script/" + directory).listFiles();
		
		if(files == null)
			return;
		
		if(!scriptTexts.isEmpty())
			for(Text t:scriptTexts.peek())
				t.setVisible(false);
		
		scriptTexts.push(new ArrayList<Text>());
		scriptNames.push(new ArrayList<String>());
		numScripts.push(0);
		
		currentPath.push(addText("script/" + directory, 50, 32, -1, 0.75f, 0));
		
		// Add folders first
		for(File file:files){
			if(file.isDirectory() && !file.getName().equals(".ref")){
				scriptTexts.peek().addAll(addText(file.getName() + "/", 50, 50 + numScripts.peek()*12, -1, 0.75f, 0));
				scriptNames.peek().add(directory + file.getName() + "/");
				
				numScripts.push(numScripts.pop() + 1);
			}
		}
		
		// Add files after
		for(File file:files){
			if(file.isFile() && file.getName().endsWith(".dscript")){
				
				scriptTexts.peek().addAll(addText(file.getName(), 50, 50 + numScripts.peek()*12, -1, 0.75f, 0));
				scriptNames.peek().add(directory + file.getName());

				numScripts.push(numScripts.pop() + 1);
			}
		}
	}
	
	public void setFPS(int fps){
		
		for(Text t:fpsText)
			t.delete();
		
		fpsText.clear();
		
		fpsText = addText(Integer.toString(fps), 624, 472, -1, 1, 0);
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
		r.updateText(text);
		
		r.render();
		
		if((!paused || tickFrame) && (!slowMode || (slowMode && time % 2 == 0))){
			rTime++;
			tickFrame = false;
		}
	}
	
	public void update(){
		
		if(scriptSelect){
			
			// Cursor up
			if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_UP)){
				if(scriptCursorIndex > 0)
					scriptCursorIndex--;
				else
					scriptCursorIndex = numScripts.peek() - 1;
			}
			
			// Cursor down
			if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_DOWN)){
				if(scriptCursorIndex < numScripts.peek() - 1)
					scriptCursorIndex++;
				else
					scriptCursorIndex = 0;
			}
			
			scriptCursor.setY(50 + scriptCursorIndex*12);
			
			if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_Z)){
				
				String script = scriptNames.peek().get(scriptCursorIndex);
				
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
				
				for(ArrayList<Text> tx:scriptTexts)
					for(Text t:tx)
						t.delete();
				
				for(ArrayList<Text> tx:currentPath)
					for(Text t:tx)
						t.delete();
				
				scriptTexts.clear();
				scriptNames.clear();
				numScripts.clear();
				currentPath.clear();
				
				scriptCursor.delete();
				
				return;
			}
			
			if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_X)){
				
				scriptCursorIndex = 0;
				
				if(scriptTexts.size() == 1)
					return;
				
				for(Text t:scriptTexts.peek())
					t.delete();
				
				scriptTexts.pop();
				scriptNames.pop();
				numScripts.pop();
				
				if(!currentPath.isEmpty())
					for(Text t:currentPath.peek())
						t.delete();
				
				currentPath.pop();
				
				for(Text t:scriptTexts.peek())
					t.setVisible(true);
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
		
		// Re-select script with ~
		if(KeyboardListener.isKeyPressed(GLFW.GLFW_KEY_GRAVE_ACCENT)){
			scriptSelectInit();
			
			for(Bullet b:enemyBullets)
				b.delete();
			for(Enemy e:enemies)
				e.delete();
			for(Effect e:effects)
				e.delete();
		}
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
		for(int i = 0; i < text.size(); i++){
			if(text.get(i).isDeleted()){
				text.remove(i);
				i--;
			}
			else
				text.get(i).update();
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
	
	public ArrayList<Text> addText(String text, float x, float y, int wrap, float scale, int lifetime){
		
		ArrayList<Text> tx = new ArrayList<Text>();
		
		if(text == null || text.isEmpty() || this.text.size() >= MAX_TEXT)
			return tx;
		
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
				if(xl > wrap){
					wrapPoints.add(lastWord);
					xl = 0;
				}
			}
		}
		
		int line = 0;
		
		for(int i = 0; i < text.length(); i++){
			
			char c = text.charAt(i);
			
			// Newline/wrap point
			if(c == '\n' || wrapPoints.contains(i)){
				xl = 0;
				line++;
				
				if(c == '\n')
					continue;
			}
			
			// Create text object
			Text t = new Text(x + xl*(7*scale), y + line*(24*scale), c, lifetime, tc);
			t.getSprite().setScale(scale);
			
			tx.add(t);
			xl++;
			
			if(this.text.size() >= MAX_TEXT)
				return tx;
		}
		
		this.text.addAll(tx);
		return tx;
	}
}
