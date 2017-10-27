package engine.script;

import java.util.ArrayList;
import java.util.Stack;

public class ScriptBranch{
	
	// Parent to sync variables with
	private ScriptBranch parent;
	
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
	private ArrayList<Integer> syncVariables;
	
	// Store return points for function calls
	private Stack<Integer> returnPoints;
	
	
	public ScriptBranch(int bytecodeIndex, Object[] variables, boolean primary){
		this.bytecodeIndex = bytecodeIndex;
		this.variables = variables.clone();
		this.primary = primary;
		
		syncVariables = new ArrayList<Integer>();
		returnPoints = new Stack<Integer>();

		for(int i = 0; i < variables.length; i++)
			if(variables[i] != null)
				syncVariables.add(i);
	}
	
	public void setParent(ScriptBranch parent){
		this.parent = parent;
	}
	
	// Copy variables from parent
	public void syncWithParent(){
		
		if(parent == null || parent.toRemove())
			return;
		
		Object[] variables = parent.getVariables();
		
		for(int i:syncVariables)
			this.variables[i] = variables[i];
	}
	
	// Copy variables to parent
	public void syncToParent(){

		if(parent == null || parent.toRemove())
			return;
		
		Object[] variables = parent.getVariables();
		
		for(int i:syncVariables)
			variables[i] = this.variables[i];
		
		parent.setVariables(variables);
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
	
	public int getWaitTime(){
		return waitTime;
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
