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
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import codeSupport.Z80;
import hardware.WorkingRegisterSet;
import utilities.hdNumberBox.HDNumberBox;
import utilities.hdNumberBox.HDNumberValueChangeEvent;
import utilities.hdNumberBox.HDNumberValueChangeListener;

public class V_IF_SpecialRegisters extends JInternalFrame  implements Runnable {
	private static final long serialVersionUID = 1L;
	
	AdapterIF_V_SpecialRegisters adapterSpecialRegisters = new AdapterIF_V_SpecialRegisters();
	
	private static RoundIcon redIcon = new RoundIcon(Color.RED);
	private static RoundIcon grayIcon = new RoundIcon(Color.GRAY);
	WorkingRegisterSet wrs = WorkingRegisterSet.getInstance();

	@Override
	public void run() {
		updateDisplay();
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

	public void updateDisplay() {
		regI.setValueQuiet(wrs.getReg(Z80.Register.I) & 0xFF);
		regR.setValueQuiet(wrs.getReg(Z80.Register.R) & 0xFF);
		cbIFF1.setSelected(wrs.isIFF1Set());
		cbIFF1.setIcon(wrs.isIFF1Set()?redIcon:grayIcon);
		cbIFF2.setSelected(wrs.isIFF2Set());
		cbIFF2.setIcon(wrs.isIFF2Set()?redIcon:grayIcon);

	}// setRegisterDisplay


	public V_IF_SpecialRegisters() {
		initialize();
		appInit();
	}// Constructor

	private void appInit() {

	}// appInit

	private void initialize() {
		setTitle("Special Registers");
		setBounds(0, 300, 230, 100);
		setIconifiable(true);
		getContentPane().setLayout(null);
		
		JLabel lblNewLabel = new JLabel("I");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblNewLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		lblNewLabel.setBounds(10, 24, 20, 14);
		getContentPane().add(lblNewLabel);
		
		JLabel lblR = new JLabel("R");
		lblR.setHorizontalAlignment(SwingConstants.TRAILING);
		lblR.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblR.setBounds(66, 24, 20, 14);
		getContentPane().add(lblR);
		
		regI = new HDNumberBox(0, 255, 0, false);
		regI.setName(REG_I);
		regI.addHDNumberValueChangedListener(adapterSpecialRegisters);
		regI.setToolTipText("Interrupt Page");
		regI.setPreferredSize(new Dimension(30, 40));
		regI.setName("regI");
		regI.setHexDisplay("%02X");
		regI.setFont(new Font("Courier New", Font.BOLD, 15));
		regI.setBounds(40, 14, 30, 35);
		getContentPane().add(regI);
		GridBagLayout gbl_regI = new GridBagLayout();
		gbl_regI.columnWidths = new int[]{0};
		gbl_regI.rowHeights = new int[]{0};
		gbl_regI.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_regI.rowWeights = new double[]{Double.MIN_VALUE};
		regI.setLayout(gbl_regI);
		
		regR = new HDNumberBox(0, 255, 0, false);
		regR.setName(REG_R);
		regR.addHDNumberValueChangedListener(adapterSpecialRegisters);
		regR.setToolTipText("Interrupt Page");
		regR.setPreferredSize(new Dimension(30, 40));
		regR.setName("regI");
		regR.setHexDisplay("%02X");
		regR.setFont(new Font("Courier New", Font.BOLD, 15));
		regR.setBounds(96, 14, 30, 35);
		getContentPane().add(regR);
		GridBagLayout gbl_regR = new GridBagLayout();
		gbl_regR.columnWidths = new int[]{0};
		gbl_regR.rowHeights = new int[]{0};
		gbl_regR.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_regR.rowWeights = new double[]{Double.MIN_VALUE};
		regR.setLayout(gbl_regR);
		
		cbIFF1 = new JCheckBox(IFF1);
		cbIFF1.setName(IFF1);
		cbIFF1.addActionListener(adapterSpecialRegisters);
		cbIFF1.setIcon(grayIcon);
		cbIFF1.setBounds(145, 7, 50, 23);
		getContentPane().add(cbIFF1);
		
		cbIFF2 = new JCheckBox(IFF2);
		cbIFF2.setName(IFF2);
		cbIFF2.addActionListener(adapterSpecialRegisters);
		cbIFF2.setBounds(144, 33, 50, 23);
		cbIFF2.setIcon(grayIcon);
		getContentPane().add(cbIFF2);
	}// initialize
	
	private static final String REG_I = "regI";
	private static final String REG_R = "regR";
	private static final String IFF1 = "IFF1";
	private static final String IFF2 = "IFF2";
	private HDNumberBox regI;
	private HDNumberBox regR;
	private JCheckBox cbIFF1;
	private JCheckBox cbIFF2;
	
	//////////////////////////////////////////////////////////
	
	public class AdapterIF_V_SpecialRegisters implements HDNumberValueChangeListener, ActionListener {

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

	}// class AdapterIF_V_SpecialRegisters	
}// class V_IF_SpecialRegisters
