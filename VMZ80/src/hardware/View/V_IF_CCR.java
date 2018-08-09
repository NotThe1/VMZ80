package hardware.View;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import hardware.WorkingRegisterSet;
import utilities.hdNumberBox.HDNumberBox;

public class V_IF_CCR extends JInternalFrame implements Runnable {
	private static final long serialVersionUID = 1L;

	private static RoundIcon redIcon = new RoundIcon(Color.RED);
	private static RoundIcon grayIcon = new RoundIcon(Color.GRAY);
	private static RoundIcon lightGrayIcon = new RoundIcon(Color.LIGHT_GRAY);
	WorkingRegisterSet wrs = WorkingRegisterSet.getInstance();
	private HDNumberBox ccHexValue;

	@Override
	public void run() {
		// TODO Auto-generated method stub
	}// run
	
	public V_IF_CCR() {
		initialize();
		appInit();
	}// Constructor
	
	private void appInit() {

	}// appInit

	public void initialize() {
		setTitle("Condition Code Registers");
		setIconifiable(true);
		setBounds(0, 410, 230, 100);
		getContentPane().setLayout(null);

		JRadioButton rbSign = new JRadioButton("");
		rbSign.setName(RB_SIGN);
		rbSign.setIcon(grayIcon);
		rbSign.setBounds(6, 10, 16, 23);
		getContentPane().add(rbSign);

		JLabel lblNewLabel = new JLabel("S");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblNewLabel.setBounds(6, 35, 16, 23);
		getContentPane().add(lblNewLabel);

		JRadioButton rbZero = new JRadioButton("");
		rbZero.setName(RB_ZERO);
		rbZero.setIcon(grayIcon);
		rbZero.setBounds(28, 10, 16, 23);
		getContentPane().add(rbZero);

		JLabel lblZ = new JLabel("Z");
		lblZ.setHorizontalAlignment(SwingConstants.CENTER);
		lblZ.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblZ.setBounds(28, 35, 16, 23);
		getContentPane().add(lblZ);

		JRadioButton rbNU1 = new JRadioButton("");
		rbNU1.setName(RB_NU);
		rbNU1.setIcon(lightGrayIcon);
		rbNU1.setBounds(50, 10, 16, 23);
		getContentPane().add(rbNU1);

		JLabel lblNU1 = new JLabel("x");
		lblNU1.setForeground(Color.LIGHT_GRAY);
		lblNU1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNU1.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblNU1.setBounds(50, 35, 16, 23);
		getContentPane().add(lblNU1);

		JRadioButton rbHalf = new JRadioButton("");
		rbHalf.setName(RB_HALF);
		rbHalf.setIcon(grayIcon);
		rbHalf.setBounds(72, 10, 16, 23);
		getContentPane().add(rbHalf);

		JLabel lblH = new JLabel("H");
		lblH.setHorizontalAlignment(SwingConstants.CENTER);
		lblH.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblH.setBounds(72, 35, 16, 23);
		getContentPane().add(lblH);

		JRadioButton rbNU2 = new JRadioButton("");
		rbNU2.setName(RB_NU);
		rbNU2.setIcon(lightGrayIcon);
		rbNU2.setBounds(94, 10, 16, 23);
		getContentPane().add(rbNU2);

		JLabel lblX = new JLabel("x");
		lblX.setForeground(Color.LIGHT_GRAY);
		lblX.setHorizontalAlignment(SwingConstants.CENTER);
		lblX.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblX.setBounds(94, 35, 16, 23);
		getContentPane().add(lblX);

		JRadioButton rbParity = new JRadioButton("");
		rbParity.setName(RB_PARITY);
		rbParity.setIcon(grayIcon);
		rbParity.setBounds(116, 10, 16, 23);
		getContentPane().add(rbParity);

		JLabel lblP = new JLabel("P");
		lblP.setHorizontalAlignment(SwingConstants.CENTER);
		lblP.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblP.setBounds(116, 35, 16, 23);
		getContentPane().add(lblP);

		JRadioButton rbNeg = new JRadioButton("");
		rbNeg.setName(RB_NEG);
		rbNeg.setIcon(grayIcon);
		rbNeg.setBounds(138, 10, 16, 23);
		getContentPane().add(rbNeg);

		JLabel lblN = new JLabel("N");
		lblN.setHorizontalAlignment(SwingConstants.CENTER);
		lblN.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblN.setBounds(138, 35, 16, 23);
		getContentPane().add(lblN);

		JRadioButton rbCarry = new JRadioButton("");
		rbCarry.setName(RB_CARRY);
		rbCarry.setIcon(grayIcon);
		rbCarry.setBounds(160, 10, 16, 23);
		getContentPane().add(rbCarry);

		JLabel lblC = new JLabel("C");
		lblC.setHorizontalAlignment(SwingConstants.CENTER);
		lblC.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblC.setBounds(160, 35, 16, 23);
		getContentPane().add(lblC);

		ccHexValue = new HDNumberBox(0, 128, 0, false);
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

}// class V_IF_CCR
