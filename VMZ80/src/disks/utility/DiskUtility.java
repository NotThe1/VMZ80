package disks.utility;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import codeSupport.AppLogger;
import disks.CPMDirectory;
import disks.CPMDirectoryEntry;
import disks.CPMFile;
import disks.Disk;
import disks.DiskMetrics;
import disks.RawDiskDrive;
import utilities.filePicker.FilePicker;
import utilities.hdNumberBox.HDNumberBox;
import utilities.hdNumberBox.HDNumberValueChangeEvent;
import utilities.hdNumberBox.HDNumberValueChangeListener;
import utilities.hdNumberBox.HDSeekPanel;
import utilities.hexEdit.HexEditPanelSimple;

public class DiskUtility extends JDialog {
	private static final long serialVersionUID = 1L;

	AppLogger log = AppLogger.getInstance();

	private RawDiskDrive diskDrive;
	private DiskMetrics diskMetrics;

	private File hostFile;
	private FileCpmModel fileCpmModel = new FileCpmModel();
	private DirectoryTableModel directoryTableModel = new DirectoryTableModel();
	private CPMDirectory directory;
	private CPMFile cpmFile;

	private int heads;
	private int tracksPerHead;
	private int sectorsPerTrack;
	private int bytesPerSector;
	private int tracksBeforeDirectory;
	private int blockSizeInSectors;
	private int totalTracks;
	private int totalSectors;
	private int maxDirectoryEntry;
	private int maxBlockNumber;
	private byte[] diskSector;

	private String hostDirectory;

	// private String radixFormat;

	private static DiskUtility instance = new DiskUtility();

	public static DiskUtility getInstance() {
		return instance;
	}// getInstance

	AdapterForDiskUtility adapterForDiskUtility = new AdapterForDiskUtility();
	private ArrayList<HDNumberBox> hdNumberBoxes = new ArrayList<HDNumberBox>();;

	// public static void main(String[] args) {
	// EventQueue.invokeLater(new Runnable() {
	// public void run() {
	// try {
	// DiskUtility window = new DiskUtility();
	// window.frameBase.setVisible(true);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }//try
	// }//run
	// });
	// }// main
	//
	private void doDisplayBase(AbstractButton button) {
		// selected = display Decimal
		if (button.isSelected()) {
			button.setText(TB_DISPLAY_HEX);
		} else {
			button.setText(TB_DISPLAY_DECIMAL);
		} // if
		setDisplayRadix();
	}// doDisplayBase

	// ---------------------------------------------------------
	// ---------------------------------------------------------

	private void diskSetup(String fileAbsolutePath) {
		diskDrive = new RawDiskDrive(fileAbsolutePath);
		haveDisk(true);

		// need the two display... methods run on different threads
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				displayPhysicalSector(0); // Physical View
			}
		});

		displayDirectoryView(); // Directory View
	}// diskSetup

	private void displayDirectoryView() {
		dirMakeDirectory();
		dirFillDirectoryTable();
	}// displayDirectoryView

	private void dirAdjustTableLook(JTable table) {
		Font realColumnFont = table.getFont();
		int charWidth = table.getFontMetrics(realColumnFont).getWidths()[0X57];

		TableColumnModel tableColumn = table.getColumnModel();
		tableColumn.getColumn(0).setPreferredWidth(charWidth * 3);
		tableColumn.getColumn(1).setPreferredWidth(charWidth * 9);
		tableColumn.getColumn(2).setPreferredWidth(charWidth * 4);
		tableColumn.getColumn(3).setPreferredWidth(charWidth * 3);
		tableColumn.getColumn(4).setPreferredWidth(charWidth * 4);
		tableColumn.getColumn(5).setPreferredWidth(charWidth * 4);
		tableColumn.getColumn(6).setPreferredWidth(charWidth * 3);
		tableColumn.getColumn(7).setPreferredWidth(charWidth * 3);
		tableColumn.getColumn(8).setPreferredWidth(charWidth * 3);

		DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
		rightAlign.setHorizontalAlignment(JLabel.RIGHT);
		tableColumn.getColumn(0).setCellRenderer(rightAlign);
		tableColumn.getColumn(3).setCellRenderer(rightAlign);
		tableColumn.getColumn(6).setCellRenderer(rightAlign);
		tableColumn.getColumn(7).setCellRenderer(rightAlign);
		tableColumn.getColumn(8).setCellRenderer(rightAlign);

		DefaultTableCellRenderer centerAlign = new DefaultTableCellRenderer();
		centerAlign.setHorizontalAlignment(JLabel.CENTER);
		tableColumn.getColumn(4).setCellRenderer(centerAlign);
		tableColumn.getColumn(5).setCellRenderer(centerAlign);
	}// adjustTableLook

	private void dirMakeDirectory() {
		if (directory != null) {
			directory = null;
		} // if

		directory = new CPMDirectory(diskDrive.getDiskType(), diskMetrics.isBootDisk());
		int directoryIndex = 0;
		for (int sector = diskMetrics.getDirectoryStartSector(); sector < diskMetrics.getDirectoryLastSector()
				+ 1; sector++) {
			diskDrive.setCurrentAbsoluteSector(sector);
			diskSector = diskDrive.read();
			for (int i = 0; i < diskMetrics.getDirectoryEntriesPerSector(); i++) {
				directory.addEntry(dirExtractDirectoryEntry(diskSector, i), directoryIndex++);
			} // for directory entry
		} // for each sector
	}// dirMakeDirectory

	private byte[] dirExtractDirectoryEntry(byte[] sector, int index) {
		byte[] result = new byte[Disk.DIRECTORY_ENTRY_SIZE];
		int startIndex = index * Disk.DIRECTORY_ENTRY_SIZE;
		for (int i = 0; i < Disk.DIRECTORY_ENTRY_SIZE; i++) {
			result[i] = sector[startIndex + i];
		} // for
		return result;
	}// dirExtractDirectoryEntry

	private void dirFillDirectoryTable() {
		directoryTableModel.clear();
		fileCpmModel.clear();
		CPMDirectoryEntry directoryEntry;
		for (int directoryIndex = 0; directoryIndex < diskMetrics.getDRM() + 1; directoryIndex++) {
			directoryEntry = directory.getDirectoryEntry(directoryIndex);
			directoryTableModel.addRow(directoryIndex, directoryEntry);
			if ((!directoryEntry.isEmpty()) && directoryEntry.getActualExtentNumber() == 0) {
				fileCpmModel.add(new DirEntry(directoryEntry.getNameAndTypePeriod(), directoryIndex));
				log.addInfo(directoryEntry.getNameAndTypePeriod());
			} // if
		} // for each directory entry
		showDirectoryDetail(0);
		directoryTable.setRowSelectionInterval(0, 0);
		// directoryTable.updateUI();
		if (!fileCpmModel.isEmpty()) {
			cbFileNames.setSelectedIndex(0);
			cbCPMFileInOut.setSelectedIndex(0);
		} // if no CPM files
		
		int count = fileCpmModel.getSize();
		log.addNL();
		log.addSpecial("fileCpmModel");
		for(int i = 0; i < count;i++) {
			log.addSpecial(String.format("count: %d, File: %s", i,fileCpmModel.getElementAt(i).fileName));
		}//for count
		
		 count = cbFileNames.getItemCount();
		log.addNL();
		log.addSpecial("cbFileNames");
		for(int i = 0; i < count;i++) {
			log.addSpecial(String.format("count: %d, File: %s", i,cbFileNames.getItemAt(i)));
		}//for count
		
		
		
	}// dirMakeDirectoryTable

	private void displayPhysicalSector(int absoluteSector) {
		if ((0 > absoluteSector) || (diskDrive.getTotalSectorsOnDisk() < absoluteSector)) {
			absoluteSector = 0;
		} // if - sector out of bounds

		diskDrive.setCurrentAbsoluteSector(absoluteSector);
		panelSectorDisplay.loadData(diskDrive.read());
	}// displayPhysicalSector

	private void haveDisk(boolean state) {
		lblActiveDisk.setForeground(Color.black);

		refreshMetrics(state);
		btnHostFile.setEnabled(state);

		if (!state) {
			manageFileMenus(MNU_DISK_CLOSE);
			panelSectorDisplay.loadData(NO_ACTIVE_DISK.getBytes());
			btnExport.setEnabled(false);
			btnImport.setEnabled(false);

			directoryTableModel.clear();
			showDirectoryDetail(new byte[32]);

			panelFileHex.loadData(NO_ACTIVE_FILE.getBytes());
			fileCpmModel.clear();
			// cbFileNames.setModel(new FileCpmModel());
			lblRecordCount.setText("0");
			lblReadOnly.setVisible(false);
			lblSystemFile.setVisible(false);
			hostFile = null;
			txtHostFileInOut.setText(EMPTY_STRING);
			txtHostFileInOut.setToolTipText(EMPTY_STRING);
			cbFileNames.setSelectedIndex(-1);
			cbCPMFileInOut.setSelectedIndex(-1);

		} // if - state is false

	}// haveDisk

	private void showDirectoryDetail(int entryNumber) {
		CPMDirectoryEntry entry = directory.getDirectoryEntry(entryNumber);
		byte[] rawDirectory = entry.getRawDirectory();
		showDirectoryDetail(rawDirectory);
	}// showDirectoryDetail

	private void showDirectoryDetail(byte[] rawDirectory) {
		lblRawUser.setText(String.format("%02X", rawDirectory[0]));

		lblRawName.setText(String.format("%02X %02X %02X %02X %02X %02X %02X %02X ", rawDirectory[1], rawDirectory[2],
				rawDirectory[3], rawDirectory[4], rawDirectory[5], rawDirectory[6], rawDirectory[7], rawDirectory[8]));
		lblRawType.setText(String.format("%02X %02X %02X", rawDirectory[9], rawDirectory[10], rawDirectory[11]));
		lblRawEX.setText(String.format("%02X", rawDirectory[12]));
		lblRawS1.setText(String.format("%02X", rawDirectory[13]));
		lblRawS2.setText(String.format("%02X", rawDirectory[14]));

		lblRawRC.setText(String.format("%02X", rawDirectory[15]));

		lblRawAllocation.setText(String.format(
				"%02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X %02X", rawDirectory[16],
				rawDirectory[17], rawDirectory[18], rawDirectory[19], rawDirectory[20], rawDirectory[21],
				rawDirectory[22], rawDirectory[23], rawDirectory[24], rawDirectory[25], rawDirectory[26],
				rawDirectory[27], rawDirectory[28], rawDirectory[29], rawDirectory[30], rawDirectory[31]));

	}// showDirectoryDetail

	private void refreshMetrics(boolean state) {

		// Modified original .. if(diskMetrics!= null......
		if (diskMetrics != null) {
			diskMetrics = null;
		} //

		diskMetrics = state ? DiskMetrics.getDiskMetric(diskDrive.getDiskType()) : null;

		setHeadTrackSectorSize(diskDrive);

		heads = state ? diskMetrics.heads : 0;
		tracksPerHead = state ? diskMetrics.tracksPerHead : 0;
		sectorsPerTrack = state ? diskMetrics.sectorsPerTrack : 0;
		bytesPerSector = state ? diskMetrics.bytesPerSector : 0;
		totalTracks = state ? heads * tracksPerHead : 0;
		totalSectors = state ? diskMetrics.getTotalSectorsOnDisk() : 0;

		tracksBeforeDirectory = state ? diskMetrics.getOFS() : 0;
		blockSizeInSectors = state ? diskMetrics.sectorsPerBlock : 0;
		maxDirectoryEntry = state ? diskMetrics.getDRM() : 0;
		maxBlockNumber = state ? diskMetrics.getDSM() : 0;

		lblActiveDisk.setText(state ? diskDrive.getFileLocalName() : NO_ACTIVE_DISK);
		lblActiveDisk.setToolTipText(state ? diskDrive.getFileAbsoluteName() : NO_ACTIVE_DISK);

		setDisplayRadix();
	}// refreshMetrics

	private void setDisplayRadix() {
		String radixFormat = getRadixFormat();

		lblHeads.setText(String.format(radixFormat, heads));
		lblTracksPerHead.setText(String.format(radixFormat, tracksPerHead));
		lblSectorsPerTrack.setText(String.format(radixFormat, sectorsPerTrack));
		lblTotalTracks.setText(String.format(radixFormat, totalTracks));
		lblTotalSectors.setText(String.format(radixFormat, totalSectors));

		lblTracksBeforeDirectory.setText(String.format(radixFormat, tracksBeforeDirectory));
		lblLogicalBlockSizeInSectors.setText(String.format(radixFormat, blockSizeInSectors));
		lblMaxDirectoryEntry.setText(String.format(radixFormat, maxDirectoryEntry));
		lblMaxBlockNumber.setText(String.format(radixFormat, maxBlockNumber));

		for (HDNumberBox hdNumberBox : hdNumberBoxes) {
			hdNumberBox.setDecimalDisplay(tbDisplayBase.isSelected());
		} // for

	}// setDisplayRadix

	private String getRadixFormat() {
		return tbDisplayBase.isSelected() ? "%,d" : "%X"; // selected = decimal
	}// setRadixFormat

	private void setHeadTrackSectorSize(RawDiskDrive diskDrive) {
		((SpinnerNumberModel) hdnHead.getNumberModel()).setValue(0);
		((SpinnerNumberModel) hdnTrack.getNumberModel()).setValue(0);
		((SpinnerNumberModel) hdnSector.getNumberModel()).setValue(1);

		// hdnSeekPanel.mute(true);
		// hdnSeekPanel.setValue(0);
		// hdnSeekPanel.mute(false);

		hdnSeekPanel.setValueQuiet(0);

		if (diskDrive == null) {
			((SpinnerNumberModel) hdnHead.getNumberModel()).setMaximum(0);
			((SpinnerNumberModel) hdnTrack.getNumberModel()).setMaximum(0);
			((SpinnerNumberModel) hdnSector.getNumberModel()).setMaximum(1);
			((SpinnerNumberModel) hdnSector.getNumberModel()).setMinimum(1);
			hdnSeekPanel.setMaxValue(0);

		} else {
			((SpinnerNumberModel) hdnHead.getNumberModel()).setMaximum(diskDrive.getHeads() - 1);
			((SpinnerNumberModel) hdnTrack.getNumberModel()).setMaximum(diskDrive.getTracksPerHead() - 1);
			((SpinnerNumberModel) hdnSector.getNumberModel()).setMaximum(diskDrive.getSectorsPerTrack());
			hdnSeekPanel.setMaxValue(diskDrive.getTotalSectorsOnDisk() - 1);
		} // if
		hdnSeekPanel.setMinValue(0);

	}// setHeadTrackSectorSize

	private void selectedNewPhysicalSector(boolean fromSeekPanel) {
		if (panelSectorDisplay.isDataChanged()) {
			diskDrive.write(panelSectorDisplay.unloadData());
		} // if dirty

		int newSector = hdnSeekPanel.getValue();

		if (fromSeekPanel) {
			diskDrive.setCurrentAbsoluteSector(newSector);
			hdnHead.setValueQuiet(diskDrive.getCurrentHead());
			hdnTrack.setValueQuiet(diskDrive.getCurrentTrack());
			hdnSector.setValueQuiet(diskDrive.getCurrentSector());
		} else {
			diskDrive.setCurrentAbsoluteSector(hdnHead.getValue(), hdnTrack.getValue(), hdnSector.getValue());
			newSector = diskDrive.getCurrentAbsoluteSector();
			hdnSeekPanel.setValueQuiet(newSector);
		} // if from seekPanel or head/track/sector spinners

		displayPhysicalSector(newSector);

	}// selectedNewPhysicalSector

	private void manageFileMenus(String source) {
		switch (source) {
		case MNU_TOOLS_NEW:
		case MNU_DISK_LOAD:
			mnuToolsNew.setEnabled(false);
			mnuDiskLoad.setEnabled(false);
			mnuDiskClose.setEnabled(true);
			mnuDiskSave.setEnabled(true);
			mnuDiskSaveAs.setEnabled(true);
			// mnuDiskExit.setEnabled(true);
			break;
		case MNU_DISK_CLOSE:
			mnuToolsNew.setEnabled(true);
			mnuDiskLoad.setEnabled(true);
			mnuDiskClose.setEnabled(false);
			mnuDiskSave.setEnabled(false);
			mnuDiskSaveAs.setEnabled(false);
			// mnuDiskExit.setEnabled(true);
		case MNU_DISK_SAVE:
		case MNU_DISK_SAVE_AS:
		case MNU_DISK_EXIT:
			break;
		default:
		}// switch
	}// manageFileMenus
		// ---------------------------------------------------------

	private void doDiskLoad() {
		JFileChooser fc = FilePicker.getDiskPicker();
		if (fc.showOpenDialog(this) == JFileChooser.CANCEL_OPTION) {
			log.addInfo("Bailed out of disk open");
			return;
		} // if - cancelled
		String absoluteFilePath = fc.getSelectedFile().getAbsolutePath();
		if (!fc.getSelectedFile().exists()) {
			log.addError(absoluteFilePath + " Does Not Exist");
			return;
		} // if - is it there

		diskSetup(absoluteFilePath);
		manageFileMenus(MNU_DISK_LOAD);

	}// doFileNew

	private void doDiskClose() {
		if (diskDrive != null) {
			diskDrive.dismount();
			diskDrive = null;
		} // if - have diskDrive
		haveDisk(false);
	}// doFileOpen

	private void doDiskSave() {
		System.out.println("** [doDiskSave] **");
	}// doFileSave

	private void doDiskSaveAs() {
		System.out.println("** [doDiskSaveAs] **");
		JFileChooser fc = FilePicker.getDiskPicker();
		if (fc.showOpenDialog(this) == JFileChooser.CANCEL_OPTION) {
			log.addInfo("Bailed out of disk open");
			return;
		} // if - cancelled
		File targetFile = fc.getSelectedFile();

		String targetFileName = targetFile.getName();
		targetFileName = cleanupFileName(targetFileName, ".F3HD");
		String sourceFileName = diskDrive.getFileAbsoluteName();
		if (targetFile.exists()) {
			if (JOptionPane.showConfirmDialog(null, "File Exits, Do you want to overwrite?", "Disk Save As...",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
				return;
			} // if
		} // if does file exists

		doDiskClose();
		try {
			Files.copy(Paths.get(sourceFileName), Paths.get(targetFileName), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			log.addError("Failed to copy from " + sourceFileName + " to " + targetFileName);
		} // try
		diskSetup(targetFileName);
		manageFileMenus(MNU_DISK_LOAD);

	}// doFileSaveAs

	private String cleanupFileName(String nameIn, String targetType) {
		// String type = ".F3HD";
		String nameOut;
		Pattern fileType = Pattern.compile("(\\..+$){1}+");
		Matcher matcher = fileType.matcher(nameIn);

		if (matcher.find()) {
			nameOut = matcher.replaceFirst(targetType);
		} else {
			nameOut = (nameIn + targetType);
		} // if
		return nameOut.trim();
	}// cleanupFileName

	private void doDiskExit() {
		appClose();
		System.exit(0);
	}// doFileExit

	private void doDisplaySelectedFile() {
		if (panelFileHex.isDataChanged()) {
			byte[] changedData = panelFileHex.unloadData();
			cpmFile.write(changedData);
			lblActiveDisk.setForeground(Color.RED);
		} // if dirty file

		if (cbFileNames.getItemCount() == 0) {
			return;
		} // if empty

		DirEntry de = (DirEntry) cbFileNames.getSelectedItem();
		String fileName = de.fileName;
		cpmFile = CPMFile.getCPMFile(diskDrive, directory, fileName);
		lblRecordCount.setText(String.format(getRadixFormat(), cpmFile.getRecordCount()));
		lblReadOnly.setVisible(cpmFile.isReadOnly());
		lblSystemFile.setVisible(cpmFile.isSystemFile());

		byte[] dataToDisplay = cpmFile.getRecordCount() == 0 ? NO_ACTIVE_FILE.getBytes() : cpmFile.readRaw();
		panelFileHex.loadData(dataToDisplay);

	}// doDisplaySelectedFile

	private void doToolsNew() {
		System.out.println("DiskUtility.doToolsNew()");
		File newFile = MakeNewDisk.makeNewDisk();
		if (newFile == null) {
			log.addError("Failed to make a new Disk");
			return;
		} // if no new disk

		diskSetup(newFile.getAbsolutePath());
		manageFileMenus(MNU_TOOLS_NEW);
	}// doToolsNew

	private void doToolsUpdate() {
		if (diskDrive == null) {
			UpdateSystemDisk.updateDisks();
		} else {
			String absoluteFilePath = diskDrive.getFileAbsoluteName();
			doDiskClose();
			UpdateSystemDisk.updateDisk(absoluteFilePath);
			diskSetup(absoluteFilePath);
			manageFileMenus(MNU_DISK_LOAD);
		} // if
	}// doToolsUpdate

	private void doGetHostFile() {
		JFileChooser nativeChooser = new JFileChooser(hostDirectory);
		nativeChooser.setMultiSelectionEnabled(false);

		if (nativeChooser.showDialog(this, "Select the file") != JFileChooser.APPROVE_OPTION) {
			btnImport.setEnabled(false);
			btnExport.setEnabled(false);
			txtHostFileInOut.setText(EMPTY_STRING);
			txtHostFileInOut.setToolTipText(EMPTY_STRING);
		} else {
			hostFile = nativeChooser.getSelectedFile();
			btnImport.setEnabled(true);
			btnExport.setEnabled(true);

			hostDirectory = hostFile.getParent();
			txtHostFileInOut.setText(hostFile.getName());
			txtHostFileInOut.setToolTipText(hostFile.getAbsolutePath());
		} // if file not chosen
	}// doGetHostFile

	private void doExport() {

		if (cbCPMFileInOut.getItemCount() == 0) {
			return;
		} // if empty list
		DirEntry dirEntry = (DirEntry) cbCPMFileInOut.getSelectedItem();
		String fileName = dirEntry.fileName;

		// if(!fileCpmModel.exists(fileName)) {
		// cbCPMFileInOut.setSelectedItem(EMPTY_STRING);
		// }// if not really there
		// cbCPMFileInOut.setSelectedItem(fileName);

		cpmFile = CPMFile.getCPMFile(diskDrive, directory, fileName);

		ByteArrayInputStream bis = new ByteArrayInputStream(cpmFile.readNet());

		try {
			Files.copy(bis, Paths.get(hostFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException ioe) {
			String message = String.format("Exported %s to %s", fileName, hostFile.getAbsolutePath());
			log.addInfo(message);
		} // try

	}// doExport

	private void doImport() {
		System.out.println("DiskUtility.()");

		boolean deleteFile = false;
		DirEntry dirEntry = (DirEntry) cbCPMFileInOut.getSelectedItem();
		String cpmFileName = dirEntry.fileName;

		if (fileCpmModel.exists(cpmFileName)) {
			this.getContentPane();
			if (JOptionPane.showConfirmDialog((Component) this, "File Exits, Do you want to overwrite?",
					"Copying a Native File to a CPM file", JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION) {
				return;
			} // if
			deleteFile = true;
		} // if file exists

		// Do we have enough space on the CP/M disk to do this?
		if (!enoughSpaceOnCPM(deleteFile, cpmFileName, hostFile)) {
			return;
		} // if - enough space

		if (deleteFile) {
			directory.deleteFile(cpmFileName);
		} // if delete

		/*
		 * We have all the pieces needed to actually move the file. We need to get a directory entry and some storage
		 * for the file
		 */
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			Files.copy(Paths.get(hostFile.getAbsolutePath()), bos);
		} catch (Exception e) {
			log.addError("Failed to read file: " + hostFile.getAbsolutePath());
			return;
		}// try
		
		byte[] dataToWrite = bos.toByteArray();
		
		CPMFile newCPMFile = CPMFile.createCPMFile(diskDrive, directory, cpmFileName);
		newCPMFile.writeNewFile(dataToWrite);
		
		//need the two display methods run on separate threads
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
//				displayPhysicalSector(0);
			}
		});
		displayDirectoryView();

		
//		javax.swing.SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
//				displayDirectoryView();
//			}
//		});
		
//		String diskName = diskDrive.getFileAbsoluteName();
//		doDiskClose();
//		diskSetup(diskName);
//		manageFileMenus(MNU_DISK_LOAD);	
		//need the two display methods run on separate threads

	}// doImport

	private boolean enoughSpaceOnCPM(boolean deleteFlag, String cpmFileName, File sourceFile) {
		boolean result;
		int blocksNeeded = (int) Math.ceil(sourceFile.length() / (float) diskMetrics.getBytesPerBlock());
		int availableBlocks = directory.getAvailableBlockCount();
		availableBlocks = deleteFlag ? availableBlocks + directory.getFileBlocksCount(cpmFileName) : availableBlocks;

		if (availableBlocks > blocksNeeded) {
			result = true;
		} else {
			String msg = String.format("Not enough space on CPM disk%n" + "Blocks Available: %,d -Blocks  Need: %,d",
					availableBlocks, blocksNeeded);
			JOptionPane.showMessageDialog((Component) this, msg, "Copying a host file to CPM",
					JOptionPane.WARNING_MESSAGE);
			result = false;
		} // if
		return result;
	}// enoughSpaceOnCPM

	////////////////////////////////////////////////////////////////////////////////////////
	private void appClose() {
		if (diskDrive != null) {
			diskDrive.dismount();
			diskDrive = null;
		} // if - have diskDrive

		Preferences myPrefs = Preferences.userNodeForPackage(DiskUtility.class).node(this.getClass().getSimpleName());
		Dimension dim = this.getSize();
		myPrefs.putInt("Height", dim.height);
		myPrefs.putInt("Width", dim.width);
		Point point = this.getLocation();
		myPrefs.putInt("LocX", point.x);
		myPrefs.putInt("LocY", point.y);

		myPrefs.put("HostDirectory", hostDirectory);
		myPrefs.putInt("Tab", tabbedPane.getSelectedIndex());
		myPrefs = null;
	}// appClose

	private void appInit() {
		Preferences myPrefs = Preferences.userNodeForPackage(DiskUtility.class).node(this.getClass().getSimpleName());
		this.setSize(myPrefs.getInt("Width", 761), myPrefs.getInt("Height", 693));
		this.setLocation(myPrefs.getInt("LocX", 100), myPrefs.getInt("LocY", 100));

		hostDirectory = myPrefs.get("HostDirectory", System.getProperty(USER_HOME, THIS_DIR));
		tabbedPane.setSelectedIndex(myPrefs.getInt("Tab", 0));
		myPrefs = null;

		hdNumberBoxes.add(hdnHead);
		hdNumberBoxes.add(hdnTrack);
		hdNumberBoxes.add(hdnSector);
		hdNumberBoxes.add(hdnSeekPanel);

		cbFileNames.setModel(fileCpmModel);
		cbCPMFileInOut.setModel(fileCpmModel);
		directoryTable.setModel(directoryTableModel);
		dirAdjustTableLook(directoryTable);

		haveDisk(false);
		manageFileMenus(MNU_DISK_CLOSE);

	}// appInit

	/**
	 * Launch the application.
	 */

	private DiskUtility() {
		initialize();
		appInit();
	}// Constructor

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		this.setTitle("DiskUtility   A.0");
		this.setBounds(100, 100, 655, 626);
		// frameBase.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				appClose();
			}
		});
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 25, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		this.getContentPane().setLayout(gridBagLayout);

		JPanel topPanel = new JPanel();
		GridBagConstraints gbc_topPanel = new GridBagConstraints();
		gbc_topPanel.anchor = GridBagConstraints.WEST;
		gbc_topPanel.insets = new Insets(0, 0, 5, 0);
		gbc_topPanel.fill = GridBagConstraints.VERTICAL;
		gbc_topPanel.gridx = 0;
		gbc_topPanel.gridy = 0;
		this.getContentPane().add(topPanel, gbc_topPanel);
		GridBagLayout gbl_topPanel = new GridBagLayout();
		gbl_topPanel.columnWidths = new int[] { 0, 90, 0, 90, 0 };
		gbl_topPanel.rowHeights = new int[] { 0, 0 };
		gbl_topPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_topPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		topPanel.setLayout(gbl_topPanel);

		JToolBar toolBar = new JToolBar();
		GridBagConstraints gbc_toolBar = new GridBagConstraints();
		gbc_toolBar.anchor = GridBagConstraints.WEST;
		gbc_toolBar.insets = new Insets(0, 0, 0, 5);
		gbc_toolBar.gridx = 1;
		gbc_toolBar.gridy = 0;
		topPanel.add(toolBar, gbc_toolBar);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		toolBar.add(horizontalStrut);
		horizontalStrut.setMinimumSize(new Dimension(50, 0));

		tbDisplayBase = new JToggleButton(TB_DISPLAY_DECIMAL);
		tbDisplayBase.setName(TB_DISPLAY_BASE);
		tbDisplayBase.addActionListener(adapterForDiskUtility);
		toolBar.add(tbDisplayBase);
		tbDisplayBase.setPreferredSize(new Dimension(120, 23));

		lblActiveDisk = new JLabel(NO_ACTIVE_DISK);
		lblActiveDisk.setFont(new Font("Arial", Font.BOLD, 18));
		GridBagConstraints gbc_lblActiveDisk = new GridBagConstraints();
		gbc_lblActiveDisk.insets = new Insets(0, 0, 5, 0);
		gbc_lblActiveDisk.gridx = 0;
		gbc_lblActiveDisk.gridy = 1;
		this.getContentPane().add(lblActiveDisk, gbc_lblActiveDisk);

		mainPanel = new JPanel();
		GridBagConstraints gbc_mainPanel = new GridBagConstraints();
		gbc_mainPanel.insets = new Insets(0, 0, 5, 0);
		gbc_mainPanel.fill = GridBagConstraints.BOTH;
		gbc_mainPanel.gridx = 0;
		gbc_mainPanel.gridy = 2;
		this.getContentPane().add(mainPanel, gbc_mainPanel);
		mainPanel.setLayout(new GridLayout(1, 0, 0, 0));

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		mainPanel.add(tabbedPane);

		JPanel tabDirectory = new JPanel();
		tabbedPane.addTab("Directory View", null, tabDirectory, null);
		GridBagLayout gbl_tabDirectory = new GridBagLayout();
		gbl_tabDirectory.columnWidths = new int[] { 0, 0 };
		gbl_tabDirectory.rowHeights = new int[] { 0, 0, 0 };
		gbl_tabDirectory.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_tabDirectory.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		tabDirectory.setLayout(gbl_tabDirectory);

		JPanel panelDirectoryEntry = new JPanel();
		GridBagConstraints gbc_panelDirectoryEntry = new GridBagConstraints();
		gbc_panelDirectoryEntry.insets = new Insets(0, 0, 5, 0);
		gbc_panelDirectoryEntry.fill = GridBagConstraints.VERTICAL;
		gbc_panelDirectoryEntry.gridx = 0;
		gbc_panelDirectoryEntry.gridy = 0;
		tabDirectory.add(panelDirectoryEntry, gbc_panelDirectoryEntry);
		GridBagLayout gbl_panelDirectoryEntry = new GridBagLayout();
		gbl_panelDirectoryEntry.columnWidths = new int[] { 0, 0 };
		gbl_panelDirectoryEntry.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelDirectoryEntry.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelDirectoryEntry.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelDirectoryEntry.setLayout(gbl_panelDirectoryEntry);

		JPanel panelRawDirectory = new JPanel();
		GridBagConstraints gbc_panelRawDirectory = new GridBagConstraints();
		gbc_panelRawDirectory.insets = new Insets(0, 0, 5, 0);
		gbc_panelRawDirectory.fill = GridBagConstraints.VERTICAL;
		gbc_panelRawDirectory.gridx = 0;
		gbc_panelRawDirectory.gridy = 0;
		panelDirectoryEntry.add(panelRawDirectory, gbc_panelRawDirectory);
		GridBagLayout gbl_panelRawDirectory = new GridBagLayout();
		gbl_panelRawDirectory.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelRawDirectory.rowHeights = new int[] { 0, 0 };
		gbl_panelRawDirectory.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelRawDirectory.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelRawDirectory.setLayout(gbl_panelRawDirectory);

		JPanel panelRawUser = new JPanel();
		panelRawUser.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panelRawUser = new GridBagConstraints();
		gbc_panelRawUser.insets = new Insets(0, 0, 0, 5);
		gbc_panelRawUser.anchor = GridBagConstraints.NORTH;
		gbc_panelRawUser.fill = GridBagConstraints.BOTH;
		gbc_panelRawUser.gridx = 0;
		gbc_panelRawUser.gridy = 0;
		panelRawDirectory.add(panelRawUser, gbc_panelRawUser);
		GridBagLayout gbl_panelRawUser = new GridBagLayout();
		gbl_panelRawUser.columnWidths = new int[] { 0, 0 };
		gbl_panelRawUser.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelRawUser.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelRawUser.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelRawUser.setLayout(gbl_panelRawUser);

		JLabel label5 = new JLabel("User[0]");
		label5.setFont(new Font("Arial", Font.PLAIN, 12));
		GridBagConstraints gbc_label5 = new GridBagConstraints();
		gbc_label5.insets = new Insets(0, 0, 5, 0);
		gbc_label5.gridx = 0;
		gbc_label5.gridy = 0;
		panelRawUser.add(label5, gbc_label5);

		lblRawUser = new JLabel("00");
		lblRawUser.setForeground(Color.BLUE);
		lblRawUser.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblRawUser = new GridBagConstraints();
		gbc_lblRawUser.gridx = 0;
		gbc_lblRawUser.gridy = 1;
		panelRawUser.add(lblRawUser, gbc_lblRawUser);

		JPanel panelRawName = new JPanel();
		panelRawName.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panelRawName = new GridBagConstraints();
		gbc_panelRawName.insets = new Insets(0, 0, 0, 5);
		gbc_panelRawName.fill = GridBagConstraints.BOTH;
		gbc_panelRawName.gridx = 1;
		gbc_panelRawName.gridy = 0;
		panelRawDirectory.add(panelRawName, gbc_panelRawName);
		GridBagLayout gbl_panelRawName = new GridBagLayout();
		gbl_panelRawName.columnWidths = new int[] { 0, 0 };
		gbl_panelRawName.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelRawName.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelRawName.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelRawName.setLayout(gbl_panelRawName);

		JLabel label1 = new JLabel("Name[1-8]");
		label1.setFont(new Font("Arial", Font.PLAIN, 12));
		GridBagConstraints gbc_label1 = new GridBagConstraints();
		gbc_label1.insets = new Insets(0, 0, 5, 0);
		gbc_label1.gridx = 0;
		gbc_label1.gridy = 0;
		panelRawName.add(label1, gbc_label1);

		lblRawName = new JLabel("00 00 00 00 00 00 00 00");
		lblRawName.setForeground(Color.BLUE);
		lblRawName.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblRawName = new GridBagConstraints();
		gbc_lblRawName.anchor = GridBagConstraints.NORTH;
		gbc_lblRawName.gridx = 0;
		gbc_lblRawName.gridy = 1;
		panelRawName.add(lblRawName, gbc_lblRawName);

		JPanel panelRawType = new JPanel();
		panelRawType.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panelRawType = new GridBagConstraints();
		gbc_panelRawType.insets = new Insets(0, 0, 0, 5);
		gbc_panelRawType.fill = GridBagConstraints.BOTH;
		gbc_panelRawType.gridx = 2;
		gbc_panelRawType.gridy = 0;
		panelRawDirectory.add(panelRawType, gbc_panelRawType);
		GridBagLayout gbl_panelRawType = new GridBagLayout();
		gbl_panelRawType.columnWidths = new int[] { 0, 0 };
		gbl_panelRawType.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelRawType.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelRawType.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelRawType.setLayout(gbl_panelRawType);

		JLabel label2 = new JLabel("Type[9-11]");
		label2.setFont(new Font("Arial", Font.PLAIN, 12));
		GridBagConstraints gbc_label2 = new GridBagConstraints();
		gbc_label2.insets = new Insets(0, 0, 5, 0);
		gbc_label2.gridx = 0;
		gbc_label2.gridy = 0;
		panelRawType.add(label2, gbc_label2);

		lblRawType = new JLabel("00 00 00");
		lblRawType.setForeground(Color.BLUE);
		lblRawType.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblRawType = new GridBagConstraints();
		gbc_lblRawType.anchor = GridBagConstraints.NORTH;
		gbc_lblRawType.gridx = 0;
		gbc_lblRawType.gridy = 1;
		panelRawType.add(lblRawType, gbc_lblRawType);

		JPanel panelEX = new JPanel();
		panelEX.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panelEX = new GridBagConstraints();
		gbc_panelEX.insets = new Insets(0, 0, 0, 5);
		gbc_panelEX.fill = GridBagConstraints.BOTH;
		gbc_panelEX.gridx = 3;
		gbc_panelEX.gridy = 0;
		panelRawDirectory.add(panelEX, gbc_panelEX);
		GridBagLayout gbl_panelEX = new GridBagLayout();
		gbl_panelEX.columnWidths = new int[] { 0, 0 };
		gbl_panelEX.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelEX.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelEX.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelEX.setLayout(gbl_panelEX);

		JLabel label3 = new JLabel("EX[12]");
		label3.setFont(new Font("Arial", Font.PLAIN, 12));
		GridBagConstraints gbc_label3 = new GridBagConstraints();
		gbc_label3.insets = new Insets(0, 0, 5, 0);
		gbc_label3.gridx = 0;
		gbc_label3.gridy = 0;
		panelEX.add(label3, gbc_label3);

		lblRawEX = new JLabel("00");
		lblRawEX.setForeground(Color.BLUE);
		lblRawEX.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblRawEX = new GridBagConstraints();
		gbc_lblRawEX.anchor = GridBagConstraints.NORTH;
		gbc_lblRawEX.gridx = 0;
		gbc_lblRawEX.gridy = 1;
		panelEX.add(lblRawEX, gbc_lblRawEX);

		JPanel panelS1 = new JPanel();
		panelS1.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panelS1 = new GridBagConstraints();
		gbc_panelS1.insets = new Insets(0, 0, 0, 5);
		gbc_panelS1.fill = GridBagConstraints.BOTH;
		gbc_panelS1.gridx = 4;
		gbc_panelS1.gridy = 0;
		panelRawDirectory.add(panelS1, gbc_panelS1);
		GridBagLayout gbl_panelS1 = new GridBagLayout();
		gbl_panelS1.columnWidths = new int[] { 0, 0 };
		gbl_panelS1.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelS1.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelS1.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelS1.setLayout(gbl_panelS1);

		JLabel lblS = new JLabel("S1[13]");
		lblS.setFont(new Font("Arial", Font.PLAIN, 12));
		GridBagConstraints gbc_lblS = new GridBagConstraints();
		gbc_lblS.insets = new Insets(0, 0, 5, 0);
		gbc_lblS.gridx = 0;
		gbc_lblS.gridy = 0;
		panelS1.add(lblS, gbc_lblS);

		lblRawS1 = new JLabel("00");
		lblRawS1.setForeground(Color.BLUE);
		lblRawS1.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblRawS1 = new GridBagConstraints();
		gbc_lblRawS1.anchor = GridBagConstraints.NORTH;
		gbc_lblRawS1.gridx = 0;
		gbc_lblRawS1.gridy = 1;
		panelS1.add(lblRawS1, gbc_lblRawS1);

		JPanel panelS2 = new JPanel();
		panelS2.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panelS2 = new GridBagConstraints();
		gbc_panelS2.insets = new Insets(0, 0, 0, 5);
		gbc_panelS2.fill = GridBagConstraints.BOTH;
		gbc_panelS2.gridx = 5;
		gbc_panelS2.gridy = 0;
		panelRawDirectory.add(panelS2, gbc_panelS2);
		GridBagLayout gbl_panelS2 = new GridBagLayout();
		gbl_panelS2.columnWidths = new int[] { 0, 0 };
		gbl_panelS2.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelS2.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelS2.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelS2.setLayout(gbl_panelS2);

		JLabel label6 = new JLabel("S2[14]");
		label6.setFont(new Font("Arial", Font.PLAIN, 12));
		GridBagConstraints gbc_label6 = new GridBagConstraints();
		gbc_label6.insets = new Insets(0, 0, 5, 0);
		gbc_label6.gridx = 0;
		gbc_label6.gridy = 0;
		panelS2.add(label6, gbc_label6);

		lblRawS2 = new JLabel("00");
		lblRawS2.setForeground(Color.BLUE);
		lblRawS2.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblRawS2 = new GridBagConstraints();
		gbc_lblRawS2.anchor = GridBagConstraints.NORTH;
		gbc_lblRawS2.gridx = 0;
		gbc_lblRawS2.gridy = 1;
		panelS2.add(lblRawS2, gbc_lblRawS2);

		JPanel panelRC = new JPanel();
		panelRC.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panelRC = new GridBagConstraints();
		gbc_panelRC.fill = GridBagConstraints.BOTH;
		gbc_panelRC.gridx = 6;
		gbc_panelRC.gridy = 0;
		panelRawDirectory.add(panelRC, gbc_panelRC);
		GridBagLayout gbl_panelRC = new GridBagLayout();
		gbl_panelRC.columnWidths = new int[] { 0, 0 };
		gbl_panelRC.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelRC.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelRC.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelRC.setLayout(gbl_panelRC);

		JLabel label7 = new JLabel("RC[15]");
		label7.setFont(new Font("Arial", Font.PLAIN, 12));
		GridBagConstraints gbc_label7 = new GridBagConstraints();
		gbc_label7.insets = new Insets(0, 0, 5, 0);
		gbc_label7.gridx = 0;
		gbc_label7.gridy = 0;
		panelRC.add(label7, gbc_label7);

		lblRawRC = new JLabel("00");
		lblRawRC.setForeground(Color.BLUE);
		lblRawRC.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblRawRC = new GridBagConstraints();
		gbc_lblRawRC.anchor = GridBagConstraints.NORTH;
		gbc_lblRawRC.gridx = 0;
		gbc_lblRawRC.gridy = 1;
		panelRC.add(lblRawRC, gbc_lblRawRC);

		JPanel panelAllocationVector = new JPanel();
		panelAllocationVector.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagConstraints gbc_panelAllocationVector = new GridBagConstraints();
		gbc_panelAllocationVector.anchor = GridBagConstraints.NORTH;
		gbc_panelAllocationVector.gridx = 0;
		gbc_panelAllocationVector.gridy = 1;
		panelDirectoryEntry.add(panelAllocationVector, gbc_panelAllocationVector);
		GridBagLayout gbl_panelAllocationVector = new GridBagLayout();
		gbl_panelAllocationVector.columnWidths = new int[] { 0, 0 };
		gbl_panelAllocationVector.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelAllocationVector.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelAllocationVector.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelAllocationVector.setLayout(gbl_panelAllocationVector);

		JLabel label8 = new JLabel("Allocation Vector[16-31]");
		label8.setFont(new Font("Arial", Font.PLAIN, 12));
		GridBagConstraints gbc_label8 = new GridBagConstraints();
		gbc_label8.insets = new Insets(0, 0, 5, 0);
		gbc_label8.gridx = 0;
		gbc_label8.gridy = 0;
		panelAllocationVector.add(label8, gbc_label8);

		lblRawAllocation = new JLabel("00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00");
		lblRawAllocation.setForeground(Color.BLUE);
		lblRawAllocation.setFont(new Font("Courier New", Font.BOLD, 15));
		GridBagConstraints gbc_lblRawAllocation = new GridBagConstraints();
		gbc_lblRawAllocation.gridx = 0;
		gbc_lblRawAllocation.gridy = 1;
		panelAllocationVector.add(lblRawAllocation, gbc_lblRawAllocation);

		scrollDirectoryTable = new JScrollPane();
		scrollDirectoryTable.setPreferredSize(new Dimension(531, 0));
		GridBagConstraints gbc_scrollDirectoryTable = new GridBagConstraints();
		gbc_scrollDirectoryTable.fill = GridBagConstraints.VERTICAL;
		gbc_scrollDirectoryTable.gridx = 0;
		gbc_scrollDirectoryTable.gridy = 1;
		tabDirectory.add(scrollDirectoryTable, gbc_scrollDirectoryTable);

		directoryTable = new JTable();
		directoryTable.setBorder(new LineBorder(Color.RED));
		directoryTable.setName(TABLE_DIRECTORY);
		;
		directoryTable.getSelectionModel().addListSelectionListener(adapterForDiskUtility);
		directoryTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		directoryTable.setFont(new Font("Tahoma", Font.PLAIN, 14));
		scrollDirectoryTable.setViewportView(directoryTable);

		JPanel tabFile = new JPanel();
		tabbedPane.addTab("File View", null, tabFile, null);
		GridBagLayout gbl_tabFile = new GridBagLayout();
		gbl_tabFile.columnWidths = new int[] { 0, 0 };
		gbl_tabFile.rowHeights = new int[] { 0, 0, 0 };
		gbl_tabFile.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_tabFile.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		tabFile.setLayout(gbl_tabFile);

		JPanel panelFileSelection = new JPanel();
		GridBagConstraints gbc_panelFileSelection = new GridBagConstraints();
		gbc_panelFileSelection.insets = new Insets(0, 0, 5, 0);
		gbc_panelFileSelection.fill = GridBagConstraints.VERTICAL;
		gbc_panelFileSelection.gridx = 0;
		gbc_panelFileSelection.gridy = 0;
		tabFile.add(panelFileSelection, gbc_panelFileSelection);
		GridBagLayout gbl_panelFileSelection = new GridBagLayout();
		gbl_panelFileSelection.columnWidths = new int[] { 200, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelFileSelection.rowHeights = new int[] { 0, 0 };
		gbl_panelFileSelection.columnWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		gbl_panelFileSelection.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelFileSelection.setLayout(gbl_panelFileSelection);

		cbFileNames = new JComboBox<DirEntry>();
		cbFileNames.setName(CB_FILE_NAMES);
		cbFileNames.addActionListener(adapterForDiskUtility);
		cbFileNames.setPreferredSize(new Dimension(200, 20));
		GridBagConstraints gbc_cbFileNames = new GridBagConstraints();
		gbc_cbFileNames.fill = GridBagConstraints.HORIZONTAL;
		gbc_cbFileNames.insets = new Insets(0, 0, 0, 5);
		gbc_cbFileNames.anchor = GridBagConstraints.NORTH;
		gbc_cbFileNames.gridx = 0;
		gbc_cbFileNames.gridy = 0;
		panelFileSelection.add(cbFileNames, gbc_cbFileNames);

		Component horizontalStrut_4 = Box.createHorizontalStrut(20);
		GridBagConstraints gbc_horizontalStrut_4 = new GridBagConstraints();
		gbc_horizontalStrut_4.insets = new Insets(0, 0, 0, 5);
		gbc_horizontalStrut_4.gridx = 1;
		gbc_horizontalStrut_4.gridy = 0;
		panelFileSelection.add(horizontalStrut_4, gbc_horizontalStrut_4);

		lblRecordCount = new JLabel("0");
		lblRecordCount.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblRecordCount = new GridBagConstraints();
		gbc_lblRecordCount.insets = new Insets(0, 0, 0, 5);
		gbc_lblRecordCount.gridx = 4;
		gbc_lblRecordCount.gridy = 0;
		panelFileSelection.add(lblRecordCount, gbc_lblRecordCount);

		Component horizontalStrut_5 = Box.createHorizontalStrut(20);
		GridBagConstraints gbc_horizontalStrut_5 = new GridBagConstraints();
		gbc_horizontalStrut_5.insets = new Insets(0, 0, 0, 5);
		gbc_horizontalStrut_5.gridx = 3;
		gbc_horizontalStrut_5.gridy = 0;
		panelFileSelection.add(horizontalStrut_5, gbc_horizontalStrut_5);

		JLabel label25 = new JLabel("Record Count");
		label25.setFont(new Font("Arial", Font.PLAIN, 13));
		GridBagConstraints gbc_label25 = new GridBagConstraints();
		gbc_label25.insets = new Insets(0, 0, 0, 5);
		gbc_label25.gridx = 2;
		gbc_label25.gridy = 0;
		panelFileSelection.add(label25, gbc_label25);

		Component horizontalStrut_6 = Box.createHorizontalStrut(20);
		GridBagConstraints gbc_horizontalStrut_6 = new GridBagConstraints();
		gbc_horizontalStrut_6.insets = new Insets(0, 0, 0, 5);
		gbc_horizontalStrut_6.gridx = 5;
		gbc_horizontalStrut_6.gridy = 0;
		panelFileSelection.add(horizontalStrut_6, gbc_horizontalStrut_6);

		lblReadOnly = new JLabel("Read Only");
		lblReadOnly.setFont(new Font("Arial", Font.BOLD, 15));
		lblReadOnly.setForeground(Color.RED);
		GridBagConstraints gbc_lblReadOnly = new GridBagConstraints();
		gbc_lblReadOnly.insets = new Insets(0, 0, 0, 5);
		gbc_lblReadOnly.gridx = 6;
		gbc_lblReadOnly.gridy = 0;
		panelFileSelection.add(lblReadOnly, gbc_lblReadOnly);

		Component horizontalStrut_7 = Box.createHorizontalStrut(20);
		GridBagConstraints gbc_horizontalStrut_7 = new GridBagConstraints();
		gbc_horizontalStrut_7.insets = new Insets(0, 0, 0, 5);
		gbc_horizontalStrut_7.gridx = 7;
		gbc_horizontalStrut_7.gridy = 0;
		panelFileSelection.add(horizontalStrut_7, gbc_horizontalStrut_7);

		lblSystemFile = new JLabel("SystemFile");
		lblSystemFile.setForeground(Color.RED);
		lblSystemFile.setFont(new Font("Arial", Font.BOLD, 15));
		GridBagConstraints gbc_lblSystemFile = new GridBagConstraints();
		gbc_lblSystemFile.gridx = 8;
		gbc_lblSystemFile.gridy = 0;
		panelFileSelection.add(lblSystemFile, gbc_lblSystemFile);

		JPanel panelFileDislay = new JPanel();
		GridBagConstraints gbc_panelFileDislay = new GridBagConstraints();
		gbc_panelFileDislay.fill = GridBagConstraints.BOTH;
		gbc_panelFileDislay.gridx = 0;
		gbc_panelFileDislay.gridy = 1;
		tabFile.add(panelFileDislay, gbc_panelFileDislay);
		GridBagLayout gbl_panelFileDislay = new GridBagLayout();
		gbl_panelFileDislay.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelFileDislay.rowHeights = new int[] { 0, 0 };
		gbl_panelFileDislay.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelFileDislay.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelFileDislay.setLayout(gbl_panelFileDislay);

		Component horizontalStrut_8 = Box.createHorizontalStrut(20);
		GridBagConstraints gbc_horizontalStrut_8 = new GridBagConstraints();
		gbc_horizontalStrut_8.insets = new Insets(0, 0, 0, 5);
		gbc_horizontalStrut_8.gridx = 0;
		gbc_horizontalStrut_8.gridy = 0;
		panelFileDislay.add(horizontalStrut_8, gbc_horizontalStrut_8);

		panelFileHex = new HexEditPanelSimple();
		GridBagConstraints gbc_panelFileHex = new GridBagConstraints();
		gbc_panelFileHex.fill = GridBagConstraints.BOTH;
		gbc_panelFileHex.gridx = 1;
		gbc_panelFileHex.gridy = 0;
		panelFileDislay.add(panelFileHex, gbc_panelFileHex);

		// GridBagLayout gbl_panelFileHex = new GridBagLayout();
		// gbl_panelFileHex.columnWidths = new int[]{0};
		// gbl_panelFileHex.rowHeights = new int[]{0};
		// gbl_panelFileHex.columnWeights = new double[]{Double.MIN_VALUE};
		// gbl_panelFileHex.rowWeights = new double[]{Double.MIN_VALUE};
		// panelFileHex.setLayout(gbl_panelFileHex);
		//
		JPanel tabPhysical = new JPanel();
		tabbedPane.addTab("Physical View", null, tabPhysical, null);
		GridBagLayout gbl_tabPhysical = new GridBagLayout();
		gbl_tabPhysical.columnWidths = new int[] { 0, 0 };
		gbl_tabPhysical.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_tabPhysical.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_tabPhysical.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		tabPhysical.setLayout(gbl_tabPhysical);

		JPanel panelHeadTrackSector = new JPanel();
		GridBagConstraints gbc_panelHeadTrackSector = new GridBagConstraints();
		gbc_panelHeadTrackSector.insets = new Insets(0, 0, 5, 0);
		gbc_panelHeadTrackSector.fill = GridBagConstraints.BOTH;
		gbc_panelHeadTrackSector.gridx = 0;
		gbc_panelHeadTrackSector.gridy = 0;
		tabPhysical.add(panelHeadTrackSector, gbc_panelHeadTrackSector);
		GridBagLayout gbl_panelHeadTrackSector = new GridBagLayout();
		gbl_panelHeadTrackSector.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelHeadTrackSector.rowHeights = new int[] { 0, 0 };
		gbl_panelHeadTrackSector.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		gbl_panelHeadTrackSector.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelHeadTrackSector.setLayout(gbl_panelHeadTrackSector);

		JLabel label10 = new JLabel("Head");
		GridBagConstraints gbc_label10 = new GridBagConstraints();
		gbc_label10.fill = GridBagConstraints.HORIZONTAL;
		gbc_label10.insets = new Insets(0, 0, 0, 5);
		gbc_label10.gridx = 0;
		gbc_label10.gridy = 0;
		panelHeadTrackSector.add(label10, gbc_label10);

		hdnHead = new HDNumberBox();
		hdnHead.setName(HDN_HEAD);
		hdnHead.addHDNumberValueChangedListener(adapterForDiskUtility);
		hdnHead.setPreferredSize(new Dimension(50, 20));
		GridBagConstraints gbc_hdnHead = new GridBagConstraints();
		gbc_hdnHead.insets = new Insets(0, 0, 0, 5);
		gbc_hdnHead.fill = GridBagConstraints.VERTICAL;
		gbc_hdnHead.gridx = 1;
		gbc_hdnHead.gridy = 0;
		panelHeadTrackSector.add(hdnHead, gbc_hdnHead);
		GridBagLayout gbl_hdnHead = new GridBagLayout();
		gbl_hdnHead.columnWidths = new int[] { 0 };
		gbl_hdnHead.rowHeights = new int[] { 0 };
		gbl_hdnHead.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_hdnHead.rowWeights = new double[] { Double.MIN_VALUE };
		hdnHead.setLayout(gbl_hdnHead);

		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		GridBagConstraints gbc_horizontalStrut_1 = new GridBagConstraints();
		gbc_horizontalStrut_1.insets = new Insets(0, 0, 0, 5);
		gbc_horizontalStrut_1.gridx = 3;
		gbc_horizontalStrut_1.gridy = 0;
		panelHeadTrackSector.add(horizontalStrut_1, gbc_horizontalStrut_1);

		JLabel label11 = new JLabel("Track");
		GridBagConstraints gbc_label11 = new GridBagConstraints();
		gbc_label11.insets = new Insets(0, 0, 0, 5);
		gbc_label11.gridx = 2;
		gbc_label11.gridy = 0;
		panelHeadTrackSector.add(label11, gbc_label11);

		hdnTrack = new HDNumberBox();
		hdnTrack.setName(HDN_TRACK);
		hdnTrack.addHDNumberValueChangedListener(adapterForDiskUtility);
		hdnTrack.setPreferredSize(new Dimension(50, 20));
		GridBagConstraints gbc_hdnTrack = new GridBagConstraints();
		gbc_hdnTrack.insets = new Insets(0, 0, 0, 5);
		gbc_hdnTrack.fill = GridBagConstraints.BOTH;
		gbc_hdnTrack.gridx = 4;
		gbc_hdnTrack.gridy = 0;
		panelHeadTrackSector.add(hdnTrack, gbc_hdnTrack);
		GridBagLayout gbl_hdnTrack = new GridBagLayout();
		gbl_hdnTrack.columnWidths = new int[] { 0 };
		gbl_hdnTrack.rowHeights = new int[] { 0 };
		gbl_hdnTrack.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_hdnTrack.rowWeights = new double[] { Double.MIN_VALUE };
		hdnTrack.setLayout(gbl_hdnTrack);

		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		GridBagConstraints gbc_horizontalStrut_2 = new GridBagConstraints();
		gbc_horizontalStrut_2.insets = new Insets(0, 0, 0, 5);
		gbc_horizontalStrut_2.gridx = 5;
		gbc_horizontalStrut_2.gridy = 0;
		panelHeadTrackSector.add(horizontalStrut_2, gbc_horizontalStrut_2);

		JLabel label12 = new JLabel("Sector");
		GridBagConstraints gbc_label12 = new GridBagConstraints();
		gbc_label12.insets = new Insets(0, 0, 0, 5);
		gbc_label12.gridx = 6;
		gbc_label12.gridy = 0;
		panelHeadTrackSector.add(label12, gbc_label12);

		hdnSector = new HDNumberBox();
		hdnSector.setName(HDN_SECTOR);
		hdnSector.addHDNumberValueChangedListener(adapterForDiskUtility);
		hdnSector.setPreferredSize(new Dimension(50, 20));
		GridBagConstraints gbc_hdnSector = new GridBagConstraints();
		gbc_hdnSector.fill = GridBagConstraints.BOTH;
		gbc_hdnSector.gridx = 8;
		gbc_hdnSector.gridy = 0;
		panelHeadTrackSector.add(hdnSector, gbc_hdnSector);
		GridBagLayout gbl_hdnSector = new GridBagLayout();
		gbl_hdnSector.columnWidths = new int[] { 0 };
		gbl_hdnSector.rowHeights = new int[] { 0 };
		gbl_hdnSector.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_hdnSector.rowWeights = new double[] { Double.MIN_VALUE };
		hdnSector.setLayout(gbl_hdnSector);

		JPanel panelPhysicalDisplay = new JPanel();
		GridBagConstraints gbc_panelPhysicalDisplay = new GridBagConstraints();
		gbc_panelPhysicalDisplay.insets = new Insets(0, 0, 5, 0);
		gbc_panelPhysicalDisplay.fill = GridBagConstraints.BOTH;
		gbc_panelPhysicalDisplay.gridx = 0;
		gbc_panelPhysicalDisplay.gridy = 1;
		tabPhysical.add(panelPhysicalDisplay, gbc_panelPhysicalDisplay);

		GridBagLayout gbl_panelPhysicalDisplay = new GridBagLayout();
		gbl_panelPhysicalDisplay.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelPhysicalDisplay.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelPhysicalDisplay.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelPhysicalDisplay.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		panelPhysicalDisplay.setLayout(gbl_panelPhysicalDisplay);

		Component horizontalStrut_3 = Box.createHorizontalStrut(20);
		GridBagConstraints gbc_horizontalStrut_3 = new GridBagConstraints();
		gbc_horizontalStrut_3.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalStrut_3.gridx = 0;
		gbc_horizontalStrut_3.gridy = 0;
		panelPhysicalDisplay.add(horizontalStrut_3, gbc_horizontalStrut_3);

		panelSectorDisplay = new HexEditPanelSimple();
		GridBagConstraints gbc_panelSectorDisplay = new GridBagConstraints();
		gbc_panelSectorDisplay.insets = new Insets(0, 0, 5, 0);
		gbc_panelSectorDisplay.fill = GridBagConstraints.BOTH;
		gbc_panelSectorDisplay.gridx = 1;
		gbc_panelSectorDisplay.gridy = 0;
		panelPhysicalDisplay.add(panelSectorDisplay, gbc_panelSectorDisplay);

		JPanel panelSeek = new JPanel();
		GridBagConstraints gbc_panelSeek = new GridBagConstraints();
		gbc_panelSeek.fill = GridBagConstraints.VERTICAL;
		gbc_panelSeek.gridx = 0;
		gbc_panelSeek.gridy = 2;
		tabPhysical.add(panelSeek, gbc_panelSeek);
		GridBagLayout gbl_panelSeek = new GridBagLayout();
		gbl_panelSeek.columnWidths = new int[] { 0, 0 };
		gbl_panelSeek.rowHeights = new int[] { 0, 0 };
		gbl_panelSeek.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelSeek.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelSeek.setLayout(gbl_panelSeek);

		hdnSeekPanel = new HDSeekPanel();
		hdnSeekPanel.setName(HDN_SEEK_PANEL);
		hdnSeekPanel.addHDNumberValueChangedListener(adapterForDiskUtility);
		hdnSeekPanel.setPreferredSize(new Dimension(260, 30));
		GridBagConstraints gbc_hdnSeekPanel = new GridBagConstraints();
		gbc_hdnSeekPanel.fill = GridBagConstraints.VERTICAL;
		gbc_hdnSeekPanel.gridx = 0;
		gbc_hdnSeekPanel.gridy = 0;
		panelSeek.add(hdnSeekPanel, gbc_hdnSeekPanel);
		GridBagLayout gbl_hdnSeekPanel = new GridBagLayout();
		gbl_hdnSeekPanel.columnWidths = new int[] { 0 };
		gbl_hdnSeekPanel.rowHeights = new int[] { 0 };
		gbl_hdnSeekPanel.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_hdnSeekPanel.rowWeights = new double[] { Double.MIN_VALUE };
		hdnSeekPanel.setLayout(gbl_hdnSeekPanel);

		JPanel tabImport = new JPanel();
		tabbedPane.addTab("Import/Export", null, tabImport, null);
		GridBagLayout gbl_tabImport = new GridBagLayout();
		gbl_tabImport.columnWidths = new int[] { 0, 0 };
		gbl_tabImport.rowHeights = new int[] { 0, 0, 0 };
		gbl_tabImport.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_tabImport.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		tabImport.setLayout(gbl_tabImport);

		JPanel panelMetrics = new JPanel();
		panelMetrics.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelMetrics = new GridBagConstraints();
		gbc_panelMetrics.insets = new Insets(0, 0, 5, 0);
		gbc_panelMetrics.fill = GridBagConstraints.BOTH;
		gbc_panelMetrics.gridx = 0;
		gbc_panelMetrics.gridy = 0;
		tabImport.add(panelMetrics, gbc_panelMetrics);
		GridBagLayout gbl_panelMetrics = new GridBagLayout();
		gbl_panelMetrics.columnWidths = new int[] { 0, 0 };
		gbl_panelMetrics.rowHeights = new int[] { 0, 0 };
		gbl_panelMetrics.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelMetrics.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelMetrics.setLayout(gbl_panelMetrics);

		JPanel panelMetrics0 = new JPanel();
		panelMetrics0.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true),
				"Disk & File System Metrics", TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, null));
		GridBagConstraints gbc_panelMetrics0 = new GridBagConstraints();
		gbc_panelMetrics0.fill = GridBagConstraints.VERTICAL;
		gbc_panelMetrics0.gridx = 0;
		gbc_panelMetrics0.gridy = 0;
		panelMetrics.add(panelMetrics0, gbc_panelMetrics0);
		GridBagLayout gbl_panelMetrics0 = new GridBagLayout();
		gbl_panelMetrics0.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelMetrics0.rowHeights = new int[] { 0, 0 };
		gbl_panelMetrics0.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelMetrics0.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelMetrics0.setLayout(gbl_panelMetrics0);

		JPanel panelDiskGeometry = new JPanel();
		panelDiskGeometry.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Disk Geometry",
				TitledBorder.LEFT, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panelDiskGeometry = new GridBagConstraints();
		gbc_panelDiskGeometry.insets = new Insets(0, 0, 0, 5);
		gbc_panelDiskGeometry.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelDiskGeometry.gridx = 0;
		gbc_panelDiskGeometry.gridy = 0;
		panelMetrics0.add(panelDiskGeometry, gbc_panelDiskGeometry);
		GridBagLayout gbl_panelDiskGeometry = new GridBagLayout();
		gbl_panelDiskGeometry.columnWidths = new int[] { 0, 50, 10, 150, 0 };
		gbl_panelDiskGeometry.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panelDiskGeometry.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelDiskGeometry.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelDiskGeometry.setLayout(gbl_panelDiskGeometry);

		lblHeads = new JLabel("0");
		lblHeads.setPreferredSize(new Dimension(50, 14));
		lblHeads.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblHeads.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblHeads = new GridBagConstraints();
		gbc_lblHeads.insets = new Insets(0, 0, 5, 5);
		gbc_lblHeads.gridx = 1;
		gbc_lblHeads.gridy = 0;
		panelDiskGeometry.add(lblHeads, gbc_lblHeads);

		JLabel label15 = new JLabel("Heads");
		label15.setHorizontalAlignment(SwingConstants.LEFT);
		label15.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_label15 = new GridBagConstraints();
		gbc_label15.anchor = GridBagConstraints.SOUTHWEST;
		gbc_label15.insets = new Insets(0, 0, 5, 0);
		gbc_label15.gridx = 3;
		gbc_label15.gridy = 0;
		panelDiskGeometry.add(label15, gbc_label15);

		lblTracksPerHead = new JLabel("0");
		lblTracksPerHead.setPreferredSize(new Dimension(50, 14));
		lblTracksPerHead.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTracksPerHead.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblTracksPerHead = new GridBagConstraints();
		gbc_lblTracksPerHead.insets = new Insets(0, 0, 5, 5);
		gbc_lblTracksPerHead.gridx = 1;
		gbc_lblTracksPerHead.gridy = 1;
		panelDiskGeometry.add(lblTracksPerHead, gbc_lblTracksPerHead);

		JLabel label16 = new JLabel("Tracks/Head");
		label16.setHorizontalAlignment(SwingConstants.LEFT);
		label16.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_label16 = new GridBagConstraints();
		gbc_label16.anchor = GridBagConstraints.WEST;
		gbc_label16.insets = new Insets(0, 0, 5, 0);
		gbc_label16.gridx = 3;
		gbc_label16.gridy = 1;
		panelDiskGeometry.add(label16, gbc_label16);

		lblSectorsPerTrack = new JLabel("0");
		lblSectorsPerTrack.setPreferredSize(new Dimension(50, 14));
		lblSectorsPerTrack.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSectorsPerTrack.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblSectorsPerTrack = new GridBagConstraints();
		gbc_lblSectorsPerTrack.insets = new Insets(0, 0, 5, 5);
		gbc_lblSectorsPerTrack.gridx = 1;
		gbc_lblSectorsPerTrack.gridy = 2;
		panelDiskGeometry.add(lblSectorsPerTrack, gbc_lblSectorsPerTrack);

		JLabel label17 = new JLabel("Sectors/Track");
		label17.setHorizontalAlignment(SwingConstants.LEFT);
		label17.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_label17 = new GridBagConstraints();
		gbc_label17.anchor = GridBagConstraints.WEST;
		gbc_label17.insets = new Insets(0, 0, 5, 0);
		gbc_label17.gridx = 3;
		gbc_label17.gridy = 2;
		panelDiskGeometry.add(label17, gbc_label17);

		lblTotalTracks = new JLabel("0");
		lblTotalTracks.setPreferredSize(new Dimension(50, 14));
		lblTotalTracks.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTotalTracks.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblTotalTracks = new GridBagConstraints();
		gbc_lblTotalTracks.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalTracks.gridx = 1;
		gbc_lblTotalTracks.gridy = 3;
		panelDiskGeometry.add(lblTotalTracks, gbc_lblTotalTracks);

		JLabel label18 = new JLabel("Total Tracks");
		label18.setHorizontalAlignment(SwingConstants.LEFT);
		label18.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_label18 = new GridBagConstraints();
		gbc_label18.anchor = GridBagConstraints.WEST;
		gbc_label18.insets = new Insets(0, 0, 5, 0);
		gbc_label18.gridx = 3;
		gbc_label18.gridy = 3;
		panelDiskGeometry.add(label18, gbc_label18);

		lblTotalSectors = new JLabel("0");
		lblTotalSectors.setPreferredSize(new Dimension(50, 14));
		lblTotalSectors.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTotalSectors.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblTotalSectors = new GridBagConstraints();
		gbc_lblTotalSectors.insets = new Insets(0, 0, 0, 5);
		gbc_lblTotalSectors.gridx = 1;
		gbc_lblTotalSectors.gridy = 4;
		panelDiskGeometry.add(lblTotalSectors, gbc_lblTotalSectors);

		JLabel label19 = new JLabel("Total Sectors");
		label19.setHorizontalAlignment(SwingConstants.LEFT);
		label19.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_label19 = new GridBagConstraints();
		gbc_label19.anchor = GridBagConstraints.WEST;
		gbc_label19.gridx = 3;
		gbc_label19.gridy = 4;
		panelDiskGeometry.add(label19, gbc_label19);

		JPanel panelFileSystemParameters = new JPanel();
		panelFileSystemParameters.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true),
				"File System Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panelFileSystemParameters = new GridBagConstraints();
		gbc_panelFileSystemParameters.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelFileSystemParameters.gridx = 1;
		gbc_panelFileSystemParameters.gridy = 0;
		panelMetrics0.add(panelFileSystemParameters, gbc_panelFileSystemParameters);
		GridBagLayout gbl_panelFileSystemParameters = new GridBagLayout();
		gbl_panelFileSystemParameters.columnWidths = new int[] { 0, 50, 10, 150, 0 };
		gbl_panelFileSystemParameters.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_panelFileSystemParameters.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelFileSystemParameters.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelFileSystemParameters.setLayout(gbl_panelFileSystemParameters);

		lblTracksBeforeDirectory = new JLabel("0");
		lblTracksBeforeDirectory.setPreferredSize(new Dimension(50, 14));
		lblTracksBeforeDirectory.setHorizontalAlignment(SwingConstants.RIGHT);
		lblTracksBeforeDirectory.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblTracksBeforeDirectory = new GridBagConstraints();
		gbc_lblTracksBeforeDirectory.insets = new Insets(0, 0, 5, 5);
		gbc_lblTracksBeforeDirectory.gridx = 1;
		gbc_lblTracksBeforeDirectory.gridy = 0;
		panelFileSystemParameters.add(lblTracksBeforeDirectory, gbc_lblTracksBeforeDirectory);

		JLabel label20 = new JLabel("Tracks Before Directory");
		GridBagConstraints gbc_label20 = new GridBagConstraints();
		gbc_label20.anchor = GridBagConstraints.WEST;
		gbc_label20.insets = new Insets(0, 0, 5, 0);
		gbc_label20.gridx = 3;
		gbc_label20.gridy = 0;
		panelFileSystemParameters.add(label20, gbc_label20);

		lblLogicalBlockSizeInSectors = new JLabel("0");
		lblLogicalBlockSizeInSectors.setPreferredSize(new Dimension(50, 14));
		lblLogicalBlockSizeInSectors.setHorizontalAlignment(SwingConstants.RIGHT);
		lblLogicalBlockSizeInSectors.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblLogicalBlockSizeInSectors = new GridBagConstraints();
		gbc_lblLogicalBlockSizeInSectors.insets = new Insets(0, 0, 5, 5);
		gbc_lblLogicalBlockSizeInSectors.gridx = 1;
		gbc_lblLogicalBlockSizeInSectors.gridy = 1;
		panelFileSystemParameters.add(lblLogicalBlockSizeInSectors, gbc_lblLogicalBlockSizeInSectors);

		JLabel label21 = new JLabel("Sectors/Block");
		GridBagConstraints gbc_label21 = new GridBagConstraints();
		gbc_label21.anchor = GridBagConstraints.WEST;
		gbc_label21.insets = new Insets(0, 0, 5, 0);
		gbc_label21.gridx = 3;
		gbc_label21.gridy = 1;
		panelFileSystemParameters.add(label21, gbc_label21);

		lblMaxDirectoryEntry = new JLabel("0");
		lblMaxDirectoryEntry.setPreferredSize(new Dimension(50, 14));
		lblMaxDirectoryEntry.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMaxDirectoryEntry.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblMaxDirectoryEntry = new GridBagConstraints();
		gbc_lblMaxDirectoryEntry.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaxDirectoryEntry.gridx = 1;
		gbc_lblMaxDirectoryEntry.gridy = 2;
		panelFileSystemParameters.add(lblMaxDirectoryEntry, gbc_lblMaxDirectoryEntry);

		JLabel label22 = new JLabel("Max Directory Entry");
		GridBagConstraints gbc_label22 = new GridBagConstraints();
		gbc_label22.anchor = GridBagConstraints.WEST;
		gbc_label22.insets = new Insets(0, 0, 5, 0);
		gbc_label22.gridx = 3;
		gbc_label22.gridy = 2;
		panelFileSystemParameters.add(label22, gbc_label22);

		lblMaxBlockNumber = new JLabel("0");
		lblMaxBlockNumber.setPreferredSize(new Dimension(50, 14));
		lblMaxBlockNumber.setHorizontalAlignment(SwingConstants.RIGHT);
		lblMaxBlockNumber.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblMaxBlockNumber = new GridBagConstraints();
		gbc_lblMaxBlockNumber.insets = new Insets(0, 0, 0, 5);
		gbc_lblMaxBlockNumber.gridx = 1;
		gbc_lblMaxBlockNumber.gridy = 3;
		panelFileSystemParameters.add(lblMaxBlockNumber, gbc_lblMaxBlockNumber);

		JLabel label23 = new JLabel("Max Block Number");
		GridBagConstraints gbc_label23 = new GridBagConstraints();
		gbc_label23.anchor = GridBagConstraints.WEST;
		gbc_label23.gridx = 3;
		gbc_label23.gridy = 3;
		panelFileSystemParameters.add(label23, gbc_label23);

		JPanel panelImportExport = new JPanel();
		panelImportExport.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelImportExport = new GridBagConstraints();
		gbc_panelImportExport.fill = GridBagConstraints.BOTH;
		gbc_panelImportExport.gridx = 0;
		gbc_panelImportExport.gridy = 1;
		tabImport.add(panelImportExport, gbc_panelImportExport);
		GridBagLayout gbl_panelImportExport = new GridBagLayout();
		gbl_panelImportExport.columnWidths = new int[] { 0, 0 };
		gbl_panelImportExport.rowHeights = new int[] { 0, 0 };
		gbl_panelImportExport.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelImportExport.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelImportExport.setLayout(gbl_panelImportExport);

		JPanel panelImportExport0 = new JPanel();
		panelImportExport0.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true),
				"Import / Export Files", TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, null));
		GridBagConstraints gbc_panelImportExport0 = new GridBagConstraints();
		gbc_panelImportExport0.anchor = GridBagConstraints.NORTH;
		gbc_panelImportExport0.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelImportExport0.gridx = 0;
		gbc_panelImportExport0.gridy = 0;
		panelImportExport.add(panelImportExport0, gbc_panelImportExport0);
		GridBagLayout gbl_panelImportExport0 = new GridBagLayout();
		gbl_panelImportExport0.columnWidths = new int[] { 0, 0, 0, 0, 0 };
		gbl_panelImportExport0.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panelImportExport0.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panelImportExport0.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelImportExport0.setLayout(gbl_panelImportExport0);

		JLabel label24 = new JLabel("Host File: ");
		GridBagConstraints gbc_label24 = new GridBagConstraints();
		gbc_label24.insets = new Insets(0, 0, 5, 5);
		gbc_label24.anchor = GridBagConstraints.EAST;
		gbc_label24.gridx = 1;
		gbc_label24.gridy = 1;
		panelImportExport0.add(label24, gbc_label24);

		btnHostFile = new JButton("...");
		btnHostFile.setName(BTN_HOST_FILE);
		btnHostFile.addActionListener(adapterForDiskUtility);
		btnHostFile.setPreferredSize(new Dimension(25, 23));
		GridBagConstraints gbc_btnHostFile = new GridBagConstraints();
		gbc_btnHostFile.insets = new Insets(0, 0, 5, 5);
		gbc_btnHostFile.gridx = 2;
		gbc_btnHostFile.gridy = 1;
		panelImportExport0.add(btnHostFile, gbc_btnHostFile);

		txtHostFileInOut = new JTextField();
		GridBagConstraints gbc_txtHostFileInOut = new GridBagConstraints();
		gbc_txtHostFileInOut.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtHostFileInOut.insets = new Insets(0, 0, 5, 0);
		gbc_txtHostFileInOut.gridx = 3;
		gbc_txtHostFileInOut.gridy = 1;
		panelImportExport0.add(txtHostFileInOut, gbc_txtHostFileInOut);
		txtHostFileInOut.setColumns(10);

		JLabel lblFile = new JLabel("CPM File: ");
		GridBagConstraints gbc_lblFile = new GridBagConstraints();
		gbc_lblFile.insets = new Insets(0, 0, 5, 5);
		gbc_lblFile.anchor = GridBagConstraints.EAST;
		gbc_lblFile.gridx = 1;
		gbc_lblFile.gridy = 2;
		panelImportExport0.add(lblFile, gbc_lblFile);

		cbCPMFileInOut = new JComboBox<DirEntry>();
		cbCPMFileInOut.setName(CB_CPM_FILE_IN_OUT);
//		cbCPMFileInOut.addActionListener(adapterForDiskUtility);
		cbCPMFileInOut.setEditable(true);
		cbCPMFileInOut.setMaximumSize(new Dimension(200, 20));
		cbCPMFileInOut.setMinimumSize(new Dimension(200, 20));
		cbCPMFileInOut.setPreferredSize(new Dimension(200, 20));
		GridBagConstraints gbc_cbCPMFileInOut = new GridBagConstraints();
		gbc_cbCPMFileInOut.anchor = GridBagConstraints.WEST;
		gbc_cbCPMFileInOut.insets = new Insets(0, 0, 5, 0);
		gbc_cbCPMFileInOut.gridx = 3;
		gbc_cbCPMFileInOut.gridy = 2;
		panelImportExport0.add(cbCPMFileInOut, gbc_cbCPMFileInOut);

		btnExport = new JButton("Export To Host File");
		btnExport.setName(BTN_EXPORT);
		btnExport.addActionListener(adapterForDiskUtility);
		GridBagConstraints gbc_btnExport = new GridBagConstraints();
		gbc_btnExport.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnExport.insets = new Insets(0, 0, 5, 5);
		gbc_btnExport.gridx = 1;
		gbc_btnExport.gridy = 3;
		panelImportExport0.add(btnExport, gbc_btnExport);

		btnImport = new JButton("Import FromHost File");
		btnImport.setName(BTN_IMPORT);
		btnImport.addActionListener(adapterForDiskUtility);
		GridBagConstraints gbc_btnImport = new GridBagConstraints();
		gbc_btnImport.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnImport.insets = new Insets(0, 0, 0, 5);
		gbc_btnImport.gridx = 1;
		gbc_btnImport.gridy = 4;
		panelImportExport0.add(btnImport, gbc_btnImport);

		JPanel tabCatalog = new JPanel();
		tabbedPane.addTab("Catalog", null, tabCatalog, null);
		GridBagLayout gbl_tabCatalog = new GridBagLayout();
		gbl_tabCatalog.columnWidths = new int[] { 0 };
		gbl_tabCatalog.rowHeights = new int[] { 0 };
		gbl_tabCatalog.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_tabCatalog.rowWeights = new double[] { Double.MIN_VALUE };
		tabCatalog.setLayout(gbl_tabCatalog);

		JPanel panelStatus = new JPanel();
		panelStatus.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelStatus = new GridBagConstraints();
		gbc_panelStatus.fill = GridBagConstraints.BOTH;
		gbc_panelStatus.gridx = 0;
		gbc_panelStatus.gridy = 3;
		this.getContentPane().add(panelStatus, gbc_panelStatus);

		JMenuBar menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);

		JMenu mnuDisk = new JMenu("Disk");
		menuBar.add(mnuDisk);

		mnuDiskLoad = new JMenuItem("Load ...");
		mnuDiskLoad.setName(MNU_DISK_LOAD);
		mnuDiskLoad.addActionListener(adapterForDiskUtility);
		mnuDisk.add(mnuDiskLoad);

		JSeparator separator98 = new JSeparator();
		mnuDisk.add(separator98);

		mnuDiskClose = new JMenuItem("Close...");
		mnuDiskClose.setName(MNU_DISK_CLOSE);
		mnuDiskClose.addActionListener(adapterForDiskUtility);
		mnuDisk.add(mnuDiskClose);

		JSeparator separator99 = new JSeparator();
		mnuDisk.add(separator99);

		mnuDiskSave = new JMenuItem("Save...");
		mnuDiskSave.setName(MNU_DISK_SAVE);
		mnuDiskSave.addActionListener(adapterForDiskUtility);
		mnuDisk.add(mnuDiskSave);

		mnuDiskSaveAs = new JMenuItem("Save As...");
		mnuDiskSaveAs.setName(MNU_DISK_SAVE_AS);
		mnuDiskSaveAs.addActionListener(adapterForDiskUtility);
		mnuDisk.add(mnuDiskSaveAs);

		JSeparator separator_2 = new JSeparator();
		mnuDisk.add(separator_2);

		JMenuItem mnuFileExit = new JMenuItem("Exit");
		mnuFileExit.setName(MNU_DISK_EXIT);
		mnuFileExit.addActionListener(adapterForDiskUtility);
		mnuDisk.add(mnuFileExit);

		JMenu mnTools = new JMenu("Tools");
		menuBar.add(mnTools);

		mnuToolsNew = new JMenuItem("New Disk");
		mnuToolsNew.setName(MNU_TOOLS_NEW);
		mnuToolsNew.addActionListener(adapterForDiskUtility);
		mnTools.add(mnuToolsNew);

		mnuToolsUpdate = new JMenuItem("Update System on Disk");
		mnuToolsUpdate.setName(MNU_TOOLS_UPDATE);
		mnuToolsUpdate.addActionListener(adapterForDiskUtility);
		mnTools.add(mnuToolsUpdate);

	}// initialize

	static final String EMPTY_STRING = "";
	static final String NO_ACTIVE_DISK = "<No Active Disk>";
	static final String NO_ACTIVE_FILE = "<No Active File>";

	//////////////////////////////////////////////////////////////////////////
	private static final String MNU_DISK_LOAD = "mnuDiskLoad";
	private static final String MNU_DISK_CLOSE = "mnuDiskClose";
	private static final String MNU_DISK_SAVE = "mnuDiskSave";
	private static final String MNU_DISK_SAVE_AS = "mnuDiskSaveAs";
	private static final String MNU_DISK_EXIT = "mnuDiskExit";

	private static final String MNU_TOOLS_NEW = "mnuToolsNew";
	private static final String MNU_TOOLS_UPDATE = "mnuToolsUpdate";

	private static final String TB_DISPLAY_BASE = "tbDisplayBase";
	private static final String TB_DISPLAY_DECIMAL = "Display Decimal";
	private static final String TB_DISPLAY_HEX = "Display Hex";

	private static final String TABLE_DIRECTORY = "tableDirectory";

	public static final String USER_HOME = "user.home";
	public static final String THIS_DIR = ".";

	private final static String CB_FILE_NAMES = "cbFileNames";
	private final static String CB_CPM_FILE_IN_OUT = "cbCpmFileInOut";

	public static final String HDN_HEAD = "hdnHead";
	public static final String HDN_TRACK = "hdnTrack";
	public static final String HDN_SECTOR = "hdnSector";
	public static final String HDN_SEEK_PANEL = "seekPanel";

	public static final String BTN_IMPORT = "btnImport";
	public static final String BTN_EXPORT = "btnExport";
	public static final String BTN_HOST_FILE = "btnHostFile";

	//////////////////////////////////////////////////////////////////////////
	// private JFrame frameBase;
	private JPanel mainPanel;
	private JToggleButton tbDisplayBase;
	private JTabbedPane tabbedPane;
	private JLabel lblActiveDisk;
	private JLabel lblRawName;
	private JLabel lblRawUser;
	private JLabel lblRawType;
	private JLabel lblRawS1;
	private JLabel lblRawS2;
	private JLabel lblRawRC;
	private JMenuItem mnuDiskLoad;
	private JMenuItem mnuDiskClose;
	private JMenuItem mnuDiskSave;
	private JMenuItem mnuDiskSaveAs;
	private JMenuItem mnuToolsNew;
	private JMenuItem mnuToolsUpdate;
	private HDNumberBox hdnHead;
	private HDNumberBox hdnSector;
	private HDNumberBox hdnTrack;
	private HDSeekPanel hdnSeekPanel;
	private JLabel lblHeads;
	private JLabel lblTracksPerHead;
	private JLabel lblSectorsPerTrack;
	private JLabel lblTotalTracks;
	private JLabel lblTracksBeforeDirectory;
	private JLabel lblMaxBlockNumber;
	private JLabel lblMaxDirectoryEntry;
	private JLabel lblLogicalBlockSizeInSectors;
	private JLabel lblTotalSectors;
	private JButton btnHostFile;
	private JTextField txtHostFileInOut;
	private JButton btnExport;
	private JButton btnImport;
	private HexEditPanelSimple panelSectorDisplay;
	private JScrollPane scrollDirectoryTable;
	private JLabel lblRawAllocation;
	private JLabel lblRawEX;
	private JComboBox<DirEntry> cbFileNames;
	private JComboBox<DirEntry> cbCPMFileInOut;
	private JLabel lblRecordCount;
	private HexEditPanelSimple panelFileHex;
	private JLabel lblReadOnly;
	private JLabel lblSystemFile;
	private JTable directoryTable;
	//////////////////////////////////////////////////////////////////////////

	class AdapterForDiskUtility implements ActionListener, ListSelectionListener, HDNumberValueChangeListener {
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String name = ((Component) actionEvent.getSource()).getName();
			switch (name) {
			case MNU_DISK_LOAD:
				doDiskLoad();
				break;
			case MNU_DISK_CLOSE:
				doDiskClose();
				break;
			case MNU_DISK_SAVE:
				doDiskSave();
				break;
			case MNU_DISK_SAVE_AS:
				doDiskSaveAs();
				break;
			case MNU_DISK_EXIT:
				doDiskExit();
				break;

			case MNU_TOOLS_NEW:
				doToolsNew();
				break;
			case MNU_TOOLS_UPDATE:
				doToolsUpdate();
				break;

			case BTN_HOST_FILE:
				doGetHostFile();
				break;

			case BTN_EXPORT:
				doExport();
				break;

			case BTN_IMPORT:
				doImport();
				int a = 5;
				break;

			case CB_FILE_NAMES:
				doDisplaySelectedFile();
				break;
			case TB_DISPLAY_BASE:
				// selected = display Decimal
				doDisplayBase(((AbstractButton) actionEvent.getSource()));
				break;
			default:
				log.addError("[actionPerformed] unknown name " + name + ".");
			}// switch
		}// actionPerformed

		@Override
		public void valueChanged(ListSelectionEvent listSelectionEvent) {
			if (listSelectionEvent.getValueIsAdjusting()) {
				return;
			} // if
			showDirectoryDetail(directoryTable.getSelectedRow());
		}// valueChanged

		@Override
		public void valueChanged(HDNumberValueChangeEvent hDNumberValueChangeEvent) {
			String name = ((Component) hDNumberValueChangeEvent.getSource()).getName();

			switch (name) {
			case HDN_HEAD:
			case HDN_TRACK:
			case HDN_SECTOR:
				selectedNewPhysicalSector(false);
				break;
			case HDN_SEEK_PANEL:
				selectedNewPhysicalSector(true);
				break;
			default:
				log.addWarning("Unrecognized HDNumberValueChange change.");
			}// switch
		}// valueChanged
	}// class AdapterAction

}// class GUItemplate