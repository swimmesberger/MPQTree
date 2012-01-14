package mwt.wow.mpq;

import java.io.EOFException;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

class DeflateReader extends Reader {

	private Reader reader;

	private Inflater inflater;

	private int compressedRemaining;

	private byte[] buffer = new byte[4096];

	public DeflateReader(Reader reader, int compressedLength)
			throws IOException {
		this.reader = reader;
		this.compressedRemaining = compressedLength;

		inflater = new Inflater();
	}

	@Override
	protected int readByte() throws IOException {
		if (inflater == null) {
			return -1;
		}
		try {
			while (inflater.inflate(buffer, 0, 1) == 0) {
				if (inflater.finished()) {
					inflater.end();
					inflater = null;
					return -1;
				} else if (inflater.needsDictionary()) {
					inflater.end();
					inflater = null;
					throw new IOException(
							"zlib preset dictionary not supported");
				} else if (inflater.needsInput()) {
					int toRead = compressedRemaining < buffer.length ? compressedRemaining
							: buffer.length;
					toRead = reader.readBlock(buffer, 0, toRead);
					if (toRead <= 0) {
						throw new EOFException("end of compressed data");
					}
					compressedRemaining -= toRead;
					inflater.setInput(buffer, 0, toRead);
				} else {
					inflater.end();
					inflater = null;
					throw new IOException("Inflater error");
				}
			}
			return buffer[0] & 0xff;
		} catch (DataFormatException e) {
			throw new IOException("inflate error: " + e.getMessage());
		}
	}

	@Override
	public int readBlock(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if (off < 0 || len < 0 || len > b.length - off) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}
		if (inflater == null) {
			return -1;
		}
		try {
			int bytesRead = 0;
			while (bytesRead < len) {
				int inflated = inflater.inflate(b, off + bytesRead, len
						- bytesRead);
				if (inflated == 0) {
					if (inflater.finished()) {
						inflater.end();
						inflater = null;
						return bytesRead == 0 ? -1 : bytesRead;
					} else if (inflater.needsDictionary()) {
						inflater.end();
						inflater = null;
						throw new IOException(
								"zlib preset dictionary not supported");
					} else if (inflater.needsInput()) {
						int toRead = compressedRemaining < buffer.length ? compressedRemaining
								: buffer.length;
						toRead = reader.readBlock(buffer, 0, toRead);
						if (toRead <= 0) {
							if (bytesRead == 0) {
								throw new EOFException("end of compressed data");
							}
							return bytesRead;
						}
						compressedRemaining -= toRead;
						inflater.setInput(buffer, 0, toRead);
					} else {
						inflater.end();
						inflater = null;
						throw new IOException("Inflater error");
					}
				} else {
					bytesRead += inflated;
				}
			}
			return bytesRead;
		} catch (DataFormatException e) {
			throw new IOException("inflate error: " + e.getMessage());
		}
	}

	@Override
	public void seek(long position) throws IOException {
		throw new IOException("Cannot seek in a compressed stream");
	}

	@Override
	public void close() throws IOException {
		if (inflater != null) {
			inflater.end();
			inflater = null;
		}
	}

}
