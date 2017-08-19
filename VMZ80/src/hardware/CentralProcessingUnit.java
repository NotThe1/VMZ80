package hardware;

import codeSupport.AppLogger;
import codeSupport.Z80.Register;
//import ioSystem.IOController;
import memory.CpuBuss;

/**
 * This class is responsible for the execution of the instruction for the system. It is a singleton Construction
 * 
 * @author Frank Martyn
 * @version 1.0
 *
 */

public class CentralProcessingUnit implements Runnable {
	private static CentralProcessingUnit instance = new CentralProcessingUnit();
	private CpuBuss cpuBuss;
	private ConditionCodeRegister ccr;
	private WorkingRegisterSet wrs;
	private Adder adder = Adder.getInstance();
	// private ArithmeticUnit au;
	private ErrorStatus error;
	// private IOController ioController;

	private AppLogger log = AppLogger.getInstance();

	public static CentralProcessingUnit getInstance() {
		return instance;
	}// getInstance

	private CentralProcessingUnit() {
		this.cpuBuss = CpuBuss.getInstance();
		this.wrs = WorkingRegisterSet.getInstance();
		// this.au = ArithmeticUnit.getInstance();
		this.ccr = ConditionCodeRegister.getInstance();
		// this.ioController = IOController.getInstance();
		// this.error = ErrorStatus.NONE;
	}// Constructor

	/**
	 * Executes instruction found at the location contained in the Program Counter
	 * 
	 * @return true if successful, false if error (accessed vis getError)
	 */

	public void run() {
		while (!isError()) {
			startInstruction();
		} // while
	}// run

	public boolean startInstruction() {
		long origin = System.nanoTime();
		executeInstruction(wrs.getProgramCounter());
		long elapsedTIme = System.nanoTime() - origin;
		return !isError();
	}// startInstruction

	public void executeInstruction(int currentAddress) {
		Register indexRegister = null;
		Instruction instruction = new Instruction();
		byte opCode = instruction.opCode0;
		int instructionLength = 0;
		switch (opCode) {
		case (byte) 0X0ED: // Extended Instructions
			instructionLength = opCodeSetED(instruction);
			break;
		case (byte) 0X0CB: // Bit Instructions
			instructionLength = opCodeSetCB(instruction);
			break;
		case (byte) 0X0DD: // IX Instructions
		case (byte) 0X0FD: // IY Instructions
			// indexRegister = opCode == (byte) 0XDD ? Z80.Register.IX : Z80.Register.IY;

			if (instruction.opCode1 == (byte) 0XCB) { // IX/IY bit instructions
				instructionLength = opCodeSetIndexRegistersBit(instruction);
			} else {// IX/IY instructions
				instructionLength = opCodeSetIndexRegisters(instruction);
			} // if bit instructions
			break;

		default:
			instructionLength = opCodeSetSingle(instruction);

		}// switch opCode
		wrs.incrementProgramCounter(instructionLength);
		return;
	}// executeInstruction

	// Extended Instructions
	private int opCodeSetED(Instruction instruction) {
		int instructionSize = 0;
		Register source;
		switch (instruction.page) {
		case 0: // Page 00
			// There are no instructions on page 0
			break;
		case 1: // Page 01
			switch (instruction.zzz) {
			case 0:// IN r(C)
				source = instruction.singleRegister1;
				instructionSize = 2;
				// DO OPCODE IN r(C) note Register.M special
				break;
			case 1:// OUT (C),r
				source = instruction.singleRegister1;
				instructionSize = 2;
				// DO OPCODE OUT (C),r note Register.M special
				break;
			case 2: // ADC HL,ss SBC HL,ss
				source = instruction.doubleRegister1;
				instructionSize = 2;
				if (instruction.bit3) {// ADC HL,ss
					// DO OPCODE ADC HL,ss
				} else {// SBC HL,ss
						// DO OPCODE SBC HL,ss
				} // if bit 3
				break;
			case 3:// LD (nn),dd LD dd,(nn)
				source = instruction.doubleRegister1;
				instructionSize = 4;
				if (instruction.bit3) {// ALD dd,(nn)
					// DO OPCODE LD dd,(nn)
				} else {// LD (nn),dd
						// DO OPCODE LD (nn),dd
				} // if bit 3
				break;
			case 4:// NEG
				instructionSize = 2;
				// DO OPCODE NEG
				break;
			case 5:// RETN RETI
				instructionSize = 2;
				if (instruction.opCode1 == 0X4D) {// RETI
					// DO OPCODE RETI
				} else {// RETN
						// DO OPCODE RETN
				} // if RETx
				break;
			case 6: // IM 0 IM 1 IM 2
				instructionSize = 2;
				switch (instruction.yyy & 0B00011) {
				case 0: // IM 0
					// DO OPCODE IM 0
					break;
				case 2: // IM 1
					// DO OPCODE IM 1
					break;
				case 3: // IM 2
					// DO OPCODE IM 2
					break;
				}// case IM x
				break;
			case 7: // LD I,A LD A,I RRD LD R,A LD A,R RLD
				instructionSize = 2;
				switch (instruction.opCode1) {
				case 0X47: // LD I,A
					// DO OPCODE LD I,A
					break;
				case 0X57: // LD A,I
					// DO OPCODE LD A,I
					break;
				case 0X67: // RRD
					// DO OPCODE RRD
					break;
				case 0X4F: // LD R,A
					// DO OPCODE LD R,A
					break;
				case 0X5F: // LD A,r
					// DO OPCODE LD A,r
					break;
				case 0X6F: // RLD
					// DO OPCODE RLD
					break;
				default:
				}// switch opCode1
				break;
			default:

			}// switch zzz
			break;

		case 2: // Page 10
			// LDI CPI INI OUTI LDD CPD IND OUTD
			// LDIR CPIR INIR OTIR LDDR CPDR INDR OTDR
			instructionSize = 2;

			switch (instruction.opCode1) {
			case (byte) 0XA0: // LDI
				// DO OPCODE LDI
				break;
			case (byte) 0XB0: // LDIR
				// DO OPCODE LDIR
				break;
			case (byte) 0XA1: // CPI
				// DO OPCODE CPI
				break;
			case (byte) 0XB1: // CPIR
				// DO OPCODE CPIR
				break;
			case (byte) 0XA2: // INI
				// DO OPCODE INI
				break;
			case (byte) 0XB2: // INIR
				// DO OPCODE INIR
				break;
			case (byte) 0XA3: // OUTI
				// DO OPCODE OUTI
				break;
			case (byte) 0XB3: // OTIR
				// DO OPCODE OTIR
				break;
			case (byte) 0XA8: // LDD
				// DO OPCODE LDD
				break;
			case (byte) 0XB8: // LDDR
				// DO OPCODE LDDR
				break;
			case (byte) 0XA9: // CPD
				// DO OPCODE CPD
				break;
			case (byte) 0XB9: // CPDR
				// DO OPCODE CPDR
				break;
			case (byte) 0XAA: // IND
				// DO OPCODE IND
				break;
			case (byte) 0XBA: // INDR
				// DO OPCODE INDR
				break;
			case (byte) 0XAB: // OUTD
				// DO OPCODE OUTD
				break;
			case (byte) 0XBB: // OTDR
				// DO OPCODE OTDR
				break;

			}// switch opCode1
			break;

		case 3: // Page 11
			// There are no instructions on page 3
			break;
		}// switch page
		return instructionSize;
	}// opCodePageED

	// Bit Instructions
	private int opCodeSetCB(Instruction instruction) {
		int instructionSize = 2;
		int bit = instruction.bit;
		Register target = instruction.singleRegister1;
		switch (instruction.page) {
		case 0: // Page 00 RLC RRC RL RR SLA SRA SLL SRL
			switch (instruction.yyy) {
			case 0: // RLC
				// DO OPCODE RLC
				break;
			case 1: // RRC
				// DO OPCODE RRC
				break;
			case 2: // RL
				// DO OPCODE RL
				break;
			case 3: // RR
				// DO OPCODE RR
				break;
			case 4: // SLA
				// DO OPCODE SLA
				break;
			case 5: // SRA
				// DO OPCODE SRA
				break;
			case 6: // SLL ******* not real
				// DO OPCODE SLL
				break;
			case 7: // SRL
				// DO OPCODE SRL
				break;
			}// switch yyy
			break;
		case 1: // Page 01 BIT b,r
			// DO OPCODE BIT b,r
			break;
		case 2: // Page 10 RES b,r
			// DO OPCODE RES b,r
			break;
		case 3: // Page 11 SET b,r
			// DO OPCODE SET b,r
			break;

		}// switch instruction Page

		return instructionSize;
	}// opCodePageCB

	private int opCodeSetIndexRegisters(Instruction instruction) {
		int instructionSize = 0;
		switch (instruction.opCode1) {
		case (byte) 0X09:// ADD IXY,BC
		case (byte) 0X19:// ADD IXY,DE
		case (byte) 0X29:// ADD IXY,IXY
		case (byte) 0X39:// ADD IXY,SP
			instructionSize = 2;
			// DO OPCODE ADD IXY,dd
			break;
		case (byte) 0X70: // LD (IXY=d),B
		case (byte) 0X71: // LD (IXY=d),C
		case (byte) 0X72: // LD (IXY=d),D
		case (byte) 0X73: // LD (IXY=d),E
		case (byte) 0X74: // LD (IXY=d),H
		case (byte) 0X75: // LD (IXY=d),L
		case (byte) 0X76: // LD (IXY=d),M
		case (byte) 0X77: // LD (IXY=d),A
			instructionSize = 3;
			// DO OPCODE LD (IXY=d),r
			break;
		case (byte) 0X46: // LD B,(IXY=d)
		case (byte) 0X4E: // LD C,(IXY=d)
		case (byte) 0X56: // LD D,(IXY=d)
		case (byte) 0X5E: // LD E,(IXY=d)
		case (byte) 0X66: // LD H,(IXY=d)
		case (byte) 0X6E: // LD L,(IXY=d)
			instructionSize = 3;
			// DO OPCODE LD r,(IXY=d)
			break;
		case (byte) 0X21: // LD IXY,dd
			instructionSize = 4;
			// DO OPCODE LD IXY,dd
			break;
		case (byte) 0X22: // LD (dd),IXY
			instructionSize = 4;
			// DO OPCODE LD IXY,dd
			break;
		case (byte) 0X23: // INC IXY
			instructionSize = 2;
			// DO OPCODE INC IXY
			break;
		case (byte) 0X2A: // LD IXY,(dd)
			instructionSize = 4;
			// DO OPCODE LD IXY,(dd)
			break;
		case (byte) 0X2B: // DEC IXY
			instructionSize = 2;
			// DO OPCODE DEC IXY
			break;
		case (byte) 0X34: // INC (IXY+d)
			instructionSize = 3;
			// DO OPCODE INC (IXY+d)
			break;
		case (byte) 0X35: // DEC (IXY+d)
			instructionSize = 3;
			// DO OPCODE DEC (IXY+d)
			break;
		case (byte) 0X36: // LD (IXY+d),n
			instructionSize = 4;
			// DO OPCODE DEC (IXY+d)
			break;
		case (byte) 0X7E: // LD	A,IXY
			instructionSize = 3;
			// DO OPCODE LD	A,IXY
			break;
		case (byte) 0X86: // ADD A,(IXY+d)
			instructionSize = 3;
			// DO OPCODE ADD A,(IXY+d)
			break;
		case (byte) 0X8E: // ADC A,(IXY+d)
			instructionSize = 3;
			// DO OPCODE ADC A,(IXY+d)
			break;
		case (byte) 0X96: // SUB (IXY+d)
			instructionSize = 3;
			// DO OPCODE SUB (IXY+d)
			break;
		case (byte) 0X9E: // SBC A,(IXY+d)
			instructionSize = 3;
			// DO OPCODE SBC A,(IXY+d)
			break;
		case (byte) 0XA6: // AND(IXY+d)
			instructionSize = 3;
			// DO OPCODE AND(IXY+d)
			break;
		case (byte) 0XAE: // XOR(IXY+d)
			instructionSize = 3;
			// DO OPCODE XOR(IXY+d)
			break;
		case (byte) 0XB6: // OR(IXY+d)
			instructionSize = 3;
			// DO OPCODE OR(IXY+d)
			break;
		case (byte) 0XBE: // CP(IXY+d)
			instructionSize = 3;
			// DO OPCODE CP(IXY+d)
			break;
		case (byte) 0XE1: // POP IXY
			instructionSize = 2;
			// DO OPCODE POP IXY
			break;
		case (byte) 0XE3: // EX (SP)IXY
			instructionSize = 2;
			// DO OPCODE EX (SP)IXY
			break;
		case (byte) 0XE5: // PUSH IXY
			instructionSize = 2;
			// DO OPCODE PUSH IXY
			break;
		case (byte) 0XE9: // JP (IXY)
			instructionSize = 2;
			// DO OPCODE JP (IXY)
			break;
		case (byte) 0XF9: // LD SP,IXY
			instructionSize = 2;
			// DO OPCODE JP (IXY)
			break;
		}// switch opCode1

		return instructionSize;
	}// opCodePageDD

	private int opCodeSetIndexRegistersBit(Instruction instruction) {
		int instructionSize = 4;
		int subInstructionPage = cpuBuss.read(wrs.getProgramCounter()+3)>>6 & 0B011;
		switch (subInstructionPage) {
		case 0: // Page 00 RLC RRC RL RR SLA SRA SLL SRL  ---  (IXY+d)
			switch (instruction.yyy) {
			case 0: // RLC (IXY+d)
				// DO OPCODE RLC (IXY+d)
				break;
			case 1: // RRC (IXY+d)
				// DO OPCODE RRC (IXY+d)
				break;
			case 2: // RL (IXY+d)
				// DO OPCODE RL (IXY+d)
				break;
			case 3: // RR (IXY+d)
				// DO OPCODE RR (IXY+d)
				break;
			case 4: // SLA (IXY+d)
				// DO OPCODE SLA (IXY+d)
				break;
			case 5: // SRA (IXY+d)
				// DO OPCODE SRA (IXY+d)
				break;
			case 6: // SLL  (IXY+d)******* not real
				// DO OPCODE SLL (IXY+d)
				break;
			case 7: // SRL (IXY+d)
				// DO OPCODE SRL (IXY+d)
				break;
			}// switch yyy (IXY+d)
			break;
		case 1: // Page 01 BIT b,(IXY+d)
			// DO OPCODE BIT b,(IXY+d)
			break;
		case 2: // Page 10 RES b,(IXY+d)
			// DO OPCODE RES b,(IXY+d)
			break;
		case 3: // Page 11 SET b,(IXY+d)
			// DO OPCODE SET b,(IXY+d)
			break;

		}// switch instruction Page
		return instructionSize;
	}// opCodePageDD

	private int opCodeSetSingle(Instruction instruction) {
		int instructionSize = 0;

		// int page = (opCode >> 6) & 0X0003; // only want the value of bits 6 & 7
		// int yyy = (opCode >> 3) & 0X0007; // only want the value of bits 3,4 & 5
		// int zzz = opCode & 0X0007; // only want the value of bits 0,1 & 2

		// switch (page) {
		// case 0:
		// // instructionLength = opCodePage00(currentAddress, opCode, yyy, zzz);
		// break;
		// case 1:
		// // instructionLength = opCodePage01(opCode, yyy, zzz);
		// break;
		// case 2:
		// // instructionLength = opCodePage10(opCode, yyy);
		// break;
		// case 3:
		// // instructionLength = opCodePage11(currentAddress, opCode, yyy, zzz);
		// break;
		// default:
		// // setError(ErrorStatus.INVALID_OPCODE);
		//
		// }// Switch page
		return instructionSize;
	}// opCodePageSingle
		// --------------------------------------------------------------------------------------------------------

	// private int opCodePage00(int currentAddress, byte opCode, int yyy, int zzz) {
	// byte hiByte, loByte;
	// int intValue;
	// byte byteValue;
	// int directAddress = cpuBuss.readWordReversed(currentAddress + 1);
	// Register register16Bit = RegisterDecode.getRegisterPairStd(opCode);
	// Register register8bit = RegisterDecode.getHighRegister(opCode);
	// int codeLength;
	// // 00 YYY ZZZ
	// switch (zzz) {
	// case 0: // zzz = 000 (NOP)
	// // NOP
	// if (yyy == 0) {// its a NOP OO length =1, cycles = 4
	// // 00 real NOP
	// codeLength = 1;
	// } else if (yyy == 6) {// Opcode "30" special for debugging **** act like a halt
	// codeLength = 0;
	// setError(ErrorStatus.STOP);
	// } else {// treat as if it is a NOP
	// // 08,10,18,20,28,30,38 - not implemented. treat as NOP
	// codeLength = 1;
	// }//
	// break;
	// case 1: // zzz = 001 (LXI & DAD)- Register Pair (BC,DE,HL,SP)
	// if ((yyy & 0X01) == 0) { // LXI
	// // word = cpuBuss.readWordReversed(currentAddress + 1);
	// wrs.setDoubleReg(register16Bit, directAddress);
	// codeLength = 3;
	// } else {// DAD
	// int hlValue = wrs.getDoubleReg(Register.HL);
	// int regValue = wrs.getDoubleReg(register16Bit);
	// wrs.setDoubleReg(Register.HL, au.add(hlValue, regValue));
	// codeLength = 1;
	// }// if
	// break;
	// case 2: // zzz = 010 (STAX LDAX)
	// int location = wrs.getDoubleReg(register16Bit);
	// switch (yyy) {// zzz = 100
	// case 0: // STAX BC
	// case 2: // STAX DE
	// cpuBuss.write(location, wrs.getAcc());
	// codeLength = 1;
	// break;
	// case 1: // LDAX BC
	// case 3: // LDAX DE
	// wrs.setAcc(cpuBuss.read(location));
	// codeLength = 1;
	// break;
	// case 4: // SHLD
	// hiByte = wrs.getReg(Register.L);
	// loByte = wrs.getReg(Register.H);
	// cpuBuss.writeWord(directAddress, hiByte, loByte);
	// codeLength = 3;
	// break;
	// case 5: // LHLD
	// wrs.setReg(Register.L, cpuBuss.read(directAddress));
	// wrs.setReg(Register.H, cpuBuss.read(directAddress + 1));
	// codeLength = 3;
	// break;
	// case 6: // STA
	// cpuBuss.write(directAddress, wrs.getAcc());
	// codeLength = 3;
	// break;
	// case 7: // LDA
	// wrs.setAcc(cpuBuss.read(directAddress));
	// codeLength = 3;
	// break;
	// default:
	// codeLength = 0;
	// setError(ErrorStatus.INVALID_OPCODE);
	// // return codeLength;
	// }// switch (yyy)
	// break;
	// case 3: // zzz = 011 (INX & DCX) - Register Pair (BC,DE,HL,SP)
	// codeLength = 1;
	// intValue = wrs.getDoubleReg(register16Bit);
	// if ((yyy & 0X01) == 0) {// INX
	// wrs.setDoubleReg(register16Bit, au.increment(intValue));
	// } else { // DCX
	// wrs.setDoubleReg(register16Bit, au.decrement(intValue));
	// }// if
	//
	// break;
	// case 4: // zzz = 100 (INC)- Register (B,C,D,E,H,L,A)
	// if (register8bit.equals(Register.M)) { // Memory reference through (HL)
	// int indirectAddress = wrs.getDoubleReg(Register.M);
	// byteValue = au.increment(cpuBuss.read(indirectAddress));
	// cpuBuss.write(indirectAddress, byteValue);
	// } else { // Standard 8-bit register
	// byteValue = wrs.getReg(register8bit);
	// wrs.setReg(register8bit, au.increment(byteValue));
	// }// if
	// codeLength = 1;
	// break;
	// case 5: // zzz = 101 (DCR)- Register (B,C,D,E,H,L,A)
	// if (register8bit.equals(Register.M)) { // Memory reference through (HL)
	// int indirectAddress = wrs.getDoubleReg(Register.M);
	// byteValue = au.decrement(cpuBuss.read(indirectAddress));
	// cpuBuss.write(indirectAddress, byteValue);
	// } else { // Standard 8-bit register
	// byteValue = wrs.getReg(register8bit);
	// wrs.setReg(register8bit, au.decrement(byteValue));
	// }// if
	// codeLength = 1;
	// break;
	// case 6: // zzz = 110 (MVI)- Register (B,C,D,E,H,L,A)
	// byteValue = cpuBuss.read(currentAddress + 1);
	// if (register8bit.equals(Register.M)) { // Memory reference through (HL)
	// int indirectAddress = wrs.getDoubleReg(Register.M);
	// cpuBuss.write(indirectAddress, byteValue);
	// } else { // Standard 8-bit register
	// wrs.setReg(register8bit, byteValue);
	// }// if
	// codeLength = 2;
	// break;
	// case 7: // zzz = 111 (RLC,RRC,RAL,RAR,DAA,CMA,STC,CMC
	// byteValue = wrs.getAcc();
	// switch (yyy) {
	// case 0: // zzz = 000 (RLC)
	// wrs.setAcc(au.rotateLeft(byteValue));
	// codeLength = 1;
	// break;
	// case 1: // zzz = 001 (RRC)
	// wrs.setAcc(au.rotateRight(byteValue));
	// codeLength = 1;
	// break;
	// case 2: // zzz = 010 (RAL)
	// wrs.setAcc(au.rotateLeftThruCarry(byteValue));
	// codeLength = 1;
	// break;
	// case 3: // zzz = 011 (RAR)
	// wrs.setAcc(au.rotateRightThruCarry(byteValue));
	// codeLength = 1;
	// break;
	// case 4: // zzz = 100 (DAA)
	// wrs.setAcc(au.decimalAdjustByte(byteValue));
	// codeLength = 1;
	// break;
	// case 5: // zzz = 101 (CMA)
	// wrs.setAcc(au.complement(byteValue));
	// codeLength = 1;
	// break;
	// case 6: // zzz = 110 (STC)
	// ccr.setCarryFlag(true);
	// codeLength = 1;
	// break;
	// case 7: // zzz = 111 (CMC)
	// boolean state = ccr.isCarryFlagSet();
	// ccr.setCarryFlag(!state);
	// codeLength = 1;
	// break;
	// default:
	// setError(ErrorStatus.INVALID_OPCODE);
	// codeLength = 0;
	// }// switch yyy
	// break;
	// default:
	// setError(ErrorStatus.INVALID_OPCODE);
	// return 0;
	// }// switch zzz
	// return codeLength;
	//
	// }// opCodePage00
	// // ----------------------------------------------------
	//
	// // private int opCodePage01(int currentAddress, byte opCode, int yyy, int zzz){
	//
	// private int opCodePage01(byte opCode, int yyy, int zzz) {
	// if (opCode == (byte) 0X76) { // (HLT)
	// setError(ErrorStatus.HLT_INSTRUCTION);
	// } else { // (MOV)
	// Register destination = RegisterDecode.getHighRegister(opCode);
	// Register source = RegisterDecode.getLowRegister(opCode);
	// byte value;
	//
	// if (source.equals(Register.M)) { // Memory reference through (HL)
	// int indirectAddress = wrs.getDoubleReg(Register.M);
	// value = cpuBuss.read(indirectAddress);
	// } else { // Standard 8-bit register
	// value = wrs.getReg(source);
	// }// if
	//
	// if (destination.equals(Register.M)) { // Memory reference through (HL)
	// int indirectAddress = wrs.getDoubleReg(Register.M);
	// cpuBuss.write(indirectAddress, value);
	// } else { // Standard 8-bit register
	// wrs.setReg(destination, value);
	// }// if
	// }// else
	// int codeLength = 1;
	// return codeLength;
	// }// opCodePage01
	//
	// private int opCodePage10(byte opCode, int yyy) {
	// byte sourceValue, accValue;
	// int indirectAddress;
	// Register register8bit = RegisterDecode.getLowRegister(opCode);
	// if (register8bit.equals(Register.M)) {
	// indirectAddress = wrs.getDoubleReg(Register.M);
	// sourceValue = cpuBuss.read(indirectAddress);
	// } else {
	// sourceValue = wrs.getReg(register8bit);
	// }// if register M for source
	//
	// accValue = wrs.getAcc();
	// int codeLength = 1;
	// switch (yyy) {
	// case 0: // yyy = 000 (ADD)
	// wrs.setAcc(au.add(accValue, sourceValue));
	// break;
	// case 1: // yyy = 001 (ADC)
	// wrs.setAcc(au.addWithCarry(accValue, sourceValue));
	// break;
	// case 2: // yyy = 010 (SUB)
	// wrs.setAcc(au.subtract(accValue, sourceValue));
	// break;
	// case 3: // yyy = 011 (SBB)
	// wrs.setAcc(au.subtractWithBorrow(accValue, sourceValue));
	// break;
	// case 4: // yyy = 100 (ANA)
	// wrs.setAcc(au.logicalAnd(accValue, sourceValue));
	// break;
	// case 5: // yyy = 101 (XRA)
	// wrs.setAcc(au.logicalXor(accValue, sourceValue));
	// break;
	// case 6: // yyy = 110 (ORA)
	// wrs.setAcc(au.logicalOr(accValue, sourceValue));
	// break;
	// case 7: // yyy = 111 (CMP)
	// au.subtract(accValue, sourceValue); // leave both values untouched
	// break;
	// default:
	// setError(ErrorStatus.INVALID_OPCODE);
	// codeLength = 0;
	// }// switch yyy
	//
	// return codeLength;
	// }// opCodePage10
	//
	// private int opCodePage11(int currentAddress, byte opCode, int yyy, int zzz) {
	// int codeLength = -1;
	// byte accValue;
	// Register register16bit = RegisterDecode.getRegisterPairAlt(opCode);
	// ConditionFlag condition = RegisterDecode.getCondition(opCode);
	// accValue = wrs.getAcc();
	//
	// switch (zzz) {
	// case 0: // zzz 000 Conditional return CCC
	// if (opCodeConditionTrue(condition)) { // do the return
	// opCode_Return();
	// codeLength = 0;
	// } else { // just skip past
	// codeLength = 1;
	// }// if
	// break;
	// case 1: // zzz 001 POP/RET/PCHL/SPHL
	// if ((yyy & 0X01) == 0) { // POP
	// int stackLocation = wrs.getStackPointer();
	// int valueInt = cpuBuss.popWord(stackLocation);
	// if (register16bit.equals(Register.AF)) { // PSW
	// wrs.setReg(Register.A, (byte) ((valueInt >> 8) & 0X00FF));
	// ccr.setConditionCode((byte) (valueInt & 0X00FF));
	// } else {
	// wrs.setDoubleReg(register16bit, valueInt);
	// }// if
	// wrs.setStackPointer(stackLocation + 2);
	// codeLength = 1;
	// } else { // RET/PCHL/SPHL
	// if (opCode == (byte) 0XC9) {// RET
	// opCode_Return();
	// codeLength = 0;
	// } else if (opCode == (byte) 0XE9) { // PCHL
	// int hlValue = wrs.getDoubleReg(Register.HL);
	// wrs.setProgramCounter(hlValue);
	// codeLength = 0;
	// } else if (opCode == (byte) 0XF9) { // SPHL
	// int hlValue = wrs.getDoubleReg(Register.HL);
	// wrs.setStackPointer(hlValue);
	// codeLength = 1;
	// } else { // Not used
	// setError(ErrorStatus.INVALID_OPCODE);
	// codeLength = 0;
	// }// inner if
	// }// if lsb = 0/1
	// break;
	// case 2: // zzz 010 Conditional Jump CCC
	// if (opCodeConditionTrue(condition)) { // do the return
	// opCode_Jump();
	// codeLength = 0;
	// } else { // just skip past
	// codeLength = 3;
	// }// if
	// break;
	// case 3: // zzz 011 JMP/OUT/IN/XTHL/XCHL/DI/EI
	// switch (yyy) {
	// case 0: // yyy 000 JMP
	// opCode_Jump();
	// codeLength = 0;
	// break;
	// case 1: // yyy 001 Not Used
	// setError(ErrorStatus.INVALID_OPCODE);
	// codeLength = 0;
	// break;
	// case 2: // yyy 010 OUT
	// Byte IOaddress = cpuBuss.read(wrs.getProgramCounter() + 1);
	// ioController.byteToDevice(IOaddress, wrs.getReg(Register.A));
	// codeLength = 2;
	// break;
	// case 3: // yyy 011 IN
	// IOaddress = cpuBuss.read(wrs.getProgramCounter() + 1);
	// wrs.setReg(Register.A, ioController.byteFromDevice(IOaddress));
	// codeLength = 2;
	// break;
	// case 4: // yyy 100 XTHL
	// byte valueL = wrs.getReg(Register.L);
	// byte valueH = wrs.getReg(Register.H);
	//
	//// int hlValue = wrs.getDoubleReg(Register.HL);
	// int stackLocation = wrs.getStackPointer();
	// wrs.setReg(Register.L, cpuBuss.read(stackLocation));
	// wrs.setReg(Register.H, cpuBuss.read(stackLocation+1));
	//// int stackValue = cpuBuss.popWord(stackLocation);
	//// wrs.setDoubleReg(Register.HL, stackValue);
	//// cpuBuss.pushWord(stackLocation, hlValue);
	// cpuBuss.write(stackLocation, valueL);
	// cpuBuss.write(stackLocation + 1, valueH);
	//
	// codeLength = 1;
	// break;
	// case 5: // yyy 101 XCHG
	// int deValue = wrs.getDoubleReg(Register.DE);
	// wrs.setDoubleReg(Register.DE, wrs.getDoubleReg(Register.HL));
	// wrs.setDoubleReg(Register.HL, deValue);
	// codeLength = 1;
	// break;
	// case 6: // yyy 110 DI
	// System.out.println("Not yet implemented");
	// codeLength = 1;
	// break;
	// case 7: // yyy 111 EI
	// System.out.println("Not yet implemented");
	// codeLength = 1;
	// break;
	// default:
	// setError(ErrorStatus.INVALID_OPCODE);
	// codeLength = 0;
	// }// switch(opCod
	// break;
	// case 4: // zzz 100 Conditional Call CCC
	// if (opCodeConditionTrue(condition)) { // do the return
	// opCode_Call();
	// codeLength = 0;
	// } else { // just skip past
	// codeLength = 3;
	// }// if
	// break;
	// case 5: // zzz 101 PUSH/CALL
	// if ((yyy & 0X01) == 0) { // PUSH
	// if (register16bit.equals(Register.AF)) {
	// opCode_Push(wrs.getReg(Register.A), ccr.getConditionCode());
	// } else {
	// opCode_Push(wrs.getDoubleReg(register16bit));
	// }// if
	// codeLength = 1;
	// } else if (opCode == (byte) 0XCD) { // CALL
	// opCode_Call();
	// codeLength = 0;
	// } else {
	// setError(ErrorStatus.INVALID_OPCODE);
	// codeLength = 0;
	// }// if odd/even
	// break;
	// case 6: // zzz 110 ADI/ACI/SUI/SBI/ANI/XRI/ORI/CPI
	// byte resultValue;
	// byte immediateValue = cpuBuss.read(wrs.getProgramCounter() + 1);
	// // accValue = wrs.getAcc();
	// codeLength = 2;
	// switch (yyy) {
	// case 0: // yyy 000 ADI
	// resultValue = au.add(accValue, immediateValue);
	// wrs.setAcc(resultValue);
	// break;
	// case 1: // yyy 001 ACI
	// resultValue = au.addWithCarry(accValue, immediateValue);
	// wrs.setAcc(resultValue);
	// break;
	// case 2: // yyy 010 SUI
	// resultValue = au.subtract(accValue, immediateValue);
	// wrs.setAcc(resultValue);
	// break;
	// case 3: // yyy 011 SBI
	// resultValue = au.subtractWithBorrow(accValue, immediateValue);
	// wrs.setAcc(resultValue);
	// break;
	// case 4: // yyy 100 ANI
	// resultValue = au.logicalAnd(accValue, immediateValue);
	// wrs.setAcc(resultValue);
	// break;
	// case 5: // yyy 101 XRI
	// resultValue = au.logicalXor(accValue, immediateValue);
	// wrs.setAcc(resultValue);
	// break;
	// case 6: // yyy 110 ORI
	// resultValue = au.logicalOr(accValue, immediateValue);
	// wrs.setAcc(resultValue);
	// break;
	// case 7: // yyy 111 CPI
	// resultValue = au.subtract(accValue, immediateValue);
	// break;
	// default:
	// setError(ErrorStatus.INVALID_OPCODE);
	// codeLength = 0;
	// }// switch(yyy)
	// break;
	// case 7: // zzz 111 RST PPP
	// opCode_Push(wrs.getProgramCounter());
	// int address = yyy * 8;
	// wrs.setProgramCounter(address);
	// codeLength = 0;
	// break;
	// default: // zzz 000
	// setError(ErrorStatus.INVALID_OPCODE);
	// return 0;
	// }// switch(zzz)
	//
	// return codeLength;
	//
	// }// opCodePage11
	//
	private void opCode_Push(byte hiByte, byte loByte) {
		int stackLocation = wrs.getStackPointer();
		cpuBuss.pushWord(stackLocation, hiByte, loByte); // push the return address
		wrs.setStackPointer(stackLocation - 2);
	}// opCode_Push

	private void opCode_Push(int value) {
		byte hiByte = (byte) (value >> 8);
		byte loByte = (byte) (value & 0X00FF);
		opCode_Push(hiByte, loByte);
		// int stackLocation = wrs.getStackPointer();
		// cpuBuss.pushWord(stackLocation, hiByte, loByte); // push the return address
		// wrs.setStackPointer(stackLocation - 2);
	}// opCode_Push

	private void opCode_Jump() {
		int currentProgramCounter = wrs.getProgramCounter();
		int memoryLocation = cpuBuss.readWordReversed(currentProgramCounter + 1);
		wrs.setProgramCounter(memoryLocation);
	}// opCode_Jump

	private void opCode_Call() {
		int currentProgramCounter = wrs.getProgramCounter();
		opCode_Push(currentProgramCounter + 3);
		int memoryLocation = cpuBuss.readWordReversed(currentProgramCounter + 1);
		wrs.setProgramCounter(memoryLocation);
		// opCodeSize = 0;

	}// opCode_Call()

	private void opCode_Return() {
		int stackPointer = wrs.getStackPointer();
		wrs.setProgramCounter(cpuBuss.popWord(stackPointer));
		wrs.setStackPointer(stackPointer + 2);
	}// opCode_Return

	/**
	 * checks to see if the given condition flag is set
	 * 
	 * @param condition
	 *            to be tested
	 * @return true if condition is met
	 */
	private boolean opCodeConditionTrue(ConditionFlag condition) {
		boolean ans = false;
		switch (condition) {
		case NZ:
			ans = ccr.isZeroFlagSet() ? false : true;
			break;
		case Z:
			ans = ccr.isZeroFlagSet() ? true : false;
			break;
		case NC:
			ans = ccr.isCarryFlagSet() ? false : true;
			break;
		case C:
			ans = ccr.isCarryFlagSet() ? true : false;
			break;
		case PO:
			ans = ccr.isPvFlagSet() ? false : true;
			break;
		case PE:
			ans = ccr.isPvFlagSet() ? true : false;
			break;
		case P:
			ans = ccr.isSignFlagSet() ? false : true;
			break;
		case M:
			ans = ccr.isSignFlagSet() ? true : false;
			break;
		default:

		}// switch
		return ans;
	}// conditionTrue

	// --------------------------------------------------------------------------------------------------------

	/**
	 * Retrieves an error of ErrorStatus
	 * 
	 * @return error type of error
	 */
	public ErrorStatus getError() {
		return this.error;
	}// setErrorFlag

	/**
	 * records an error of ErrorStatus
	 * 
	 * @param error
	 *            type of error to record
	 */
	public void setError(ErrorStatus error) {
		this.error = error;
	}// setErrorFlag

	/**
	 * indicated if an error is recorded
	 * 
	 * @return false if no error, else true
	 */
	public boolean isError() {
		return error.equals(ErrorStatus.NONE) ? false : true;
	}// isError

}// class CentralProcessingUnit
