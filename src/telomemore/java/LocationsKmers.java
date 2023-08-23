package telomemore.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class LocationsKmers {
	
	
	
	public static void main(String[] args) throws IOException {
		
		File inputFasta=new File("/home/mahogny/Downloads/all.fa");
		
		BufferedReader br=new BufferedReader(new FileReader(inputFasta));
		
		String curChrom=null;
		StringBuilder seq=new StringBuilder();
		
		while(true) {

			//Read one chromosome		
			String line=br.readLine();
			if(line==null)
				break;
			if(line.startsWith(">")) {
				if(curChrom!=null) {
					process(curChrom, seq.toString());
				}
				curChrom=line.substring(1);
			}
			else 
				seq.append(line);
		}
		
		
		br.close();
		
	}

	private static void process(String curChrom, String seq) {

		int numkmer=3;
		
		String kmer="TTAGGG";

		//Find all occurrences
		int curpos=0;
		ArrayList<Integer> listpos=new ArrayList<>();
		while(true) {
			int nextpos=seq.indexOf(kmer, curpos);
			if(nextpos==-1)
				break;
			listpos.add(nextpos);
			curpos=nextpos;			
		}
		
		
		//Check each occurrence
		for(int i=0;i<listpos.size();i++) {
			int j=i+numkmer-1;
			
			
			
		}
		
		
	}

}
