package disks;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFileChooser;

import codeSupport.AppLogger;
import disks.diskPanel.DiskPanelEvent;
import disks.diskPanel.DiskPanelEventListener;
import disks.diskPanel.V_IF_DiskPanel;
import memory.Core;
import memory.CpuBuss;
import memory.IoBuss;
import utilities.filePicker.FilePicker;

public class DiskControlUnit {
	AdapterDCU adapterDCU = new AdapterDCU();
	private AppLogger log = AppLogger.getInstance();
	
	private V_IF_DiskPanel ifDisks;

	private CpuBuss cpuBuss = CpuBuss.getInstance();
	private IoBuss ioBuss = IoBuss.getInstance();
	private DiskDrive[] drives;
	// private int maxNumberOfDrives;

	// private int currentDrive;
	private int currentDiskControlByte;
	// private byte currentCommand;
	// private int currentUnit;
	// private int currentHead;
	// private int currentTrack;
	// private int currentSector;
	// private int currentByteCount;
	// private int currentDMAAddress;
	private boolean goodOperation;

	private static DiskControlUnit instance = new DiskControlUnit();

	public static DiskControlUnit getInstance() {
		return instance;
	}// getInstance
	
//	public static DiskControlUnit getInstance(V_IF_DiskPanel ifDisks) {
//		ifDisks = ifDisks;
//		ifDisks.addDiskPanelActionListener(adapterDCU);
//		return instance;
//	}
	
public void setDisplay(V_IF_DiskPanel ifDisks) {
	this.ifDisks=ifDisks;
	ifDisks.addDiskPanelActionListener(adapterDCU);
}//
	
	private boolean isDiskMounted(File newFile) {
		boolean ans = false;
		for(int i = 0; i <Disk.NUMBER_OF_DISKS;i++) {
			if(drives[i]==null) {
				continue;
			}//if null
			if(newFile.getAbsolutePath().equals(drives[i].getFilePath())) {
				ans = true;
				break;
			}//if
		}//for
		return ans;
	}//isDiskMounted


	private void reportStatus(byte firstCode, byte secondCode) {
		ioBuss.write(DISK_STATUS_BLOCK, firstCode);
		ioBuss.write(DISK_STATUS_BLOCK + 1, secondCode);
		ioBuss.write(currentDiskControlByte, BYTE00); // reset - operation
	}// reportStatus

	private void doDiskError(int errorCode, String message) {
		log.errorf("DCU error: -5D -%s%n", errorCode, message);
		goodOperation = false;
		reportStatus(BYTE00, BYTE00);
	}// doDiskError

	private void doMemoryTrap() {

	}// doMemoryTrap
	
	private void doDiskPanelEvent(DiskPanelEvent diskPanelEvent) {

		if (diskPanelEvent.isSelected()) {
			mountDisk(diskPanelEvent.getDiskIndex());
		}else {
			dismountDisk(diskPanelEvent.getDiskIndex());			
		}//if mount/dismount
		
		ifDisks.updateDisks(drives);
		
		Thread t_ifDisks = new Thread(ifDisks);
		t_ifDisks.start();
	}//doDiskpanelEvent
	
	private void mountDisk(int diskIndex) {
		if(drives[diskIndex] !=null) {
			log.warnf("disk already mounted at index %d - %s%n", diskIndex,drives[diskIndex].getFilePath());
			return;
		}//if mounted already
		
		JFileChooser fc = FilePicker.getDiskPicker();
		if (fc.showOpenDialog(null) == JFileChooser.CANCEL_OPTION) {
			log.info("Bailed out of the open");
			return;
		} // if

		File selectedFile = fc.getSelectedFile();
		
		if (!selectedFile.exists()) {
			log.info("Selected Disk does not exists");
			return; // try again
		} //if exists
		
		if (isDiskMounted(selectedFile)) {
			log.warn("Disk already mounted");
			return;
		}// already mounted
		
		drives[diskIndex] = new DiskDrive(selectedFile.getAbsolutePath());
		drives[diskIndex].addVDiskErrorListener(adapterDCU);
		log.infof("Mounted Disk - Index %d, Path: %s%n", diskIndex,drives[diskIndex].getFilePath());
		return;		
	}//mountDisk
	
	private void dismountDisk(int diskIndex) {
		if (drives[diskIndex] ==null) {
			log.warn("No Disk to Dismount");
		}else {
			log.infof("Dismounted Disk - Index %d, Path: %s%n", diskIndex,drives[diskIndex].getFilePath());
			removeDiskDrive(diskIndex);
		}//if
		
	}//dismountDisk
	

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

	/////////////////////////////

	public void close() {
		appClose();
	}// close

	private void appClose() {
		cpuBuss.removeTrap(DISK_CONTROL_BYTE_5, Core.Trap.IO);
		cpuBuss.deleteObserver(adapterDCU);
		ifDisks.removeDiskPanelActionListener(adapterDCU);
		removeAllDiskDrives();
	}//

	private void appInit() {
		cpuBuss.addObserver(adapterDCU);
		cpuBuss.addTrap(DISK_CONTROL_BYTE_5, Core.Trap.IO);
		drives = new DiskDrive[Disk.NUMBER_OF_DISKS];
	}// appinit

	private DiskControlUnit() {
		appInit();
	}// Constructor

	private static final byte BYTE00 = (byte) 0x00;

	// private static final int ERROR_NO_DISK = 10;
	private static final int ERROR_INVALID_SECTOR_DESIGNATOR = 11;
	// private static final int ERROR_NO_DRIVE = 12;
	// private static final int ERROR_SECTOR_NOT_SET = 13;
	// private static final int ERROR_INVALID_DMA_ADDRESS = 14;
	// private static final int ERROR_INVALID_BYTE_COUNT = 15;
	//
	// private static final byte COMMAND_READ = 01;
	// private static final byte COMMAND_WRITE = 02;
	//
	// private static final int DISK_CONTROL_BYTE_8 = 0X0040;
	private static final int DISK_CONTROL_BYTE_5 = 0X0045;
	private static final int DISK_STATUS_BLOCK = 0X0043;
	// // Disk Control Table
	// private static final int DCT_COMMAND = 0; // DB 1
	// private static final int DCT_UNIT = 1; // DB 1
	// private static final int DCT_HEAD = 2; // DB 1
	// private static final int DCT_TRACK = 3; // DB 1
	// private static final int DCT_SECTOR = 4; // DB 1
	// private static final int DCT_BYTE_COUNT = 5; // DW 1
	// private static final int DCT_DMA_ADDRESS = 7; // DW 1
	// private static final int DCT_NEXT_STATUS_BLOCK = 9; // DW 1
	// private static final int DCT_NEXT_DCT = 11; // DW 1

	//////////////////////////////////////////////

	class AdapterDCU implements Observer, VDiskErrorListener,DiskPanelEventListener {
		/* Observer */
		@Override
		public void vdiskError(VDiskErrorEvent vdee) {
			doDiskError(ERROR_INVALID_SECTOR_DESIGNATOR, vdee.getMessage());
		}// vdiskError

		/* VDiskErrorListener */
		@Override
		public void update(Observable o, Object arg) {
			// TODO Auto-generated method stub

		}// update

		/* DiskPanelEventListener */
		@Override
		public void diskPanelAction(DiskPanelEvent diskPanelEvent) {
			 doDiskPanelEvent( diskPanelEvent);	
		}//diskPanelAction

	}// class AdapterDCU

}// class DiskControlUnit
