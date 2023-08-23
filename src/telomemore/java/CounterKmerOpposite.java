package telomemore.java;

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
public class CounterKmerOpposite implements Counter {

	private Histogram histogramUniqueCount=new Histogram();
	private Histogram histogramDups=new Histogram();
	private Histogram histogramKmerCount=new Histogram();

	private Pattern p;
	private Pattern pRC;
	private String patternSeq;
	private int minCount=5;
	


	//For each barcode, the associated reads
	private TreeMap<String, ArrayList<String>> reads=new TreeMap<String, ArrayList<String>>();
	//The BCs we have seen anywhere in the file
	private TreeSet<String> seenBC=new TreeSet<String>();
	//Unique counts for each BC
	private TreeMap<String, Integer> dedupCountForBC=new TreeMap<String, Integer>();
	private TreeMap<String, Integer> totalCountForBC=new TreeMap<String, Integer>();

	
	
	public CounterKmerOpposite(String seq, int minCount) {
		patternSeq=seq;
		p = Pattern.compile(seq);
		pRC = Pattern.compile(SeqUtil.reverseComplement(seq));		
		this.minCount=minCount;
	}
	
	public void reset() {
		histogramUniqueCount=new Histogram();
		histogramDups=new Histogram();
		histogramKmerCount=new Histogram();
		reads.clear();
		seenBC.clear();
		dedupCountForBC.clear();
		totalCountForBC.clear();
	}
	
	private int countWithPattern(String seq1, Pattern p) {
		//Count the occurrences
		Matcher m = p.matcher(seq1);  
		int count = 0;
		while (m.find()) {
		    count++;
		}
		return count;		
	}
	

	@Override
	public void count(String bc, String seq1, String seq2) {
		
		//Which BCs we have seen
		seenBC.add(bc);
		
		int count1=countWithPattern(seq1, p) + countWithPattern(seq2, pRC);
		int count2=countWithPattern(seq1, pRC) + countWithPattern(seq2, p);
		int count=Math.max(count1, count2);

		histogramKmerCount.add(count);

		//If enough counts, keep it
		if(count>=minCount) {
			//System.out.println(bc+"\t"+seq1+"\t"+seq2);
			
			ArrayList<String> readlist=reads.get(bc);
			if(readlist==null)
				reads.put(bc,readlist=new ArrayList<String>());
			
			readlist.add(seq1+seq2);
		}
	}
	
	
	
	

	@Override
	public void process() {

		//Initial counts from 0
		for(String bc:seenBC) {
			dedupCountForBC.put(bc,0);
			totalCountForBC.put(bc,0);
		}
		
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
			int numTotal=readlist.size();
			dedupCountForBC.put(bc,numUnique);
			totalCountForBC.put(bc,numTotal);
			histogramUniqueCount.add(numUnique);
		}
		
		System.out.println("For kmer "+patternSeq);
		System.out.println("Histogram unique counts: ");
		System.out.println(histogramUniqueCount.toString());
		System.out.println("Histogram of duplicates: ");
		System.out.println(histogramDups.toString());
		System.out.println("Histogram of kmer: ");
		System.out.println(histogramKmerCount.toString());
		System.out.println();
	}

	public Collection<String> getBC(){
		return seenBC;
	}
	
	public void addOutputHeader(ArrayList<String> header) {
		header.add("dedupcnt_"+patternSeq);
		header.add("totalcnt_"+patternSeq);
	}

	public void addCellInfo(ArrayList<String> line, String bc) {
		line.add(dedupCountForBC.get(bc).toString());
		line.add(totalCountForBC.get(bc).toString());
	}

	public void storeExtras(File fOutCSV) throws FileNotFoundException {
		File fHistUnique=new File(fOutCSV.getParentFile(), fOutCSV.getName()+".histUnique");
		File fHistDups=new File(fOutCSV.getParentFile(), fOutCSV.getName()+".histDups");
		File fHistKmers=new File(fOutCSV.getParentFile(), fOutCSV.getName()+".histKmer_"+patternSeq);
		
		PrintWriter pw=new PrintWriter(fHistUnique);
		pw.print(histogramUniqueCount.toString()+"\n");
		pw.close();
		
		pw=new PrintWriter(fHistDups);
		pw.print(histogramDups.toString()+"\n");
		pw.close();

		pw=new PrintWriter(fHistKmers);
		pw.print(histogramKmerCount.toString()+"\n");
		pw.close();
	}

	
}
