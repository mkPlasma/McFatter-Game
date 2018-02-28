package engine.newscript;

import java.util.ArrayList;

import engine.entities.Text;
import engine.newscript.runner.Branch;
import engine.newscript.runner.ScriptRunner;
import engine.screens.MainScreen;

/**
 * 
 * Handles running of DScript. Controls ScriptRunner and ScriptBranches.
 * 
 * @author Daniel
 * 
 */

public class ScriptHandler{
	
	private final MainScreen screen;
	
	private final Compiler compiler;
	private final ScriptRunner runner;
	
	private Text errorText;
	
	private boolean stop;
	
	// Running branches
	private ArrayList<Branch> branches;
	
	// Current running branch index
	private int currentBranch;
	
	private String scriptPath;
	private DScript script;
	
	
	public ScriptHandler(MainScreen screen){
		this.screen = screen;
		
		compiler = new Compiler();
		runner = new ScriptRunner(this);
		
		errorText = new Text("", 40, 40, 0.75f, screen.getTextureCache());
		errorText.setVisible(false);
		screen.addText(errorText);
		
		branches	= new ArrayList<Branch>();
	}
	
	public void init(String scriptPath){
		this.scriptPath = scriptPath;
		init();
	}
	
	private void init(){
		script = new DScript(scriptPath);
		
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
	}
	
	public void reload(){
		init();
	}
	
	public void addBranch(Branch branch){
		branches.add(currentBranch + 1, branch);
	}
	
	public MainScreen getScreen(){
		return screen;
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
