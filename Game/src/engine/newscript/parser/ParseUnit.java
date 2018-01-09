package engine.newscript.parser;

import engine.newscript.lexer.Token;

public class ParseUnit{
	
	private final String type;
	
	private ParseUnit parent;
	
	private Object[] contents;
	
	public ParseUnit(String type, Object[] contents){
		this.type = type;
		this.contents = contents;
	}
	
	public String getFile(){
		
		Object o = contents[0];
		
		if(o instanceof ParseUnit)
			return ((ParseUnit)o).getFile();
		
		if(!(o instanceof Token))
			return null;
		
		return ((Token)o).getFile();
	}
	
	public int getLineNum(){
		
		Object o = contents[0];
		
		if(o instanceof ParseUnit)
			return ((ParseUnit)o).getLineNum();
		
		if(!(o instanceof Token))
			return -1;
		
		return ((Token)o).getLineNum();
	}
	
	
	public String getType(){
		return type;
	}
	
	public void setParent(ParseUnit parent){
		this.parent = parent;
	}
	
	public ParseUnit getParent(){
		return parent;
	}
	
	public void setContents(Object[] contents){
		this.contents = contents;
	}
	
	public Object[] getContents(){
		return contents;
	}
}
