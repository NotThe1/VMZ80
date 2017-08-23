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
	int immediateByte;
	int indexDisplacement; // used for IXY+d

	public Instruction() {
		int currentLocation = wrs.getProgramCounter();
		byte opCode0 = cpuBuss.read(currentLocation);
		byte opCode1 = cpuBuss.read(currentLocation + 1);

		switch (opCode0) {
		case (byte) 0X0ED: // Extended Instructions
			setMembers(opCode1);
			switch (this.page) {
			// case 0: // ED page 0. there are no Page 0 instructions for ED
			// break;
			case 1: // ED page 1
				switch (this.zzz) {
				case 0:// ED Page= 1 ZZZ= 0
					this.singleRegister1 = Z80.singleRegisters[this.yyy];
					break;
				case 1:// ED Page= 1 ZZZ= 1
					this.singleRegister1 = Z80.singleRegisters[this.yyy];
					break;
				case 2:// ED Page= 1 ZZZ= 2
					this.dd = this.yyy >> 1;
					this.doubleRegister1 = Z80.Register.HL;
					this.doubleRegister2 = Z80.doubleRegisters1[this.dd];
					break;
				case 3:// ED Page= 1 ZZZ= 3
					this.dd = this.yyy >> 1;
					this.doubleRegister1 = Z80.doubleRegisters1[this.dd];
					this.immediateWord = cpuBuss.readWordReversed(wrs.getProgramCounter() + 2);
					break;
				// case 4: // ED Page= 1 ZZZ= 4
				// break;
				// case 5: // ED Page= 1 ZZZ= 5
				// break;
				// case 6: // ED Page= 1 ZZZ= 6
				// break;
				// case 7: // ED Page= 1 ZZZ= 7
				// break;

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
			this.doubleRegister1 = opCode0 == (byte) 0XDD ? Z80.Register.IX : Z80.Register.IY;
			this.dd = this.yyy >> 1;
			this.doubleRegister2 = Z80.doubleRegisters1[this.dd];
			this.doubleRegister2 = doubleRegister2.equals(Z80.Register.HL) ? doubleRegister1 : doubleRegister2;
			switch (opCode1) {
			case (byte) 0X21: // LD IXY,nn
			case (byte) 0X22: // LD nn,IXY
			case (byte) 0X2A: // LD IXY,(nn)
				this.immediateWord = cpuBuss.readWordReversed(wrs.getProgramCounter() + 2);
				break;

			case (byte) 0X34: // INC (IXY + d)
			case (byte) 0X35: // DEC (IXY + d)
				this.indexDisplacement = cpuBuss.read(wrs.getProgramCounter() + 2);
				break;

			case (byte) 0X36: // DEC (IXY + d)
				this.indexDisplacement = cpuBuss.read(wrs.getProgramCounter() + 2);
				this.immediateByte = cpuBuss.read(wrs.getProgramCounter() + 3);
				break;

			case (byte) 0X70: // LD (IXY + d),B
			case (byte) 0X71: // LD (IXY + d),C
			case (byte) 0X72: // LD (IXY + d),D
			case (byte) 0X73: // LD (IXY + d),E
			case (byte) 0X74: // LD (IXY + d),H
			case (byte) 0X75: // LD (IXY + d),L
			case (byte) 0X76: // LD (IXY + d),M
			case (byte) 0X77: // LD (IXY + d),A
				this.singleRegister1 = Z80.singleRegisters[zzz];
				this.indexDisplacement = cpuBuss.read(wrs.getProgramCounter() + 2);
				break;
			case (byte) 0X46: // LD B,(IXY + d)
			case (byte) 0X4E: // LD C,(IXY + d)
			case (byte) 0X56: // LD D,(IXY + d)
			case (byte) 0X5E: // LD E,(IXY + d)
			case (byte) 0X66: // LD H,(IXY + d)
			case (byte) 0X6E: // LD L,(IXY + d)
			case (byte) 0X7E: // LD L,(IXY + d)
				this.singleRegister1 = Z80.singleRegisters[yyy];
				this.indexDisplacement = cpuBuss.read(wrs.getProgramCounter() + 2);
				break;
			case (byte) 0X86: // ADD r,(IXY + d)
			case (byte) 0X8E: // ADC r,(IXY + d)
			case (byte) 0X96: // SUB (IXY + d)
			case (byte) 0X9E: // SBC r,(IXY + d)
			case (byte) 0XA6: // AND (IXY + d)
			case (byte) 0XAE: // XOR (IXY + d)
			case (byte) 0XB6: // OR (IXY + d)
			case (byte) 0XBE: // CP (IXY + d)
				this.singleRegister1 = Z80.Register.A; // Acc
				this.indexDisplacement = cpuBuss.read(wrs.getProgramCounter() + 2);
				break;

			case (byte) 0X0CB: // Bit Instructions for IXY
				bit = cpuBuss.read(wrs.getProgramCounter() + 3) >> 3 & 0X0F;

				// if (this.page != 0) {
				// this.bit = this.yyy;
				// } // if page NOT 0
				break;

			}// switch opCode1
			break;
		// end of IXY instructions
		default:
			setMembers(opCode0);
			switch (this.page) {
			case 0: // page 00
				this.dd = this.yyy >> 1;
				this.doubleRegister1 = Z80.doubleRegisters1[dd];
				this.singleRegister1 = Z80.singleRegisters[yyy];
				switch (this.zzz) {
				case 0:
					this.immediateByte = cpuBuss.read(wrs.getProgramCounter() + 1);
					break;
				case 1:
					this.immediateWord = cpuBuss.readWordReversed(wrs.getProgramCounter() + 1);
					break;
				case 2:
					this.immediateWord = cpuBuss.readWordReversed(wrs.getProgramCounter() + 1);
					break;
				// case 3:
				// break;
				// case 4:
				// break;
				// case 5:
				// break;
				case 6:
					this.immediateByte = cpuBuss.read(wrs.getProgramCounter() + 1);
					break;
				// case 7:
				// break;
				}// switch zzz
				break;

			case 1: // page 01
				this.singleRegister1 = Z80.singleRegisters[yyy]; // Destination
				this.singleRegister2 = Z80.singleRegisters[zzz]; // Source
				break;

			case 2: // page 10
				this.singleRegister1 = Z80.singleRegisters[zzz]; // Source
				break;

			case 3: // page 11
				switch (zzz) {
				case 0: // 000 Conditional RETURN
					conditionCode = Z80.conditionCode[yyy];
					break;
				case 1: // 001 POP rr
					boolean bit3 = ((this.opCode0 & Z80.BIT_3) == Z80.BIT_3);
					if (!bit3) {
						this.dd = this.yyy >> 1;
						this.doubleRegister1 = Z80.doubleRegisters2[dd];
					} // if not bit3
					break;
				case 2: // 010 Conditional JUMP
					this.immediateWord = cpuBuss.readWordReversed(wrs.getProgramCounter() + 1);
					conditionCode = Z80.conditionCode[yyy];
					break;
				case 3: // JUMP
					this.immediateWord = cpuBuss.readWordReversed(wrs.getProgramCounter() + 1);
					break;
				case 4: // 100 Conditional CALL
					this.immediateWord = cpuBuss.readWordReversed(wrs.getProgramCounter() + 1);
					conditionCode = Z80.conditionCode[yyy];
					break;
				case 5: // 101 PUSH rr
					bit3 = ((this.opCode0 & Z80.BIT_3) == Z80.BIT_3);
					if (!bit3) {
						this.dd = this.yyy >> 1;
						this.doubleRegister1 = Z80.doubleRegisters2[dd];
					} // if not bit3
					break;
				case 6:
					this.immediateByte = cpuBuss.read(wrs.getProgramCounter() + 1);
					break;
				// case 7: // RST
				// break;
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

	public WorkingRegisterSet getWrs() {
		return wrs;
	}

	public CpuBuss getCpuBuss() {
		return cpuBuss;
	}

	public byte getOpCode0() {
		return opCode0;
	}

	public byte getOpCode1() {
		return opCode1;
	}

	public int getPage() {
		return page;
	}

	public int getYyy() {
		return yyy;
	}

	public int getZzz() {
		return zzz;
	}

	public int getDd() {
		return dd;
	}

	public int getBit() {
		return bit;
	}

	public Register getSingleRegister1() {
		return singleRegister1;
	}

	public Register getSingleRegister2() {
		return singleRegister2;
	}

	public Register getDoubleRegister1() {
		return doubleRegister1;
	}

	public Register getDoubleRegister2() {
		return doubleRegister2;
	}

	public ConditionCode getConditionCode() {
		return conditionCode;
	}

	public int getImmediateWord() {
		return immediateWord;
	}

	public int getImmediateByte() {
		return immediateByte;
	}

	public int getIndexDisplacement() {
		return indexDisplacement;
	}
}// class OpCode
