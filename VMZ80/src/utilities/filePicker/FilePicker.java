package utilities.filePicker;

import java.io.File;
import java.nio.file.Path;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * 
 * @author Frank Martyn
 *
 *         This Class follows the design pattern for a Factory class. It generates a specialized JFileChooser, that
 *         handles the identification of various files used by the VM application. All the file are found in the user's
 *         directory, or path passed on the constructor, in a directory called VMdata. Virtual disks are in a sub
 *         directory called "Disks". Each type of file is identified by its suffix.
 * 
 *         examples: JFileChooser fc = FilePicker.getDataPicker("Memory Image Files", "mem", "hex");
 */

public class FilePicker {


	private FilePicker() {
		
	}// Constructor
	public static JFileChooser getAsmPicker() {
//		asmPath = Paths.get(CODE_PATH);
		return customiseChooser(asmPath,  "Listing Files ", "list");
	}// getDiskPicker customize
	

	public static JFileChooser getAsmPicker(Path newMemoryPath) {
		asmPath = newMemoryPath.toString();
		return customiseChooser(asmPath, "Listing Files ", "list");
	}// getDiskPicker customize



	public static JFileChooser getDiskPicker(String filterDescription, String... filterExtemsions) {
		return customiseChooser(diskPath, filterDescription, filterExtemsions);
	}// getDiskPicker

	public static JFileChooser getDiskPicker() {
		return customiseChooser(diskPath, "Disketts", "F3HD");
	}// getDiskPicker
	
	
	public static JFileChooser getMemoryPicker(String filterDescription, String... filterExtemsions) {
		return customiseChooser(memoryPath, filterDescription, filterExtemsions);
	}// getDiskPicker

	public static JFileChooser getMemoryPicker() {
		return customiseChooser(memoryPath, "Memory Files", "mem","hex");
	}// getDiskPicker
	
	public static JFileChooser getListAsmPicker() {
		return customiseChooser(listPath,"Listing files Lists",LIST_ASM_SUFFIX);
	}//getListAsmPicker
	
	public static JFileChooser getListZ80Picker() {
		return customiseChooser(listPath,"Listing files Lists",LIST_Z80_SUFFIX);
	}//getListAsmPicker
	
	public static JFileChooser getListMemoryPicker() {
		return customiseChooser(listPath,"Memory files Lists",LIST_MEM_SUFFIX);
	}//JFileChooser
	
	public static JFileChooser getAllListPicker() {
		return customiseChooser(listPath,"All Lists",LIST_ASM_SUFFIX,LIST_Z80_SUFFIX,LIST_MEM_SUFFIX);
	}//getAnyListPicker
	
	public static JFileChooser getZ80ListPicker() {
		return customiseChooser(CODE_PATH,"Z80 Lists","list");
	}//getAnyListPicker
	
	public static JFileChooser getZ80SourcePicker() {
		return customiseChooser(CODE_PATH,"Z80 Source",Z80_NAME);
	}//getAnyListPicker
	
	public static JFileChooser customiseChooser(String target, String filterDescription, String... filterExtensions) {
		if (!new File(target).exists()) {
			new File(target).mkdirs();
		} // make sure the target directory exists
		JFileChooser customChooser = new JFileChooser(target);
		customChooser.setMultiSelectionEnabled(false);
		customChooser.addChoosableFileFilter(new FileNameExtensionFilter(filterDescription, filterExtensions));
		customChooser.setAcceptAllFileFilterUsed(false);
		return customChooser;
	}// customiseChooser

	// private static void setTargetPaths(String subjectName) {
	//
	// }//setTargetPaths

	///////////////////////////////////////////////////////////////////////////////////////
	private static final String DATA_NAME = "VMdata";
	private static final String DISK_NAME = "Disks";
	private static final String MEMORY_NAME = "Memory";
	private static final String ASM_NAME = "Asm";
	private static final String Z80_NAME = "Z80";
	private static final String LISTS = "Lists";
	private static final String CPM_LISTING = "cpmListings";
	private static final String CODE_PATH = "C:\\Users\\admin\\git\\assemblerZ80\\assemblerZ80\\Code";

	public static final String LIST_ASM_SUFFIX = "ListAsm";
	public static final String LIST_Z80_SUFFIX = "ListZ80";
	public static final String LIST_MEM_SUFFIX = "ListMem";
	public static final String LISTING_SUFFIX = "txt";

	private static String userDirectory = System.getProperty("user.home", ".");
	private static String fileSeparator = System.getProperty("file.separator", "\\");
	private static String baseDirectory = userDirectory + fileSeparator + DATA_NAME + fileSeparator;

	private static String dataPath = baseDirectory + DATA_NAME;
	private static String diskPath = baseDirectory + DISK_NAME;
	private static String memoryPath = baseDirectory + MEMORY_NAME;
	private static String asmPath = baseDirectory + ASM_NAME;
	private static String z80Path = baseDirectory + Z80_NAME;
	private static String listPath = baseDirectory + LISTS;


	// private static Path listPath = Paths.get(System.getProperty("user.home","."),Z80_NAME);
	// private static Path listingPath = Paths.get(System.getProperty("user.home","."),Z80_NAME);

}// class FilePicker
