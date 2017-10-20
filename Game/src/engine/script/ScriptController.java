package engine.script;

import java.util.ArrayList;

import engine.entities.Bullet;

public class ScriptController{
	
	private final ScriptRunner runner;
	
	private ArrayList<ScriptBranch> branches;
	
	private DScript script;
	
	private boolean haltRun = false;
	private boolean finished = false;
	
	public ScriptController(DScript script){
		this.script = script;
		
		runner = new ScriptRunner(script);
		branches = new ArrayList<ScriptBranch>();
	}
	
	public void init(){
		branches.add(runner.init());
		haltRun = runner.haltRun();
	}
	
	public void run(){
		
		if(haltRun || (finished && branches.isEmpty()))
			return;
		
		for(int i = 0; i < branches.size(); i++){
			
			ScriptBranch branch = branches.get(i);
			
			// Run current branch and update
			runner.run(branch);
			runner.updateState(branch);
			
			// Remove branch
			if(branch.toRemove())
				branches.remove(i);
			
			// Add new branches
			branches.addAll(runner.getBranches());
			
			haltRun = runner.haltRun();
			finished = runner.isFinished() && branches.isEmpty();
		}
	}
	
	public ArrayList<Bullet> getBullets(){
		return runner.getBullets();
	}
	
	public boolean isFinished(){
		return finished;
	}
}
