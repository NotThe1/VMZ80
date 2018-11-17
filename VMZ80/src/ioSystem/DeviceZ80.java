package ioSystem;

import java.util.AbstractQueue;
/*
 * 2018-11-16 Changed to Queue for I/O
 */
import java.util.concurrent.ConcurrentLinkedQueue;

abstract public class DeviceZ80 {

	protected Byte addressIn;
	protected Byte addressOut;
	protected Byte addressStatus;

	protected AbstractQueue<Byte> dataToCPU;
	protected AbstractQueue<Byte> dataFromCPU;

	protected AbstractQueue<Byte> statusToCPU;
	protected AbstractQueue<Byte> statusFromCPU;

	private String name;

	public DeviceZ80(String name, Byte addressIn, Byte addressOut, Byte addressStatus) {
		this.name = name;
		this.addressIn = addressIn;
		this.addressOut = addressOut;
		this.addressStatus = addressStatus;
		this.dataToCPU = new ConcurrentLinkedQueue<Byte>();
		this.dataFromCPU = new ConcurrentLinkedQueue<Byte>();
		this.statusToCPU = new ConcurrentLinkedQueue<Byte>();
		this.statusFromCPU = new ConcurrentLinkedQueue<Byte>();
	}// Constructor

	public String getName() {
		return this.name;
	}// getName

	public void setName(String name) {
		this.name = name;
	}// setName

	public void setDataToCpuQueue(AbstractQueue<Byte> dataToCPU) {
		this.dataToCPU = dataToCPU;
	}// setDataToCpuQueue

	public void setDataFromCpuQueue(AbstractQueue<Byte> dataFromCPU) {
		this.dataFromCPU = dataFromCPU;
	}// setDataFromCpuQueue

	public void setStatusToCpuQueue(AbstractQueue<Byte> statusToCPU) {
		this.statusToCPU = statusToCPU;
	}// setStatusToCpuQueue

	public void setStatusFromCpuQueue(AbstractQueue<Byte> statusFromCPU) {
		this.statusFromCPU = statusFromCPU;
	}// setStatusFromCpuQueue

	public AbstractQueue<Byte> getDataToCPU() {
		return this.dataToCPU;
	}// getDataToCPU

	public AbstractQueue<Byte> getDataFromCPU() {
		return this.dataFromCPU;
	}// getDataFromCPU

	public AbstractQueue<Byte> getStatusToCPU() {
		return this.statusToCPU;
	}// getDataToCPU

	public AbstractQueue<Byte> getStatusFromCPU() {
		return this.statusFromCPU;
	}// getDataFromCPU

	 public Byte getAddressIn(){
		 return this.addressIn;
	 }//getAddressIn
	 
	 public Byte getAddressOut(){
		 return this.addressOut;
	 }//getAddressOut
	 
	 public Byte getAddressStatus(){
		 return this.addressStatus;
	 }//getAddressStatus
	 
//	abstract public Byte getAddressOut();
//
//	abstract public Byte getAddressStatus();

	abstract public void byteFromCPU(Byte value);

	abstract public void byteToCPU(Byte value);

//	abstract public void statusRequest(Byte value);
//
//	abstract public void statusResponse(Byte value);

	abstract public void close();

	abstract public void setVisible(boolean state);

	abstract public boolean isVisible();

	public static final String DATA_TO_CPU = "DataToCPU";
	public static final String DATA_FROM_CPU = "DataFromCPU";
	public static final String STATUS_TO_CPU = "StatusToCPU";
	public static final String STATUS_FROM_CPU = "StatusFromCPU";
}// class DeviceZ80A
