package mwt.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import mwt.wow.mpq.MpqArchive;

public class DumpMpqStructure {

	public static void main(String[] args) throws IOException {
		File mpqFile = new File(args[0]);
		MpqArchive mpqArchive = new MpqArchive(mpqFile);
		mpqArchive.readExtData();
		mpqArchive.dump(args.length > 1 ? new PrintStream(new File(args[1]))
				: System.out);
		mpqArchive.close();
	}
}
