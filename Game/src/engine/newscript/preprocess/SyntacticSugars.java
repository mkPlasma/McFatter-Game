package engine.newscript.preprocess;

import java.util.HashMap;
import java.util.Map;

import content.FrameList;
import engine.newscript.DScript;

/**
 * 
 * Simple syntactic sugar replacements for DScript programming convenience.
 * 
 * @author Daniel
 * 
 */

public class SyntacticSugars{
	
	private final Map<String, String> sugars;
	
	public SyntacticSugars(){
		sugars = new HashMap<String, String>();
		initMap();
	}
	
	private void initMap(){
		
		// Bullet/enemy types, colors, etc.
		String[][] vars = FrameList.getVars();
		
		for(String[] v:vars)
			sugars.put(v[0], v[1]);
		
		// Screen center
		sugars.put("_cx", "224");
		sugars.put("_cy", "240");
		
		// Screen borders
		sugars.put("_left", "32");
		sugars.put("_right", "416");
		sugars.put("_top", "16");
		sugars.put("_bottom", "464");
		
		// Misc
		sugars.put("_time", "scriptTime()");
		sugars.put("_px", "playerX()");
		sugars.put("_py", "playerY()");
		sugars.put("_pi", "3.1415927");
		sugars.put("_2pi", "6.2831855");
	}
	
	public void process(DScript script){
		
		String[] file = script.getFile();
		
		// Replace
		for(int i = 0; i < file.length; i++)
			for(Map.Entry<String, String> e:sugars.entrySet())
				file[i] = file[i].replaceAll("\\b" + e.getKey() + "\\b", e.getValue());
		
		script.setFile(file);
	}
}
