package hardware;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import codeSupport.Z80;
import utilities.hdNumberBox.HDNumberBox;
import utilities.hdNumberBox.HDNumberValueChangeEvent;
import utilities.hdNumberBox.HDNumberValueChangeListener;

public class ViewCCR extends JPanel implements Runnable {
	AdapterViewCCR adapterViewCCR = new AdapterViewCCR();
	ConditionCodeRegister ccr = ConditionCodeRegister.getInstance();
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		setColorsForAllIndicators();
	}//run	
	
	private void setHexValue(int rawValue) {
		byte value = (byte)rawValue;
		ccr.setSignFlag((value & Z80.BIT_SIGN)==Z80.BIT_SIGN);
		ccr.setZeroFlag((value & Z80.BIT_ZERO)==Z80.BIT_ZERO);
		ccr.setHFlag((value & Z80.BIT_AUX)==Z80.BIT_AUX);
		ccr.setPvFlag((value & Z80.BIT_PV)==Z80.BIT_PV);
		ccr.setNFlag((value & Z80.BIT_N)==Z80.BIT_N);
		ccr.setCarryFlag((value & Z80.BIT_CARRY)==Z80.BIT_CARRY);
		setColorsForAllIndicators();
	}//setHexValue
	
	private void setColorsForAllIndicators() {
		ccSign.setForeground(ccr.isSignFlagSet()?ON:OFF);
		ccZero.setForeground(ccr.isZeroFlagSet()?ON:OFF);
		ccHalfCarry.setForeground(ccr.isHFlagSet()?ON:OFF);
		ccParity.setForeground(ccr.isPvFlagSet()?ON:OFF);
		ccAddSubtract.setForeground(ccr.isNFlagSet()?ON:OFF);
		ccCarry.setForeground(ccr.isCarryFlagSet()?ON:OFF);
		ccHexValue.setValueQuiet(0x00 << 24 | ccr.getConditionCode() & 0xff);
		
	}//setColorsForAllIndicators

	private void appInit() {
		ccHexValue.setHexDisplay("%02X");
		setColorsForAllIndicators();
	}// appInit

	public ViewCCR() {
		initialize();
		appInit();
	}// Constructor

	/**
	 * Create the panel.
	 */
	private void initialize() {
		setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Condition Codes", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));

		this.setSize(500, 70);
		FlowLayout flowLayout = new FlowLayout(FlowLayout.LEFT, 5, 5);
		flowLayout.setAlignOnBaseline(true);
		setLayout(flowLayout);

		ccSign = new JLabel("S");
		ccSign.setToolTipText("Sign");
		ccSign.setForeground(OFF);
		ccSign.setName(CC_SIGN);
		ccSign.addMouseListener(adapterViewCCR);
		add(ccSign);
		ccSign.setHorizontalAlignment(SwingConstants.CENTER);
		ccSign.setBorder(new LineBorder(new Color(0, 0, 0)));
		ccSign.setFont(new Font("Tahoma", Font.BOLD, 15));
		ccSign.setAlignmentX(0.5f);

		ccZero = new JLabel("Z");
		ccZero.setToolTipText("Zero");
		ccZero.setName(CC_ZERO);
		ccZero.addMouseListener(adapterViewCCR);
		add(ccZero);
		ccZero.setHorizontalAlignment(SwingConstants.CENTER);
		ccZero.setFont(new Font("Tahoma", Font.BOLD, 15));
		ccZero.setBorder(new LineBorder(new Color(0, 0, 0)));
		ccZero.setAlignmentX(0.5f);

		JLabel ccNU5 = new JLabel("X");
		ccNU5.setToolTipText("Not Used");
		add(ccNU5);
		ccNU5.setForeground(Color.LIGHT_GRAY);
		ccNU5.setHorizontalAlignment(SwingConstants.CENTER);
		ccNU5.setFont(new Font("Tahoma", Font.BOLD, 15));
		ccNU5.setBorder(new LineBorder(new Color(0, 0, 0)));
		ccNU5.setAlignmentX(0.5f);

		ccHalfCarry = new JLabel("H");
		ccHalfCarry.setToolTipText("Half Carry");
		ccHalfCarry.setName(CC_HALF_CARRY);
		ccHalfCarry.addMouseListener(adapterViewCCR);
		add(ccHalfCarry);
		ccHalfCarry.setHorizontalAlignment(SwingConstants.CENTER);
		ccHalfCarry.setFont(new Font("Tahoma", Font.BOLD, 15));
		ccHalfCarry.setBorder(new LineBorder(new Color(0, 0, 0)));
		ccHalfCarry.setAlignmentX(0.5f);

		JLabel ccNU3 = new JLabel("X");
		ccNU3.setToolTipText("Not Used");
		add(ccNU3);
		ccNU3.setForeground(Color.LIGHT_GRAY);
		ccNU3.setHorizontalAlignment(SwingConstants.CENTER);
		ccNU3.setFont(new Font("Tahoma", Font.BOLD, 15));
		ccNU3.setBorder(new LineBorder(new Color(0, 0, 0)));
		ccNU3.setAlignmentX(0.5f);

		ccParity = new JLabel("P");
		ccParity.setToolTipText("Parity/Overflow");
		ccParity.setName(CC_PARITY);
		ccParity.addMouseListener(adapterViewCCR);
		add(ccParity);
		ccParity.setHorizontalAlignment(SwingConstants.CENTER);
		ccParity.setFont(new Font("Tahoma", Font.BOLD, 15));
		ccParity.setBorder(new LineBorder(new Color(0, 0, 0)));
		ccParity.setAlignmentX(0.5f);

		ccAddSubtract = new JLabel("N");
		ccAddSubtract.setToolTipText("Add/Subtract");
		ccAddSubtract.setName(CC_ADD_SUBTRACT);
		ccAddSubtract.addMouseListener(adapterViewCCR);
		add(ccAddSubtract);
		ccAddSubtract.setHorizontalAlignment(SwingConstants.CENTER);
		ccAddSubtract.setFont(new Font("Tahoma", Font.BOLD, 15));
		ccAddSubtract.setBorder(new LineBorder(new Color(0, 0, 0)));
		ccAddSubtract.setAlignmentX(0.5f);

		ccCarry = new JLabel("C");
		ccCarry.setToolTipText("Carry");
		ccCarry.setName(CC_CARRY);
		ccCarry.addMouseListener(adapterViewCCR);
		add(ccCarry);
		ccCarry.setHorizontalAlignment(SwingConstants.CENTER);
		ccCarry.setFont(new Font("Tahoma", Font.BOLD, 15));
		ccCarry.setBorder(new LineBorder(new Color(0, 0, 0)));
		ccCarry.setAlignmentX(0.5f);

		ccHexValue = new HDNumberBox(0, 128, 0, false);
		ccHexValue.setValueQuiet(0xF0);
		ccHexValue.addHDNumberValueChangedListener(adapterViewCCR);
		ccHexValue.setMinimumSize(new Dimension(40, 20));
		ccHexValue.setPreferredSize(new Dimension(30, 20));
		ccHexValue.setMinValue(00);
		ccHexValue.setMaxValue(255);
		add(ccHexValue);
	}// initialize

	public class AdapterViewCCR implements MouseListener, HDNumberValueChangeListener{

		/*          MouseListener         */

		@Override
		public void mouseClicked(MouseEvent mouseEvent) {
			String name = ((JComponent) mouseEvent.getSource()).getName();	
			switch (name) {
			case CC_SIGN:
				ccr.setSignFlag(!ccr.isSignFlagSet());
				break;
			case CC_ZERO:
				ccr.setZeroFlag(!ccr.isZeroFlagSet());
				break;
			case CC_HALF_CARRY:
				ccr.setHFlag(!ccr.isHFlagSet());
				break;
			case CC_PARITY:
				ccr.setPvFlag(!ccr.isPvFlagSet());
				break;
			case CC_ADD_SUBTRACT:
				ccr.setNFlag(!ccr.isNFlagSet());
				break;
			case CC_CARRY:
				ccr.setCarryFlag(!ccr.isCarryFlagSet());
				break;
			}// switch
			
			setColorsForAllIndicators();
		}// mouseClicked

		@Override
		public void mouseEntered(MouseEvent arg0) {
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
		}

		/*          HDNumberValueChangeListener         */
		@Override
		public void valueChanged(HDNumberValueChangeEvent hDNumberValueChangeEvent) {
			setHexValue(hDNumberValueChangeEvent.getNewValue());
		}//valueChanged

	}//

	private static final long serialVersionUID = 1L;
	private JLabel ccZero;
	private JLabel ccSign;
	private JLabel ccHalfCarry;
	private JLabel ccParity;
	private JLabel ccAddSubtract;
	private JLabel ccCarry;

	private final static String CC_SIGN = "ccSign";
	private final static String CC_ZERO = "ccZero";
	private final static String CC_HALF_CARRY = "ccHalfCarry";
	private final static String CC_PARITY = "ccParity";
	private final static String CC_ADD_SUBTRACT = "ccAddSubtract";
	private final static String CC_CARRY = "ccCarry";
	
	private final static Color ON =Color.RED;
	private final static Color OFF = Color.DARK_GRAY;
	private HDNumberBox ccHexValue;




}// class ViewCCR
