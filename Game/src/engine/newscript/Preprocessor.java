package engine.newscript;

import java.util.ArrayList;

import engine.IOFunctions;

public class Preprocessor extends CompilerUnit{
	
	private DScript script;
	
	private ArrayList<String> file;
	
	private int lineNum;
	
	public Preprocessor(Compiler compiler){
		super(compiler);
	}
	
	public void process(DScript script){
		
		this.script = script;
		
		// Read file
		file = IOFunctions.readToArrayList(script.getPath());
		
		// Preliminary check for $ characters
		for(String line:file)
			if(line.contains("$"))
				return;
		
		if(file == null){
			compiler.error("File " + script.getPath() + " could not be found");
			return;
		}
		
		process();
	}
	
	private void process(){
		
		lineNum = 0;
		
		// Process each line
		for(int i = 0; i < file.size(); i++){
			
			String line = file.get(i);
			
			
			// Include statement
			if(line.startsWith("#include")){
				
				// Get contents of file
				ArrayList<String> file2 = replaceInclude(line);
				
				if(file2 == null)
					return;
				
				// Replace include statement
				file.remove(i);
				file.addAll(i, file2);
			}
			
			// Single-line comment
			int ind = line.indexOf("//");
			
			if(ind > -1){
				
				// Check that comment is not in string
				int q = 0;
				
				for(int j = 0; j < ind; j++){
					if(line.charAt(j) == '"' && (j == 0 || (j > 0 && line.charAt(j - 1) != '\\')))
						q++;
				}
				
				// If not in string, remove and replace
				if(q%2 == 0)
					file.set(i, line.substring(0, ind));
			}
			
			lineNum++;
		}
		
		for(String l:file)
			System.out.println(l);
		
		script.setFile(file.toArray(new String[0]));
	}
	
	private ArrayList<String> replaceInclude(String line){
		String path = script.getFolder() + "/" + line.replaceAll("#include\\s+", "").replace(";", "") + ".dscript";
		
		ArrayList<String> file2 = IOFunctions.readToArrayList(path);

		if(file2 == null){
			compiler.error("File " + path + " could not be found");
			return null;
		}
		
		file2.add(0, "$file " + path);
		file2.add("$file " + script.getPath());
		file2.add("$line " + (lineNum + 1));
		
		return file2;
	}
}
