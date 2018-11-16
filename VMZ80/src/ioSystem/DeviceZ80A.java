package ioSystem;

import java.util.AbstractQueue;
/*
 * 2018-11-16 Changed to Queue for I/O
 */

abstract public class DeviceZ80A {
	
	protected byte addressIn;
	protected byte addressOut;
	protected byte addressStatus;
	

	protected AbstractQueue<Byte> dataToCPU;
	protected AbstractQueue<Byte> dataFromCPU;

	protected AbstractQueue<Byte> statusToCPU;
	protected AbstractQueue<Byte> statusFromCPU;

	private String name;
	
	public DeviceZ80A(String name,byte addressIn,byte addressOut,byte addressStatus,AbstractQueue<Byte> dataToCPU,AbstractQueue<Byte> dataFromCPU,
			AbstractQueue<Byte> statusToCPU,AbstractQueue<Byte> statusFromCPU) {
		this.name = name;
		this.addressIn=addressIn;
		this.addressOut=addressOut;
		this.addressStatus=addressStatus;
		this.dataToCPU=dataToCPU;
		this.dataFromCPU=dataFromCPU;
		this.statusToCPU=statusToCPU;
		this.statusFromCPU=statusFromCPU;
	}// Constructor

//	public DeviceZ80A(String name, boolean input, Byte addressIn, boolean output, Byte addressOut, Byte addressStatus) {
//		this.name = name;
//
//	}// Constructor
//
//	public DeviceZ80A(String name, Byte addressIn, Byte addressOut, Byte addressStatus) {
//		this(name, true, addressIn, true, addressOut, addressStatus);
//	}// Constructor - all addresses

	public String getName() {
		return this.name;
	}// getName

	public void setName(String name) {
		this.name = name;
	}// setName

	public void setDataToCpuQueue(AbstractQueue<Byte> dataToCPU) {
		this.dataToCPU=dataToCPU;
	}// setDataToCpuQueue

	public void setDataFromCpuQueue(AbstractQueue<Byte> dataFromCPU) {
		this.dataFromCPU=dataFromCPU;
	}// setDataFromCpuQueue

	public void setStatusToCpuQueue(AbstractQueue<Byte> statusToCPU) {
		this.statusToCPU=statusToCPU;
	}// setStatusToCpuQueue

	public void setStatusFromCpuQueue(AbstractQueue<Byte> statusFromCPU) {
		this.statusFromCPU=statusFromCPU;
	}// setStatusFromCpuQueue
	
	public AbstractQueue<Byte> getDataToCPU(){
		return this.dataToCPU;
	}//getDataToCPU
	
	public AbstractQueue<Byte> getDataFromCPU(){
		return this.dataFromCPU;
	}//getDataFromCPU

	public AbstractQueue<Byte> getStatusToCPU(){
		return this.statusToCPU;
	}//getDataToCPU
	
	public AbstractQueue<Byte> getStatusFromCPU(){
		return this.statusFromCPU;
	}//getDataFromCPU


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

	public static final String DATA_TO_CPU = "DataToCPU";
	public static final String DATA_FROM_CPU = "DataFromCPU";
	public static final String STATUS_TO_CPU = "StatusToCPU";
	public static final String STATUS_FROM_CPU = "StatusFromCPU";
}// class DeviceZ80A
