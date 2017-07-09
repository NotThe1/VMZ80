package hardware;

import java.util.BitSet;

import codeSupport.Z80;
//import codeSupport.Z80;

public class Adder {

	private static Adder instance = new Adder();

	// private AppLogger appLogger = AppLogger.getInstance();
	private static final int SIZE = 16;
	private BitSet augend = new BitSet(SIZE);
	private BitSet addend = new BitSet(SIZE);
	private BitSet sum = new BitSet(SIZE);
	private BitSet carryOut = new BitSet(SIZE);
	private BitSet carryIn = new BitSet(SIZE);

	private boolean sign;
	private boolean zero;
	private boolean halfCarry;
	private boolean parity; // Parity/ Overflow
	private boolean overflow; // Parity/ Overflow
	private boolean nFlag; // N flag add/Subtract
	private boolean carry;

	private boolean signArg1;// what it is being subtracted from
	private boolean signArg2; // what is being subtracted
	
	public byte and(byte argument1, byte argument2) {
		return and(new byte[] {argument1},new  byte[] {argument2});
	}//And

	private byte and(byte[] argument1, byte[] argument2) {
		this.setArgument1(argument1);
		this.setArgument2(argument2);
		sum = (BitSet) augend.clone();
		sum.and(addend);
		setFlags(BYTE_ARG);
		halfCarry=true;
		carry = false;
		return this.getSum()[0];
	}// and
	
	public byte or(byte argument1, byte argument2) {
		return or(new byte[] {argument1},new  byte[] {argument2});
	}//or


	private byte or(byte[] argument1, byte[] argument2) {
		this.setArgument1(argument1);
		this.setArgument2(argument2);
		sum = (BitSet) augend.clone();
		sum.or(addend);
		setFlags(BYTE_ARG);
		halfCarry=false;
		carry = false;
		return this.getSum()[0];
	}// or
	
	public byte xor(byte argument1, byte argument2) {
		return xor(new byte[] {argument1},new  byte[] {argument2});
	}//xor


	private byte xor(byte[] argument1, byte[] argument2) {
		this.setArgument1(argument1);
		this.setArgument2(argument2);
		sum = (BitSet) augend.clone();
		sum.xor(addend);
		setFlags(BYTE_ARG);
		halfCarry=false;
		carry = false;
		return this.getSum()[0];
	}// xor

//	// evaluate argument1(ACC) - argument2
//	private void compare(byte[] argument1, byte[] argument2) {
//		subWithCarry(argument1, argument2, false);
//		return;
//	}// sub
	
	// evaluate argument1(ACC) - argument2
	public void compare( byte argument1, byte argument2) {
		subWithCarry(argument1, argument2, false);
		return;
	}// sub
	
// one's complement
	public byte complement(byte argument1) {
		this.setArgument1(new byte[] {argument1});
		sum = (BitSet) augend.clone();
		sum.flip(0, 16);
		halfCarry = true;
		nFlag = true;
		return this.getSum()[0];
		
//		return complement(new byte[] {argument1});
	}//complement - one's complement

	
//	private byte complement(byte[] argument1) {
//		this.setArgument1(argument1);
//		sum = (BitSet) augend.clone();
//		sum.flip(0, 16);
//		halfCarry = true;
//		nFlag = true;
//		return this.getSum()[0];
//	}// one's complement
	
	// one's complement
	private byte[] complementBare(byte[] argument1) {
		this.setArgument1(argument1);
		sum = (BitSet) augend.clone();
		sum.flip(0, 16);
//		halfCarry = true;
//		nFlag = true;
		return this.getSum();
	}// one's complement

	// two's complement
	public byte negate(byte argument) {
		return sub( (byte) 0X00 , argument);
	}// negatetwo's complement

//	// two's complement
//	private byte negate(byte[] argument) {
//		return sub(new byte[] { (byte) 0X000, (byte) 0X00 }, argument);
//	}// negatetwo's complement

	private byte increment(byte[] argument) {
		return add(argument, new byte[] { (byte) 0X001, (byte) 0X00 });
	}// increment

	public byte increment(byte argument) {
		return add(new byte[] {argument}, new byte[] { (byte) 0X001, (byte) 0X00 });
	}// increment

	public byte[] incrementWord(byte[] argument) {
		return addWord(argument, new byte[] { (byte) 0X001, (byte) 0X00 });
	}// increment

//	private byte decrement(byte[] argument) {
//		return sub(argument, new byte[] { (byte) 0X001, (byte) 0X00 });
//	}// increment
//	
	public byte decrement(byte argument) {
		return sub(argument, (byte) 0X01);
	}// increment

	public byte[] decrementWord(byte[] argument) {
		return subWordWithCarry(argument, new byte[] { (byte) 0X001, (byte) 0X00 }, false);
	}// increment

	private byte add(byte[] argument1, byte[] argument2) {
		return addWithCarry(argument1, argument2, false);
	}// add
	
	public byte add(byte arg1, byte arg2){
		return addWithCarry(new byte[]{arg1}, new byte[]{arg2}, false);
	}//add
	
	public byte addWithCarry(byte arg1, byte arg2,boolean carryState){
		return addWithCarry(new byte[]{arg1}, new byte[]{arg2}, carryState);
	}//addWithCarry

	private byte addWithCarry(byte[] argument1, byte[] argument2, boolean carryState) {
		this.setArgument1(argument1);
		this.setArgument2(argument2);
		this.add(carryState);
		setFlags(BYTE_ARG);
		return this.getSum()[0];
	}// addWithCarry

	public byte[] addWord(byte[] argument1, byte[] argument2) {
		return addWordWithCarry(argument1, argument2, false);
	}// add

	public byte[] addWordWithCarry(byte[] argument1, byte[] argument2, boolean carryState) {
		this.setArgument1(argument1);
		this.setArgument2(argument2);
		this.add(carryState);
		setFlags(WORD_ARG);
		return this.getSum();
	}// add

	private void add(boolean carryInBit0) {
		clearSets();
		carryIn.set(0, carryInBit0);
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
		case 1: // byte
			byte b0 = ans[0];
			ans = new byte[] { b0, 0X00 };
			break;
		case 2: // word
			// ok
			break;
		default:
		}// switch
		return ans;
	}// getSum
//	public byte subWithCarryY(byte argument1,  byte argument2, boolean carryState) {
//		return subWithCarry(new byte[]{ argument1}, new byte[] { argument2},  carryState);
//	}//subWithCarry
	
	public byte subWithCarry(byte argument1,  byte argument2, boolean carryState) {
		byte arg2 = argument2;
		boolean halfCarry0 = false;
		boolean carry0 = false;
		if (carryState) {
			arg2 = this.increment(argument2);
			halfCarry0 = halfCarry;
			carry0 = carry;
			// argument2[0] = arg2;
		} // if
		signArg1 = (argument1 & Z80.BIT_SIGN) == Z80.BIT_SIGN;
		// signArg2 = (arg2 & Z80.BIT_SIGN ) == Z80.BIT_SIGN;
		signArg2 = (argument2 & Z80.BIT_SIGN) == Z80.BIT_SIGN;

		byte[] subtrahend = this.complementBare(new byte[] { arg2 });
		arg2 = this.increment(subtrahend);
		halfCarry0 = halfCarry | halfCarry0;
		carry0 = carry | carry0;

		subtrahend[0] = arg2;
//		byte ans = this.add(argument1, subtrahend);
		byte ans = this.add(argument1, arg2);
		setFlags(BYTE_ARG, true);
		halfCarry = !(halfCarry | halfCarry0);
		carry = !(carry | carry0);
		return ans;
	}//subWithCarry

//	private byte subWithCarry(byte[] argument1, byte[] argument2, boolean carryState) {
//		byte arg2 = argument2[0];
//		boolean halfCarry0 = false;
//		boolean carry0 = false;
//		if (carryState) {
//			arg2 = this.increment(argument2);
//			halfCarry0 = halfCarry;
//			carry0 = carry;
//			// argument2[0] = arg2;
//		} // if
//		signArg1 = (argument1[0] & Z80.BIT_SIGN) == Z80.BIT_SIGN;
//		// signArg2 = (arg2 & Z80.BIT_SIGN ) == Z80.BIT_SIGN;
//		signArg2 = (argument2[0] & Z80.BIT_SIGN) == Z80.BIT_SIGN;
//
//		byte[] subtrahend = this.complementBare(new byte[] { arg2 });
//		arg2 = this.increment(subtrahend);
//		halfCarry0 = halfCarry | halfCarry0;
//		carry0 = carry | carry0;
//
//		subtrahend[0] = arg2;
//		byte ans = this.add(argument1, subtrahend);
//		setFlags(BYTE_ARG, true);
//		halfCarry = !(halfCarry | halfCarry0);
//		carry = !(carry | carry0);
//		return ans;
//	}// subWithCarry

	public byte[] subWordWithCarry(byte[] argument1, byte[] argument2, boolean carryState) {
		byte[] arg2 = argument2.clone();
		boolean halfCarry0 = false;
		boolean carry0 = false;

		if (carryState) {
			arg2 = this.incrementWord(argument2);
			halfCarry0 = halfCarry;
			carry0 = carry;
		} // if

		signArg1 = (argument1[1] & Z80.BIT_SIGN) == Z80.BIT_SIGN;
		signArg2 = (argument2[1] & Z80.BIT_SIGN) == Z80.BIT_SIGN;

		byte[] subtrahend = this.complementBare(arg2);
		subtrahend = this.incrementWord(subtrahend);
		halfCarry0 = halfCarry | halfCarry0;
		carry0 = carry | carry;

		byte[] ans = this.addWord(argument1, subtrahend);
		setFlags(WORD_ARG, true);

		halfCarry = !(halfCarry | halfCarry0);
		carry = !(carry | carry0);
		return ans;
	}// subWithCarry

//	// return argument1 - argument2
//	private byte sub(byte[] argument1, byte[] argument2) {
//		return subWithCarry(argument1, argument2, false);
//	}// sub
	
//	public byte sub(byte argument1, byte argument2) {
//		return subWithCarry(new byte[]{argument1}, new byte[]{argument2}, false);
//	}// sub
	
	public byte sub(byte argument1, byte argument2) {
		return subWithCarry(argument1, argument2, false);
	}// sub


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
		return carry;
	}// isHalfCarrySet
	
	public boolean isNFlagSet(){
		return nFlag;
	}

	/*
	 * For addition, operands with different signs never cause Overflow. When adding operands with like signs and the
	 * result has a different sign, the Overflow Flag is set,
	 */

	public boolean hasOverflow() {
		return overflow;
	}// hasOverflow

	/*
	 * The number of 1 bits in a byte are counted. If the total is Odd, ODD parity is flagged (P = 0). If the total is
	 * Even, EVEN parity is flagged (P = 1).
	 * 
	 */

	public boolean hasParity() {
		return parity;
	}// hasEvenParity

	public boolean hasHalfCarry() {
		return halfCarry;
	}// isHalfCarrySet

	public boolean isZero() {
		return zero;
	}// isZero

	public boolean hasSign() {
		return sign;
	}// hasSign

	private void setFlags(String operationSize) {
		setFlags(operationSize, false);
	}// setFlags

	private void setFlags(String operationSize, boolean aSubtraction) {
		int bitIndex = (operationSize == BYTE_ARG) ? 8 : 16;
		sign = sum.get(bitIndex - 1);

		BitSet bs = sum.get(0, bitIndex);
		zero = (bs.cardinality()) == 0 ? true : false;

		halfCarry = carryOut.get(bitIndex - 5);

		parity = (bs.cardinality() % 2) == 0 ? true : false;

		if (aSubtraction) {
			overflow = false;
			if ((signArg1 ^ signArg2)) {
				overflow = signArg2 == sign;
			} // if
		} else {
			overflow = carryIn.get(bitIndex - 1) ^ carryOut.get(bitIndex - 1);
		}//if

		nFlag = aSubtraction;

		carry = carryOut.get(bitIndex - 1);

	}// setFlags

	// ---------------------------------------------------
	private static final String BYTE_ARG = "ByteArg";
	private static final String WORD_ARG = "WordArg";

}// class Adder
