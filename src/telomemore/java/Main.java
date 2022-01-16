package telomemore.java;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * 
 * Telomemore entry point
 * 
 * @author Johan Henriksson
 *
 */
public class Main {
	
	
	public static void printHelp() {
		System.out.println("================ Telomemore =====================================================");
		System.out.println("");
		System.out.println("This software is aimed at simple counting of telomeric reads in single-cell data.");
		System.out.println("Note that the order of the arguments IS important, and that little checking is performed");
		System.out.println("");
		System.out.println("java -Xmx2g -jar telomemore.jar OUTPUT.csv ");
		System.out.println("                          [-filterbc barcodes.tsv.gz]");
		System.out.println("                          [-countkmer CCCTAA[,5]]");
		System.out.println("                          [-counttotal]");
		System.out.println("                          [[-sciatac R1.fq.gz,R2.fq.gz]]");
		System.out.println("                          [[-bulkfastq R1.fq.gz[,R2.fq.gz]]]   ---  maybe do not implement");
		System.out.println("                          [[-10xbampe possorted.bam]]");
		System.out.println("                          [[-10xbamse possorted.bam]]");
		System.out.println();
		System.exit(0);
	}
	
	/**
	 * Entry point
	 */
	public static void main(String[] args) throws IOException {
		ArrayList<Counter> listCounters=new ArrayList<Counter>();

		///// For testing
		//data/testout.csv -countkmer CCCTAA -counttotal -sciatac data/R1.fastq.gz,data/R2.fastq.gz
		//data/testout.csv -countkmer CCCTAA -counttotal -10xbampe data/little_atac_possorted_bam.bam
		//args="data/testout.csv -countkmer CCCTAA -counttotal -10xbam data/atac_possorted_bam.bam
		
		if(args.length==0) {
			printHelp();
		}
		
		CounterLimitBC limitbc=null;
		
		File fOutCSV=new File(args[0]);
		
		for(int curarg=1;curarg<args.length;curarg++) {
			if(args[curarg].equals("-h"))
				printHelp();
			else if(args[curarg].equals("-filterbc")) {
				limitbc=new CounterLimitBC(new File(args[curarg+1]));
				curarg++;
			}
			else if(args[curarg].equals("-countkmer")) {
				String[] extras=args[curarg+1].split(",",0);
				curarg++;
				String kmerseq=extras[0];
				int numkmer=5;
				if(extras.length>1)
					numkmer=Integer.parseInt(extras[1]);
				
				Counter c=new CounterKmer(kmerseq,numkmer);
				if(limitbc!=null)
					c=limitbc.wrapCounter(c);
				listCounters.add(c);
			}
			else if(args[curarg].equals("-counttotal")) {
				Counter c=new CounterTotal();
				if(limitbc!=null)
					c=limitbc.wrapCounter(c);
				listCounters.add(c);
			}
			else if(args[curarg].equals("-sciatac")) {
				
				String[] extras=args[curarg+1].split(",",0);
				curarg++;

				File fR1=new File(extras[0]);
				File fR2=new File(extras[1]);

				System.out.println("Processing sciATAC reads from "+fR1+"  and  "+fR2);
				SciAtacReader reader=new SciAtacReader();
				reader.read(
						fR1,fR2,
						listCounters
						);
			}
			else if(args[curarg].equals("-bulkfastq")) {
				System.out.println("todo");
			}
			else if(args[curarg].equals("-10xbampe")) {
				String[] extras=args[curarg+1].split(",",0);
				curarg++;
				File fBAM=new File(extras[0]);
				Reader10xbamPE r=new Reader10xbamPE();
				r.read(fBAM, listCounters);
			} else if(args[curarg].equals("-10xbamse")) {
					String[] extras=args[curarg+1].split(",",0);
					curarg++;
					File fBAM=new File(extras[0]);
					Reader10xbamSE r=new Reader10xbamSE();
					r.read(fBAM, listCounters);
			} else {
				System.out.println("Unknown parameter: "+args[curarg]);
				System.exit(1);
			}
		}
		
		
		System.out.println("Final processing");
		for(Counter counter:listCounters)
			counter.process();
		
		System.out.println("Produce outputs");
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
