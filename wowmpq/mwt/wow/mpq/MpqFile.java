package mwt.wow.mpq;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MpqFile {

	final MpqArchive mpqArchive;

	final String filePath;

	final HashTableEntry hashTableEntry;

	final BlockTableEntry blockTableEntry;

	protected MpqFile(MpqArchive mpqArchive, String filePath,
			HashTableEntry hashTableEntry, BlockTableEntry blockTableEntry) {
		this.mpqArchive = mpqArchive;
		this.filePath = filePath;
		this.hashTableEntry = hashTableEntry;
		this.blockTableEntry = blockTableEntry;
	}

	public String getFilePath() {
		return filePath;
	}

	public long getFileSize() {
		return blockTableEntry.getFileSize() & 0xffffffffL;
	}

	public InputStream getInputStream() throws IOException {
		return new MpqInputStream(this);
	}

	public void extractTo(File file) throws IOException {
		InputStream inputStream = getInputStream();
		try {
			OutputStream outputStream = new FileOutputStream(file);
			try {
				byte[] buffer = new byte[mpqArchive.getSectorSize()];
				while (true) {
					int len = inputStream.read(buffer);
					if (len == -1) {
						break;
					}
					outputStream.write(buffer, 0, len);
				}
			} finally {
				outputStream.close();
			}
		} finally {
			inputStream.close();
		}
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("File path: ").append(filePath).append('\n');
		s.append(hashTableEntry);
		s.append(blockTableEntry);
		return s.toString();
	}

}
