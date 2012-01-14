package mwt.wow.mpq;

import java.io.EOFException;
import java.io.IOException;

abstract class Reader {

	public abstract void seek(long position) throws IOException;

	public String readChar4() throws IOException {
		int c1 = readByte();
		int c2 = readByte();
		int c3 = readByte();
		int c4 = readByte();
		if (c4 == -1)
			throw new EOFException();
		return new String(new char[] { (char) c1, (char) c2, (char) c3,
				(char) c4 });
	}

	public int readInt32() throws IOException {
		int c1 = readByte();
		int c2 = readByte();
		int c3 = readByte();
		int c4 = readByte();
		if (c4 == -1)
			throw new EOFException();
		return c1 + c2 * 256 + c3 * 256 * 256 + c4 * 256 * 256 * 256;
	}

	public int readInt16() throws IOException {
		int c1 = readByte();
		int c2 = readByte();
		if (c2 == -1)
			throw new EOFException();
		return c1 + c2 * 256;
	}

	public int readInt8() throws IOException {
		int c1 = readByte();
		if (c1 == -1)
			throw new EOFException();
		return c1;
	}

	protected abstract int readByte() throws IOException;
	
    public int readBlock(byte b[], int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}

		int c = readByte();
		if (c == -1) {
			return -1;
		}
		b[off] = (byte) c;

		int i = 1;
		try {
			for (; i < len; i++) {
				c = readByte();
				if (c == -1) {
					break;
				}
				b[off + i] = (byte) c;
			}
		} catch (IOException ee) {
			// eat exception, return the bytes we did read
		}
		return i;
	}

	public void close() throws IOException {
		// can optionally be implemented by subclasses
	}

}
