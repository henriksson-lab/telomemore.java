package telomemore.java;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;


/**
 * A generic counter for reads
 * 
 * @author Johan Henriksson
 *
 */
public interface Counter {

	void count(String bc, String seq1, String seq2);

	void process();

	public Collection<String> getBC();

	public void addOutputHeader(ArrayList<String> header);

	public void addCellInfo(ArrayList<String> line, String bc);

	public void storeExtras(File fOutCSV) throws FileNotFoundException;

}