package telomemore.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class CTCFFilter {
	
	BufferedReader brCtcfRegions;
	String chrCTCF;
	int posCTCF;
	
	private boolean nextCTCF() throws IOException {
		String line=brCtcfRegions.readLine();
		if(line==null)
			return false;
		else {
			StringTokenizer stok=new StringTokenizer(line,"\t");
			//System.out.println("ctcf "+line);
			chrCTCF=stok.nextToken();
			posCTCF=Integer.parseInt(stok.nextToken());
			return true;
		}
	}
	
	public void filter(File fTSV, File fCTCF, File fOut) throws IOException {
		
		BufferedReader br=new BufferedReader(new FileReader(fTSV));
		brCtcfRegions=new BufferedReader(new FileReader(fCTCF));

		PrintWriter pw=new PrintWriter(fOut);
		
		if(nextCTCF()) {
			//For each ATAC fragment
			String line;
			done: while((line=br.readLine())!=null) {
				//System.out.println("@ "+line);
				
				if(line.startsWith("#")) {
					pw.println(line);
				} else {
					StringTokenizer stok=new StringTokenizer(line,"\t");

					String chr=stok.nextToken();
					int posFrom=Integer.parseInt(stok.nextToken());
					int posTo=Integer.parseInt(stok.nextToken());

					//Move current CTCF forward until we are in range
					while(chrCTCF.compareTo(chr)<0 || posCTCF<posFrom) {
						//If no more CTCF sites then we are done
						if(!nextCTCF())
							break done;
					}
					
					//Keep this fragment if CTCF in it
					if(chr.equals(chrCTCF) && posCTCF<posTo) {
						//System.out.println("ok");
						pw.println(line);
					}					
				}
			}			
		}
		
		brCtcfRegions.close();
		br.close();
		pw.close();
	}
	
	
	public static void main(String[] args) throws IOException {
		
		File fTSV=new File("/home/mahogny/Downloads/ctcf/test.tsv");
		File fCTCF=new File("/home/mahogny/Downloads/ctcf/test2.tsv");
		File fOut=new File("/home/mahogny/Downloads/ctcf/test.out");
		
		if(args.length>0) {
			fTSV=new File(args[0]);
			fCTCF=new File(args[1]);
			fOut=new File(args[2]);
		}
		
		CTCFFilter f=new CTCFFilter();
		f.filter(
				fTSV,
				fCTCF,
				fOut);
	}

}
