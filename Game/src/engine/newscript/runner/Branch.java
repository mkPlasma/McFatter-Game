package engine.newscript.runner;

import java.util.ArrayList;
import java.util.Stack;

public class Branch{
	
	// Current instruction index
	private int instructionIndex;
	
	// If branch has finished running
	private boolean finished;
	
	// Current waiting time
	private int waitTime;
	
	
	// Stack for temporary storage for operations
	private Stack<Object> workStack;
	
	// Local branch variables
	private Stack<ArrayList<Object>> localVariables;
	
	// Return stack for function calls
	private Stack<Integer> returnStack;
	private Stack<Boolean> returnValStack;
	

	public Branch(int instructionIndex){
		this.instructionIndex = instructionIndex;
		
		workStack = new Stack<Object>();
		
		localVariables	= new Stack<ArrayList<Object>>();
		
		returnStack		= new Stack<Integer>();
		returnValStack	= new Stack<Boolean>();
	}
	
	@SuppressWarnings("unchecked")
	public Branch(int instructionIndex, Stack<Object> workStack, Stack<ArrayList<Object>> localVariables, Stack<Integer> returnStack, Stack<Boolean> returnValStack){
		this.instructionIndex = instructionIndex;
		this.workStack		= (Stack<Object>)workStack.clone();
		this.localVariables	= (Stack<ArrayList<Object>>)localVariables.clone();
		this.returnStack	= (Stack<Integer>)returnStack.clone();
		this.returnValStack	= (Stack<Boolean>)returnValStack.clone();
	}
	
	// Set wait timer
	public void setWait(int waitTime){
		this.waitTime = waitTime;
	}
	
	// Tick wait timer and return true if still waiting
	public boolean tickWait(){
		
		if(waitTime > 0)
			waitTime--;
		
		return waitTime != 0;
	}
	
	public void setInstructionIndex(int i){
		instructionIndex = i;
	}
	
	public int getInstructionIndex(){
		return instructionIndex;
	}
	
	public void finish(){
		finished = true;
	}
	
	public boolean isFinished(){
		return finished;
	}
	
	public void pushBranch(Branch b){
		workStack.push(b);
	}
	
	public Stack<Object> getWorkStack(){
		return workStack;
	}
	
	public Stack<ArrayList<Object>> getLocalVariables(){
		return localVariables;
	}
	
	public Stack<Integer> getReturnStack(){
		return returnStack;
	}
	
	public Stack<Boolean> getReturnValStack(){
		return returnValStack;
	}
}
