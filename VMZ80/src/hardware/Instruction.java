package hardware;

import codeSupport.Z80;
import codeSupport.Z80.ConditionCode;
import codeSupport.Z80.Register;
import memory.CpuBuss;

/*
 * Instruction will parse the opcode into it constituent elements
 * 
 */

public class Instruction {
	WorkingRegisterSet wrs = WorkingRegisterSet.getInstance();
	CpuBuss cpuBuss = CpuBuss.getInstance();
	byte opCode0;
	byte opCode1;
	byte opCode2;
	int page;
	int yyy;
	int zzz;
	int dd;
	// boolean bit3;
	int bit = -1;
	Register singleRegister1; // if two registers, the destination
	Register singleRegister2; // if two registers, the source
	Register doubleRegister1; // if two registers, the destination
	Register doubleRegister2; // if two registers, the source
	ConditionCode conditionCode; // used by CALL and JUMP
	int immediateWord;
	byte immediateByte;
	byte indexDisplacement; // used for IXY+d
	int indexRegisterContents; // used for IXY + d;

	public Instruction() {
		int currentLocation = wrs.getProgramCounter();
		opCode0 = cpuBuss.read(currentLocation);
		opCode1 = cpuBuss.read(currentLocation + 1);

		switch (opCode0) {
		case (byte) 0X0ED: // Extended Instructions
			setMembers(opCode1);
			switch (this.page) {
			// case 0: // ED page 0. there are no Page 0 instructions for ED
			// break;
			case 1: // ED (40- 7F)page 1
				switch (this.zzz) {
				case 0:// ED (40,48,50,58,60,68,70,78) - IN r,(C)
					this.singleRegister1 = Z80.singleRegisters[this.yyy];
					this.singleRegister2 = Z80.Register.C;
					break;
				case 1:// ED (41,49,51,59,61,69,71,79) - OUT r,(C)
					this.singleRegister1 = Z80.Register.C;
					this.singleRegister2 = Z80.singleRegisters[this.yyy];
					break;
				case 2:// ED (42,52,62,72) - SBC HL,rr |ED (4A,5A,6A,7A) - ADC HL,rr
					this.dd = this.yyy >> 1;
					this.doubleRegister1 = Z80.Register.HL;
					this.doubleRegister2 = Z80.doubleRegisters1[this.dd];
					break;
				case 3:// ED (43,4B,53,5B,63,6B,73,7B) - LD (nn),rr & LD rr,(nn)
					this.dd = this.yyy >> 1;
					this.doubleRegister1 = Z80.doubleRegisters1[this.dd];
					this.immediateWord = cpuBuss.readWordReversed(wrs.getProgramCounter() + 2);
					break;
				case 4: // ED (44,4C,54,5C,64,6C,74,7C) - NEG and unused
					this.singleRegister1 = Z80.Register.A;
					break;
				// case 5: // ED (45,4D,55,5D,65,6D,75,7D) - RETN
				// break;
				case 6: // ED (46,4E,56,5E,66,6E,76,7E) - Page= 1 ZZZ= 6
					switch (this.yyy) {
					case 0:// 46 IM 0
					case 4:// 66 IM 0
						this.immediateByte = (byte) 0X00;
						break;
					case 2:// 56 IM 1
					case 6:// 76 IM 1
						this.immediateByte = (byte) 0X01;
						break;
					case 3:// 5E IM 2
					case 7:// 7E IM 2
						this.immediateByte = (byte) 0X02;
						break;
					}// switch YYY
					break;
				case 7: // ED (47,4F,57,5F,67,6F,77,7F) - Page= 1 ZZZ= 7
					switch (this.yyy) {
					case 0: // 47 LD I,A
						this.singleRegister1 = Z80.Register.I;
						this.singleRegister2 = Z80.Register.A;
						break;
					case 2: // 57 LD A,I
						this.singleRegister1 = Z80.Register.A;
						this.singleRegister2 = Z80.Register.I;
						break;

					case 1: // 4F LD R,A
						this.singleRegister1 = Z80.Register.R;
						this.singleRegister2 = Z80.Register.A;
						break;
					case 3: // 5F LD A,R
						this.singleRegister1 = Z80.Register.A;
						this.singleRegister2 = Z80.Register.R;
						break;

					case 4: // 67 RRD
					case 5: // F7 RLD
						this.singleRegister1 = Z80.Register.A;
						this.doubleRegister1 = Z80.Register.HL;
					}// switch YYY
					break;

				}// this zzz ED page 1
				break;
			// case 2:// ED page 2
			// break;
			// case 3:// ED page 3
			// break;

			}// switch ED Page
			break;
		// end of ED instructions

		case (byte) 0X0CB: // Bit Instructions
			setMembers(opCode1);
			this.singleRegister1 = Z80.singleRegisters[this.zzz];
			if (this.page != 0) {
				this.bit = this.yyy;
			} // if NOT page 0
			break;
		// end of CB instructions

		case (byte) 0X0DD: // IX Instructions
		case (byte) 0X0FD: // IY Instructions
			Z80.Register activeIndexRegister = opCode0 == (byte) 0XDD ? Z80.Register.IX : Z80.Register.IY;
			indexRegisterContents = wrs.getDoubleReg(activeIndexRegister);
			// this.doubleRegister1 = opCode0 == (byte) 0XDD ? Z80.Register.IX : Z80.Register.IY;
			setMembers(opCode1);
			// this.dd = this.yyy >> 1;
			// this.doubleRegister2 = Z80.doubleRegisters1[this.dd];
			// this.doubleRegister2 = doubleRegister2.equals(Z80.Register.HL) ? doubleRegister1 : doubleRegister2;
			switch (opCode1) {
			case (byte) 0X09: // ADD IXY,BC
			case (byte) 0X19: // ADD IXY,DE
			case (byte) 0X39: // ADD IXY,SP
				this.doubleRegister1 = activeIndexRegister;
				this.doubleRegister2 = Z80.doubleRegisters1[this.yyy >> 1];
				break;
			case (byte) 0X29: // ADD IXY,IXY
				this.doubleRegister1 = activeIndexRegister;
				this.doubleRegister2 = activeIndexRegister;
				break;
			case (byte) 0X21: // LD IXY,nn
			case (byte) 0X22: // LD nn,IXY
			case (byte) 0X2A: // LD IXY,(nn)
				this.doubleRegister1 = activeIndexRegister;
				this.immediateWord = cpuBuss.readWordReversed(wrs.getProgramCounter() + 2);
				break;
			case (byte) 0X23: // INC IXY
			case (byte) 0X2B: // DEC IXY
				this.doubleRegister1 = activeIndexRegister;
				break;

			case (byte) 0X34: // INC (IXY + d)
			case (byte) 0X35: // DEC (IXY + d)
				this.doubleRegister1 = activeIndexRegister;
				this.indexDisplacement = cpuBuss.read(wrs.getProgramCounter() + 2);
				break;
			case (byte) 0X36: // LD (IXY + d),n
				this.doubleRegister1 = activeIndexRegister;
				this.indexDisplacement = cpuBuss.read(wrs.getProgramCounter() + 2);
				this.immediateByte = cpuBuss.read(wrs.getProgramCounter() + 3);
				break;

			case (byte) 0X70: // LD (IXY + d),B
			case (byte) 0X71: // LD (IXY + d),C
			case (byte) 0X72: // LD (IXY + d),D
			case (byte) 0X73: // LD (IXY + d),E
			case (byte) 0X74: // LD (IXY + d),H
			case (byte) 0X75: // LD (IXY + d),L
				// case (byte) 0X76: // LD (IXY + d),M
			case (byte) 0X77: // LD (IXY + d),A
				this.doubleRegister1 = activeIndexRegister;
				this.singleRegister1 = Z80.singleRegisters[zzz];
				this.indexDisplacement = cpuBuss.read(wrs.getProgramCounter() + 2);
				break;
			case (byte) 0X46: // LD B,(IXY + d)
			case (byte) 0X4E: // LD C,(IXY + d)
			case (byte) 0X56: // LD D,(IXY + d)
			case (byte) 0X5E: // LD E,(IXY + d)
			case (byte) 0X66: // LD H,(IXY + d)
			case (byte) 0X6E: // LD L,(IXY + d)
			case (byte) 0X7E: // LD A,(IXY + d)
				this.doubleRegister1 = activeIndexRegister;
				this.singleRegister1 = Z80.singleRegisters[yyy];
				this.indexDisplacement = cpuBuss.read(wrs.getProgramCounter() + 2);
				break;
			case (byte) 0X86: // ADD A,(IXY + d)
			case (byte) 0X8E: // ADC A,(IXY + d)
			case (byte) 0X96: // SUB (IXY + d)
			case (byte) 0X9E: // SBC A,(IXY + d)
			case (byte) 0XA6: // AND (IXY + d)
			case (byte) 0XAE: // XOR (IXY + d)
			case (byte) 0XB6: // OR (IXY + d)
			case (byte) 0XBE: // CP (IXY + d)
				this.doubleRegister1 = activeIndexRegister;
				this.singleRegister1 = Z80.Register.A; // Acc
				this.indexDisplacement = cpuBuss.read(wrs.getProgramCounter() + 2);
				break;

			case (byte) 0X0CB: // Bit Instructions for IXY
				opCode2 = cpuBuss.read(wrs.getProgramCounter() + 3);
				int zzz3 = opCode2 & 0X07;
				switch (zzz3) {
				case 6: // DDCB (m6 and mE)
					this.doubleRegister1 = activeIndexRegister;
					this.indexDisplacement = cpuBuss.read(wrs.getProgramCounter() + 2);
					bit = (cpuBuss.read(wrs.getProgramCounter() + 3) >> 3) & 0x07;
					break;
				}// switch zzz3

				break;

			case (byte) 0X0E1: // POP IXY
			case (byte) 0X0E5: // PUSH IXY
			case (byte) 0X0E9: // JP (IXY)
				this.doubleRegister1 = activeIndexRegister;
				break;

			case (byte) 0X0E3: // EX (SP),IXY
			case (byte) 0X0F9: // LD SP,IXY
				this.doubleRegister2 = Z80.Register.SP;
				this.doubleRegister1 = activeIndexRegister;
				break;

			}// switch opCode1
			break;
		// end of IXY instructions
		default: // Main instructions
			setMembers(opCode0);
			switch (this.page) {
			case 0: // page 00
				// this.dd = this.yyy >> 1;
				// this.doubleRegister1 = Z80.doubleRegisters1[dd];
				// this.singleRegister1 = Z80.singleRegisters[yyy];
				switch (this.zzz) {
				case 0: // 00,08,10,18,20,28,30,38
					switch (this.yyy) {
					// case 0: //00 NOP
					// break;
					// case 1:// 08 EX AF,AF'
					// break;
					case 2: // 10 DJNZ n
					case 3: // 18 JR n
						this.immediateByte = cpuBuss.read(wrs.getProgramCounter() + 1);
						break;
					case 4: // 20 JR NZ,n
					case 5: // 28 JR Z,n
					case 6: // 30 JR NC,n
					case 7: // 38 JR C,N
						this.immediateByte = cpuBuss.read(wrs.getProgramCounter() + 1);
						this.conditionCode = Z80.conditionCode[this.yyy & 0b011];
						break;
					}// switch yyy
					break;

				case 1: // 01,09,11,19,21,29,31,39
					if ((this.yyy & Z80.BIT_0) != Z80.BIT_0) {// 01,11,21,31 LD RR,nn
						this.immediateWord = cpuBuss.readWordReversed(wrs.getProgramCounter() + 1);
						this.doubleRegister1 = Z80.doubleRegisters1[this.yyy >> 1];
					} else {// 09,19,29,39 ADD HL,RR
						this.doubleRegister1 = Z80.Register.HL;
						this.doubleRegister2 = Z80.doubleRegisters1[this.yyy >> 1];
					} // if
					break;
				case 2: // 02,0A,12,1A,22,2A,32,3A
					switch (this.yyy) {
					case 0: // 02 LD (BC),A
					case 1: // 0A LD A,(BC)
						this.singleRegister1 = Z80.Register.A;
						this.doubleRegister1 = Z80.Register.BC;
						break;
					case 2: // 12 LD (DE),A
					case 3: // 1A LD A,(DE)
						this.singleRegister1 = Z80.Register.A;
						this.doubleRegister1 = Z80.Register.DE;
						break;
					case 4: // 22 LD (nn),HL
					case 5: // 2A LD HL,(NN)
						this.immediateWord = cpuBuss.readWordReversed(wrs.getProgramCounter() + 1);
						this.doubleRegister1 = Z80.Register.HL;
						break;
					case 6: // 32 LD (nn),A
					case 7: // 3A LD A,(NN)
						this.immediateWord = cpuBuss.readWordReversed(wrs.getProgramCounter() + 1);
						this.singleRegister1 = Z80.Register.A;
						break;
					}//// switch yyy
					this.immediateWord = cpuBuss.readWordReversed(wrs.getProgramCounter() + 1);
					break;
				case 3: // 03,0B,13,1B,23,2B,33,3B - INC rr  DEC rr
					this.doubleRegister1 = Z80.doubleRegisters1[this.yyy >> 1];
					break;
				case 4: // 04,0C,14,1C,24,2C,34,3C - INC r
				case 5: // 05,0D,15,1D,25,2D,35,3D - DEC r
					this.singleRegister1 = Z80.singleRegisters[yyy];
					break;
				case 6: // 06,0E,16,1E,26,2E,36,3E - LD r,n
					this.singleRegister1 = Z80.singleRegisters[yyy];
					this.immediateByte = cpuBuss.read(wrs.getProgramCounter() + 1);
					break;
				case 7: // 07,0F,17,1F,27,2F,37,3F
					switch (this.yyy) {
					case 0: // 07 RLCA
					case 1: // 0F RRCA
					case 2: // 17 RLA
					case 3: // 1F RRA
					case 4: // 27 DAA
					case 5: // 2F CPL
						this.singleRegister1 = Z80.Register.A;
						break;
					// case 6: // 37 SCF
					// case 7: // 3F CCF
					// break;
					}// switch yyy
					break;
				}// switch zzz
				break;

			case 1: // page 01
				this.singleRegister1 = Z80.singleRegisters[yyy]; // Destination
				this.singleRegister2 = Z80.singleRegisters[zzz]; // Source
				break;

			case 2: // page 10
				this.singleRegister1 = Z80.Register.A; // Destination
				this.singleRegister2 = Z80.singleRegisters[zzz]; // Source
				break;

			case 3: // page 11
				switch (this.zzz) {
				case 0: // 000 Conditional RETURN - C0,C8,D0,D8.E0.E8.F0.F8
					conditionCode = Z80.conditionCode[yyy];
					break;
				case 1: // C1,C9,D1,D9,E1,E9,F1,F9
					switch (this.yyy) {
					case 0:// C1 - POP BC
					case 2:// D1 - POP DE
					case 4:// E1 - POP HL
					case 6:// F1 - POP AF
						this.doubleRegister1 = Z80.doubleRegisters2[this.yyy >> 1];
						break;
					// case 1:// C9 - RET
					// break;
					// case 3: // D9 - EXX
					// break;
					case 5: // E9 - JP (HL)
						this.doubleRegister1 = Z80.Register.PC;
						this.doubleRegister2 = Z80.Register.HL;
						break;
					case 7: // F9 - LD SP,HL
						this.doubleRegister1 = Z80.Register.SP;
						this.doubleRegister2 = Z80.Register.HL;
						break;
					}// switch yyy
					break;
				case 2: // 010 Conditional JUMP C2,CA,D2,DA,E2,EA,F2,FA
					this.immediateWord = cpuBuss.readWordReversed(wrs.getProgramCounter() + 1);
					conditionCode = Z80.conditionCode[yyy];
					break;
				case 3: // JUMP
					switch (this.yyy) {
					case 0:// C3 JMP nn
						this.immediateWord = cpuBuss.readWordReversed(wrs.getProgramCounter() + 1);
						break;
					case 1:// CB - BITS
						break;
					case 2:// D3 - OUT (*),A
					case 3:// DB - IN A,(n)
						this.immediateByte = cpuBuss.read(wrs.getProgramCounter() + 1);
						this.singleRegister1 = Z80.Register.A;
						break;
					case 4:// E3 - EX (SP),HL
						this.doubleRegister1 = Z80.Register.SP;
						this.doubleRegister2 = Z80.Register.HL;
						break;
					case 5: // EB - EX DE,HL
						this.doubleRegister1 = Z80.Register.DE;
						this.doubleRegister2 = Z80.Register.HL;
						break;
					// case 6: // F3 - DI
					// case 7: // FB - EI
					// break;
					}// switch YYY
					break;
				case 4: // 100 Conditional CALL - C4,CC,D4,DC,E4,EC,F4,FC
					this.immediateWord = cpuBuss.readWordReversed(wrs.getProgramCounter() + 1);
					conditionCode = Z80.conditionCode[yyy];
					break;
				case 5: // 101 PUSH rr
					// bit3 = ((this.opCode0 & Z80.BIT_3) == Z80.BIT_3);
					// if (!bit3) {
					// this.dd = this.yyy >> 1;
					// this.doubleRegister1 = Z80.doubleRegisters2[dd];
					// } // if not bit3

					switch (this.yyy) {
					case 0:// C5 - PUSH BC
					case 2:// D5 - PUSH DE
					case 4:// E5 - PUSH HL
					case 6:// F5 - PUSH AF
						this.doubleRegister1 = Z80.doubleRegisters2[this.yyy >> 1];
						break;
					case 1:// CD - CALL NN
						this.immediateWord = cpuBuss.readWordReversed(wrs.getProgramCounter() + 1);

						break;
					// case 3: // DD - IX
					// case 5: // ED - EXTD
					// case 7: // FD - IY
					// break;
					}// switch yyy
					break;
				case 6:// Immediate instructions - C6,CE,D6,DE,E6,EE,F6,FE
					this.singleRegister1 = Z80.Register.A;
					this.immediateByte = cpuBuss.read(wrs.getProgramCounter() + 1);
					break;
				case 7: // RST - C7,CF,D7,DF,E7,EF,F7,FF
					this.immediateByte = (byte) (this.yyy << 3);
					break;
				} // switch zzz

				break;

			default:
				// // setError(ErrorStatus.INVALID_OPCODE);
			} // switch page
		}// switch opCode
	}// constructor

	private void setMembers(byte source) {
		this.page = (source >> 6) & 0X0003; // only want the value of bits 6 & 7
		this.yyy = (source >> 3) & 0X0007; // only want the value of bits 3,4 & 5
		this.zzz = source & 0X0007; // only want the value of bits 0,1 & 2
	}// setMembers

	// public WorkingRegisterSet getWrs() {
	// return wrs;
	// }//

	// public CpuBuss getCpuBuss() {
	// return cpuBuss;
	// }//

	public byte getOpCode0() {
		return opCode0;
	}// getOpCode0

	public byte getOpCode1() {
		return opCode1;
	}// getOpCode1

	public int getPage() {
		return page;
	}// getPage

	public int getYyy() {
		return yyy;
	}// getYyy

	public int getZzz() {
		return zzz;
	}// getZzz

	// public int getDd() {
	// return dd;
	// }

	public int getBit() {
		return bit;
	}// getBit

	public Register getSingleRegister1() {
		return singleRegister1;
	}// getSingleRegister1

	public Register getSingleRegister2() {
		return singleRegister2;
	}// getSingleRegister2

	public Register getDoubleRegister1() {
		return doubleRegister1;
	}// getDoubleRegister1

	public Register getDoubleRegister2() {
		return doubleRegister2;
	}// getDoubleRegister2

	public ConditionCode getConditionCode() {
		return conditionCode;
	}// getConditionCode

	public int getImmediateWord() {
		return immediateWord;
	}// getImmediateWord

	public byte[] getImmediateWordArray() {
		byte[] ans = new byte[] { (byte) 00, (byte) 00 };
		return ans;
	}// getImmediateWord

	public byte getImmediateByte() {
		return immediateByte;
	}// getImmediateByte

	public byte getIndexDisplacement() {
		return indexDisplacement;
	}// getIndexDisplacement

	public int getNetIndexValue() {
		int displacement = indexDisplacement;
		return displacement + indexRegisterContents;
	}// getNetIndexValue
}// class OpCode
