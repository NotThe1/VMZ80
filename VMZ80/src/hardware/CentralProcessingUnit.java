package hardware;

import codeSupport.AppLogger;
import codeSupport.Z80;
import codeSupport.Z80.ConditionCode;
import codeSupport.Z80.Register;
import ioSystem.IOController;
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
	private IOController ioc = IOController.getInstance();
	private AppLogger log = AppLogger.getInstance();
	// private IOController ioController;

	private static ErrorStatus errorCurrent = ErrorStatus.NONE;
	private int instructionBase;
	private int interruptMode = Z80.MODE_0;

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
			executeInstruction(wrs.getProgramCounter());
		} // while
		System.out.printf("[]cpu.run pc = %04X%n", wrs.getProgramCounter());
	}// run

	public boolean startInstruction() {
		// long origin = System.nanoTime();
		executeInstruction(wrs.getProgramCounter());
		// long elapsedTIme = System.nanoTime() - origin;
		return !isError();
	}// startInstruction

//	static int count = 0;

	public void executeInstruction(int currentAddress) {
		byte opCode = cpuBuss.readOpCode(currentAddress);
		if (cpuBuss.isThisDebugHalt()) {
			setError(ErrorStatus.STOP);
			return;
		} // if debug

//		log.infof("%05d\t%04X%n", count++, currentAddress);
//		log.infof("%04X: %02X\t%d%n", currentAddress,opCode,count++);
		
		this.instructionBase = currentAddress;
		setError(ErrorStatus.NONE);

		int instructionLength;
		switch (opCode) {
		case (byte) 0X0ED: // Extended Instructions
			instructionLength = opCodeSetED();
			break;
		case (byte) 0X0CB: // Bit Instructions
			instructionLength = opCodeSetCB();
			break;
		case (byte) 0X0DD: // IX Instructions
		case (byte) 0X0FD: // IY Instructions
			// indexRegister = instruction.doubleRegister1

			if (cpuBuss.read(instructionBase + 1) == (byte) 0XCB) { // IX/IY bit instructions
				instructionLength = opCodeSetIndexRegistersBit();
			} else {// IX/IY instructions
				instructionLength = opCodeSetIndexRegisters();
			} // if bit instructions
			break;

		default: // Main instructions
			instructionLength = opCodeSetMain();
		}// switch opCode
		wrs.incrementProgramCounter(instructionLength);
		return;
	}// executeInstruction

	// Extended Instructions
	private int opCodeSetED() {
		int insBasePlus1 = this.instructionBase + 1;
		byte opCode1 = cpuBuss.read(insBasePlus1);
		int instructionSize = 0;
		Register currentRegister;
		byte value;
		byte[] ansWord, sourceWord, destinationWord;
		int sourceLocation;

		int page = getPage(insBasePlus1);
		int zzz = getZZZ(insBasePlus1);

		switch (page) {
		case 0: // Page 00
			reportOpCodeError();
			// There are no instructions on page 0
			break;
		case 1: // Page 01
			switch (zzz) {
			case 0:// ED (40,48,50,58,60,68,70,78) - IN r,(C)
					// destinationRegister = instruction.singleRegister1;
				currentRegister = getSingleRegister345(insBasePlus1);
				instructionSize = 2;
				// DO OPCODE IN r(C) note Register.M special
				break;
			case 1:// ED (41,49,51,59,61,69,71,79) - OUT r,(C)
					// destinationRegister = instruction.singleRegister1;
				currentRegister = getSingleRegister345(insBasePlus1);
				instructionSize = 2;
				// DO OPCODE OUT (C),r note Register.M special
				break;
			case 2: // ED (42,52,62,72) - SBC HL,rr |ED (4A,5A,6A,7A) - ADC HL,rr
				destinationWord = wrs.getDoubleRegArray(Register.HL);
				currentRegister = getDoubleRegister1_45(insBasePlus1);
				sourceWord = wrs.getDoubleRegArray(currentRegister);
				instructionSize = 2;
				if (isBit3Set(insBasePlus1)) {// ADC HL,ss
					ansWord = au.addWordWithCarry(destinationWord, sourceWord, ccr.isCarryFlagSet());
				} else {// SBC HL,ss
					ansWord = au.subWordWithCarry(destinationWord, sourceWord, ccr.isCarryFlagSet());
				} // if bit 3
				wrs.setDoubleReg(Register.HL, ansWord);
				break;
			case 3:// ED (43,53,63,73) - LD (nn),dd | ED (4B,5B,6B,7B) LD dd,(nn)
				currentRegister = getDoubleRegister1_45(insBasePlus1);
				sourceLocation = getImmediateWord(instructionBase + 2);
				instructionSize = 4;

				if (isBit3Set(insBasePlus1)) {// ED (4B,5B,6B,7B) LD dd,(nn)
					wrs.setDoubleReg(currentRegister, cpuBuss.read(sourceLocation), cpuBuss.read(sourceLocation + 1));
				} else {// ED (43,53,63,73) - LD (nn),dd
					byte[] valueArray = wrs.getDoubleRegArray(currentRegister);
					cpuBuss.write(sourceLocation, valueArray[0]);
					cpuBuss.write(sourceLocation + 1, valueArray[1]);
				} // if bit 3
				break;
			case 4:// NEG ED 44
				instructionSize = 2;
				byte startingValue = wrs.getAcc();

				byte result = au.negate(startingValue);
				wrs.setAcc(result);

				ccr.setSignFlag(au.isSignFlagSet());
				ccr.setZeroFlag(au.isZeroFlagSet());
				ccr.setHFlag(au.isHCarryFlagSet());
				ccr.setPvFlag(startingValue == (byte) 0X80);
				ccr.setNFlag(true);
				ccr.setCarryFlag(startingValue != 00);
				break;
			case 5:// RETN RETI
				instructionSize = 0;
				if (opCode1 == 0X4D) {// RETI ED 4D
					opCode_Return();
					// TO DO send signal to I/I devices
				} else {// RETN ED 45
					wrs.setIFF1(wrs.isIFF2Set());
					opCode_Return();
					// DO OPCODE RETN
				} // if RETx
				break;
			case 6: // IM 0 IM 1 IM 2
				instructionSize = 2;
				switch (opCode1) {
				case 0x46: // IM 0
				case 0x66: // IM 0
					interruptMode = Z80.MODE_0;
					break;
				case 0x56: // IM 1
				case 0x76: // IM 1
					interruptMode = Z80.MODE_1;
					break;
				case 0x5E: // IM 2
				case 0x7E: // IM 2
					interruptMode = Z80.MODE_2;
					break;
				}// case IM x
				break;
			case 7: // LD I,A LD A,I RRD LD R,A LD A,R RLD
				instructionSize = 2;
				switch (opCode1) {
				case 0X47: // LD I,A
				case 0X4F: // LD R,A
					wrs.setReg(opCode1 == 0x47 ? Register.I : Register.R, wrs.getAcc());
					// no flag affected
					break;
				case 0X57: // LD A,I
				case 0X5F: // LD R,A
					value = wrs.getReg(opCode1 == 0x57 ? Register.I : Register.R);
					wrs.setAcc(value);
					ccr.setSignFlag((value & Z80.BIT_7) == Z80.BIT_7);
					ccr.setZeroFlag(value == (byte) 00);
					ccr.setHFlag(false);
					ccr.setPvFlag(wrs.isIFF2Set());
					ccr.setNFlag(false);
					break;
				case 0X67: // RRD
				case 0X6F: // RLD
					int memLocation = wrs.getDoubleReg(Register.HL);
					byte memBefore = cpuBuss.read(memLocation);
					byte accBefore = wrs.getAcc();
					byte accResult = (byte) (accBefore & 0XF0);
					byte memResult;
					if (opCode1 == (byte) 0X67) {// RRD
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
					reportOpCodeError();
				}// switch opCode1
				break;
			default:
				reportOpCodeError();
			}// switch zzz
			break;

		case 2: // Page 10
			// LDI CPI INI OUTI LDD CPD IND OUTD
			// LDIR CPIR INIR OTIR LDDR CPDR INDR OTDR
			instructionSize = 2;

			switch (opCode1) {
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
				ccr.setHFlag(au.isHCarryFlagSet());
				ccr.setSignFlag(au.isSignFlagSet());
				ccr.setZeroFlag(au.isZeroFlagSet());
				break;
			case (byte) 0XB1: // CPIR
				boolean pvFlag = true;
				while (true) {
					if (compareMemoryIncDec(+1) == false)
						break;
					if (au.isZeroFlagSet())
						break;
				} // while

				ccr.setPvFlag(pvFlag);
				ccr.setHFlag(au.isHCarryFlagSet());
				ccr.setSignFlag(au.isSignFlagSet());
				ccr.setZeroFlag(au.isZeroFlagSet());
				break;
			case (byte) 0XA2: // INI
				// TODO OPCODE INI
				break;
			case (byte) 0XB2: // INIR
				// TODO OPCODE INIR
				break;
			case (byte) 0XA3: // OUTI
				// TODO OPCODE OUTI
				break;
			case (byte) 0XB3: // OTIR
				// TODO OPCODE OTIR
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
				ccr.setHFlag(au.isHCarryFlagSet());
				ccr.setSignFlag(au.isSignFlagSet());
				ccr.setZeroFlag(au.isZeroFlagSet());
				break;
			case (byte) 0XB9: // CPDR
				pvFlag = true;
				while (true) {
					if (compareMemoryIncDec(-1) == false)
						break;
					if (au.isZeroFlagSet())
						break;
				} // while

				ccr.setPvFlag(pvFlag);
				ccr.setHFlag(au.isHCarryFlagSet());
				ccr.setSignFlag(au.isSignFlagSet());
				ccr.setZeroFlag(au.isZeroFlagSet());
				break;
			case (byte) 0XAA: // IND
				// TODO OPCODE IND
				break;
			case (byte) 0XBA: // INDR
				// TODO OPCODE INDR
				break;
			case (byte) 0XAB: // OUTD
				// TODO OPCODE OUTD
				break;
			case (byte) 0XBB: // OTDR
				// TODO OPCODE OTDR
				break;

			}// switch opCode1
			break;

		case 3: // Page 11
			// There are no instructions on page 3
			break;
		}// switch page
		return instructionSize;
	}// opCodePageED

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
	private int opCodeSetCB() {
		int instructionSize = 2;
		int page = getPage(instructionBase + 1);
		int yyy = getYYY(instructionBase + 1);
		// int zzz = getZZZ(instructionBase + 1);
		int targetBit = yyy;

		Register subject = getSingleRegister012(instructionBase + 1);
		byte sourceByte = wrs.getReg(subject);
		byte resultByte;
		switch (page) {
		case 0: // Page 00 RLC RRC RL RR SLA SRA SLL SRL
			switch (yyy) {
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
			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(false);
			ccr.setPvFlag(au.isParityFlagSet());
			ccr.setNFlag(false);
			ccr.setCarryFlag(au.isCarryFlagSet());
			break;
		case 1: // Page 01 BIT b,r
			au.bitTest(sourceByte, targetBit);
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(true);
			ccr.setNFlag(false);
			break;
		case 2: // Page 10 RES b,r
			resultByte = au.bitRes(sourceByte, targetBit);
			wrs.setReg(subject, resultByte);
			break;
		case 3: // Page 11 SET b,r
			resultByte = au.bitSet(sourceByte, targetBit);
			wrs.setReg(subject, resultByte);
			break;
		}// switch instruction Page

		return instructionSize;
	}// opCodePageCB

	private int opCodeSetIndexRegisters() {
		int instructionSize = 0;
		Z80.Register activeIndexRegister = cpuBuss.read(instructionBase) == (byte) 0XDD ? Z80.Register.IX
				: Z80.Register.IY;

		int indexRegisterContents = wrs.getDoubleReg(activeIndexRegister);
		int indexDisplacement = cpuBuss.read(instructionBase + 2);
		int netLocation = (indexRegisterContents + indexDisplacement) & Z80.WORD_MASK;

		Register regDouble, regSingle;
		byte[] arg1, arg2, result;
		byte argument, answer, immediateByte;
		int immediateWord;

		switch (cpuBuss.read(instructionBase + 1)) {
		case (byte) 0X09:// ADD IXY,BC
		case (byte) 0X19:// ADD IXY,DE
		case (byte) 0X29:// ADD IXY,IXY
		case (byte) 0X39:// ADD IXY,SP
			instructionSize = 2;
			regDouble = Z80.doubleRegisters1[(cpuBuss.read(instructionBase + 1) >> 4) & 0b0000_0011];
			arg1 = getValue(wrs.getDoubleReg(activeIndexRegister));
			arg2 = getValue(wrs.getDoubleReg(regDouble));
			result = au.addWord(arg1, arg2);
			wrs.setDoubleReg(activeIndexRegister, result);
			ccr.setNFlag(false);
			ccr.setHFlag(au.isHCarryFlagSet());
			ccr.setCarryFlag(au.isCarryFlagSet());
			break;
		case (byte) 0X70: // LD (IXY+d),B
		case (byte) 0X71: // LD (IXY+d),C
		case (byte) 0X72: // LD (IXY+d),D
		case (byte) 0X73: // LD (IXY+d),E
		case (byte) 0X74: // LD (IXY+d),H
		case (byte) 0X75: // LD (IXY+d),L
		case (byte) 0X76: // LD (IXY+d),M
		case (byte) 0X77: // LD (IXY=d),A
			instructionSize = 3;
			regSingle = Z80.singleRegisters[cpuBuss.read(instructionBase + 1) & 0b0000_0111];
			argument = wrs.getReg(regSingle);
			cpuBuss.write(netLocation, argument);
			break;
		case (byte) 0X46: // LD B,(IXY=d)
		case (byte) 0X4E: // LD C,(IXY=d)
		case (byte) 0X56: // LD D,(IXY=d)
		case (byte) 0X5E: // LD E,(IXY=d)
		case (byte) 0X66: // LD H,(IXY=d)
		case (byte) 0X6E: // LD L,(IXY=d)
		case (byte) 0X7E: // LD A,(IXY+d)
			instructionSize = 3;
			regSingle = Z80.singleRegisters[(cpuBuss.read(instructionBase + 1) >> 3) & 0b0000_0111];
			argument = cpuBuss.read(netLocation);
			wrs.setReg(regSingle, argument);
			break;
		case (byte) 0X21: // LD IXY,dd
			instructionSize = 4;
			immediateWord = getImmediateWord(instructionBase + 2);
			wrs.setDoubleReg(activeIndexRegister, immediateWord);
			break;
		case (byte) 0X22: // LD (dd),IXY
			instructionSize = 4;
			immediateWord = getImmediateWord(instructionBase + 2);
			byte[] values = wrs.getDoubleRegArray(activeIndexRegister);
			cpuBuss.writeWord(immediateWord, values[0], values[1]);
			break;
		case (byte) 0X23: // INC IXY
			instructionSize = 2;
			result = wrs.getDoubleRegArray(activeIndexRegister);
			wrs.setDoubleReg(activeIndexRegister, au.incrementWord(result));
			break;
		case (byte) 0X2A: // LD IXY,(dd)
			immediateWord = getImmediateWord(instructionBase + 2);
			int value = cpuBuss.readWordReversed(immediateWord);
			wrs.setDoubleReg(activeIndexRegister, value);
			instructionSize = 4;
			break;
		case (byte) 0X2B: // DEC IXY
			instructionSize = 2;
			result = wrs.getDoubleRegArray(activeIndexRegister);
			wrs.setDoubleReg(activeIndexRegister, au.decrementWord(result));
			break;
		case (byte) 0X34: // INC (IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(netLocation);
			answer = au.increment(argument);
			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(au.isHCarryFlagSet());
			ccr.setPvFlag(argument == 0X7F ? true : false);
			ccr.setNFlag(false);

			cpuBuss.write(netLocation, answer);
			break;
		case (byte) 0X35: // DEC (IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(netLocation);
			answer = au.decrement(argument);
			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(au.isHCarryFlagSet());
			ccr.setPvFlag(argument == -128 ? true : false);
			ccr.setNFlag(true);

			cpuBuss.write(netLocation, answer);
			break;
		case (byte) 0X36: // LD (IXY+d),n
			instructionSize = 4;
			immediateByte = cpuBuss.read(instructionBase + 3);
			cpuBuss.write(netLocation, immediateByte);
			break;
		case (byte) 0X86: // ADD A,(IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(netLocation);
			answer = au.add(wrs.getAcc(), argument);
			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(au.isHCarryFlagSet());
			ccr.setPvFlag(au.isOverflowFlagSet());
			ccr.setNFlag(false);
			ccr.setCarryFlag(au.isCarryFlagSet());

			wrs.setAcc(answer);
			break;
		case (byte) 0X8E: // ADC A,(IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(netLocation);
			answer = au.addWithCarry(wrs.getAcc(), argument, ccr.isCarryFlagSet());
			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(au.isHCarryFlagSet());
			ccr.setPvFlag(au.isOverflowFlagSet());
			ccr.setNFlag(false);
			ccr.setCarryFlag(au.isCarryFlagSet());

			wrs.setAcc(answer);
			break;
		case (byte) 0X96: // SUB (IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(netLocation);
			answer = au.sub(wrs.getAcc(), argument);
			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(au.isHCarryFlagSet());
			ccr.setPvFlag(au.isOverflowFlagSet());
			ccr.setNFlag(false);
			ccr.setCarryFlag(au.isCarryFlagSet());

			wrs.setAcc(answer);
			break;
		case (byte) 0X9E: // SBC A,(IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(netLocation);
			answer = au.subWithCarry(wrs.getAcc(), argument, ccr.isCarryFlagSet());
			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(au.isHCarryFlagSet());
			ccr.setPvFlag(au.isOverflowFlagSet());
			ccr.setNFlag(true);
			ccr.setCarryFlag(au.isCarryFlagSet());

			wrs.setAcc(answer);

			break;
		case (byte) 0XA6: // AND(IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(netLocation);
			answer = au.and(wrs.getAcc(), argument);
			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(true);
			ccr.setPvFlag(au.isParityFlagSet());
			ccr.setNFlag(false);
			ccr.setCarryFlag(false);

			wrs.setAcc(answer);
			break;
		case (byte) 0XAE: // XOR(IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(netLocation);
			answer = au.xor(wrs.getAcc(), argument);
			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(false);
			ccr.setPvFlag(au.isParityFlagSet());
			ccr.setNFlag(false);
			ccr.setCarryFlag(false);

			wrs.setAcc(answer);
			break;
		case (byte) 0XB6: // OR(IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(netLocation);
			answer = au.or(wrs.getAcc(), argument);
			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(false);
			ccr.setPvFlag(au.isParityFlagSet());
			ccr.setNFlag(false);
			ccr.setCarryFlag(false);

			wrs.setAcc(answer);
			break;
		case (byte) 0XBE: // CP(IXY+d)
			instructionSize = 3;
			argument = cpuBuss.read(netLocation);
			au.compare(wrs.getAcc(), argument);

			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(au.isHCarryFlagSet());
			ccr.setPvFlag(au.isOverflowFlagSet());
			ccr.setNFlag(true);
			ccr.setCarryFlag(au.isCarryFlagSet());

			break;
		case (byte) 0XE1: // POP IXY
			instructionSize = 2;
			result = this.opCode_Pop();
			if (activeIndexRegister.equals(Register.IX)) {
				wrs.setIX(result);
			} else {
				wrs.setIY(result);
			} // if
			break;
		case (byte) 0XE3: // EX (SP)IXY
			instructionSize = 2;
			int sp = wrs.getStackPointer();
			arg1 = cpuBuss.popWordLoHi(sp);
			arg2 = wrs.getDoubleRegArray(activeIndexRegister);
			wrs.setDoubleReg(activeIndexRegister, arg1);
			cpuBuss.write(sp, arg2[0]);
			cpuBuss.write(sp + 1, arg2[1]);
			break;
		case (byte) 0XE5: // PUSH IXY
			instructionSize = 2;
			this.opCode_Push(wrs.getDoubleReg(activeIndexRegister));
			break;
		case (byte) 0XE9: // JP (IXY)
			instructionSize = 0;
			wrs.setProgramCounter(wrs.getDoubleReg(activeIndexRegister));
			break;
		case (byte) 0XF9: // LD SP,IXY
			instructionSize = 2;
			wrs.setStackPointer(wrs.getDoubleReg(activeIndexRegister));
			break;
		}// switch opCode1

		return instructionSize;
	}// opCodePageDD

	private byte[] getValue(int workingValue) {
		byte msb = (byte) ((workingValue & 0XFF00) >> 8);
		byte lsb = (byte) ((byte) workingValue & 0X00FF);
		return new byte[] { lsb, msb };
	}// getValue

	private int opCodeSetIndexRegistersBit() {
		int instructionSize = 4;
		Z80.Register activeIndexRegister = cpuBuss.read(instructionBase) == (byte) 0XDD ? Z80.Register.IX
				: Z80.Register.IY;

		int indexRegisterContents = wrs.getDoubleReg(activeIndexRegister);
		int indexDisplacement = cpuBuss.read(instructionBase + 2);
		int netLocation = (indexRegisterContents + indexDisplacement) & Z80.WORD_MASK;

		byte result = 0x00;
		int page = getPage(instructionBase + 3);
		byte sourceByte = cpuBuss.read(netLocation);

		switch (page) {
		case 0: // Page 00 RLC RRC RL RR SLA SRA SLL SRL --- (IXY+d)
			byte opCode3 = cpuBuss.read(instructionBase + 3);

			switch (opCode3) {
			case 0x06: // RLC (IXY+d)
				result = au.rotateLeft(sourceByte);
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

			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(false);
			ccr.setPvFlag(au.isParityFlagSet());
			ccr.setNFlag(false);
			ccr.setCarryFlag(au.isCarryFlagSet());

			break; // case 0 page 0
		case 1: // Page 01 BIT b,(IXY+d)
			int bit = (cpuBuss.read(instructionBase + 3) >> 3) & 0b0000_0111;
			au.bitTest(sourceByte, bit);
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(true);
			ccr.setNFlag(false);
			break;
		case 2: // Page 10 RES b,(IXY+d)
			bit = (cpuBuss.read(instructionBase + 3) >> 3) & 0b0000_0111;
			result = au.bitRes(sourceByte, bit);
			cpuBuss.write(netLocation, result);
			break;
		case 3: // Page 11 SET b,(IXY+d)
			bit = (cpuBuss.read(instructionBase + 3) >> 3) & 0b0000_0111;
			result = au.bitSet(sourceByte, bit);
			cpuBuss.write(netLocation, result);

			break;

		}// switch instruction Page
		return instructionSize;
	}// opCodePageDD

	private int opCodeSetMain() {
		int instructionSize = 0;

		byte opCode = cpuBuss.read(instructionBase);
		int page = (opCode >> 6) & 0b000_0011; // only want the value of bits 6 & 7

		switch (page) {
		case 0:
			instructionSize = opCodePage00();
			break;
		case 1:
			instructionSize = opCodePage01();
			break;
		case 2:
			instructionSize = opCodePage10();
			break;
		case 3:
			instructionSize = opCodePage11();
			break;
		default:
			reportOpCodeError();
		}// Switch page
		return instructionSize;
	}// opCodePageSingle
		// --------------------------------------------------------------------------------------------------------

	private int opCodePage00() {
		int instructionSize = 0;
		int yyy = getYYY(instructionBase);
		int zzz = getZZZ(instructionBase);

		byte source, ans;

		switch (zzz) {// 00 YYY ZZZ
		case 0: // NOP DJNZ e
			byte immediateByte = cpuBuss.read(instructionBase + 1);

			switch (yyy) {
			case 0: // NOP - 00
				instructionSize = 1;
				break;
			case 1: // EX AF,AF' - 08
				instructionSize = 1;
				wrs.swapAF();
				break;
			case 2: // DJNZ e - 10
				source = wrs.getReg(Register.B);
				ans = au.decrement(source);
				wrs.setReg(Register.B, ans);
				if (au.isZeroFlagSet()) {
					instructionSize = 0;
					opCode_Jump(instructionBase + immediateByte + 2);
				} else {
					instructionSize = 2;
				} // if
				break;
			case 3: // JR e - 18
				instructionSize = 0;
				opCode_Jump(instructionBase + immediateByte + 2);
				break;
			case 4: // JR NZ,e - 20
				if (isConditionTrue(ConditionCode.NZ)) {
					instructionSize = 0;
					opCode_Jump(instructionBase + immediateByte + 2);
				} else {
					instructionSize = 2;
				} // if
				break;
			case 5: // JR Z,e - 28
				if (isConditionTrue(ConditionCode.Z)) {
					instructionSize = 0;
					opCode_Jump(instructionBase + immediateByte + 2);
				} else {
					instructionSize = 2;
				} // if
				break;
			case 6: // JR NC,e - 30
				if (isConditionTrue(ConditionCode.NC)) {
					instructionSize = 0;
					opCode_Jump(instructionBase + immediateByte + 2);
				} else {
					instructionSize = 2;
				} // if
				break;
			case 7: // JR C,e - 38
				if (isConditionTrue(ConditionCode.C)) {
					instructionSize = 0;
					opCode_Jump(instructionBase + immediateByte + 2);
				} else {
					instructionSize = 2;
				} // if
				break;

			}// switch yyy

			break;
		case 1: // LD rr,dd / ADD HL,rr
			if (isBit3Set(instructionBase)) {// ADD HL,rr - 09,19,29,39
				instructionSize = 1;
				byte[] destinationValueArray = wrs.getDoubleRegArray(Register.HL);
				byte[] sourceValueArray = wrs.getDoubleRegArray(getDoubleRegister1_45(instructionBase));
				byte[] ansValueArray = au.addWord(destinationValueArray, sourceValueArray);
				wrs.setDoubleReg(Register.HL, ansValueArray);
			} else { // LD rr,dd - 01,11,21,31
				instructionSize = 3;
				wrs.setDoubleReg(getDoubleRegister1_45(instructionBase), getImmediateWord(instructionBase + 1));
			} // if bit 3
			break;
		case 2: // 02,0A,12,1A,22,2A,32,3A
			// LD (BC),A LD (DE),A LD (DE),A LD (nn),A
			// LD A,(BC) LD (DE),A LD HL,(nn) LD A,(nn)
			switch (yyy) {
			case 0: // 02 - LD (BC),A
			case 2: // 12 - LD (DE),A
				source = wrs.getAcc();
				int destinationLocation = wrs.getDoubleReg(getDoubleRegister1_45(instructionBase));
				cpuBuss.write(destinationLocation, source);
				instructionSize = 1;
				break;
			case 1: // 0A - LD A,(BC)
			case 3: // 1A - LD A,(DE)
				int sourceLocation = wrs.getDoubleReg(getDoubleRegister1_45(instructionBase));
				wrs.setAcc(cpuBuss.read(sourceLocation));
				instructionSize = 1;
				break;
			case 4: // 22 - LD (nn),HL
				instructionSize = 3;
				int directAddress = cpuBuss.readWordReversed(instructionBase + 1);
				byte[] valueArray = wrs.getDoubleRegArray(Register.HL);
				cpuBuss.write(directAddress, valueArray[0]);
				cpuBuss.write(directAddress + 1, valueArray[1]);
				break;
			case 5: // 2A - LD HL,(nn)
				instructionSize = 3;
				directAddress = cpuBuss.readWordReversed(instructionBase + 1);
				wrs.setDoubleReg(Register.HL, cpuBuss.read(directAddress+1), cpuBuss.read(directAddress ));
				break;
			case 6: // 32 - LD (nn),A
				instructionSize = 3;
				cpuBuss.write(getImmediateWord(instructionBase + 1), wrs.getAcc());
				break;
			case 7: // 3A - LD A,(nn)
				instructionSize = 3;
				wrs.setAcc(cpuBuss.read(getImmediateWord(instructionBase + 1)));
				break;
			}// switch yyy
			break;

		case 3: // INC rr / DEC rr - 03,13,23,33/0B,1B,2B,3B
			instructionSize = 1;
			byte conditionCodes = ccr.getConditionCode();
			byte[] sourceValueArray = wrs.getDoubleRegArray(getDoubleRegister1_45(instructionBase));
			if (isBit3Set(instructionBase)) {// DEC,rr - 0B,1B,2B,3B
				wrs.setDoubleReg(getDoubleRegister1_45(instructionBase), au.decrementWord(sourceValueArray));
			} else { // INC rr - 03,13,23,33
				wrs.setDoubleReg(getDoubleRegister1_45(instructionBase), au.incrementWord(sourceValueArray));
			} // if bit 3
			ccr.setConditionCode(conditionCodes);
			break;
		case 4: // INC r - 04,0C,14,1C,24,2C,34,3C
			instructionSize = 1;
			source = wrs.getReg(getSingleRegister345(instructionBase));
			ans = au.increment(source);
			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(au.isHCarryFlagSet());
			ccr.setPvFlag(source == (byte) 0x7F ? true : false);
			ccr.setNFlag(false);

			wrs.setReg(getSingleRegister345(instructionBase), ans);
			break;
		case 5: // DEC r - 05,0D,15,1D,25,2D,35,3D
			instructionSize = 1;
			source = wrs.getReg(getSingleRegister345(instructionBase));
			ans = au.decrement(source);
			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(au.isHCarryFlagSet());
			ccr.setPvFlag(source == (byte) 0x80 ? true : false);
			ccr.setNFlag(true);

			wrs.setReg(getSingleRegister345(instructionBase), ans);
			break;
		case 6: // LD r,d - 06,0E,16,1E,26,2E,36,3E
			instructionSize = 2;
			Register destination = getSingleRegister345(instructionBase);
			if (destination.equals(Register.M)) {
				int indirectAddress = wrs.getDoubleReg(Register.M);
				cpuBuss.write(indirectAddress, getImmediateByte(instructionBase + 1));
			} else {
				wrs.setReg(destination, getImmediateByte(instructionBase + 1));
			} //
			break;
		case 7: // RLCA RRCA RLA RRA DAA CPL SCF CCF - 07,0F,17,1F,27,2F,37,3F
			instructionSize = 1;
			switch (yyy) {
			case 0: // RLCA - 07
				wrs.setAcc(au.rotateLeft(wrs.getAcc()));
				ccr.setHFlag(false);
				ccr.setNFlag(false);
				ccr.setCarryFlag(au.isCarryFlagSet());
				break;
			case 1: // RRCA - 0F
				wrs.setAcc(au.rotateRight(wrs.getAcc()));
				ccr.setHFlag(false);
				ccr.setNFlag(false);
				ccr.setCarryFlag(au.isCarryFlagSet());
				break;
			case 2: // RLA - 17
				wrs.setAcc(au.rotateLeftThru(wrs.getAcc(), ccr.isCarryFlagSet()));
				ccr.setHFlag(false);
				ccr.setNFlag(false);
				ccr.setCarryFlag(au.isCarryFlagSet());
				break;
			case 3: // RRA - 1F
				wrs.setAcc(au.rotateRightThru(wrs.getAcc(), ccr.isCarryFlagSet()));
				ccr.setHFlag(false);
				ccr.setNFlag(false);
				ccr.setCarryFlag(au.isCarryFlagSet());
				break;
			case 4: // DAA - 27
				ans = au.daa(wrs.getAcc(), ccr.isNFlagSet(), ccr.isCarryFlagSet(), ccr.isHFlagSet());
				ccr.setSignFlag(au.isSignFlagSet());
				ccr.setZeroFlag(au.isZeroFlagSet());
				ccr.setHFlag(au.isHCarryFlagSet());
				ccr.setPvFlag(au.isParityFlagSet());
				ccr.setCarryFlag(au.isCarryFlagSet());
				wrs.setAcc(ans);
				break;
			case 5: // CPL - 2F
				ans = au.complement(wrs.getAcc());
				ccr.setHFlag(true);
				ccr.setNFlag(true);
				wrs.setAcc(ans);
				break;
			case 6: // SCF - 37
				ccr.setHFlag(false);
				ccr.setNFlag(false);
				ccr.setCarryFlag(true);
				break;
			case 7: // CCF - 3F
				ccr.setHFlag(ccr.isCarryFlagSet());// previous carry
				ccr.setCarryFlag(!ccr.isHFlagSet());
				ccr.setNFlag(false);
				break;
			}// switch yyy
			break;

		}// switch zzz
		return instructionSize;
		//
	}// opCodePage00

	private int opCodePage01() {
		int instructionSize = 1;
		if (cpuBuss.read(instructionBase) == (byte) 0X76) {// HALT
			instructionSize = 0;
			setError(ErrorStatus.HALT_INSTRUCTION);
		} else {// LD r, r1
			wrs.setReg(getSingleRegister345(instructionBase), wrs.getReg(getSingleRegister012(instructionBase)));
		} // if halt
		return instructionSize;
	}// opCodePage01

	private int opCodePage10() {
		int instructionSize = 1;
		int yyy = getYYY(instructionBase);

		byte accValue = wrs.getAcc();
		byte regValue = wrs.getReg(getSingleRegister012(instructionBase));
		byte result;
		switch (yyy) {
		case 0: // ADD A,r - 80,81,82,83,84,85,86,87
			result = au.add(accValue, regValue);
			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(au.isHCarryFlagSet());
			ccr.setPvFlag(au.isOverflowFlagSet());
			ccr.setNFlag(false);
			ccr.setCarryFlag(au.isCarryFlagSet());
			wrs.setAcc(result);
			break;
		case 1: // ADC A,r - 88,89,8A,8B,8C,8D,8E,8E,8F
			result = au.addWithCarry(accValue, regValue, ccr.isCarryFlagSet());
			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(au.isHCarryFlagSet());
			ccr.setPvFlag(au.isOverflowFlagSet());
			ccr.setNFlag(false);
			ccr.setCarryFlag(au.isCarryFlagSet());
			wrs.setAcc(result);
			break;
		case 2: // SUB r - 90,91,92,93,94,95,96,97
			result = au.sub(accValue, regValue);
			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(au.isHCarryFlagSet());
			ccr.setPvFlag(au.isOverflowFlagSet());
			ccr.setNFlag(true);
			ccr.setCarryFlag(au.isCarryFlagSet());
			wrs.setAcc(result);
			break;
		case 3: // SBC A,r - 98,99,9A,9B,9C,9D,9E,9F
			result = au.subWithCarry(accValue, regValue, ccr.isCarryFlagSet());
			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(au.isHCarryFlagSet());
			ccr.setPvFlag(au.isOverflowFlagSet());
			ccr.setNFlag(true);
			ccr.setCarryFlag(au.isCarryFlagSet());
			wrs.setAcc(result);
			break;
		case 4: // AND r - A0,A1,A2,A3,A4,A5,A6,A7
			result = au.and(accValue, regValue);
			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(true);
			ccr.setPvFlag(au.isParityFlagSet());
			ccr.setNFlag(false);
			ccr.setCarryFlag(false);
			wrs.setAcc(result);
			break;
		case 5: // XOR r - A8,A9,AA,AB,AC,AD,AE,AF
			result = au.xor(accValue, regValue);
			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(false);
			ccr.setPvFlag(au.isParityFlagSet());
			ccr.setNFlag(false);
			ccr.setCarryFlag(false);
			wrs.setAcc(result);
			break;
		case 6: // OR r - B0,B1,B2,B3,B4,B5,B6,B7
			result = au.or(accValue, regValue);
			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(false);
			ccr.setPvFlag(au.isParityFlagSet());
			ccr.setNFlag(false);
			ccr.setCarryFlag(false);
			wrs.setAcc(result);
			break;
		case 7: // CP r - B8,B9,BA,BB,BC,BD,BE,BF
			au.compare(accValue, regValue);
			ccr.setSignFlag(au.isSignFlagSet());
			ccr.setZeroFlag(au.isZeroFlagSet());
			ccr.setHFlag(au.isHCarryFlagSet());
			ccr.setPvFlag(au.isOverflowFlagSet());
			ccr.setNFlag(true);
			ccr.setCarryFlag(au.isCarryFlagSet());
			break;
		}// switch yyy

		return instructionSize;
	}// opCodePage10

	private int opCodePage11() {
		int instructionSize = 0;
		int yyy = getYYY(instructionBase);
		int zzz = getZZZ(instructionBase);

		int directAddress = cpuBuss.readWordReversed(instructionBase + 1);

		switch (zzz) {
		case 0: // Conditional Returns C0,C8,D0,D8.E0.E8.F0.F8
			// RNZ, RZ, RNC, RC,RPO,RPE,RP,RM
			if (isConditionTrue(getConditionCode(instructionBase))) {
				instructionSize = 0;
				opCode_Return();
			} else {
				instructionSize = 1;
			} // if
			break;
		case 1: // POP rr RET JP (HL) LD SP,HL
			instructionSize = 1;
			if (!isBit3Set(instructionBase)) {// POP rr - C1,D1,E1,F1
				byte[] regValue = this.opCode_Pop();
				wrs.setDoubleReg(getDoubleRegister2_45(instructionBase), regValue);
			} else {// RET EXX JP (HL) LD SP,HL
				switch (yyy) {
				case 1: // RET - C9
					instructionSize = 0;
					opCode_Return();
					break;
				case 3: // EXX - D9
					wrs.swapMainRegisters();
					break;
				case 5: // JP (HL) - E9
					instructionSize = 0;
					wrs.setProgramCounter(wrs.getDoubleReg(Register.HL));
					break;
				case 7: // LD SP,HL - F9
					wrs.setStackPointer(wrs.getDoubleReg(Register.HL));
					break;
				}// switch yyy
			} // if bit 3
			break;
		case 2: // Conditional Jumps - C2,CA,D2,DA,E2,EA,F2,FA
			if (isConditionTrue(getConditionCode(instructionBase))) {
				opCode_Jump(directAddress);
				instructionSize = 0;
			} else {
				instructionSize = 3;
			} // if
			break;
		case 3: // JP nn OUT (n),A IN A,(n) EX (SP),HL EX DE,HL DI EI
			switch (yyy) {

			case 0: // JP nn - C3
				instructionSize = 0;
				opCode_Jump(directAddress);
				break;
			case 1: // CB BITS - CB
				// Extended Code
				// DO OPCODE elsewhere
				break;
			case 2: // OUT (n),A - D3
				instructionSize = 2;
				Byte IOaddress = cpuBuss.read(wrs.getProgramCounter() + 1);
				ioc.byteFromCPU(IOaddress, wrs.getReg(Register.A));
				break;
			case 3: // IN A,(n) - DB
				instructionSize = 2;
				IOaddress = cpuBuss.read(wrs.getProgramCounter() + 1);
					wrs.setReg(Register.A, ioc.byteToCPU(IOaddress));
//				try {
//				} catch (IOException e) {
//					log.errorf("Bad IO read from %02XH%n", IOaddress);
//					System.err.print(e.toString());
//				} // try

				break;
			case 4: // EX (SP),HL - E3
				instructionSize = 1;
				int sp = wrs.getStackPointer();
				byte hi = wrs.getReg(Register.H);
				byte lo = wrs.getReg(Register.L);
				wrs.setReg(Register.L, cpuBuss.read(sp));
				wrs.setReg(Register.H, cpuBuss.read(sp + 1));
				cpuBuss.write(sp, lo);
				cpuBuss.write(sp + 1, hi);
				break;
			case 5: // EX DE,HL - EB
				instructionSize = 1;
				int deBefore = wrs.getDoubleReg(Register.DE);
				wrs.setDoubleReg(Register.DE, wrs.getDoubleReg(Register.HL));
				wrs.setDoubleReg(Register.HL, deBefore);
				break;
			case 6: // DI - F3
				instructionSize = 1;
				wrs.setIFF1(false);
				wrs.setIFF2(false);
				break;
			case 7: // EI - FB
				instructionSize = 1;
				wrs.setIFF1(true);
				wrs.setIFF2(true);
				break;
			}// switch yyy

			break;
		case 4: // Conditional Calls - C4,CC,D4,DC,E4,EC,F4,FC
			if (isConditionTrue(getConditionCode(instructionBase))) {
				opCode_Call();
				instructionSize = 0;
			} else {
				instructionSize = 3;
			} // if
			break;
		case 5: // PUSH rr CALL nn
			if (!isBit3Set(instructionBase)) {// PUSH rr C5,D5,E5,F5
				instructionSize = 1;
				this.opCode_Push(wrs.getDoubleReg(getDoubleRegister2_45(instructionBase)));
			} else {// RET EXX JP (HL) LD SP,HL
				switch (yyy) {
				case 1: // CALL nn - CD
					instructionSize = 0;
					opCode_Call();
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
			byte result;
			byte accValue = wrs.getAcc();
			/// byte immediateByte = cpuBuss.read(instructionBase + 1);

			switch (yyy) {
			case 0: // ADD A,n - C6
				result = au.add(accValue, getImmediateByte(instructionBase + 1));
				ccr.setSignFlag(au.isSignFlagSet());
				ccr.setZeroFlag(au.isZeroFlagSet());
				ccr.setHFlag(au.isHCarryFlagSet());
				ccr.setPvFlag(au.isOverflowFlagSet());
				ccr.setNFlag(false);
				ccr.setCarryFlag(au.isCarryFlagSet());
				wrs.setAcc(result);
				break;
			case 1: // ADC A,n - CE
				result = au.addWithCarry(accValue, getImmediateByte(instructionBase + 1), ccr.isCarryFlagSet());
				ccr.setSignFlag(au.isSignFlagSet());
				ccr.setZeroFlag(au.isZeroFlagSet());
				ccr.setHFlag(au.isHCarryFlagSet());
				ccr.setPvFlag(au.isOverflowFlagSet());
				ccr.setNFlag(false);
				ccr.setCarryFlag(au.isCarryFlagSet());
				wrs.setAcc(result);
				break;
			case 2: // SUB n - D6
				result = au.sub(accValue, getImmediateByte(instructionBase + 1));
				ccr.setSignFlag(au.isSignFlagSet());
				ccr.setZeroFlag(au.isZeroFlagSet());
				ccr.setHFlag(au.isHCarryFlagSet());
				ccr.setPvFlag(au.isOverflowFlagSet());
				ccr.setNFlag(true);
				ccr.setCarryFlag(au.isCarryFlagSet());
				wrs.setAcc(result);
				break;
			case 3: // SBC A,n - DE
				result = au.subWithCarry(accValue, getImmediateByte(instructionBase + 1), ccr.isCarryFlagSet());
				ccr.setSignFlag(au.isSignFlagSet());
				ccr.setZeroFlag(au.isZeroFlagSet());
				ccr.setHFlag(au.isHCarryFlagSet());
				ccr.setPvFlag(au.isOverflowFlagSet());
				ccr.setNFlag(true);
				ccr.setCarryFlag(au.isCarryFlagSet());
				wrs.setAcc(result);
				break;
			case 4: // AND n - E6
				result = au.and(accValue, getImmediateByte(instructionBase + 1));
				ccr.setSignFlag(au.isSignFlagSet());
				ccr.setZeroFlag(au.isZeroFlagSet());
				ccr.setHFlag(true);
				ccr.setPvFlag(au.isParityFlagSet());
				ccr.setNFlag(false);
				ccr.setCarryFlag(false);
				wrs.setAcc(result);
				break;
			case 5: // XOR n - EE
				result = au.xor(accValue, getImmediateByte(instructionBase + 1));
				ccr.setSignFlag(au.isSignFlagSet());
				ccr.setZeroFlag(au.isZeroFlagSet());
				ccr.setHFlag(false);
				ccr.setPvFlag(au.isParityFlagSet());
				ccr.setNFlag(false);
				ccr.setCarryFlag(false);
				wrs.setAcc(result);
				break;
			case 6: // OR n - F6
				result = au.or(accValue, getImmediateByte(instructionBase + 1));
				ccr.setSignFlag(au.isSignFlagSet());
				ccr.setZeroFlag(au.isZeroFlagSet());
				ccr.setHFlag(false);
				ccr.setPvFlag(au.isParityFlagSet());
				ccr.setNFlag(false);
				ccr.setCarryFlag(false);
				wrs.setAcc(result);
				break;
			case 7: // CP n - FE
				au.compare(accValue, getImmediateByte(instructionBase + 1));
				ccr.setSignFlag(au.isSignFlagSet());
				ccr.setZeroFlag(au.isZeroFlagSet());
				ccr.setHFlag(au.isHCarryFlagSet());
				ccr.setPvFlag(au.isOverflowFlagSet());
				ccr.setNFlag(true);
				ccr.setCarryFlag(au.isCarryFlagSet());
				break;
			}// switch yyy

			break;
		case 7: // RST - C7,CF,D7,DF,E7,EF,F7,F8
			instructionSize = 0;
			int PCvalue = wrs.getProgramCounter() + 1;
			opCode_Push(PCvalue);

			PCvalue = (cpuBuss.read(instructionBase) & 0b0011_1000);

			wrs.setProgramCounter(PCvalue);
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

	}// opCode_Push

	private void opCode_Jump(int targetLocation) {
		wrs.setProgramCounter(targetLocation);
	}// opCode_Jump

	private void opCode_Call() {
		int currentProgramCounter = wrs.getProgramCounter();
		opCode_Push(currentProgramCounter + 3);
		int memoryLocation = cpuBuss.readWordReversed(currentProgramCounter + 1);
		wrs.setProgramCounter(memoryLocation);
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
	private boolean isConditionTrue(ConditionCode condition) {
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
			String message = String.format("[cpu] isConditionTrue() - bad condition: %s", condition);
			log.error(message);
			setError(ErrorStatus.INVALID_CONDITION_CODE);
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
	}// ldd

	private void incrementDoubleRegister(Register reg) {
		byte[] values = au.incrementWord(wrs.getDoubleRegArray(reg));
		wrs.setDoubleReg(reg, values[1], values[0]);
	}// incrementDoubleRegister

	private boolean decrementDoubleRegister(Register reg) {
		byte[] values = au.decrementWord(wrs.getDoubleRegArray(reg));
		wrs.setDoubleReg(reg, values);
		return au.isZeroFlagSet();
	}// incrementDoubleRegister

	// --------------------------------------------------------------------------------------------------------

	// private byte[] splitWord(int wordValue) {
	// return new byte[] { (byte) ((wordValue >> 8) & 0XFF), (byte) (wordValue & 0XFF) };
	// }// split word
	//
	// private byte[] splitWordReverse(int wordValue) {
	// return new byte[] { (byte) (wordValue & 0XFF), (byte) ((wordValue >> 8) & 0XFF) };
	// }// split word
	//
	/**
	 * Retrieves an error of ErrorStatus
	 * 
	 * @return error type of error
	 */
	public ErrorStatus getError() {
		return errorCurrent;
	}// setErrorFlag

	/**
	 * records an error of ErrorStatus
	 * 
	 * @param error
	 *            type of error to record
	 */
	public static void setError(ErrorStatus error) {
		errorCurrent = error;
	}// setErrorFlag

	/**
	 * indicated if an error is recorded
	 * 
	 * @return false if no error, else true
	 */
	public boolean isError() {
		return errorCurrent.equals(ErrorStatus.NONE) ? false : true;
	}// isError

	private void reportOpCodeError() {
		String message = String.format("[cpu] Bad opCode: %02X,Location: %04X", cpuBuss.read(instructionBase),
				instructionBase);
		log.error(message);
		setError(ErrorStatus.INVALID_OPCODE);
	}// reportOpCodeError

	//////////////////////////////

	private int getPage(int location) {
		byte source = cpuBuss.read(location);
		return (source & Z80.MASK_PAGE) >> 6;
	}// getZZZ

	private int getYYY(int location) {
		byte source = cpuBuss.read(location);
		return (source & Z80.MASK_YYY) >> 3;
	}// getZZZ

	private int getZZZ(int location) {
		byte source = cpuBuss.read(location);
		return source & Z80.MASK_ZZZ;
	}// getZZZ

	private int getImmediateWord(int location) {
		return cpuBuss.readWordReversed(location);
	}// getImmediateWord

	private byte getImmediateByte(int location) {
		return cpuBuss.read(location);
	}// getImmediateByte

	private Register getDoubleRegister1_45(int location) {
		byte source = cpuBuss.read(location);
		int index = (source & Z80.MASK_DOUBLE_REGISTER45) >> 4;
		return Z80.doubleRegisters1[index];
	}// getDoubleRegister1_45

	private Register getDoubleRegister2_45(int location) {
		byte source = cpuBuss.read(location);
		int index = (source & Z80.MASK_DOUBLE_REGISTER45) >> 4;
		return Z80.doubleRegisters2[index];
	}// getDoubleRegister2_45

	private Register getSingleRegister012(int location) {
		byte source = cpuBuss.read(location);
		int index = source & Z80.MASK_REGISTER_123;
		return Z80.singleRegisters[index];
	}// getSingleRegister012

	private Register getSingleRegister345(int location) {
		byte source = cpuBuss.read(location);
		int index = (source & Z80.MASK_REGISTER_345) >> 3;
		return Z80.singleRegisters[index];
	}// getSingleRegister345

	private boolean isBit3Set(int location) {
		return (cpuBuss.read(location) & Z80.BIT_3) == (Z80.BIT_3);
	}// isBit3Set

	private ConditionCode getConditionCode(int location) {
		byte source = cpuBuss.read(location);
		return Z80.conditionCode[(source & Z80.MASK_CONDITION_CODE) >> 3];
	}

}// class CentralProcessingUnit
