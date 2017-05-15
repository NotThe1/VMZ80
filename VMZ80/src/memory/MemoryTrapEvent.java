package memory;

import java.util.EventObject;

import memory.Core.Trap;
/**
 * 
 * @author Frank Martyn
 * @version 1.0
 *<p> captures the data about memory Traps for both IO and Debug
 */
public class MemoryTrapEvent extends EventObject {
	
	private static final long serialVersionUID = 1L;
	private int location;
	private Trap trap;
/**
 * 
 * @param source Object making and sending the event
 * @param location Where in memory the event originated
 * @param trap the type of Trap. IO or Debug
 */
	public MemoryTrapEvent(Core source, int location,Core.Trap trap) {
		super(source);
		this.location = location;
		this.trap = trap;				
	}//Constructor - MemoryTrapEvent
/**
 * 
 * @return Where in memory the event originated
 */
	public int getLocation(){
		return location;
	}//getLocation
/**
 * 	
 * @return trap the type of Trap. IO or Debug
 */
	public Trap getTrap(){
		return trap;
	}//getType
/**
 * 	
 * @return String with trap type(IO or Debug) and where it originated
 */
	public String getMessage(){
		return String.format("Error type: %s%n location: 0X%04X", trap.toString(),location);
	}//getMessage
	
}//class MemoryEvent

