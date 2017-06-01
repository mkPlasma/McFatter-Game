package engine;

import org.keplerproject.luajava.LuaState;
import org.keplerproject.luajava.LuaStateFactory;

public class Script{
	
	private final LuaState luaState;
	
	public Script(String path){
		luaState = LuaStateFactory.newLuaState();
		luaState.openLibs();
		luaState.LdoFile("Game/script/" + path + ".lua");
	}
	
	public void close(){
		luaState.close();
	}
}
