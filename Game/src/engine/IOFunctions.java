package engine;

import java.io.File;
import java.io.FileInputStream;
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
}
