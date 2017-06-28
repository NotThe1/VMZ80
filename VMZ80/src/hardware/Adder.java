package hardware;

import java.util.BitSet;

import codeSupport.AppLogger;

public class Adder {

	private static Adder instance = new Adder();

	private AppLogger appLogger = AppLogger.getInstance();
	private static final int SIZE = 16;
	private BitSet augend = new BitSet(SIZE);
	private BitSet addend = new BitSet(SIZE);
	private BitSet sum = new BitSet(SIZE);
	private BitSet carryOut = new BitSet(SIZE);
	private BitSet carryIn = new BitSet(SIZE);
	
	public byte[] and(byte[] argument1, byte[] argument2) {
		this.setArgument1(argument1);
		this.setArgument2(argument2);
		sum= (BitSet) augend.clone();
		sum.and(addend);
		return this.getSum();	
	}//and

	public byte[] or(byte[] argument1, byte[] argument2) {
		this.setArgument1(argument1);
		this.setArgument2(argument2);
		sum= (BitSet) augend.clone();
		sum.or(addend);
		return this.getSum();	
	}//or
	
	public byte[] xor(byte[] argument1, byte[] argument2) {
		this.setArgument1(argument1);
		this.setArgument2(argument2);
		sum= (BitSet) augend.clone();
		sum.xor(addend);
		return this.getSum();	
	}//xor
	
	
	public byte[] complement(byte[] argument1) {
		this.setArgument1(argument1);
		sum= (BitSet) augend.clone();
		sum.flip(0,8);
		return this.getSum();	
	}//and

	public byte[] add(byte[] argument1, byte[] argument2) {
		this.setArgument1(argument1);
		this.setArgument2(argument2);
		this.add();
		return this.getSum();
	}// add

	private void add() {
		clearSets();
		int bitCount;
		for (int bitIndex = 0; bitIndex < SIZE; bitIndex++) {
			bitCount = 0;
			bitCount = augend.get(bitIndex) == true ? bitCount + 1 : bitCount;
			bitCount = addend.get(bitIndex) == true ? bitCount + 1 : bitCount;
			bitCount = carryIn.get(bitIndex) == true ? bitCount + 1 : bitCount;
			switch (bitCount) {
			case 0:
				sum.set(bitIndex, false);
				carryOut.set(bitIndex, false);
				carryIn.set(bitIndex + 1, false);
				break;
			case 1:
				sum.set(bitIndex, true);
				carryOut.set(bitIndex, false);
				carryIn.set(bitIndex + 1, false);
				break;
			case 2:
				sum.set(bitIndex, false);
				carryOut.set(bitIndex, true);
				carryIn.set(bitIndex + 1, true);
				break;
			case 3:
				sum.set(bitIndex, true);
				carryOut.set(bitIndex, true);
				carryIn.set(bitIndex + 1, true);
				break;
			default:
			}// switch
		} // for
	}// add


	public byte[] getSum() {
		byte[] ans = sum.toByteArray();
		switch (ans.length) {
		case 0:
			ans = new byte[] { 0X00, 0X00 };
			break;
		case 1:
			byte b0 = ans[0];
			ans = new byte[] {b0, 0X00};
			break;
		case 2:
			// ok
			break;
		default:
		}//switch
		return ans;
	}// getSum


	public static Adder getInstance() {
		return instance;
	}// Factory method

	private Adder() {
		clearSets();
	}// Constructor

	public void setArgument1(byte[] argument1) {
		augend = BitSet.valueOf(argument1);
	}// setArgument1

	public void setArgument2(byte[] argument2) {
		addend = BitSet.valueOf(argument2);
	}// setArgument1

	public void setArguments(byte[] argument1, byte[] argument2) {
		setArgument1(argument1);
		setArgument2(argument2);
	}// setArguments

	private void clearSets() {
		sum.clear();
		carryOut.clear();
		carryIn.clear();
	}// clearSets
	//

	public boolean hasCarry() {
		return hasCarryBase(BYTE_ARG);
	}// isHalfCarrySet

	public boolean hasCarryWord() {
		return hasCarryBase(WORD_ARG);
	}// isHalfCarrySet

	private boolean hasCarryBase(String arg) {
		int bitIndex = arg == BYTE_ARG ? 7 : 15;
		return carryOut.get(bitIndex);
	}// hasCarryBase
	//
	/*
	 * For addition, operands with different signs never cause Overflow. When adding operands with like signs and the
	 * result has a different sign, the Overflow Flag is set,
	 */

	public boolean hasOverflow() {
		return hasOverflowBase(BYTE_ARG);
	}// hasOverflow

	public boolean hasOverflowWord() {
		return hasOverflowBase(WORD_ARG);
	}// hasOverflowWord

	private boolean hasOverflowBase(String arg) {
		int bitIndex = arg == BYTE_ARG ? 7 : 15;
		boolean ans = false;
//		if (!(augend.get(15) ^ addend.get(15))) { // xor
//			ans = augend.get(15) ^ sum.get(15);
//		} // if
//		return ans;
		return carryIn.get(bitIndex) ^ carryOut.get(bitIndex);
	}// hasOverflowBase
	//
	/*
	 * The number of 1 bits in a byte are counted. If the total is Odd, ODD parity is flagged (P = 0). If the total is
	 * Even, EVEN parity is flagged (P = 1).
	 * 
	 */

	public boolean hasParity() {
		return hasParityBase(BYTE_ARG);
	}// hasEvenParity

	public boolean hasParityWord() {
		return hasParityBase(WORD_ARG);
	}// hasEvenParityWord

	private boolean hasParityBase(String arg) {
		int bitIndex = arg == BYTE_ARG ? 8 : 16;
		BitSet bs = sum.get(0, bitIndex);
		return (bs.cardinality() % 2) == 0 ? true : false;
	}// hasEvenParityBase
	//

	public boolean hasHalfCarry() {
		return hasHalfCarryBase(BYTE_ARG);
	}// isHalfCarrySet

	public boolean hasHalfCarryWord() {
		return hasHalfCarryBase(WORD_ARG);
	}// isHalfCarrySet

	private boolean hasHalfCarryBase(String arg) {
		int bitIndex = arg == BYTE_ARG ? 3 : 11;
		return carryOut.get(bitIndex);
	}// hasHalfCarryBase
	//

	public boolean isZero() {
		return isZeroBase(BYTE_ARG);
	}// isZero

	public boolean isZeroWord() {
		return isZeroBase(WORD_ARG);
	}// isZeroWord

	private boolean isZeroBase(String arg) {
		int bitIndex = arg == BYTE_ARG ? 8 : 16;
		BitSet bs = sum.get(0, bitIndex);
		return (bs.cardinality()) == 0 ? true : false;
	}// isZeroBase
	//

	public boolean hasSign() {
		return hasSignBase(BYTE_ARG);
	}// hasSign

	public boolean hasSignWord() {
		return hasSignBase(WORD_ARG);
	}// hasSignWord

	private boolean hasSignBase(String arg) {
		int bitIndex = arg == BYTE_ARG ? 7 : 15;
		return sum.get(bitIndex);
	}// hasSignBase

	// ---------------------------------------------------
	private static final String BYTE_ARG = "ByteArg";
	private static final String WORD_ARG = "WordArg";



}// class Adder
