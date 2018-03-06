package engine.newscript.preprocess;

import java.util.ArrayList;

import engine.IOFunctions;
import engine.newscript.DScript;
import engine.newscript.ScriptException;

/**
 * 
 * Reads .dscript files and stores them into a string array.
 * Removes comments, replaces include statements, and adds syntactic sugars.
 * 
 * @author Daniel
 * 
 */

public class Preprocessor{
	
	private final SyntacticSugars syntacticSugars;
	
	private DScript script;
	
	private ArrayList<ArrayList<String>> files;
	private ArrayList<String> filePaths;
	private ArrayList<String> mainFile;
	
	private int lineNum;
	
	// Muli-line comments
	private boolean inComment;
	
	
	public Preprocessor(){
		files		= new ArrayList<ArrayList<String>>();
		filePaths	= new ArrayList<String>();
		
		syntacticSugars = new SyntacticSugars();
	}
	
	public void process(DScript script) throws ScriptException{
		
		this.script = script;
		
		files.clear();
		filePaths.clear();
		
		// Read file
		mainFile = IOFunctions.readToArrayList(script.getPath());
		
		files.add(mainFile);
		filePaths.add(script.getPath());
		
		if(mainFile == null)
			throw new ScriptException("File " + script.getPath().substring(16) + " could not be found", 0);
		
		// Set raw file
		setScriptFiles();
		
		// Preliminary check for $ characters
		for(int i = 0; i < mainFile.size(); i++)
			if(mainFile.get(i).contains("$"))
				throw new ScriptException("Invalid token '$'", i + 1);
		
		process();
		syntacticSugars.process(script);
	}
	
	private void process() throws ScriptException{
		
		lineNum = 1;
		
		// Process each line
		for(int i = 0; i < mainFile.size(); i++){
			
			String line = mainFile.get(i);
			
			if(line.isEmpty()){
				lineNum++;
				continue;
			}
			
			// Multi-line comment
			int ind, ind2;
			
			do{
				ind = getIndexOutOfQuotes(line, "/*");
				ind2 = getIndexOutOfQuotes(line, "*/");
				
				// If closing found, or both and closing is first
				if(ind2 > -1 && (ind < 0 || (ind > -1 && ind2 < ind))){
					
					// Close comment
					if(inComment){
						line = line.substring(ind2 + 2);
						mainFile.set(i, line);
						continue;
					}
					else
						throw new ScriptException("Invalid end of comment", lineNum);
				}
				
				// Comment on single line
				if(ind > -1 && ind2 > -1){
					line = line.substring(0, ind) + line.substring(ind2 + 2);
					mainFile.set(i, line);
					continue;
				}
				
				// Start of comment
				if(ind > -1){
					inComment = true;
					line = line.substring(0, ind);
					mainFile.set(i, line);
					continue;
				}
				
			}while(ind > -1 || ind2 > -1);
			
			
			if(inComment){
				lineNum++;
				continue;
			}
			
			// Single-line comment
			ind = getIndexOutOfQuotes(line, "//");
			
			if(ind > -1){
				line = line.substring(0, ind);
				mainFile.set(i, line);
			}
			
			// Include statement
			if(line.startsWith("#include")){
				
				// Get contents of file
				ArrayList<String> file2 = replaceInclude(line);
				
				if(file2 == null)
					return;
				
				// Replace include statement
				mainFile.remove(i);
				mainFile.addAll(i, file2);
			}
			
			
			
			
			
			lineNum++;
		}
		
		script.setFile(mainFile.toArray(new String[0]));
	}
	
	// Get index of a string only if it is outside of quotes
	private int getIndexOutOfQuotes(String line, String s){
		
		int ind;
		int offset = 0;
		
		do{
			ind = line.substring(offset).indexOf(s);
			
			if(ind > -1){
				ind += offset;
				
				// Check that comment is not in string
				int q = 0;
				
				for(int j = 0; j < ind; j++){
					if(line.charAt(j) == '"' && (j == 0 || (j > 0 && line.charAt(j - 1) != '\\')))
						q++;
				}
				
				// If not in string, return
				if(q%2 == 0)
					return ind;
				
				offset = ind + 2;
			}
		}while(ind > -1);
		
		return -1;
	}
	
	private ArrayList<String> replaceInclude(String line) throws ScriptException{
		String path = script.getFolder() + "/" + line.replaceAll("#include\\s+(.*);", "$1").replace(";", "") + ".dscript";
		
		ArrayList<String> file2 = IOFunctions.readToArrayList(path);
		
		if(file2 == null)
			throw new ScriptException("File " + path.substring(16) + " could not be found", lineNum);
		
		// Add to raw file list
		files.add(file2);
		filePaths.add(path);
		setScriptFiles();
		
		for(int i = 0; i < file2.size(); i++)
			if(file2.get(i).contains("$"))
				throw new ScriptException("Invalid token '$'", path, i + 1);
		
		file2.add(0, "$file " + path);
		file2.add("$file " + script.getPath());
		file2.add("$line " + lineNum);
		
		return file2;
	}
	
	private void setScriptFiles(){
		
		String[][] rawFiles = new String[files.size()][];
		
		for(int i = 0; i < files.size(); i++)
			rawFiles[i] = files.get(i).toArray(new String[0]);
		
		script.setRawFiles(rawFiles);
		script.setFilePaths(filePaths.toArray(new String[0]));
	}
}
