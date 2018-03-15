package ioSystem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import codeSupport.AppLogger;
import ioSystem.ttyZ80.TTYZ80;

public class IOController {
	private AppLogger log = AppLogger.getInstance();
	private static IOController instance = new IOController();

	private Set<DeviceZ80> devicePopulation = new HashSet<>();

	private HashMap<Byte, DeviceZ80> devicesInput = new HashMap<>();
	private HashMap<Byte, DeviceZ80> devicesOutput = new HashMap<>();
	private HashMap<Byte, DeviceZ80> devicesStatus = new HashMap<>();

	private TTYZ80 tty = new TTYZ80();;
	private DeviceZ80 device;
	
	
	public static IOController getInstance() {
		return instance;
	}// getInstance

	private IOController() {
		addDevice(tty);
	}// Constructor

	private void addDevice(DeviceZ80 device) {
		if (device.getAddressIn() != null) {
			devicesInput.put(device.getAddressIn(), device);
		} // if input

		if (device.getAddressOut() != null) {
			devicesOutput.put(device.getAddressOut(), device);
		} // if input

		if (device.getAddressStatus() != null) {
			devicesStatus.put(device.getAddressStatus(), device);
		} // if input

		devicePopulation.add(device);
	}// addDevice
	
	public void close(){
		for(DeviceZ80 d:devicePopulation){
			if(d!=null){
				d.close();
				d = null;
			}//if
		}//
	}//close
	
	public void byteToDevice(byte address, byte value) {
		if(devicesOutput.containsKey(address)) {
			device = devicesOutput.get(address);
			device.byteFromCPU(address, value);
		}else {
			log.addError(String.format("[byteToDevice] could not identify device at: %02X",address));
		}//if 
	}//byteToDevice
	
	public Byte byteFromDevice(Byte address) {
		Byte value;
		if(devicesInput.containsKey(address)) {
			device = devicesInput.get(address);
			value = device.byteToCPU(address);
		}else if(devicesStatus.containsKey(address)) {
			device = devicesStatus.get(address);
			value = device.byteToCPU(address);
		}else {
			log.addError(String.format("[byteFromDevice] could not identify device at: %02X",address));
			value = null;
		}//if
		return value;
	}//byteFromDevice
	
	public String stringFromDevice(Byte address) {
		return byteFromDevice(address).toString();
		
	}//stringFromDevice


}// class IOController
