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
	A92,
	A91,
	A90,
	A89,
	A88,
	A87,
	A86,
	A85,
	A00,
	A01
	

}// Z80InstructionType
// A99  0 arguments  - Instruction
// A98  1 argument   - Instruction	getDestination(), Source = "D8";
// A97  1 argument   - Instruction	Source = "D8";
// A96  1 argument   - Instruction	(getDestination()  + Source = "D8");  (IX+d)
// A95  2 arguments  - Instruction	(getDestination()  + Source = "D8"), N;  LD (IX+d),n
// A94  1 arguments  - Instruction	getDestination(),   (getSource + arg);  LD R,(IX+d)
// A93  1 arguments  - Instruction	(getDestination()  + arg), geSource;  LD (IX+d),n
// A92	0 arguments	 - Instruction   getDestintion(),getSource() ;   BIT 0,B
// A91  2 arguments  - Instruction (value3value2), getSource() ; LD (1234),BC
// A90  2 arguments  - Instruction  getDestiation(),(value3value2) ; LD BC,(1234)
// A89  2 arguments  - Instruction  getDestiation(),value3value2 ; LD BC,1234
// A88  2 arguments  - Instruction  value3value2 ;  JP xxyy
// A87	2 arguments see A91 using values 1 & 2
// A86	2 arguments see A90 using values 1 & 2
// A85  2 arguments  - Instruction  getDestiation(),(getSource() + value2) ;  Bit 4,(IX+5)

// A00 no arguments - Instruction, Destination
// A01  1 argument	-	Instruction (value1),getSource()   ;  OUT(3),A



// A01 no arguments - Instruction,Destination,Source

// A10 1 argument - Instruction, Arg1
// A11 1 argument - Instruction, Destination, Arg1

// A20	2 arguments - Instruction, Arg2, Arg1, Source ; Destination = 'addr' ()
// A21	2 arguments - Instruction, Destination,Arg1, Arg1  ; Source = 'addr' ()
// A22	2 arguments - Instruction, Destination,Arg1, Arg1  ; no address indicator
/*
 * 	A01,
	A10,
	A11,
	A20,
	A21,
	A22,
	
	MAIN, EXTENDED, BIT, INDEX_IX, INDEX_IY, INDEX_BIT_IX, INDEX_BIT_IY
 */

/* @formatter:on  */