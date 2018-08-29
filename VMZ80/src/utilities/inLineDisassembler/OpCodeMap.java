package utilities.inLineDisassembler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import codeSupport.AppLogger;

/**
 * 
 * @author Frank Martyn This class defines all the opcodes and the structure of each one. It is a helper class for the
 *         in-line disassembler that displays with the rest of the machine displays.
 *
 */

public class OpCodeMap {
	static HashMap<String, OperationStructure> codeMap = new HashMap<>();

	// Constructor is at end of this file after class OperationStructure

	static public void tool() {
		AppLogger log = AppLogger.getInstance();
		Set mapKeys = codeMap.keySet();
		List<String> keys = new ArrayList<String>(mapKeys);
		OperationStructure os;
		keys.sort(null);
		log.special("os.getSource().equals(\"D8\"");
		for (String key : keys) {
			os = codeMap.get(key);
			if (os.getSource().equals("D8")) {
				log.info("%s:  %d - %-8s , %s%n", os.getType(), os.getSize(), key, os.getDestination());
			} // if
		} // for
		// os.getSize()==1 && !os.getSource().equals("")&& os.getDestination().equals("")

	}// tool

	static public int getSize(String opCodeMapKey) {
		// System.out.println(opCodeMapKey);
		// System.out.printf("[OpCodeMap.getSize] opCodeMapKey = %s.%n", opCodeMapKey);
		return codeMap.get(opCodeMapKey).getSize();
	}// getSize

	static public String getFunction(String opCodeMapKey) {
		return codeMap.get(opCodeMapKey).getFunction();
	}// getFunction

	static public String getAssemblerCode(String opCodeMapKey) {
		return codeMap.get(opCodeMapKey).getAssemblerCode();
	}// getAssemblerCode - no arguments

	static public String getAssemblerCode(String opCodeMapKey, byte plusOne) {
		return codeMap.get(opCodeMapKey).getAssemblerCode(plusOne);
	}// getAssemblerCode - one argument

	static public String getAssemblerCode(String opCodeMapKey, byte plusOne, byte plusTwo) {
		return codeMap.get(opCodeMapKey).getAssemblerCode(plusOne, plusTwo);
	}// getAssemblerCode - two arguments

	static public String getAssemblerCodeA99(String opCodeMapKey) {
		return codeMap.get(opCodeMapKey).getAssemblerCodeA99();
	}// getAssemblerCodeA99

	static public String getAssemblerCodeA98(String opCodeMapKey, byte value1) {
		return codeMap.get(opCodeMapKey).getAssemblerCodeA98(value1);
	}// getAssemblerCodeA98

	static public String getAssemblerCodeA97(String opCodeMapKey, byte value1) {
		return codeMap.get(opCodeMapKey).getAssemblerCodeA97(value1);
	}// getAssemblerCodeA97

	static public String getAssemblerCodeA96(String opCodeMapKey, byte value2) {
		return codeMap.get(opCodeMapKey).getAssemblerCodeA96(value2);
	}// getAssemblerCodeA96

	static public String getAssemblerCodeA95(String opCodeMapKey, byte value2, byte value3) {
		return codeMap.get(opCodeMapKey).getAssemblerCodeA95(value2, value3);
	}// getAssemblerCodeA95

	static public String getAssemblerCodeA94(String opCodeMapKey, byte value2) {
		return codeMap.get(opCodeMapKey).getAssemblerCodeA94(value2);
	}// getAssemblerCodeA94

	static public String getAssemblerCodeA93(String opCodeMapKey, byte value2) {
		return codeMap.get(opCodeMapKey).getAssemblerCodeA93(value2);
	}// getAssemblerCodeA93

	static public String getAssemblerCodeA92(String opCodeMapKey) {
		return codeMap.get(opCodeMapKey).getAssemblerCodeA92();
	}// getAssemblerCodeA92

	static public String getAssemblerCodeA91(String opCodeMapKey, byte value2, byte value3) {
		return codeMap.get(opCodeMapKey).getAssemblerCodeA91(value2, value3);
	}// getAssemblerCodeA91

	static public String getAssemblerCodeA90(String opCodeMapKey, byte value2, byte value3) {
		return codeMap.get(opCodeMapKey).getAssemblerCodeA90(value2, value3);
	}// getAssemblerCodeA90

	static public String getAssemblerCodeA89(String opCodeMapKey, byte value1, byte value2) {
		return codeMap.get(opCodeMapKey).getAssemblerCodeA89(value2, value1);
	}// getAssemblerCodeA89

	static public String getAssemblerCodeA88(String opCodeMapKey, byte value1, byte value2) {
		return codeMap.get(opCodeMapKey).getAssemblerCodeA88(value2, value1);
	}// getAssemblerCodeA88

	static public String getAssemblerCodeA85(String opCodeMapKey, byte value2) {
		return codeMap.get(opCodeMapKey).getAssemblerCodeA85(value2);
	}// getAssemblerCodeA85

	/////////////////////////////////////////////////////////////////////
	static public String getAssemblerCodeA00(String opCodeMapKey) {
		return codeMap.get(opCodeMapKey).getAssemblerCodeA00();
	}// getAssemblerCodeA00

	static public String getAssemblerCodeA01(String opCodeMapKey,byte value1) {
		return codeMap.get(opCodeMapKey).getAssemblerCodeA01( value1);
	}// getAssemblerCodeA01

	static public String getAssemblerCodeA10(String opCodeMapKey, byte plusOne) {
		return codeMap.get(opCodeMapKey).getAssemblerCodeA10(plusOne);
	}// getAssemblerCodeA10

	static public String getAssemblerCodeA11(String opCodeMapKey, byte plusOne) {
		return codeMap.get(opCodeMapKey).getAssemblerCodeA11(plusOne);
	}// getAssemblerCode11

	static public String getAssemblerCodeA20(String opCodeMapKey, byte plusOne, byte plusTwo) {
		return codeMap.get(opCodeMapKey).getAssemblerCodeA20(plusOne, plusTwo);
	}// getAssemblerCodeA20

	static public String getAssemblerCodeA21(String opCodeMapKey, byte plusOne, byte plusTwo) {
		return codeMap.get(opCodeMapKey).getAssemblerCodeA21(plusOne, plusTwo);
	}// getAssemblerCodeA21

	static public String getAssemblerCodeA22(String opCodeMapKey, byte plusOne, byte plusTwo) {
		return codeMap.get(opCodeMapKey).getAssemblerCodeA22(plusOne, plusTwo);
	}// getAssemblerCodeA22

	static public int getMapCount() {
		return codeMap.size();
	}// getMapCount

	static public Z80Type getType(String opCodeMapKey) {
		return codeMap.get(opCodeMapKey).getType();
	}// getType

	// +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	public OpCodeMap() {
		makeMainTypes();
		makeBitTypes(); // CBxx
		makeExtendedTypes(); // EDxx
		makeIndexTypes(); // DDxx & FDxx
	}// Constructor

	private void makeIndexTypes() {
		// codeMap.put(key , new OperationStructure(key, Z80Type.BIT, 2, instruction, register, "", function));
		/* IX DDXX */
		codeMap.put("DD09", new OperationStructure("DD09", Z80Type.A92, 2, "ADD", "IX", "BC", "IX<-IX + BC"));
		codeMap.put("DD19", new OperationStructure("DD19", Z80Type.A92, 2, "ADD", "IX", "DE", "IX<-IX + DE"));
		codeMap.put("DD29", new OperationStructure("DD29", Z80Type.A92, 2, "ADD", "IX", "IX", "IX<-IX + IX"));
		codeMap.put("DD39", new OperationStructure("DD39", Z80Type.A92, 2, "ADD", "IX", "SP", "IX<-IX + SP"));

		codeMap.put("DD21", new OperationStructure("DD21", Z80Type.A89, 4, "LD", "IX", "D16", "IX<- D16"));
		codeMap.put("DD22", new OperationStructure("DD22", Z80Type.A91, 4, "LD", "D16", "IX", "(D16)<-IX"));
		codeMap.put("DD23", new OperationStructure("DD23", Z80Type.A00, 2, "INC", "IX", "", "IX<-IX +1"));
		codeMap.put("DD2A", new OperationStructure("DD2A", Z80Type.A90, 4, "LD", "IX", "addr", "IX<-(D16)"));
		codeMap.put("DD2B", new OperationStructure("DD2B", Z80Type.A00, 2, "DEC", "IX", "", "IX<-IX -1"));

		codeMap.put("DD34", new OperationStructure("DD34", Z80Type.A96, 3, "INC", "IX", "D8", "(IX+d)<-(IX+d)+1"));
		codeMap.put("DD35", new OperationStructure("DD35", Z80Type.A96, 3, "DEC", "IX", "D8", "(IX+d)<-(IX+d)-1"));
		codeMap.put("DD36", new OperationStructure("DD36", Z80Type.A95, 4, "LD", "IX", "D8", "(IX+d)<-D8"));

		codeMap.put("DD46", new OperationStructure("DD46", Z80Type.A94, 3, "LD", "IX", "B", "B<-(IX+d)"));
		codeMap.put("DD4E", new OperationStructure("DD4E", Z80Type.A94, 3, "LD", "IX", "C", "C<-(IX+d)"));
		codeMap.put("DD56", new OperationStructure("DD56", Z80Type.A94, 3, "LD", "IX", "D", "D<-(IX+d)"));
		codeMap.put("DD5E", new OperationStructure("DD5E", Z80Type.A94, 3, "LD", "IX", "D", "E<-(IX+d)"));
		codeMap.put("DD66", new OperationStructure("DD66", Z80Type.A94, 3, "LD", "IX", "H", "H<-(IX+d)"));
		codeMap.put("DD6E", new OperationStructure("DD6E", Z80Type.A94, 3, "LD", "IX", "L", "L<-(IX+d)"));
		codeMap.put("DD7E", new OperationStructure("DD7E", Z80Type.A94, 3, "LD", "IX", "A", "A<-(IX+d)"));

		codeMap.put("DD70", new OperationStructure("DD70", Z80Type.A93, 3, "LD", "B", "IX", "(IX+d)<-B"));
		codeMap.put("DD71", new OperationStructure("DD71", Z80Type.A93, 3, "LD", "C", "IX", "(IX+d)<-C"));
		codeMap.put("DD72", new OperationStructure("DD72", Z80Type.A93, 3, "LD", "D", "IX", "(IX+d)<-D"));
		codeMap.put("DD73", new OperationStructure("DD73", Z80Type.A93, 3, "LD", "E", "IX", "(IX+d)<-E"));
		codeMap.put("DD74", new OperationStructure("DD74", Z80Type.A93, 3, "LD", "H", "IX", "(IX+d)<-H"));
		codeMap.put("DD75", new OperationStructure("DD75", Z80Type.A93, 3, "LD", "L", "IX", "(IX+d)<-L"));
		codeMap.put("DD77", new OperationStructure("DD77", Z80Type.A93, 3, "LD", "A", "IX", "(IX+d)<A"));

		codeMap.put("DD86", new OperationStructure("DD86", Z80Type.A94, 3, "ADD", "IX", "A", "A<-A+(IX+d)"));
		codeMap.put("DD8E", new OperationStructure("DD8E", Z80Type.A94, 3, "ADC", "IX", "A", "A<-A+(IX+d)+cy"));
		codeMap.put("DD96", new OperationStructure("DD96", Z80Type.A96, 3, "SUB", "IX", "D8", "A<-A-(IX+d)"));
		codeMap.put("DD9E", new OperationStructure("DD9E", Z80Type.A94, 3, "SBC", "IX", "A", "A<-A-(IX+d)-cy"));
		codeMap.put("DDA6", new OperationStructure("DDA6", Z80Type.A96, 3, "AND", "IX", "D8", "A<-A and (IX+d)"));
		codeMap.put("DDAE", new OperationStructure("DDAE", Z80Type.A96, 3, "XOR", "IX", "D8", "A<-A xor (IX+d)"));
		codeMap.put("DDB6", new OperationStructure("DDB6", Z80Type.A96, 3, "OR", "IX", "D8", "A<-A or(IX+d)"));
		codeMap.put("DDBE", new OperationStructure("DDBE", Z80Type.A96, 3, "CP", "IX", "D8", "A<-A -(IX+d)"));

		codeMap.put("DDE1", new OperationStructure("DDE1", Z80Type.A00, 2, "POP", "IX", "", "IX<-(SP)"));
		codeMap.put("DDE3", new OperationStructure("DDE3", Z80Type.A00, 2, "EX", "IX", "", "IX<->(SP)"));
		codeMap.put("DDE5", new OperationStructure("DDE5", Z80Type.A00, 2, "PUSH", "IX", "", "(SP)<-IX"));
		codeMap.put("DDE9", new OperationStructure("DDE9", Z80Type.A00, 2, "JP", "IX", "", "PC<-IX"));
		codeMap.put("DDF9", new OperationStructure("DDF9", Z80Type.A92, 2, "LD", "SP", "IX", "SP<-IX"));

		/* INDEXED BIT INSTRUCTIONS IX DDXXYY */

		codeMap.put("DDCB06",
				new OperationStructure("DDCB06", Z80Type.A96, 4, "RLC", "IX", "D8", "IX+d:cy<-[b7<-b0]<-b7"));
		codeMap.put("DDCB0E",
				new OperationStructure("DDCB0E", Z80Type.A96, 4, "RRC", "IX", "D8", "IX+d:b0->[b7->b0]->cy"));
		codeMap.put("DDCB16",
				new OperationStructure("DDCB16", Z80Type.A96, 4, "RL", "IX", "D8", "IX+d:cy<-[b7<-b0]<-cy"));
		codeMap.put("DDCB1E",
				new OperationStructure("DDCB1E", Z80Type.A96, 4, "RR", "IX", "D8", "IX+d:cy->[b7->b0]->cy"));
		codeMap.put("DDCB26",
				new OperationStructure("DDCB26", Z80Type.A96, 4, "SLA", "IX", "D8", "IX+d:cy<-[b7<-b0]<-0"));
		codeMap.put("DDCB2E",
				new OperationStructure("DDCB2E", Z80Type.A96, 4, "SRA", "IX", "D8", "IX+d:b7->[b7->b0]->cy"));
		codeMap.put("DDCB3E",
				new OperationStructure("DDCB3E", Z80Type.A96, 4, "SRL", "IX", "D8", "IX+d:0->[b7->b0]->cy"));

		codeMap.put("DDCB46", new OperationStructure("DDCB46", Z80Type.A85, 4, "BIT", "IX", "0", " b0 & IX+d"));
		codeMap.put("DDCB4E", new OperationStructure("DDCB4E", Z80Type.A85, 4, "BIT", "IX", "1", " b1 & IX+d"));
		codeMap.put("DDCB56", new OperationStructure("DDCB56", Z80Type.A85, 4, "BIT", "IX", "2", " b2 & IX+d"));
		codeMap.put("DDCB5E", new OperationStructure("DDCB5E", Z80Type.A85, 4, "BIT", "IX", "3", " b3 & IX+d"));
		codeMap.put("DDCB66", new OperationStructure("DDCB66", Z80Type.A85, 4, "BIT", "IX", "4", " b4 & IX+d"));
		codeMap.put("DDCB6E", new OperationStructure("DDCB6E", Z80Type.A85, 4, "BIT", "IX", "5", " b5 & IX+d"));
		codeMap.put("DDCB76", new OperationStructure("DDCB76", Z80Type.A85, 4, "BIT", "IX", "6", " b6 & IX+d"));
		codeMap.put("DDCB7E", new OperationStructure("DDCB7E", Z80Type.A85, 4, "BIT", "IX", "7", " b7 & IX+d"));

		codeMap.put("DDCB86", new OperationStructure("DDCB86", Z80Type.A85, 4, "RES", "IX", "0", "0-> b0 in IX+d"));
		codeMap.put("DDCB8E", new OperationStructure("DDCB8E", Z80Type.A85, 4, "RES", "IX", "1", "0-> b1 in IX+d"));
		codeMap.put("DDCB96", new OperationStructure("DDCB96", Z80Type.A85, 4, "RES", "IX", "2", "0-> b2 in IX+d"));
		codeMap.put("DDCB9E", new OperationStructure("DDCB9E", Z80Type.A85, 4, "RES", "IX", "3", "0-> b3 in IX+d"));
		codeMap.put("DDCBA6", new OperationStructure("DDCBA6", Z80Type.A85, 4, "RES", "IX", "4", "0-> b4 in IX+d"));
		codeMap.put("DDCBAE", new OperationStructure("DDCBAE", Z80Type.A85, 4, "RES", "IX", "5", "0-> b5 in IX+d"));
		codeMap.put("DDCBB6", new OperationStructure("DDCBB6", Z80Type.A85, 4, "RES", "IX", "6", "0-> b6 in IX+d"));
		codeMap.put("DDCBBE", new OperationStructure("DDCBBE", Z80Type.A85, 4, "RES", "IX", "7", "0-> b7 in IX+d"));

		codeMap.put("DDCBC6", new OperationStructure("DDCBC6", Z80Type.A85, 4, "SET", "IX", "0", "1-> b0 in IX+d"));
		codeMap.put("DDCBCE", new OperationStructure("DDCBCE", Z80Type.A85, 4, "SET", "IX", "1", "1-> b1 in IX+d"));
		codeMap.put("DDCBD6", new OperationStructure("DDCBD6", Z80Type.A85, 4, "SET", "IX", "2", "1-> b2 in IX+d"));
		codeMap.put("DDCBDE", new OperationStructure("DDCBDE", Z80Type.A85, 4, "SET", "IX", "3", "1-> b3 in IX+d"));
		codeMap.put("DDCBE6", new OperationStructure("DDCBE6", Z80Type.A85, 4, "SET", "IX", "4", "1-> b4 in IX+d"));
		codeMap.put("DDCBEE", new OperationStructure("DDCBEE", Z80Type.A85, 4, "SET", "IX", "5", "1-> b5 in IX+d"));
		codeMap.put("DDCBF6", new OperationStructure("DDCBF6", Z80Type.A85, 4, "SET", "IX", "6", "1-> b6 in IX+d"));
		codeMap.put("DDCBFE", new OperationStructure("DDCBFE", Z80Type.A85, 4, "SET", "IX", "7", "1-> b7 in IX+d"));

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		/* IY FDXX */
		codeMap.put("FD09", new OperationStructure("FD09", Z80Type.A92, 2, "ADD", "IY", "BC", "IY<-IY + BC"));
		codeMap.put("FD19", new OperationStructure("FD19", Z80Type.A92, 2, "ADD", "IY", "DE", "IY<-IY + DE"));
		codeMap.put("FD29", new OperationStructure("FD29", Z80Type.A92, 2, "ADD", "IY", "IY", "IY<-IY + IY"));
		codeMap.put("FD39", new OperationStructure("FD39", Z80Type.A92, 2, "ADD", "IY", "SP", "IY<-IY + SP"));

		codeMap.put("FD21", new OperationStructure("FD21", Z80Type.A89, 4, "LD", "IY", "D16", "IY<- D16"));
		codeMap.put("FD22", new OperationStructure("FD22", Z80Type.A91, 4, "LD", "D16", "IY", "(D16)<-IY"));
		codeMap.put("FD23", new OperationStructure("FD23", Z80Type.A00, 2, "INC", "IY", "", "IY<-IY +1"));
		codeMap.put("FD2A", new OperationStructure("FD2A", Z80Type.A90, 4, "LD", "IY", "addr", "IY<-(D16)"));
		codeMap.put("FD2B", new OperationStructure("FD2B", Z80Type.A00, 2, "DEC", "IY", "", "IY<-IY -1"));

		codeMap.put("FD34", new OperationStructure("FD34", Z80Type.A96, 3, "INC", "IY", "D8", "(IY+d)<-(IY+d)+1"));
		codeMap.put("FD35", new OperationStructure("FD35", Z80Type.A96, 3, "DEC", "IY", "D8", "(IY+d)<-(IY+d)-1"));
		codeMap.put("FD36", new OperationStructure("FD36", Z80Type.A95, 4, "LD", "IY", "D8", "(IY+d)<-D8"));

		codeMap.put("FD46", new OperationStructure("FD46", Z80Type.A94, 3, "LD", "IY", "B", "B<-(IY+d)"));
		codeMap.put("FD4E", new OperationStructure("FD4E", Z80Type.A94, 3, "LD", "IY", "C", "C<-(IY+d)"));
		codeMap.put("FD56", new OperationStructure("FD56", Z80Type.A94, 3, "LD", "IY", "D", "D<-(IY+d)"));
		codeMap.put("FD5E", new OperationStructure("FD5E", Z80Type.A94, 3, "LD", "IY", "E", "E<-(IY+d)"));
		codeMap.put("FD66", new OperationStructure("FD66", Z80Type.A94, 3, "LD", "IY", "H", "H<-(IY+d)"));
		codeMap.put("FD6E", new OperationStructure("FD6E", Z80Type.A94, 3, "LD", "IY", "L", "L<-(IY+d)"));
		codeMap.put("FD7E", new OperationStructure("FD7E", Z80Type.A94, 3, "LD", "IY", "A", "A<-(IY+d)"));

		codeMap.put("FD70", new OperationStructure("FD70", Z80Type.A93, 3, "LD", "B", "IY", "(IY+d)<-B"));
		codeMap.put("FD71", new OperationStructure("FD71", Z80Type.A93, 3, "LD", "C", "IY", "(IY+d)<-C"));
		codeMap.put("FD72", new OperationStructure("FD72", Z80Type.A93, 3, "LD", "D", "IY", "(IY+d)<-D"));
		codeMap.put("FD73", new OperationStructure("FD73", Z80Type.A93, 3, "LD", "E", "IY", "(IY+d)<-E"));
		codeMap.put("FD74", new OperationStructure("FD74", Z80Type.A93, 3, "LD", "H", "IY", "(IY+d)<-H"));
		codeMap.put("FD75", new OperationStructure("FD75", Z80Type.A93, 3, "LD", "L", "IY", "(IY+d)<-L"));
		codeMap.put("FD77", new OperationStructure("FD77", Z80Type.A93, 3, "LD", "A", "IY", "(IY+d)<A"));

		codeMap.put("FD86", new OperationStructure("FD86", Z80Type.A94, 3, "ADD", "IY", "A", "A<-A+(IY+d)"));
		codeMap.put("FD8E", new OperationStructure("FD8E", Z80Type.A94, 3, "ADC", "IY", "A", "A<-A+(IY+d)+cy"));
		codeMap.put("FD96", new OperationStructure("FD96", Z80Type.A96, 3, "SUB", "IY", "D8", "A<-A-(IY+d)"));
		codeMap.put("FD9E", new OperationStructure("FD9E", Z80Type.A94, 3, "SBC", "IY", "A", "A<-A-(IY+d)-cy"));
		codeMap.put("FDA6", new OperationStructure("FDA6", Z80Type.A96, 3, "AND", "IY", "D8", "A<-A and (IY+d)"));
		codeMap.put("FDAE", new OperationStructure("FDAE", Z80Type.A96, 3, "XOR", "IY", "D8", "A<-A xor (IY+d)"));
		codeMap.put("FDB6", new OperationStructure("FDB6", Z80Type.A96, 3, "OR", "IY", "D8", "A<-A or(IY+d)"));
		codeMap.put("FDBE", new OperationStructure("FDBE", Z80Type.A96, 3, "CP", "IY", "D8", "A<-A -(IY+d)"));

		codeMap.put("FDE1", new OperationStructure("FDE1", Z80Type.A00, 2, "POP", "IY", "", "IY<-(SP)"));
		codeMap.put("FDE3", new OperationStructure("FDE3", Z80Type.A00, 2, "EX", "IY", "", "IY<->(SP)"));
		codeMap.put("FDE5", new OperationStructure("FDE5", Z80Type.A00, 2, "PUSH", "IY", "", "(SP)<-IY"));
		codeMap.put("FDE9", new OperationStructure("FDE9", Z80Type.A00, 2, "JP", "IY", "", "PC<-IY"));
		codeMap.put("FDF9", new OperationStructure("FDF9", Z80Type.A92, 2, "JP", "SP", "IY", "SP<-IY"));

		/* INDEXED BIT INSTRUCTIONS IY FDXXYY */

		codeMap.put("FDCB06",
				new OperationStructure("FDCB06", Z80Type.A96, 4, "RLC", "IY", "D8", "IY+d:cy<-[b7<-b0]<-b7"));
		codeMap.put("FDCB0E",
				new OperationStructure("FDCB0E", Z80Type.A96, 4, "RRC", "IY", "D8", "IY+d:b0->[b7->b0]->cy"));
		codeMap.put("FDCB16",
				new OperationStructure("FDCB16", Z80Type.A96, 4, "RL", "IY", "D8", "IY+d:cy<-[b7<-b0]<-cy"));
		codeMap.put("FDCB1E",
				new OperationStructure("FDCB1E", Z80Type.A96, 4, "RR", "IY", "D8", "IY+d:cy->[b7->b0]->cy"));
		codeMap.put("FDCB26",
				new OperationStructure("FDCB26", Z80Type.A96, 4, "SLA", "IY", "D8", "IY+d:cy<-[b7<-b0]<-0"));
		codeMap.put("FDCB2E",
				new OperationStructure("FDCB2E", Z80Type.A96, 4, "SRA", "IY", "D8", "IY+d:b7->[b7->b0]->cy"));
		codeMap.put("FDCB3E",
				new OperationStructure("FDCB3E", Z80Type.A96, 4, "SRL", "IY", "D8", "IY+d:0->[b7->b0]->cy"));

		codeMap.put("FDCB46", new OperationStructure("FDCB46", Z80Type.A85, 4, "BIT", "IY", "0", " b0 & IY+d"));
		codeMap.put("FDCB4E", new OperationStructure("FDCB4E", Z80Type.A85, 4, "BIT", "IY", "1", " b1 & IY+d"));
		codeMap.put("FDCB56", new OperationStructure("FDCB56", Z80Type.A85, 4, "BIT", "IY", "2", " b2 & IY+d"));
		codeMap.put("FDCB5E", new OperationStructure("FDCB5E", Z80Type.A85, 4, "BIT", "IY", "3", " b3 & IY+d"));
		codeMap.put("FDCB66", new OperationStructure("FDCB66", Z80Type.A85, 4, "BIT", "IY", "4", " b4 & IY+d"));
		codeMap.put("FDCB6E", new OperationStructure("FDCB6E", Z80Type.A85, 4, "BIT", "IY", "5", " b5 & IY+d"));
		codeMap.put("FDCB76", new OperationStructure("FDCB76", Z80Type.A85, 4, "BIT", "IY", "6", " b6 & IY+d"));
		codeMap.put("FDCB7E", new OperationStructure("FDCB7E", Z80Type.A85, 4, "BIT", "IY", "7", " b7 & IY+d"));

		codeMap.put("FDCB86", new OperationStructure("FDCB86", Z80Type.A85, 4, "RES", "IY", "0", "0-> b0 in IY+d"));
		codeMap.put("FDCB8E", new OperationStructure("FDCB8E", Z80Type.A85, 4, "RES", "IY", "1", "0-> b1 in IY+d"));
		codeMap.put("FDCB96", new OperationStructure("FDCB96", Z80Type.A85, 4, "RES", "IY", "2", "0-> b2 in IY+d"));
		codeMap.put("FDCB9E", new OperationStructure("FDCB9E", Z80Type.A85, 4, "RES", "IY", "3", "0-> b3 in IY+d"));
		codeMap.put("FDCBA6", new OperationStructure("FDCBA6", Z80Type.A85, 4, "RES", "IY", "4", "0-> b4 in IY+d"));
		codeMap.put("FDCBAE", new OperationStructure("FDCBAE", Z80Type.A85, 4, "RES", "IY", "5", "0-> b5 in IY+d"));
		codeMap.put("FDCBB6", new OperationStructure("FDCBB6", Z80Type.A85, 4, "RES", "IY", "6", "0-> b6 in IY+d"));
		codeMap.put("FDCBBE", new OperationStructure("FDCBBE", Z80Type.A85, 4, "RES", "IY", "7", "0-> b7 in IY+d"));

		codeMap.put("FDCBC6", new OperationStructure("FDCBC6", Z80Type.A85, 4, "SET", "IY", "0", "1-> b0 in IY+d"));
		codeMap.put("FDCBCE", new OperationStructure("FDCBCE", Z80Type.A85, 4, "SET", "IY", "1", "1-> b1 in IY+d"));
		codeMap.put("FDCBD6", new OperationStructure("FDCBD6", Z80Type.A85, 4, "SET", "IY", "2", "1-> b2 in IY+d"));
		codeMap.put("FDCBDE", new OperationStructure("FDCBDE", Z80Type.A85, 4, "SET", "IY", "3", "1-> b3 in IY+d"));
		codeMap.put("FDCBE6", new OperationStructure("FDCBE6", Z80Type.A85, 4, "SET", "IY", "4", "1-> b4 in IY+d"));
		codeMap.put("FDCBEE", new OperationStructure("FDCBEE", Z80Type.A85, 4, "SET", "IY", "5", "1-> b5 in IY+d"));
		codeMap.put("FDCBF6", new OperationStructure("FDCBF6", Z80Type.A85, 4, "SET", "IY", "6", "1-> b6 in IY+d"));
		codeMap.put("FDCBFE", new OperationStructure("FDCBFE", Z80Type.A85, 4, "SET", "IY", "7", "1-> b7 in IY+d"));

	}// makeIndexTypes

	private void makeExtendedTypes() {
		// codeMap.put(key , new OperationStructure(key, Z80Type.BIT, 2, instruction, register, "", function));

		codeMap.put("ED40", new OperationStructure("ED40", Z80Type.A92, 2, "IN", "B", "(C)", "B<-(C)"));
		codeMap.put("ED41", new OperationStructure("ED41", Z80Type.A92, 2, "OUT", "(C)", "B", "(C)<-B"));
		codeMap.put("ED42", new OperationStructure("ED42", Z80Type.A92, 2, "SBC", "HL", "BC", "HL<-HL-BC-cy"));
		codeMap.put("ED43", new OperationStructure("ED43", Z80Type.A91, 4, "LD", "addr", "BC", "(mm)<-BC"));
		codeMap.put("ED44", new OperationStructure("ED44", Z80Type.A99, 2, "NEG", "", "", "A<- 0-A"));
		codeMap.put("ED45", new OperationStructure("ED45", Z80Type.A99, 2, "RETN", "", "", "PC <- (SP)"));
		codeMap.put("ED46", new OperationStructure("ED46", Z80Type.A00, 2, "IM", "0", "", "Set mode 0"));
		codeMap.put("ED47", new OperationStructure("ED47", Z80Type.A92, 2, "LD", "I", "A", "I <- A"));
		codeMap.put("ED48", new OperationStructure("ED48", Z80Type.A92, 2, "IN", "C", "(C)", "C<-(C)"));
		codeMap.put("ED49", new OperationStructure("ED49", Z80Type.A92, 2, "OUT", "(C)", "C", "(C)<-C"));
		codeMap.put("ED4A", new OperationStructure("ED4A", Z80Type.A92, 2, "ADC", "HL", "BC", "HL<-HL+BC+cy"));
		codeMap.put("ED4B", new OperationStructure("ED4B", Z80Type.A90, 4, "LD", "BC", "addr", "BC<-(HL)"));
		codeMap.put("ED4C", new OperationStructure("ED4C", Z80Type.A99, 2, "NEG", "", "", "A<- 0-A *UD"));
		codeMap.put("ED4D", new OperationStructure("ED4D", Z80Type.A99, 2, "RETI", "", "", "PC <- (SP)"));
		codeMap.put("ED4E", new OperationStructure("ED4E", Z80Type.A00, 2, "IM", "0/1", "", "Set mode 0/1 *UD"));
		codeMap.put("ED4F", new OperationStructure("ED4F", Z80Type.A92, 2, "LD", "R", "A", "R <- A"));
		codeMap.put("ED50", new OperationStructure("ED50", Z80Type.A92, 2, "IN", "D", "(C)", "D<-(C)"));
		codeMap.put("ED51", new OperationStructure("ED51", Z80Type.A92, 2, "OUT", "(C)", "D", "(c)<-D)"));
		codeMap.put("ED52", new OperationStructure("ED52", Z80Type.A92, 2, "SBC", "HL", "DE", "HL<-HL-DE-cy"));
		codeMap.put("ED53", new OperationStructure("ED53", Z80Type.A91, 4, "LD", "addr", "DE", "(mm)<-DE"));
		codeMap.put("ED54", new OperationStructure("ED54", Z80Type.A99, 2, "NEG", "", "", "A<- 0-A"));
		codeMap.put("ED55", new OperationStructure("ED55", Z80Type.A99, 2, "RETN", "", "", "PC <- (SP)"));
		codeMap.put("ED56", new OperationStructure("ED56", Z80Type.A00, 2, "IM", "1", "", "Set mode 1"));
		codeMap.put("ED57", new OperationStructure("ED57", Z80Type.A92, 2, "LD", "A", "I", "A <- I"));
		codeMap.put("ED58", new OperationStructure("ED58", Z80Type.A92, 2, "IN", "E", "(C)", "E<-(C)"));
		codeMap.put("ED59", new OperationStructure("ED59", Z80Type.A92, 2, "OUT", "(C)", "E", "(C)<-E"));
		codeMap.put("ED5A", new OperationStructure("ED5A", Z80Type.A92, 2, "ADC", "HL", "DE", "HL,-HL+DE+cy"));
		codeMap.put("ED5B", new OperationStructure("ED5B", Z80Type.A90, 4, "LD", "DE", "addr", "DE<-(HL)"));
		codeMap.put("ED5C", new OperationStructure("ED5C", Z80Type.A99, 2, "NEG", "", "", "A<- 0-A "));
		codeMap.put("ED5D", new OperationStructure("ED5D", Z80Type.A99, 2, "RETN", "", "", "PC <- (SP)"));
		codeMap.put("ED5E", new OperationStructure("ED5E", Z80Type.A00, 2, "IM", "2", "", "Set mode 2"));
		codeMap.put("ED5F", new OperationStructure("ED5F", Z80Type.A92, 2, "LD", "A", "R", "A <- R"));
		codeMap.put("ED60", new OperationStructure("ED60", Z80Type.A92, 2, "IN", "H", "(C)", "H<-(C)"));
		codeMap.put("ED61", new OperationStructure("ED61", Z80Type.A92, 2, "OUT", "(C)", "H", "(C)<-H"));
		codeMap.put("ED62", new OperationStructure("ED62", Z80Type.A92, 2, "SBC", "HL", "HL", "HL<-HL-HL-cy"));
		codeMap.put("ED63", new OperationStructure("ED63", Z80Type.A91, 4, "LD", "addr", "HL", "(mm)<-HL"));
		codeMap.put("ED64", new OperationStructure("ED64", Z80Type.A99, 2, "NEG", "", "", "A<- 0-A"));
		codeMap.put("ED65", new OperationStructure("ED65", Z80Type.A99, 2, "RETN", "", "", "PC <- (SP)"));
		codeMap.put("ED66", new OperationStructure("ED66", Z80Type.A00, 2, "IM", "0", "", "Set mode 0"));
		codeMap.put("ED67", new OperationStructure("ED57", Z80Type.A99, 2, "RRD", "", "", ""));
		codeMap.put("ED68", new OperationStructure("ED68", Z80Type.A92, 2, "IN", "L", "(C)", "L<-(C)"));
		codeMap.put("ED69", new OperationStructure("ED69", Z80Type.A92, 2, "OUT", "(C)", "L", "(C)<-L"));
		codeMap.put("ED6A", new OperationStructure("ED6A", Z80Type.A92, 2, "ADC", "HL", "HL", "HL,-HL+HL+cy"));
		codeMap.put("ED6B", new OperationStructure("ED6B", Z80Type.A90, 4, "LD", "HL", "addr", "HL<-(HL)"));
		codeMap.put("ED6C", new OperationStructure("ED6C", Z80Type.A99, 2, "NEG", "", "", "A<- 0-A *UD"));
		codeMap.put("ED6D", new OperationStructure("ED6D", Z80Type.A99, 2, "RETN", "", "", "PC <- (SP)"));
		codeMap.put("ED6E", new OperationStructure("ED6E", Z80Type.A00, 2, "IM", "0/1", "", "Set mode 0/1 *UD"));
		codeMap.put("ED6F", new OperationStructure("ED6F", Z80Type.A99, 2, "RLD", "", "", ""));
		codeMap.put("ED71",
				new OperationStructure("ED71", Z80Type.A92, 2, "OUT", "(C)", "0", "undocumented/unsupported"));
		codeMap.put("ED72", new OperationStructure("ED72", Z80Type.A92, 2, "SBC", "HL", "SP", "HL<-HL-SP-cy"));
		codeMap.put("ED73", new OperationStructure("ED73", Z80Type.A91, 4, "LD", "addr", "SP", "(mm)<-SP"));
		codeMap.put("ED74", new OperationStructure("ED74", Z80Type.A99, 2, "NEG", "", "", "A<- 0-A"));
		codeMap.put("ED75", new OperationStructure("ED75", Z80Type.A99, 2, "RETN", "", "", "PC <- (SP)"));
		codeMap.put("ED76", new OperationStructure("ED76", Z80Type.A00, 2, "IM", "1", "", "Set mode 1"));
		codeMap.put("ED78", new OperationStructure("ED78", Z80Type.A92, 2, "IN", "A", "(C)", "A<-(C)"));
		codeMap.put("ED79", new OperationStructure("ED79", Z80Type.A92, 2, "OUT", "(C)", "A", "(C)<-A"));
		codeMap.put("ED7A", new OperationStructure("ED7A", Z80Type.A92, 2, "ADC", "HL", "SP", "HL,-HL+SP+cy"));
		codeMap.put("ED7B", new OperationStructure("ED7B", Z80Type.A90, 4, "LD", "SP", "addr", "SP<-(HL)"));
		codeMap.put("ED7C", new OperationStructure("ED7C", Z80Type.A99, 2, "NEG", "", "", "A<- 0-A *UD"));
		codeMap.put("ED7D", new OperationStructure("ED7D", Z80Type.A99, 2, "RETN", "", "", "PC <- (SP)"));
		codeMap.put("ED7E", new OperationStructure("ED7E", Z80Type.A00, 2, "IM", "2", "", "Set mode 2"));

		codeMap.put("EDA0", new OperationStructure("EDA0", Z80Type.A99, 2, "LDI", "", "",
				"(DE)<-(HL), DE<-DE + 1, HL<-HL + 1, BC<-BC -1"));
		codeMap.put("EDA1",
				new OperationStructure("EDA1", Z80Type.A99, 2, "CPI", "", "", "A- (HL), HL ← HL +1, BC ← BC -1"));
		codeMap.put("EDA2",
				new OperationStructure("EDA2", Z80Type.A99, 2, "INI", "", "", "(HL) ← (C), B ← B -1, HL ← HL + 1"));
		codeMap.put("EDA3",
				new OperationStructure("EDA3", Z80Type.A99, 2, "OUTI", "", "", "(C) ← (HL), B ← B -1, HL ← HL + 1"));
		codeMap.put("EDA8", new OperationStructure("EDA8", Z80Type.A99, 2, "LDD", "", "",
				"(DE) ← (HL), DE ← DE -1, HL ← HL-1, BC ← BC-1"));
		codeMap.put("EDA9",
				new OperationStructure("EDA9", Z80Type.A99, 2, "CPD", "", "", "A -(HL), HL ← HL -1, BC ← BC -1"));
		codeMap.put("EDAA",
				new OperationStructure("EDAA", Z80Type.A99, 2, "IND", "", "", "(HL) ← (C), B ← B -1, HL ← HL -1"));
		codeMap.put("EDAB",
				new OperationStructure("EDAB", Z80Type.A99, 2, "OUTD", "", "", "(C) ← (HL), B ← B -1, HL ← HL -1"));

		codeMap.put("EDB0", new OperationStructure("EDB0", Z80Type.A99, 2, "LDIR", "", "",
				"(DE) ← (HL), DE ← DE + 1, HL ← HL + 1, BC F↔ BC -1"));
		codeMap.put("EDB1",
				new OperationStructure("EDB1", Z80Type.A99, 2, "CPIR", "", "", "A-(HL), HL ← HL+1, BC ← BC-1"));
		codeMap.put("EDB2",
				new OperationStructure("EDB2", Z80Type.A99, 2, "INIR", "", "", "(HL) ← (C), B ← B -1, HL ← HL +1"));
		codeMap.put("EDB3",
				new OperationStructure("EDB3", Z80Type.A99, 2, "OTIR", "", "", "(C) ← (HL), B ← B -1, HL ← HL + 1"));
		codeMap.put("EDB8", new OperationStructure("EDB8", Z80Type.A99, 2, "LDDR", "", "",
				"(DE) ← (HL), DE ← D ← 1, HL ← HL-1, BC ← BC-1"));
		codeMap.put("EDB9",
				new OperationStructure("EDB9", Z80Type.A99, 2, "CPDR", "", "", "A -(HL), HL ← HL -1, BC ← BC -1"));
		codeMap.put("EDBA",
				new OperationStructure("EDBA", Z80Type.A99, 2, "INDR", "", "", "(HL) ← (C), B ← 131, HL ← HL1"));
		codeMap.put("EDBB",
				new OperationStructure("EDBB", Z80Type.A99, 2, "OTDR", "", "", "(C) ← (HL), B ← B - 1, HL ← HL - 1"));

	}// makeExtendedTypes

	private void makeBitTypes() {
		String key;
		String[] registers = new String[] { "B", "C", "D", "E", "H", "L", "(HL)", "A" };
		String register;
		String bitString = "";
		String instruction = "";
		String function = "";
		for (int code = 0; code < 0x40; code++) {
			int hiByte = (int) (code / 8);
			switch (hiByte) {
			case 0:
				instruction = "RLC";
				function = "cy<-[b7<-b0]<-b7";
				break;
			case 1:
				instruction = "RRC";
				function = "b0->[b7->b0]->cy";
				break;
			case 2:
				instruction = "RL";
				function = "cy<-[b7<-b0]<-cy";
				break;
			case 3:
				instruction = "RR";
				function = "cy->[b7->b0]->cy";
				break;
			case 4:
				instruction = "SLA";
				function = "cy<-[b7<-b0]<-0";
				break;
			case 5:
				instruction = "SRA";
				function = "b7->[b7->b0]->cy";
				break;
			case 6:
				instruction = "SLL";
				function = "cy<-[b7<-b0]<-1 *undoc";
				break;
			case 7:
				instruction = "SRL";
				function = "0->[b7->b0]->cy";
				break;
			}// switch
			register = registers[code % 8];

			key = String.format("CB%02X", code);
			codeMap.put(key, new OperationStructure(key, Z80Type.A00, 2, instruction, register, "", function));

			// System.out.printf("%s,Z80Type.BIT,2,%s,%s,MT,%s%n", key,instruction,register,function);

		} // for lower instructions
		String operation = "";
		for (int code = 0x40; code < 0x100; code++) {
			int hiByte = (int) (code / 0x40);
			switch (hiByte) {
			case 0:
				instruction = "BAD";
				break;
			case 1:
				instruction = "BIT";
				operation = "Test";
				break;
			case 2:
				instruction = "RES";
				operation = "Reset";
				break;
			case 3:
				instruction = "SET";
				operation = "Set";
				break;
			}// switch instruction

			int bitBase = code - (hiByte * 0x40);

			switch (bitBase / 8) {
			case 0:
				bitString = "0";
				break;
			case 1:
				bitString = "1";
				break;
			case 2:
				bitString = "2";
				break;
			case 3:
				bitString = "3";
				break;
			case 4:
				bitString = "4";
				break;
			case 5:
				bitString = "5";
				break;
			case 6:
				bitString = "6";
				break;
			case 7:
				bitString = "7";
				break;
			}// switch bits
			key = String.format("CB%02X", code);
			register = registers[code % 8];
			function = String.format("%s bit %s in register %s", operation, bitString, register);
			codeMap.put(key, new OperationStructure(key, Z80Type.A92, 2, instruction, bitString, register, function));
			// System.out.printf("%s,Z80Type.BIT,2,%s,%s,%s,%s%n", key,instruction,register,bitString,function);

		} // for bits and registers
	}// makeBitTypes

	private void makeMainTypes() {
		// codeMap = new HashMap<Byte, OperationStructure>();
		codeMap.put("00", new OperationStructure("00", Z80Type.A99, 1, "NOP", "", "", ""));
		codeMap.put("01", new OperationStructure("01", Z80Type.A89, 3, "LD", "BC", "D16", "B<- byte3,C<- byte2"));
		codeMap.put("02", new OperationStructure("02", Z80Type.A92, 1, "LD", "(BC)", "A", "(BC)<-A"));
		codeMap.put("03", new OperationStructure("03", Z80Type.A00, 1, "INC", "BC", "", "BC<-BC+1"));
		codeMap.put("04", new OperationStructure("04", Z80Type.A00, 1, "INC", "B", "", "B<-B+1"));
		codeMap.put("05", new OperationStructure("05", Z80Type.A00, 1, "DEC", "B", "", "B<-B-1"));
		codeMap.put("06", new OperationStructure("06", Z80Type.A98, 2, "LD", "B", "D8", "B<-byte2"));
		codeMap.put("07", new OperationStructure("07", Z80Type.A99, 1, "RLCA", "", "", "A=A << not thru carry"));
		codeMap.put("08", new OperationStructure("08", Z80Type.A92, 1, "EX", "AF", "AF'", "AF<->AF'"));
		codeMap.put("09", new OperationStructure("09", Z80Type.A92, 1, "ADD", "HL", "BC", "HL = HL + BC"));
		codeMap.put("0A", new OperationStructure("0A", Z80Type.A92, 1, "LD", "A", "(BC)", "A<-(BC)"));
		codeMap.put("0B", new OperationStructure("0B", Z80Type.A00, 1, "DEC", "BC", "", "BC = BC-1"));
		codeMap.put("0C", new OperationStructure("0C", Z80Type.A00, 1, "INC", "C", "", "C <-C+1"));
		codeMap.put("0D", new OperationStructure("0D", Z80Type.A00, 1, "DEC", "C", "", "C <-C-1"));
		codeMap.put("0E", new OperationStructure("0E", Z80Type.A98, 2, "LD", "C", "D8", "C,-byte2"));
		codeMap.put("0F", new OperationStructure("0F", Z80Type.A99, 1, "RRCA", "", "", "A = A>> not thru carry"));
		//
		codeMap.put("10", new OperationStructure("10", Z80Type.A97, 2, "DJNZ", "D8", "",
				"B<-B-1; if not Zero,(PC)<-(PC)+immediate"));
		codeMap.put("11", new OperationStructure("11", Z80Type.A89, 3, "LD", "DE", "D16", "D<-byte3,E<-byte2"));
		codeMap.put("12", new OperationStructure("12", Z80Type.A92, 1, "LD", "(DE)", "A", "(DE)<-A"));
		codeMap.put("13", new OperationStructure("13", Z80Type.A00, 1, "INC", "DE", "", "DE<-DE + 1"));
		codeMap.put("14", new OperationStructure("14", Z80Type.A00, 1, "INC", "D", "", "D<-D+1"));
		codeMap.put("15", new OperationStructure("15", Z80Type.A00, 1, "DEC", "D", "", "D<-D-1"));
		codeMap.put("16", new OperationStructure("16", Z80Type.A98, 2, "LD", "D", "D8", "D<-byte2"));
		codeMap.put("17", new OperationStructure("17", Z80Type.A99, 1, "RLA", "", "", "A=A << thru carry"));
		codeMap.put("18", new OperationStructure("18", Z80Type.A97, 2, "JR", "D8", "", "(PC)<-(PC)+immediate"));
		codeMap.put("19", new OperationStructure("19", Z80Type.A92, 1, "ADD", "HL", "DE", "HL = HL + DE"));
		codeMap.put("1A", new OperationStructure("1A", Z80Type.A92, 1, "LD", "A", "(DE)", "A<-(DE)"));
		codeMap.put("1B", new OperationStructure("1B", Z80Type.A00, 1, "DEC", "DE", "", "DE = DE-1"));
		codeMap.put("1C", new OperationStructure("1C", Z80Type.A00, 1, "INC", "E", "", "E <-E+1"));
		codeMap.put("1D", new OperationStructure("1D", Z80Type.A00, 1, "DEC", "E", "", "E <-E-1"));
		codeMap.put("1E", new OperationStructure("1E", Z80Type.A98, 2, "LD", "E", "D8", "E,-byte2"));
		codeMap.put("1F", new OperationStructure("1f", Z80Type.A99, 1, "RRA", "", "", "A = A>>  thru carry"));
		//
		codeMap.put("20",
				new OperationStructure("20", Z80Type.A98, 2, "JR", "NZ", "D8", "if Z not set,(PC)<-(PC)+immediate"));
		codeMap.put("21", new OperationStructure("21", Z80Type.A89, 3, "LD", "HL", "D16", "H<-byte3,L<-byte2"));
		codeMap.put("22", new OperationStructure("22", Z80Type.A87, 3, "LD", "addr", "HL", "(addr)<-L;(addr+1)<-H"));
		codeMap.put("23", new OperationStructure("23", Z80Type.A00, 1, "INC", "HL", "", "HL<-HL + 1"));
		codeMap.put("24", new OperationStructure("24", Z80Type.A00, 1, "INC", "H", "", "H<-H+1"));
		codeMap.put("25", new OperationStructure("25", Z80Type.A00, 1, "DEC", "H", "", "H<-H-1"));
		codeMap.put("26", new OperationStructure("26", Z80Type.A98, 2, "LD", "H", "D8", "H<-byte2"));
		codeMap.put("27", new OperationStructure("27", Z80Type.A99, 1, "DAA", "", "", "Decimal Adjust Acc."));
		codeMap.put("28", new OperationStructure("28", Z80Type.A98, 2, "JR", "Z", "D8", "if Z is set,(PC)<- (PC)+d8"));
		codeMap.put("29", new OperationStructure("29", Z80Type.A92, 1, "ADD", "HL", "HL", "HL = HL + HL"));
		codeMap.put("2A", new OperationStructure("2A", Z80Type.A86, 3, "LD", "HL", "addr", "L<-(addr);H<-(addr+1)"));
		codeMap.put("2B", new OperationStructure("2B", Z80Type.A00, 1, "DEC", "HL", "", "HL = HL-1"));
		codeMap.put("2C", new OperationStructure("2C", Z80Type.A00, 1, "INC", "L", "", "L <-L+1"));
		codeMap.put("2D", new OperationStructure("2D", Z80Type.A00, 1, "DEC", "L", "", "L <-L-1"));
		codeMap.put("2E", new OperationStructure("2E", Z80Type.A98, 2, "LD", "L", "D8", "L,-byte2"));
		codeMap.put("2F", new OperationStructure("2F", Z80Type.A99, 1, "CPL", "", "", "A = !A"));
		//
		codeMap.put("30", new OperationStructure("30", Z80Type.A98, 2, "JR", "NC", "D8",
				"if Carry not set,(PC)<-(PC)+immediate"));
		codeMap.put("31", new OperationStructure("31", Z80Type.A89, 3, "LXI", "SP", "D16", "SP-data"));
		codeMap.put("32", new OperationStructure("32", Z80Type.A87, 3, "LD", "addr", "A", "(addr)<-A"));
		codeMap.put("33", new OperationStructure("33", Z80Type.A00, 1, "INC", "SP", "", "SP<-SP + 1"));
		codeMap.put("34", new OperationStructure("34", Z80Type.A00, 1, "INC", "(HL)", "", "(HL)<-(HL)+1"));
		codeMap.put("35", new OperationStructure("35", Z80Type.A00, 1, "DEC", "(HL)", "", "(HL)<-(HL)-1"));
		codeMap.put("36", new OperationStructure("36", Z80Type.A98, 2, "LD", "(HL)", "D8", "(HL)<-byte2"));
		codeMap.put("37", new OperationStructure("37", Z80Type.A99, 1, "SCF", "", "", "CY=1"));
		codeMap.put("38",
				new OperationStructure("38", Z80Type.A98, 2, "JR", "C", "D8", "If CY set,(PC)<-(PC)-immediate"));
		codeMap.put("39", new OperationStructure("39", Z80Type.A92, 1, "ADD", "HL", "SP", "HL = HL + SP"));
		codeMap.put("3A", new OperationStructure("3A", Z80Type.A86, 3, "LD", "A", "addr", "A<-(addr)"));
		codeMap.put("3B", new OperationStructure("3B", Z80Type.A00, 1, "DEC", "SP", "", "SP = SP-1"));
		codeMap.put("3C", new OperationStructure("3C", Z80Type.A00, 1, "INC", "A", "", "A <-A+1"));
		codeMap.put("3D", new OperationStructure("3D", Z80Type.A00, 1, "DEC", "A", "", "A <-A-1"));
		codeMap.put("3E", new OperationStructure("3E", Z80Type.A98, 2, "LD", "A", "D8", "A,-byte2"));
		codeMap.put("3F", new OperationStructure("3F", Z80Type.A99, 1, "CCF", "", "", "CY=!CY"));
		//
		codeMap.put("40", new OperationStructure("40", Z80Type.A92, 1, "LD", "B", "B", "B <- B"));
		codeMap.put("41", new OperationStructure("41", Z80Type.A92, 1, "LD", "B", "C", "B <- C"));
		codeMap.put("42", new OperationStructure("42", Z80Type.A92, 1, "LD", "B", "D", "B <- D"));
		codeMap.put("43", new OperationStructure("43", Z80Type.A92, 1, "LD", "B", "E", "B <- E"));
		codeMap.put("44", new OperationStructure("44", Z80Type.A92, 1, "LD", "B", "H", "B <- H"));
		codeMap.put("45", new OperationStructure("45", Z80Type.A92, 1, "LD", "B", "L", "B <- L"));
		codeMap.put("46", new OperationStructure("46", Z80Type.A92, 1, "LD", "B", "(HL)", "B <- (HL)"));
		codeMap.put("47", new OperationStructure("47", Z80Type.A92, 1, "LD", "B", "A", "B <- A"));
		// Z80Type.A92,
		codeMap.put("48", new OperationStructure("48", Z80Type.A92, 1, "LD", "C", "B", "C <- B"));
		codeMap.put("49", new OperationStructure("49", Z80Type.A92, 1, "LD", "C", "C", "C <- C"));
		codeMap.put("4A", new OperationStructure("4A", Z80Type.A92, 1, "LD", "C", "D", "C <- D"));
		codeMap.put("4B", new OperationStructure("4B", Z80Type.A92, 1, "LD", "C", "E", "C <- E"));
		codeMap.put("4C", new OperationStructure("4C", Z80Type.A92, 1, "LD", "C", "H", "C <- H"));
		codeMap.put("4D", new OperationStructure("4D", Z80Type.A92, 1, "LD", "C", "L", "C <- L"));
		codeMap.put("4E", new OperationStructure("4E", Z80Type.A92, 1, "LD", "C", "(HL)", "C <- (HL)"));
		codeMap.put("4F", new OperationStructure("4F", Z80Type.A92, 1, "LD", "C", "A", "C <- A"));
		//
		codeMap.put("50", new OperationStructure("50", Z80Type.A92, 1, "LD", "D", "B", "D <- B"));
		codeMap.put("51", new OperationStructure("51", Z80Type.A92, 1, "LD", "D", "C", "D <- C"));
		codeMap.put("52", new OperationStructure("52", Z80Type.A92, 1, "LD", "D", "D", "D <- D"));
		codeMap.put("53", new OperationStructure("53", Z80Type.A92, 1, "LD", "D", "E", "D <- E"));
		codeMap.put("54", new OperationStructure("54", Z80Type.A92, 1, "LD", "D", "H", "D <- H"));
		codeMap.put("55", new OperationStructure("55", Z80Type.A92, 1, "LD", "D", "L", "D <- L"));
		codeMap.put("56", new OperationStructure("56", Z80Type.A92, 1, "LD", "D", "(HL)", "D <- (HL)"));
		codeMap.put("57", new OperationStructure("57", Z80Type.A92, 1, "LD", "D", "A", "D <- A"));
		//
		codeMap.put("58", new OperationStructure("58", Z80Type.A92, 1, "LD", "E", "B", "E <- B"));
		codeMap.put("59", new OperationStructure("59", Z80Type.A92, 1, "LD", "E", "C", "E <- C"));
		codeMap.put("5A", new OperationStructure("5A", Z80Type.A92, 1, "LD", "E", "D", "E <- D"));
		codeMap.put("5B", new OperationStructure("5B", Z80Type.A92, 1, "LD", "E", "E", "E <- E"));
		codeMap.put("5C", new OperationStructure("5C", Z80Type.A92, 1, "LD", "E", "H", "E <- H"));
		codeMap.put("5D", new OperationStructure("5D", Z80Type.A92, 1, "LD", "E", "L", "E <- L"));
		codeMap.put("5E", new OperationStructure("5E", Z80Type.A92, 1, "LD", "E", "(HL)", "E <- (HL)"));
		codeMap.put("5F", new OperationStructure("5F", Z80Type.A92, 1, "LD", "E", "A", "E <- A"));
		//
		codeMap.put("60", new OperationStructure("60", Z80Type.A92, 1, "LD", "H", "B", "H <- B"));
		codeMap.put("61", new OperationStructure("61", Z80Type.A92, 1, "LD", "H", "C", "H <- C"));
		codeMap.put("62", new OperationStructure("62", Z80Type.A92, 1, "LD", "H", "D", "H <- D"));
		codeMap.put("63", new OperationStructure("63", Z80Type.A92, 1, "LD", "H", "E", "H <- E"));
		codeMap.put("64", new OperationStructure("64", Z80Type.A92, 1, "LD", "H", "H", "H <- H"));
		codeMap.put("65", new OperationStructure("65", Z80Type.A92, 1, "LD", "H", "L", "H <- L"));
		codeMap.put("66", new OperationStructure("66", Z80Type.A92, 1, "LD", "H", "(HL)", "H <- (HL)"));
		codeMap.put("67", new OperationStructure("67", Z80Type.A92, 1, "LD", "H", "A", "H <- A"));
		//
		codeMap.put("68", new OperationStructure("68", Z80Type.A92, 1, "LD", "L", "B", "L <- B"));
		codeMap.put("69", new OperationStructure("69", Z80Type.A92, 1, "LD", "L", "C", "L <- C"));
		codeMap.put("6A", new OperationStructure("6A", Z80Type.A92, 1, "LD", "L", "D", "L <- D"));
		codeMap.put("6B", new OperationStructure("6B", Z80Type.A92, 1, "LD", "L", "E", "L <- E"));
		codeMap.put("6C", new OperationStructure("6C", Z80Type.A92, 1, "LD", "L", "H", "L <- H"));
		codeMap.put("6D", new OperationStructure("6D", Z80Type.A92, 1, "LD", "L", "L", "L <- L"));
		codeMap.put("6E", new OperationStructure("6E", Z80Type.A92, 1, "LD", "L", "(HL)", "L <- (HL)"));
		codeMap.put("6F", new OperationStructure("6F", Z80Type.A92, 1, "LD", "L", "A", "L <- A"));
		//
		codeMap.put("70", new OperationStructure("70", Z80Type.A92, 1, "LD", "(HL)", "B", "(HL) <- B"));
		codeMap.put("71", new OperationStructure("71", Z80Type.A92, 1, "LD", "(HL)", "C", "(HL) <- C"));
		codeMap.put("72", new OperationStructure("72", Z80Type.A92, 1, "LD", "(HL)", "D", "(HL) <- D"));
		codeMap.put("73", new OperationStructure("73", Z80Type.A92, 1, "LD", "(HL)", "E", "(HL) <- E"));
		codeMap.put("74", new OperationStructure("74", Z80Type.A92, 1, "LD", "(HL)", "H", "(HL) <- H"));
		codeMap.put("75", new OperationStructure("75", Z80Type.A92, 1, "LD", "(HL)", "L", "(HL) <- L"));
		codeMap.put("76", new OperationStructure("76", Z80Type.A99, 1, "HLT", "", "", "Halt")); // Special
		codeMap.put("77", new OperationStructure("77", Z80Type.A92, 1, "LD", "(HL)", "A", "(HL) <- A"));
		//
		codeMap.put("78", new OperationStructure("78", Z80Type.A92, 1, "LD", "A", "B", "A <- B"));
		codeMap.put("79", new OperationStructure("79", Z80Type.A92, 1, "LD", "A", "C", "A <- C"));
		codeMap.put("7A", new OperationStructure("7A", Z80Type.A92, 1, "LD", "A", "D", "A <- D"));
		codeMap.put("7B", new OperationStructure("7B", Z80Type.A92, 1, "LD", "A", "E", "A <- E"));
		codeMap.put("7C", new OperationStructure("7C", Z80Type.A92, 1, "LD", "A", "H", "A <- H"));
		codeMap.put("7D", new OperationStructure("7D", Z80Type.A92, 1, "LD", "A", "L", "A <- L"));
		codeMap.put("7E", new OperationStructure("7E", Z80Type.A92, 1, "LD", "A", "(HL)", "A <- (HL)"));
		codeMap.put("7F", new OperationStructure("7F", Z80Type.A92, 1, "LD", "A", "A", "A <- A"));
		//
		codeMap.put("80", new OperationStructure("80", Z80Type.A92, 1, "ADD", "A", "B", "A <- A+B"));
		codeMap.put("81", new OperationStructure("81", Z80Type.A92, 1, "ADD", "A", "C", "A <- A+C"));
		codeMap.put("82", new OperationStructure("82", Z80Type.A92, 1, "ADD", "A", "D", "A <- A+D"));
		codeMap.put("83", new OperationStructure("83", Z80Type.A92, 1, "ADD", "A", "E", "A <- A+E"));
		codeMap.put("84", new OperationStructure("84", Z80Type.A92, 1, "ADD", "A", "H", "A <- A+H"));
		codeMap.put("85", new OperationStructure("85", Z80Type.A92, 1, "ADD", "A", "L", "A <- A+L"));
		codeMap.put("86", new OperationStructure("86", Z80Type.A92, 1, "ADD", "A", "(HL)", "A <- A+(HL)"));
		codeMap.put("87", new OperationStructure("87", Z80Type.A92, 1, "ADD", "A", "A", "A <- A+A"));
		//
		codeMap.put("88", new OperationStructure("88", Z80Type.A92, 1, "ADC", "A", "B", "A <- A+B + CY"));
		codeMap.put("89", new OperationStructure("89", Z80Type.A92, 1, "ADC", "A", "C", "A <- A+C + CY"));
		codeMap.put("8A", new OperationStructure("8A", Z80Type.A92, 1, "ADC", "A", "D", "A <- A+D + CY"));
		codeMap.put("8B", new OperationStructure("8B", Z80Type.A92, 1, "ADC", "A", "E", "A <- A+E + CY"));
		codeMap.put("8C", new OperationStructure("8C", Z80Type.A92, 1, "ADC", "A", "H", "A <- A+H + CY"));
		codeMap.put("8D", new OperationStructure("8D", Z80Type.A92, 1, "ADC", "A", "L", "A <- A+L + CY"));
		codeMap.put("8E", new OperationStructure("8E", Z80Type.A92, 1, "ADC", "A", "(HL)", "A <- A+(HL) + CY"));
		codeMap.put("8F", new OperationStructure("8F", Z80Type.A92, 1, "ADC", "A", "A", "A <- A+A + CY"));
		//
		codeMap.put("90", new OperationStructure("90", Z80Type.A00, 1, "SUB", "B", "", "A <- A-B"));
		codeMap.put("91", new OperationStructure("91", Z80Type.A00, 1, "SUB", "C", "", "A <- A-C"));
		codeMap.put("92", new OperationStructure("92", Z80Type.A00, 1, "SUB", "D", "", "A <- A-D"));
		codeMap.put("93", new OperationStructure("93", Z80Type.A00, 1, "SUB", "E", "", "A <- A-E"));
		codeMap.put("94", new OperationStructure("94", Z80Type.A00, 1, "SUB", "H", "", "A <- A-H"));
		codeMap.put("95", new OperationStructure("95", Z80Type.A00, 1, "SUB", "L", "", "A <- A-L"));
		codeMap.put("96", new OperationStructure("96", Z80Type.A00, 1, "SUB", "(HL)", "", "A <- A-(HL)"));
		codeMap.put("97", new OperationStructure("97", Z80Type.A00, 1, "SUB", "A", "", "A <- A-A"));
		//
		codeMap.put("98", new OperationStructure("98", Z80Type.A92, 1, "SBC", "A", "B", "A <- A-B - CY"));
		codeMap.put("99", new OperationStructure("99", Z80Type.A92, 1, "SBC", "A", "C", "A <- A-C - CY"));
		codeMap.put("9A", new OperationStructure("9A", Z80Type.A92, 1, "SBC", "A", "D", "A <- A-D - CY"));
		codeMap.put("9B", new OperationStructure("9B", Z80Type.A92, 1, "SBC", "A", "E", "A <- A-E - CY"));
		codeMap.put("9C", new OperationStructure("9C", Z80Type.A92, 1, "SBC", "A", "H", "A <- A-H - CY"));
		codeMap.put("9D", new OperationStructure("9D", Z80Type.A92, 1, "SBC", "A", "L", "A <- A-L - CY"));
		codeMap.put("9E", new OperationStructure("9E", Z80Type.A92, 1, "SBC", "A", "(HL)", "A <- A-(HL) - CY"));
		codeMap.put("9F", new OperationStructure("9F", Z80Type.A92, 1, "SBC", "A", "A", "A <- A-A - CY"));
		//
		codeMap.put("A0", new OperationStructure("A0", Z80Type.A00, 1, "AND", "B", "", "A <- A&B"));
		codeMap.put("A1", new OperationStructure("A1", Z80Type.A00, 1, "AND", "C", "", "A <- A&C"));
		codeMap.put("A2", new OperationStructure("A2", Z80Type.A00, 1, "AND", "D", "", "A <- A&D"));
		codeMap.put("A3", new OperationStructure("A3", Z80Type.A00, 1, "AND", "E", "", "A <- A&E"));
		codeMap.put("A4", new OperationStructure("A4", Z80Type.A00, 1, "AND", "H", "", "A <- A&H"));
		codeMap.put("A5", new OperationStructure("A5", Z80Type.A00, 1, "AND", "L", "", "A <- A&L"));
		codeMap.put("A6", new OperationStructure("A6", Z80Type.A00, 1, "AND", "(HL)", "", "A <- A&(HL)"));
		codeMap.put("A7", new OperationStructure("A7", Z80Type.A00, 1, "AND", "A", "", "A <- A&A"));
		// ,
		codeMap.put("A8", new OperationStructure("A8", Z80Type.A00, 1, "XOR", "B", "", "A <- A^B"));
		codeMap.put("A9", new OperationStructure("A9", Z80Type.A00, 1, "XOR", "C", "", "A <- A^C"));
		codeMap.put("AA", new OperationStructure("AA", Z80Type.A00, 1, "XOR", "D", "", "A <- A^D"));
		codeMap.put("AB", new OperationStructure("AB", Z80Type.A00, 1, "XOR", "E", "", "A <- A^E"));
		codeMap.put("AC", new OperationStructure("AC", Z80Type.A00, 1, "XOR", "H", "", "A <- A^H"));
		codeMap.put("AD", new OperationStructure("AD", Z80Type.A00, 1, "XOR", "L", "", "A <- A^L"));
		codeMap.put("AE", new OperationStructure("AE", Z80Type.A00, 1, "XOR", "(HL)", "", "A <- A^(HL)"));
		codeMap.put("AF", new OperationStructure("AF", Z80Type.A00, 1, "XOR", "A", "", "A <- A^A"));
		//
		codeMap.put("B0", new OperationStructure("B0", Z80Type.A00, 1, "OR", "B", "", "A <- A|B"));
		codeMap.put("B1", new OperationStructure("B1", Z80Type.A00, 1, "OR", "C", "", "A <- A|C"));
		codeMap.put("B2", new OperationStructure("B2", Z80Type.A00, 1, "OR", "D", "", "A <- A|D"));
		codeMap.put("B3", new OperationStructure("B3", Z80Type.A00, 1, "OR", "E", "", "A <- A|E"));
		codeMap.put("B4", new OperationStructure("B4", Z80Type.A00, 1, "OR", "H", "", "A <- A|H"));
		codeMap.put("B5", new OperationStructure("B5", Z80Type.A00, 1, "OR", "L", "", "A <- A|L"));
		codeMap.put("B6", new OperationStructure("B6", Z80Type.A00, 1, "OR", "(HL)", "", "A <- A|(HL)"));
		codeMap.put("B7", new OperationStructure("B7", Z80Type.A00, 1, "OR", "A", "", "A <- A|A"));
		//
		codeMap.put("B8", new OperationStructure("B8", Z80Type.A00, 1, "CP", "B", "", "A - B"));
		codeMap.put("B9", new OperationStructure("B9", Z80Type.A00, 1, "CP", "C", "", "A - C"));
		codeMap.put("BA", new OperationStructure("BA", Z80Type.A00, 1, "CP", "D", "", "A - D"));
		codeMap.put("BB", new OperationStructure("BB", Z80Type.A00, 1, "CP", "E", "", "A - E"));
		codeMap.put("BC", new OperationStructure("BC", Z80Type.A00, 1, "CP", "H", "", "A - H"));
		codeMap.put("BD", new OperationStructure("BD", Z80Type.A00, 1, "CP", "L", "", "A - L"));
		codeMap.put("BE", new OperationStructure("BE", Z80Type.A00, 1, "CP", "(HL)", "", "A - (HL)"));
		codeMap.put("BF", new OperationStructure("BF", Z80Type.A00, 1, "CP", "A", "", "A - A"));
		//
		codeMap.put("C0", new OperationStructure("C0", Z80Type.A00, 1, "RET", "NZ", "", "if NZ, (PC)<-(SP)"));
		codeMap.put("C1", new OperationStructure("C1", Z80Type.A00, 1, "POP", "BC", "", "(BC<-(SP)"));
		codeMap.put("C2", new OperationStructure("C2", Z80Type.A89, 3, "JP", "NZ", "D16", "if NZ,PC<-D16"));
		codeMap.put("C3", new OperationStructure("C3", Z80Type.A88, 3, "JP", "D16", "", "PC<-D16"));
		codeMap.put("C4",
				new OperationStructure("C4", Z80Type.A89, 3, "CALL", "NZ", "D16", "if NZ,(SP)<-PC,(PC),- address"));
		codeMap.put("C5", new OperationStructure("C5", Z80Type.A00, 1, "PUSH", "BC", "", "(SP)<-(BC)"));
		codeMap.put("C6", new OperationStructure("C6", Z80Type.A98, 2, "ADD", "A", "D8", "A<-A + byte2"));
		codeMap.put("C7", new OperationStructure("C7", Z80Type.A00, 1, "RST", "00H", "", "CALL $0"));
		codeMap.put("C8", new OperationStructure("C8", Z80Type.A00, 1, "RET", "Z", "", "if Z set, PC<-(SP); SP<-SP+2"));
		codeMap.put("C9", new OperationStructure("C9", Z80Type.A99, 1, "RET", "", "", "PC<-(SP); SP<-SP+2"));
		codeMap.put("CA", new OperationStructure("CA", Z80Type.A89, 3, "JP", "Z", "D16", "if Z set,PC<-D16"));
		// /* CB */ Bit instructions makeBitTypes
		codeMap.put("CC", new OperationStructure("CC", Z80Type.A89, 3, "CALL", "Z", "D16", "if Z,CALL D16"));
		codeMap.put("CD", new OperationStructure("CD", Z80Type.A88, 3, "CALL", "D16", "", "CALL D16"));
		codeMap.put("CE", new OperationStructure("CE", Z80Type.A98, 2, "ADC", "A", "D8", "A<- A + data + cy"));
		codeMap.put("CF", new OperationStructure("CF", Z80Type.A00, 1, "RST", "08H", "", "CALL $8"));
		//
		codeMap.put("D0", new OperationStructure("D0", Z80Type.A00, 1, "RET", "NC", "", "if CY not set, (PC)<-(SP)"));
		codeMap.put("D1", new OperationStructure("D1", Z80Type.A00, 1, "POP", "DE", "", ""));
		codeMap.put("D2", new OperationStructure("D2", Z80Type.A89, 3, "JP", "NC", "D16", "if CY not set,PC<-D16"));
		codeMap.put("D3", new OperationStructure("D3", Z80Type.A01, 2, "OUT", "D8", "A", "OUT  (D8),A   I/O")); // Special
		codeMap.put("D4", new OperationStructure("D4", Z80Type.A89, 3, "CALL", "NC", "D16", "if CY not set,CALL D16"));
		codeMap.put("D5", new OperationStructure("D5", Z80Type.A00, 1, "PUSH", "DE", "", ""));
		codeMap.put("D6", new OperationStructure("D6", Z80Type.A97, 2, "SUB", "D8", "", "A<-A - byte2"));
		codeMap.put("D7", new OperationStructure("D7", Z80Type.A00, 1, "RST", "10", "", "CALL $10"));
		codeMap.put("D8", new OperationStructure("D8", Z80Type.A00, 1, "RET", "C", "", "if CY  set,PC<-(SP)"));
		codeMap.put("D9", new OperationStructure("D9", Z80Type.A99, 1, "EXX", "", "", "Main regs <-> Alt regs"));
		codeMap.put("DA", new OperationStructure("DA", Z80Type.A89, 3, "JP", "C", "D16", "if CY  set,PC<-D16"));
		codeMap.put("DB", new OperationStructure("DB", Z80Type.A98, 2, "IN", "A", "D8", "i/O")); // Special
		codeMap.put("DC",
				new OperationStructure("DC", Z80Type.A89, 3, "CALL", "C", "D16", "if CY set ,(SP)<-PC,PC<- address"));
		/*
		 * DD codeMap.put("DD", new OperationStructure("DD", Z80Type.MAIN, 3, "Alt", "D16", "", "Alt CALL D16"));
		 */
		codeMap.put("DE", new OperationStructure("DE", Z80Type.A98, 2, "SBC", "A", "D8", "A<- A - data - cy"));
		codeMap.put("DF", new OperationStructure("DF", Z80Type.A00, 1, "RST", "18H", "", "CALL $18"));
		//
		codeMap.put("E0", new OperationStructure("E0", Z80Type.A00, 1, "RET", "PO", "", "if P is reset, set,PC<-(SP)"));
		codeMap.put("E1", new OperationStructure("E1", Z80Type.A00, 1, "POP", "HL", "", ""));
		codeMap.put("E2", new OperationStructure("E2", Z80Type.A89, 3, "JP", "PO", "D16", "if P is reset,PC<-D16"));
		codeMap.put("E3", new OperationStructure("E3", Z80Type.A92, 1, "EX", "(SP)", "HL", "L<->(SP);H<->(SP+1)"));
		codeMap.put("E4", new OperationStructure("E4", Z80Type.A89, 3, "CALL", "PO", "D16",
				"if P is reset,(SP)<-PC,PC<- address"));
		codeMap.put("E5", new OperationStructure("E5", Z80Type.A00, 1, "PUSH", "HL", "", ""));
		codeMap.put("E6", new OperationStructure("E6", Z80Type.A97, 2, "AND", "D8", "", "A<-A & byte2"));
		codeMap.put("E7", new OperationStructure("E7", Z80Type.A00, 1, "RST", "20H", "", "CALL $20"));
		codeMap.put("E8", new OperationStructure("E8", Z80Type.A00, 1, "RET", "PE", "", "if P is set, set,PC<-(SP)"));
		codeMap.put("E9", new OperationStructure("E9", Z80Type.A00, 1, "JP", "(HL)", "", "PC.hi<-H;PC.lo<-L"));
		codeMap.put("EA", new OperationStructure("EA", Z80Type.A89, 3, "JP", "PE", "D16", "if P is set,PC<-D16"));
		codeMap.put("EB", new OperationStructure("EB", Z80Type.A92, 1, "EX", "DE", "HL", "H<->D;L<->E")); // Special
		codeMap.put("EC",
				new OperationStructure("EC", Z80Type.A89, 3, "CALL", "PE", "D16", "if P is set,(SP)<-PC,PC<- address"));
		// /* ED */ Extended instructions makeExtendedTypes
		codeMap.put("EE", new OperationStructure("EE", Z80Type.A97, 2, "XOR", "D8", "", "A<- A ^ data"));
		codeMap.put("EF", new OperationStructure("EF", Z80Type.A00, 1, "RST", "28H", "", "CALL $28"));
		//
		codeMap.put("F0", new OperationStructure("F0", Z80Type.A00, 1, "RET", "P", "", "if S rest, set,PC<-(SP)"));
		codeMap.put("F1",
				new OperationStructure("F1", Z80Type.A00, 1, "POP", "AF", "", "flags<-(SP);A<-(SP+1); SP<-SP+2"));
		codeMap.put("F2", new OperationStructure("F2", Z80Type.A89, 3, "JP", "P", "D16", "if S reset,PC<-D16"));
		codeMap.put("F3", new OperationStructure("F3", Z80Type.A99, 1, "DI", "", "", "Disable interrups"));
		codeMap.put("F4",
				new OperationStructure("F4", Z80Type.A89, 3, "CALL", "P", "D16", "if S reset,(SP)<-PC,PC<- address"));
		codeMap.put("F5", new OperationStructure("F5", Z80Type.A00, 1, "PUSH", "AF", "", "(SP)<-A & cc"));
		codeMap.put("F6", new OperationStructure("F6", Z80Type.A97, 2, "OR", "D8", "", "A<-A | byte2"));
		codeMap.put("F7", new OperationStructure("F7", Z80Type.A00, 1, "RST", "30H", "", "CALL $30"));
		codeMap.put("F8", new OperationStructure("F8", Z80Type.A00, 1, "RET", "M", "", "if S set, set,PC<-(SP)"));
		codeMap.put("F9", new OperationStructure("F9", Z80Type.A92, 1, "LD", "SP", "HL", "SP<-HL"));
		codeMap.put("FA", new OperationStructure("FA", Z80Type.A89, 3, "JP", "M", "D16", "if S set,PC<-D16"));
		codeMap.put("FB", new OperationStructure("FB", Z80Type.A99, 1, "EI", "", "", "Enable interrupts"));
		codeMap.put("FC",
				new OperationStructure("FC", Z80Type.A89, 3, "CALL", "M", "D16", "if S set,(SP)<-PC,PC<- address"));
		/*
		 * FDcodeMap.put("FD", new OperationStructure("FD", Z80Type.MAIN, 3, "Alt", "D16", "", "Alt CALL D16"));
		 */codeMap.put("FE", new OperationStructure("FE", Z80Type.A97, 2, "CP", "D8", "", "A - data"));
		codeMap.put("FF", new OperationStructure("FF", Z80Type.A00, 1, "RST", "38H", "", "CALL $38"));

	}// makeMainTypes
}// class OpCodeMap
