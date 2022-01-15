package telomemore.java;

import java.util.ArrayList;

public class Histogram {

	ArrayList<Integer> bins=new ArrayList<Integer>();

	
	public void add(int bin) {
		
		while(bins.size()<bin+1)
			bins.add(0);
		bins.set(bin,bins.get(bin)+1);
	}
	
	public void print() {
		for(Integer i:bins) {
			System.out.print(i+" ");
		}
		System.out.println();

	}
}
