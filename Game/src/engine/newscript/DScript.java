package engine.newscript;

public class DScript{
	
	private final String path;
	
	public DScript(String path){
		this.path = "Game/res/script/" + path;
	}
	
	public String getPath(){
		return path;
	}
}
