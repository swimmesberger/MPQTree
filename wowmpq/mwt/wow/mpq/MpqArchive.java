package mwt.wow.mpq;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class MpqArchive implements ReadMpqArchive, ReadWriteMpqArchive {
	
	private static final String MPQ_SIGNATURE = "MPQ\032";

	private final File file;

	int archiveOffset;

	private String signature;

	private int headerSize;

	private long archiveSize;

	private int formatVersion;

	int sectorSizeShift;

	private int unknown1;

	private long hashTableOffset;

	private long blockTableOffset;

	private int hashTableEntries;

	private int blockTableEntries;

	private long extBlockTableOffset;
	
	private Integer extAttrsVersion;
	
	private Integer extAttrsPresent;

	private final List<BlockTableEntry> blockTable = new ArrayList<BlockTableEntry>();

	private HashTableEntry[] hashTable;

	RandomAccessFile randomAccessFile;
	
	private final boolean rw;

	static final int[] cryptTable;

	static {
		cryptTable = new int[0x500];
		int seed = 0x00100001;
		int index1;
		int index2;
		int i;
		for (index1 = 0; index1 < 0x100; index1++) {
			for (index2 = index1, i = 0; i < 5; i++, index2 += 0x100) {
				int temp1, temp2;
				seed = (seed * 125 + 3) % 0x2aaaab;
				temp1 = (seed & 0xffff) << 0x10;

				seed = (seed * 125 + 3) % 0x2aaaab;
				temp2 = (seed & 0xffff);

				cryptTable[index2] = temp1 | temp2;
			}
		}
	}

	public MpqArchive(File file) throws IOException {
		this(file, false);
	}

	public MpqArchive(File file, boolean rw) throws IOException {
		this.file = file;
		this.rw = rw;
		openAndRead();
	}

	public File getArchiveFile() {
		return file;
	}

	public void close() throws IOException {
		if (randomAccessFile != null) {
			randomAccessFile.close();
			randomAccessFile = null;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (randomAccessFile != null) {
			System.err.println("MpqArchive was not closed (finalizer)");
			close();
		}
	}

	private int hashString(String string, int hashType) {
		int seed1 = 0x7fed7fed;
		int seed2 = 0xeeeeeeee;

		string = string.toUpperCase();
		for (int i = 0; i < string.length(); i++) {
			int ch = string.charAt(i);
			seed1 = cryptTable[hashType * 0x100 + ch] ^ (seed1 + seed2);
			seed2 = ch + seed1 + seed2 + (seed2 << 5) + 3;
		}

		return seed1;
	}

	private void openAndRead() throws IOException {
		randomAccessFile = new RandomAccessFile(file, rw ? "rw" : "r");
		archiveOffset = 0;
		if (randomAccessFile.length() == 0) {
			writeHeader();
		}
		findArchiveOffset();
		readHeader();
		readBlockTable();
		readHashTable();
	}
	
	int getSectorSize() {
		return 512 * (1 << sectorSizeShift);
	}

	private void writeHeader() throws IOException {
		checkReadWrite();
		if (signature == null) {
			// new file
			signature = MPQ_SIGNATURE;
			headerSize = 44; // ?
			archiveSize = 0; // ?
			formatVersion = 1;
			sectorSizeShift = 3;
			unknown1 = 0;
			hashTableOffset = 0;
			blockTableOffset = 0;
			hashTableEntries = 0;
			blockTableEntries = 0;
			extBlockTableOffset = 0;
		}
		FileWriter fileWriter = new FileWriter(randomAccessFile, archiveOffset);
		fileWriter.writeChar4(signature);
		fileWriter.writeInt32(headerSize);
		fileWriter.writeInt32((int) archiveSize);
		fileWriter.writeInt16(formatVersion);
		fileWriter.writeInt8(sectorSizeShift);
		fileWriter.writeInt8(unknown1);
		fileWriter.writeInt32((int) hashTableOffset);
		fileWriter.writeInt32((int) blockTableOffset);
		fileWriter.writeInt32(hashTableEntries);
		fileWriter.writeInt32(blockTableEntries);
		if (formatVersion >= 1) {
			fileWriter.writeInt32((int) extBlockTableOffset);
			fileWriter.writeInt32((int) (extBlockTableOffset >>> 32));
			fileWriter.writeInt16((int) (hashTableOffset >>> 32));
			fileWriter.writeInt16((int) (blockTableOffset >>> 32));
		}
		fileWriter.close();
	}

	private void checkReadWrite() {
		if (!rw) {
			throw new IllegalStateException("read only");
		}
	}
	
	private void writeBlockTable() throws IOException {
		checkReadWrite();
		assert blockTableEntries == blockTable.size();
		if (formatVersion >= 1 && extBlockTableOffset != 0) {
			FileWriter extBlockTableWriter = new FileWriter(randomAccessFile, archiveOffset
					+ extBlockTableOffset);
			for (BlockTableEntry entry : blockTable) {
				extBlockTableWriter.writeInt16((int) (entry.getBlockOffset() >>> 32));
			}
		}
		FileWriter fileWriter = new FileWriter(randomAccessFile, archiveOffset
				+ blockTableOffset);
		CryptWriter blockTableWriter = new CryptWriter(fileWriter,
				blockTableEntries * 16, hashString("(block table)", 3));
		for (BlockTableEntry entry : blockTable) {
			blockTableWriter.writeInt32((int) entry.getBlockOffset());
			blockTableWriter.writeInt32(entry.getBlockSize());
			blockTableWriter.writeInt32(entry.getFileSize());
			blockTableWriter.writeInt32(entry.getFlags());
		}
	}

	private void writeHashTable() throws IOException {
		checkReadWrite();
		FileWriter fileWriter = new FileWriter(randomAccessFile, archiveOffset
				+ hashTableOffset);
		CryptWriter cryptWriter = new CryptWriter(fileWriter,
				hashTableEntries * 16, hashString("(hash table)", 3));
		for (int i = 0; i < hashTableEntries; i++) {
			if (hashTable[i] == null) {
				hashTable[i] = new HashTableEntry(-1, -1, -1, -1, -1, -1);
			}
			cryptWriter.writeInt32(hashTable[i].getFilePathHashA());
			cryptWriter.writeInt32(hashTable[i].getFilePathHashB());
			cryptWriter.writeInt16(hashTable[i].getLanguage());
			cryptWriter.writeInt8(hashTable[i].getPlatform());
			cryptWriter.writeInt8(hashTable[i].getUnknown());
			cryptWriter.writeInt32(hashTable[i].getFileBlockIndex());
		}
	}
	
	public void saveMetadata() throws IOException {
		checkReadWrite();
		
		writeAttributesFile();
		
		long maxBlockEnd = headerSize;
		boolean extBlockTableNeeded = false;
		for (BlockTableEntry blockTableEntry : blockTable) {
			long blockEnd = blockTableEntry.getBlockOffset() + blockTableEntry.getBlockSize();
			if (blockEnd > maxBlockEnd)
				maxBlockEnd = blockEnd;
			if (blockTableEntry.getBlockOffset() > 0xffffffffL)
				extBlockTableNeeded = true;
		}
		
		if (maxBlockEnd > hashTableOffset) {
			hashTableOffset = maxBlockEnd;
		}
		hashTableEntries = hashTable.length;
		maxBlockEnd = hashTableOffset + hashTableEntries * 16;
		
		if (maxBlockEnd > blockTableOffset) {
			blockTableOffset = maxBlockEnd;
		}
		blockTableEntries = blockTable.size();
		maxBlockEnd = blockTableOffset + blockTableEntries * 16;
		
		if (extBlockTableNeeded) {
			if (maxBlockEnd > extBlockTableOffset) {
				extBlockTableOffset = maxBlockEnd;
			}
			maxBlockEnd = extBlockTableOffset + blockTableEntries * 2;
		} else {
			extBlockTableOffset = 0;
		}
		
		archiveSize = maxBlockEnd;
		
		writeHeader();
		writeHashTable();
		writeBlockTable();
	}
	
	public boolean isNew() {
		assert (hashTable.length == 0) == blockTable.isEmpty();
		return hashTable.length == 0;
	}
	
	public void initHashtable(int size) {
		if (!isNew())
			throw new IllegalStateException();
		if (Integer.bitCount(size) != 1)
			throw new IllegalArgumentException("size must be (1<<x)");
		hashTable = new HashTableEntry[size];
		hashTableEntries = size;
		blockTable.clear();
	}

	private void findArchiveOffset() throws IOException {
		int offset = 0;
		while (offset < randomAccessFile.length()) {
			FileReader fileReader = new FileReader(randomAccessFile, offset);
			signature = fileReader.readChar4();
			if (!signature.equals(MPQ_SIGNATURE)) {
				offset += 1024;
				continue;
			}
			archiveOffset = offset;
			return;
		}
		throw new IOException("no valid MPQ signature found");
	}

	private void readHeader() throws IOException {
		FileReader fileReader = new FileReader(randomAccessFile, archiveOffset);
		signature = fileReader.readChar4();
		if (!signature.equals(MPQ_SIGNATURE)) {
			throw new IOException("no valid MPQ signature found");
		}
		headerSize = fileReader.readInt32();
		archiveSize = fileReader.readInt32() & 0xffffffffL;
		formatVersion = fileReader.readInt16();
		sectorSizeShift = fileReader.readInt8();
		unknown1 = fileReader.readInt8();
		hashTableOffset = fileReader.readInt32() & 0xffffffffL;
		blockTableOffset = fileReader.readInt32() & 0xffffffffL;
		hashTableEntries = fileReader.readInt32();
		blockTableEntries = fileReader.readInt32();
		if (formatVersion >= 1) {
			extBlockTableOffset = fileReader.readInt32() & 0xffffffffL;
			extBlockTableOffset |= (fileReader.readInt32() & 0xffffffffL) << 32;
			hashTableOffset |= (fileReader.readInt16() & 0xffffL) << 32;
			blockTableOffset |= (fileReader.readInt16() & 0xffffL) << 32;
		}
		fileReader.close();
	}

	private void readBlockTable() throws IOException {
		blockTable.clear();
		int[] highOffsetBits = new int[blockTableEntries];
		if (formatVersion >= 1 && extBlockTableOffset != 0) {
			FileReader extBlockTableReader = new FileReader(randomAccessFile, archiveOffset
					+ extBlockTableOffset);
			for (int i = 0; i < blockTableEntries; i++) {
				highOffsetBits[i] = extBlockTableReader.readInt16() & 0xffff;
			}
		}
		FileReader fileReader = new FileReader(randomAccessFile, archiveOffset
				+ blockTableOffset);
		DecryptReader blockTableReader = new DecryptReader(fileReader,
				blockTableEntries * 16, hashString("(block table)", 3));
		for (int i = 0; i < blockTableEntries; i++) {
			long blockOffset = blockTableReader.readInt32() & 0xffffffffL;
			blockOffset |= ((long) highOffsetBits[i]) << 32;
			int blockSize = blockTableReader.readInt32();
			int fileSize = blockTableReader.readInt32();
			int flags = blockTableReader.readInt32();
			blockTable.add(new BlockTableEntry(
					blockOffset, blockSize, fileSize, flags));
		}
		assert blockTableEntries == blockTable.size();
	}

	private void readHashTable() throws IOException {
		FileReader fileReader = new FileReader(randomAccessFile, archiveOffset
				+ hashTableOffset);
		DecryptReader decryptReader = new DecryptReader(fileReader,
				hashTableEntries * 16, hashString("(hash table)", 3));
		hashTable = new HashTableEntry[hashTableEntries];
		for (int i = 0; i < hashTableEntries; i++) {
			int filePathHashA = decryptReader.readInt32();
			int filePathHashB = decryptReader.readInt32();
			int language = decryptReader.readInt16();
			int platform = decryptReader.readInt8();
			int unknown = decryptReader.readInt8();
			int fileBlockIndex = decryptReader.readInt32();
			hashTable[i] = new HashTableEntry(filePathHashA, filePathHashB,
					language, platform, unknown, fileBlockIndex);
		}
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("File: ").append(file).append('\n');
		s.append("Archive offset: ").append(archiveOffset).append('\n');
		s.append("Signature: ").append(signature).append('\n');
		s.append("Header size: ").append(headerSize).append('\n');
		s.append("Archive size: ").append(archiveSize).append('\n');
		s.append("Archive version: ").append(formatVersion).append('\n');
		s.append("Sector size shift: ").append(sectorSizeShift).append('\n');
		s.append("unknown1: ").append(unknown1).append('\n');
		s.append("Hash table offset: ").append(hashTableOffset).append('\n');
		s.append("Block table offset: ").append(blockTableOffset).append('\n');
		s.append("Hash table entries: ").append(hashTableEntries).append('\n');
		s.append("Block table entries: ").append(blockTableEntries)
				.append('\n');
		if (formatVersion >= 1) {
			s.append("Extended block table offset: ").append(
					extBlockTableOffset).append('\n');
		}
		for (BlockTableEntry block : blockTable) {
			s.append(block.toString());
		}
		for (int i = 0; i < hashTable.length; i++) {
			HashTableEntry hashTableEntry = hashTable[i];
			if (hashTableEntry.getFileBlockIndex() != -1) {
				s.append(hashTableEntry.toString());
			}
		}
		return s.toString();
	}

	private void checkClosed() {
		if (randomAccessFile == null) {
			throw new IllegalStateException("Archive is closed");
		}
	}

	public MpqFile getFile(String filePath, Integer language, Integer platform)
			throws IOException {
		checkClosed();
		int initEntry = hashString(filePath, 0) & (hashTableEntries - 1);
		HashTableEntry entry = hashTable[initEntry];
		if (entry == null || entry.getFileBlockIndex() == -1) {
			return null;
		}
		int hashA = hashString(filePath, 1);
		int hashB = hashString(filePath, 2);
		int curEntry = initEntry;
		do {
			if (entry != null && entry.getFileBlockIndex() != -2) {
				if (entry.getFilePathHashA() == hashA
						&& entry.getFilePathHashB() == hashB
						&& (language == null || entry.getLanguage() == language)
						&& (platform == null || entry.getPlatform() == platform)) {
					entry.setFilePath(filePath);
					blockTable.get(entry.getFileBlockIndex()).setFilePath(filePath);
					return new MpqFile(this, filePath, entry, blockTable
							.get(entry.getFileBlockIndex()));
				}
			}
			curEntry = (curEntry + 1) % hashTableEntries;
			entry = hashTable[curEntry];
		} while (entry != null && entry.getFileBlockIndex() != -1 && curEntry != initEntry);
		return null;
	}
	
	public boolean deleteFile(String filePath, Integer language, Integer platform) {
		checkClosed();
		checkReadWrite();
		int initEntry = hashString(filePath, 0) & (hashTableEntries - 1);
		HashTableEntry entry = hashTable[initEntry];
		if (entry == null || entry.getFileBlockIndex() == -1) {
			return false;
		}
		int hashA = hashString(filePath, 1);
		int hashB = hashString(filePath, 2);
		int curEntry = initEntry;
		boolean deletedSomething = false;
		do {
			if (entry != null && entry.getFileBlockIndex() != -2) {
				if (entry.getFilePathHashA() == hashA
						&& entry.getFilePathHashB() == hashB
						&& (language == null || entry.getLanguage() == language)
						&& (platform == null || entry.getPlatform() == platform)) {
					int blockIndex = entry.getFileBlockIndex();
					blockTable.set(blockIndex, new BlockTableEntry(
							blockTable.get(blockIndex).getBlockOffset(),
							blockTable.get(blockIndex).getBlockSize(), 0, 0));
					int nextEntry = (curEntry + 1) % hashTableEntries;
					if (hashTable[nextEntry].getFileBlockIndex() == -1) {
						hashTable[curEntry] = new HashTableEntry(0, 0, 0, 0, 0, -2);
					} else {
						hashTable[curEntry] = new HashTableEntry(0, 0, 0, 0, 0, -1);
					}
					deletedSomething = true;
				}
			}
			curEntry = (curEntry + 1) % hashTableEntries;
			entry = hashTable[curEntry];
		} while (entry != null && entry.getFileBlockIndex() != -1 && curEntry != initEntry);
		return deletedSomething;
	}
	
	private int findFreeHashTableEntry(int fileHash) {
		int initEntry = fileHash & (hashTableEntries - 1);
		HashTableEntry entry = hashTable[initEntry];
		int curEntry = initEntry;
		do {
			if (entry == null || entry.getFileBlockIndex() == -1 || entry.getFileBlockIndex() == -2) {
				return curEntry;
			}
			curEntry = (curEntry + 1) % hashTableEntries;
			entry = hashTable[curEntry];
		} while (curEntry != initEntry);
		return -1;
	}
	
	public boolean addFile(File newFile, String filePath, int language,
			int platform) throws IOException {
		checkClosed();
		checkReadWrite();
		long fileSize = newFile.length();
		FileInputStream fileInputStream = new FileInputStream(newFile);
		try {
			return addFile(filePath, language, platform, fileInputStream,
					fileSize);
		} finally {
			fileInputStream.close();
		}
	}

	public boolean addFile(String filePath, int language, int platform,
			InputStream inputStream, long fileSize) throws IOException {
		long maxBlockEnd = headerSize;
		for (BlockTableEntry blockTableEntry : blockTable) {
			long blockEnd = blockTableEntry.getBlockOffset() + blockTableEntry.getBlockSize();
			if (blockEnd > maxBlockEnd)
				maxBlockEnd = blockEnd;
		}
		int hashTableEntry = findFreeHashTableEntry(hashString(filePath, 0));
		if (hashTableEntry < 0) {
			return false;
		}
		int hashA = hashString(filePath, 1);
		int hashB = hashString(filePath, 2);
		MpqWriter mpqWriter = new MpqWriter(this, maxBlockEnd, fileSize);
		int sectorSize = getSectorSize();
		for (int i = 0; i < (fileSize + sectorSize - 1) / sectorSize; i++) {
			long remaining = fileSize - i * sectorSize;
			byte[] data = new byte[(int) (remaining > sectorSize ? sectorSize
					: remaining)];
			int datalen = inputStream.read(data);
			if (datalen != data.length) {
				throw new IOException("couldn't read input file");
			}
			mpqWriter.writeSector(data);
		}
		BlockTableEntry blockTableEntry = mpqWriter.getBlockTableEntry();
		blockTableEntry.setFilePath(filePath);
		blockTable.add(blockTableEntry);
		HashTableEntry entry = new HashTableEntry(hashA, hashB, language,
				platform, 0, blockTable.indexOf(blockTableEntry));
		entry.setFilePath(filePath);
		hashTable[hashTableEntry] = entry;
		return true;
	}
	
	private boolean writeAttributesFile() throws IOException {
		checkClosed();
		checkReadWrite();

		String filePath = "(attributes)";
		deleteFile(filePath, null, null);
		
		if (extAttrsVersion == null) {
			extAttrsVersion = 100;
			extAttrsPresent = 7;
		}
		int blocks = blockTable.size() + 1;
		int size = 8;
		int attrsPresent = extAttrsPresent;
		if ((attrsPresent & 1) != 0) {
			// CRC32
			size += blocks * 4;
		}
		if ((attrsPresent & 2) != 0) {
			// filetime
			size += blocks * 8;
		}
		if ((attrsPresent & 4) != 0) {
			// MD5
			size += blocks * 16;
		}
		ByteBuffer buffer = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
		buffer.putInt(extAttrsVersion);
		buffer.putInt(extAttrsPresent);
		if ((attrsPresent & 1) != 0) {
			for (int i = 0; i < blocks; i++) {
				int crc32;
				if (i < blockTable.size()) {
					Integer v = blockTable.get(i).getExtCRC32();
					crc32 = v == null ? 0 : v;
				} else {
					crc32 = 0;
				}
				buffer.putInt(crc32);
			}
		}
		if ((attrsPresent & 2) != 0) {
			for (int i = 0; i < blocks; i++) {
				long filetime;
				if (i < blockTable.size()) {
					Long v = blockTable.get(i).getExtFiletime();
					filetime = v == null ? 0L : v;
				} else {
					filetime = 0L;
				}
				buffer.putLong(filetime);
			}
		}
		if ((attrsPresent & 4) != 0) {
			for (int i = 0; i < blocks; i++) {
				byte[] md5 = null;
				if (i < blockTable.size()) {
					md5 = blockTable.get(i).getExtMD5();
				}
				if (md5 == null) {
					md5 = new byte[16];
				}
				for (byte b : md5) {
					buffer.put(b);
				}
			}
		}
		assert buffer.position() == buffer.limit();

		ByteArrayInputStream inputStream = new ByteArrayInputStream(buffer
				.array(), buffer.arrayOffset(), size);
		return addFile(filePath, 0, 0, inputStream, size);
	}
	
	private static int isReadInt32(InputStream is) throws IOException {
		int c1 = is.read();
		int c2 = is.read();
		int c3 = is.read();
		int c4 = is.read();
		if (c4 == -1)
			throw new EOFException();
		return c1 + c2 * 256 + c3 * 256 * 256 + c4 * 256 * 256 * 256;
	}
	
	public void readExtData() throws IOException {
		checkClosed();
		MpqFile attrFile = getFile("(attributes)", 0, 0);
		if (attrFile != null) {
			InputStream inputStream = attrFile.getInputStream();
			try {
				int version = isReadInt32(inputStream);
				extAttrsVersion = version;
				int attrsPresent = isReadInt32(inputStream);
				extAttrsPresent = attrsPresent;
				if ((attrsPresent & 1) != 0) {
					// CRC32
					for (int i = 0; i < blockTableEntries; i++) {
						int crc32 = isReadInt32(inputStream);
						blockTable.get(i).setExtCRC32(crc32);
					}
				}
				if ((attrsPresent & 2) != 0) {
					// filetime
					for (int i = 0; i < blockTableEntries; i++) {
						int lowTime = isReadInt32(inputStream);
						int highTime = isReadInt32(inputStream);
						long extFiletime = (lowTime & 0xffffffffL) | ((highTime & 0xffffffffL) << 32);
						blockTable.get(i).setExtFiletime(extFiletime);
					}
				}
				if ((attrsPresent & 4) != 0) {
					// MD5
					for (int i = 0; i < blockTableEntries; i++) {
						byte[] md5 = new byte[16];
						for (int j = 0; j < 16; j++) {
							int b = inputStream.read();
							if (b == -1)
								throw new EOFException();
							md5[j] = (byte) b;
						}
						blockTable.get(i).setExtMD5(md5);
					}
				}
			} finally {
				inputStream.close();
			}
		}
		MpqFile listFile = getFile("(listfile)", 0, 0);
		if (listFile != null) {
			InputStream inputStream = listFile.getInputStream();
			try {
				Scanner scan = new Scanner(inputStream, "UTF-8");
				scan.useDelimiter(Pattern.compile("[;\\r\\n]+"));
				while (scan.hasNext()) {
					String name = scan.next();
					getFile(name, null, null);
				}
				scan.close();
			} finally {
				inputStream.close();
			}
		}
	}
	
	public Collection<String> listFileNames() {
		List<String> names = new ArrayList<String>();
		for (HashTableEntry entry : hashTable) {
			if (entry != null && entry.getFileBlockIndex() != -1 && entry.getFileBlockIndex() != -2) {
				String name = entry.getFilePath();
				if (name != null) {
					names.add(name);
				}
			}
		}
		return names;
	}

	public void dump(PrintStream out) {
		StringBuilder s = new StringBuilder();
		s.append("File: ").append(file).append('\n');
		s.append("Archive offset: ").append(archiveOffset).append('\n');
		s.append("Signature: ").append(signature).append('\n');
		s.append("Header size: ").append(headerSize).append('\n');
		s.append("Archive size: ").append(archiveSize).append('\n');
		s.append("Archive version: ").append(formatVersion).append('\n');
		s.append("Sector size shift: ").append(sectorSizeShift).append('\n');
		s.append("unknown1: ").append(unknown1).append('\n');
		s.append("Hash table offset: ").append(hashTableOffset).append('\n');
		s.append("Block table offset: ").append(blockTableOffset).append('\n');
		s.append("Hash table entries: ").append(hashTableEntries).append('\n');
		s.append("Block table entries: ").append(blockTableEntries)
				.append('\n');
		if (formatVersion >= 1) {
			s.append("Extended block table offset: ").append(
					extBlockTableOffset).append('\n');
		}
		out.print(s);
		for (BlockTableEntry block : blockTable) {
			out.print(block.toString());
		}
		for (int i = 0; i < hashTable.length; i++) {
			HashTableEntry hashTableEntry = hashTable[i];
			if (hashTableEntry.getFileBlockIndex() != -1) {
				out.print(hashTableEntry.toString());
			}
		}
	}

}
