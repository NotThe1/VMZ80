package utilities.inLineDisassembler;
/* @formatter:off */

public enum Z80Type {
	A99,
	A98,
	A97,
	A96,
	A95,
	A94,
	A93,
	A00,
	A01,
	A10,
	A11,
	A20,
	A21,
	A22,
	
	MAIN, EXTENDED, BIT, INDEX_IX, INDEX_IY, INDEX_BIT_IX, INDEX_BIT_IY
}// Z80InstructionType
// A99 no arguments - Instruction
// A98  1 argument  - Instruction	getDestination(), Source = "D8";
// A97  1 argument  - Instruction	Source = "D8";
// A96  1 argument  - Instruction	(getDestination()  + Source = "D8");  (IX+d)
// A95  2 arguments  - Instruction	(getDestination()  + Source = "D8"), N;  LD (IX+d),n
// A94  1 arguments  - Instruction	getDestination(),   (getSource + arg);  LD R,(IX+d)
// A93  1 arguments  - Instruction	(getDestination()  + arg), geSource;  LD (IX+d),n


// A00 no arguments - Instruction,Destination
// A01 no arguments - Instruction,Destination,Source

// A10 1 argument - Instruction, Arg1
// A11 1 argument - Instruction, Destination, Arg1

// A20	2 arguments - Instruction, Arg2, Arg1, Source ; Destination = 'addr' ()
// A21	2 arguments - Instruction, Destination,Arg1, Arg1  ; Source = 'addr' ()
// A22	2 arguments - Instruction, Destination,Arg1, Arg1  ; no address indicator

/* @formatter:on  */