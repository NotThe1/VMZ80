package hardware;

import memory.CpuBuss;
import memory.IoBuss;

public class Backplane {
CpuBuss cpuBuss = CpuBuss.getInstance();
IoBuss ioBuss = IoBuss.getInstance();
	public Backplane() {
		
	}//Constructor

}//class Backplane
