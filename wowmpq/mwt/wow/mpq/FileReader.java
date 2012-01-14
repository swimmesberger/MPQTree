package mwt.wow.mpq;

import java.io.IOException;
import java.io.RandomAccessFile;

class FileReader extends Reader {

	private final RandomAccessFile file;

	private static final int defaultBufferLength = 8 * 1024;

	private long bufferStartPosition;

	private int bufferDataLength;

	private int bufferLength;

	private byte[] buffer;

	private long readerPosition;

	public FileReader(RandomAccessFile file) throws IOException {
		this.file = file;
		readerPosition = file.getFilePointer();
	}

	public FileReader(RandomAccessFile file, long position) throws IOException {
		this.file = file;
		readerPosition = position;
		file.seek(position);
	}

	@Override
	protected int readByte() throws IOException {
		if (buffer == null) {
			startBuffer();
		}
		if (readerPosition < bufferStartPosition) {
			readBuffer();
		}
		if (readerPosition >= bufferStartPosition + bufferDataLength) {
			if (readerPosition >= file.length()) {
				return -1;
			}
			readBuffer();
		}
		int bufferIndex = (int) (readerPosition - bufferStartPosition);
		int b = buffer[bufferIndex];
		readerPosition++;
		return b & 0xff;
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
		if (buffer == null) {
			startBuffer();
		}
		if (readerPosition < bufferStartPosition) {
			readBuffer();
		}
		int bytesRead = 0;
		while (bytesRead < len) {
			if (readerPosition >= bufferStartPosition + bufferDataLength) {
				if (readerPosition >= file.length()) {
					return bytesRead == 0 ? -1 : bytesRead;
				}
				readBuffer();
			}
			int bufferIndex = (int) (readerPosition - bufferStartPosition);
			int bytesAvailable = bufferDataLength - bufferIndex;
			int remaining = len - bytesRead;
			int toRead = bytesAvailable < remaining ? bytesAvailable : remaining;
			System.arraycopy(buffer, bufferIndex, b, off + bytesRead, toRead);
			readerPosition += toRead;
			bytesRead += toRead;
		}
		return bytesRead;
	}

	private void startBuffer() throws IOException {
		bufferLength = defaultBufferLength;
		buffer = new byte[bufferLength];
		readBuffer();
	}

	private void readBuffer() throws IOException {
		bufferStartPosition = readerPosition;
		file.seek(bufferStartPosition);
		bufferDataLength = file.read(buffer);
	}

	@Override
	public void seek(long position) throws IOException {
		readerPosition = position;
	}

}
