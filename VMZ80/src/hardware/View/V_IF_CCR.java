package hardware.View;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import codeSupport.RoundIcon;
import codeSupport.Z80;
import hardware.ConditionCodeRegister;
import hdNumberBox.HDNbox;
import hdNumberBox.HDNumberValueChangeEvent;
import hdNumberBox.HDNumberValueChangeListener;

public class V_IF_CCR extends JInternalFrame implements Runnable {
	private static final long serialVersionUID = 1L;

	AdapterIF_V_CCR adapterCCR = new AdapterIF_V_CCR();
	// WorkingRegisterSet wrs = WorkingRegisterSet.getInstance();
	ConditionCodeRegister ccr = ConditionCodeRegister.getInstance();

	private static RoundIcon redIcon = new RoundIcon(Color.RED);
	private static RoundIcon grayIcon = new RoundIcon(Color.GRAY);
	private static RoundIcon lightGrayIcon = new RoundIcon(Color.LIGHT_GRAY);
	private HDNbox ccHexValue;

	@Override
	public void run() {
		updateDisplay();
	}// run
	
	@Override
	public void setEnabled(boolean state) {
		super.setEnabled(state);
		rbSign.setEnabled(state);
		rbZero.setEnabled(state);
		rbHalf.setEnabled(state);
		rbParity.setEnabled(state);
		rbNeg.setEnabled(state);
		rbCarry.setEnabled(state);
		ccHexValue.setEnabled(state);
	}//setEnabled

	public void updateDisplay() {
		// byte currentValue= ccr.getConditionCode();

		rbSign.setSelected(ccr.isSignFlagSet() ? true : false);
		rbSign.setIcon(rbSign.isSelected() ? redIcon : grayIcon);

		rbZero.setSelected(ccr.isZeroFlagSet() ? true : false);
		rbZero.setIcon(rbZero.isSelected() ? redIcon : grayIcon);

		rbHalf.setSelected(ccr.isHFlagSet() ? true : false);
		rbHalf.setIcon(rbHalf.isSelected() ? redIcon : grayIcon);

		rbParity.setSelected(ccr.isPvFlagSet() ? true : false);
		rbParity.setIcon(rbParity.isSelected() ? redIcon : grayIcon);

		rbNeg.setSelected(ccr.isNFlagSet() ? true : false);
		rbNeg.setIcon(rbNeg.isSelected() ? redIcon : grayIcon);

		rbCarry.setSelected(ccr.isCarryFlagSet() ? true : false);
		rbCarry.setIcon(rbCarry.isSelected() ? redIcon : grayIcon);

		ccHexValue.setValueQuiet(0x00 << 24 | ccr.getConditionCode() & 0xff);

	}// setHexValue

	private void setByHexValue(byte rawValue) {
		byte value = (byte) rawValue;
		ccr.setSignFlag((value & Z80.BIT_SIGN) == Z80.BIT_SIGN);
		ccr.setZeroFlag((value & Z80.BIT_ZERO) == Z80.BIT_ZERO);
		ccr.setHFlag((value & Z80.BIT_AUX) == Z80.BIT_AUX);
		ccr.setPvFlag((value & Z80.BIT_PV) == Z80.BIT_PV);
		ccr.setNFlag((value & Z80.BIT_N) == Z80.BIT_N);
		ccr.setCarryFlag((value & Z80.BIT_CARRY) == Z80.BIT_CARRY);
		updateDisplay();
	}// setHexValue

	private void doFlagChange(String name) {
		switch (name) {
		case RB_SIGN:
			rbSign.doClick();
			break;
		case RB_ZERO:
			rbZero.doClick();
			break;
		case RB_HALF:
			rbHalf.doClick();
			break;
		case RB_PARITY:
			rbParity.doClick();
			break;
		case RB_NEG:
			rbNeg.doClick();
			break;
		case RB_CARRY:
			rbCarry.doClick();
			break;
		case RB_NU:
		default:
			System.err.printf("[V_IF_CCR.doFlagChange] unknown argument %s%n", name);
			break;
		}// switch

	}// doFlagChange

	private void doFlagChange(JRadioButton button, String name) {
		switch (name) {
		case RB_SIGN:
			ccr.setSignFlag(rbSign.isSelected());
			break;
		case RB_ZERO:
			ccr.setZeroFlag(rbZero.isSelected());
			break;
		case RB_HALF:
			ccr.setHFlag(rbHalf.isSelected());
			break;
		case RB_PARITY:
			ccr.setPvFlag(rbParity.isSelected());
			break;
		case RB_NEG:
			ccr.setHFlag(rbHalf.isSelected());
			break;
		case RB_CARRY:
			ccr.setCarryFlag(rbCarry.isSelected());
			break;
		case RB_NU:
		default:
			System.err.printf("[V_IF_CCR.doFlagChange] unknown argument %s%n", name);
			break;
		}// switch
		button.setIcon(button.isSelected() ? redIcon : grayIcon);
		ccHexValue.setValueQuiet(0x00 << 24 | ccr.getConditionCode() & 0xff);

	}// doFlagChange

	//////////////////////////////////////////////////////////////////
	public V_IF_CCR() {
		initialize();
		appInit();
	}// Constructor

	private void appInit() {
		setName("V_IF_CCR");
	}// appInit

	public void initialize() {
		setTitle("Condition Code Registers");
		setIconifiable(true);
		setBounds(0, 410, 230, 100);
		getContentPane().setLayout(null);

		rbSign = new JRadioButton("");
		rbSign.setName(RB_SIGN);
		rbSign.addActionListener(adapterCCR);
		rbSign.setIcon(grayIcon);
		rbSign.setBounds(6, 10, 16, 23);
		getContentPane().add(rbSign);

		JLabel lblSign = new JLabel("S");
		lblSign.setName(RB_SIGN);
		lblSign.addMouseListener(adapterCCR);
		lblSign.setHorizontalAlignment(SwingConstants.CENTER);
		lblSign.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblSign.setBounds(6, 35, 16, 23);
		getContentPane().add(lblSign);

		rbZero = new JRadioButton("");
		rbZero.setName(RB_ZERO);
		rbZero.addActionListener(adapterCCR);
		rbZero.setIcon(grayIcon);
		rbZero.setBounds(28, 10, 16, 23);
		getContentPane().add(rbZero);

		JLabel lblZero = new JLabel("Z");
		lblZero.setName(RB_ZERO);
		lblZero.addMouseListener(adapterCCR);
		lblZero.setHorizontalAlignment(SwingConstants.CENTER);
		lblZero.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblZero.setBounds(28, 35, 16, 23);
		getContentPane().add(lblZero);

		JRadioButton rbNU1 = new JRadioButton("");
		rbNU1.setName(RB_NU);
		rbNU1.setIcon(lightGrayIcon);
		rbNU1.setBounds(50, 10, 16, 23);
		getContentPane().add(rbNU1);

		JLabel lblNU1 = new JLabel("x");
		lblNU1.setName(RB_NU);
		lblNU1.setForeground(Color.LIGHT_GRAY);
		lblNU1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNU1.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblNU1.setBounds(50, 35, 16, 23);
		getContentPane().add(lblNU1);

		rbHalf = new JRadioButton("");
		rbHalf.setName(RB_HALF);
		rbHalf.addActionListener(adapterCCR);
		rbHalf.setIcon(grayIcon);
		rbHalf.setBounds(72, 10, 16, 23);
		getContentPane().add(rbHalf);

		JLabel lblHalf = new JLabel("H");
		lblHalf.setName(RB_HALF);
		lblHalf.addMouseListener(adapterCCR);
		lblHalf.setHorizontalAlignment(SwingConstants.CENTER);
		lblHalf.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblHalf.setBounds(72, 35, 16, 23);
		getContentPane().add(lblHalf);

		JRadioButton rbNU2 = new JRadioButton("");
		rbNU2.setName(RB_NU);
		rbNU2.setIcon(lightGrayIcon);
		rbNU2.setBounds(94, 10, 16, 23);
		getContentPane().add(rbNU2);

		JLabel lblNU2 = new JLabel("x");
		lblNU2.setName(RB_NU);
		lblNU2.setForeground(Color.LIGHT_GRAY);
		lblNU2.setHorizontalAlignment(SwingConstants.CENTER);
		lblNU2.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblNU2.setBounds(94, 35, 16, 23);
		getContentPane().add(lblNU2);

		rbParity = new JRadioButton("");
		rbParity.setName(RB_PARITY);
		rbParity.addActionListener(adapterCCR);
		rbParity.setIcon(grayIcon);
		rbParity.setBounds(116, 10, 16, 23);
		getContentPane().add(rbParity);

		JLabel lblParity = new JLabel("P");
		lblParity.setName(RB_PARITY);
		lblParity.addMouseListener(adapterCCR);
		lblParity.setHorizontalAlignment(SwingConstants.CENTER);
		lblParity.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblParity.setBounds(116, 35, 16, 23);
		getContentPane().add(lblParity);

		rbNeg = new JRadioButton("");
		rbNeg.setName(RB_NEG);
		rbNeg.addActionListener(adapterCCR);
		rbNeg.setIcon(grayIcon);
		rbNeg.setBounds(138, 10, 16, 23);
		getContentPane().add(rbNeg);

		JLabel lblNeg = new JLabel("N");
		lblNeg.setName(RB_NEG);
		lblNeg.addMouseListener(adapterCCR);
		lblNeg.setHorizontalAlignment(SwingConstants.CENTER);
		lblNeg.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblNeg.setBounds(138, 35, 16, 23);
		getContentPane().add(lblNeg);

		rbCarry = new JRadioButton("");
		rbCarry.setName(RB_CARRY);
		rbCarry.addActionListener(adapterCCR);
		rbCarry.setIcon(grayIcon);
		rbCarry.setBounds(160, 10, 16, 23);
		getContentPane().add(rbCarry);

		JLabel lblCarry = new JLabel("C");
		lblCarry.setName(RB_CARRY);
		lblCarry.addMouseListener(adapterCCR);
		lblCarry.setHorizontalAlignment(SwingConstants.CENTER);
		lblCarry.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblCarry.setBounds(160, 35, 16, 23);
		getContentPane().add(lblCarry);

		ccHexValue = new HDNbox(0, 128, 0, false);
		ccHexValue.addHDNumberValueChangedListener(adapterCCR);
		ccHexValue.setValueQuiet(240);
		ccHexValue.setPreferredSize(new Dimension(30, 20));
		ccHexValue.setMinimumSize(new Dimension(40, 20));
		ccHexValue.setMinValue(0);
		ccHexValue.setMaxValue(255);
		ccHexValue.setHexDisplay("%02X");
		ccHexValue.setBounds(182, 22, 25, 20);
		getContentPane().add(ccHexValue);
	}// initialize

	private static final String RB_NU = "Not Used";
	private static final String RB_SIGN = "rbSign";
	private static final String RB_ZERO = "rbZero";
	private static final String RB_HALF = "rbHalf";
	private static final String RB_PARITY = "rbParity";
	private static final String RB_NEG = "rbNeg";
	private static final String RB_CARRY = "rbCarry";
	private JRadioButton rbSign;
	private JRadioButton rbCarry;
	private JRadioButton rbNeg;
	private JRadioButton rbZero;
	private JRadioButton rbHalf;
	private JRadioButton rbParity;

	//////////////////////////////////////////////////////////

	public class AdapterIF_V_CCR implements HDNumberValueChangeListener, ActionListener, MouseListener {

		/* HDNumberValueChangeListener */
		@Override
		public void valueChanged(HDNumberValueChangeEvent hDNumberValueChangeEvent) {
			byte newValue = (byte) hDNumberValueChangeEvent.getNewValue();
			setByHexValue(newValue);
		}// valueChanged

		/* ActionListener */
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			Component source = (Component) actionEvent.getSource();
			String name = source.getName();
			if (actionEvent.getSource() instanceof JRadioButton) {
				doFlagChange((JRadioButton) source, name);
			} // if

		}// actionPerformed

		/* MouseListener */
		@Override
		public void mouseClicked(MouseEvent mouseEvent) {
			String name = ((Component) mouseEvent.getSource()).getName();
			doFlagChange(name);
		}// mouseClicked

		@Override
		public void mouseEntered(MouseEvent mouseEvent) {
			/* Ignored */
		}// mouseEntered

		@Override
		public void mouseExited(MouseEvent mouseEvent) {
			/* Ignored */
		}// mouseExited

		@Override
		public void mousePressed(MouseEvent mouseEvent) {
			/* Ignored */
		}// mousePressed

		@Override
		public void mouseReleased(MouseEvent mouseEvent) {
			/* Ignored */
		}// mouseReleased

	}// class AdapterIF_V_CCR

}// class V_IF_CCR
