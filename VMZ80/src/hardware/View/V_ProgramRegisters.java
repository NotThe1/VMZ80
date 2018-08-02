package hardware.View;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import hardware.WorkingRegisterSet;
import utilities.hdNumberBox.HDNumberBox;
import utilities.hdNumberBox.HDNumberValueChangeEvent;
import utilities.hdNumberBox.HDNumberValueChangeListener;

public class V_ProgramRegisters extends JPanel implements Runnable {
	private static final long serialVersionUID = 1L;
	 AdapterV_ProgramRegisters adapterVPR = new AdapterV_ProgramRegisters();
	WorkingRegisterSet wrs = WorkingRegisterSet.getInstance();
	private HDNumberBox regPC;
	private HDNumberBox regSP;

	@Override
	public void run() {
		setRegisterDisplay();
	}// run

	private void doValueChanged(int newValue, HDNumberBox reg) {
		String name = reg.getName();
		switch (name) {
		case REG_PC:
			wrs.setProgramCounter(newValue);
			break;
		case REG_SP:
			wrs.setStackPointer(newValue);
			break;
		default:
			System.err.printf("[V_ProgramRegisters.doValueChanged] bad reg argument %s%n", reg.toString());
		}// switch
	}// doValueChanged

	private void setRegisterDisplay() {
		regPC.setValueQuiet(wrs.getProgramCounter());
		regSP.setValueQuiet(wrs.getStackPointer());
	}// setRegisterDisplay

	public V_ProgramRegisters() {
		initialize();
		appInit();
	}// Constructor

	private void appInit() {

	}// appInit

	private void initialize() {
		setPreferredSize(new Dimension(240, 75));
		setBorder(new TitledBorder(null, "Program Registers", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		setLayout(null);

		JLabel lblPc = new JLabel("PC");
		lblPc.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblPc.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPc.setToolTipText("Program Counter");
		lblPc.setBounds(10, 32, 46, 14);
		add(lblPc);

		regPC = new HDNumberBox(0, 0xFFFF, 00, false);
		regPC.setToolTipText("Program Counter");
		regPC.addHDNumberValueChangedListener(adapterVPR);
		regPC.setName(REG_PC);
		// regPC.addHDNumberValueChangedListener(adapterVPR);
		regPC.setFont(new Font("Courier New", Font.BOLD, 15));
		regPC.setHexDisplay("%04X");
		regPC.setBounds(66, 22, 40, 35);
		add(regPC);
		GridBagLayout gbl_regPC = new GridBagLayout();
		gbl_regPC.columnWidths = new int[] { 0 };
		gbl_regPC.rowHeights = new int[] { 0 };
		gbl_regPC.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_regPC.rowWeights = new double[] { Double.MIN_VALUE };
		regPC.setLayout(gbl_regPC);

		JLabel lblSp = new JLabel("SP");
		lblSp.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblSp.setToolTipText("StackPointer");
		lblSp.setHorizontalAlignment(SwingConstants.TRAILING);
		lblSp.setBounds(116, 32, 46, 14);
		add(lblSp);

		regSP = new HDNumberBox(0, 65535, 0, false);
		regSP.setToolTipText("StackPointer");
		regSP.addHDNumberValueChangedListener(adapterVPR);
		regSP.setName(REG_SP);
		regSP.setHexDisplay("%04X");
		regSP.setFont(new Font("Courier New", Font.BOLD, 15));
		regSP.setBounds(172, 22, 40, 35);
		add(regSP);
		GridBagLayout gbl_regSP = new GridBagLayout();
		gbl_regSP.columnWidths = new int[] { 0 };
		gbl_regSP.rowHeights = new int[] { 0 };
		gbl_regSP.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_regSP.rowWeights = new double[] { Double.MIN_VALUE };
		regSP.setLayout(gbl_regSP);
	}// initialize

	public class AdapterV_ProgramRegisters implements HDNumberValueChangeListener {

		@Override
		public void valueChanged(HDNumberValueChangeEvent hDNumberValueChangeEvent) {
			int newValue =  hDNumberValueChangeEvent.getNewValue() & 0xFFFF;
			doValueChanged(newValue, (HDNumberBox) hDNumberValueChangeEvent.getSource());
		}// valueChanged

	}// class AdapterV_ProgramRegisters

	private static final String REG_PC = "regPC";
	private static final String REG_SP = "regSP";

}// class V_ProgramRegisters
