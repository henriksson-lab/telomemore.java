package telomemore.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * 
 * Reader of aligned 10x bam files. Can be position sorted, does not need index.
 * 
 * This is for single-end reads. Currently it ignores UMIs
 * 
 * @author Johan Henriksson
 *
 */
public class Reader10xbamSE {
	
	public void read(
			File fBAM,
			ArrayList<Counter> listCounters)
					throws IOException {
		
		
		// samtools view atac_possorted_bam.bam
		
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
				System.out.println("records so far: "+readRecords);
			}
			
			String[] parts=line.split("\t", 0);
			String readseq=parts[9];
			
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
				for(Counter c:listCounters) {
					c.count(cb, readseq, "");
				}
			}
				
		}
		inp.close();
		
		System.out.println("total records read: "+readRecords);
	}

}
