package mwt.wow.mpq;

class BlockTableEntry {

	private long blockOffset;

	private int blockSize;

	private int fileSize;

	private int flags;

	private String filePath;

	private Integer extCRC32;

	private Long extFiletime;

	private byte[] extMD5;

	public BlockTableEntry(long blockOffset, int blockSize, int fileSize,
			int flags) {
		this.blockOffset = blockOffset;
		this.blockSize = blockSize;
		this.fileSize = fileSize;
		this.flags = flags;
	}

	public long getBlockOffset() {
		return blockOffset;
	}

	public int getBlockSize() {
		return blockSize;
	}

	public int getFileSize() {
		return fileSize;
	}

	public int getFlags() {
		return flags;
	}

	public boolean isFile() {
		return (flags & 0x80000000) != 0;
	}

	public boolean isSingleUnit() {
		return (flags & 0x01000000) != 0;
	}

	public boolean isEncryptionAdjusted() {
		return (flags & 0x00020000) != 0;
	}

	public boolean isEncrypted() {
		return (flags & 0x00010000) != 0;
	}

	public boolean isCompressed() {
		return (flags & 0x00000200) != 0;
	}

	public boolean isImploded() {
		return (flags & 0x00000100) != 0;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Integer getExtCRC32() {
		return extCRC32;
	}

	public void setExtCRC32(Integer extCRC32) {
		this.extCRC32 = extCRC32;
	}

	public Long getExtFiletime() {
		return extFiletime;
	}

	public void setExtFiletime(Long extFiletime) {
		this.extFiletime = extFiletime;
	}

	public byte[] getExtMD5() {
		return extMD5;
	}

	public void setExtMD5(byte[] extMD5) {
		if (extMD5 != null && extMD5.length != 16) {
			throw new IllegalArgumentException("extMD5");
		}
		this.extMD5 = extMD5;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append('\n');
		s.append("Block offset: ").append(blockOffset).append('\n');
		s.append("Block size: ").append(blockSize).append('\n');
		s.append("File size: ").append(fileSize).append('\n');
		s.append("Flags: ").append(String.format("%08x", flags)).append('\n');
		s.append("File path: ").append(filePath).append('\n');
		if (extCRC32 != null) {
			s.append("CRC32: ").append(extCRC32).append('\n');
		}
		if (extFiletime != null) {
			s.append("File time: ").append(extFiletime).append('\n');
		}
		if (extMD5 != null) {
			s.append("MD5:");
			for (byte b : extMD5) {
				s.append(String.format(" %02x", b & 0xff));
			}
			s.append('\n');
		}
		return s.toString();
	}

}
