package ioSystem;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import codeSupport.AppLogger;
import ioSystem.listDevice.ListDevice;
import ioSystem.ttyZ80.TTYZ80;

public class IOController {
	private AppLogger log = AppLogger.getInstance();
	private static IOController instance = new IOController();

	private Set<DeviceZ80> devicePopulation = new HashSet<>();

	private HashMap<Byte, DevicePipes> devicesInput = new HashMap<>();
	private HashMap<Byte, DevicePipes> devicesOutput = new HashMap<>();
	private HashMap<Byte, DevicePipes> devicesStatus = new HashMap<>();

	private TTYZ80 tty = new TTYZ80();
	private ListDevice listDevice = new ListDevice();

	public static IOController getInstance() {
		return instance;
	}// getInstance

	private IOController() {
		try {
			addDevice(tty);
			Thread threadTTY = new Thread(tty);
			threadTTY.start();
			addDevice(listDevice);
			Thread threadLST = new Thread(listDevice);
			threadLST.start();
		} catch (IOException e) {
			log.error("[IOController.IOController()]  Failed to Add a Device: " + e.getMessage());
		} // try
	}// Constructor

	private void addDevice(DeviceZ80 device) throws IOException {//TTYZ80
		
		if (device.getAddressIn() != null) {// IN Command data to CPU
			PipedOutputStream dataToCpuSender = new PipedOutputStream(); 
			PipedInputStream dataToCpuReceiver = new PipedInputStream(dataToCpuSender);	
			devicesInput.put(device.getAddressIn(), new DevicePipes(device, dataToCpuReceiver));
			device.setDataToCpuSender(dataToCpuSender);
			device.setDataToCpuReceiver(dataToCpuReceiver);
		} // if input

		if (device.getAddressOut() != null) {// OUT Command - data from CPU
			PipedOutputStream dataFromCpuPutter = new PipedOutputStream();
			PipedInputStream dataFromCpuGetter = new PipedInputStream(dataFromCpuPutter);
			devicesOutput.put(device.getAddressOut(), new DevicePipes(device, dataFromCpuPutter));
			device.setDataFromCpuReceiver(dataFromCpuGetter);
		} // if input
		

		if (device.getAddressStatus() != null) {
			PipedOutputStream statusRequestSender = new PipedOutputStream();// from CPU
			PipedInputStream statusRequestReceiver = new PipedInputStream(statusRequestSender);
			
			
			PipedOutputStream statusResponseSender = new PipedOutputStream();
			PipedInputStream  statusResponseReceiver= new PipedInputStream(statusResponseSender);


			devicesStatus.put(device.getAddressStatus(),
					new DevicePipes(device,statusResponseReceiver, statusRequestSender));
			
			device.setStatusRequestReceiver(statusRequestReceiver);
			device.setStatusResponseSender(statusResponseSender);
			
		} // if input
		devicePopulation.add(device);
	}// addDevice

	public void close() {
		for (DeviceZ80 d : devicePopulation) {
			if (d != null) {
				d.close();
				d = null;
			} // if
		} // for each device
	}// close
	
	public boolean isVisible(String deviceName) {
		for (DeviceZ80 d :devicePopulation) {
			if (d.getName().equals(deviceName)){
				return d.isVisible();
			}// if
		}//for
		log.warnf("device: %s not found for isVisible%n", deviceName);
		return false; // default to not visible
	}//is visible
	
	public void setVisible(String deviceName, boolean state) {
		for (DeviceZ80 d :devicePopulation) {
			if (d.getName().equals(deviceName)){
				d.setVisible(state);
				return;
			}// if
		}//for
		log.warnf("device: %s not found for setVisible %s%n", deviceName,state);
		return;
	}//setVisible

	public void byteToDevice(byte address, byte value) {
		if (devicesOutput.containsKey(address)) {
			try {
				devicesOutput.get(address).os.write(value);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // try
		} else {
			log.error(String.format("[byteToDevice] could not identify device at: %02X", address));
		} // if
	}// byteToDevice

	public Byte byteFromDevice(Byte address) throws IOException {
		 int STATUS_DELAY = 8;
		Byte value = 0x00;
		if (devicesInput.containsKey(address)) { //data
			if (devicesInput.get(address).is.available() > 0) {
				value = (byte) devicesInput.get(address).is.read();
			} // if something to read
		} else if (devicesStatus.containsKey(address)) {  //Status
			try {
//				if (address==(byte)0x11){
//					System.out.printf("[ioc.byteFromDevice] address = %02X%n", address);
//				}//if
				DevicePipes device = devicesStatus.get(address);
				device.os.write(GET_STATUS);
				device.os.flush();
				if(device.is.available() >0) {
					value = (byte) device.is.read();
					if (address==(byte)0x11){
						System.out.printf("[ioc.byteFromDevice] addressx = %02X, Value = %02X%n", address,value);
					}//if

				}else {
					Thread.sleep(STATUS_DELAY);
				}//if
				
				if(device.is.available() >0) {
					value = (byte) device.is.read();
				}else {
					log.errorf("device %02X has timed out on Status Read%n", address);
				}//if
			} catch (Exception e) {
//				e.printStackTrace();
//				System.exit(-1);
			} // try

		} // if input device
		return value;
	}// byteFromDevice
	
	
	/* this is really just a structure */
	class DevicePipes{
		public DeviceZ80 device;
		public PipedInputStream is;
		public PipedOutputStream os;

		public DevicePipes(DeviceZ80 device,PipedInputStream is, PipedOutputStream os) {
			this.device = device;
			this.is = is;
			this.os = os;
		}// Constructor
		public DevicePipes(DeviceZ80 device,PipedInputStream is ) {
			this(device,is,null);
		}// Constructor
		public DevicePipes(DeviceZ80 device, PipedOutputStream os) {
			this(device,null,os);
		}// Constructor

		
	}//class DevicePipes{
	
	

	public static final byte GET_STATUS = (byte) 0xFF;

	
}// class IOController
