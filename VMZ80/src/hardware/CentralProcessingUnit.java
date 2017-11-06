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
	private CpuBuss cpuBuss = CpuBuss.getInstance();
	private ConditionCodeRegister ccr = ConditionCodeRegister.getInstance();
	private WorkingRegisterSet wrs = WorkingRegisterSet.getInstance();
	private ArithmeticUnit au = ArithmeticUnit.getInstance();
	private ErrorStatus error;
	// private IOController ioController;

	private AppLogger log = AppLogger.getInstance();

	public static CentralProcessingUnit getInstance() {
		return instance;
	}// getInstance

	private CentralProcessingUnit() {

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
		// long origin = System.nanoTime();
		executeInstruction(wrs.getProgramCounter());
		// long elapsedTIme = System.nanoTime() - origin;
		return !isError();
	}// startInstruction

	public void executeInstruction(int currentAddress) {
		// Register indexRegister = null;
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
			// indexRegister = instruction.doubleRegister1

			if (instruction.opCode1 == (byte) 0XCB) { // IX/IY bit instructions
				instructionLength = opCodeSetIndexRegistersBit(instruction);
			} else {// IX/IY instructions
				instructionLength = opCodeSetIndexRegisters(instruction);
			} // if bit instructions
			break;

		default: // Main instructions
			instructionLength = opCodeSetMain(instruction);

		}// switch opCode
		wrs.incrementProgramCounter(instructionLength);
		return;
	}// executeInstruction

	// Extended Instructions
	private int opCodeSetED(Instruction instruction) {
		int instructionSize = 0;
		Register sourceRegister;
		// Register destinationRegister;
		byte[] ansWord;
		// byte sourceByte, destinationByte;
		byte[] sourceWord, destinationWord;
		int sourceValue, destinationValue, destinationLocation;
		int sourceLocation;
		switch (instruction.page) {
		case 0: // Page 00
			log.addError(String.format("bad instruction %02X %02X %02X, at location %04X", instruction.opCode0,
					instruction.opCode1, instruction.opCode2, wrs.getProgramCounter()));
			System.exit(-1);
			// There are no instructions on page 0
			break;
		case 1: // Page 01
			switch (instruction.zzz) {
			case 0:// ED (40,48,50,58,60,68,70,78) - IN r,(C)
					// destinationRegister = instruction.singleRegister1;
				sourceRegister = instruction.singleRegister2;
				instructionSize = 2;
				// DO OPCODE IN r(C) note Register.M special
				break;
			case 1:// ED (41,49,51,59,61,69,71,79) - OUT r,(C)
					// destinationRegister = instruction.singleRegister1;
				sourceRegister = instruction.singleRegister2;
				instructionSize = 2;
				// DO OPCODE OUT (C),r note Register.M special
				break;
			case 2: // ED (42,52,62,72) - SBC HL,rr |ED (4A,5A,6A,7A) - ADC HL,rr
				destinationWord = wrs.getDoubleRegArray(instruction.doubleRegister1);
				sourceWord = wrs.getDoubleRegArray(instruction.doubleRegister2);
				instructionSize = 2;
				boolean bit3 = ((instruction.opCode1 & Z80.BIT_3) == Z80.BIT_3);
				if (bit3) {// ADC HL,ss
					ansWord = au.addWordWithCarry(destinationWord, sourceWord, ccr.isCarryFlagSet());
				} else {// SBC HL,ss
					ansWord = au.subWordWithCarry(destinationWord, sourceWord, ccr.isCarryFlagSet());
				} // if bit 3
				wrs.setDoubleReg(instruction.doubleRegister1, ansWord);
				break;
			case 3:// ED (43,53,63,73) - LD (nn),dd | ED (4B,5B,6B,7B) LD dd,(nn)
				sourceRegister = instruction.doubleRegister1;
				sourceLocation = instruction.getImmediateWord();
				instructionSize = 4;
				bit3 = ((instruction.opCode1 & Z80.BIT_3) == Z80.BIT_3);
				if (bit3) {// ED (4B,5B,6B,7B) LD dd,(nn)
					wrs.setDoubleReg(sourceRegister, cpuBuss.read(sourceLocation), cpuBuss.read(sourceLocation + 1));
					// DO OPCODE LD dd,(nn)
				} else {// ED (43,53,63,73) - LD (nn),dd
					byte[] valueArray = wrs.getDoubleRegArray(sourceRegister);
					cpuBuss.write(sourceLocation, valueArray[0]);
					cpuBuss.write(sourceLocation + 1, valueArray[1]);
					// DO OPCODE LD (nn),dd
				} // if bit 3
				break;
			case 4:// NEG
				instructionSize = 2;
				byte startingValue = wrs.getAcc();

				byte result = au.negate(startingValue);
				wrs.setAcc(result);

				ccr.setSignFlag(au.hasSign());
				ccr.setZeroFlag(au.isZero());
				ccr.setHFlag(au.hasHalfCarry());
				ccr.setPvFlag(startingValue == (byte) 0X80);
				ccr.setNFlag(true);
				ccr.setCarryFlag(startingValue != 00);
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
				case 0X4F: // LD R,A
					wrs.setReg(instruction.getSingleRegister1(), wrs.getReg(instruction.getSingleRegister2()));
					// no flag affected
					break;
				case 0X57: // LD A,I
				case 0X5F: // LD R,A
					byte value = wrs.getReg(instruction.getSingleRegister2());
					wrs.setReg(instruction.getSingleRegister1(), value);
					ccr.setSignFlag((value & Z80.BIT_7) == Z80.BIT_7);
					ccr.setZeroFlag(value == (byte) 00);
					ccr.setHFlag(false);
					ccr.setPvFlag(wrs.isIFF2Set());
					ccr.setNFlag(false);
					break;
				case 0X67: // RRD
				case 0X6F: // RLD
					int memLocation = wrs.getDoubleReg(instruction.getDoubleRegister1());
					byte memBefore = cpuBuss.read(memLocation);
					byte accBefore = wrs.getAcc();
					byte accResult = (byte) (accBefore & 0XF0);
					byte memResult;
					if (instruction.opCode1 == (byte) 0X67) {// RRD
						accResult = (byte) (accResult | (memBefore & 0X0F));
						memResult = (byte) ((accBefore & 0X0F) << 4);
						memResult = (byte) (memResult | ((memBefore >> 4) & 0X0F)); // DO OPCODE RRD
					} else {// RLD
						accResult = (byte) (accResult | ((memBefore >> 4) & 0X0F));
						memResult = (byte) ((memBefore << 4) & 0XF0);
						memResult = (byte) (memResult | (accBefore & 0X0F));
					} // if 67 0r 6F
					wrs.setAcc(accResult);
					cpuBuss.write(memLocation, memResult);
					ccr.setZSP(accResult);
					ccr.setHFlag(false);
					ccr.setNFlag(false);
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
				ccr.setPvFlag(!ldi(1));
				ccr.setHFlag(false);
				ccr.setNFlag(false);
				// DO OPCODE LDI
				break;
			case (byte) 0XB0: // LDIR
				ldi(wrs.getDoubleReg(Z80.Register.BC));
				break;
			case (byte) 0XA1: // CPI
				ccr.setPvFlag(compareMemoryIncDec(+1));
				ccr.setHFlag(au.hasHalfCarry());
				ccr.setSignFlag(au.hasSign());
				ccr.setZeroFlag(au.isZero());
				break;
			case (byte) 0XB1: // CPIR
				boolean pvFlag = true;
				while (true) {
					if (compareMemoryIncDec(+1) == false)
						break;

					if (au.isZero())
						break;
				} // while

				ccr.setPvFlag(pvFlag);
				ccr.setHFlag(au.hasHalfCarry());
				ccr.setSignFlag(au.hasSign());
				ccr.setZeroFlag(au.isZero());

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
				ccr.setPvFlag(!ldd(1));
				ccr.setHFlag(false);
				ccr.setNFlag(false);
				// DO OPCODE LDD
				break;
			case (byte) 0XB8: // LDDR
				ldd(wrs.getDoubleReg(Z80.Register.BC));
				// DO OPCODE LDDR
				break;
			case (byte) 0XA9: // CPD
				ccr.setPvFlag(compareMemoryIncDec(-1));
				ccr.setHFlag(au.hasHalfCarry());
				ccr.setSignFlag(au.hasSign());
				ccr.setZeroFlag(au.isZero());
				break;
			case (byte) 0XB9: // CPDR
				pvFlag = true;
				while (true) {
					if (compareMemoryIncDec(-1) == false)
						break;

					if (au.isZero())
						break;
				} // while

				ccr.setPvFlag(pvFlag);
				ccr.setHFlag(au.hasHalfCarry());
				ccr.setSignFlag(au.hasSign());
				ccr.setZeroFlag(au.isZero());

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

	// private boolean compareMemoryIncrement() {
	// return compareMemoryIncDec(+1);
	// }// compareMemoryIncrement
	//
	// private boolean compareMemoryDecrement() {
	// return compareMemoryIncDec(-1);
	// }// compareMemoryDecrement

	private boolean compareMemoryIncDec(int delta) {
		int hlValue = wrs.getDoubleReg(Register.HL);
		int bcValue = wrs.getDoubleReg(Register.BC);
		byte accValue = wrs.getAcc();
		byte memValue = cpuBuss.read(hlValue);
		hlValue = (hlValue + delta) & Z80.WORD_MASK;
		wrs.setDoubleReg(Register.HL, hlValue);
		bcValue = (bcValue - 1) & Z80.WORD_MASK;
		wrs.setDoubleReg(Register.BC, bcValue);
		au.compare(accValue, memValue);
		ccr.setNFlag(true);
		return bcValue != 0;
	}// compareMemoryDecrement

	// Bit Instructions
	private int opCodeSetCB(Instruction instruction) {
		int instructionSize = 2;

		int bit = instruction.bit;
		// byte bitMask = Z80.BITS[bit];

		Register subject = instruction.singleRegister1;
		byte sourceByte = wrs.getReg(subject);
		byte resultByte;
		switch (instruction.page) {
		case 0: // Page 00 RLC RRC RL RR SLA SRA SLL SRL
			switch (instruction.yyy) {
			case 0: // RLC 00-07
				wrs.setReg(subject, au.rotateLeft(sourceByte));
				break;
			case 1: // RRC 08-0F
				wrs.setReg(subject, au.rotateRight(sourceByte));
				break;
			case 2: // RL 08-0F
				wrs.setReg(subject, au.rotateLeftThru(sourceByte, ccr.isCarryFlagSet()));
				break;
			case 3: // RR 18-1F
				wrs.setReg(subject, au.rotateRightThru(sourceByte, ccr.isCarryFlagSet()));
				break;
			case 4: // SLA
				wrs.setReg(subject, au.shiftSLA(sourceByte));
				break;
			case 5: // SRA
				wrs.setReg(subject, au.shiftSRA(sourceByte));
				break;
			case 6: // SLL ******* not real
				// wrs.setReg(subject, au.shiftSLL(sourceByte));
				break;
			case 7: // SRL
				wrs.setReg(subject, au.shiftSRL(sourceByte));
				break;
			}// switch yyy
			ccr.setSignFlag(au.hasSign());
			ccr.setZeroFlag(au.isZero());
			ccr.setHFlag(false);
			ccr.setPvFlag(au.hasParity());
			ccr.setNFlag(false);
			ccr.setCarryFlag(au.hasCarry());

			break;
		case 1: // Page 01 BIT b,r
			au.bitTest(sourceByte, bit);
			ccr.setZeroFlag(au.isZero());
			ccr.setHFlag(true);
			ccr.setNFlag(false);
			break;
		case 2: // Page 10 RES b,r
			resultByte = au.bitRes(sourceByte, bit);
			wrs.setReg(subject, resultByte);
			break;
		case 3: // Page 11 SET b,r
			resultByte = au.bitSet(sourceByte, bit);
			wrs.setReg(subject, resultByte);
			break;
		}// switch instruction Page

		return instructionSize;
	}// opCodePageCB

	private int opCodeSetIndexRegisters(Instruction instruction) {
		int instructionSize = 0;
		Register regIXY = instruction.doubleRegister1;
		Register regRR = instruction.doubleRegister2;
		byte[] arg1, arg2;
		byte[] result;
		byte argument, answer;
		switch (instruction.opCode1) {
		case (byte) 0X09:// ADD IXY,BC
		case (byte) 0X19:// ADD IXY,DE
		case (byte) 0X29:// ADD IXY,IXY
		case (byte) 0X39:// ADD IXY,SP
			instructionSize = 2;
			arg1 = getValue(wrs.getDoubleReg(regIXY));
			arg2 = getValue(wrs.getDoubleReg(regRR));
			result = au.addWord(arg1, arg2);
			wrs.setDoubleReg(regIXY, result);
			ccr.setNFlag(false);
			ccr.setHFlag(au.hasHalfCarry());
			ccr.setCarryFlag(au.hasCarry());
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
			argument = wrs.getReg(instruction.singleRegister1);
			cpuBuss.write(instruction.getNetIndexValue(), argument);
			break;
		case (byte) 0X46: // LD B,(IXY=d)
		case (byte) 0X4E: // LD C,(IXY=d)
		case (byte) 0X56: // LD D,(IXY=d)
		case (byte) 0X5E: // LD E,(IXY=d)
		case (byte) 0X66: // LD H,(IXY=d)
		case (byte) 0X6E: // LD L,(IXY=d)
		case (byte) 0X7E: // LD A,(IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(instruction.getNetIndexValue());
			wrs.setReg(instruction.singleRegister1, argument);
			break;
		case (byte) 0X21: // LD IXY,dd
			instructionSize = 4;
			wrs.setDoubleReg(regIXY, instruction.immediateWord);
			break;
		case (byte) 0X22: // LD (dd),IXY
			instructionSize = 4;
			byte[] values = wrs.getDoubleRegArray(regIXY);
			cpuBuss.writeWord(instruction.immediateWord, values[0], values[1]);
			break;
		case (byte) 0X23: // INC IXY
			instructionSize = 2;
			result = wrs.getDoubleRegArray(regIXY);
			wrs.setDoubleReg(regIXY, au.incrementWord(result));
			break;
		case (byte) 0X2A: // LD IXY,(dd)
			int value = cpuBuss.readWordReversed(instruction.immediateWord);
			wrs.setDoubleReg(regIXY, value);
			instructionSize = 4;
			break;
		case (byte) 0X2B: // DEC IXY
			instructionSize = 2;
			result = wrs.getDoubleRegArray(regIXY);
			wrs.setDoubleReg(regIXY, au.decrementWord(result));
			break;
		case (byte) 0X34: // INC (IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(instruction.getNetIndexValue());
			answer = au.increment(argument);
			ccr.setSignFlag(au.hasSign());
			ccr.setZeroFlag(au.isZero());
			ccr.setHFlag(au.hasHalfCarry());
			ccr.setPvFlag(argument == 0X7F ? true : false);
			ccr.setNFlag(false);

			cpuBuss.write(instruction.getNetIndexValue(), answer);
			break;
		case (byte) 0X35: // DEC (IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(instruction.getNetIndexValue());
			answer = au.decrement(argument);
			ccr.setSignFlag(au.hasSign());
			ccr.setZeroFlag(au.isZero());
			ccr.setHFlag(au.hasHalfCarry());
			ccr.setPvFlag(argument == 0X80 ? true : false);
			ccr.setNFlag(true);

			cpuBuss.write(instruction.getNetIndexValue(), answer);
			break;
		case (byte) 0X36: // LD (IXY+d),n
			instructionSize = 4;
			cpuBuss.write(instruction.getNetIndexValue(), instruction.immediateByte);
			break;
		case (byte) 0X86: // ADD A,(IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(instruction.getNetIndexValue());
			answer = au.add(wrs.getAcc(), argument);
			ccr.setSignFlag(au.hasSign());
			ccr.setZeroFlag(au.isZero());
			ccr.setHFlag(au.hasHalfCarry());
			ccr.setPvFlag(au.hasOverflow());
			ccr.setNFlag(false);
			ccr.setCarryFlag(au.hasCarry());

			wrs.setAcc(answer);
			break;
		case (byte) 0X8E: // ADC A,(IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(instruction.getNetIndexValue());
			answer = au.addWithCarry(wrs.getAcc(), argument, ccr.isCarryFlagSet());
			ccr.setSignFlag(au.hasSign());
			ccr.setZeroFlag(au.isZero());
			ccr.setHFlag(au.hasHalfCarry());
			ccr.setPvFlag(au.hasOverflow());
			ccr.setNFlag(false);
			ccr.setCarryFlag(au.hasCarry());

			wrs.setAcc(answer);
			break;
		case (byte) 0X96: // SUB (IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(instruction.getNetIndexValue());
			answer = au.sub(wrs.getAcc(), argument);
			ccr.setSignFlag(au.hasSign());
			ccr.setZeroFlag(au.isZero());
			ccr.setHFlag(au.hasHalfCarry());
			ccr.setPvFlag(au.hasOverflow());
			ccr.setNFlag(false);
			ccr.setCarryFlag(au.hasCarry());

			wrs.setAcc(answer);
			break;
		case (byte) 0X9E: // SBC A,(IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(instruction.getNetIndexValue());
			answer = au.subWithCarry(wrs.getAcc(), argument, ccr.isCarryFlagSet());
			ccr.setSignFlag(au.hasSign());
			ccr.setZeroFlag(au.isZero());
			ccr.setHFlag(au.hasHalfCarry());
			ccr.setPvFlag(au.hasOverflow());
			ccr.setNFlag(true);
			ccr.setCarryFlag(au.hasCarry());

			wrs.setAcc(answer);

			break;
		case (byte) 0XA6: // AND(IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(instruction.getNetIndexValue());
			answer = au.and(wrs.getAcc(), argument);
			ccr.setSignFlag(au.hasSign());
			ccr.setZeroFlag(au.isZero());
			ccr.setHFlag(true);
			ccr.setPvFlag(au.hasParity());
			ccr.setNFlag(false);
			ccr.setCarryFlag(false);

			wrs.setAcc(answer);
			break;
		case (byte) 0XAE: // XOR(IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(instruction.getNetIndexValue());
			answer = au.xor(wrs.getAcc(), argument);
			ccr.setSignFlag(au.hasSign());
			ccr.setZeroFlag(au.isZero());
			ccr.setHFlag(false);
			ccr.setPvFlag(au.hasParity());
			ccr.setNFlag(false);
			ccr.setCarryFlag(false);

			wrs.setAcc(answer);
			break;
		case (byte) 0XB6: // OR(IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(instruction.getNetIndexValue());
			answer = au.or(wrs.getAcc(), argument);
			ccr.setSignFlag(au.hasSign());
			ccr.setZeroFlag(au.isZero());
			ccr.setHFlag(false);
			ccr.setPvFlag(au.hasParity());
			ccr.setNFlag(false);
			ccr.setCarryFlag(false);

			wrs.setAcc(answer);
			break;
		case (byte) 0XBE: // CP(IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(instruction.getNetIndexValue());
			au.compare(wrs.getAcc(), argument);

			ccr.setSignFlag(au.hasSign());
			ccr.setZeroFlag(au.isZero());
			ccr.setHFlag(au.hasHalfCarry());
			ccr.setPvFlag(au.hasOverflow());
			ccr.setNFlag(true);
			ccr.setCarryFlag(au.hasCarry());

			break;
		case (byte) 0XE1: // POP IXY
			instructionSize = 2;
			result = this.opCode_Pop();
			if (regIXY.equals(Register.IX)) {
				wrs.setIX(result);
			} else {
				wrs.setIY(result);
			} // if
			break;
		case (byte) 0XE3: // EX (SP)IXY
			instructionSize = 2;
			int sp = wrs.getStackPointer();
			arg1 = cpuBuss.popWordLoHi(sp);
			arg2 = wrs.getDoubleRegArray(regIXY);
			wrs.setDoubleReg(regIXY, arg1);
			cpuBuss.write(sp, arg2[0]);
			cpuBuss.write(sp + 1, arg2[1]);
			break;
		case (byte) 0XE5: // PUSH IXY
			instructionSize = 2;
			this.opCode_Push(wrs.getDoubleReg(regIXY));
			break;
		case (byte) 0XE9: // JP (IXY)
			instructionSize = 0;
			wrs.setProgramCounter(wrs.getDoubleReg(regIXY));
			break;
		case (byte) 0XF9: // LD SP,IXY
			instructionSize = 2;
			wrs.setStackPointer(wrs.getDoubleReg(regIXY));
			break;
		}// switch opCode1

		return instructionSize;
	}// opCodePageDD

	// private byte[] intToByteArray(int value) {
	// byte[] ans = new byte[2];
	// ans[0] = (byte) (value & Z80.BYTE_MASK); // lsb
	// ans[1] = (byte) ((value >> 8) & Z80.BYTE_MASK);
	// return ans;
	// }//intToByteArray
	//
	private byte[] getValue(int workingValue) {
		byte msb = (byte) ((workingValue & 0XFF00) >> 8);
		byte lsb = (byte) ((byte) workingValue & 0X00FF);
		return new byte[] { lsb, msb };
	}// getValue

	private int opCodeSetIndexRegistersBit(Instruction instruction) {
		int instructionSize = 4;
		int netLocation;
		byte sourceByte, result = 0x00;
		byte code3 = cpuBuss.read(wrs.getProgramCounter() + 3);
		int subInstructionPage = code3 >> 6 & 0B011;

		netLocation = instruction.getNetIndexValue();
		// sourceByte = cpuBuss.read(instruction.getNetIndexValue());
		sourceByte = cpuBuss.read(netLocation);

		switch (subInstructionPage) {
		case 0: // Page 00 RLC RRC RL RR SLA SRA SLL SRL --- (IXY+d)

			switch (code3) {
			case 0x06: // RLC (IXY+d)
				result = au.rotateLeft(sourceByte);
				// DO OPCODE RLC (IXY+d)
				break;
			case 0x0E: // RRC (IXY+d)
				result = au.rotateRight(sourceByte);
				break;
			case 0x16: // RL (IXY+d)
				result = au.rotateLeftThru(sourceByte, ccr.isCarryFlagSet());
				break;
			case 0x1E: // RR (IXY+d)
				result = au.rotateRightThru(sourceByte, ccr.isCarryFlagSet());
				break;
			case 0x26: // SLA (IXY+d)
				result = au.shiftSLA(sourceByte);
				break;
			case 0x2E: // SRA (IXY+d)
				result = au.shiftSRA(sourceByte);
				break;
			case 0x36: // SLL (IXY+d)******* not real
				// DO OPCODE SLL (IXY+d)
				break;
			case 0x3E: // SRL (IXY+d)
				result = au.shiftSRL(sourceByte);
				break;
			}// switch code3 (IXY+d)
			cpuBuss.write(netLocation, result);

			ccr.setSignFlag(au.hasSign());
			ccr.setZeroFlag(au.isZero());
			ccr.setHFlag(false);
			ccr.setPvFlag(au.hasParity());
			ccr.setNFlag(false);
			ccr.setCarryFlag(au.hasCarry());

			break; // case 0 page 0
		case 1: // Page 01 BIT b,(IXY+d)
			au.bitTest(sourceByte, instruction.bit);
			ccr.setZeroFlag(au.isZero());
			ccr.setHFlag(true);
			ccr.setNFlag(false);
			break;
		case 2: // Page 10 RES b,(IXY+d)
			result = au.bitRes(sourceByte, instruction.bit);
			cpuBuss.write(netLocation, result);
			break;
		case 3: // Page 11 SET b,(IXY+d)
			result = au.bitSet(sourceByte, instruction.bit);
			cpuBuss.write(netLocation, result);

			break;

		}// switch instruction Page
		return instructionSize;
	}// opCodePageDD

	private int opCodeSetMain(Instruction instruction) {
		int instructionSize = 0;

		// int page = (opCode >> 6) & 0X0003; // only want the value of bits 6 & 7
		// int yyy = (opCode >> 3) & 0X0007; // only want the value of bits 3,4 & 5
		// int zzz = opCode & 0X0007; // only want the value of bits 0,1 & 2

		switch (instruction.page) {
		case 0:
			instructionSize = opCodePage00(instruction);
			break;
		case 1:
			instructionSize = opCodePage01(instruction);
			break;
		case 2:
			instructionSize = opCodePage10(instruction);
			break;
		case 3:
			instructionSize = opCodePage11(instruction);
			break;
		default:
			// // setError(ErrorStatus.INVALID_OPCODE);

		}// Switch page
		return instructionSize;
	}// opCodePageSingle
		// --------------------------------------------------------------------------------------------------------

	private int opCodePage00(Instruction instruction) {
		int sourceValue, destinationValue, ansValue;
		byte source, destination, ans;
		byte[] sourceValueArray, destinationValueArray, ansValueArray;
		int instructionSize = 0;
		boolean bit3;
		int currentAddress = wrs.getProgramCounter();
		int directAddress = cpuBuss.readWordReversed(currentAddress + 1);
		// 00 YYY ZZZ
		switch (instruction.zzz) {

		case 0: // NOP DJNZ e
			switch (instruction.yyy) {
			case 0: // NOP
				instructionSize = 1;
				// DO OPCODE NOP
				break;
			case 1: // EX AF,AF'
				instructionSize = 1;
				wrs.swapAF();
				break;
			case 2: // DJNZ e
				source = wrs.getReg(Register.B);
				ans = au.decrement(source);
				wrs.setReg(Register.B, ans);
				if (au.isZero()) {
					instructionSize = 0;
					opCode_Jump(currentAddress + instruction.immediateByte + 2);
				} else {
					instructionSize = 2;
				} // if
				break;
			case 3: // JR e
				instructionSize = 0;
				opCode_Jump(currentAddress + instruction.immediateByte + 2);
				break;
			case 4: // JR NZ,e
				if (opCodeConditionTrue(ConditionFlag.NZ)) {
					instructionSize = 0;
					opCode_Jump(currentAddress + instruction.immediateByte + 2);
				} else {
					instructionSize = 2;
				} // if
				break;
			case 5: // JR Z,e
				if (opCodeConditionTrue(ConditionFlag.Z)) {
					instructionSize = 0;
					opCode_Jump(currentAddress + instruction.immediateByte + 2);
				} else {
					instructionSize = 2;
				} // if
				break;
			case 6: // JR NC,e
				if (opCodeConditionTrue(ConditionFlag.NC)) {
					instructionSize = 0;
					opCode_Jump(currentAddress + instruction.immediateByte + 2);
				} else {
					instructionSize = 2;
				} // if
				break;
			case 7: // JR C,e
				if (opCodeConditionTrue(ConditionFlag.C)) {
					instructionSize = 0;
					opCode_Jump(currentAddress + instruction.immediateByte + 2);
				} else {
					instructionSize = 2;
				} // if

				break;
			}// switch yyy

			break;
		case 1: // LD rr,dd ADD HL,rr
			bit3 = ((instruction.opCode1 & Z80.BIT_3) == Z80.BIT_3);
			if (bit3) {// ADD HL,rr

				destinationValueArray = wrs.getDoubleRegArray(instruction.doubleRegister1);
				sourceValueArray = wrs.getDoubleRegArray(instruction.doubleRegister2);
				ansValueArray = au.addWord(destinationValueArray, sourceValueArray);
				wrs.setDoubleReg(instruction.doubleRegister1, ansValueArray);

				// destinationValue = wrs.getDoubleReg(instruction.doubleRegister1);
				// sourceValue = wrs.getDoubleReg(instruction.doubleRegister2);
				// ansValue = au.addWord(destinationValue, sourceValue);
				// wrs.setDoubleReg(instruction.doubleRegister1, ansValue);
				instructionSize = 1;
				// DO OPCODE ADD HL,rr
			} else { // LD rr
				instructionSize = 3;
				// DO OPCODE LD rr,dd
			} // if bit 3
			break;
		case 2: // 02,0A,12,1A,22,2A,32,3A
			// LD (BC),A LD (DE),A LD (DE),A LD (nn),A
			// LD A,(BC) LD (DE),A LD HL,(nn) LD A,(nn)
			switch (instruction.yyy) {
			case 0: // 02 - LD (BC),A
				instructionSize = 1;
				// DO OPCODE LD (BC),A
				break;
			case 1: // 0A - LD A,(BC)
				instructionSize = 1;
				// DO OPCODE LD A,(BC)
				break;
			case 2: // 12 - LD (DE),A
				instructionSize = 1;
				// DO OPCODE LD (DE),A
				break;
			case 3: // 1A - LD A,(DE)
				instructionSize = 1;
				// DO OPCODE LD (DE),A
				break;
			case 4: // 22 - LD (nn),HL
				instructionSize = 3;
				byte[] valueArray = wrs.getDoubleRegArray(instruction.doubleRegister1);
				cpuBuss.write(directAddress, valueArray[0]);
				cpuBuss.write(directAddress + 1, valueArray[1]);

				break;
			case 5: // 2A - LD HL,(nn)
				instructionSize = 3;
				wrs.setDoubleReg(instruction.doubleRegister1, cpuBuss.read(directAddress),
						cpuBuss.read(directAddress + 1));
				break;
			case 6: // 32 - LD (nn),A
				instructionSize = 3;
				// DO OPCODE LD (nn),A
				break;
			case 7: // 3A - LD A,(nn)
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
		int currentAddress = wrs.getProgramCounter();
		int directAddress = cpuBuss.readWordReversed(currentAddress + 1);

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
		case 3: // JP nn OUT (n),A IN A,(n) EX (SP),HL EX DE,HL DI EI
			switch (instruction.yyy) {

			case 0: // JP nn
				instructionSize = 0;
				opCode_Jump(directAddress);
				break;
			case 1: // CB BITS
				// Extended Code
				// DO OPCODE elsewhere
				break;
			case 2: // OUT (n),A
				instructionSize = 2;
				// DO OPCODE OUT (n),A
				break;
			case 3: // IN A,(n)
				instructionSize = 2;
				// DO OPCODE IN A,(n)
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
		case 6: // ADC A,n ADC A,n SUB n SBC A,n AND n XOR n OR n CP n
			instructionSize = 2;
			switch (instruction.yyy) {
			case 0: // ADC A,n
				// DO OPCODE ADC A,n
				break;
			case 1: // ADC A,n
				// DO OPCODE ADC A,n
				break;
			case 2: // SUB n
				// DO OPCODE SUB n
				break;
			case 3: // SBC A,n
				// DO OPCODE SBC A,n
				break;
			case 4: // AND n
				// DO OPCODE AND n
				break;
			case 5: // XOR n
				// DO OPCODE XOR n
				break;
			case 6: // OR n
				// DO OPCODE OR n
				break;
			case 7: // CP n
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

	/**
	 * gets the top of the stack and adjusts the stack pointer (+2)
	 * 
	 * @return result[0] <- (SP), result[1] <- (SP+1)
	 */
	private byte[] opCode_Pop() {
		int spLocation = wrs.getStackPointer();
		byte[] result = cpuBuss.popWordLoHi(spLocation);
		wrs.setStackPointer(spLocation + 2);
		return result;
	}

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

	private void opCode_Jump(int targetLocation) {
		// int currentProgramCounter = wrs.getProgramCounter();
		// int memoryLocation = cpuBuss.readWordReversed(currentProgramCounter + 1);
		wrs.setProgramCounter(targetLocation);
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

	private boolean ldi(int count) {
		int hlLocation, deLocation;
		boolean pvSetting = false;
		for (int i = 0; i < count; i++) {
			hlLocation = wrs.getDoubleReg(Z80.Register.HL);
			deLocation = wrs.getDoubleReg(Z80.Register.DE);
			cpuBuss.write(deLocation, cpuBuss.read(hlLocation));
			incrementDoubleRegister(Z80.Register.HL);
			incrementDoubleRegister(Z80.Register.DE);
			pvSetting = decrementDoubleRegister(Z80.Register.BC);
		} // for < count
		return pvSetting;
	}// ldi

	private boolean ldd(int count) {
		int hlLocation, deLocation;
		boolean pvSetting = false;
		for (int i = 0; i < count; i++) {
			hlLocation = wrs.getDoubleReg(Z80.Register.HL);
			deLocation = wrs.getDoubleReg(Z80.Register.DE);
			cpuBuss.write(deLocation, cpuBuss.read(hlLocation));
			decrementDoubleRegister(Z80.Register.HL);
			decrementDoubleRegister(Z80.Register.DE);
			pvSetting = decrementDoubleRegister(Z80.Register.BC);
		} // for < count
		return pvSetting;
	}// ldi

	private void incrementDoubleRegister(Register reg) {
		byte[] values = au.incrementWord(wrs.getDoubleRegArray(reg));

		wrs.setDoubleReg(reg, values[1], values[0]);
	}// incrementDoubleRegister

	private boolean decrementDoubleRegister(Register reg) {
		byte[] values = au.decrementWord(wrs.getDoubleRegArray(reg));
		wrs.setDoubleReg(reg, values);
		return au.isZero();
	}// incrementDoubleRegister

	// --------------------------------------------------------------------------------------------------------

	private byte[] splitWord(int wordValue) {
		return new byte[] { (byte) ((wordValue >> 8) & 0XFF), (byte) (wordValue & 0XFF) };
	}// split word

	private byte[] splitWordReverse(int wordValue) {
		return new byte[] { (byte) (wordValue & 0XFF), (byte) ((wordValue >> 8) & 0XFF) };
	}// split word

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
