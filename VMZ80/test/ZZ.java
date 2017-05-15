import codeSupport.Z80;

public class ZZ {

	public static void main(String[] args) {
		String s = String.format("%02X <-> %02X%n", Z80.ccCarry, Z80.ccnCarry);
		System.out.printf("%02X <-> %02X%n", Z80.ccSign, Z80.ccnSign);
		System.out.printf("%02X <-> %02X%n", Z80.ccZero, Z80.ccnZero);
		System.out.printf("%02X <-> %02X%n", Z80.ccBit5, Z80.ccnBit5);
		System.out.printf("%02X <-> %02X%n", Z80.ccAux, Z80.ccnAux);
		System.out.printf("%02X <-> %02X%n", Z80.ccBit3, Z80.ccnBit3);
		System.out.printf("%02X <-> %02X%n", Z80.ccPV, Z80.ccnPV);
		System.out.printf("%02X <-> %02X%n", Z80.ccN, Z80.ccnN);
		System.out.printf("%02X <-> %02X%n", Z80.ccCarry, Z80.ccnCarry);

	}// main

}
