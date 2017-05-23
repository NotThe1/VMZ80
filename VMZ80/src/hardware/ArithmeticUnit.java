package hardware;

/**
 * 
 * @author Frank Martyn
 * @version 1.0
 *
 *          ArithmeticUnit - does the computation for the machine
 *          <p>
 *          This class does all computation for the virtual machine. It follows the rules laid out for the Zilog Z80 as
 *          it states how and when those operations are:
 *          <p>
 *          1 affected by the condition codes
 *          <p>
 *          2 how those operations set/reset those same condition codes.
 *          <p>
 *          It is an 8bit engine with some 16 bit operations.
 * 
 *          <p>
 *          this class is a singleton
 * 
 * 
 * 
 * 
 */
public class ArithmeticUnit {
	static ConditionCodeRegister ccr;
	private static ArithmeticUnit instance = new ArithmeticUnit();

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
	public static ArithmeticUnit getInstance() {

		return instance;
	}// getArithmeticUnit

	/**
	 * 
	 * @param ccr
	 *            requires the system Condition Code Register
	 */
	private ArithmeticUnit() {
		ccr = ConditionCodeRegister.getInstance();
	}// Constructor

	/**
	 * returns a one's complement on the passed value
	 */

	// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

	/**
	 * will give the appropriate carry condition based on the mask. Masks : CARRY_AUX,CARRY_BYTE,CARRY_WORD
	 * 
	 * @param operand1
	 *            first number to use
	 * @param operand2
	 *            second number
	 * @param carryMask
	 *            type of carry desired to be calculated
	 * @return state of carry desired
	 */
	private boolean carryOut(int operand1, int operand2, int carryMask) {
		int result = (operand1 & carryMask) + (operand2 & carryMask);
		return (result > carryMask) ? true : false;
	}// carryOut

	/**
	 * add the two provided numbers as Bytes or Words.
	 * <p>
	 * The operandSize (8/16) controls the type of add as well as what Carry flags are calculated and set/reset.Only a
	 * Byte add will calculate the AUX Carry. The Carry flag is always calculated.
	 * 
	 * @param operand1
	 *            first value
	 * @param operand2
	 *            second value
	 * @param operandMask
	 *            either MASK_BYTE or MASK_WORD to control the type of add
	 * @return
	 */
	private int add(int operand1, int operand2, int operandMask) {
		int carryMask;
		if (operandMask == MASK_BYTE) {// need to handle Auxiliary Carry
			boolean auxilaryCarryFlag = carryOut(operand1, operand2, CARRY_AUX);
			ccr.setHFlag(auxilaryCarryFlag);
			carryMask = CARRY_BYTE; // two nibbles
		} else {
			carryMask = CARRY_WORD; // two bytes
		} // if
		boolean carryFlag = carryOut(operand1, operand2, carryMask);
		ccr.setCarryFlag(carryFlag);
		return (operand1 + operand2) & operandMask;
	}// add

	/**
	 * add two bytes
	 * <p>
	 * Add the two supplied bytes and set/resets All condition flags
	 * 
	 * @param operand1
	 *            First value
	 * @param operand2
	 *            Second value
	 * @return 8 bit sum
	 */
	// @Override
	public byte add(byte operand1, byte operand2) {
		byte result = (byte) add((byte) operand1, (byte) operand2, MASK_BYTE);
//		boolean overflow = false;
//		if (((operand1 ^ operand2) & MASK_SIGN) == 0) { // same sign
//			if (((operand1 ^ result) & MASK_SIGN) != 0) { // result different sign sign
//				overflow = true;
//			} // if result had same sign
//		} // if source had same sign
		ccr.setZSP(result);
		ccr.setPvFlag(hasOverflow(operand1,operand2,result,MASK_SIGN_BYTE));	//replace P with V
		ccr.setNFlag(false);
		return result;
	}// add(byte operand1, byte operand2)

	/**
	 * Add two 16 bit words
	 * <p>
	 * This operation set/resets the Carry flag, but does not affect any other flags
	 * 
	 * @param operand1
	 *            First Value
	 * @param operand2
	 *            Other Value
	 * @return 16 bit sum of the two values
	 */
	// @Override
	public int add(int operand1, int operand2) { // add words
		return add(operand1, operand2, MASK_WORD);
	}// add(short operand1, short operand2)

	/**
	 * addWithCarry two bytes plus the Carry bit
	 * <p>
	 * Add the two supplied bytes plus Carry and set/resets All condition flags
	 * 
	 * @param operand1
	 *            First value
	 * @param operand2
	 *            Second value
	 * @return 8 bit sum
	 */
	// @Override
	public byte addWithCarry(byte operand1, byte operand2) {
		boolean carryFlagIn = ccr.isCarryFlagSet();
		boolean carryFromIncrement = false;
		boolean auxCarryFromIncrement = false;
		if (carryFlagIn) {
			operand2 = (byte) this.add(operand2, 1, MASK_BYTE);
			carryFromIncrement = ccr.isCarryFlagSet(); // carry flag from increment
			auxCarryFromIncrement = ccr.isHFlagSet(); // Aux carry from increment
		} // if carryFlagIn

		byte result = (byte) add((int) operand1, (int) operand2, MASK_BYTE);
		boolean carryFlagOut = ccr.isCarryFlagSet() | carryFromIncrement;// carry is true if either flag is set
		boolean auxCarryFlagOut = ccr.isHFlagSet() | auxCarryFromIncrement;// Aux carry is true if either
																			// flag is set
		ccr.setHFlag(auxCarryFlagOut);
		ccr.setCarryFlag(carryFlagOut);
		ccr.setZSP(result);
		return result;
	}// addWithCarry
	
	private boolean hasOverflow(int operand1, int operand2,int result, int  signMask){
		boolean hasOverflow = false;
		if (((operand1 ^ operand2) & signMask) == 0) { // same sign
			if (((operand1 ^ result) & signMask) != 0) { // result different sign sign
				hasOverflow = true;
			} // if result had same sign
		} // if source had same sign
		return hasOverflow;
	}
		// // <><><><><><><><><><><><><><><><><><><><><><><><>

	// /**
	// * increment - will increment the passed byte value by one. It will set/reset the following flags : Aux
	// Carry,Zero,
	// * Sign and Parity. Note the Carry flag remains unchanged!
	// *
	// * @param value
	// * byte value to be incremented
	// * @return byte result of incrementing input value.
	// */
	// @Override
	// public byte increment(byte value) {
	// boolean priorCarry = ccr.isCarryFlagSet();
	// byte result = (byte) add((int) value, 1, MASK_BYTE);
	// ccr.setCarryFlag(priorCarry);
	// ccr.setZSP(result);
	// return result;
	// }// increment(byte value)
	//
	// /**
	// * increment - will increment the passed int value by one. It will set/reset the following flags : None
	// *
	// * @param value
	// * value to be incremented
	// * @return result of incrementing input value.
	// */
	// @Override
	// public int increment(int value) {
	// byte conditionCodesPrior = ccr.getConditionCode();
	// int ans = add((int) value, 1, MASK_WORD);
	// ccr.setConditionCode(conditionCodesPrior);
	// return ans & MASK_WORD;
	// }// increment(byte value)
	//
	// /**
	// * decrement - will decrement the passed byte value by one. It will set/reset the following flags : Aux
	// Carry,Zero,
	// * Sign and Parity. Note the Carry flag remains unchanged!
	// *
	// * @param value
	// * byte value to be decremented
	// * @return byte result of decrementing input value.
	// */
	// @Override
	// public byte decrement(byte value) {
	// boolean priorCarry = ccr.isCarryFlagSet();
	// byte result = (byte) subtract((int) value, 1, MASK_BYTE);
	// ccr.setCarryFlag(priorCarry);
	// ccr.setZSP(result);
	// return result;
	// }// decrement(byte value)
	//
	// /**
	// * decrement - will decrease the passed int value by one. It will set/reset the following flags : None
	// *
	// * @param value
	// * value to be decremented
	// * @return result of decrementing input value.
	// */
	// @Override
	// public int decrement(int value) {
	// byte conditionCodesPrior = ccr.getConditionCode();
	// int ans = subtract((int) value, 1, MASK_WORD);
	// ccr.setConditionCode(conditionCodesPrior);
	// return ans & MASK_WORD;
	// }// decrement
	//
	// /**
	// * logicalAnd - the two values are logically ANDed and returned. It will set/reset the following flags :Zero, Sign
	// * and Parity. The Carry flag is reset to 0. The Aux Carry flag is reset to 0
	// *
	// * @param operand1
	// * first value to be anded
	// * @param operand2
	// * second value to be anded
	// * @return Result of logically ANDing the values passed in.
	// */
	// @Override
	// public byte logicalAnd(byte operand1, byte operand2) {
	//// boolean originalAuxCarryValue = ccr.isAuxilaryCarryFlagSet();
	// byte result = (byte) (operand1 & operand2);
	// ccr.setZSPclearCYandAUX(result);
	//// ccr.setAuxilaryCarryFlag(originalAuxCarryValue);
	// return result;
	// }// logicalAnd
	//
	// /**
	// * logicalOr - the two values are logically ORed and returned. It will set/reset the following flags :Zero, Sign
	// and
	// * Parity. The Carry flag is reset to 0. The Aux Carry flag is reset to 0
	// *
	// * @param operand1
	// * The first value to be ORed
	// * @param operand2
	// * The Second value to be ORed
	// * @return Result of logically ORing the values passed in.
	// */
	// @Override
	// public byte logicalOr(byte operand1, byte operand2) {
	//// boolean originalAuxCarryValue = ccr.isAuxilaryCarryFlagSet();
	// byte result = (byte) (operand1 | operand2);
	// ccr.setZSPclearCYandAUX(result);
	//// ccr.setAuxilaryCarryFlag(originalAuxCarryValue);
	// return result;
	// }// logicalOr
	//
	// /**
	// * logicalXor - the two values are logically exclusively ORed and returned. It will set/reset the following flags
	// * :Zero, Sign and Parity. The Carry flag is reset to 0. The Aux Carry flag is reset to 0
	// *
	// * @param operand1
	// * The first value to be exclusively ORed
	// * @param operand2
	// * The Second value to be exclusively ORed
	// * @return Result of logically exclusively ORed the values passed in.
	// */
	// @Override
	// public byte logicalXor(byte operand1, byte operand2) {
	// byte result = (byte) (operand1 ^ operand2);
	// ccr.setZSPclearCYandAUX(result);
	// return result;
	// }// logicalXor
	//
	// /**
	// * rotateRight - The carry bit is set equal to the low-order bit of the source value. The source value bits are
	// * rotated one position to the right, with the low order bit being transferred to the high-order bit position of
	// the
	// * resulting value. It will set/reset the following flags: Carry
	// *
	// * @param source
	// * Value to be rotated
	// * @return result of rotating the source right.
	// */
	// @Override
	// public byte rotateRight(byte source) {
	// return rotateRight(source, false);
	// }// rotateRight
	//
	// /**
	// * rotateRightThruCarry - The source value bits are rotated one position to the right, with the low order bit
	// * replacing the carry, while the carry bit replaces the high-order bit position of the resulting value.It will
	// * set/reset the following flags: Carry
	// *
	// * @param source
	// * Value to be rotated
	// * @return result of rotating the source right.
	// */
	// @Override
	// public byte rotateRightThruCarry(byte source) {
	// return rotateRight(source, true);
	// }// rotateRightThruCarry
	//
	// /**
	// * rotateRight - handle both types of rotate( thru carry and not thru carry)
	// *
	// * @param source
	// * value to be rotated right
	// * @param thruCarry
	// * flag that signals to rotate thru the Carry flag or not
	// * @return result of the rotate
	// */
	//
	// private byte rotateRight(byte source, boolean thruCarry) {
	// boolean oldFlag = ccr.isCarryFlagSet();
	// boolean originalBit0Set = ((source & 0X01) != 0) ? true : false;
	//
	// ccr.setCarryFlag(originalBit0Set); // Set CY = original LSB
	// int s = (source >> 1) & 0X7F; // shift value 1 position to the right
	// if (thruCarry) { // rotate thru carry
	// s = oldFlag ? (s | 0X80) : s & 0X7F;
	// } else {// not rotate thru carry
	// s = originalBit0Set ? (s | 0X80) : s & 0X7F;
	// }
	// return (byte) (s & 0XFF);
	// }// private rotateRight
	//
	// /**
	// * rotateLeft - The carry bit is set equal to the low-order bit of the source value. The source value bits are
	// * rotated one position to the left, with the high order bit being transferred to the low-order bit position of
	// the
	// * resulting value. It will set/reset the following flags: Carry
	// *
	// * @param source
	// * Value to be rotated
	// * @return result of rotating the source left.
	// */
	// @Override
	// public byte rotateLeft(byte source) {
	// return rotateLeft(source, false);
	// }// rotateLeft
	//
	// /**
	// * rotateLeftThruCarry - The source value bits are rotated one position to the right, with the low order bit
	// * replacing the carry, while the carry bit replaces the high-order bit position of the resulting value. It will
	// * set/reset the following flags: Carry
	// *
	// * @param source
	// * Value to be rotated
	// * @return result of rotating the source left.
	// */
	// @Override
	// public byte rotateLeftThruCarry(byte source) {
	// return rotateLeft(source, true);
	// }// rotateLeft
	//
	// private byte rotateLeft(byte source, boolean thruCarry) {
	// boolean oldFlag = ccr.isCarryFlagSet();
	// boolean originalBit7Set = ((source & 0X80) != 0) ? true : false;
	//
	// int s = (int) source << 1;
	// ccr.setCarryFlag(originalBit7Set);
	// if (thruCarry) { // rotate thru carry
	// s = oldFlag ? (s | 0X01) : s & 0XFE;
	// } else {
	// s = originalBit7Set ? (s | 0X01) : s & 0XFE;
	// }// if for Bit0
	//
	// return (byte) (s & 0XFF);
	// }// rotateLeft(byte source, boolean thruCarry)
	//
	// /**
	// * complement - does a one's-complement of the value passed in. It will set/reset the following flags: None
	// *
	// * @param value
	// * value to be complemented
	// * @return results of one's-complementing the original value
	// */
	// /**
	// * decimalAdjustByte - the eight-bit hexadecimal number in the passed in is adjusted to form two four-bit
	// * binary-coded decimal digits. It will set/reset the following flags: Carry, Aux Carry,Zero, Sign and Parity
	// *
	// * @param value
	// * - 8-bit hex value to be converted into bcd digits
	// * @return binary-coded-decimal representation of input value
	// */
	// @Override
	// public byte decimalAdjustByte(byte value) {
	// byte loNibble = getLoNibble(value);
	// byte ans = value;
	//
	// // save both Carry and Aux state on entry
	// boolean auxCarryTemp = ccr.isAuxFlagSet();
	// boolean carryTemp = ccr.isCarryFlagSet();
	//
	// if ((loNibble > 9) || auxCarryTemp) {
	// ans = this.add(value, (byte) 0X06);
	// auxCarryTemp = ccr.isAuxFlagSet(); // remember the Aux flag for exit
	// }// if
	//
	// byte hiNibble = (byte) ((ans & 0XF0) >> 4);
	// if ((hiNibble > 9) || carryTemp) {// carry when we entered the operation
	// ans = this.add(ans, (byte) 0X60);
	// }//if
	// ccr.setAuxFlag(auxCarryTemp);
	// ccr.setZSP(ans);
	// return ans;
	// }// decimalAdjustByte
	//
	// private byte getLoNibble(byte value) {
	// return (byte) (value & 0X0F);
	// }// getLoNibble

	// @Override
	public byte complement(byte value) {
		ccr.setNFlag(true);
		ccr.setHFlag(true);
		return (byte) (~value & 0XFF);
	}// complement

	//
	// /**
	// * Subtract performs subtraction on bytes or words depending on the parameter - wordSize. The operandSize (8/16)
	// * controls the type of subtraction as well as what Carry flags are calculated and set/reset.Only a Byte add will
	// * calculate the AUX Carry. The Carry flag is always calculated.
	// *
	// * @param minuend
	// * The value to be reduced
	// * @param subtrahend
	// * The value to diminish the minuend by.
	// * @param wordSize
	// * Determines if it is an 8-bit(MASK_BYTE) or 16-bit(MASK_WORD) operation
	// * @return
	// */
	// private int subtract(int minuend, int subtrahend, int wordSize) {
	// boolean carryFromAddingOne,auxCarryFromAddingOne;
	// int takeAway = add(~subtrahend, 1, wordSize);
	// carryFromAddingOne = ccr.isCarryFlagSet();
	// auxCarryFromAddingOne = ccr.isAuxFlagSet();
	//
	// int result = add(minuend, takeAway, wordSize);
	//
	//// boolean auxC = ccr.isAuxilaryCarryFlagSet() || auxCarryFromAddingOne;
	// ccr.setAuxFlag(ccr.isAuxFlagSet() || auxCarryFromAddingOne); // set if both either are set
	// ccr.setCarryFlag(!ccr.isCarryFlagSet() & !carryFromAddingOne); // only set if both are not set
	// return result;
	// }// subtract(int minuend, int subtrahend, int wordSize)

	// /**
	// * subtract - will return a byte value that is the difference between the minuend and subtrahend. It It will
	// * set/reset the following flags :Carry, Aux Carry,Zero, Sign and Parity
	// *
	// * @param minuend
	// * The byte value to be subtracted from
	// * @param subtrahend
	// * The byte value to reduce the minuend by
	// * @return the byte result of subtracting subtrahend from the minuend
	// */
	// @Override
	// public byte subtract(byte minuend, byte subtrahend) {
	// byte result = (byte) subtract((int) minuend, (int) subtrahend, MASK_BYTE);
	// ccr.setZSP(result);
	// return result;
	// }// subtract(byte minuend,byte subtrahend)
	//
	// /**
	// * subtract - will return a word (16 bit) value that is the difference between the minuend and subtrahend. It will
	// * set/reset the following flags : None
	// *
	// * @param minuend
	// * The int value to be subtracted from
	// * @param subtrahend
	// * The int value to reduce the minuend by
	// * @return the word (16 bit)result of subtracting subtrahend from the minuend
	// */
	// @Override
	// public int subtract(int minuend, int subtrahand) {
	// byte conditionCodesPrior = ccr.getConditionCode();
	// int ans = subtract(minuend, subtrahand, MASK_WORD);
	// ccr.setConditionCode(conditionCodesPrior);
	// return ans & MASK_WORD;
	// }// subtract(short minuend,short subtrahend)
	//
	// /**
	// * subtract - will return a byte value that is the difference between the minuend and the result of adding the
	// Carry
	// * flag to the subtrahend. It will set/reset the following flags :Carry, Aux Carry,Zero, Sign and Parity
	// *
	// * @param minuend
	// * The byte value to be subtracted from
	// * @param subtrahend
	// * The byte value to reduce the minuend by
	// * @return the byte result of subtracting subtrahend from the minuend
	// */
	// @Override
	// public byte subtractWithBorrow(byte minuend, byte subtrahand) {
	// int carryValue = ccr.isCarryFlagSet() ? 1 : 0; // get carry value
	// subtrahand = (byte) this.add(subtrahand, carryValue, MASK_BYTE);// add to subtrahend
	// return subtract(minuend, subtrahand);
	// }// subtractWithBorrow
	//

	private static final int CARRY_AUX = 0X000F;
	private static final int CARRY_BYTE = 0X00FF;
	private static final int CARRY_WORD = 0XFFFF;

	private static final int MASK_BYTE = 0X00FF;
	private static final int MASK_WORD = 0XFFFF;
	private static final byte MASK_SIGN_BYTE = (byte) 0X080;
	private static final byte MASK_SIGN_WORD = (byte) 0X08000;

}// class ArithmeticUnit
