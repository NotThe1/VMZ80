package disks;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import codeSupport.AppLogger;

public class DiskDrive {
	
	private String diskType;
	private boolean bootable;
	protected int heads;
	private int currentHead;
	protected int tracksPerHead;
	private int currentTrack;
	protected int sectorsPerTrack;
	private int currentSector;
	private int currentAbsoluteSector;
	protected int bytesPerSector;
	protected int sectorsPerHead;
	protected int totalSectorsOnDisk;
	protected long totalBytesOnDisk;
	private String fileAbsoluteName;
	private String fileLocalName;
	public String description;
	
	private FileChannel fileChannel;
	private MappedByteBuffer disk;
	private byte[] readSector;
	private ByteBuffer writeSector;

	private RandomAccessFile raf;
	
	private AppLogger log = AppLogger.getInstance();



	
	public DiskDrive(Path path) {
		this(path.toString());
	}// Constructor

	public DiskDrive(String strPathName) {
		resolveDiskType(strPathName);
		
		try {
			File file = new File(strPathName);
			
			raf = new RandomAccessFile(file,"rw");
			fileChannel = raf.getChannel();
			disk =fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileChannel.size());// total Bytes on disk
			fileAbsoluteName = file.toString();
			fileLocalName = file.getName();
			
		}catch (IOException ioException){
			log.addError("[DiskDrive]: " + ERR_IO + ioException.getMessage());
			fireVDiskError(1l,ERR_IO + ioException.getMessage());
		}//try
		readSector = new byte[bytesPerSector];
		writeSector = ByteBuffer.allocate(bytesPerSector);
	}// Constructor

	private void resolveDiskType(String strPathName) {
		Pattern patternFileType = Pattern.compile("\\.([^.]+$)");
		Matcher matcher = patternFileType.matcher(strPathName);
		String diskType = matcher.find() ? matcher.group(1).toLowerCase() : NONE;

		DiskMetrics diskMetric = DiskMetrics.getDiskMetric(diskType);
		if (diskMetric == null) {
			fireVDiskError((long) 2, "Not a Valid disk type " + diskType);
		} // if
		
		this.diskType = diskType;
		this.heads = diskMetric.heads;
		this.tracksPerHead = diskMetric.tracksPerHead;
		this.sectorsPerTrack = diskMetric.sectorsPerTrack;
		this.bytesPerSector = diskMetric.bytesPerSector;
		this.sectorsPerHead = diskMetric.getTotalSectorsPerHead();
		this.totalSectorsOnDisk = diskMetric.getTotalSectorsOnDisk();
		this.totalBytesOnDisk = diskMetric.getTotalBytes();
		this.bootable = diskMetric.isBootDisk();
		this.description = diskMetric.descriptor;
	}// resolveDiskType
	
	// ---------------------------------------
	
	public String getDiskType() {
		return this.diskType;
	}// getDiskType

	public String getFileAbsoluteName() {
		return this.fileAbsoluteName;
	}// getFileAbsoluteName


	public String getFileLocalName() {
		return this.fileLocalName;
	}// getFileLocalName



	// ---------------------------------------
	private Vector<VDiskErrorListener> vdiskErrorListeners = new Vector<VDiskErrorListener>();

	public synchronized void addVDiskErrorListener(VDiskErrorListener vdel) {
		if (vdiskErrorListeners.contains(vdel)) {
			return; // Already have it
		} // if
		vdiskErrorListeners.addElement(vdel);
	}// addVDiskErrorListener

	public synchronized void removeVDiskErrorListener(VDiskErrorListener vdel) {
		vdiskErrorListeners.remove(vdel);
	}// removeVDiskErrorListener

	@SuppressWarnings("unchecked")
	private void fireVDiskError(long value, String errorMessage) {
		
		int size = vdiskErrorListeners.size();
		if (size == 0) {
			return; // No Listeners
		} // if
		
		VDiskErrorEvent vdee = new VDiskErrorEvent(this, value, errorMessage);
		
		Vector<VDiskErrorListener> vdels;
		synchronized (this) {
			vdels = (Vector<VDiskErrorListener>) vdiskErrorListeners.clone();
		} // sync

		for (VDiskErrorListener listener : vdels) {
			listener.vdiskError(vdee);
		} // for
	}// fireVDiskError

	// private static final String DISK_TYPES = "(?i)"
	private static final String NONE = "<none>";

	private static final String ERR_TRACK = "Invalid Track";
	private static final String ERR_HEAD = "Invalid Head";
	private static final String ERR_SECTOR = "Invalid Sector";
	private static final String ERR_ABSOLUTE_SECTOR = "Invalid Absolute Sector";
	private static final String ERR_DISK = "Invalid Disk - ";
	private static final String ERR_SECTOR_SIZE = "Write buffer size does not match disk sector size";
	private static final String ERR_IO = "Physical I/O Error - ";

	

}// class DiskDrive
