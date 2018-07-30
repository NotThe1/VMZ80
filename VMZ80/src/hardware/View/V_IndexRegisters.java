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

public class V_IndexRegisters extends JPanel implements Runnable {
	private static final long serialVersionUID = 1L;
	private AdapterV_IndexRegisters adapterIndexRegs = new AdapterV_IndexRegisters();
	WorkingRegisterSet wrs = WorkingRegisterSet.getInstance();
	private HDNumberBox regIX;
	private HDNumberBox regIY;

	@Override
	public void run() {
		setRegisterDisplay();
	}// run

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
			System.err.printf("[V_IndexRegisters.doValueChanged] bad reg argument %s%n", reg.toString());
		}// switch
	}// doValueChanged

	private void setRegisterDisplay() {
		regIX.setValueQuiet(wrs.getIX());
		regIY.setValueQuiet(wrs.getIY());
	}// setRegisterDisplay

	public V_IndexRegisters() {
		initialize();
		appInit();
	}// Constructor

	private void appInit() {

	}// appInit

	private void initialize() {
		setPreferredSize(new Dimension(240, 75));
		setBorder(new TitledBorder(null, "Index Registers", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		setLayout(null);

		JLabel lblIX = new JLabel("IX");
		lblIX.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblIX.setHorizontalAlignment(SwingConstants.TRAILING);
		lblIX.setToolTipText("Program Counter");
		lblIX.setBounds(10, 32, 46, 14);
		add(lblIX);

		regIX = new HDNumberBox(0, 0xFFFF, 00, false);
		regIX.setToolTipText("Program Counter");
		regIX.addHDNumberValueChangedListener(adapterIndexRegs);
		regIX.setName(REG_IX);
		// regPC.addHDNumberValueChangedListener(adapterVPR);
		regIX.setFont(new Font("Courier New", Font.BOLD, 15));
		regIX.setHexDisplay("%04X");
		regIX.setBounds(66, 22, 40, 35);
		add(regIX);
		GridBagLayout gbl_regPC = new GridBagLayout();
		gbl_regPC.columnWidths = new int[] { 0 };
		gbl_regPC.rowHeights = new int[] { 0 };
		gbl_regPC.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_regPC.rowWeights = new double[] { Double.MIN_VALUE };
		regIX.setLayout(gbl_regPC);

		JLabel lblIY = new JLabel("IY");
		lblIY.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblIY.setToolTipText("StackPointer");
		lblIY.setHorizontalAlignment(SwingConstants.TRAILING);
		lblIY.setBounds(116, 32, 46, 14);
		add(lblIY);

		regIY = new HDNumberBox(0, 65535, 0, false);
		regIY.setToolTipText("StackPointer");
		regIY.addHDNumberValueChangedListener(adapterIndexRegs);
		regIY.setName(REG_IY);
		regIY.setHexDisplay("%04X");
		regIY.setFont(new Font("Courier New", Font.BOLD, 15));
		regIY.setBounds(172, 22, 40, 35);
		add(regIY);
		GridBagLayout gbl_regSP = new GridBagLayout();
		gbl_regSP.columnWidths = new int[] { 0 };
		gbl_regSP.rowHeights = new int[] { 0 };
		gbl_regSP.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_regSP.rowWeights = new double[] { Double.MIN_VALUE };
		regIY.setLayout(gbl_regSP);
	}// initialize

	private class AdapterV_IndexRegisters implements HDNumberValueChangeListener {

		@Override
		public void valueChanged(HDNumberValueChangeEvent hDNumberValueChangeEvent) {
			int newValue =  hDNumberValueChangeEvent.getNewValue() & 0xFFFF;
			doValueChanged(newValue, (HDNumberBox) hDNumberValueChangeEvent.getSource());
		}// valueChanged

	}// class AdapterV_PrimaryRegisters

	private static final String REG_IX = "regIX";
	private static final String REG_IY = "regIY";

}// class V_ProgramRegisters
