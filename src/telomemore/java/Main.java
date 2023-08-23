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
		System.out.println();
		System.out.println("  Specify what to count: ");
		System.out.println();
		System.out.println("   [-filterbc barcodes.tsv.gz]  --- Optionally only count for these barcodes");
		System.out.println("   [[-countkmer CCCTAA[,5]]]    --- Deduplicate and count; minimum #kmers can be given.");  //TODO search for opposite too?
		System.out.println("                                    Specify multiple times to count several different kmers");
		System.out.println("   [[-countkmerRC CCCTAA[,5]]]  --- Like countkmer, but search for RC in opposite read. R1+R2 kmers>=count");
		System.out.println("   [-counttotal]                --- Count total number of reads per cell");
		System.out.println();
		System.out.println("  Then pick one of these input methods:");
		System.out.println();
		System.out.println("   [[-sciatac R1.fq.gz,R2.fq.gz]]       ---  sci-ATAC FASTQ files");
		System.out.println("   [[-10xbampe possorted.bam]]          ---  Paired end aligned BAM-file");
		System.out.println("   [[-10xbamse possorted.bam]]          ---  Single end aligned BAM-file");
		System.out.println("   [[-10xatacs directoryWithAllCellrangerFolders]]  -- Convenience method for ATAC and ARC");
		System.out.println("                                                    -- will automatically find BAM and barcodes to filter by");
		System.out.println();
		System.out.println("   It is also possible to store all detected reads with [-storereads OUTPUT.fa]");
		System.out.println();
		System.exit(0);
	}
	
	
	public static ArrayList<Counter> wrapCounters(ArrayList<Counter> listCounters, CounterLimitBC limitbc){
		if(limitbc!=null) {
			ArrayList<Counter> newCounters=new ArrayList<Counter>();
			for(Counter c:listCounters)
				newCounters.add(limitbc.wrapCounter(c));
			return newCounters;
		} else
			return listCounters;
	}
	
	/**
	 * Entry point
	 */
	public static void main(String[] args) throws IOException {
		ArrayList<Counter> listCounters=new ArrayList<Counter>();

		File fStoreReads=null;
		
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
			else if(args[curarg].equals("-storereads")) {
				fStoreReads=new File(args[curarg+1]);
				curarg++;
			}
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
			else if(args[curarg].equals("-countkmerRC")) {
				String[] extras=args[curarg+1].split(",",0);
				curarg++;
				String kmerseq=extras[0];
				int numkmer=5;
				if(extras.length>1)
					numkmer=Integer.parseInt(extras[1]);
				
				Counter c=new CounterKmerOpposite(kmerseq,numkmer);
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
				ReaderSciAtac reader=new ReaderSciAtac();
				reader.read(
						fR1,fR2,
						wrapCounters(listCounters, limitbc)
						);
			}
			/*else if(args[curarg].equals("-bulkfastq")) {
				System.out.println("TODO; use old python tool instead for now");
			}*/
			else if(args[curarg].equals("-10xbampe")) {
				String[] extras=args[curarg+1].split(",",0);
				curarg++;
				File fBAM=new File(extras[0]);
				Reader10xbamPE r=new Reader10xbamPE();
				r.read(fBAM, wrapCounters(listCounters, limitbc));
			} else if(args[curarg].equals("-10xbamse")) {
				String[] extras=args[curarg+1].split(",",0);
				curarg++;
				File fBAM=new File(extras[0]);
				Reader10xbamSE r=new Reader10xbamSE();
				r.read(fBAM, wrapCounters(listCounters, limitbc));
			} else if(args[curarg].equals("-10xatacs")) {
				String[] extras=args[curarg+1].split(",",0);
				curarg++;
				
				File fBasedir=new File(extras[0]);
				String csvheader=null;
				StringBuilder sbBody=new StringBuilder();
				for(File fCR:fBasedir.listFiles()) {
					
					File outs=new File(fCR,"outs");
					
					//Detect the filename for the BC filtering
					limitbc=null;
					File fBC=new File(new File(outs,"filtered_peak_bc_matrix"),"barcodes.tsv");
					if(!fBC.exists())
						fBC=new File(new File(outs,"filtered_feature_bc_matrix"),"barcodes.tsv.gz");
					if(fBC.exists()) {
						System.out.println("Will filter by barcodes from "+fBC);
						limitbc=new CounterLimitBC(fBC);
					}
					else
						System.out.println("Will not filter by barcodes");
					
					//Detect the filename for the BAM
					File fBAM=new File(outs,"atac_possorted_bam.bam");
					if(!fBAM.exists())
						fBAM=new File(outs,"possorted_bam.bam");
					if(fBAM.exists()) {
						
						ArrayList<Counter> listCountersWrapped = wrapCounters(listCounters, limitbc);
						
						System.out.println("===== Processing "+fBAM);
						Reader10xbamPE r=new Reader10xbamPE();
						r.read(fBAM, listCountersWrapped);

						System.out.println("Final processing for "+fCR);
						for(Counter counter:listCountersWrapped)
							counter.process();
						
						System.out.println("Produce partial outputs for "+fCR);
						csvheader=makeCSVheader(listCountersWrapped, true);
						sbBody.append(makeCSVbody(listCountersWrapped, fCR.getName()));

						//No attempt at gathering all the histograms currently.
						//Put them next to the BAM-files
						for(Counter counter:listCounters)
							counter.storeExtras(fBAM);

						//Prepare for the next round
						for(Counter counter:listCountersWrapped)
							counter.reset();
						
					} else {
						System.out.println("Detected no BAM in "+fCR);
					}
					
				}
				
				PrintWriter pw=new PrintWriter(fOutCSV);
				pw.print(csvheader);
				pw.print(sbBody.toString());
				pw.close();
				
				//This overrides the normal counting at the end.
				//Note that the histograms will not be written
				System.exit(0);
			} else {
				System.out.println("Unknown parameter: "+args[curarg]);
				System.exit(1);
			}
		}
		
		
		System.out.println("Final processing");
		for(Counter counter:listCounters)
			counter.process();
		
		System.out.println("Produce outputs");
		String csvheader=makeCSVheader(listCounters, false);
		String csvbody=makeCSVbody(listCounters, null);
		
		PrintWriter pw=new PrintWriter(fOutCSV);
		pw.print(csvheader);
		pw.print(csvbody);
		pw.close();
		
		//Other goodies to save
		for(Counter counter:listCounters)
			counter.storeExtras(fOutCSV);
		
		System.out.println("done");
		
	}

	
	/**
	 * Write the body of a CSV file
	 * @param listCounters
	 * @param dataset
	 * @return
	 */
	private static String makeCSVbody(ArrayList<Counter> listCounters, String dataset) {
		StringBuilder sb=new StringBuilder();
		
		//Write a line for each cell
		for(String bc:listCounters.get(0).getBC()) {
			ArrayList<String> line=new ArrayList<String>();
			for(Counter counter:listCounters)
				counter.addCellInfo(line,bc);
			
			sb.append("\""+bc+"\"");
			for(String s:line)
				sb.append(","+s+"");
			if(dataset!=null)
				sb.append(","+dataset+"");
			sb.append("\n");
		}
		return sb.toString();
	}

	
	
	private static String makeCSVheader(ArrayList<Counter> listCounters, boolean dataset) {
		StringBuilder sb=new StringBuilder();
		ArrayList<String> header=new ArrayList<String>();
		for(Counter counter:listCounters)
			counter.addOutputHeader(header);
		sb.append("\"barcode\"");
		for(String s:header)
			sb.append(",\""+s+"\"");
		if(dataset)
			sb.append(",\"dataset\"");
		sb.append("\n");
		return sb.toString();
	}
	

}
