package hardware.View;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import codeSupport.Z80;
import hardware.WorkingRegisterSet;
import utilities.hdNumberBox.HDNumberBox;
import utilities.hdNumberBox.HDNumberValueChangeEvent;
import utilities.hdNumberBox.HDNumberValueChangeListener;

public class V_SpecialPurposeRegisters extends JPanel implements Runnable {
	private static final long serialVersionUID = 1L;
	private AdapterV_SpecialPurposeRegisters adapterSPR = new AdapterV_SpecialPurposeRegisters();
	WorkingRegisterSet wrs = WorkingRegisterSet.getInstance();
	private static RoundIcon redIcon = new RoundIcon(Color.RED);
	private static RoundIcon grayIcon = new RoundIcon(Color.GRAY);
	private HDNumberBox regI;
	private HDNumberBox regR;

	@Override
	public void run() {
		setRegisterDisplay();
	}// run

	private void doValueChanged(byte newValue, HDNumberBox reg) {
		String name = reg.getName();
		switch (name) {
		case REG_I:
			wrs.setReg(Z80.Register.I, newValue);
			break;
		case REG_R:
			wrs.setReg(Z80.Register.R, newValue);
			break;
		default:
			System.err.printf("[V_ProgramRegisters.doValueChanged] bad reg argument %s%n", reg.toString());
		}// switch
	}// doValueChanged
	
	private void doFlagChange(String name,boolean isSelected){
		switch(name) {
		case IFF1:
			wrs.setIFF1(isSelected);
			cbIFF1.setSelected(wrs.isIFF1Set());
			cbIFF1.setIcon(isSelected?redIcon:grayIcon);
			break;
		case IFF2:
			wrs.setIFF2(isSelected);
			cbIFF2.setSelected(wrs.isIFF2Set());
			cbIFF2.setIcon(isSelected?redIcon:grayIcon);
			break;
		}//
		//switch}
		
	}//doFlagChange

	private void setRegisterDisplay() {
		regI.setValueQuiet(wrs.getReg(Z80.Register.I) & 0xFF);
		regR.setValueQuiet(wrs.getReg(Z80.Register.R) & 0xFF);
		cbIFF1.setSelected(wrs.isIFF1Set());
		cbIFF1.setIcon(wrs.isIFF1Set()?redIcon:grayIcon);
		cbIFF2.setSelected(wrs.isIFF2Set());
		cbIFF2.setIcon(wrs.isIFF2Set()?redIcon:grayIcon);

	}// setRegisterDisplay

	public V_SpecialPurposeRegisters() {
		initialize();
		appInit();
	}// Constructor

	private void appInit() {

	}// appInit

	private void initialize() {
		setPreferredSize(new Dimension(200, 115));
		setBorder(
				new TitledBorder(null, "Special Purpose Registers", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		setLayout(null);

		JLabel lblI = new JLabel("I");
		lblI.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblI.setHorizontalAlignment(SwingConstants.TRAILING);
		lblI.setToolTipText("Interrupt Page");
		lblI.setBounds(21, 32, 20, 14);
		add(lblI);

		regI = new HDNumberBox(0, 0xFF, 00, false);
		regI.setBounds(51, 22, 30, 35);
		regI.setPreferredSize(new Dimension(30, 40));
		regI.setToolTipText("Interrupt Page");
		regI.addHDNumberValueChangedListener(adapterSPR);
		regI.setName(REG_I);
		// regPC.addHDNumberValueChangedListener(adapterVPR);
		regI.setFont(new Font("Courier New", Font.BOLD, 15));
		regI.setHexDisplay("%02X");
		add(regI);
		GridBagLayout gbl_regI = new GridBagLayout();
		gbl_regI.columnWidths = new int[] { 0 };
		gbl_regI.rowHeights = new int[] { 0 };
		gbl_regI.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_regI.rowWeights = new double[] { Double.MIN_VALUE };
		regI.setLayout(gbl_regI);

		JLabel lblR = new JLabel("R");
		lblR.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblR.setToolTipText("Memory Refresh");
		lblR.setHorizontalAlignment(SwingConstants.TRAILING);
		lblR.setBounds(21, 78, 20, 14);
		add(lblR);

		regR = new HDNumberBox(0, 0xFF, 0, false);
		regR.setMinimumSize(new Dimension(30, 40));
		regR.setBounds(51, 68, 30, 35);
		regR.setPreferredSize(new Dimension(30, 40));
		regR.setToolTipText("Memory Refresh");
		regR.addHDNumberValueChangedListener(adapterSPR);
		regR.setName(REG_R);
		regR.setHexDisplay("%02X");
		regR.setFont(new Font("Courier New", Font.BOLD, 15));
		add(regR);
		GridBagLayout gbl_regM = new GridBagLayout();
		gbl_regM.columnWidths = new int[] { 0 };
		gbl_regM.rowHeights = new int[] { 0 };
		gbl_regM.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_regM.rowWeights = new double[] { Double.MIN_VALUE };
		regR.setLayout(gbl_regM);

		cbIFF1 = new JCheckBox("IFF1");
		cbIFF1.addActionListener(adapterSPR);
		cbIFF1.setIcon(grayIcon);
		cbIFF1.setName(IFF1);
		// cbIFF1.add
		cbIFF1.setBounds(104, 28, 60, 23);
		add(cbIFF1);
		
		cbIFF2 = new JCheckBox("IFF2");
		cbIFF2.addActionListener(adapterSPR);
		cbIFF2.setIcon(grayIcon);
		cbIFF2.setName(IFF2);
		cbIFF2.setBounds(104, 74, 60, 23);
		add(cbIFF2);
		
	}// initialize

	public class AdapterV_SpecialPurposeRegisters implements HDNumberValueChangeListener, ActionListener {

		/* HDNumberValueChangeListener */
		@Override
		public void valueChanged(HDNumberValueChangeEvent hDNumberValueChangeEvent) {
			byte newValue = (byte) hDNumberValueChangeEvent.getNewValue();
			doValueChanged(newValue, (HDNumberBox) hDNumberValueChangeEvent.getSource());
		}// valueChanged

		/* ActionListener */
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			Component source = (Component) actionEvent.getSource();
			String name = source.getName();
			if(actionEvent.getSource() instanceof JCheckBox) {
				doFlagChange(name,((AbstractButton) source).isSelected());
			}//if

		}//actionPerformed

	}// class AdapterV_PrimaryRegisters

	private static final String REG_I = "regI";
	private static final String REG_R = "regR";
	private JCheckBox cbIFF2;
	private JCheckBox cbIFF1;
	private static final String IFF1 = "IFF1";
	private static final String IFF2 = "IFF2";

}// class V_ProgramRegisters
