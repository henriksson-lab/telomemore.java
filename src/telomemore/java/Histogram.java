package telomemore.java;

import java.util.ArrayList;

public class Histogram {

	ArrayList<Integer> bins=new ArrayList<Integer>();

	
	public void add(int bin) {
		
		while(bins.size()<bin+1)
			bins.add(0);
		bins.set(bin,bins.get(bin)+1);
	}
	
	public String toString() {
		StringBuilder sb=new StringBuilder();
		for(Integer i:bins) {
			sb.append(i+" ");
		}
		return sb.toString();
	}
}
