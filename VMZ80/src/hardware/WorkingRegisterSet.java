package hardware;

import java.util.HashMap;

import codeSupport.AppLogger;
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

	public void setProgramCounter(int programCounter) {
		this.programCounter = programCounter & WORD_MASK;
	}// setProgramCounter
	
	public void setProgramCounter(byte[] programCounterValue) {
		int hi = (int) (programCounterValue[0] << 8);
		int lo = (int) (programCounterValue[1] & 0X00FF);
		setProgramCounter((hi + lo) & WORD_MASK);
	}// setProgramCounter

	
	public void setProgramCounter(byte hiByte, byte loByte) {
		int hi = (int) (hiByte << 8);
		int lo = (int) (loByte & 0X00FF);
		setProgramCounter((hi + lo) & WORD_MASK);
	}// setStackPointer


	public void incrementProgramCounter(int delta) {// setProgramCounter
		setProgramCounter(this.programCounter + delta);
	}// incrementProgramCounter
	
	public int getProgramCounter() {
		return this.programCounter;
	}// getProgramCounter
	
	public byte[] getProgramCounterArray() {
		return splitWord(this.programCounter);
	}//getProgramCounterArray



	public int getStackPointer() {
		return this.stackPointer;
	}// getStackPointer

	public byte[] getStackPointerArray() {
		return splitWord(this.stackPointer);
	}// getStackPointerarray

	public int getIX() {
		return this.IX;
	}// getIX
	
	public byte[]getIXarray(){
		return splitWord(this.IX);
	}//getIXarray

	public int getIY() {
		return this.IY;
	}// getIY
	
	public byte[]getIYarray(){
		return splitWord(this.IY);
	}//getIYarray


	public boolean isIFF1Set() {
		return iff1;
	}// isIFF1Set

	public boolean isIFF2Set() {
		return iff2;
	}// isIFF2Set


//	public void setStackPointer(byte hiByte, byte loByte) {
//		int hi = (int) (hiByte << 8);
//		int lo = (int) (loByte & 0X00FF);
//		this.stackPointer = (hi + lo) & WORD_MASK;
//	}// setIX
	
	public void setIX(byte hiByte, byte loByte) {
		int hi = (int) (hiByte << 8);
		int lo = (int) (loByte & 0X00FF);
		this.IX = (hi + lo) & WORD_MASK;
	}// setIX

	public void setIX(int ixValue) {
		this.IX = ixValue & WORD_MASK;
	}// setIX
	
	public void setIX(byte[] IXValue) {
		int hi = (int) (IXValue[0] << 8);
		int lo = (int) (IXValue[1] & 0X00FF);
		setIX((hi + lo) & WORD_MASK);
	}// setIX

	public void setIY(int IYValue) {
		this.IY = IYValue & WORD_MASK;
	}// setIY
	
	public void setIY(byte hiByte, byte loByte) {
		int hi = (int) (hiByte << 8);
		int lo = (int) (loByte & 0X00FF);
		setIY((hi + lo) & WORD_MASK);
	}// setIY
	
	public void setIY(byte[] IYValue) {
		int hi = (int) (IYValue[0] << 8);
		int lo = (int) (IYValue[1] & 0X00FF);
		setIX((hi + lo) & WORD_MASK);
	}// setIX



	public void setIFF1(boolean state) {
		iff1 = state;
	}// setIFF1

	public void setIFF2(boolean state) {
		iff2 = state;
	}// setIFF2
	
	public void setStackPointer(int stackPointerValue) {
		this.stackPointer = stackPointerValue & WORD_MASK;
	}// setStackPointer

	public void setStackPointer(byte[] stackPointerValue) {
		int hi = (int) (stackPointerValue[0] << 8);
		int lo = (int) (stackPointerValue[1] & 0X00FF);
		setStackPointer((hi + lo) & WORD_MASK);
	}// setStackPointer

	public void setStackPointer(byte hiByte, byte loByte) {
		int hi = (int) (hiByte << 8);
		int lo = (int) (loByte & 0X00FF);
		setStackPointer((hi + lo) & WORD_MASK);
		
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
		int result = 0;

		switch (reg) {
		case BC:
//			hi = this.getReg(Register.B);
//			lo = this.getReg(Register.C);
//			result = (((hi << 8) + (lo & BYTE_MASK)) & WORD_MASK);
			result = (((getReg(Register.B) << 8) + (getReg(Register.C) & BYTE_MASK)) & WORD_MASK);	
			break;
		case DE:
			result = (((getReg(Register.D) << 8) + (getReg(Register.E) & BYTE_MASK)) & WORD_MASK);
			break;
		case HL:
		case M:
			result = (((getReg(Register.H) << 8) + (getReg(Register.L) & BYTE_MASK)) & WORD_MASK);
			break;
		case SP:
			result =  this.getStackPointer();
		break;
		case PC:
			result = this.getProgramCounter();
		break;
		case IX:
			result =  this.getIX();
		break;
		case IY:
			result = this.getIY();
		break;
		default:
			log.addError(String.format("[workingRegisterSet] %n getDoubleReg-%s at location %04X,",
					reg,this.programCounter));
			System.exit(-1);
		}// switch

		return result;
	}// getDoubleReg
	
	public void setDoubleReg(Register reg, byte hi, byte lo) {
		setDoubleReg(reg,new byte[] {hi,lo});
	}//setDoubleReg
	
	public void setDoubleReg(Register reg, byte[] registerValues) {

		switch (reg) {
		case BC:
			setReg(Register.B, registerValues[0]);
			setReg(Register.C, registerValues[1]);
			break;
		case DE:
			setReg(Register.D, registerValues[0]);
			setReg(Register.E, registerValues[1]);
			break;
		case HL:
		case M:
			setReg(Register.H, registerValues[0]);
			setReg(Register.L, registerValues[1]);
			break;
		case SP:
			setStackPointer(registerValues[0],registerValues[1]);
			break;
		case PC:
			setStackPointer(registerValues[0],registerValues[1]);
			break;
		case IX:
			setIX(registerValues[0],registerValues[1]);
			break;
		case IY:
			setIY(registerValues[0],registerValues[1]);
			break;
		default:
			// just fall thru
		}// switch

		return;
	}// setDoubleReg

	public byte[] getDoubleRegArray(Register reg) {
		byte[] ans = new byte[2];

		switch (reg) {
		case BC:
			ans[0] = this.getReg(Register.B);
			ans[1] = this.getReg(Register.C);
			break;
		case DE:
			ans[0] = this.getReg(Register.D);
			ans[1] = this.getReg(Register.E);
			break;
		case HL:
		case M:
			ans[0] = this.getReg(Register.H);
			ans[1] = this.getReg(Register.L);
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
			// just use 0;
		}// switch

		return ans;
	}// getDoubleReg

	private byte[] splitWord(int wordValue) {
		return new byte[] { (byte) ((wordValue >> 8) & 0XFF), (byte) (wordValue & 0XFF) };
	}// split word

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
