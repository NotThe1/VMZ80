package utilities.inLineDisassembler;

 class OperationStructure {
	private String opCode;
	private Z80Type type;
	private int size;
	private String instruction;
	private String source;
	private String destination;
	private String function;

	OperationStructure(String opCode, Z80Type type,int size,
			String instruction, String destination, String source, String function) {
		this.opCode = opCode;
		this.type = type;
		this.size = size;
		this.instruction = instruction;
		this.source = source;
		this.destination = destination;
		this.function = function;
	}// CONSTRUCTOR

	 public String getOpCode() {
		return this.opCode;
	}// getOpCode

	public int getSize() {
		return this.size;
	}// getSize

	private String getInstruction() {
		return this.instruction;
	}// getInstruction

	private String getSource() {
		return this.source;
	}// getSource

	private String getDestination() {
		return this.destination;
	}// getDestination

	public String getFunction() {
		return this.function;
	}// getFunction

	// public String getFunctionFormatted() {
	// return String.format("%8s%s%n", "", this.function);
	// }// getFunction

	public String getAssemblerCode() {
		return String.format("%-4s %s%s", getInstruction(), getDestination(), getSource());
	}// getAssemblerCode

	public String getAssemblerCode(byte plusOne) {
		String ans;
		if (getDestination().equals("D8")) {
			ans = String.format("%-4s %02X", getInstruction(), plusOne);
		} else {
			ans = String.format("%-4s %s,%02X", getInstruction(), getDestination(), plusOne);
		}// if
		return ans;
	}// getAssemblerCode

	public String getAssemblerCode(byte plusOne, byte plusTwo) {
		String ans;
		if (getDestination().equals("addr")) {
			ans = String.format("%-4s %02X%02X", getInstruction(), plusTwo, plusOne);
		} else {
			ans = String.format("%-4s %s,%02X%02X", getInstruction(), getDestination(), plusTwo, plusOne);
		}// if
		return ans;
	}// getAssemblerCode

}// class operationStructure
	// <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
