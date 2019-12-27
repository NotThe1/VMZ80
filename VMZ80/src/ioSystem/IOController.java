package ioSystem;
/*
 * 2019-07-11 Cleaned up adding devices
 * 2018-11-16 Changed to Queue for I/O
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import codeSupport.AppLogger;
import ioSystem.listDevice.GenericPrinter;
import ioSystem.terminals.TTYZ80;
import ioSystem.terminals.VT100;

public class IOController {
	private AppLogger log = AppLogger.getInstance();
	private static IOController instance = new IOController();

	private Set<DeviceZ80> devicePopulation = new HashSet<>();

	private HashMap<Byte, DeviceZ80> devicesInput = new HashMap<>();
	private HashMap<Byte, DeviceZ80> devicesOutput = new HashMap<>();
	private HashMap<Byte, DeviceZ80> devicesStatus = new HashMap<>();

	public static IOController getInstance() {
		return instance;
	}// getInstance

	private IOController() {
		addDevice(new GenericPrinter("printer", GenericPrinter.IN, GenericPrinter.OUT, GenericPrinter.STATUS));
		addDevice(new TTYZ80("tty", TTYZ80.IN, TTYZ80.OUT, TTYZ80.STATUS));
		addDevice(new VT100("crt", VT100.IN, VT100.OUT, VT100.STATUS));
	}// Constructor

	public void addDevice(DeviceZ80 device) {// TTYZ80 throws IOException
		if (device.getAddressIn() != null) {// IN Command data to CPU
			devicesInput.put(device.getAddressIn(), device);
		} // if input

		if (device.getAddressOut() != null) {// OUT Command - data from CPU
			devicesOutput.put(device.getAddressOut(), device);
		} // if input

		if (device.getAddressStatus() != null) {
			devicesStatus.put(device.getAddressStatus(), device);
		} // if status

		Thread threadDevice = new Thread(device, device.getName());
		threadDevice.start();
		devicePopulation.add(device);
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
		StringBuilder sb = new StringBuilder();
		for (DeviceZ80 d : devicePopulation) {
			if (d.isVisible()) {
				sb.append("|" + d.getName());
			} // if
		} // for
		return sb.toString();

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

			while (deviceStatus.statusToCPU.size() == 0) {
			} // while - Wait until response is there

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
