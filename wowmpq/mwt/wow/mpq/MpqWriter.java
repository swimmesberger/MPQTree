package mwt.wow.mpq;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

public class MpqWriter {

	private MpqArchive archive;

	private long blockOffset;

	private long fileSize;

	private int sectorSize;

	private int nextSector;

	private int[] sectorTable;

	private boolean isSingleUnit;

	private boolean isCompressed;

	private boolean isImploded;

	private CRC32 crc32;

	private MessageDigest md5;

	public MpqWriter(MpqArchive archive, long blockOffset, long fileSize) {
		if (fileSize > 0xffffffffL) {
			throw new IllegalArgumentException("file too large");
		}
		this.archive = archive;
		this.blockOffset = blockOffset;
		this.fileSize = fileSize & 0xffffffffL;
		isCompressed = true;
	}

	public void writeSector(byte[] data) throws IOException {
		if (nextSector == 0) {
			prepareWrite();
		}
		if (crc32 != null) {
			crc32.update(data);
		}
		if (md5 != null) {
			md5.update(data);
		}
		int datalen = data.length;
		byte[] cdata = new byte[datalen];
		Deflater deflater = new Deflater();
		deflater.setInput(data);
		deflater.finish();
		int cdatalen = deflater.deflate(cdata);
		if (cdatalen + 1 >= datalen) {
			sectorTable[nextSector + 1] = sectorTable[nextSector] + datalen;
			FileWriter writer = new FileWriter(archive.randomAccessFile,
					archive.archiveOffset + blockOffset
							+ sectorTable[nextSector]);
			writer.writeBlock(data, 0, datalen);
			writer.close();
		} else {
			sectorTable[nextSector + 1] = sectorTable[nextSector] + cdatalen
					+ 1;
			FileWriter writer = new FileWriter(archive.randomAccessFile,
					archive.archiveOffset + blockOffset
							+ sectorTable[nextSector]);
			writer.writeInt8(0x02); // deflate
			writer.writeBlock(cdata, 0, cdatalen);
			writer.close();
		}
		nextSector++;
		if (nextSector + 1 == sectorTable.length - 1) {
			{
				sectorTable[nextSector + 1] = sectorTable[nextSector];
				// TODO: figure out what this if for. last sector is empty on some files, but not always
				nextSector++;
			}
			FileWriter writer = new FileWriter(archive.randomAccessFile,
					archive.archiveOffset + blockOffset);
			for (int i = 0; i < sectorTable.length; i++) {
				writer.writeInt32(sectorTable[i]);
			}
			writer.close();
		}
	}

	private void prepareWrite() {
		if (isSingleUnit) {
			sectorSize = (int) fileSize;
		} else {
			sectorSize = archive.getSectorSize();
		}
		if ((isCompressed || isImploded) && !isSingleUnit) {
			sectorTable = new int[(int) ((fileSize + sectorSize - 1)
					/ sectorSize + 1) + 1];
			sectorTable[0] = sectorTable.length * 4;
		}
		crc32 = new CRC32();
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public BlockTableEntry getBlockTableEntry() {
		if (fileSize == 0 && sectorTable == null) {
			prepareWrite();
			sectorTable[1] = sectorTable[0];
			nextSector++;
		}
		if (sectorTable == null || nextSector + 1 != sectorTable.length) {
			throw new IllegalStateException("not written all sectors yet");
		}
		int flags = 0;
		flags |= 0x80000000; // isFile
		flags |= 0x04000000; // TODO: what is this for?
		if (isSingleUnit) {
			flags |= 0x01000000;
		}
		// if (isEncryptionAdjusted) {
		// 	flags |= 0x00020000;
		// }
		// if (isEncrypted) {
		// 	flags |= 0x00010000;
		// }
		if (isCompressed) {
			flags |= 0x00000200;
		}
		if (isImploded) {
			flags |= 0x00000100;
		}
		BlockTableEntry blockTableEntry = new BlockTableEntry(blockOffset,
				sectorTable[nextSector], (int) fileSize, flags);
		if (crc32 != null) {
			blockTableEntry.setExtCRC32((int) crc32.getValue());
		}
		if (md5 != null) {
			blockTableEntry.setExtMD5(md5.digest());
		}
		return blockTableEntry;
	}
}
