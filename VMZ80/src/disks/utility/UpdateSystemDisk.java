package disks.utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import javax.swing.JFileChooser;

import codeSupport.AppLogger;
import disks.DiskMetrics;
import hardware.Z80Machine;
import memory.MemoryLoaderFromFile;
import utilities.filePicker.FilePicker;

public class UpdateSystemDisk {
	
	static AppLogger log = AppLogger.getInstance();

	public static void updateDisk(File selectedFile) {
		String fileExtension = "F3HD";
		DiskMetrics diskMetric = DiskMetrics.getDiskMetric(fileExtension);
		if (diskMetric == null) {
			System.err.printf("Bad disk type: %s%n", fileExtension);
			return;
		} // if diskMetric

		try (FileChannel fileChannel = new RandomAccessFile(selectedFile, "rw").getChannel();) {
			MappedByteBuffer disk = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, diskMetric.getTotalBytes());

			/** set up as system disk **/
			Class<Z80Machine> thisClass = Z80Machine.class;
			/* Boot Sector */
			// URL rom = thisClass.getResource("/disks/resources/BootSector.mem");

			InputStream in = thisClass.getClass().getResourceAsStream("/workingOS/BootSector.mem");
//			InputStream in = thisClass.getClass().getResourceAsStream("/Z80Code/BootSector.mem");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			byte[] dataBoot = MemoryLoaderFromFile.loadMemoryImage(reader, 0x0200);
			disk.position(0);
			disk.put(dataBoot);

			in = thisClass.getClass().getResourceAsStream("/Z80Code/CCP.mem");
//			in = thisClass.getClass().getResourceAsStream("/workingOS/CCP.mem");
			reader = new BufferedReader(new InputStreamReader(in));
			byte[] dataCCP = MemoryLoaderFromFile.loadMemoryImage(reader, 0x0800);
			disk.put(dataCCP);

			in = thisClass.getClass().getResourceAsStream("/Z80Code/BDOS.mem");
//			in = thisClass.getClass().getResourceAsStream("/workingOS/BDOS.mem");
			reader = new BufferedReader(new InputStreamReader(in));
			byte[] dataBDOS = MemoryLoaderFromFile.loadMemoryImage(reader, 0x0E00);
			disk.put(dataBDOS);

			in = thisClass.getClass().getResourceAsStream("/Z80Code/BIOS.mem");
//			in = thisClass.getClass().getResourceAsStream("/workingOS/BIOS.mem");
			reader = new BufferedReader(new InputStreamReader(in));
			byte[] dataBIOS = MemoryLoaderFromFile.loadMemoryImage(reader, 0x0A00);
			disk.put(dataBIOS);

			fileChannel.force(true);
			fileChannel.close();
			disk = null;
		} catch (IOException e) {
			e.printStackTrace();
		} // try
	}

	public static void updateDisk(String diskPath) {

		File selectedFile = new File(diskPath);
		if (!selectedFile.exists()) {
			System.err.printf("this file does not exist: %s%n", diskPath);
			return;
		} // if

		updateDisk(selectedFile);

	}// updateDisk

	public static void updateDisks() {
		JFileChooser fc = FilePicker.getDisks();
//		JFileChooser fc = FilePicker.getDiskPicker();
//		fc.setMultiSelectionEnabled(true); // Override the default single selection.
		if (fc.showOpenDialog(null) == JFileChooser.CANCEL_OPTION) {
			return;
		} // if

		File[] files = fc.getSelectedFiles();
		for (File file : files) {
			log.info("Updated System on " + file.toString());
//			System.out.printf("File: %s%n", file);
			updateDisk(file);
		}

	}// updateDisks

}// class UpdateSystemDisk
