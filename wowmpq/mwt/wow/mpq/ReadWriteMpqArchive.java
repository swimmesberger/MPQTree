package mwt.wow.mpq;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface ReadWriteMpqArchive extends ReadMpqArchive {

	void saveMetadata() throws IOException;

	boolean isNew();

	void initHashtable(int size);

	boolean deleteFile(String filePath, Integer language, Integer platform);

	boolean addFile(File newFile, String filePath, int language, int platform)
			throws IOException;

	boolean addFile(String filePath, int language, int platform,
			InputStream inputStream, long fileSize) throws IOException;

}
