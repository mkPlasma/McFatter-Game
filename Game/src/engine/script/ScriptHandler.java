package engine.script;

import java.util.ArrayList;

import engine.entities.Text;
import engine.screens.MainScreen;
import engine.script.runner.Branch;
import engine.script.runner.ScriptRunner;

/**
 * 
 * Handles running of DScript. Controls ScriptRunner and ScriptBranches.
 * 
 * @author Daniel
 * 
 */

public class ScriptHandler{
	
	// How many frames to display active branches of
	private static final int ACTIVE_BRANCH_FRAMES = 20;
	
	
	private final MainScreen screen;
	
	private final Compiler compiler;
	private final ScriptRunner runner;

	private Text compileText;
	private Text errorText;
	private Text branchText;
	
	// Init on next frame
	private int init;
	
	// Stop running
	private boolean stop;
	
	// Running branches
	private ArrayList<Branch> branches;
	
	// Number of active branches for past frames
	private ArrayList<Integer> activeBranches;
	
	// Current running branch index
	private int currentBranch;
	
	private String scriptPath;
	private DScript script;
	
	
	public ScriptHandler(MainScreen screen){
		this.screen = screen;
		
		compiler = new Compiler();
		runner = new ScriptRunner(this);
		
		compileText = new Text("Compiling...", 40, 40, 0.75f, screen.getTextureCache());
		compileText.setVisible(false);
		screen.addText(compileText);
		
		errorText = new Text("", 40, 40, 0.75f, screen.getTextureCache());
		errorText.setVisible(false);
		screen.addText(errorText);
		
		branchText = new Text("Active Branches: \nTotal Branches: ", 430, 160, 0.75f, screen.getTextureCache());
		screen.addText(branchText);
		
		branches	= new ArrayList<Branch>();
		
		activeBranches = new ArrayList<Integer>(ACTIVE_BRANCH_FRAMES);
	}
	
	public void init(String scriptPath){
		this.scriptPath = scriptPath;
		initNextFrame();
	}
	
	public void reload(){
		initNextFrame();
	}
	
	private void initNextFrame(){
		init = 1;
		compileText.setVisible(true);
		errorText.setVisible(false);
	}
	
	private void init(){
		script = new DScript(scriptPath);
		
		compileText.setVisible(false);
		init = -1;
		
		try{
			compiler.compile(script);
		}
		catch(ScriptException e){
			compilationError(e);
			return;
		}
		
		runner.init(script);
		
		errorText.setVisible(false);
		
		branches.clear();
		branches.add(new Branch(script.getEntryPoint()));
		
		stop = false;
	}
	
	public void run(){
		
		if(init == 0)
			init();
		else if(init > 0){
			init--;
			return;
		}
		
		if(stop)
			return;
		
		for(currentBranch = 0; currentBranch < branches.size(); currentBranch++){
			
			Branch b = branches.get(currentBranch);
			
			try{
				runner.run(b);
			}
			catch(ScriptException e){
				runtimeError(e);
				return;
			}
			
			if(b.isFinished())
				branches.remove(currentBranch--);
		}
		
		setBranchText();
	}
	
	private void setBranchText(){
		
		activeBranches.add(runner.getActiveBranches());
		
		// Remove earliest number if past max frames
		if(activeBranches.size() > ACTIVE_BRANCH_FRAMES)
			activeBranches.remove(0);
		
		
		// Display highest active branch count of past frames
		int active = 0;
		
		for(int a:activeBranches)
			active = Math.max(active, a);
		
		branchText.setText("Active Branches: " + active + "\nTotal Branches: " + branches.size());
	}
	
	public void addBranch(Branch branch){
		branches.add(currentBranch + 1, branch);
	}
	
	public MainScreen getScreen(){
		return screen;
	}
	
	public void hideErrorText(){
		errorText.setVisible(false);
	}
	
	private void compilationError(ScriptException e){
		
		String file = e.getFile();
		int line = e.getLine();
		
		errorText.setVisible(true);
		errorText.setText(
			"Compilation error in " + (file == null ? script.getFileName(): file.substring(16)) +
			" on line " + line + ":\n" + e.getMessage() +
			(line > 0 ? "\n" + line + ": " + script.getLine(file, line) : "")
		);
		
		stop = true;
	}
	
	private void runtimeError(ScriptException e){
		
		int file = e.getFileIndex();
		int line = e.getLine();
		
		errorText.setVisible(true);
		errorText.setText(
			"Runtime error in " + (file == 0 ? script.getFileName(): script.getFileName(file)) +
			" on line " + line + ":\n" + e.getMessage() +
			(line > 0 ? "\n" + line + ": " + script.getLine(file, line) : "")
		);
		
		stop = true;
	}
}
