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
	ConditionCode conditionCode;	// used by CALL and JUMP
	public Instruction() {
		int currentLocation = wrs.getProgramCounter();
		byte opCode0 = cpuBuss.read(currentLocation);
		byte opCode1 = cpuBuss.read(currentLocation + 1);

		switch (opCode0) {
		case (byte) 0X0ED: // Extended Instructions
			setMembers(opCode1);
			this.singleRegister1 = Z80.singleRegisters[this.yyy];
			this.dd = this.yyy >> 1;
			this.doubleRegister1 = Z80.doubleRegisters1[this.dd];
			break;
		case (byte) 0X0CB: // Bit Instructions
			setMembers(opCode1);
			this.singleRegister1 = Z80.singleRegisters[this.zzz];
			if (this.page != 0) {
				this.bit = this.yyy;
			} // if page NOT 0
			break;
		case (byte) 0X0DD: // IX Instructions
		case (byte) 0X0FD: // IY Instructions
			this.doubleRegister1 = opCode0 == (byte) 0XDD ? Z80.Register.IX : Z80.Register.IY;
			this.dd = this.yyy >> 1;
			this.doubleRegister2 = Z80.doubleRegisters1[this.dd];
			this.doubleRegister2 = doubleRegister2.equals(Z80.Register.HL) ? doubleRegister1 : doubleRegister2;
			switch (opCode1) {
			case (byte) 0X70: // LD (IXY=d),B
			case (byte) 0X71: // LD (IXY=d),C
			case (byte) 0X72: // LD (IXY=d),D
			case (byte) 0X73: // LD (IXY=d),E
			case (byte) 0X74: // LD (IXY=d),H
			case (byte) 0X75: // LD (IXY=d),L
			case (byte) 0X76: // LD (IXY=d),M
			case (byte) 0X77: // LD (IXY=d),A
				this.singleRegister1 = Z80.singleRegisters[zzz];
				break;
			case (byte) 0X46: // LD B,(IXY=d)
			case (byte) 0X4E: // LD C,(IXY=d)
			case (byte) 0X56: // LD D,(IXY=d)
			case (byte) 0X5E: // LD E,(IXY=d)
			case (byte) 0X66: // LD H,(IXY=d)
			case (byte) 0X6E: // LD L,(IXY=d)
				this.singleRegister1 = Z80.singleRegisters[yyy];
				break;
			case (byte) 0X0CB: // Bit Instructions for IXY
				bit = cpuBuss.read(wrs.getProgramCounter() + 3) >> 3 & 0X0F;

				// if (this.page != 0) {
				// this.bit = this.yyy;
				// } // if page NOT 0
				break;

			}// switch opCode1
			break;

		default:
			setMembers(opCode0);
			switch (this.page) {
			case 0: // page 00
				this.dd = this.yyy >> 1;
				this.doubleRegister1 = Z80.doubleRegisters1[dd];
				this.singleRegister1 = Z80.singleRegisters[yyy];
				break;

			case 1: // page 01
				this.singleRegister1 = Z80.singleRegisters[yyy]; // Destination
				this.singleRegister2 = Z80.singleRegisters[zzz]; // Source
				break;

			case 2: // page 10
				this.singleRegister1 = Z80.singleRegisters[zzz]; // Source
				break;

			case 3: // page 11
				switch (yyy) {
				case 1: // 001 POP rr
				case 5: // 101 PUSH rr
					boolean bit3 = ((this.opCode0 & Z80.BIT_3) == Z80.BIT_3);
					if (!bit3) {
						this.dd = this.yyy >> 1;
						this.doubleRegister1 = Z80.doubleRegisters2[dd];
					} // if not bit3
					break;
				case 0:	// 000 Conditional RETURN
				case 2:	// 010 Conditional JUMP
				case 4:	// 100 Conditional CALL
					conditionCode = Z80.conditionCode[yyy];
				} // switch yyy
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
}// class OpCode
