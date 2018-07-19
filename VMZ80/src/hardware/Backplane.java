package hardware;

import codeSupport.AppLogger;
import memory.Core;
import memory.CpuBuss;
import memory.IoBuss;

public class Backplane {
	AppLogger log = AppLogger.getInstance();
	
	Core core = Core.getInstance();
	CpuBuss cpuBuss = CpuBuss.getInstance();
	IoBuss ioBuss = IoBuss.getInstance();
	
	ConditionCodeRegister ccr = ConditionCodeRegister.getInstance();
	WorkingRegisterSet wrs = WorkingRegisterSet.getInstance();
	ArithmeticUnit au = ArithmeticUnit.getInstance();

	public Backplane() {

	}// Constructor

}// class Backplane
