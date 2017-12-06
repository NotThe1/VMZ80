package hardware;

import java.util.BitSet;

import codeSupport.AppLogger;
import codeSupport.Z80;
//import codeSupport.Z80;

public class ArithmeticUnit {

	private static ArithmeticUnit instance = new ArithmeticUnit();
	private ConditionCodeRegister ccr = ConditionCodeRegister.getInstance();

	private AppLogger log = AppLogger.getInstance();
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

	public static ArithmeticUnit getInstance() {
		return instance;
	}// Factory method

	private ArithmeticUnit() {
		clearSets();
	}// Constructor
		// ----------------------------------------------------------------------------

	public byte daa(byte value, boolean subtractFlag, boolean carryIn, boolean halfCarryIn) {
		byte ans = (byte) 00;
		byte fudge = (byte) 00;
		boolean carryOut = false;
		boolean halfCarryOut = false;

		int flagMix = halfCarryIn ? 1 : 0;
		flagMix = carryIn ? flagMix + 2 : flagMix;
		if (subtractFlag) {// Subtraction
			carryOut = carryIn;
			halfCarryOut = false;
			switch (flagMix) {
			case 0:// !carryIn and !halfCarryIn
				fudge = (byte) 0X00;
				break;
			case 1:// !carryIn and halfCarryIn
				fudge = (byte) 0XFA;
				break;
			case 2:// carryIn and !halfCarryIn
				fudge = (byte) 0XA0;
				break;
			case 3:// carryIn and halfCarryIn
				fudge = (byte) 0X9A;
				break;
			default:
				String message = String.format(
						"[au] daa()%n bad flagMix%n" + " value: %02X, subtractFlag: %s, carryIn: %s,halfCarryIn: %s",
						value, subtractFlag, carryIn, halfCarryIn);
				log.addError(message);
				CentralProcessingUnit.setError(ErrorStatus.ARITHMETIC_UNIT_ERROR);
			}// switch Subtraction

		} else {// Addition
			byte loNibble = (byte) (value & 0X0F);
			byte hiNibble = (byte) ((value & 0XF0) >> 4);
			switch (flagMix) {
			case 0:// !carryIn and !halfCarryIn
				if (loNibble < 0X0A) {// (0-9)
					if (hiNibble < 0X0A) {// 0-9)
						carryOut = false;
						halfCarryOut = false;
						fudge = (byte) 0X00;
					} else {// (A-F)
						carryOut = true;
						halfCarryOut = false;
						fudge = (byte) 0X60;
					} // if hiNibble

				} else {// (A-F)
					if (hiNibble < 0X09) {// (0-8)
						carryOut = false;
						halfCarryOut = true;
						fudge = (byte) 0X06;
					} else {// (9-F)
						carryOut = true;
						halfCarryOut = true;
						fudge = (byte) 0X66;

					} // if hiNibble

				} // if loNibble

				break;
			case 1:// !carryIn and halfCarryIn
				halfCarryOut = false;
				if (hiNibble > 9) {
					carryOut = true;
					fudge = (byte) 0X066;
				} else {
					carryOut = false;
					fudge = (byte) 0X006;
				}
				break;
			case 2:// carryIn and !halfCarryIn
				carryOut = true;
				if (loNibble < 0X0A) {
					fudge = (byte) 0X060;
					halfCarryOut = false;
				} else {
					fudge = (byte) 0X066;
					halfCarryOut = true;
				} // if
				break;
			case 3:// carryIn and halfCarryIn
				fudge = (byte) 0X66;
				carryOut = true;
				halfCarryOut = false;
				break;
			default:
				String message = String.format(
						"[au] daa()%n bad flagMix%n" + " value: %02X, subtractFlag: %s, carryIn: %s,halfCarryIn: %s",
						value, subtractFlag, carryIn, halfCarryIn);
				log.addError(message);
				CentralProcessingUnit.setError(ErrorStatus.ARITHMETIC_UNIT_ERROR);
			}// switch Addition

		} // if add v Sub

		ans = (byte) (value + fudge);

		carry = carryOut;
		halfCarry = halfCarryOut;

		sign = (ans & Z80.BIT_SIGN) == Z80.BIT_SIGN;
		zero = ans == (byte) 0x00;

		BitSet bs = new BitSet(8);
		bs = BitSet.valueOf(new byte[] { ans });
		parity = (bs.cardinality() % 2) == 0 ? true : false;

		return ans;
	}
	// ----------------------------------------------------------------------------

	public byte and(byte argument1, byte argument2) {
		this.setArgument1(new byte[] { argument1 });
		this.setArgument2(new byte[] { argument2 });
		sum = (BitSet) augend.clone();
		sum.and(addend);
		setFlags(BYTE_ARG);
		halfCarry = true;
		carry = false;
		return this.getSum()[0];
	}// And

	public byte or(byte argument1, byte argument2) {
		this.setArgument1(new byte[] { argument1 });
		this.setArgument2(new byte[] { argument2 });
		sum = (BitSet) augend.clone();
		sum.or(addend);
		setFlags(BYTE_ARG);
		halfCarry = false;
		carry = false;
		return this.getSum()[0];
	}// or

	public byte xor(byte argument1, byte argument2) {
		this.setArgument1(new byte[] { argument1 });
		this.setArgument2(new byte[] { argument2 });
		sum = (BitSet) augend.clone();
		sum.xor(addend);
		setFlags(BYTE_ARG);
		halfCarry = false;
		carry = false;
		return this.getSum()[0];

	}// xor

	// evaluate argument1(ACC) - argument2
	public void compare(byte argument1, byte argument2) {
		subWithCarry(argument1, argument2, false);
		return;
	}// sub

	// one's complement
	public byte complement(byte argument1) {
		this.setArgument1(new byte[] { argument1 });
		sum = (BitSet) augend.clone();
		sum.flip(0, 16);
		halfCarry = true;
		nFlag = true;
		return this.getSum()[0];
	}// complement - one's complement

	// one's complement
	private byte[] complementWord(byte[] argument1) {
		this.setArgument1(argument1);
		sum = (BitSet) augend.clone();
		sum.flip(0, 16);
		return this.getSum();
	}// one's complement

	// two's complement
	public byte negate(byte argument) {
		return sub((byte) 0X00, argument);
	}// negatetwo's complement
		// -----------------------------------------------------------------------------------------------------

	public byte rotateLeft(byte arg) {
		return rotateLeft(arg, false, false);
	}// rotateLeft

	public byte rotateLeftThru(byte arg, boolean carryBefore) {
		return rotateLeft(arg, carryBefore, true);
	}// rotateLeftThru

	private byte rotateLeft(byte arg, boolean carryBefore, boolean thru) {
		clearSets();
		setSum(arg);
		boolean originalBit7 = sum.get(7);

		for (int i = 7; i > 0; i--) {
			sum.set(i, sum.get(i - 1));
		} // for

		if (thru) {
			sum.set(0, carryBefore);
		} else {
			sum.set(0, originalBit7);
		} //
		setFlagsRotate(originalBit7);
		return getSum()[0];
	}// rotateLeft

	public byte rotateRight(byte arg) {
		return rotateRight(arg, false, false);
	}// rotateRight

	public byte rotateRightThru(byte arg, boolean carryBefore) {
		return rotateRight(arg, carryBefore, true);
	}// rotateRightThru

	private byte rotateRight(byte arg, boolean carryBefore, boolean thru) {
		clearSets();
		setSum(arg);
		boolean originalBit0 = sum.get(0);

		for (int i = 0; i < 7; i++) {
			sum.set(i, sum.get(i + 1));
		} // for

		if (thru) {
			sum.set(7, carryBefore);
		} else {
			sum.set(7, originalBit0);
		} //
		setFlagsRotate(originalBit0);
		return getSum()[0];
	}// rotateRight

	// ------------------ shift SLA, SRA, SRL
	private byte shiftRight(byte arg, boolean zeroSeed) {
		clearSets();
		setSum(arg);
		boolean originalBit0 = sum.get(0);
		boolean originalBit7 = sum.get(7);

		for (int i = 0; i < 7; i++) {
			sum.set(i, sum.get(i + 1));
		} // for

		if (zeroSeed) {
			sum.set(7, false);
		} else {
			sum.set(7, originalBit7);
		} // if
		setFlagsRotate(originalBit0);
		return getSum()[0];

	}// shiftRight

	public byte shiftSRL(byte arg) {
		return shiftRight(arg, true);
	}// shiftSRL

	public byte shiftSRA(byte arg) {
		return shiftRight(arg, false);
	}// shiftSRA

	public byte shiftSLA(byte arg) {
		return shiftSLASLL(arg, false);
	}// shiftSLA

	public byte shiftSLL(byte arg) {
		return shiftSLASLL(arg, true);
	}// shiftSLA

	public byte shiftSLASLL(byte arg, boolean seed) {
		clearSets();
		setSum(arg);
		boolean originalBit7 = sum.get(7);

		for (int i = 7; i > 0; i--) {
			sum.set(i, sum.get(i - 1));
		} // for

		sum.set(0, false);
		setFlagsRotate(originalBit7);
		return getSum()[0];

	}// shiftSLA

	// -----------------------------------------------------------------------------------------------------

	private int getBitValue(int arg) {
		// int index = Math.max(arg, 0);
		return Math.min(7, arg);
	}// getBitValue

	public void bitTest(byte arg, int bit) {
		byte mask = Z80.BITS[getBitValue(bit)];
		halfCarry = true;
		nFlag = false;
		zero = (arg & mask) != mask;
	}// bitTest

	public byte bitSet(byte arg, int bit) {
		byte mask = Z80.BITS[getBitValue(bit)];
		return (byte) (arg | mask);
	}// bitTest

	public byte bitRes(byte arg, int bit) {
		byte mask = Z80.BITS_NOT[getBitValue(bit)];
		return (byte) (arg & mask);
	}// bitTest

	// -----------------------------------------------------------------------------------------------------
	public byte increment(byte argument) {
		return add(argument, (byte) 0X001);
	}// increment

	public byte[] incrementWord(byte[] argument) {
		return addWord(argument, new byte[] { (byte) 0X001, (byte) 0X00 });
	}// increment

	public byte decrement(byte argument) {
		return sub(argument, (byte) 0X01);
	}// increment

	public byte[] decrementWord(byte[] argument) {
		return subWordWithCarry(argument, new byte[] { (byte) 0X001, (byte) 0X00 }, false);
	}// increment

	public byte add(byte arg1, byte arg2) {
		return addWithCarry(arg1, arg2, false);
	}// add

	public byte addWithCarry(byte arg1, byte arg2, boolean carryState) {
		this.setArgument1(new byte[] { arg1 });
		this.setArgument2(new byte[] { arg2 });
		this.add(carryState);
		setFlags(BYTE_ARG);
		return this.getSum()[0];
	}// addWithCarry

	public int addWord(int word1, int word2) {
		// byte arg1[] = {(byte)((word1 >>8) & 0XFF),(byte) (word1 & 0XFF)};
		// byte arg2[] = {(byte)((word2 >>8) & 0XFF),(byte) (word2 & 0XFF)};
		byte arg1[] = { (byte) (word1 & 0XFF), (byte) ((word1 >> 8) & 0XFF) };
		byte arg2[] = { (byte) (word2 & 0XFF), (byte) ((word2 >> 8) & 0XFF) };

		byte[] ans = addWordWithCarry(arg1, arg2, false);
		return (int) (ans[0] << 8 + ans[1]);
	}// addWord(int,int)

	public byte[] addWord(byte[] argument1, byte[] argument2) {
		return addWordWithCarry(argument1, argument2, false);
	}// add

	public byte[] addWordWithCarry(int word1, int word2, boolean carryState) {
		byte arg1[] = { (byte) ((word1 >> 8) & 0XFF), (byte) (word1 & 0XFF) };
		byte arg2[] = { (byte) ((word2 >> 8) & 0XFF), (byte) (word2 & 0XFF) };
		return addWordWithCarry(arg1, arg2, carryState);
	}// addWord(int,int)

	public byte[] addWordWithCarry(byte[] argument1, byte[] argument2, boolean carryState) {
		this.setArgument1(argument1);
		this.setArgument2(argument2);
		this.add(carryState);
		setFlags(WORD_ARG);
		ccr.setNFlag(false);
		ccr.setHFlag(halfCarry);
		ccr.setCarryFlag(carry);
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
				String message = String.format(
						"[au] add()%n bad bitCount: %02X%n"
				+ " augend: %s ,addend: %s ,carryIn: %s",
						augend,addend,carryIn);
				log.addError(message);
				CentralProcessingUnit.setError(ErrorStatus.ARITHMETIC_UNIT_ERROR);
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
			String message = String.format("[au] getSum(): bad ans.length: %02X%n",ans.length);
			log.addError(message);
			CentralProcessingUnit.setError(ErrorStatus.ARITHMETIC_UNIT_ERROR);
		}// switch
		return ans;
	}// getSum

	public byte subWithCarry(byte argument1, byte argument2, boolean carryState) {
		signArg1 = (argument1 & Z80.BIT_SIGN) == Z80.BIT_SIGN;
		signArg2 = (argument2 & Z80.BIT_SIGN) == Z80.BIT_SIGN;

		byte arg2 = argument2;
		boolean halfCarry0 = false;
		boolean carry0 = false;
		if (carryState) {
			arg2 = this.increment(argument2);
			halfCarry0 = halfCarry;
			carry0 = carry;
		} // if

		byte subtrahend = this.complement(arg2);
		arg2 = this.increment(subtrahend);

		// halfCarry0 = halfCarry | halfCarry0;
		// carry0 = carry | carry0;
		halfCarry0 = halfCarry ^ halfCarry0;
		carry0 = carry ^ carry0;

		byte ans = this.add(argument1, arg2);
		setFlags(BYTE_ARG, true);
		halfCarry = !(halfCarry | halfCarry0);
		carry = !(carry | carry0);
		return ans;
	}// subWithCarry

	public byte[] subWordWithCarry(byte[] argument1, byte[] argument2, boolean carryState) {
		signArg1 = (argument1[1] & Z80.BIT_SIGN) == Z80.BIT_SIGN;
		signArg2 = (argument2[1] & Z80.BIT_SIGN) == Z80.BIT_SIGN;

		byte[] arg2 = argument2.clone();
		boolean halfCarry0 = false;
		boolean carry0 = false;

		if (carryState) {
			arg2 = this.incrementWord(argument2);
			halfCarry0 = halfCarry;
			carry0 = carry;
		} // if

		byte[] subtrahend = this.complementWord(arg2);
		subtrahend = this.incrementWord(subtrahend);

		halfCarry0 = halfCarry ^ halfCarry0;
		carry0 = carry ^ carry0;
		byte[] ans = this.addWord(argument1, subtrahend);
		setFlags(WORD_ARG, true);

		halfCarry = !(halfCarry ^ halfCarry0);
		carry = !(carry ^ carry0);
		ccr.setNFlag(true);
		ccr.setHFlag(halfCarry);
		ccr.setCarryFlag(carry);

		return ans;
	}// subWithCarry

	public byte sub(byte argument1, byte argument2) {
		return subWithCarry(argument1, argument2, false);
	}// sub

	public void setArgument1(byte[] argument1) {
		augend = BitSet.valueOf(argument1);
	}// setArgument1

	public void setArgument2(byte[] argument2) {
		addend = BitSet.valueOf(argument2);
	}// setArgument1

	// private void setArguments(byte[] argument1, byte[] argument2) {
	// setArgument1(argument1);
	// setArgument2(argument2);
	// }// setArguments

	private void setSum(byte argument) {
		sum = BitSet.valueOf(new byte[] { argument });
	}// setSum

	private void clearSets() {
		sum.clear();
		carryOut.clear();
		carryIn.clear();
	}// clearSets
		//

	public boolean isCarryFlagSet() {
		return carry;
	}// isHalfCarrySet

	public boolean isNFlagSet() {
		return nFlag;
	}

	/*
	 * For addition, operands with different signs never cause Overflow. When adding operands with like signs and the
	 * result has a different sign, the Overflow Flag is set,
	 */

	public boolean isOverflowFlagSet() {
		return overflow;
	}// hasOverflow

	/*
	 * The number of 1 bits in a byte are counted. If the total is Odd, ODD parity is flagged (P = 0). If the total is
	 * Even, EVEN parity is flagged (P = 1).
	 * 
	 */

	public boolean isParityFlagSet() {
		return parity;
	}// hasEvenParity

	public boolean isHCarryFlagSet() {
		return halfCarry;
	}// isHalfCarrySet

	public boolean isZeroFlagSet() {
		return zero;
	}// isZero

	public boolean isSignFlagSet() {
		return sign;
	}// hasSign

	private void setFlagsRotate(boolean carryResult) {
		carry = carryResult;
		halfCarry = false;
		nFlag = false;

		sign = sum.get(7);

		BitSet bs = sum.get(0, 8);
		zero = (bs.cardinality()) == 0 ? true : false;
		parity = (bs.cardinality() % 2) == 0 ? true : false;

	}// setFlagsRotate

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
		} // if

		nFlag = aSubtraction;

		carry = carryOut.get(bitIndex - 1);

	}// setFlags

	// ---------------------------------------------------
	private static final String BYTE_ARG = "ByteArg";
	private static final String WORD_ARG = "WordArg";

}// class Adder
