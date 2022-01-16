package telomemore.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

/**
 * 
 * Only count reads within a barcode list
 * 
 * @author Johan Henrikson
 *
 */
public class CounterLimitBC {

	private HashSet<String> bclist=new HashSet<String>();
	
	public CounterLimitBC(File fBarcodes) throws IOException {
		
		GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(fBarcodes));
		BufferedReader br = new BufferedReader(new InputStreamReader(gzip));
		
		String line=null;
		while((line=br.readLine())!=null) {
			//10x barcodes end with -1  -- remove these? need to be an option actually...
			//line=line.replaceAll("-1", "");
			bclist.add(line);
		}
		br.close();
	}
	
	
	/**
	 * Wraps another counter such that that counter will only see filtered reads
	 */
	public Counter wrapCounter(Counter c) {
		return new Counter() {

			@Override
			public void count(String bc, String seq1, String seq2) {
				if(bclist.contains(bc))
					c.count(bc,seq1,seq2);
			}

			@Override
			public void process() {
				c.process();
			}

			@Override
			public Collection<String> getBC() {
				return c.getBC();
			}

			@Override
			public void addOutputHeader(ArrayList<String> header) {
				c.addOutputHeader(header);
			}

			@Override
			public void addCellInfo(ArrayList<String> line, String bc) {
				c.addCellInfo(line, bc);
			}

			@Override
			public void storeExtras(File fOutCSV) throws FileNotFoundException {
				c.storeExtras(fOutCSV);
			}
		};
	}
	
}
