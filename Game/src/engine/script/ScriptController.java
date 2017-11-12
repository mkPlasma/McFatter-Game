package engine.script;

import java.util.ArrayList;

import content.FrameList;
import engine.entities.Player;
import engine.screens.MainScreen;

/*
 * 		ScriptController.java
 * 		
 * 		Purpose:	Runs DScript and connects to other engine components.
 * 		Notes:		Holds script branches and adds generated bullets.
 * 		
 */

public class ScriptController{
	
	private final ScriptRunner runner;
	
	private ArrayList<ScriptBranch> branches;
	
	private DScript script;
	
	private boolean haltRun = false;
	private boolean finished = false;
	
	private int time;
	
	public ScriptController(DScript script, MainScreen screen){
		this.script = script;
		
		runner = new ScriptRunner(script, this, screen);
		branches = new ArrayList<ScriptBranch>();
		
		time = 0;
	}
	
	public void init(){
		branches.add(runner.init());
		haltRun = runner.haltRun();
	}
	
	public void run(){
		
		if(haltRun || finished)
			return;
		
		runner.setTime(time);
		
		for(int i = 0; i < branches.size(); i++){
			
			ScriptBranch branch = branches.get(i);
			
			// Run current branch
			runner.run(branch);
			
			// Remove branch
			if(branch.toRemove()){
				branches.remove(i);
				i--;
			}
			
			haltRun = runner.haltRun();
			finished = runner.isFinished() && branches.isEmpty();
		}
		
		time++;
	}
	
	// Reload script
	public void reload(){
		System.out.println("Reloaded " + script.getFileName() + "!");
		
		branches.clear();
		branches.add(runner.init());
		
		haltRun = runner.haltRun();
		finished = false;
		
		time = 0;
	}
	
	public void addBranch(ScriptBranch branch){
		branches.add(branch);
	}
	
	public void setPlayer(Player player){
		runner.setPlayer(player);
	}
	
	public void setFrameList(FrameList frameList){
		runner.setFrameList(frameList);
	}
	
	public boolean isFinished(){
		return finished;
	}
}
