package ioSystem;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import codeSupport.AppLogger;
import ioSystem.ttyZ80.TTYZ80_A;

public class IOController {
	private AppLogger log = AppLogger.getInstance();
	private static IOController instance = new IOController();

	private Set<DeviceZ80_A> devicePopulation = new HashSet<>();

	private HashMap<Byte, DeviceInputStatus> devicesInput = new HashMap<>();
	private HashMap<Byte, DeviceOutput> devicesOutput = new HashMap<>();
	private HashMap<Byte, DeviceInputStatus> devicesStatus = new HashMap<>();

	private TTYZ80_A tty = new TTYZ80_A();;
//	private DeviceZ80_A device;

	public static IOController getInstance() {
		return instance;
	}// getInstance

	private IOController() {
		try {
			addDevice(tty);
			Thread threadTTY = new Thread(tty);
			threadTTY.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // try
	}// Constructor

	private void addDevice(TTYZ80_A device) throws IOException {
		PipedOutputStream os;// = new PipedOutputStream();
		PipedInputStream is;// = new PipedInputStream(pos);
		if (device.getAddressIn() != null) {
			os = new PipedOutputStream(); // Sender
			is = new PipedInputStream(os); // Receiver
			devicesInput.put(device.getAddressIn(), new DeviceInputStatus(device,is));
			device.setPipeIn( os);
		} // if input

		if (device.getAddressOut() != null) {
			os = new PipedOutputStream();
			is = new PipedInputStream(os);
			devicesOutput.put(device.getAddressOut(), new DeviceOutput(device,os));
			device.setPipeOut(is);
		} // if input

		if (device.getAddressStatus() != null) {
			os = new PipedOutputStream();
			is = new PipedInputStream(os);
			devicesStatus.put(device.getAddressStatus(), new DeviceInputStatus(device,is));
			device.setPipeStatus(os);
		} // if input
		devicePopulation.add(device);
	}// addDevice

	public void close() {
		for (DeviceZ80_A d : devicePopulation) {
			if (d != null) {
				d.close();
				d = null;
			} // if
		} //
	}// close

	public void byteToDevice(byte address, byte value) {
		if (devicesOutput.containsKey(address)) {
			try {
				devicesOutput.get(address).os.write(value);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// device.byteFromCPU(address, value);
		} else {
			log.addError(String.format("[byteToDevice] could not identify device at: %02X", address));
		} // if
	}// byteToDevice

	public Byte byteFromDevice(Byte address) throws IOException {

		Byte value = 0x00;
		if(devicesInput.containsKey(address)) {
			if(devicesInput.get(address).is.available()>0) {
				value = (byte) devicesInput.get(address).is.read();
			}//if something to read
		}//if input device

		return value;

	}// byteFromDevice

	// public String stringFromDevice(Byte address) {
	// return byteFromDevice(address).toString();
	//
	// }// stringFromDevice

	
	/* this is really just a structure */
	class DeviceInputStatus {
		public TTYZ80_A device;
		public PipedInputStream is;

		public DeviceInputStatus(TTYZ80_A device, PipedInputStream is) {
			this.device= device;
			this.is = is;
		}// Constructor
	}// class DeviceInputStatus
	
	/* this is really just a structure */
	class DeviceOutput {
		public TTYZ80_A device;
		public PipedOutputStream os;

		public DeviceOutput(TTYZ80_A device, PipedOutputStream os) {
			this.device= device;
			this.os = os;
		}// Constructor
	}// class DeviceOutput
	
	

}// class IOController
