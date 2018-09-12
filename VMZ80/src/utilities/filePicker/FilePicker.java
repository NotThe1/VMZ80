package utilities.filePicker;

import java.io.File;
import java.nio.file.Path;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FilePicker {
	/**
	 * 
	 * @author Frank Martyn
	 *
	 *         This Class follows the design pattern for a Factory class. It generates a specialized JFileChooser, that
	 *         handles the identification of various files used by the VM application. All the file are found in the
	 *         user's directory, or path passed on the constructor, in a directory called VMdata. Virtual disks are in a
	 *         sub directory called "Disks". Each type of file is identified by its suffix.
	 * 
	 *         examples: JFileChooser fc = FilePicker.getDataPicker("Memory Image Files", "mem", "hex");
	 *         
	 *         2018-09-12 - major cleanup.
	 */

	/* Listing files */
	public static JFileChooser getListing(Path newPath) {
		pathCode = newPath.toString();
		return getListing();
	}// getListings

	public static JFileChooser getListing() {
		return getChooser(pathCode, FilterFactory.getListingFiles(), false);
	}// getListings

	public static JFileChooser getListings(Path newPath) {
		pathCode = newPath.toString();
		return getListings();
	}// getListings

	public static JFileChooser getListings() {
		return getChooser(pathCode, FilterFactory.getListingFiles(), true);
	}// getListings

	/* Disks */
	public static JFileChooser getDisk(Path newPath) {
		pathDisk = newPath.toString();
		return getDisk();
	}// getDisk

	public static JFileChooser getDisk() {
		return getChooser(pathDisk, FilterFactory.getDisk(), false);
	}// getDisk

	public static JFileChooser getDisks(Path newPath) {
		pathDisk = newPath.toString();
		return getDisks();
	}// getDisks

	public static JFileChooser getDisks() {
		return getChooser(pathDisk, FilterFactory.getDisk(), false);
	}// getDisks

	/* Memory */
	public static JFileChooser getMemory(Path newPath) {
		pathMemory = newPath.toString();
		return getMemory();
	}// getMemory

	public static JFileChooser getMemory() {
		return getChooser(pathMemory, FilterFactory.getMemory(), false);
	}// getMemory

	public static JFileChooser getMemories() {
		return getChooser(pathMemory, FilterFactory.getMemory(), true);
	}// getMemories

	/* Collections  Listings*/
	public static JFileChooser getListingCollection(Path newPath) {
		collectionsPath = newPath.toString();
		return getListingCollection();
	}// getMemory

	public static JFileChooser getListingCollection() {
		return getChooser(collectionsPath, FilterFactory.getListingCollection(), false);
	}// getMemory


	/* Collections Memory*/
	public static JFileChooser getMemoryCollection(Path newPath) {
		collectionsPath = newPath.toString();
		return getMemoryCollection();
	}// getMemory

	public static JFileChooser getMemoryCollection() {
		return getChooser(collectionsPath, FilterFactory.getMemoryCollection(), false);
	}// getMemory


	////////////////////////////////////////////////////////////////////////////////
	public FilePicker() {
	}// Constructor

	private static JFileChooser getChooser(String target, String filterDescription, String... filterExtensions) {
		FileNameExtensionFilter filter = new FileNameExtensionFilter(filterDescription, filterExtensions);
		return getChooser(target, filter, false);
	}// customiseChooser

	private static JFileChooser getChoosers(String target, String filterDescription, String... filterExtensions) {
		FileNameExtensionFilter filter = new FileNameExtensionFilter(filterDescription, filterExtensions);
		return getChooser(target, filter, true);
	}// customiseChooser

	private static JFileChooser getChooser(String target, FileNameExtensionFilter filter, boolean multiSelect) {
		if (!new File(target).exists()) {
			new File(target).mkdirs();
		} // make sure the target directory exists
		JFileChooser fileChooser = new JFileChooser(target);
		fileChooser.setMultiSelectionEnabled(multiSelect);
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		return fileChooser;
	}// customiseChooser

	// * private static final String DATA_NAME = "VMdata";
	private static final String DIR_PARENT = "Z80Work";
	// * private static final String DISK_NAME = "Disks";
	private static final String DIR_DISK = "Disks";
	// * private static final String MEMORY_NAME = "Memory";
	private static final String DIR_MEMORY = "Memory";
	// * private static final String ASM_NAME = "Asm";
	private static final String DIR_CODE = "Code";
	// private static final String Z80_NAME = "Z80";
	// * private static final String LISTS = "Lists";
	private static final String DIR_COLLECTIONS = "Collections";
	// private static final String CPM_LISTING = "cpmListings";
	// private static final String CODE_PATH = "C:\\Users\\admin\\git\\assemblerZ80\\assemblerZ80\\Code";
	//
	// public static final String LIST_ASM_SUFFIX = "ListAsm";
	// public static final String LIST_Z80_SUFFIX = "ListZ80";
	// public static final String LIST_MEM_SUFFIX = "ListMem";
	// public static final String LISTING_SUFFIX = "txt";
	public static final String COLLECTIONS_LISTING = "colListing";
	public static final String COLLECTIONS_MEMORY = "colMemory";
	//
	private static String userDirectory = System.getProperty("user.home", ".");
	private static String fileSeparator = System.getProperty("file.separator", "\\");
	private static String parentDirectory = userDirectory + fileSeparator + DIR_PARENT + fileSeparator;
	//
	// private static String dataPath = baseDirectory + DATA_NAME;
	// * private static String diskPath = baseDirectory + DISK_NAME;
	private static String pathDisk = parentDirectory + DIR_DISK;
	// * private static String memoryPath = baseDirectory + MEMORY_NAME;
	private static String pathMemory = parentDirectory + DIR_MEMORY;
	// * private static String asmPath = baseDirectory + ASM_NAME;
	private static String pathCode = parentDirectory + DIR_CODE;
	// private static String z80Path = baseDirectory + Z80_NAME;
	// * private static String listPath = baseDirectory + LISTS;
	private static String collectionsPath = parentDirectory + DIR_COLLECTIONS;

}// class FilePicker1
