package mwt.tools;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import mwt.wow.mpq.MpqArchive;
import mwt.wow.mpq.ReadMpqArchive;

public class ExtractMpq {

	public static void main(String[] args) throws IOException {
		File mpqFile = new File(args[0]);
		File dir = new File(args[1]);
		dir.mkdirs();
		
		long start = System.currentTimeMillis();
		ReadMpqArchive mpqArchive = new MpqArchive(mpqFile);
		mpqArchive.readExtData();
		Collection<String> names = mpqArchive.listFileNames();
		for (String name : names) {
			File dest = new File(dir, name);
			if (dest.exists()) {
				continue;
			}
			dest.getParentFile().mkdirs();
			mpqArchive.getFile(name, null, null).extractTo(dest);
		}
		mpqArchive.close();
		long end = System.currentTimeMillis();
		System.out.println("Took: " + (end - start) + "ms");
	}
}
