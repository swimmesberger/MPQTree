package mwt.tools;

import java.io.File;
import java.io.IOException;

import mwt.wow.mpq.MpqArchive;
import mwt.wow.mpq.ReadWriteMpqArchive;

public class PackMpq {

	public static void main(String[] args) throws IOException {
		File mpqFile = new File(args[0]);
		File dir = new File(args[1]);
		dir.mkdirs();

		mpqFile.delete();

		long start = System.currentTimeMillis();
		ReadWriteMpqArchive mpqArchive = new MpqArchive(mpqFile, true);
		int count = countFiles(dir);
		int hashTableSize = Integer.highestOneBit(count * 4 / 3) << 1;
		mpqArchive.initHashtable(hashTableSize);
		addDir(mpqArchive, dir, "");
		mpqArchive.saveMetadata();
		mpqArchive.close();
		long end = System.currentTimeMillis();
		System.out.println("Took: " + (end - start) + "ms");
	}

	private static int countFiles(File dir) throws IOException {
		int count = 0;
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				count += countFiles(file);
			} else {
				count++;
			}
		}
		return count;
	}

	private static void addDir(ReadWriteMpqArchive mpqArchive, File dir,
			String baseName) throws IOException {
		for (File file : dir.listFiles()) {
			String name = baseName + file.getName();
			if (file.isDirectory()) {
				addDir(mpqArchive, file, name + '\\');
			} else {
				if (!name.equalsIgnoreCase("(attributes)")) {
					mpqArchive.addFile(file, name, 0, 0);
				}
			}
		}
	}
}
