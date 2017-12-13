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
	
	private boolean haltRun = false;
	private boolean finished = false;
	
	private int time;
	
	public ScriptController(DScript script, MainScreen screen){
		runner = new ScriptRunner(script, screen);
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
		
		boolean loop = false;
		
		do{
			for(int i = 0; i < branches.size(); i++){
				
				ScriptBranch branch = branches.get(i);
				
				// Run current branch
				runner.run(branch);
				
				// Remove branch
				if(branch.toRemove()){
					branches.remove(i);
					i--;
				}
				
				// Add branch
				ScriptBranch newBranch = runner.getAddedBranch();
				
				if(newBranch != null){
					branches.add(i, newBranch);
					i--;
				}
				
				haltRun = runner.haltRun();
				finished = runner.isFinished() && branches.isEmpty();
				loop = runner.continueLoop();
				
				if(haltRun){
					loop = false;
					break;
				}
			}
		}while(loop);
		
		time++;
	}
	
	// Reload script
	public void reload(){
		branches.clear();
		branches.add(runner.init());
		
		haltRun = runner.haltRun();
		finished = false;
		
		time = 0;
	}
	
	public void addBranch(ScriptBranch branch){
		branches.add(branches.size() - 1, branch);
	}
	
	public void setPlayer(Player player){
		runner.setPlayer(player);
	}
	
	public boolean isFinished(){
		return finished;
	}
}
