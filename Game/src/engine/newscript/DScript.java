package engine.newscript;

public class DScript{
	
	private final String path;
	
	private String[] file;
	private Token[] tokens;
	
	public DScript(String path){
		this.path = "Game/res/script/" + path;
	}
	
	public String getPath(){
		return path;
	}
	
	public String getFolder(){
		return path.substring(0, path.lastIndexOf('/'));
	}
	
	public void setFile(String[] file){
		this.file = file;
	}
	
	public void clearFile(){
		file = null;
	}
	
	public String[] getFile(){
		return file;
	}
	
	public void setTokens(Token[] tokens){
		this.tokens = tokens;
	}
	
	public void clearTokens(){
		tokens = null;
	}
	
	public Token[] getTokens(){
		return tokens;
	}
}
