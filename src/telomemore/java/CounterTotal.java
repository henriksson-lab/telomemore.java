package telomemore.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

/**
 * 
 * Just count the total number of reads per cell
 * 
 * @author Johan Henriksson
 *
 */
public class CounterTotal implements Counter {

	private TreeMap<String, Integer> reads=new TreeMap<String, Integer>();
	
	@Override
	public void count(String bc, String seq1, String seq2) {
		
		if(reads.containsKey(bc))
			reads.put(bc,reads.get(bc)+1);
		else
			reads.put(bc,1);
	}
	
	@Override
	public void process() {
	}
	
	public Collection<String> getBC(){
		return reads.keySet();
	}

	public void addOutputHeader(ArrayList<String> header) {
		header.add("total");
	}

	
	public void addCellInfo(ArrayList<String> line, String bc) {
		line.add(reads.get(bc).toString());
	}

	
	
}
