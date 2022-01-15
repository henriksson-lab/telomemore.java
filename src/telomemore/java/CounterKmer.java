package telomemore.java;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * Count Kmers using regexp
 * 
 * @author Johan Henriksson
 *
 */
public class CounterKmer implements Counter {

	Pattern p;
	int minCount=5;
	
	String patternSeq;
	public CounterKmer(String seq) {
		patternSeq=seq;
		p = Pattern.compile(seq);
	}
	
	private TreeMap<String, ArrayList<String>> reads=new TreeMap<String, ArrayList<String>>();
	
	private TreeSet<String> seenBC=new TreeSet<String>();
	
	@Override
	public void count(String bc, String seq1, String seq2) {
		
		//Which BCs we have seen
		seenBC.add(bc);
		
		//Count the occurences
		Matcher m = p.matcher(seq1);  
		int count = 0;
		while (m.find()) {
		    count++;
		}
		m = p.matcher(seq2);  
		while (m.find()) {
		    count++;
		}

		//If enough counts, keep it
		if(count>=minCount) {
			//System.out.println(bc+"\t"+seq1+"\t"+seq2);
			
			ArrayList<String> readlist=reads.get(bc);
			if(readlist==null)
				reads.put(bc,readlist=new ArrayList<String>());
			
			readlist.add(seq1+seq2);
		}
	}
	
	
	Histogram histogramUniqueCount=new Histogram();
	Histogram histogramDups=new Histogram();
	
	
	TreeMap<String, Integer> countForBC=new TreeMap<String, Integer>();

	@Override
	public void process() {

		//Initial counts from 0
		for(String bc:seenBC)
			countForBC.put(bc,0);
		
		//Check which are unique
		for(Entry<String,ArrayList<String>> e:reads.entrySet()) {
			String bc=e.getKey();
			ArrayList<String> readlist=e.getValue();

			TreeMap<String, Integer> readcount=new TreeMap<String, Integer>();
			for(String r:readlist) {
				if(readcount.containsKey(r))
					readcount.put(r,readcount.get(r)+1);
				else
					readcount.put(r,1);
			}
			//Build histogram of doublets
			for(Integer cnt:readcount.values()) {
				histogramDups.add(cnt);
			}
			
			//Build histogram of barcode counts
			int numUnique=readcount.size();
			countForBC.put(bc,numUnique);
			histogramUniqueCount.add(numUnique);
		}
		
		System.out.println("For kmer "+patternSeq);
		System.out.println("Histogram unique counts: ");
		System.out.println(histogramUniqueCount.print());
		System.out.println();
		System.out.println("Histogram of duplicates: ");
		System.out.println(histogramDups.print());
		System.out.println();
	}

	public Collection<String> getBC(){
		return seenBC;
	}
	
	public void addOutputHeader(ArrayList<String> header) {
		header.add("cnt_patternSeq");
	}

	public void addCellInfo(ArrayList<String> line, String bc) {
		line.add(countForBC.get(bc).toString());
	}

	public void storeExtras(File fOutCSV) throws FileNotFoundException {
		File fHistUnique=new File(fOutCSV.getParentFile(), fOutCSV.getName()+".histUnique");
		File fHistDups=new File(fOutCSV.getParentFile(), fOutCSV.getName()+".histDups");
		
		PrintWriter pw=new PrintWriter(fHistUnique);
		pw.print(histogramUniqueCount.print());
		pw.close();
		pw=new PrintWriter(fHistDups);
		pw.print(histogramDups.print());
		pw.close();
	}

	
}
