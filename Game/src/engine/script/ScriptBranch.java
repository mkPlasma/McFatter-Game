package engine.script;

import java.util.ArrayList;
import java.util.Stack;

public class ScriptBranch{
	
	// Where in bytecode state is running
	private int bytecodeIndex;
	
	// Waiting time
	private int waitTime;
	
	// Is primary branch
	// Primary branch runs outside of any tasks
	private boolean primary;
	
	// Branch ended
	private boolean remove;
	
	// Variables
	private Object[] variables;
	
	// Sync variables with other states
	private int[] syncVariables;
	
	// Store return points for function calls
	private Stack<Integer> returnPoints;
	
	
	public ScriptBranch(int bytecodeIndex, Object[] variables, boolean primary){
		this.bytecodeIndex = bytecodeIndex;
		this.variables = variables.clone();
		this.primary = primary;
		
		returnPoints = new Stack<Integer>();
		
		// Get synced variables
		ArrayList<Integer> sync = new ArrayList<Integer>();
		
		for(int i = 0; i < variables.length; i++)
			if(variables[i] != null)
				sync.add(i);
		
		syncVariables = new int[sync.size()];
		
		for(int i = 0; i < sync.size(); i++)
			syncVariables[i] = sync.get(i);
	}
	
	// Sync variables with previous ScriptState
	public void syncVariables(Object[] variables){
		
		if(variables == null)
			return;
		
		for(int i:syncVariables)
			this.variables[i] = variables[i];
	}
	
	public int getBytecodeIndex(){
		return bytecodeIndex;
	}
	
	public void setBytecodeIndex(int bytecodeIndex){
		this.bytecodeIndex = bytecodeIndex;
	}
	
	public boolean tickWaitTime(){
		if(waitTime > 0)
			waitTime--;
		
		return waitTime == 0;
	}
	
	public void setWaitTime(int waitTime){
		this.waitTime = waitTime;
	}
	
	public boolean isPrimary(){
		return primary;
	}
	
	public void setPrimary(boolean primary){
		this.primary = primary;
	}
	
	public boolean toRemove(){
		return remove;
	}
	
	public void remove(){
		remove = true;
	}
	
	public Object[] getVariables(){
		return variables.clone();
	}
	
	public void setVariables(Object[] variables){
		this.variables = variables.clone();
	}
	
	public Stack<Integer> getReturnPoints(){
		return returnPoints;
	}
	
	public void setReturnPoints(Stack<Integer> returnPoints){
		this.returnPoints = returnPoints;
	}
}
