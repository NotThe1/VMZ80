package hardware;

public interface IArithmeticUnit {
	/**
	 * add - will sum the two supplied byte values and return a byte value. It will set/reset the following flags :
	 * Carry, Aux Carry,Zero, Sign and Parity
	 * 
	 * @param operand1
	 *            First byte value to be added
	 * @param operand2
	 *            Second byte value to be added
	 * @return the byte sum of the two values provided as parameters
	 */
	public byte add(byte operand1, byte operand2);

	/**
	 * add - will sum the two supplied int values and return a word (16bit) value. It will set/reset the following flags
	 * : Carry, Aux Carry,Zero, Sign and Parity
	 * 
	 * @param operand1
	 *            First int value to be added
	 * @param operand2
	 *            Second int value to be added
	 * @return the word(16 bite) sum of the two values provided as parameters
	 */
	public int add(int operand1, int operand2);

	/**
	 * addWithCarry - will add the two supplied int values and the carryFlag value and return a byte value. It will
	 * set/reset the following flags : Carry, Aux Carry,Zero, Sign and Parity
	 * 
	 * @param operand1
	 *            First int value to be added
	 * @param operand2
	 *            Second int value to be added
	 * @return the word(16 bite) sum of the two values provided as parameters
	 */
	public byte addWithCarry(byte operand1, byte operand2);

	/**
	 * subtract - will return a byte value that is the difference between the minuend and subtrahend. It It will
	 * set/reset the following flags :Carry, Aux Carry,Zero, Sign and Parity
	 * 
	 * @param minuend
	 *            The byte value to be subtracted from
	 * @param subtrahend
	 *            The byte value to reduce the minuend by
	 * @return the byte result of subtracting subtrahend from the minuend
	 */
	public byte subtract(byte minuend, byte subtrahend);

	/**
	 * subtract - will return a word (16 bit) value that is the difference between the minuend and subtrahend. It will
	 * set/reset the following flags :None
	 * 
	 * @param minuend
	 *            The int value to be subtracted from
	 * @param subtrahend
	 *            The int value to reduce the minuend by
	 * @return the word (16 bit)result of subtracting subtrahend from the minuend
	 */
	public int subtract(int minuend, int subtrahand);

	/**
	 * subtract - will return a byte value that is the difference between the minuend and the result of adding the Carry
	 * flag to the subtrahend. It will set/reset the following flags :Carry, Aux Carry,Zero, Sign and Parity
	 * 
	 * @param minuend
	 *            The byte value to be subtracted from
	 * @param subtrahend
	 *            The byte value to reduce the minuend by
	 * @return the byte result of subtracting subtrahend from the minuend
	 */
	public byte subtractWithBorrow(byte minuend, byte subtrahand);

	/**
	 * increment - will increment the passed byte value by one. It will set/reset the following flags : Aux Carry,Zero,
	 * Sign and Parity. Note the Carry flag remains unchanged!
	 * 
	 * @param value
	 *            byte value to be incremented
	 * @return byte result of incrementing input value.
	 */
	public byte increment(byte value);

	/**
	 * increment - will increment the passed int value by one. It will set/reset the following flags : None
	 * 
	 * @param value
	 *            value to be incremented
	 * @return result of incrementing input value.
	 */
	public int increment(int value);

	/**
	 * decrement - will decrement the passed byte value by one. It will set/reset the following flags : Aux Carry,Zero,
	 * Sign and Parity. Note the Carry flag remains unchanged!
	 * 
	 * @param value
	 *            byte value to be decremented
	 * @return byte result of decrementing input value.
	 */
	public byte decrement(byte value);

	/**
	 * decrement - will decrease the passed int value by one. It will set/reset the following flags : None
	 * 
	 * @param value
	 *            value to be decremented
	 * @return result of decrementing input value.
	 */
	public int decrement(int value);

	/**
	 * logicalAnd - the two values are logically ANDed and returned. It will set/reset the following flags :Zero, Sign
	 * and Parity. The Carry flag is reset to 0. The Aux Carry flag is reset to 0
	 * 
	 * @param operand1
	 *            first value to be anded
	 * @param operand2
	 *            second value to be anded
	 * @return Result of logically ANDing the values passed in.
	 */
	public byte logicalAnd(byte operand1, byte operand2);

	/**
	 * logicalOr - the two values are logically ORed and returned. It will set/reset the following flags :Zero, Sign and
	 * Parity. The Carry flag is reset to 0. The Aux Carry flag is reset to 0
	 * 
	 * @param operand1
	 *            The first value to be ORed
	 * @param operand2
	 *            The Second value to be ORed
	 * @return Result of logically ORing the values passed in.
	 */
	public byte logicalOr(byte operand1, byte operand2);

	/**
	 * logicalXor - the two values are logically exclusively ORed and returned. It will set/reset the following flags
	 * :Zero, Sign and Parity. The Carry flag is reset to 0. The Aux Carry flag is reset to 0
	 * 
	 * @param operand1
	 *            The first value to be exclusively ORed
	 * @param operand2
	 *            The Second value to be exclusively ORed
	 * @return Result of logically exclusively ORed the values passed in.
	 */
	public byte logicalXor(byte operand1, byte operand2);

	/**
	 * rotateRight - The carry bit is set equal to the low-order bit of the source value. The source value bits are
	 * rotated one position to the right, with the low order bit being transferred to the high-order bit position of the
	 * resulting value. It will set/reset the following flags: Carry
	 * 
	 * @param source
	 *            Value to be rotated
	 * @return result of rotating the source right.
	 */
	public byte rotateRight(byte source);

	/**
	 * rotateRightThruCarry - The source value bits are rotated one position to the right, with the low order bit
	 * replacing the carry, while the carry bit replaces the high-order bit position of the resulting value.It will
	 * set/reset the following flags: Carry
	 * 
	 * @param source
	 *            Value to be rotated
	 * @return result of rotating the source right.
	 */
	public byte rotateRightThruCarry(byte source);

	/**
	 * rotateLeft - The carry bit is set equal to the low-order bit of the source value. The source value bits are
	 * rotated one position to the left, with the high order bit being transferred to the low-order bit position of the
	 * resulting value. It will set/reset the following flags: Carry
	 * 
	 * @param source
	 *            Value to be rotated
	 * @return result of rotating the source left.
	 */
	public byte rotateLeft(byte source);

	/**
	 * rotateLeftThruCarry - The source value bits are rotated one position to the right, with the low order bit
	 * replacing the carry, while the carry bit replaces the high-order bit position of the resulting value. It will
	 * set/reset the following flags: Carry
	 * 
	 * @param source
	 *            Value to be rotated
	 * @return result of rotating the source left.
	 */
	public byte rotateLeftThruCarry(byte source);

	/**
	 * complement - does a one's-complement of the value passed in. It will set/reset the following flags: None
	 * 
	 * @param value
	 *            value to be complemented
	 * @return results of one's-complementing the original value
	 */
	public byte complement(byte value);

	/**
	 * decimalAdjustByte - the eight-bit hexadecimal number in the passed in is adjusted to form two four-bit
	 * binary-coded decimal digits. It will set/reset the following flags: Carry, Aux Carry,Zero, Sign and Parity
	 * 
	 * @param value - 8-bit hex value to be converted into bcd digits
	 * @return	binary-coded-decimal representation of input value
	 */
	public byte decimalAdjustByte(byte value);

}// interface IArithmeticUnit
