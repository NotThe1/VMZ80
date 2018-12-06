package codeSupport;

public class Z80 {

	public enum Register {
		// Single Byte Registers
		A, B, C, D, E, H, L, F, Ap, Fp, Bp, Cp, Dp, Ep, Hp, Lp, I, R,
		// AFp,BCp, DEp, HLp,
		// no instructions reference these 16 Bit registers

		// Double Byte Registers
		// used for identification only
		// nothing is stored directly into one of these
		AF, BC, DE, HL, M, SP, PC, IX, IY
	}// enum Register

	public enum ConditionCode {
		NZ, Z, NC, C, PO, PE, P, M
	}// enum conditionCode
	
	public static  byte getBit(int bit) {
		return (byte) (BITS[bit] & BYTE_MASK);
	}//getBit
	
	public static  byte getBitNot(int bit) {
		return (byte) (BITS_NOT[bit] & BYTE_MASK);
	}//getBitNot
	
	public static Register getSingleRegister(int index) {
		return singleRegisters[index];
	}//getSingleRegister
	
	public static Register[] getAllSingleRegisters() {
		return singleRegisters.clone();
	}//getAllSingleResisters()
	
	public static ConditionCode getConditionCode(int index) {
		return conditionCode[index];
	}//getConditionCode
	
	public static Register getDoubleRegister1(int index) {
		return doubleRegisters1[index];
	}//getDoubleRegister1
	
	public static Register[] getAllDoubleRegisters1() {
		return doubleRegisters1.clone();
	}//getAllDoubleRegisters1
	
	public static Register getDoubleRegister2(int index) {
		return doubleRegisters2[index];
	}//getDoubleRegister1
	
	public static Register[] getAllDoubleRegisters2() {
		return doubleRegisters2.clone();
	}//getAllDoubleRegisters1
	
	/**
	 *  tables
	 */

	private static Register[] singleRegisters = new Register[] { Register.B, Register.C, Register.D, Register.E,
			Register.H, Register.L, Register.M, Register.A };

	private static Register[] doubleRegisters1 = new Register[] { Register.BC, Register.DE, Register.HL, Register.SP };

	private static Register[] doubleRegisters2 = new Register[] { Register.BC, Register.DE, Register.HL, Register.AF };

	private static ConditionCode[] conditionCode = new ConditionCode[] { ConditionCode.NZ, ConditionCode.Z,
			ConditionCode.NC, ConditionCode.C, ConditionCode.PO, ConditionCode.PE, ConditionCode.P, ConditionCode.M };

	/**
	 * Constants
	 */

	public static final int WORD_MASK = 0X00FFFF;
	public static final int BYTE_MASK = 0X00FF;
	public static final int HI_BYTE_MASK = 0X00FF00;
	public static final int MSB_BYTE_MASK = 0b1000_0000;
	public static final int MSB_WORD_MASK = 0b1000_0000_0000_0000;
	public static final int ASCII_MASK = 0x7F;	
	
	public static final int MASK_PAGE = 0b1100_0000;
	public static final int MASK_ZZZ = 0b0000_0111;
	public static final int MASK_YYY = 0b0011_1000;
	
	
	public static final int MASK_REGISTER_123 = 0b0000_0111;
	public static final int MASK_REGISTER_345 = 0b0011_1000;
	
	public static final int MASK_DOUBLE_REGISTER45 = 0b0011_0000;
	
	public static final int MODE_0 = 0;
	public static final int MODE_1 = 1;
	public static final int MODE_2 = 2;
	
	
	/**
	 * registers
	 */
	public static final String REG_A = "REG_A";
	public static final String REG_F = "REG_F";
	public static final String REG_B = "REG_B";
	public static final String REG_C = "REG_C";
	public static final String REG_D = "REG_D";
	public static final String REG_E = "REG_E";
	public static final String REG_H = "REG_H";
	public static final String REG_L = "REG_L";
	public static final String REG_M = "REG_M";
	public static final String REG_I = "REG_I";
	public static final String REG_R = "REG_R";

	public static final String REG_AF = "REG_AF";
	public static final String REG_BC = "REG_BC";
	public static final String REG_DE = "REG_DE";
	public static final String REG_HL = "REG_HL";

	public static final String REG_AFp = "REG_AFp";
	public static final String REG_BCp = "REG_BCp";
	public static final String REG_DEp = "REG_DEp";
	public static final String REG_HLp = "REG_HLp";

	public static final String REG_IX = "REG_IX";
	public static final String REG_IY = "REG_IY";
	public static final String REG_SP = "REG_SP";
	public static final String REG_PC = "REG_PC";

	/**
	 * Conditions
	 */
	public static final String COND_NZ = "COND_NZ";
	public static final String COND_Z = "COND_Z";
	public static final String COND_NC = "COND_NC";
	public static final String COND_C = "COND_C";
	public static final String COND_PO = "COND_PO";
	public static final String COND_PE = "COND_PE";
	public static final String COND_P = "COND_P";
	public static final String COND_M = "COND_M";
	public static final int    MASK_CONDITION_CODE = 0b0011_1000;

	/**
	 * masks
	 */
	public static final int WORD_SIGN = 0b1000000000000000;

	public static final byte BIT_SIGN = (byte) 0b10000000;
	public static final byte BIT_ZERO = (byte) 0b01000000;
	public static final byte BIT_BIT5 = (byte) 0b00100000;
	public static final byte BIT_AUX = (byte) 0b00010000;
	public static final byte BIT_BIT3 = (byte) 0b00001000;
	public static final byte BIT_PV = (byte) 0b00000100;
	public static final byte BIT_N = (byte) 0b00000010;
	public static final byte BIT_CARRY = (byte) 0b00000001;

	public static final byte MASK_SIGN = (byte) ~BIT_SIGN;
	public static final byte MASK_ZERO = (byte) ~BIT_ZERO;
	public static final byte MASK_BIT5 = (byte) ~BIT_BIT5;
	public static final byte MASK_AUX = (byte) ~BIT_AUX;
	public static final byte MASK_BIT3 = (byte) ~BIT_BIT3;
	public static final byte MASK_PV = (byte) ~BIT_PV;
	public static final byte MASK_N = (byte) ~BIT_N;
	public static final byte MASK_CARRY = (byte) ~BIT_CARRY;

	public static final byte BIT_0 = (byte) 0b00000001;
	public static final byte BIT_1 = (byte) 0b00000010;
	public static final byte BIT_2 = (byte) 0b00000100;
	public static final byte BIT_3 = (byte) 0b00001000;
	public static final byte BIT_4 = (byte) 0b00010000;
	public static final byte BIT_5 = (byte) 0b00100000;
	public static final byte BIT_6 = (byte) 0b01000000;
	public static final byte BIT_7 = (byte) 0b10000000;

	public static final byte BIT_NOT_0 = (byte) ~BIT_0;
	public static final byte BIT_NOT_1 = (byte) ~BIT_1;
	public static final byte BIT_NOT_2 = (byte) ~BIT_2;
	public static final byte BIT_NOT_3 = (byte) ~BIT_3;
	public static final byte BIT_NOT_4 = (byte) ~BIT_4;
	public static final byte BIT_NOT_5 = (byte) ~BIT_5;
	public static final byte BIT_NOT_6 = (byte) ~BIT_6;
	public static final byte BIT_NOT_7 = (byte) ~BIT_7;

	private static final byte[] BITS = { BIT_0, BIT_1, BIT_2, BIT_3, BIT_4, BIT_5, BIT_6, BIT_7 };
	private static final byte[] BITS_NOT = { BIT_NOT_0, BIT_NOT_1, BIT_NOT_2, BIT_NOT_3, BIT_NOT_4, BIT_NOT_5, BIT_NOT_6,
			BIT_NOT_7 };



}// class Z80 