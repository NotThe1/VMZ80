package utilities.hexEdit;

import java.util.EventObject;

public class HexSourceChangeEvent extends EventObject {
	private static final long serialVersionUID = 1L;
	private int location;
	private byte value;

	public HexSourceChangeEvent(HexEditDocumentFilter source,int location, byte value) {
		super(source);
		this.location= location;
		this.value = value;
	}//Constructor

	public int getLocation() {
		return location;
	}//getLocation

	public byte getValue() {
		return value;
	}//getValue

}//class HexSourceChangeEvent
