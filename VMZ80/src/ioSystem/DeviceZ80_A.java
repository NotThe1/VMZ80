package ioSystem;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

abstract public class DeviceZ80_A {

	protected PipedInputStream pipeIn_In, pipeOut_In, pipeStatus_In;
	protected PipedOutputStream pipeIn_Out, pipeOut_Out, pipeStatus_Out;

	private String name;
	private IOType type;

	public DeviceZ80_A(String name, IOType type, boolean input, Byte addressIn, boolean output, Byte addressOut,
			Byte addressStatus) {
		this.name = name;
		this.type = type;

	}// Constructor

	public DeviceZ80_A(String name, IOType type, Byte addressIn, Byte addressOut, Byte addressStatus) {
		this(name, type, true, addressIn, true, addressOut, addressStatus);
	}// Constructor - all addresses

	public String getName() {
		return this.name;
	}// getName

	public void setName(String name) {
		this.name = name;
	}// setName

	public void setPipesIn(PipedInputStream pipeIn, PipedOutputStream pipeOut) {
		this.pipeIn_In = pipeIn;
		this.pipeIn_Out = pipeOut;
	}// setPipeIn

	public void setPipesOut(PipedInputStream pipeIn, PipedOutputStream pipeOut) {
		this.pipeOut_In = pipeIn;
		this.pipeOut_Out = pipeOut;
	}// setpipeOut

	public void setPipesStatus(PipedInputStream pipeIn, PipedOutputStream pipeOut) {
		this.pipeStatus_In = pipeIn;
		this.pipeStatus_Out = pipeOut;
	}// setpipeOut

	abstract public void byteFromCPU(Byte address, Byte value);

	abstract public byte byteToCPU(Byte address);

	abstract public void close();

}// class DeviceZ80
