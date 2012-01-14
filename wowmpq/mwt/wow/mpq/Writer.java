package mwt.wow.mpq;

import java.io.IOException;

abstract class Writer {

	public abstract void seek(long position) throws IOException;

	public void writeChar4(String v) throws IOException {
		if (v == null)
			throw new NullPointerException("no string");
		if (v.length() != 4)
			throw new IllegalArgumentException("invalid string length");
		writeByte(v.charAt(0));
		writeByte(v.charAt(1));
		writeByte(v.charAt(2));
		writeByte(v.charAt(3));
	}

	public void writeInt32(int v) throws IOException {
		writeByte(v & 0xff);
		writeByte((v >>> 8) & 0xff);
		writeByte((v >>> 16) & 0xff);
		writeByte((v >>> 24) & 0xff);
	}

	public void writeInt16(int v) throws IOException {
		writeByte(v & 0xff);
		writeByte((v >>> 8) & 0xff);
	}

	public void writeInt8(int v) throws IOException {
		writeByte(v & 0xff);
	}

	protected abstract void writeByte(int v) throws IOException;

    public void writeBlock(byte b[], int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if ((off < 0) || (off > b.length) || (len < 0)
				|| ((off + len) > b.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}
		for (int i = 0; i < len; i++) {
			writeByte(b[off + i]);
		}
	}

	public void close() throws IOException {
		// can optionally be implemented by subclasses
	}

}
