package telomemore.java;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * 
 * 
 * @author Johan Henriksson
 *
 */
public class Main {
	
	/**
	 * Entry point
	 */
	public static void main(String[] args) throws IOException {
		
		
		File fR1=new File("data/R1.fastq.gz");
		File fR2=new File("data/R2.fastq.gz");
		File fOutCSV=new File("data/testout.csv");

		
		//Set up the counters
		ArrayList<Counter> listCounters=new ArrayList<Counter>();
		listCounters.add(new CounterKmer("CCCTAA"));
		listCounters.add(new CounterTotal());
		
		System.out.println("Processing the reads");
		SciAtacReader reader=new SciAtacReader();
		reader.read(
				fR1,fR2,
				listCounters
				);
		
		System.out.println("Deduplicating");
		for(Counter counter:listCounters)
			counter.process();
		
		System.out.println("Produce a count table");
		
		
		PrintWriter pw=new PrintWriter(fOutCSV);
		
		//Write the CSV header
		ArrayList<String> header=new ArrayList<String>();
		for(Counter counter:listCounters)
			counter.addOutputHeader(header);
		pw.print("\"\"");
		for(String s:header)
			pw.print(",\""+s+"\"");
		pw.println();
		
		//Write a line for each cell
		for(String bc:listCounters.get(0).getBC()) {
			ArrayList<String> line=new ArrayList<String>();
			for(Counter counter:listCounters)
				counter.addCellInfo(line,bc);
			
			pw.print(",\""+bc+"\"");
			for(String s:line)
				pw.print(","+s+"");
			pw.println();
		}
		pw.close();
		
		System.out.println("done");
		
	}
	

}
