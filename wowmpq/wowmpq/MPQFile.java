package wowmpq;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import mwt.wow.mpq.MpqArchive;
import mwt.wow.mpq.ReadMpqArchive;

/**
 * The mpq "file" class.
 * Basicaly its just a wrap around of mwaat's real mpq functions.
 * In order to be later able to change the whole mpq engine i tried to broke
 * down all the posibilitys to that what is needed in taliis-> extract files! 
 * 
 * @author tharo
 *
 */

public class MPQFile {
	File mpqFile;
	ReadMpqArchive mpqArchive = null;
	boolean parsed = false;
	
	/**
	 * get the file handle
	 * @param f
	 */
	public MPQFile(File f) {
		mpqFile = f;
	}
	// no bytebuffer acepted/needed
	public MPQFile(ByteBuffer dataBuffer){};
	
	/**
	 * Had we init the mpq archive?
	 * @return
	 */
	public boolean isParsed() {
		return parsed;
	}
	
	/**
	 * Read in the archive!
	 * @throws IOException 
	 */
	public void parseFile() throws IOException {
		mpqArchive = new MpqArchive(mpqFile);
		mpqArchive.readExtData();
		parsed=true;
	}
	
	/**
	 * Close our archive
	 * @throws IOException 
	 */
	public void close() throws IOException {
		if(mpqArchive!=null)
			mpqArchive.close();
	
		mpqArchive = null;
		parsed=false;
	}
	
	/**
	 * Main function of the while thing: extract a file to a destination!
	 * needed subfolders get automaticaly created ..
	 * 
	 * @param filename
	 * @param destination
	 * @throws IOException
	 */
	public void extractFile(String filename, File destination) throws IOException {
		destination.getParentFile().mkdirs();
		
		mpqArchive
			.getFile(filename, null, null)
				.extractTo(destination);
	}
	
	public ReadMpqArchive getHanlde() {
		return mpqArchive;
	}
}
