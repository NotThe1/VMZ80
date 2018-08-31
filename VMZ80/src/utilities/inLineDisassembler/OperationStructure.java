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

	public String getAssemblerCodeA99() {
		return  String.format("%-4s", getInstruction());			
	}// getAssemblerCodeA99
	
	public String getAssemblerCodeA98(byte value1) {
		return  String.format("%-4s %s,%02XH", getInstruction(), getDestination(),value1);			
	}// getAssemblerCodeA98

	public String getAssemblerCodeA97(byte value1) {
		return  String.format("%-4s %02XH",getInstruction(),value1);			
	}// getAssemblerCodeA97

	public String getAssemblerCodeA96(byte value2) {
		return  String.format("%-4s (%s+%02XH)", getInstruction(),getDestination(),value2);			
	}// getAssemblerCodeA96

	public String getAssemblerCodeA95(byte value2,byte value3) {
		return  String.format("%-4s (%s+%02XH),%02XH", getInstruction(),getDestination(),value2,value3);			
	}// getAssemblerCodeA95

	public String getAssemblerCodeA94(byte value2) {
		return  String.format("%-4s %s,(%s+%02XH)", getInstruction(),getSource(),getDestination(),value2);			
	}// getAssemblerCodeA94

	public String getAssemblerCodeA93(byte value2) {
		return  String.format("%-4s (%s+%02XH),%s", getInstruction(),getSource(),value2,getDestination());			
	}// getAssemblerCodeA93

	public String getAssemblerCodeA92() {
		return  String.format("%-4s %s,%s", getInstruction(),getDestination(),getSource());			
	}// getAssemblerCodeA92

	public String getAssemblerCodeA91(byte value2,byte value3) {
		return  String.format("%-4s (%02X%02XH),%s", getInstruction(),value3,value2,getSource());			
	}// getAssemblerCodeA91

	public String getAssemblerCodeA90(byte value2,byte value3) {
		return  String.format("%-4s %s,(%02X%02XH)", getInstruction(),getDestination(),value3,value2);			
	}// getAssemblerCodeA90

	public String getAssemblerCodeA89(byte value2,byte value3) {
		return  String.format("%-4s %s,%02X%02XH", getInstruction(),getDestination(),value3,value2);			
	}// getAssemblerCodeA89

	public String getAssemblerCodeA88(byte value1,byte value2) {
		return  String.format("%-4s %02X%02XH", getInstruction(),value2,value1);			
	}// getAssemblerCodeA88

	public String getAssemblerCodeA85(byte value2) {
		return  String.format("%-4s %s,(%s+%02XH)", getInstruction(),getSource(),getDestination(),value2);			
	}// getAssemblerCodeA85
	
//	public String getAssemblerCodeA86(byte value1,byte value2) {
//		return  String.format("%-4s %02X%02XH,%s", getInstruction(),value2,value1, getSource());			
//	}// getAssemblerCodeA87
//	
	
	public String getAssemblerCodeA00() {
		return  String.format("%-4s %s", getInstruction(), getDestination());			
	}// getAssemblerCodeA00
	
	public String getAssemblerCodeA01(byte value1) {
			return String.format("%-4s (%02XH),%s", getInstruction(), value1, getSource());
	}// getAssemblerCodeA01
	
	public String getAssemblerCodeA10(byte plusOne) {
		return String.format("%-4s %02X", getInstruction(), plusOne);
}// getAssemblerCodeA10
	
	public String getAssemblerCodeA11(byte plusOne) {
		return String.format("%-4s %s,%02X", getInstruction(), getDestination(), plusOne);
}// getAssemblerCodeA11
	
	public String getAssemblerCodeA20(byte plusOne,byte plusTwo) {
		return  String.format("%-4s (%02X%02X),%s", getInstruction(), plusTwo, plusOne,getSource());
}// getAssemblerCodeA20
	
	public String getAssemblerCodeA21(byte plusOne,byte plusTwo) {
		return String.format("%-4s %s,(%02X%02X)",  getInstruction(),getDestination(), plusTwo, plusOne);
}// getAssemblerCodeA21
	
	public String getAssemblerCodeA22(byte plusOne,byte plusTwo) {
		return String.format("%-4s %s,%02X%02X", getInstruction(), getDestination(), plusTwo, plusOne);
}// getAssemblerCodeA10

	


	public String getAssemblerCode() {
		String ans ="";
		if(getSource().equals("")) {
			ans = String.format("%-4s %s", getInstruction(), getDestination());
		}else {
			ans =String.format("%-4s %s,%s", getInstruction(), getDestination(), getSource());;
		}//if	 
		return ans;
		
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
			ans = String.format("%-4s (%02X%02X),%s", getInstruction(), plusTwo, plusOne,getSource());
		}else if(getSource().equals("addr")){
			ans = String.format("%-4s %s,(%02X%02X)",  getInstruction(),getDestination(), plusTwo, plusOne);
		} else {
			ans = String.format("%-4s %s,%02X%02X", getInstruction(), getDestination(), plusTwo, plusOne);
		}// if
		return ans;
	}// getAssemblerCode

}// class operationStructure
	// <><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>
	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
