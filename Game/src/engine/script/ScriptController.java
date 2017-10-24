package engine.script;

import java.util.ArrayList;

import content.BulletList;
import engine.entities.Bullet;
import engine.entities.Player;

public class ScriptController{
	
	private final ScriptRunner runner;
	
	private ArrayList<ScriptBranch> branches;
	
	private DScript script;
	
	private boolean haltRun = false;
	private boolean finished = false;
	
	private int time;
	
	public ScriptController(DScript script){
		this.script = script;
		
		runner = new ScriptRunner(script);
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
			
			// Add new branches
			branches.addAll(runner.getBranches());
			
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
		
		haltRun = false;
		finished = false;
		
		time = 0;
	}
	
	public void setPlayer(Player player){
		runner.setPlayer(player);
	}
	
	public void setBulletList(BulletList bulletList){
		runner.setBulletList(bulletList);
	}
	
	public ArrayList<Bullet> getBullets(){
		return runner.getBullets();
	}
	
	public boolean isFinished(){
		return finished;
	}
}
