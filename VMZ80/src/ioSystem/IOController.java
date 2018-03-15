package ioSystem;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import codeSupport.AppLogger;
import ioSystem.ttyZ80.TTYZ80;

public class IOController {
	private AppLogger log = AppLogger.getInstance();
	private static IOController instance = new IOController();

	private Set<DeviceZ80_A> devicePopulation = new HashSet<>();

	private HashMap<Byte, DeviceZ80_A> devicesInput = new HashMap<>();
	private HashMap<Byte, DeviceZ80_A> devicesOutput = new HashMap<>();
	private HashMap<Byte, DeviceZ80_A> devicesStatus = new HashMap<>();

	private TTYZ80 tty = new TTYZ80();;
	private DeviceZ80_A device;

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
		}
	}// Constructor

	private void addDevice(DeviceZ80_A device) throws IOException {
		PipedOutputStream os;// = new PipedOutputStream();
		PipedInputStream is;// = new PipedInputStream(pos);
		if (device.getAddressIn() != null) {
			devicesInput.put(device.getAddressIn(), device);
			os = new PipedOutputStream();
			is = new PipedInputStream(os);
			device.setPipesIn(is, os);
		} // if input

		if (device.getAddressOut() != null) {
			devicesOutput.put(device.getAddressOut(), device);
			os = new PipedOutputStream();
			is = new PipedInputStream(os);
			device.setPipesOut(is, os);
		} // if input

		if (device.getAddressStatus() != null) {
			devicesStatus.put(device.getAddressStatus(), device);
			os = new PipedOutputStream();
			is = new PipedInputStream(os);
			device.setPipesIn(is, os);
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
			device = devicesOutput.get(address);
			device.byteFromCPU(address, value);
		} else {
			log.addError(String.format("[byteToDevice] could not identify device at: %02X", address));
		} // if
	}// byteToDevice

	public Byte byteFromDevice(Byte address) throws IOException {

		Byte value = 0x00;
		if (devicesInput.containsKey(address)) {
			device = devicesInput.get(address);
			if (device.pipeOut_In.available()>0 )

			value = (byte) device.pipeOut_In.read();

		} else if (devicesStatus.containsKey(address)) {
			device = devicesStatus.get(address);
			value = device.byteToCPU(address);
		} else {
			log.addError(String.format("[byteFromDevice] could not identify device at: %02X", address));
			value = null;
		} // if
		return value;

		///////////////////////////////////////////////
		// Byte value;
		// if (devicesInput.containsKey(address)) {
		// device = devicesInput.get(address);
		// value = device.byteToCPU(address);
		// } else if (devicesStatus.containsKey(address)) {
		// device = devicesStatus.get(address);
		// value = device.byteToCPU(address);
		// } else {
		// log.addError(String.format("[byteFromDevice] could not identify device at: %02X", address));
		// value = null;
		// } // if
		// return value;
	}// byteFromDevice

//	public String stringFromDevice(Byte address) {
//		return byteFromDevice(address).toString();
//
//	}// stringFromDevice

}// class IOController
