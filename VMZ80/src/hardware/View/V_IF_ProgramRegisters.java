package hardware.View;

import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;

import hardware.WorkingRegisterSet;
import utilities.hdNumberBox.HDNumberBox;
import utilities.hdNumberBox.HDNumberValueChangeEvent;
import utilities.hdNumberBox.HDNumberValueChangeListener;

public class V_IF_ProgramRegisters extends JInternalFrame  implements Runnable{

	AdapterV_IF_ProgramRegisters adapterProgramRegisters = new AdapterV_IF_ProgramRegisters();	
	WorkingRegisterSet wrs = WorkingRegisterSet.getInstance();
	private HDNumberBox regPC;
	private HDNumberBox regSP;
	
	EventListenerList hdNumberValueChangeListenerList = new EventListenerList();;


	@Override
	public void run() {
		updateDisplay();		
	}//run
	
	private void doValueChanged(int newValue, HDNumberBox reg) {
		String name = reg.getName();
		switch (name) {
		case REG_PC:
			wrs.setProgramCounter(newValue);
			fireProgramCounterValueChanged(newValue);
			break;
		case REG_SP:
			wrs.setStackPointer(newValue);
			break;
		default:
			System.err.printf("[V_ProgramRegisters.doValueChanged] bad reg argument %s%n", reg.toString());
		}// switch
	}// doValueChanged

	public void updateDisplay() {
		regPC.setValueQuiet(wrs.getProgramCounter());
		regSP.setValueQuiet(wrs.getStackPointer());
	}// setRegisterDisplay

	
	/////////////////////////////////////////////////////////////////////
	
	public V_IF_ProgramRegisters() {
		setTitle("Program Registers");
		initialize();
		appInit();
	}//Constructor
	
	
	private void appInit() {
		
	}//appInit
	
	private void initialize() {
		setTitle("Program Registers");
		setIconifiable(true);
		setBounds(0, 105, 230, 100);
		getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("PC");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblNewLabel.setBounds(10, 21, 30, 14);
		getContentPane().add(lblNewLabel);
		
		JLabel lblSp = new JLabel("SP");
		lblSp.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSp.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblSp.setBounds(100, 21, 30, 14);
		getContentPane().add(lblSp);
		
		regPC = new HDNumberBox(0,0xFFFF,0,false);
		regPC.setName(REG_PC);
		regPC.addHDNumberValueChangedListener(adapterProgramRegisters);
		regPC.setHexDisplay("%04X");
		regPC.setFont(new Font("Courier New", Font.BOLD, 15));
		regPC.setBounds(50, 11, 40, 35);
		getContentPane().add(regPC);
		GridBagLayout gbl_regPC = new GridBagLayout();
		gbl_regPC.columnWidths = new int[] { 0 };
		gbl_regPC.rowHeights = new int[] { 0 };
		gbl_regPC.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_regPC.rowWeights = new double[] { Double.MIN_VALUE };
		regPC.setLayout(gbl_regPC);
		
		regSP = new HDNumberBox(0, 65535, 0, false);
		regSP.setName(REG_SP);
		regSP.addHDNumberValueChangedListener(adapterProgramRegisters);
		regSP.setHexDisplay("%04X");
		regSP.setFont(new Font("Courier New", Font.BOLD, 15));
		regSP.setBounds(140, 11, 40, 35);
		getContentPane().add(regSP);
		GridBagLayout gbl_regSP = new GridBagLayout();
		gbl_regSP.columnWidths = new int[]{0};
		gbl_regSP.rowHeights = new int[]{0};
		gbl_regSP.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_regSP.rowWeights = new double[]{Double.MIN_VALUE};
		regSP.setLayout(gbl_regSP);
		
	}//initialize
	
	// ---------------------------
	public void addHDNumberValueChangedListener(HDNumberValueChangeListener seekValueChangeListener) {
		hdNumberValueChangeListenerList.add(HDNumberValueChangeListener.class, seekValueChangeListener);
	}// addSeekValueChangedListener

	public void removeHDNumberValueChangedListener(HDNumberValueChangeListener seekValueChangeListener) {
		hdNumberValueChangeListenerList.remove(HDNumberValueChangeListener.class, seekValueChangeListener);
	}// addSeekValueChangedListener

	protected void fireProgramCounterValueChanged(int newValue) {
		Object[] listeners = hdNumberValueChangeListenerList.getListenerList();
		// process
		HDNumberValueChangeEvent hdNumberValueChangeEvent = new HDNumberValueChangeEvent(this, Integer.MIN_VALUE,
				newValue);

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == HDNumberValueChangeListener.class) {
				((HDNumberValueChangeListener) listeners[i + 1]).valueChanged(hdNumberValueChangeEvent);
			} // if
		} // for

	}// fireSeekValueChanged

	// --------------------------------------------------------

	
	
	
	private static final String REG_PC = "regPC";
	private static final String REG_SP = "regSP";
		
	///////////////////////////////////////////////////////////////////
	
	public class AdapterV_IF_ProgramRegisters implements HDNumberValueChangeListener {

		@Override
		public void valueChanged(HDNumberValueChangeEvent hDNumberValueChangeEvent) {
			int newValue =  hDNumberValueChangeEvent.getNewValue() & 0xFFFF;
			doValueChanged(newValue, (HDNumberBox) hDNumberValueChangeEvent.getSource());
		}// valueChanged

	}// class AdapterV_IF_ProgramRegisters

}//class V_IF_ProgramRegisters
