package memory;

import java.util.List;

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

public interface IcpuBuss {

	/**
	 * Add a location to the trap list and identifies it type
	 * 
	 * @param location
	 *            where to set the trap
	 * @param trap
	 *            what kind of trap - IO or Debug
	 */
	public void addTrap(int location, Trap trap);

	/**
	 * RemoveTraps removes all traps of a specified type from trap list
	 * 
	 * @param trap
	 *            Type of trap to remove - IO or Debug
	 */
	public void removeTraps(Trap trap);
	
	/**
	 * Returns an array of all debug trap locations
	 * 
	 * @return array of debug locations
	 */
	public void removeTrap(int location, Trap trap);
	
	/**
	 * Removes a specific trap at one location
	 * 
	 * @param location
	 *            remove entry from trap list
	 * @param trap
	 *            the kind of trap to remove - IO or Debug
	 */
	public List<Integer> getTraps();
	
	/**
	 * Allows the debugging trapping to occur
	 * 
	 * @param state
	 *            true to enable debugging
	 */
	public void setDebugTrapEnabled(boolean state);

	/**
	 * Returns an array of all traps of a specified type
	 * 
	 * @param trap
	 *            type of trap - IO or Debug
	 * @return ArrayList of traps specified by type
	 */
	public List<Integer> getTraps(Trap trap);

	/**
	 * Reads bytes from location and location +1. Primarily used for stack work.Reads the locations opposite to the way
	 * readWord does.  Does not check for traps
	 * 
	 * @param location
	 *            - location contains lo byte, location + 1 contains hi byte
	 * @return word - 16 bit value
	 */
	public int popWord(int location);

	/**
	 * Writes bytes in location -1 and location-2. Primarily used for stack work.
	 *  Does not check for traps
	 * 
	 * @param location
	 *            1 higher than actual memory address that will be written
	 * @param hiByte
	 *            - goes into location -1
	 * @param loByte
	 *            - goes into location -2
	 */
	public void pushWord(int location, int value);
	/**
	 * Writes bytes in location -1 and location-2. Primarily used for stack work.
	 *  Does not check for traps
	 * 
	 * @param location
	 *            1 higher than actual memory address that will be written
	 * @param value
	 *            - goes into location -1  & location -1
	 * 
	 */
	public void pushWord(int location, byte hiByte, byte loByte);

	/**
	 * Returns a word value (16 bits)
	 *   Does not check for traps
	 * 
	 * @param location
	 *            - location contains hi byte, location + 1 contains lo byte
	 * @return word - 16 bit value
	 */
	public int readWord(int location);

	/**
	 * Reverses the order of the immediate word byte 2 is lo byte byte 3 is hi byte
	 * 
	 * @param location
	 *            - starting place in memory to find vale
	 * @return word as used by calls and jumps
	 */
	public int readWordReversed(int location);

	/**
	 * Write a word (16) bits to memory
	 *   Does not check for traps
	 * 
	 * @param location
	 *            starting place in memory for the write
	 * @param hiByte
	 *            - first byte to write, at location
	 * @param loByte
	 *            - second byte to write, at location + 1
	 */
	public void writeWord(int location, byte hiByte, byte loByte);
}// interface IcpuBuss
