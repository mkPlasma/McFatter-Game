package engine.newscript;

import java.util.HashMap;
import java.util.Map;

import content.FrameList;

public class Sweetener extends CompilerUnit{
	
	private final Map<String, String> sugar;
	
	public Sweetener(Compiler compiler){
		super(compiler);
		
		sugar = new HashMap<String, String>();
		initMap();
	}
	
	private void initMap(){
		
		// Bullet/enemy types, colors, etc.
		String[][] vars = FrameList.getVars();
		
		for(String[] v:vars)
			sugar.put(v[0], v[1]);
		
		// Screen center
		sugar.put("_cx", "224");
		sugar.put("_cy", "240");
		
		// Screen borders
		sugar.put("_lx", "32");
		sugar.put("_rx", "416");
		sugar.put("_ty", "16");
		sugar.put("_by", "464");
		
		// Misc
		sugar.put("_time", "scriptTime()");
		sugar.put("_px", "playerX()");
		sugar.put("_py", "playerY()");
	}
	
	public void process(DScript script){
		
		String[] file = script.getFile();
		
		// Replace
		for(int i = 0; i < file.length; i++)
			for(Map.Entry<String, String> e:sugar.entrySet())
				file[i] = file[i].replace(e.getKey(), e.getValue());
		
		script.setFile(file);
	}
}
