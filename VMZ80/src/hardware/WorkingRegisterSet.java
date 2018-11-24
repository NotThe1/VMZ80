package hardware;

import java.util.HashMap;

import codeSupport.AppLogger;
import codeSupport.Z80;
import codeSupport.Z80.Register;
import memory.IoBuss;

/**
 * 
 * @author Frank Martyn
 * @version 1.0
 * 
 *          Contains the information about all the registers in the machine. Has both the data registers as well as
 *          Program Counter and Stack pointer.
 *          <p>
 *          This class is a singleton
 *
 */
public class WorkingRegisterSet {

	private static WorkingRegisterSet instance = new WorkingRegisterSet();

	private int programCounter;
	private int stackPointer;
	private int IX;
	private int IY;

	private boolean iff1 = false;
	private boolean iff2 = false;
	private HashMap<Register, Byte> registers;

	private AppLogger log = AppLogger.getInstance();

	public static WorkingRegisterSet getInstance() {

		return instance;
	}// getWorkingRegisterSet

	/**
	 * Sets up the Program Counter, Stack Pointer and establishes the data registers based on the class - Register. It
	 * determines the register names and count.
	 */
	private WorkingRegisterSet() {
		registers = new HashMap<Register, Byte>();
		initialize();
	}// Constructor
		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	public  void initialize() {
		registers.clear();
		Byte value = 0X00;
		for (Register register : Register.values()) {
			registers.put(register, value);
		} // for
		programCounter = 0X0000;
		stackPointer = 0X0100; // set to non zero
		IX = 0X0000;
		IY = 0X0000;
		iff1 = false;
		iff2 = false;
	}// initialize

	public void setDoubleReg(Register reg, int value) {
		byte[] registerValues = splitWord(value);
		setDoubleReg(reg, registerValues[1], registerValues[0]);
	}// setDoubleReg

	public void setDoubleReg(Register reg, byte[] registerValues) {
		setDoubleReg(reg, registerValues[1], registerValues[0]);
		return;
	}// setDoubleReg

	public void setDoubleReg(Register reg, byte hi, byte lo) {
		switch (reg) {
		case AF:
			setReg(Register.A, hi);
			setReg(Register.F, lo);
			break;
		case BC:
			setReg(Register.B, hi);
			setReg(Register.C, lo);
			break;
		case DE:
			setReg(Register.D, hi);
			setReg(Register.E, lo);
			break;
		case HL:
		case M:
			setReg(Register.H, hi);
			setReg(Register.L, lo);
			break;
		case SP:
			setStackPointer(hi, lo);
			break;
		case PC:
			setProgramCounter(hi, lo);
			break;
		case IX:
			setIX(hi, lo);
			break;
		case IY:
			setIY(hi, lo);
			break;
		default:
			String message = String.format("[wrs] setDoubleReg() - bad register %s", reg);
			log.error(message);
			CentralProcessingUnit.setRunning(false);
			CentralProcessingUnit.setError(ErrorStatus.WORKING_REGISTER_SET_ERROR);
		}// switch
	}// setDoubleReg

	public int getDoubleReg(Register reg) {
		int result = 0;

		switch (reg) {
		case AF:
			result = (((getReg(Register.A) << 8) + (getReg(Register.F) & Z80.BYTE_MASK)) & Z80.WORD_MASK);
			break;
		case BC:
			result = (((getReg(Register.B) << 8) + (getReg(Register.C) & Z80.BYTE_MASK)) & Z80.WORD_MASK);
			break;
		case DE:
			result = (((getReg(Register.D) << 8) + (getReg(Register.E) & Z80.BYTE_MASK)) & Z80.WORD_MASK);
			break;
		case HL:
		case M:
			result = (((getReg(Register.H) << 8) + (getReg(Register.L) & Z80.BYTE_MASK)) & Z80.WORD_MASK);
			break;
		case SP:
			result = this.getStackPointer();
			break;
		case PC:
			result = this.getProgramCounter();
			break;
		case IX:
			result = this.getIX();
			break;
		case IY:
			result = this.getIY();
			break;
		default:
			log.errorf("[workingRegisterSet] %n getDoubleReg-%s at location %04X,", reg,
					this.programCounter);
			CentralProcessingUnit.setRunning(false);
			CentralProcessingUnit.setError(ErrorStatus.WORKING_REGISTER_SET_ERROR);
		}// switch

		return result;
	}// getDoubleReg

	// return [0] = lsb , [1] = msb
	public byte[] getDoubleRegArray(Register reg) {
		byte[] ans = new byte[2];

		switch (reg) {
		case AF:
			ans[1] = this.getReg(Register.A);
			ans[0] = this.getReg(Register.F);
			break;
		case BC:
			ans[1] = this.getReg(Register.B);
			ans[0] = this.getReg(Register.C);
			break;
		case DE:
			ans[1] = this.getReg(Register.D);
			ans[0] = this.getReg(Register.E);
			break;
		case HL:
		case M:
			ans[1] = this.getReg(Register.H);
			ans[0] = this.getReg(Register.L);
			break;
		case SP:
			ans = splitWord(this.getStackPointer());
			break;
		case PC:
			ans = splitWord(this.getProgramCounter());
			break;
		case IX:
			ans = splitWord(this.getIX());
			break;
		case IY:
			ans = splitWord(this.getIY());
			break;
		default:
			String message = String.format("[wrs] getDoubleRegArray() - bad register %s", reg);
			log.error(message);
			CentralProcessingUnit.setRunning(false);
			CentralProcessingUnit.setError(ErrorStatus.WORKING_REGISTER_SET_ERROR);
		}// switch

		return ans;
	}// getDoubleReg

	public void setProgramCounter(int programCounter) {
		this.programCounter = programCounter & Z80.WORD_MASK;
	}// setProgramCounter

	public void setProgramCounter(byte[] programCounterValue) {
		int hi = (int) (programCounterValue[1] << 8);
		int lo = (int) (programCounterValue[0] & 0X00FF);
		setProgramCounter((hi + lo) & Z80.WORD_MASK);
	}// setProgramCounter

	public void setProgramCounter(byte hiByte, byte loByte) {
		int hi = (int) (hiByte << 8);
		int lo = (int) (loByte & 0X00FF);
		setProgramCounter((hi + lo) & Z80.WORD_MASK);
	}// setStackPointer

	public void incrementProgramCounter(int delta) {// setProgramCounter
		setProgramCounter((this.programCounter + delta & Z80.WORD_MASK));
	}// incrementProgramCounter

	public int getProgramCounter() {
		return this.programCounter;
	}// getProgramCounter

	public byte[] getProgramCounterArray() {
		return splitWord(this.programCounter);
	}// getProgramCounterArray

	public void setStackPointer(int stackPointerValue) {
		this.stackPointer = stackPointerValue & Z80.WORD_MASK;
	}// setStackPointer

	public void setStackPointer(byte[] stackPointerValue) {
		int hi = (int) (stackPointerValue[1] << 8);
		int lo = (int) (stackPointerValue[0] & 0X00FF);
		setStackPointer((hi + lo) & Z80.WORD_MASK);
	}// setStackPointer

	public void setStackPointer(byte hiByte, byte loByte) {
		int hi = (int) (hiByte << 8);
		int lo = (int) (loByte & 0X00FF);
		setStackPointer((hi + lo) & Z80.WORD_MASK);

	}// setStackPointer

	public int getStackPointer() {
		return this.stackPointer;
	}// getStackPointer

	public byte[] getStackPointerArray() {
		return splitWord(this.stackPointer);
	}// getStackPointerarray

	public void setIX(int ixValue) {
		this.IX = ixValue & Z80.WORD_MASK;
	}// setIX

	public void setIX(byte[] IXValue) {
		int hi = (int) (IXValue[1] << 8);
		int lo = (int) (IXValue[0] & 0X00FF);
		setIX((hi + lo) & Z80.WORD_MASK);
	}// setIX

	public void setIX(byte hiByte, byte loByte) {
		int hi = (int) (hiByte << 8);
		int lo = (int) (loByte & 0X00FF);
		this.IX = (hi + lo) & Z80.WORD_MASK;
	}// setIX

	public int getIX() {
		return this.IX;
	}// getIX

	public byte[] getIXarray() {
		return splitWord(this.IX);
	}// getIXarray

	public void setIY(int IYValue) {
		this.IY = IYValue & Z80.WORD_MASK;
	}// setIY

	public void setIY(byte[] IYValue) {
		int hi = (int) (IYValue[1] << 8);
		int lo = (int) (IYValue[0] & 0X00FF);
		setIY((hi + lo) & Z80.WORD_MASK);
	}// setIX

	public void setIY(byte hiByte, byte loByte) {
		int hi = (int) (hiByte << 8);
		int lo = (int) (loByte & 0X00FF);
		setIY((hi + lo) & Z80.WORD_MASK);
	}// setIY

	public int getIY() {
		return this.IY;
	}// getIY

	public byte[] getIYarray() {
		return splitWord(this.IY);
	}// getIYarray

	public boolean isIFF1Set() {
		return iff1;
	}// isIFF1Set

	public boolean isIFF2Set() {
		return iff2;
	}// isIFF2Set

	// public void setStackPointer(byte hiByte, byte loByte) {
	// int hi = (int) (hiByte << 8);
	// int lo = (int) (loByte & 0X00FF);
	// this.stackPointer = (hi + lo) & Z80.WORD_MASK;
	// }// setIX

	public void setIFF1(boolean state) {
		iff1 = state;
	}// setIFF1

	public void setIFF2(boolean state) {
		iff2 = state;
	}// setIFF2

	public void setAcc(byte value) {
		registers.put(Register.A, value);
	}// loadReg

	public byte getAcc() {
		return registers.get(Register.A);
	}// getReg

	public void setReg(Register reg, byte value) {
		switch (reg) {
		case F:
			ConditionCodeRegister.getInstance().setConditionCode(value);
			break;
		case M:
			IoBuss.getInstance().write(this.getDoubleReg(Register.HL), value);
			break;
		default:
			registers.put(reg, value);
		}// switch

	}// loadReg

	public byte getReg(Register reg) {
		byte ans;
		switch (reg) {
		case F:
			ans = ConditionCodeRegister.getInstance().getConditionCode();
			break;
		case M:
			ans = IoBuss.getInstance().read(this.getDoubleReg(Register.HL));
			break;
		default:
			ans = registers.get(reg);
		}// switch
		return ans;
	}// getReg

	// return [0] = lsb , [1] = msb
	private byte[] splitWord(int wordValue) {
		return new byte[] { (byte) (wordValue & 0XFF), (byte) ((wordValue >> 8) & 0XFF) };
	}// split word

	public void swapAF() {
		swap1Reg(Register.A, Register.Ap);

		byte temp = getReg(Register.Fp);
		setReg(Register.Fp, ConditionCodeRegister.getInstance().getConditionCode());
		setReg(Register.F, temp);
		ConditionCodeRegister.getInstance().setConditionCode(temp);
		return;
	}// swapAF

	public void swapMainRegisters() {
		swap1Reg(Register.B, Register.Bp);
		swap1Reg(Register.C, Register.Cp);
		swap1Reg(Register.D, Register.Dp);
		swap1Reg(Register.E, Register.Ep);
		swap1Reg(Register.H, Register.Hp);
		swap1Reg(Register.L, Register.Lp);
	}// swapMainRegisters

	private void swap1Reg(Register register, Register registerPrime) {
		byte temp = getReg(registerPrime);
		setReg(registerPrime, getReg(register));
		setReg(register, temp);
	}// swap1Reg

//	private static final int WORD_MASK = Z80.WORD_MASK;
//	private static final int BYTE_MASK = Z80.BYTE_MASK;
//	private static final int HI_BYTE_MASK = Z80.HI_BYTE_MASK;

}// class WorkingRegisterSet
