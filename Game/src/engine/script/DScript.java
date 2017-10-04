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
	
	private String[] tokens;
	private long[] bytecode;
	private final String path;
	
	public DScript(String path){
		this.path = path;
	}

	public void setTokens(String[] tokens){
		this.tokens = tokens;
	}
	
	// Clear tokens after parsing
	public void clearTokens(){
		tokens = null;
	}
	
	public void setBytecode(long[] bytecode){
		this.bytecode = bytecode;
	}
	
	public String[] getTokens(){
		return tokens;
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