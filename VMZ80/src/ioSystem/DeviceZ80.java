package ioSystem;

/*
 *       2018-09-21    changed the interface to accommodate the devices ability to respond to status queries
 */

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

abstract public class DeviceZ80 {

	protected PipedOutputStream dataFromCpuSender;
	protected PipedInputStream dataFromCpuReceiver;

	protected PipedOutputStream dataToCpuSender;
	protected PipedInputStream dataToCpuReceiver;

	protected PipedOutputStream statusRequestSender;
	protected PipedInputStream statusRequestReceiver;

	protected PipedOutputStream statusResponseSender;
	protected PipedInputStream statusResponseReceiver;

	private String name;

	public DeviceZ80(String name, boolean input, Byte addressIn, boolean output, Byte addressOut, Byte addressStatus) {
		this.name = name;

	}// Constructor

	public DeviceZ80(String name, Byte addressIn, Byte addressOut, Byte addressStatus) {
		this(name, true, addressIn, true, addressOut, addressStatus);
	}// Constructor - all addresses

	public String getName() {
		return this.name;
	}// getName

	public void setName(String name) {
		this.name = name;
	}// setName

	public void setDataFromCpuSender(PipedOutputStream sender) {
		this.dataFromCpuSender = sender;
	}// setDataFromCpuPipeOut

	public void setDataFromCpuReceiver(PipedInputStream receiver) {
		this.dataFromCpuReceiver = receiver;
	}// setDataFromCpuPipeIn

	public void setDataToCpuSender(PipedOutputStream sender) {
		this.dataToCpuSender = sender;
	}// setDataFromCpuPipeOut

	public void setDataToCpuReceiver(PipedInputStream receiver) {
		this.dataToCpuReceiver = receiver;
	}// setDataFromCpuPipeIn

	public void setStatusRequestSender(PipedOutputStream sender) {
		this.statusRequestSender = sender;
	}// setStatusFromCpuPipeOut

	public void setStatusRequestReceiver(PipedInputStream receiver) {
		this.statusRequestReceiver = receiver;
	}// setStatusFromCpuPipeIn

	public void setStatusResponseSender(PipedOutputStream sender) {
		this.statusResponseSender = sender;
	}// setStatusFromCpuPipeOut

	public void setStatusResponReceiver(PipedInputStream receiver) {
		this.statusResponseReceiver = receiver;
	}// setStatusFromCpuPipeIn

	abstract public Byte getAddressIn();

	abstract public Byte getAddressOut();

	abstract public Byte getAddressStatus();

	abstract public void byteFromCPU(Byte value);

	abstract public void byteToCPU(Byte value);

	abstract public void statusRequest(Byte value);

	abstract public void statusResponse(Byte value);

	abstract public void close();
	
	abstract public void setVisible(boolean state);
	
	abstract public boolean isVisible();

}// class DeviceZ80
