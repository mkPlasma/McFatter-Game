package engine.newscript.preprocess;

import java.util.HashMap;
import java.util.Map;

import content.FrameList;
import engine.newscript.DScript;

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
		sugars.put("_lx", "32");
		sugars.put("_rx", "416");
		sugars.put("_ty", "16");
		sugars.put("_by", "464");
		
		// Misc
		sugars.put("_time", "scriptTime()");
		sugars.put("_px", "playerX()");
		sugars.put("_py", "playerY()");
	}
	
	public void process(DScript script){
		
		String[] file = script.getFile();
		
		// Replace
		for(int i = 0; i < file.length; i++)
			for(Map.Entry<String, String> e:sugars.entrySet())
				file[i] = file[i].replace(e.getKey(), e.getValue());
		
		script.setFile(file);
	}
}
