package mwt.wow.mpq;

import java.io.IOException;
import java.io.InputStream;

class MpqInputStream extends InputStream {

	private MpqFile file;

	private Reader sectorReader;

	private long length;

	private int[] sectorTable;

	private int sectorSize;

	private int readerSector;

	private int readerOffset;

	MpqInputStream(MpqFile file) throws IOException {
		this.file = file;
		FileReader reader = new FileReader(file.mpqArchive.randomAccessFile,
				file.mpqArchive.archiveOffset
						+ file.blockTableEntry.getBlockOffset());
		length = file.blockTableEntry.getFileSize() & 0xffffffffl;
		if (file.blockTableEntry.isSingleUnit()) {
			sectorSize = file.blockTableEntry.getFileSize();
		} else {
			sectorSize = 512 * (1 << file.mpqArchive.sectorSizeShift);
		}
		if ((file.blockTableEntry.isCompressed() || file.blockTableEntry
				.isImploded())
				&& !file.blockTableEntry.isSingleUnit()) {
			sectorTable = new int[(file.blockTableEntry.getFileSize()
					+ sectorSize - 1)
					/ sectorSize + 1];
			for (int i = 0; i < sectorTable.length; i++) {
				sectorTable[i] = reader.readInt32();
			}
		}
		readerSector = -1;
		readerOffset = 0;
	}

	@Override
	public int read() throws IOException {
		if (readerOffset >= length) {
			return -1;
		}
		if (readerOffset / sectorSize == readerSector + 1) {
			prepareSector();
		}
		readerOffset++;
		return sectorReader.readInt8();
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}
		if (readerOffset >= length) {
			return -1;
		}
		if (len > length - readerOffset) {
			len = (int) (length - readerOffset);
		}
		int bytesRead = 0;
		while (bytesRead < len && readerOffset < length) {
			if (readerOffset / sectorSize == readerSector + 1) {
				prepareSector();
			}
			int bytesAvailable = sectorSize - (readerOffset % sectorSize);
			int remaining = len - bytesRead;
			int toRead = bytesAvailable < remaining ? bytesAvailable : remaining;
			toRead = sectorReader.readBlock(b, off + bytesRead, toRead);
			if (toRead == -1) {
				return bytesRead == 0 ? -1 : bytesRead;
			}
			readerOffset += toRead;
			bytesRead += toRead;
		}
		return bytesRead;
	}

	private void prepareSector() throws IOException {
		readerSector++;
		if (file.blockTableEntry.isCompressed()
				|| file.blockTableEntry.isImploded()) {
			int compressedSectorSize;
			if (file.blockTableEntry.isSingleUnit()) {
				compressedSectorSize = file.blockTableEntry.getBlockSize();
			} else {
				compressedSectorSize = sectorTable[readerSector + 1]
						- sectorTable[readerSector];
			}
			long curSectorSize = length - readerOffset;
			if (!file.blockTableEntry.isSingleUnit() && curSectorSize > sectorSize) {
				curSectorSize = sectorSize;
			}
			if (!file.blockTableEntry.isSingleUnit()) {
				sectorReader = new FileReader(file.mpqArchive.randomAccessFile,
						file.mpqArchive.archiveOffset
								+ file.blockTableEntry.getBlockOffset()
								+ sectorTable[readerSector]);
			} else {
				sectorReader = new FileReader(file.mpqArchive.randomAccessFile,
						file.mpqArchive.archiveOffset
								+ file.blockTableEntry.getBlockOffset()
								+ readerSector * sectorSize);
			}
			if (file.blockTableEntry.isEncrypted()) {
				throw new IOException("todo encryption");
			}
			if (file.blockTableEntry.isCompressed()) {
				if (compressedSectorSize < curSectorSize) {
					int compressionFlags = sectorReader.readInt8();
					if ((compressionFlags & 0x10) != 0) {
						sectorReader = new BZip2Reader(sectorReader, compressedSectorSize - 1);
					}
					if ((compressionFlags & 0x08) != 0) {
						throw new IOException("todo imploded");
					}
					if ((compressionFlags & 0x02) != 0) {
						sectorReader = new DeflateReader(sectorReader, compressedSectorSize - 1);
					}
					if ((compressionFlags & 0x01) != 0) {
						throw new IOException("todo huffman");
					}
					if ((compressionFlags & 0x80) != 0) {
						throw new IOException("ima adpcm stereo");
					}
					if ((compressionFlags & 0x40) != 0) {
						throw new IOException("ima adpcm mono");
					}
					if ((compressionFlags & 0x24) != 0) {
						throw new IOException("unsupported compression "
								+ String.format("%02x", compressionFlags));
					}
				}
			} else {
				throw new IOException();
			}
		} else {
			sectorReader = new FileReader(file.mpqArchive.randomAccessFile,
					file.mpqArchive.archiveOffset
							+ file.blockTableEntry.getBlockOffset()
							+ readerSector * sectorSize);
		}
	}

	@Override
	public void close() throws IOException {
		super.close();
		if (sectorReader != null) {
			sectorReader.close();
		}
		file = null;
		sectorReader = null;
	}

}
