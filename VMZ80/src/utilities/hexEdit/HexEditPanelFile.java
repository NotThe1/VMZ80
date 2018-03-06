package utilities.hexEdit;

import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.SortedMap;

public class HexEditPanelFile extends HexEditPanelBase {
	public HexEditPanelFile() {
		setPreferredSize(new Dimension(825, 400));
		setMinimumSize(new Dimension(825, 400));
	}

	private static final long serialVersionUID = 1L;
	private FileChannel fileChannel;
	private MappedByteBuffer fileMap;

	// ---------------------------------------------------------------
	
	
	// ---------------------------------------------------------------
	public void loadData(Object src) {
		closeFile();
		File sourceFile = (File) src;
		long sourceLength = sourceFile.length();
		if (sourceLength >= Integer.MAX_VALUE){
			System.out.printf("[HexEditPanelFile : loadData] file too large %,d%n", sourceLength);
			return;
		}//if
		
//		System.out.printf("[loadData] sourceLength %,d%n", sourceLength);
//		System.out.printf("[loadData] Max Value %,d  [%08X]%n", Integer.MAX_VALUE, Integer.MAX_VALUE);
		try {
			fileChannel = new RandomAccessFile(sourceFile, "rw").getChannel();
			fileMap = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileChannel.size());// this.totalBytesOnDisk);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//try

		
		this.source = fileMap.asReadOnlyBuffer();
		
		loadDataCommon(source.capacity());

		
	}// loadDocument
	
	public void closeFile(){
		try {
			if (fileChannel != null) {
				fileChannel.close();
			} //if
		} catch (IOException e) {
			e.printStackTrace();
		}// try
	}

	public byte[] unloadData() {
		return applyChanges(source.array(), source.limit(), 0);
	}// unloadDocument

	public boolean isDataChanged() {
		return !changes.isEmpty();
	}// isDataChanges

	public SortedMap<Integer, Byte> getChangedData() {
		return changes;
	}// getChangedData

}// class hexEditPanelSimple
