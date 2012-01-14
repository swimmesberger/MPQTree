package mwt.wow.mpq;

import java.io.IOException;
import java.io.RandomAccessFile;

class FileWriter extends Writer {

	private final RandomAccessFile file;

	private long writerPosition;

	public FileWriter(RandomAccessFile file) throws IOException {
		this.file = file;
		writerPosition = file.getFilePointer();
	}

	public FileWriter(RandomAccessFile file, long position) throws IOException {
		this.file = file;
		writerPosition = position;
		file.seek(position);
	}

	@Override
	protected void writeByte(int v) throws IOException {
		file.write(v);
		writerPosition++;
	}

	@Override
	public void writeBlock(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if ((off < 0) || (off > b.length) || (len < 0)
				|| ((off + len) > b.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return;
		}
		file.write(b, off, len);
		writerPosition += len;
	}

	@Override
	public void seek(long position) throws IOException {
		writerPosition = position;
		file.seek(position);
	}

}
