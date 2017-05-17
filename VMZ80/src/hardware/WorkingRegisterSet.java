package hardware;

import java.util.HashMap;

import codeSupport.Z80;
import codeSupport.Z80.Register;

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

	private int programCounter = 0X0000;
	private int stackPointer = 0X0100;
	private int IX = 0X0000;
	private int IY = 0X0000;

	private boolean iff1 = false;
	private boolean iff2 = false;
	private HashMap<Register, Byte> registers;

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

	public void initialize() {
		registers.clear();
		for (Register register : Register.values()) {
			registers.put(register, (byte) 0);
		} // for
		programCounter = 0000;
		stackPointer = 0X0100; // set to non zero
		iff1 = false;
		iff2 = false;
	}// initialize

	public int getProgramCounter() {
		return programCounter;
	}// getProgramCounter

	public void setProgramCounter(int programCounter) {
		this.programCounter = programCounter & WORD_MASK;
	}// setProgramCounter

	public void incrementProgramCounter(int delta) {// setProgramCounter
		setProgramCounter(this.programCounter + delta);
	}// incrementProgramCounter

	public int getStackPointer() {
		return stackPointer;
	}// getStackPointer

	private int getIX() {
		return IX;
	}// getIX

	private int getIY() {
		return IY;
	}// getIY

	public boolean isIFF1Set() {
		return iff1;
	}// isIFF1Set

	public boolean isIFF2Set() {
		return iff2;
	}// isIFF2Set

	public void setStackPointer(int stackPointer) {
		this.stackPointer = stackPointer & WORD_MASK;
	}// setStackPointer

	public void setIX(byte hiByte, byte loByte) {
		int hi = (int) (hiByte << 8);
		int lo = (int) (loByte & 0X00FF);
		this.IX = (hi + lo) & WORD_MASK;
	}// setIX

	private void setIX(int stackPointer) {
		this.IX = stackPointer & WORD_MASK;
	}// setIX

	public void setIY(byte hiByte, byte loByte) {
		int hi = (int) (hiByte << 8);
		int lo = (int) (loByte & 0X00FF);
		this.IY = (hi + lo) & WORD_MASK;
	}// setIY

	private void setIY(int stackPointer) {
		this.IY = stackPointer & WORD_MASK;
	}// setIY

	public void setIFF1(boolean state) {
		iff1 = state;
	}// setIFF1

	public void setIFF2(boolean state) {
		iff2 = state;
	}// setIFF2

	public void setStackPointer(byte hiByte, byte loByte) {
		int hi = (int) (hiByte << 8);
		int lo = (int) (loByte & 0X00FF);
		this.stackPointer = (hi + lo) & WORD_MASK;
	}// setStackPointer

	public void setAcc(byte value) {
		registers.put(Register.A, value);
	}// loadReg

	public byte getAcc() {
		return registers.get(Register.A);
	}// getReg

	public void setReg(Register reg, byte value) {
		registers.put(reg, value);
	}// loadReg

	public byte getReg(Register reg) {
		return registers.get(reg);
	}// getReg

	public void setDoubleReg(Register reg, int value) {
		int hi = value & HI_BYTE_MASK;
		byte hiByte = (byte) ((hi >> 8) & BYTE_MASK);
		byte loByte = (byte) (value & BYTE_MASK);

		switch (reg) {
		case BC:
			setReg(Register.B, hiByte);
			setReg(Register.C, loByte);
			break;
		case DE:
			setReg(Register.D, hiByte);
			setReg(Register.E, loByte);
			break;
		case HL:
		case M:
			setReg(Register.H, hiByte);
			setReg(Register.L, loByte);
			break;
		case SP:
			setStackPointer(value);
			break;
		case PC:
			setProgramCounter(value);
			break;
		case IX:
			setIX(value);
			break;
		case IY:
			setIY(value);
			break;
		default:
			// just fall thru
		}// switch

		return;
	}// setDoubleReg

	public int getDoubleReg(Register reg) {
		byte hi = 0;
		byte lo = 0;

		switch (reg) {
		case BC:
			hi = this.getReg(Register.B);
			lo = this.getReg(Register.C);
			break;
		case DE:
			hi = this.getReg(Register.D);
			lo = this.getReg(Register.E);
			break;
		case HL:
		case M:
			hi = this.getReg(Register.H);
			lo = this.getReg(Register.L);
			break;
		case SP:
			return this.getStackPointer();
		// exits here for SP
		case PC:
			return this.getProgramCounter();
		// exits here for PC
		case IX:
			return this.getIX();
		// exits here for IX
		case IY:
			return this.getIY();
		// exits here for IY
		default:
			// just use 0;
		}// switch
		int result = (((hi << 8) + (lo & BYTE_MASK)) & WORD_MASK);

		return result;
	}// getDoubleReg

	public byte swapAF(byte flags) {
		swap1Reg(Register.A, Register.Ap);

		byte temp = getReg(Register.Fp);
		setReg(Register.Fp, flags);
		return temp;
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

	private static final int WORD_MASK = Z80.WORD_MASK;
	private static final int BYTE_MASK = Z80.BYTE_MASK;
	private static final int HI_BYTE_MASK = Z80.HI_BYTE_MASK;

}// class WorkingRegisterSet
