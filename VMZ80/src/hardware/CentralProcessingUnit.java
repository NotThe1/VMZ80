package hardware;

import codeSupport.AppLogger;
import codeSupport.Z80;
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
				boolean bit3 = ((instruction.opCode1 & Z80.BIT_3) == Z80.BIT_3);
				if (bit3) {// ADC HL,ss
					// DO OPCODE ADC HL,ss
				} else {// SBC HL,ss
						// DO OPCODE SBC HL,ss
				} // if bit 3
				break;
			case 3:// LD (nn),dd LD dd,(nn)
				source = instruction.doubleRegister1;
				instructionSize = 4;
				bit3 = ((instruction.opCode1 & Z80.BIT_3) == Z80.BIT_3);
				if (bit3) {// ALD dd,(nn)
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
		case (byte) 0X7E: // LD A,IXY
			instructionSize = 3;
			// DO OPCODE LD A,IXY
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
		int subInstructionPage = cpuBuss.read(wrs.getProgramCounter() + 3) >> 6 & 0B011;
		switch (subInstructionPage) {
		case 0: // Page 00 RLC RRC RL RR SLA SRA SLL SRL --- (IXY+d)
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
			case 6: // SLL (IXY+d)******* not real
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

		switch (instruction.page) {
		case 0:
			instructionSize = opCodePage00(instruction);
			break;
		case 1:
			// // instructionLength = opCodePage01(opCode, yyy, zzz);
			break;
		case 2:
			// // instructionLength = opCodePage10(opCode, yyy);
			break;
		case 3:
			// // instructionLength = opCodePage11(currentAddress, opCode, yyy, zzz);
			break;
		default:
			// // setError(ErrorStatus.INVALID_OPCODE);

		}// Switch page
		return instructionSize;
	}// opCodePageSingle
		// --------------------------------------------------------------------------------------------------------

	private int opCodePage00(Instruction instruction) {
		int instructionSize = 0;
		boolean bit3;
		int currentAddress = wrs.getProgramCounter();
		int directAddress = cpuBuss.readWordReversed(currentAddress + 1);
		// 00 YYY ZZZ
		switch (instruction.zzz) {

		case 0: // NOP
			instructionSize = 1;
			// DO OPCODE NOP
			break;
		case 1: // LD rr,dd ADD HL,rr
			instructionSize = 3;
			bit3 = ((instruction.opCode1 & Z80.BIT_3) == Z80.BIT_3);
			if (bit3) {// ADD HL,rr
				instructionSize = 1;
				// DO OPCODE ADD HL,rr
			} else { // LD rr
				instructionSize = 3;
				// DO OPCODE LD rr,dd
			} // if bit 3
			break;
		case 2:
			// LD (BC),A LD (DE),A LD (DE),A LD (nn),A
			// LD A,(BC) LD (DE),A LD HL,(nn) LD A,(nn)
			switch (instruction.yyy) {
			case 0: // LD (BC),A
				instructionSize = 1;
				// DO OPCODE LD (BC),A
				break;
			case 1: // LD A,(BC)
				instructionSize = 1;
				// DO OPCODE LD A,(BC)
				break;
			case 2: // LD (DE),A
				instructionSize = 1;
				// DO OPCODE LD (DE),A
				break;
			case 3: // LD (DE),A
				instructionSize = 1;
				// DO OPCODE LD (DE),A
				break;
			case 4: // LD (DE),A
				instructionSize = 3;
				// DO OPCODE LD (DE),A
				break;
			case 5: // LD HL,(nn)
				instructionSize = 3;
				// DO OPCODE LD HL,(nn)
				break;
			case 6: // LD (nn),A
				instructionSize = 3;
				// DO OPCODE LD (nn),A
				break;
			case 7: // LD A,(nn)
				instructionSize = 3;
				// DO OPCODE LD A,(nn)
				break;
			}// switch yyy
			break;

		case 3: // INC rr DEC rr
			instructionSize = 1;
			bit3 = ((instruction.opCode1 & Z80.BIT_3) == Z80.BIT_3);
			if (bit3) {// DEC,rr
				// DO OPCODE DEC,rr
			} else { // INC rr
				// DO OPCODE INC,rr
			} // if bit 3
			break;
		case 4: // INC r
			instructionSize = 1;
			// DO OPCODE INC r
			break;
		case 5: // DEC r
			instructionSize = 1;
			// DO OPCODE DEC r
			break;
		case 6: // LD r,d
			instructionSize = 2;
			// DO OPCODE LD r,d
			break;
		case 7: // RLCA RRCA RLA RRA DAA CPL SCF CCF
			instructionSize = 1;
			switch (instruction.yyy) {
			case 0: // RLCA
				// DO OPCODERLCA
				break;
			case 1: // RRCA
				// DO OPCODE RRCA
				break;
			case 2: // RLA
				// DO OPCODE RLA
				break;
			case 3: // RRA
				// DO OPCODE RRA
				break;
			case 4: // DAA
				// DO OPCODE DAA
				break;
			case 5: // CPL
				// DO OPCODE CPL
				break;
			case 6: // SCF
				// DO OPCODE SCF
				break;
			case 7: // CCF
				// DO OPCODE CCF
				break;
			}// switch yyy
			break;

		}// switch zzz
		return instructionSize;
		//
	}// opCodePage00

	private int opCodePage01(Instruction instruction) {
		int instructionSize = 1;
		if (instruction.opCode0 == (byte) 0X76) {// HALT
			// DO OPCODE HALT
		} else {// LD r, r1
				// DO OPCODE LD r, r1
		} // if halt
		return instructionSize;
	}// opCodePage01

	private int opCodePage10(Instruction instruction) {
		int instructionSize = 1;
		switch (instruction.yyy) {
		case 0: // ADD A,r
			// DO OPCODE ADD A,r
			break;
		case 1: // ADC A,r
			// DO OPCODE ADC A,r
			break;
		case 2: // SUB r
			// DO OPCODE SUB r
			break;
		case 3: // SBC A,r
			// DO OPCODE SBC A,r
			break;
		case 4: // AND r
			// DO OPCODE AND r
			break;
		case 5: // XOR r
			// DO OPCODE XOR r
			break;
		case 6: // OR r
			// DO OPCODE OR r
			break;
		case 7: // CP r
			// DO OPCODE CP r
			break;
		}// switch yyy

		return instructionSize;
	}// opCodePage10

	private int opCodePage11(Instruction instruction) {
		int instructionSize = 0;
		boolean bit3;
		switch (instruction.zzz) {
		case 0: // Conditional Returns
			instructionSize = 1;
			// DO OPCODE Conditional Returns
			break;
		case 1: // POP rr RET JP (HL) LD SP,HL
			instructionSize = 1;
			bit3 = ((instruction.opCode1 & Z80.BIT_3) == Z80.BIT_3);
			if (bit3) {// POP rr
				// DO OPCODE POP rr
			} else {// RET EXX JP (HL) LD SP,HL
				switch (instruction.yyy) {
				case 1: // RET
					// DO OPCODE RET
					break;
				case 3: // EXX
					// DO OPCODE EXX
					break;
				case 5: // JP (HL)
					// DO OPCODE JP (HL)
					break;
				case 7: // LD SP,HL
					// DO OPCODE LD SP,HL
					break;
				}// switch yyy
			} // if bit 3
			break;
		case 2: // Conditional Jumps
			instructionSize = 3;
			// DO OPCODE Conditional Jumps
			break;
		case 3: // JP nn OUT (nn),A IN A,(nn) EX (SP),HL EX DE,HL DI EI
			switch (instruction.yyy) {

			case 0: // JP nn
				instructionSize = 3;
				// DO OPCODE JP nn
				break;
			case 1: // CB BITS
				// Extended Code
				// DO OPCODE elsewhere
				break;
			case 2: // OUT (nn),A
				instructionSize = 2;
				// DO OPCODE OUT (nn),A
				break;
			case 3: // IN A,(nn)
				instructionSize = 2;
				// DO OPCODE IN A,(nn)
				break;
			case 4: // EX (SP),HL
				instructionSize = 1;
				// DO OPCODE EX (SP),HL
				break;
			case 5: // EX DE,HL
				instructionSize = 1;
				// DO OPCODE EX DE,HL
				break;
			case 6: // DI
				instructionSize = 1;
				// DO OPCODE DI
				break;
			case 7: // EI
				instructionSize = 1;
				// DO OPCODE EI
				break;
			}// switch yyy

			break;
		case 4: // Conditional Calls
			instructionSize = 3;
			// DO OPCODE Conditional Calls
			break;
		case 5: // PUSH rr CALL nn
			bit3 = ((instruction.opCode1 & Z80.BIT_3) == Z80.BIT_3);
			if (bit3) {// PUSH rr
				instructionSize = 1;
				// DO OPCODE PUSH rr
			} else {// RET EXX JP (HL) LD SP,HL
				switch (instruction.yyy) {
				case 1: // CALL nn
					instructionSize = 3;
					// DO OPCODE CALL nn
					break;
				case 3: // DD IX
				case 5: // ED EXTD
				case 7: // FD IY
					// Extended Code
					// DO OPCODE elsewhere
					break;
				}// switch yyy
			} // if bit 3
				
			break;
		case 6: // ADC A,n ADC A,n  SUB n SBC A,n AND n XOR n OR n CP n
			instructionSize = 2;
			switch (instruction.yyy) {
			case 0:	// ADC A,n
				// DO OPCODE ADC A,n
				break;
			case 1:	// ADC A,n
				// DO OPCODE ADC A,n
				break;
			case 2:	// SUB n 
				// DO OPCODE  SUB n 
				break;
			case 3:	// SBC A,n
				// DO OPCODE SBC A,n
				break;
			case 4:	// AND n
				// DO OPCODE AND n
				break;
			case 5:	//  XOR n
				// DO OPCODE XOR n
				break;
			case 6:	// OR n
				// DO OPCODE OR n
				break;
			case 7:	// CP n
				// DO OPCODE CP n
				break;
			}// switch yyy
			// DO OPCODE
			break;
		case 7: //
			// DO OPCODE
			break;
		}// switch yyy

		return instructionSize;
	}// opCodePage11

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
