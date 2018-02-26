package engine.newscript;

import java.util.ArrayList;

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
	
	// Running branches
	private ArrayList<Branch> branches;
	
	// Current running branch index
	private int currentBranch;
	
	private DScript script;
	
	
	public ScriptHandler(MainScreen screen){
		this.screen = screen;
		
		compiler = new Compiler();
		runner = new ScriptRunner(this);
		
		branches	= new ArrayList<Branch>();
	}
	
	public void init(String scriptPath){
		script = new DScript(scriptPath);
		init();
	}
	
	private void init(){
		compiler.compile(script);
		runner.init(script);
		
		branches.clear();
		branches.add(new Branch(script.getEntryPoint()));
	}
	
	public void run(){
		
		for(currentBranch = 0; currentBranch < branches.size(); currentBranch++){
			
			Branch b = branches.get(currentBranch);
			
			try{
				runner.run(b);
			}
			catch(ScriptException e){
				error(e);
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
	
	private void error(ScriptException e){
		
		int file = e.getFileIndex();
		int line = e.getLine();
		
		System.err.println(
			"Runtime error in " + (file == 0 ? script.getFileName(): script.getFileName(file)) +
			" on line " + line + ":\n" + e.getMessage() +
			(line > 0 ? "\n" + line + ": " + script.getLine(file, line) : "")
		);
	}
}
