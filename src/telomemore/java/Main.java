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
		
		
		File fOutCSV=new File("data/testout.csv");
		File fR1=new File("data/R1.fastq.gz");
		File fR2=new File("data/R2.fastq.gz");

		
		if(args.length==0) {
			args="data/testout.csv -countkmer CCCTAA -counttotal -sciatac data/R1.fastq.gz,data/R2.fastq.gz".split(" ", 0);
		}
		

		
		
		if(args.length!=0) {
			fOutCSV=new File(args[0]);
			fR1=new File(args[1]);
			fR2=new File(args[2]);
		}
		
		
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
		
		//Other goodies to save
		for(Counter counter:listCounters)
			counter.storeExtras(fOutCSV);
		
		System.out.println("done");
		
	}
	

}
