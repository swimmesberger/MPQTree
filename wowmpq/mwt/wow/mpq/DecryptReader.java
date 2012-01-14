package mwt.wow.mpq;

import java.io.IOException;

class DecryptReader extends Reader {

	private Reader reader;

	private long length;

	private int key;

	private int seed;

	private int decrypted;

	private int decryptedBytesLeft;

	static final int[] cryptTable = MpqArchive.cryptTable;

	public DecryptReader(Reader reader, long length, int key) {
		if (reader == null) {
			throw new NullPointerException("reader");
		}
		if (cryptTable == null) {
			throw new NullPointerException("cryptTable");
		}
		if ((length & 3) != 0) {
			throw new IllegalArgumentException("length");
		}
		this.reader = reader;
		this.length = length;
		this.key = key;
		this.seed = 0xeeeeeeee;
	}

	@Override
	protected int readByte() throws IOException {
		if (decryptedBytesLeft == 0) {
			if (length <= 0) {
				return -1;
			}
			seed += cryptTable[0x400 + (key & 0xff)];
			decrypted = reader.readInt32() ^ (key + seed);
			key = ((~key << 0x15) + 0x11111111) | (key >>> 0xb);
			seed = decrypted + seed + (seed << 5) + 3;
			decryptedBytesLeft = 4;
			length -= 4;
		}
		int b = decrypted & 0xff;
		decrypted >>>= 8;
		decryptedBytesLeft--;
		return b;
	}

	@Override
	public void seek(long position) throws IOException {
		reader.seek(position);
	}

}
