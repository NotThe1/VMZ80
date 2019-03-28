package disks;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.JFileChooser;

import codeSupport.AppLogger;
import disks.diskPanel.DiskPanelEvent;
import disks.diskPanel.DiskPanelEventListener;
import disks.diskPanel.V_IF_DiskPanel;
import memory.Core;
import memory.CpuBuss;
import memory.IoBuss;
import memory.MemoryTrapEvent;
import utilities.filePicker.FilePicker;

public class DiskControlUnit {
	AdapterDCU adapterDCU = new AdapterDCU();
	private AppLogger log = AppLogger.getInstance();

	private V_IF_DiskPanel ifDisks;

	private CpuBuss cpuBuss = CpuBuss.getInstance();
	private IoBuss ioBuss = IoBuss.getInstance();
	private DiskDrive[] drives;
	// private int maxNumberOfDrives;

	private int currentDrive;
	private int currentDiskControlByte;
	private byte currentCommand;
	private int currentUnit;
	private int currentHead;
	private int currentTrack;
	private int currentSector;
	private int currentByteCount;
	private int currentDMAAddress;

	private boolean goodOperation;

	private static DiskControlUnit instance = new DiskControlUnit();

	public static DiskControlUnit getInstance() {
		return instance;
	}// getInstance

	public void setDisplay(V_IF_DiskPanel ifDisks) {
		this.ifDisks = ifDisks;
		ifDisks.addDiskPanelActionListener(adapterDCU);
	}// setDisplay

	private boolean isDiskMounted(File newFile) {
		boolean ans = false;
		for (int i = 0; i < Disk.NUMBER_OF_DISKS; i++) {
			if (drives[i] == null) {
				continue;
			} // if null
			if (newFile.getAbsolutePath().equals(drives[i].getFilePath())) {
				ans = true;
				break;
			} // if
		} // for
		return ans;
	}// isDiskMounted

	private boolean isDiskMounted(String absolutePath) {
		boolean ans = false;
		for (int i = 0; i < Disk.NUMBER_OF_DISKS; i++) {
			if (drives[i] == null) {
				continue;
			} // if null
			if (absolutePath.equals(drives[i].getFilePath())) {
				ans = true;
				break;
			} // if
		} // for

		return ans;
	}// isDiskMounted

	private void reportStatus(byte firstCode, byte secondCode) {
		ioBuss.write(DISK_STATUS_BLOCK, firstCode);
		ioBuss.write(DISK_STATUS_BLOCK + 1, secondCode);
		ioBuss.write(currentDiskControlByte, BYTE00); // reset - operation
	}// reportStatus

	private void doDiskError(int errorCode, String message) {
		log.errorf("DCU error: -5D -%s%n", errorCode, message);
		goodOperation = false;
		reportStatus(BYTE00, BYTE00);
		ioBuss.write(DISK_STATUS_BLOCK, (byte) 00);
		ioBuss.write(DISK_STATUS_BLOCK + 1, (byte) errorCode);
		ioBuss.write(currentDiskControlByte, (byte) 00); // reset - operation is over

	}// doDiskError

	// private void doMemoryTrap() {
	//
	// }// doMemoryTrap

	private void doDiskPanelEvent(DiskPanelEvent diskPanelEvent) {
		if (diskPanelEvent.isSelected()) {
			mountDisk(diskPanelEvent.getDiskIndex());
		} else {
			dismountDisk(diskPanelEvent.getDiskIndex());
		} // if mount/dismount

		ifDisks.updateDisks(drives);

		Thread t_ifDisks = new Thread(ifDisks);
		t_ifDisks.start();
	}// doDiskpanelEvent

	private void mountDisk(int diskIndex) {
		if (drives[diskIndex] != null) {
			log.warnf("disk already mounted at index %d - %s%n", diskIndex, drives[diskIndex].getFilePath());
			return;
		} // if mounted already

		JFileChooser fc = FilePicker.getDisk();
		if (fc.showOpenDialog(ifDisks) == JFileChooser.CANCEL_OPTION) {
			log.info("Bailed out of the open");
			return;
		} // if

		String absolutePath = fc.getSelectedFile().getAbsolutePath();

		if (java.nio.file.Files.notExists(Paths.get(absolutePath))) {
			log.info("Selected Disk does not exists");
			return;
		} // if

		if (isDiskMounted(absolutePath)) {
			log.warn("Disk already mounted");
			return;
		} // already mounted
		
		 drives[diskIndex] = new DiskDrive(absolutePath);
		 drives[diskIndex].addVDiskErrorListener(adapterDCU);
		 log.infof("Mounted Disk - Index %d, Path: %s%n", diskIndex, absolutePath);

		// File selectedFile = fc.getSelectedFile();
		//
		// if (!selectedFile.exists()) {
		// log.info("Selected Disk does not exists");
		// return; // try again
		// } // if exists
		//
		// if (isDiskMounted(selectedFile)) {
		// log.warn("Disk already mounted");
		// return;
		// } // already mounted
		//
		// drives[diskIndex] = new DiskDrive(selectedFile.getAbsolutePath());
		// drives[diskIndex].addVDiskErrorListener(adapterDCU);
		// log.infof("Mounted Disk - Index %d, Path: %s%n", diskIndex, drives[diskIndex].getFilePath());
		return;
	}// mountDisk

	private void dismountDisk(int diskIndex) {
		if (drives[diskIndex] == null) {
			log.warn("No Disk to Dismount");
		} else {
			log.infof("Dismounted Disk - Index %d, Path: %s%n", diskIndex, drives[diskIndex].getFilePath());
			removeDiskDrive(diskIndex);
		} // if
	}// dismountDisk

	private void removeDiskDrive(int diskIndex) {
		if (drives[diskIndex] == null) {
			log.warnf("At index %d there is no Disk to Dismount%n", diskIndex);
		} else {
			drives[diskIndex].removeVDiskErrorListener(adapterDCU);
			drives[diskIndex].dismount();
			drives[diskIndex] = null;
		} // if - disk (not) mounted
	}// removeDiskDrive

	private void removeAllDiskDrives() {
		for (int i = 0; i < Disk.NUMBER_OF_DISKS; i++) {
			removeDiskDrive(i);
		} // for
	}// removeAllDiskDrives

	public void setCurrentDrive(int currentDrive) {
		if ((currentDrive >= 0) & (currentDrive < Disk.NUMBER_OF_DISKS)) {
			this.currentDrive = currentDrive;
		} else {
			log.errorf("unable to set current drive to index %d, there are %d total disks%n", currentDrive,
					Disk.NUMBER_OF_DISKS);
		} // if
	}// setCurrentDrive

	public int getCurrentDrive() {
		return currentDrive;
	}// getCurrentDrive

	public boolean isBootDiskLoaded() {
		return drives[0] != null ? true : false;
	}// isBootDiskLoaded

	public int getMaxNumberOfDrives() {
		return Disk.NUMBER_OF_DISKS;
	}// getMaxNumberOfDrives

	// public DiskDrive[] getDrives() {
	// return drives;
	// }// getDrives

	private void debugShowControlTable() {
		// log.infof("currentCommand: %02X%n", currentCommand);
		// log.infof("currentUnit: %02X%n", currentUnit);
		// log.infof("currentHead: %02X%n", currentHead);
		// log.infof("currentTrack: %02X%n", currentTrack);
		// log.infof("currentSector: %02X%n", currentSector);
		// log.infof("currentByteCount: %04X%n", currentByteCount);
		// log.infof("currentDMAAddress: %04X%n", currentDMAAddress);

		// log.infof("[DCU] Location: %04X, Value: %02X%n", currentDiskControlByte,
		// ioBuss.read(currentDiskControlByte));
		String command = currentCommand == COMMAND_READ ? "Read" : "Write";
		log.infof("\t%s - Unit %02X, Head %02X, Trk %02X, Sec %04X, Bytes %04X, DMA %04X%n", command, currentUnit,
				currentHead, currentTrack, currentSector, currentByteCount, currentDMAAddress);

	}// debugShowControlTable

	private void doUpdate(Object event) {
		if (((MemoryTrapEvent) event).getTrap().equals(Core.Trap.DEBUG)) {
			return; // Don't care
		} // if

		// we have an IO trap
		int trapLocation = ((MemoryTrapEvent) event).getLocation() & 0XFFF;
		currentDiskControlByte = trapLocation; // 0X0040 for 8" / 0X0045 for 5"

		if ((ioBuss.read(currentDiskControlByte) & 0X80) == 0) {
			return; // not a disk activation command
		} // if

		goodOperation = true; // assume all goes well

		// System.out.printf("DCU: Location: %04X, Value: %02X%n", currentDiskControlByte,
		// ioBuss.read(currentDiskControlByte));

		int controlTableLocation = cpuBuss.readWordReversed(currentDiskControlByte + 1);
		currentCommand = ioBuss.read(controlTableLocation + DCT_COMMAND);
		currentUnit = ioBuss.read(controlTableLocation + DCT_UNIT);
		currentHead = ioBuss.read(controlTableLocation + DCT_HEAD);
		currentTrack = ioBuss.read(controlTableLocation + DCT_TRACK);
		currentSector = ioBuss.read(controlTableLocation + DCT_SECTOR);
		currentByteCount = cpuBuss.readWordReversed(controlTableLocation + DCT_BYTE_COUNT);
		currentDMAAddress = cpuBuss.readWordReversed(controlTableLocation + DCT_DMA_ADDRESS);
		currentDrive = (currentDiskControlByte == DISK_CONTROL_BYTE_5) ? 0 : 2; // 5" => A or B
		currentDrive += currentUnit;

		if ((currentDrive < 0) || (currentDrive >= Disk.NUMBER_OF_DISKS)) {
			doDiskError(ERROR_NO_DRIVE, String.format("No unit %d", currentDrive));
			return;
		} // if

		if (drives[currentDrive] == null) {
			doDiskError(ERROR_NO_DISK, String.format(" No disk in unit %d", currentDrive));
			return;
		} // if

		int currentSectorSize = drives[currentDrive].getBytesPerSector();
		if (!drives[currentDrive].setCurrentAbsoluteSector(currentHead, currentTrack, currentSector)) {
			doDiskError(ERROR_SECTOR_NOT_SET, "Sector not set properly");
			return;
		} //
		int numberOfSectorsToMove = currentByteCount / currentSectorSize;
		if (numberOfSectorsToMove < 0) {
			doDiskError(ERROR_INVALID_BYTE_COUNT, String.format("Invalid Byte Count: %04X", currentByteCount));
			return;
		} // if - bad byte count

		// System.out.printf("DCU: Head: %d, Track: %d, Sector: %d AbsoluteSector: %d%n", currentHead, currentTrack,
		// currentSector, drives[currentDrive].getCurrentAbsoluteSector());
		if (!goodOperation) {
			return; // return if any problems - don't do any I/O
		} // if

		fireDCUAction(currentDrive, currentCommand); // notify the listeners

		// ----- now get to work

		if (currentCommand == COMMAND_READ) {
			ByteBuffer readByteBuffer = ByteBuffer.allocate(numberOfSectorsToMove * currentSectorSize);
			readByteBuffer.put(drives[currentDrive].read());
			for (int i = 0; i < numberOfSectorsToMove - 1; i++) {
				readByteBuffer.put(drives[currentDrive].readNext());
			} // for
			byte[] readBuffer = readByteBuffer.array();
			ioBuss.writeDMA(currentDMAAddress, readBuffer);
			// System.out.printf("DCU:Value: %02X, length = %d%n", readBuffer[1], readBuffer.length);
		} else if (currentCommand == COMMAND_WRITE) { // its a COMMAND_WRITE

			byte[] writeSector = new byte[currentSectorSize];
			byte[] readFromCore = ioBuss.readDMA(currentDMAAddress, currentByteCount);

			ByteBuffer writeByteBuffer = ByteBuffer.wrap(readFromCore);
			// ByteBuffer writeSectorBuffer =
			ByteBuffer.allocate(currentSectorSize);
			writeByteBuffer.get(writeSector);
			drives[currentDrive].write(writeSector);
			for (int i = 0; i < numberOfSectorsToMove - 1; i++) {
				writeByteBuffer.get(writeSector);
				drives[currentDrive].writeNext(writeSector);
			} // for

		} else {
			log.errorf("Bad Disk command: %02X%n", currentCommand);
			debugShowControlTable();
		} // if read or write
		if (goodOperation) {
			reportStatus((byte) 0X80, (byte) 00); // reset - operation is over
		} // if ok

	}// doUpdate

	/////////////////////////////

	public void close() {
		appClose();
	}// close

	private void appClose() {
		cpuBuss.removeTrap(DISK_CONTROL_BYTE_5, Core.Trap.IO);
		cpuBuss.deleteObserver(adapterDCU);
		ifDisks.removeDiskPanelActionListener(adapterDCU);
		removeAllDiskDrives();
	}// appClose

	private void appInit() {
		cpuBuss.addObserver(adapterDCU);
		cpuBuss.addTrap(DISK_CONTROL_BYTE_5, Core.Trap.IO);
		drives = new DiskDrive[Disk.NUMBER_OF_DISKS];
	}// appInit

	private DiskControlUnit() {
		appInit();
	}// Constructor

	////////////////////////////////////////////////////////////////////
	/* \/ Event Handling Routines \/ */

	private Vector<DCUActionListener> dcuActionListeners = new Vector<DCUActionListener>();

	public synchronized void addDCUActionListener(DCUActionListener dcuListener) {
		if (dcuActionListeners.contains(dcuListener)) {
			return; // Already has it
		} // if
		dcuActionListeners.addElement(dcuListener);
	}// addVDiskErroListener

	public synchronized void removeDCUActionListener(DCUActionListener dcuListener) {
		dcuActionListeners.remove(dcuListener);
	}// addVDiskErroListener

	@SuppressWarnings("unchecked")
	private void fireDCUAction(int diskIndex, int actionType) {
		Vector<DCUActionListener> actionListeners;
		synchronized (this) {
			actionListeners = (Vector<DCUActionListener>) dcuActionListeners.clone();
		} // sync
		int size = actionListeners.size();
		if (size == 0) {
			return; // no listeners
		} // if

		DCUActionEvent dcuActionEvent = new DCUActionEvent(this, diskIndex, actionType);
		for (int i = 0; i < size; i++) {
			DCUActionListener listener = (DCUActionListener) actionListeners.elementAt(i);
			listener.dcuAction(dcuActionEvent);
		} // for

	}// fireDCUAction

	/* /\ Event Handling Routines /\ */

	////////////////////////////////////////////////////////////////////

	private static final byte BYTE00 = (byte) 0x00;

	private static final int ERROR_NO_DISK = 10;
	private static final int ERROR_INVALID_SECTOR_DESIGNATOR = 11;
	private static final int ERROR_NO_DRIVE = 12;
	private static final int ERROR_SECTOR_NOT_SET = 13;
	// private static final int ERROR_INVALID_DMA_ADDRESS = 14;
	private static final int ERROR_INVALID_BYTE_COUNT = 15;
	//
	private static final byte COMMAND_READ = 01;
	private static final byte COMMAND_WRITE = 02;
	//
	// private static final int DISK_CONTROL_BYTE_8 = 0X0040;
	private static final int DISK_CONTROL_BYTE_5 = 0X0045;
	private static final int DISK_STATUS_BLOCK = 0X0043;
	//
	// Disk Control Table
	private static final int DCT_COMMAND = 0; // DB 1
	private static final int DCT_UNIT = 1; // DB 1
	private static final int DCT_HEAD = 2; // DB 1
	private static final int DCT_TRACK = 3; // DB 1
	private static final int DCT_SECTOR = 4; // DB 1
	private static final int DCT_BYTE_COUNT = 5; // DW 1
	private static final int DCT_DMA_ADDRESS = 7; // DW 1
	// private static final int DCT_NEXT_STATUS_BLOCK = 9; // DW 1
	// private static final int DCT_NEXT_DCT = 11; // DW 1

	//////////////////////////////////////////////

	class AdapterDCU implements Observer, VDiskErrorListener, DiskPanelEventListener {
		/* Observer */
		@Override
		public void vdiskError(VDiskErrorEvent vdee) {
			doDiskError(ERROR_INVALID_SECTOR_DESIGNATOR, vdee.getMessage());
		}// vdiskError

		/* VDiskErrorListener */
		@Override
		public void update(Observable o, Object event) {
			doUpdate(event);

		}// update

		/* DiskPanelEventListener */
		@Override
		public void diskPanelAction(DiskPanelEvent diskPanelEvent) {
			doDiskPanelEvent(diskPanelEvent);
		}// diskPanelAction

	}// class AdapterDCU

}// class DiskControlUnit
