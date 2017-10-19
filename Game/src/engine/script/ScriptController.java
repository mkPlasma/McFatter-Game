package engine.script;

import java.util.ArrayList;

import engine.entities.Bullet;

public class ScriptController{
	
	private final ScriptRunner runner;
	
	private DScript script;
	
	private boolean haltRun = false;
	private boolean finished = false;
	
	public ScriptController(DScript script){
		runner = new ScriptRunner(script);
		
		this.script = script;
	}
	
	public void init(){
		runner.init();
	}
	
	public void run(){
		runner.run();
		
		haltRun = runner.haltRun();
		finished = runner.isFinished();
	}
	
	public ArrayList<Bullet> getBullets(){
		return runner.getBullets();
	}
	
	public boolean isFinished(){
		return finished;
	}
}
