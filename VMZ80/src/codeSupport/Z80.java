package codeSupport;

public class Z80 {
	
	public enum Register {
		// Single Byte Registers
		A, B, C, D, E, H, L,F,
		Ap, Fp,Bp, Cp, Dp, Ep, Hp, Lp,
		I,R,
			// AFp,BCp, DEp, HLp,
			// no instructions reference these 16 Bit registers
		
			// Double Byte Registers
			// used for identification only
			// nothing is stored directly into one of these
		 AF,BC, DE, HL,
		 M,
		 SP, PC,
		 IX,IY
	}//enum Register
	
	/**
	 * Constants
	 */
	
	public static final int WORD_MASK = 0X00FFFF;
	public static final int BYTE_MASK = 0X00FF;
	public static final int HI_BYTE_MASK = 0X00FF00;
	/**
	 * registers
	 */
	public static final String REG_A = "REG_A";
	public static final String REG_F = "REG_F";
	public static final String REG_B = "REG_B";
	public static final String REG_C = "REG_C";
	public static final String REG_D = "REG_D";
	public static final String REG_E = "REG_E";
	public static final String REG_H = "REG_H";
	public static final String REG_L = "REG_L";
	public static final String REG_M = "REG_M";
	public static final String REG_I = "REG_I";
	public static final String REG_R = "REG_R";
	
	public static final String REG_AF = "REG_AF";
	public static final String REG_BC = "REG_BC";
	public static final String REG_DE = "REG_DE";
	public static final String REG_HL = "REG_HL";
	
	public static final String REG_AFp = "REG_AFp";
	public static final String REG_BCp = "REG_BCp";
	public static final String REG_DEp = "REG_DEp";
	public static final String REG_HLp = "REG_HLp";
	                           
	public static final String REG_IX = "REG_IX";
	public static final String REG_IY = "REG_IY";
	public static final String REG_SP = "REG_SP";
	public static final String REG_PC = "REG_PC";
	
	/**
	 * Conditions
	 */
	public static final String COND_NZ = "COND_NZ";
	public static final String COND_Z = "COND_Z";
	public static final String COND_NC = "COND_NC";
	public static final String COND_C = "COND_C";
	public static final String COND_PO = "COND_PO";
	public static final String COND_PE = "COND_PE";
	public static final String COND_P = "COND_P";
	public static final String COND_M = "COND_M";
	
	
	/**
	 * flags
	 */
	public static final byte BIT_SIGN = (byte) 0b10000000;
	public static final byte BIT_ZERO = (byte) 0b01000000;
	public static final byte BIT_BIT5 = (byte) 0b00100000;
	public static final byte BIT_AUX  = (byte) 0b00010000;
	public static final byte BIT_BIT3 = (byte) 0b00001000;
	public static final byte BIT_PV = (byte) 0b00000100;
	public static final byte BIT_N = (byte) 0b00000010;
	public static final byte BIT_CARRY = (byte) 0b00000001;

	public static final byte MASK_SIGN = (byte) ~BIT_SIGN;
	public static final byte MASK_ZERO =(byte)  ~BIT_ZERO;
	public static final byte MASK_BIT5 =(byte)  ~BIT_BIT5;
	public static final byte MASK_AUX =(byte)  ~BIT_AUX;
	public static final byte MASK_BIT3 =(byte)  ~BIT_BIT3;
	public static final byte MASK_PV =(byte)  ~BIT_PV;
	public static final byte MASK_N =(byte)  ~BIT_N;
	public static final byte MASK_CARRY =(byte)  ~BIT_CARRY;
	
	/**
	 * tables
	 */
	
//	public static final HashMap<String, Byte> conditionTable = new HashMap<>();
//	static {
//		conditionTable.put("NZ", (byte) 0b00000000);
//		conditionTable.put("Z", (byte) 0b00001000);
//		conditionTable.put("NC", (byte) 0b00010000);
//		conditionTable.put("C", (byte) 0b00011000);
//		conditionTable.put("PO", (byte) 0b00100000);
//		conditionTable.put("PE", (byte) 0b00101000);
//		conditionTable.put("P", (byte) 0b00110000);
//		conditionTable.put("M", (byte) 0b00111000);
//	}// static conditionTable

//	public static final HashMap<String, Byte> registerTable = new HashMap<>();
//	static {
//		registerTable.put("A", (byte) 0b00000111);
//		registerTable.put("B", (byte) 0b00000000);
//		registerTable.put("C", (byte) 0b00000001);
//		registerTable.put("D", (byte) 0b00000010);
//		registerTable.put("E", (byte) 0b00000011);
//		registerTable.put("H", (byte) 0b00000100);
//		registerTable.put("L", (byte) 0b00000101);
//		registerTable.put("M", (byte) 0b00000110);
//		registerTable.put("(HL)", (byte) 0b00000110);
//
//		registerTable.put("BC", (byte) 0b00000000);
//		registerTable.put("DE", (byte) 0b00010000);
//		registerTable.put("HL", (byte) 0b00100000);
//		registerTable.put("SP", (byte) 0b00110000);
//		registerTable.put("AF", (byte) 0b00110000);
//		registerTable.put("IX", (byte) 0b00100000);
//		registerTable.put("IY", (byte) 0b00100000);
//
//	}// static registerTable

//	public static final HashMap<Integer, Byte> bitTable = new HashMap<>();
//	static {
//		bitTable.put(0, (byte) 0b00000000);
//		bitTable.put(1, (byte) 0b00001000);
//		bitTable.put(2, (byte) 0b00010000);
//		bitTable.put(3, (byte) 0b00011000);
//		bitTable.put(4, (byte) 0b00100000);
//		bitTable.put(5, (byte) 0b00101000);
//		bitTable.put(6, (byte) 0b00110000);
//		bitTable.put(7, (byte) 0b00111000);
//	}// static bitTable
	
	
	
}
