package memory;

import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.stream.Collectors;

import memory.Core.Trap;

/*
 * @author Frank Martyn
 *  June 2016
 ** @version 1.0
 *  <p> This interface describes the actions required to support access to memory used by
 *  the CPU. 
 *  <p> Trap handling is monitored by the CPU-Memory interface.
 *  Debug - allows the CPU to stop execution at predefined locations when the debug flag is enabled.
 *  I/O - monitors specific location to start Disk I/O operations 
 *  
 *  The CPU also requires word sized transfer of data. along with Push and Pop operations
 */
public class CpuBuss extends Observable implements ICore, IcpuBuss {
	private static CpuBuss instance = new CpuBuss();
	private Core core;
	private HashMap<Integer, Trap> traps;

	private boolean isDebug = false;
	private boolean isDebugEnabled = false;

	private static final byte DEBUG_CODE = (byte) 0X30; // DEBUG opCode

	public static CpuBuss getInstance() {
		return instance;
	}// getInstance

	private CpuBuss() {
		core = Core.getInstance();
		traps = new HashMap<Integer, Trap>();
	}// Constructor
		// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * Returns the value found at the specified location, and checks for DEBUG, if
	 * 
	 * @param location
	 *            where to get the value from
	 * @return value found at the location, or a HLT command if this is the first access to a debug marked location
	 */
	public synchronized byte read(int location) {
		byte ans = core.read(location);
		// skip if debug flag is false
		if (isDebugEnabled) {
			// skip if not a debug location
			if (isDebugLocation(location)) {
				if (!isDebug) {// is this the first encounter ?
					isDebug = true; // then set the flag
					tellObservers(location, Trap.DEBUG);
					ans = DEBUG_CODE; // replace with fake halt
				} else {
					isDebug = false; // else reset set the flag and return the actual value
				} // inner if
					// may want to fire trap - fireMemoryTrap(location, Trap.DEBUG);
			} // inner if
		} // outer if

		return ans;
	}// read

	/**
	 * Places value into memory and check for IO trap
	 * 
	 * @param location
	 *            where to put the value in memory
	 * @param value
	 *            what to put into memory
	 */
	public synchronized void write(int location, byte value) {
		core.write(location, value);

		if (isDiskTrapLocation(location, value)) {
			tellObservers(location, Trap.IO);
			// fireMemoryTrap(location, Trap.IO);
		} // if
	}// write

	/**
	 * Notify the registered observers
	 * 
	 * @param location
	 *            where the trap occured
	 * @param trap
	 *            Type of trap (IO or Debug)
	 */
	private void tellObservers(int location, Trap trap) {
		MemoryTrapEvent mte = new MemoryTrapEvent(core, location, trap);
		setChanged();
		notifyObservers(mte);
		clearChanged();
	}// tellObservers
		// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * 
	 * <p>
	 * Checks to see if this location is marked for debugging.
	 * <p>
	 * we only want to stop the machine on the first encounter, the second time will be the resumption of execution
	 * 
	 * @param location
	 *            location to check
	 * @return true if the program is to halt
	 */
	private boolean isDebugLocation(int location) {
		Trap t = traps.get(location);
		
//		if (t.equals(Trap.DEBUG))
//			return true;
//		return false;
		
		if ((t == null) || !t.equals(Trap.DEBUG))
			return false;

		return true;

		// return traps.get(location).equals(Trap.DEBUG) ? true : false;

	}// isDebugLocation

	/**
	 * Let the write know if at an IO trap location
	 * 
	 * @param location
	 * @param value
	 * @return
	 */
	private boolean isDiskTrapLocation(int location, byte value) {
		if (traps.containsKey(location)) {
			Trap thisTrap = traps.get(location);
			return thisTrap.equals(Trap.IO) ? true : false;
		} else {
			return false;
		} //
	}// isDiskTrapLocation

	/**
	 * Add a location to the trap list and identifies it type
	 * 
	 * @param location
	 *            where to set the trap
	 * @param trap
	 *            what kind of trap - IO or Debug
	 */
	public void addTrap(int location, Trap trap) {
		if (isValidAddress(location)) {
			traps.put(location, trap); // may be different trap type
		} // if

	}// addTrapLocation

	/**
	 * Removes a specific trap at one location
	 * 
	 * @param location
	 *            remove entry from trap list
	 * @param trap
	 *            the kind of trap to remove - IO or Debug
	 */
	public void removeTrap(int location, Trap trap) {
		traps.remove(location, trap);
	}// removeTrapLocation

	/**
	 * RemoveTraps removes all traps of a specified type from trap list
	 * 
	 * @param trap
	 *            Type of trap to remove - IO or Debug
	 */
	public void removeTraps(Trap trap) {
		traps.entrySet().removeIf(s -> s.getValue().equals(trap));
	}// removeTraps

	/**
	 * Returns an array of all debug trap locations
	 * 
	 * @return array of debug locations
	 */
	public List<Integer> getTraps() {
		return getTraps(Trap.DEBUG);
	}// getTrapLocations - DEBUG

	/**
	 * Returns an array of all traps of a specified type
	 * 
	 * @param trap
	 *            type of trap - IO or Debug
	 * @return ArrayList of traps specified by type
	 */
	public List<Integer> getTraps(Trap trap) {
		List<Integer> getTrapLocations = traps.entrySet().stream().filter((t) -> t.getValue().equals(trap))
				.map((t) -> t.getKey()).collect(Collectors.toList());

		return getTrapLocations;
	}// getTrapLocations

	@Override
	public void setDebugTrapEnabled(boolean state) {
		this.isDebugEnabled = state;
	}// setDebugTrapEnabled

	/**
	 * Reads bytes from location and location +1. Primarily used for stack work.Reads the locations opposite to the way
	 * readWord does. Does not check for traps
	 * 
	 * @param location
	 *            - location contains lo byte, location + 1 contains hi byte
	 * @return word - 16 bit value
	 */
	@Override
	public int popWord(int location) {
		int loByte = (int) core.read(location ) & 0X00FF;
		int hiByte = (int) (core.read(location+ 1) << 8) & 0XFF00;
		return 0XFFFF & (hiByte + loByte);
	}// popWord used for stack work

	/**
	 * Writes bytes in location -1 and location-2. Primarily used for stack work. Does not check for traps
	 * 
	 * @param location
	 *            1 higher than actual memory address that will be written
	 * @param value
	 *            - goes into location -1 & location -2
	 */
	@Override
	public void pushWord(int location, int value) {
		byte hiByte = (byte) ((value & 0XFF00) >> 8);
		byte loByte = (byte) (value & 0X00FF);
		pushWord(location, hiByte, loByte);
	}// pushWord used for stack work

	/**
	 * Writes bytes in location -1 and location-2. Primarily used for stack work. Does not check for traps
	 * 
	 * @param location
	 *            1 higher than actual memory address that will be written
	 * @param hiByte
	 *            - goes into location -1
	 * @param loByte
	 *            - goes into location -2
	 */
	@Override
	public void pushWord(int location, byte hiByte, byte loByte) {
		core.write(location - 1, hiByte);
		core.write(location - 2, loByte);
	}// pushWord used for stack work

	/**
	 * Returns a word value (16 bits) Does not check for traps
	 * 
	 * @param location
	 *            - location contains hi byte, location + 1 contains lo byte
	 * @return word - 16 bit value
	 */
	@Override
	public int readWord(int location) {
		int hiByte = (core.read(location) << 8) & 0XFF00;
		int loByte = core.read(location + 1) & 0X00FF;
		return 0XFFFF & (hiByte + loByte);
	}// readWord

	/**
	 * Reverses the order of the immediate word byte 2 is lo byte byte 3 is hi byte Does not check for traps
	 * 
	 * @param location
	 *            - starting place in memory to find vale
	 * @return word as used by calls and jumps
	 */
	@Override
	public int readWordReversed(int location) {
		int loByte = (core.read(location + 1) << 8) & 0XFF00;
		int hiByte = core.read(location) & 0X00FF;
		return 0XFFFF & (hiByte + loByte);
	}// readWordReversed

	/**
	 * Write a word (16) bits to memory
	 * 
	 * @param location
	 *            starting place in memory for the write
	 * @param hiByte
	 *            - first byte to write, at location
	 * @param loByte
	 *            - second byte to write, at location + 1
	 */
	@Override
	public void writeWord(int location, byte hiByte, byte loByte) {
		this.write(location, hiByte);
		this.write(location + 1, loByte);
	}// writeWord

	/**
	 * Gets memory size
	 * 
	 * @return the size of the memory in bytes
	 */
	@Override
	public int getSize() {
		return core.getSize();
	}// getSize

	/**
	 * Gets memory size in K
	 * 
	 * @return the size of the memory in K (1024)
	 */
	@Override
	public int getSizeInK() {
		return core.getSizeInK();
	}// getSizeInK

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
		return core.isValidAddress(location);
	}// isValidAddress

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++
}// class CpuBuss
