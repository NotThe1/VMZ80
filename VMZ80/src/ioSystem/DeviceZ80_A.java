package ioSystem;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

abstract public class DeviceZ80_A implements Runnable {

	protected PipedInputStream pipeOut;
	protected PipedOutputStream pipeIn, pipeStatus;

	private String name;

	public DeviceZ80_A(String name,  boolean input, Byte addressIn, boolean output, Byte addressOut,
			Byte addressStatus) {
		this.name = name;

	}// Constructor

	public DeviceZ80_A(String name,  Byte addressIn, Byte addressOut, Byte addressStatus) {
		this(name,  true, addressIn, true, addressOut, addressStatus);
	}// Constructor - all addresses

	public String getName() {
		return this.name;
	}// getName

	public void setName(String name) {
		this.name = name;
	}// setName

	public void setPipeIn(PipedOutputStream pipedOutputStream) {
		this.pipeIn = pipedOutputStream;
	}// setPipeIn

	public void setPipeOut(PipedInputStream pipedInputStream) {
		this.pipeOut = pipedInputStream;
	}// setpipeOut

	public void setPipeStatus(PipedOutputStream pipedOutputStream) {
		this.pipeStatus = pipedOutputStream;
	}// setpipeOut

	abstract public Byte getAddressIn();

	abstract public Byte getAddressOut();

	abstract public Byte getAddressStatus();

	abstract public void byteFromCPU(Byte value);

	abstract public void byteToCPU(Byte value);

	abstract public void close();

}// class DeviceZ80
