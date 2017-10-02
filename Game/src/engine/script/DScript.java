package engine.script;

/*
 * 		DScript.java
 * 		
 * 		Purpose:	Holds DScript bytecode.
 * 		Notes:		WIP
 * 		
 * 		Last modified by:	Daniel
 * 		Date:				10/2
 * 		Changes:			
 */

public class DScript{

	private long[] bytecode;
	private final String path;
	
	public DScript(String path){
		this.path = path;
	}
	
	public void setBytecode(long[] bytecode){
		this.bytecode = bytecode;
	}
	
	public long[] getBytecode(){
		return bytecode;
	}
	
	public String getPath(){
		return path;
	}
	
	public String getFileName(){
		return path.substring(path.lastIndexOf('/') + 1);
	}
}