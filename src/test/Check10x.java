package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * Reader of aligned 10x bam files. Can be position sorted, does not need index
 * 
 * This is for paired-end reads. It keeps an infinite buffer to try and find the corresponding other read
 * 
 * Note: unlike fastq, should search for the same motif (not reverse complemented) in both pairs
 * 
 * @author Johan Henriksson
 *
 */
public class Check10x {
	
	private static TreeMap<String, String[]> previousRead=new TreeMap<String, String[]>();
	
	private static Pattern p1 = Pattern.compile("CCCTAA");
	private static Pattern p2 = Pattern.compile("TTAGGG");

	
	public static int count(Pattern p, String seq1) {
		
		//Count the occurrences
		Matcher m = p.matcher(seq1);  
		int count = 0;
		while (m.find()) {
		    count++;
		}
		return count;
	}
	
	
	public static void read(
			File fBAM)
					throws IOException {
		
		
		System.out.println("chr1,pos1,chr2,pos2,count1CCCTAA,count1TTTAGG,count2CCCTAA,count2TTTAGG");

		
		Process p = Runtime.getRuntime().exec("samtools view "+fBAM);
		BufferedReader inp = new BufferedReader( new InputStreamReader(p.getInputStream()) );

		//Example line:
		//A00689:445:HNTW5DRXY:1:1114:21359:1125	99	chr1	9996	0	50M	=	10179	232	TCCCATAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAACCCTAA	F:F,FF,FF:FFFF:FFF:FFFFFFFFFF:FF,FFFFF,FFFFFF,FFFF	NM:i:1	MD:Z:3G46	AS:i:46	XS:i:46	CR:Z:GCAAGGTGTTAACTGC	CY:Z:FFFFFFFFFFFFFFFF	CB:Z:CGGCTCACAACTAGAA-1	RG:Z:lib6:MissingLibrary:1:HNTW5DRXY:1
		
		String line;
		int readRecords=0;
		while((line=inp.readLine())!=null) {
			//Update user about progress
			readRecords++;
			if(readRecords%1000000 == 0){
				//Calculate progress
				//System.out.println("records so far: "+readRecords+"    num reads to be matched up: "+previousRead.size());
			}
			
			String[] parts=line.split("\t", 0);
			String readname=parts[0];
			String readseq=parts[9];
			
			//Try to match up with the other read
			if(previousRead.containsKey(readname)) {
				
				String[] otherReadFull=previousRead.remove(readname);
				String otherRead=otherReadFull[9];
				

				//Locate the cell barcode
				String cb=null;
				for(int i=9;i<parts.length;i++)
					if(parts[i].startsWith("CB:")) {
						cb=parts[i];
						//CB:Z:CGGCTCACAACTAGAA-1
						cb=cb.substring(5);
						break;
					}

				//Can figure out which is R1 and R2 if we want to
				//NM:i:0   vs   NM:i:0    this is R1 and R2 I assume

				if(cb!=null) {
					
					int count1a=count(p1, readseq);
					int count1b=count(p2, readseq);
					
					int count2a=count(p1, otherRead);
					int count2b=count(p2, otherRead);

					int mincount=2;
					if(count1a>mincount || count1b>mincount|| count2a>mincount|| count2b>mincount) {
						
						String chrom1=parts[2];
						String chrom2=otherReadFull[2];
						
						String pos1=parts[3];
						String pos2=otherReadFull[3];
						
						String seq1=parts[9];
						String seq2=otherReadFull[9];


						System.out.println(
								chrom1+","+pos1+","+
								chrom2+","+pos2+","+
								count1a+","+count1b+","+
								count2a+","+count2b+","+
								seq1+","+seq2
								
								+","+parts[0]+","+otherReadFull[0]
								);
						
						
					}
					
					
					//////////// TODO
					//cb, readseq, otherRead
					
					
				}
			} else {
				previousRead.put(readname, parts);
			}

		}
		inp.close();
		
		//System.out.println("total records read: "+readRecords+"    num reads to be matched up: "+previousRead.size());
	}

	
	public static void main(String[] args) throws IOException {
		
		read(new File("/media/mahogny/Elements/lib2/atac_possorted_bam.bam"));
		//;new File(args[0]));
		
		
	}
	
}
