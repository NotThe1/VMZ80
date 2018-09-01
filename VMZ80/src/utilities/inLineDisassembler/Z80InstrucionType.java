package utilities.inLineDisassembler;
/* @formatter:off */

public enum Z80InstrucionType {
	
	I00,
	I01,
	I02,
	I10,
	
	I11,
	I12,
	I13,
	I14,
	I15,
	I16,
	
	I20,
	I21,
	I22,
	I23,
	I24,
	I25,
	I26,
	I27
}// Z80InstructionType


// I00	0 arguments		- Instruction								 [DJNZ]
// I01	0 arguments		- Instruction source						 [DEC B]
// I02  0 arguments     - Instruction source target					[LD(BC),A]

// I10	1 argument		- Instruction value1						[DJNZ 01H]
// I11	1 argument		- instruction target, value1				[LD B,02H]
// I12	1 argument		- instruction (value1H),Source				[OUT (03H),A]
// I13	1 argument		- Instruction (Source + value2H)			[INC (IX+04H)]
// I14	1 argument		- instruction (target+ value2H), Source		[LD (IX+05H),B]
// I15	1 argument		- instruction target,(Source + value2H)		[BIT 0,(IX+06H)]
// I16	1 argument		- instruction target,(value1)				[IN A,(08H)]

// I20	2 arguments		- instruction value2:value1H				[JP 0789AH]
// I21	2 arguments		- instruction target,value2:value1H			[LD BC,0BCDEH]
// I22	2 arguments		- instruction target,value3:value2H			[LD IX,0BCDEH]
// I23	2 arguments		- instruction (value3:value2H),source		[LD (0F012H),IX]
// I25	2 arguments		- instruction (value2:value1H),source		[LD (03456H),HL]
// I25	2 arguments		- instruction target,(value3:value2H)		[LD IY,(0789AH)]
// I26	2 arguments		- instruction target,(value2:value1H)		[LD HL,(0BCDEH)]
// I27	2 arguments		- instruction (target + value2),value3		[LD (IX+0EH),00]



/* @formatter:on  */