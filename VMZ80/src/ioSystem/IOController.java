package ioSystem;
/*
 * 2018-11-16 Changed to Queue for I/O
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import codeSupport.AppLogger;
import ioSystem.listDevice.GenericPrinter;
import ioSystem.ttyZ80.TTYZ80;

public class IOController {
	private AppLogger log = AppLogger.getInstance();
	private static IOController instance = new IOController();

	private Set<DeviceZ80> devicePopulation = new HashSet<>();

	private HashMap<Byte, DeviceZ80> devicesInput = new HashMap<>();
	private HashMap<Byte, DeviceZ80> devicesOutput = new HashMap<>();
	private HashMap<Byte, DeviceZ80> devicesStatus = new HashMap<>();

	private TTYZ80 tty;
	private GenericPrinter printer;

	public static IOController getInstance() {
		return instance;
	}// getInstance

	private IOController() {
		tty = new TTYZ80("tty", TTYZ80.IN, TTYZ80.OUT, TTYZ80.STATUS);
		try {
			addDevice(tty);
			Thread threadTTY = new Thread(tty);
			threadTTY.start();
		} catch (IOException e) {
			log.error("[IOController.IOController()]  Failed to Add a Device: tty " + e.getMessage());
		} // try
		printer = new GenericPrinter("printer", GenericPrinter.IN, GenericPrinter.OUT, GenericPrinter.STATUS);
		try {
			addDevice(printer);
			Thread threadPrinter = new Thread(printer);
			threadPrinter.start();
		} catch (IOException e) {
			log.error("[IOController.IOController()]  Failed to Add a Device: printer" + e.getMessage());
		} // try
	}// Constructor

	private void addDevice(DeviceZ80 device) throws IOException {// TTYZ80

		if (device.getAddressIn() != null) {// IN Command data to CPU
			devicesInput.put(device.getAddressIn(), device);
		} // if input

		if (device.getAddressOut() != null) {// OUT Command - data from CPU
			devicesOutput.put(device.getAddressOut(), device);
		} // if input

		if (device.getAddressStatus() != null) {
			devicesStatus.put(device.getAddressStatus(), device);
		} // if status
	}// addDevice

	public void close() {
		// System.err.printf("[IOController.close] - %s%n" , "Close");
		for (DeviceZ80 d : devicePopulation) {
			if (d != null) {
				d.close();
				d = null;
			} // if
		} // for each device
	}// close

	public boolean isVisible(String deviceName) {
		for (DeviceZ80 d : devicePopulation) {
			if (d.getName().equals(deviceName)) {
				return d.isVisible();
			} // if
		} // for
		log.warnf("device: %s not found for isVisible%n", deviceName);
		return false; // default to not visible
	}// is visible

	public void setVisible(String deviceName, boolean state) {
		for (DeviceZ80 d : devicePopulation) {
			if (d.getName().equals(deviceName)) {
				d.setVisible(state);
				return;
			} // if
		} // for
		log.warnf("device: %s not found for setVisible %s%n", deviceName, state);
		return;
	}// setVisible

	public String getVisibleDevices() {
		String ans = "";
		for (DeviceZ80 d : devicePopulation) {
			if (d.isVisible()) {
				ans += "|" + d.getName();
			} // if
		} // for
		System.out.printf("[IOController.getVisibleDevices] %s%n", ans);
		return ans;
	}// getVisibleDevices

	public void setVisibleDevices(String deviceSet) {
		setDevicesVisible(false);
		String devices[] = deviceSet.split("\\|");
		for (String deviceName : devices) {
			if (deviceName.equals("")) {
				continue;
			} // if
			setVisible(deviceName, true);
		} // for
	}// setVisibleDevices

	private void setDevicesVisible(boolean state) {
		for (DeviceZ80 d : devicePopulation) {
			d.setVisible(state);
		} // for
	}// setDevicesVisible

	public void byteFromCPU(byte address, byte value) {
		DeviceZ80 device = devicesOutput.get(address);
		if (device != null) {
			device.dataFromCPU.offer(value);
		} // if have output device at this address
	}// byteToDevice

	public Byte byteToCPU(Byte address) {

		Byte value = null;
		;
		DeviceZ80 deviceStatus = devicesStatus.get(address);
		DeviceZ80 deviceData = devicesInput.get(address);

		if (deviceStatus != null) {
			deviceStatus.statusFromCPU.offer(address);

//			try {
//				Thread.sleep(1);
//			} catch (InterruptedException interruptedException) {
//				log.errorf("Status timeout for address %02X%n", address);
//			} // try
			
			long startMilli = System.currentTimeMillis();
			while (deviceStatus.statusToCPU.size()==0) {
				// Wait until response is there
			}//while
			log.infof("IO time = %d%n", System.currentTimeMillis()-startMilli);

			return deviceStatus.statusToCPU.poll();

		} else if (deviceData != null) {
			value = deviceData.dataToCPU.poll();
		} else {
			log.infof("Address unknown for Data/Status to CPU : %02X%n", address);
			return null;
		} // if
		return value;
	}// byteFromDevice

}// class IOController
