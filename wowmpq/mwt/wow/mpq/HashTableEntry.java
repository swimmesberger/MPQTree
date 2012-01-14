package mwt.wow.mpq;

class HashTableEntry {

	private int filePathHashA;

	private int filePathHashB;

	private int language;

	private int platform;

	private int unknown;

	private int fileBlockIndex;

	private String filePath;

	public HashTableEntry(int filePathHashA, int filePathHashB, int language,
			int platform, int unknown, int fileBlockIndex) {
		this.filePathHashA = filePathHashA;
		this.filePathHashB = filePathHashB;
		this.language = language;
		this.platform = platform;
		this.unknown = unknown;
		this.fileBlockIndex = fileBlockIndex;
	}

	public int getFilePathHashA() {
		return filePathHashA;
	}

	public int getFilePathHashB() {
		return filePathHashB;
	}

	public int getLanguage() {
		return language;
	}

	public int getPlatform() {
		return platform;
	}

	public int getUnknown() {
		return unknown;
	}

	public int getFileBlockIndex() {
		return fileBlockIndex;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append('\n');
		s.append("File path hash A: ").append(getFilePathHashA()).append('\n');
		s.append("File path hash B: ").append(getFilePathHashB()).append('\n');
		s.append("Language: ").append(getLanguage()).append('\n');
		s.append("Platform: ").append(getPlatform()).append('\n');
		s.append("Unknown: ").append(getUnknown()).append('\n');
		s.append("File block index: ").append(getFileBlockIndex()).append('\n');
		s.append("File path: ").append(filePath).append('\n');
		return s.toString();
	}
}
