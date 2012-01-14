package mwt.wow.mpq;

import java.io.EOFException;
import java.io.IOException;

class CryptWriter extends Writer {

	private Writer writer;

	private long length;

	private int key;

	private int seed;

	private int bytes;

	private int byteCount;

	static final int[] cryptTable = MpqArchive.cryptTable;

	public CryptWriter(Writer writer, long length, int key) {
		if (writer == null) {
			throw new NullPointerException("writer");
		}
		if ((length & 3) != 0) {
			throw new IllegalArgumentException("length");
		}
		this.writer = writer;
		this.length = length;
		this.key = key;
		this.seed = 0xeeeeeeee;
	}

	@Override
	protected void writeByte(int v) throws IOException {
		if (length <= 0) {
			throw new EOFException();
		}
		bytes |= (v & 0xff) << (byteCount * 8);
		byteCount++;
		if (byteCount == 4) {
			seed += cryptTable[0x400 + (key & 0xff)];
			writer.writeInt32(bytes ^ (key + seed));
			key = ((~key << 0x15) + 0x11111111) | (key >>> 0xb);
			seed = bytes + seed + (seed << 5) + 3;
			byteCount = 0;
			length -= 4;
			bytes = 0;
		}
	}

	@Override
	public void seek(long position) throws IOException {
		writer.seek(position);
	}

}
