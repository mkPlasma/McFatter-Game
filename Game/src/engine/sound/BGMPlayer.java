package engine.sound;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;

public class BGMPlayer{
	
	private final int track;
	private final IntBuffer buffer, source;
	
	private int time;
	
	
	public BGMPlayer(int track){
		this.track = track;
		
		buffer = BufferUtils.createIntBuffer(1);
		source = BufferUtils.createIntBuffer(1);
	}
	
	public void load(){
		
		AL10.alGenBuffers(buffer);
		
		if(error(AL10.alGetError()))
			return;
		
		
		WaveData wave = WaveData.create("Game/res/music/" + (track < 10 ? "0" : "") + track + ".wav");
		
		AL10.alBufferData(buffer.get(0), wave.format, wave.data, wave.samplerate);
		AL10.alGenSources(source);
		
		if(error(AL10.alGetError()))
			return;
		
		AL10.alSourcei(source.get(0), AL10.AL_BUFFER,	buffer.get(0));
		
		
		if(error(AL10.alGetError()))
			return;
	}

	public void play(){
		
		if(time != 0)
			AL10.alSourcei(source.get(0), AL11.AL_SAMPLE_OFFSET, time);
		
		time = 0;
		AL10.alSourcePlay(source.get(0));
	}
	
	public void pause(){
		time = AL10.alGetSourcei(source.get(0), AL11.AL_SAMPLE_OFFSET);
		AL10.alSourceStop(source.get(0));
	}
	
	public void stop(){
		time = 0;
		AL10.alSourceStop(source.get(0));
	}
	
	public void cleanup(){
		AL10.alDeleteSources(source.get(0));
		AL10.alDeleteBuffers(buffer.get(0));
	}
	
	private boolean error(int error){
		
		if(error == AL10.AL_NO_ERROR)
			return false;
		
		System.err.println("Failed to load music file (error " + error + ")");
		return true;
	}
}
