package engine.entities;

import java.util.Comparator;

public class BGComparator implements Comparator<BGEntity>{
	
	public int compare(BGEntity o1, BGEntity o2){
		return o1.getZ() - o2.getZ() < 0 ? -1 : 1;
	}
}
