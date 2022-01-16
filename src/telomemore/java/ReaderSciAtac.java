package telomemore.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

/**
 * Reader for sciATAC
 * 
 * @author Johan Henriksson
 *
 */
public class ReaderSciAtac {

	public static BufferedReader openBR(File fInput) throws IOException {
		InputStream is=new FileInputStream(fInput);
		if(fInput.getName().endsWith(".gz"))
			is = new GZIPInputStream(is);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		return br;
	}
	
	public void read(
			File fR1,File fR2,
			ArrayList<Counter> listCounters) throws IOException {
		
		BufferedReader br1 = openBR(fR1);
		BufferedReader br2 = openBR(fR2);

		int numReads=0;
		
		String line=null;
		while((line=br1.readLine())!=null) {
			//Handle R1
			//@AACATCCTCGTAGTTGGTTGGA:K00168:263:H7WNNBBXY:5:1103:30787:4127 1:N:0:0
			String bc=line.split(":", 2)[0].substring(1);
			String seq1=br1.readLine();
			br1.readLine();
			br1.readLine();

			//Handle R2
			br2.readLine();
			String seq2=br2.readLine();
			br2.readLine();
			br2.readLine();

			for(Counter counter:listCounters)
				counter.count(bc, seq1, seq2);
			
			if(numReads%1000000==0)
				System.out.println("#reads so far: "+numReads);
			numReads++;
		}
		System.out.println("#reads so far: "+numReads);
		
		br1.close();
		br2.close();
		
	}
	
	
	

}
