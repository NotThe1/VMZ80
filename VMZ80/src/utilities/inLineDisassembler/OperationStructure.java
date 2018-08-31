package utilities.inLineDisassembler;

 class OperationStructure {
	private String opCode;
	private Z80InstrucionType type;
	private int size;
	private String instruction;
	private String source;
	private String destination;
	private String function;

	OperationStructure(String opCode, Z80InstrucionType type,int size,
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

	public String getInstruction() {
		return this.instruction;
	}// getInstruction

	public String getSource() {
		return this.source;
	}// getSource

	public String getDestination() {
		return this.destination;
	}// getDestination

	public String getFunction() {
		return this.function;
	}// getFunction

	public Z80InstrucionType getType() {
		return this.type;
	}// getFunction

}// class operationStructure
