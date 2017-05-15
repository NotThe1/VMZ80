package memory;

import java.util.Observable;

//import memory.Core.Trap;

/**
 * 
 * @author Frank Martyn
 * @version 1.0
 * 
 *          <p>
 *          Core is the class that acts as the physical memory.
 *          <p>
 *          Core is a base level object. It represents the memory in a virtual machine.
 * 
 *          The access to the data is handled by 3 sets of read and write operations:
 * 
 *          1) read, write, readWord, writeWord, popWord and pushWord all participate in the monitoring of the locations
 *          read for Debug Trap and written for IO trap. 2) readForIO and writeForIO are to be used for byte memory
 *          access by devices. They do not engage the Trap system. 3) readDMA and writeDMA are for burst mode reading
 *          and writing. They also do not engage the Trap system.
 * 
 *          Changed from using traps to Observer/Observable for IO/DEBUG?INVALID notification Traps for IO are triggered
 *          by the writes. Debug traps are triggered by the reads in conjunction with the isDebugEnabled flag
 * 
 *          <p>
 *          this is constructed as a singleton
 * 
 * 
 * 
 *
 */

public class Core extends Observable implements ICore {
	private static Core instance = new Core();
	private  byte[] storage;
	private  int maxAddress;

	/**
	 * 
	 * @param size
	 *            The size of Memory to create first time
	 * @return The only instance of the core object
	 */
	public static Core getInstance() {
		return instance;
	}// getInstance


	private Core() {
		int size = 64 * 1024;
		storage = new byte[size];
		maxAddress = size - 1;
//		System.out.println("In core constructor");
	}// Constructor
		// <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>
	/**
	 * clears all memory locations to zeros
	 */
	public void initialize(){
		for ( int i = 0; i < maxAddress +1; i++){
			storage[i] = (byte)00;
		}//for
	}//initialize
	
	public  byte[] getStorage(){
		return this.storage;
	}//getStorage

	/**
	 * Gets memory size
	 * 
	 * @return the size of the memory in bytes
	 */
	@Override
	public int getSize() {
		return storage.length;
	}// getSize

	/**
	 * Gets memory size in K
	 * 
	 * @return the size of the memory in K (1024)
	 */
	@Override
	public int getSizeInK() {
		return storage.length / K;
	}// getSizeInBytes

	/**
	 * Returns the value found at the specified location, and checks for DEBUG
	 * 
	 * @param location
	 *            where to get the value from
	 * @return value found at the location, or a HLT command if this is the first access to a debug marked location
	 */

	@Override
	public byte read(int location) {
		return isValidAddress(location) ? storage[location] : (byte) 0X00;
	}// read

	@Override
	public void write(int location, byte value) {
		if (isValidAddress(location)) {
			storage[location] = value;
		}// if
		return; // bad address;
	}// write

	/**
	 * Confirms the location is in addressable memory.
	 * <p>
	 * Will fire an MemoryAccessError if out of addressable memory
	 * 
	 * @param location
	 *            - address to be checked
	 * @return true if address is valid
	 * 
	 */
	@Override
	public boolean isValidAddress(int location) {
		boolean checkAddress = true;
		if ((location < PROTECTED_MEMORY) | (location > maxAddress)) {
			checkAddress = false;
			MemoryTrapEvent mte = new MemoryTrapEvent(this, location, Trap.INVALID);
			setChanged();
			notifyObservers(mte);
			clearChanged();
		}// if
		return checkAddress;
	}// isValidAddress



	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	public enum Trap {
		IO, DEBUG, INVALID
	}// enum Trap

	static int K = 1024;
	static int PROTECTED_MEMORY = 0; // 100;
	static int MINIMUM_MEMORY = 16 * K;
	static int MAXIMUM_MEMORY = 64 * K;
	static int DEFAULT_MEMORY = 64 * K;

}// class Core
