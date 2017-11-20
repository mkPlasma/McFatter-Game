package engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.lwjgl.BufferUtils;

/*
 * 		IOFunctions.java
 * 		
 * 		Purpose:	Handles file reading and writing.
 * 		Notes:		
 * 		
 */

public class IOFunctions{
	
	public static String readToString(String path) throws IOException{
		return new String(Files.readAllBytes(Paths.get(path)));
	}
	
	public static ByteBuffer readToByteBuffer(String path) throws IOException{
		FileInputStream fis = new FileInputStream(new File(path));
		FileChannel fc = fis.getChannel();
		
		ByteBuffer buffer = BufferUtils.createByteBuffer((int)fc.size() + 1);
		
		while(fc.read(buffer) != -1);
		
		fis.close();
		fc.close();
		buffer.flip();
		
		return buffer;
	}
	
	public static String getLine(String path, int lineNum){
		try{
			return Files.readAllLines(Paths.get(path)).get(lineNum - 1).trim();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		
		return "";
	}
	
	public static int getLineCount(String path){
		try(BufferedReader br = new BufferedReader(new FileReader(path))){
			
			int n = 0;
			while(br.readLine() != null) n++;
			
			return n;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return -1;
	}
}
