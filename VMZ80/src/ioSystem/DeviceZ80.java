package ioSystem;

abstract public class DeviceZ80 {

	private boolean inputFlag;
	private boolean outputFlag;

	private Byte addressIn;
	private Byte addressOut;
	private Byte addressStatus;

	private String name;
	private IOType type;
	private String errMessage;

	public DeviceZ80(String name, IOType type, boolean input, Byte addressIn, boolean output, Byte addressOut,
			Byte addressStatus) {
		this.name = name;
		this.type = type;
		this.inputFlag = input;
		this.addressIn = addressIn;
		this.outputFlag = output;
		this.addressOut = addressOut;
		this.addressStatus = addressStatus;

		String msgLead = String.format("Constructor %s device error - ", this.name);
		if (!(this.inputFlag | this.outputFlag)) {
			errMessage = msgLead + "Need  Input or Output or Both";
			throw new IOExceptionZ80(errMessage);
		} // if in or out

		if (this.outputFlag && (this.addressOut == null)) {
			errMessage = msgLead + "Output set true with out an Output address";
			throw new IOExceptionZ80(errMessage);
		} // if output
		if (this.inputFlag && (this.addressIn == null)) {
			errMessage = msgLead + "Input set true with out an input address";
			throw new IOExceptionZ80(errMessage);
		} // if input
	}// Constructor
	
	public DeviceZ80(String name, IOType type, Byte addressIn, Byte addressOut,	Byte addressStatus) {
		this(name,type,true,addressIn,true,addressOut,addressStatus);
	}//Constructor - all addresses

	public boolean isInput() {
		return this.inputFlag;
	}//isInput

	public boolean isOutput() {
		return this.outputFlag;
	}//isInput
	
	public void setOutput(boolean output) {
		this.outputFlag = output;
	}// setOutput

	public void setInput(boolean input) {
		this.inputFlag = input;
	}// setIinput
	
	public Byte getAddressIn() {
		return addressIn;
	}// getAddressIn

	public void setAddressIn(Byte addressIn) {
		this.addressIn = addressIn; // only 8 bits wide
	}// setAddressIn

	public Byte getAddressOut() {
		return addressOut;
	}// getAddressOut

	public void setAddressOut(Byte addressOut) {
		this.addressOut = addressOut; // only 8 bits wide
	}// setAddressOut

	public Byte getAddressStatus() {
		return addressStatus;
	}// getAddressStatus

	public void setAddressStatus(Byte addressStatus) {
		this.addressStatus = addressStatus; // only 8 bits wide
	}// setAddressStatus

	public String getName() {
		return this.name;
	}// getName

	public void setName(String name) {
		this.name = name;
	}// setName

	public IOType getType() {
		return this.type;
	}// getType

	public void setType(IOType type) {
		this.type = type;
	}// setType

	abstract public void byteFromCPU(Byte address, Byte value);

	abstract public byte byteToCPU(Byte address);
	
	abstract public void close();



}// class DeviceZ80
