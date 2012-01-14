package mwt.wow.mpq;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tools.bzip2.CBZip2InputStream;

class BZip2Reader extends Reader {

	private CBZip2InputStream is;

	public BZip2Reader(final Reader reader, final int compressedLength) throws IOException {
		int cB = reader.readInt8();
		int cZ = reader.readInt8();
		if (cB != 'B' || cZ != 'Z') {
			throw new IOException("No BZip2 stream found");
		}
		is = new CBZip2InputStream(new InputStream() {

			int remaining = compressedLength - 2;
			
			@Override
			public int read() throws IOException {
				if (remaining <= 0) {
					return -1;
				}
				try {
					remaining--;
					return reader.readInt8();
				} catch (EOFException e) {
					return -1;
				}
			}

		});
	}

	@Override
	protected int readByte() throws IOException {
		if (is == null) {
			return -1;
		}
		int c = is.read();
		if (c == -1) {
			close();
		}
		return c;
	}

	@Override
	public void seek(long position) throws IOException {
		throw new IOException("Cannot seek in a compressed stream");
	}

	@Override
	public void close() throws IOException {
		if (is != null) {
			is.close();
			is = null;
		}
	}

}
