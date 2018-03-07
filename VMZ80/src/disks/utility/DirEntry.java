package disks.utility;

class DirEntry {
	public String fileName;
	public int directoryIndex;

	public DirEntry(String fileName, int directoryIndex) {
		this.fileName = fileName;
		this.directoryIndex = directoryIndex;
	}// constructor

	public String toString() {
		return this.fileName;
	}// toString
}// class DirEntry
