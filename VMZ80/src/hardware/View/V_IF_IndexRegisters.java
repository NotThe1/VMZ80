package hardware.View;

import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import hardware.WorkingRegisterSet;
import utilities.hdNumberBox.HDNumberBox;
import utilities.hdNumberBox.HDNumberValueChangeEvent;
import utilities.hdNumberBox.HDNumberValueChangeListener;

public class V_IF_IndexRegisters extends JInternalFrame  implements Runnable{
	private static final long serialVersionUID = 1L;
	
	AdapterV_IF_IndexRegisters adapterProgramRegisters = new AdapterV_IF_IndexRegisters();	
	WorkingRegisterSet wrs = WorkingRegisterSet.getInstance();
	private HDNumberBox regIX;
	private HDNumberBox regIY;

	@Override
	public void run() {
		setRegisterDisplay();		
	}//run
	
	private void doValueChanged(int newValue, HDNumberBox reg) {
		String name = reg.getName();
		switch (name) {
		case REG_IX:
			wrs.setIX(newValue);
			break;
		case REG_IY:
			wrs.setIY(newValue);
			break;
		default:
			System.err.printf("[V_IF_IndexRegisters.doValueChanged] bad reg argument %s%n", reg.toString());
		}// switch
	}// doValueChanged

	private void setRegisterDisplay() {
		regIX.setValueQuiet(wrs.getIX());
		regIY.setValueQuiet(wrs.getIY());
	}// setRegisterDisplay

	
	/////////////////////////////////////////////////////////////////////
	
	public V_IF_IndexRegisters() {
		setTitle("Index Registers");
		initialize();
		appInit();
	}//Constructor
	
	
	private void appInit() {
		
	}//appInit
	
	private void initialize() {
		setTitle("Index Registers");
		setBounds(0, 200, 230, 100);
		setIconifiable(true);
		getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("IX");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblNewLabel.setBounds(10, 21, 30, 14);
		getContentPane().add(lblNewLabel);
		
		JLabel lblSp = new JLabel("IY");
		lblSp.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSp.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblSp.setBounds(100, 21, 30, 14);
		getContentPane().add(lblSp);
		
		regIX = new HDNumberBox(0,0xFFFF,0,false);
		regIX.setName(REG_IX);
		regIX.addHDNumberValueChangedListener(adapterProgramRegisters);
		regIX.setHexDisplay("%04X");
		regIX.setFont(new Font("Courier New", Font.BOLD, 15));
		regIX.setBounds(50, 11, 40, 35);
		getContentPane().add(regIX);
		GridBagLayout gbl_regPC = new GridBagLayout();
		gbl_regPC.columnWidths = new int[] { 0 };
		gbl_regPC.rowHeights = new int[] { 0 };
		gbl_regPC.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_regPC.rowWeights = new double[] { Double.MIN_VALUE };
		regIX.setLayout(gbl_regPC);
		
		regIY = new HDNumberBox(0, 65535, 0, false);
		regIY.setName(REG_IY);
		regIY.addHDNumberValueChangedListener(adapterProgramRegisters);
		regIY.setHexDisplay("%04X");
		regIY.setFont(new Font("Courier New", Font.BOLD, 15));
		regIY.setBounds(140, 11, 40, 35);
		getContentPane().add(regIY);
		GridBagLayout gbl_regSP = new GridBagLayout();
		gbl_regSP.columnWidths = new int[]{0};
		gbl_regSP.rowHeights = new int[]{0};
		gbl_regSP.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_regSP.rowWeights = new double[]{Double.MIN_VALUE};
		regIY.setLayout(gbl_regSP);
		
	}//initialize
	private static final String REG_IX = "regIX";
	private static final String REG_IY = "regIY";
		
	///////////////////////////////////////////////////////////////////
	
	public class AdapterV_IF_IndexRegisters implements HDNumberValueChangeListener {

		@Override
		public void valueChanged(HDNumberValueChangeEvent hDNumberValueChangeEvent) {
			int newValue =  hDNumberValueChangeEvent.getNewValue() & 0xFFFF;
			doValueChanged(newValue, (HDNumberBox) hDNumberValueChangeEvent.getSource());
		}// valueChanged

	}// class AdapterV_IF_IndexRegisters

}//class V_IF_IndexRegisters
