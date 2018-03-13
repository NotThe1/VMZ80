package hardware;

import codeSupport.Z80;

// |7|6|5|4|3|2|1|0
// |S|Z|0|A|0|P|1|C
/**
 *  
 * @author Frank Martyn
 * @version 1.0
 *
 *          ConditionCodeRegister - has the system registers
 *          <p>
 *          This class is just a little more than a repository. It mostly has getter and setters for the five boolean
 *          flags Sign,Zero, AUX parity,Parity and Carry. There are Methods for: - Clearing all the codes. - Setting the
 *          all the codes collectively - Setting the Zero, Sign and Parity based on value passed
 * 
 */
public class ConditionCodeRegister  {

	private static ConditionCodeRegister instance = new ConditionCodeRegister();

	private boolean signFlag = false; // set to most significant bit (7)
	private boolean zeroFlag = false; // set if result = 0;
//	private boolean bit5Flag = false; 
	private boolean hFlag = false;
	
//	private boolean bit3Flag = false; 
	private boolean pvFlag = false; // set to one if even parity, reset if odd
	private boolean nFlag = false;
	private boolean carryFlag = false;

	// +++++++++++++++++++++++++++++++++++++++++++++

	public static ConditionCodeRegister getInstance() {
		
		return instance;
	}// getConditionCodeRegister

	/**
	 * No work done just makes the object
	 */
	private ConditionCodeRegister() {
	}// Constructor - ConditionCodeRegister()
		// +++++++++++++++++++++++++++++++++++++++++++++

	public byte getConditionCode() {
		// |7|6|5|4|3|2|1|0
		// |S|Z|0|A|0|P|N|C

//		byte conditionCode = (byte) 0B00000010;
		byte conditionCode = (byte) 0B00000000;
		conditionCode = (byte) ((signFlag) ? conditionCode | Z80.BIT_SIGN : conditionCode & Z80.MASK_SIGN);
		conditionCode = (byte) ((zeroFlag) ? conditionCode | Z80.BIT_ZERO : conditionCode & Z80.MASK_ZERO);
		conditionCode = (byte) ((hFlag) ? conditionCode | Z80.BIT_AUX : conditionCode & Z80.MASK_AUX);
		conditionCode = (byte) ((pvFlag) ? conditionCode | Z80.BIT_PV : conditionCode & Z80.MASK_PV);
		conditionCode = (byte) ((nFlag) ? conditionCode | Z80.BIT_N : conditionCode & Z80.MASK_N);
		conditionCode = (byte) ((carryFlag) ? conditionCode | Z80.BIT_CARRY : conditionCode & Z80.MASK_CARRY);

		return conditionCode;
	}// getConditionCode

	public void setConditionCode(byte flags) {
		// |7|6|5|4|3|2|1|0
		// |S|Z|0|A|0|P|N|C

		setSignFlag((flags & Z80.BIT_SIGN) == Z80.BIT_SIGN);
		setZeroFlag((flags & Z80.BIT_ZERO) == Z80.BIT_ZERO);
		setHFlag((flags & Z80.BIT_AUX) == Z80.BIT_AUX);
		setPvFlag((flags & Z80.BIT_PV) == Z80.BIT_PV);
		setNFlag((flags & Z80.BIT_N) == Z80.BIT_N);
		setCarryFlag((flags & Z80.BIT_CARRY) == Z80.BIT_CARRY);
	}// setConditionCode

	public void clearAllCodes() {
		hFlag = false;
		carryFlag = false;
		signFlag = false;
		pvFlag = false;
		nFlag = false;
		zeroFlag = false;
	}// clearAllCodes

	public void setZSP(byte value) {
		this.setZeroFlag(value == 0);
		this.setSignFlag((value & Z80.BIT_SIGN) != 0);
		this.setPvFlag((Integer.bitCount(value) % 2) == 0);
	}// setZSP
	
	public void setZSP16(int value){
		this.setZeroFlag(value == 0);
		this.setSignFlag((value & Z80.WORD_SIGN) == Z80.WORD_SIGN);
		this.setPvFlag((Integer.bitCount(value) % 2) == 0);
	}//setZSP16

	public void setZSPclearCYandAUX(byte value) {
		clearAllCodes();
		setZSP(value);
	}// setZSPclearCYandAUX

	public boolean isHFlagSet() {
		return hFlag;
	}// isAuxilaryCarryFlagSet

	public void setHFlag(boolean auxFlag) {
		this.hFlag = auxFlag;
	}// setAuxilaryCarryFlag

	public boolean isCarryFlagSet() {
		return carryFlag;
	}// isCarryFlagSet

	public void setCarryFlag(boolean carryFlag) {
		this.carryFlag = carryFlag;
	}// setCarryFlag

	public boolean isNFlagSet() {
		return nFlag;
	}// isCarryFlagSet

	public void setNFlag(boolean NFlag) {
		this.nFlag = NFlag;
	}// setCarryFlag

	public boolean isSignFlagSet() {
		return signFlag;
	}// isSignFlagSet

	public void setSignFlag(boolean signFlag) {
		this.signFlag = signFlag;
	}// setSignFlag

	public boolean isPvFlagSet() {
		return pvFlag;
	}// isParityFlagSet

	public void setPvFlag(boolean PVFlag) {
		this.pvFlag = PVFlag;
	}// setParityFlag

	public boolean isZeroFlagSet() {
		return zeroFlag;
	}// isZeroFlagSet

	public void setZeroFlag(boolean zeroFlag) {
		this.zeroFlag = zeroFlag;
	}// setZeroFlag


}// ConditionCodeRegister
