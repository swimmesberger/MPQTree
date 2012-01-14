package mwt.wow.mpq;

import java.io.IOException;
import java.util.Collection;

public interface ReadMpqArchive {

	void close() throws IOException;

	MpqFile getFile(String filePath, Integer language, Integer platform)
			throws IOException;

	void readExtData() throws IOException;

	Collection<String> listFileNames();

}
