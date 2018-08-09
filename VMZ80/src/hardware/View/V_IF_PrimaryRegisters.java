package hardware.View;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JInternalFrame;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import codeSupport.Z80;
import hardware.WorkingRegisterSet;
import utilities.hdNumberBox.HDNumberBox;
import utilities.hdNumberBox.HDNumberValueChangeEvent;
import utilities.hdNumberBox.HDNumberValueChangeListener;

public class V_IF_PrimaryRegisters extends JInternalFrame implements Runnable {
	private static final long serialVersionUID = 1L;

	AdapterV_IF_PrimaryRegisters adapterViewPrimaryRegisters = new AdapterV_IF_PrimaryRegisters();

	WorkingRegisterSet wrs = WorkingRegisterSet.getInstance();
	HashMap<HDNumberBox, DisplayRegisterAttributes> displayRegisters = new HashMap<>();
	
	@Override
	public void run() {
		setAllRegisterDisplays(tbMainAux.isSelected());
	}// run

	private void doValueChanged(byte newValue, HDNumberBox numberBox) {
		byte value = newValue;
		if (numberBox.equals(regF)) {
			value = (byte) (numberBox.equals(regF) ? (newValue & FLAG_MASK) : newValue);
			numberBox.setValueQuiet(value & 0xFF);
		} // if Condition Code Register

		DisplayRegisterAttributes dra = displayRegisters.get(numberBox);
		if (tbMainAux.isSelected()) {
			wrs.setReg(dra.getRegAlt(), value);
		} else {
			wrs.setReg(dra.getRegMain(), value);
		} // if
	}// doValueChanged

	private void setAllRegisterDisplays(boolean auxRegisters) {
		Set<HDNumberBox> ks = displayRegisters.keySet();
		for (HDNumberBox numberBox : ks) {
			DisplayRegisterAttributes dra = displayRegisters.get(numberBox);
			if (auxRegisters) {
				setRegisterDisplayTitle(numberBox, dra.getTitleAlt());
				setRegisterValueQuiet(numberBox, dra.getRegAlt());
			} else {
				setRegisterDisplayTitle(numberBox, dra.getTitleMain());
				setRegisterValueQuiet(numberBox, dra.getRegMain());
			} // if
		} // for
	}// setRegisterDisplay

	private void setRegisterDisplayTitle(HDNumberBox reg, String title) {
		reg.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), title, TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
	}// setRegisterDisplayTitle

	private void setRegisterValueQuiet(HDNumberBox regDisplay, Z80.Register regCCR) {
		regDisplay.setValueQuiet(wrs.getReg(regCCR)&0xFF);
	}// setRegisterValue

	/////////////////////////////////////////////////////////////////////////////////
	/**
	 * Create the frame.
	 */
	public V_IF_PrimaryRegisters() {
		initialize();
		appInit();
	}// Constructor

	private void appInit() {
		
		/* initialize the register control array */
		displayRegisters.put(regA, new DisplayRegisterAttributes("A", Z80.Register.A, "A'", Z80.Register.Ap));
		displayRegisters.put(regB, new DisplayRegisterAttributes("B", Z80.Register.B, "B'", Z80.Register.Bp));
		displayRegisters.put(regC, new DisplayRegisterAttributes("C", Z80.Register.C, "C'", Z80.Register.Cp));
		displayRegisters.put(regD, new DisplayRegisterAttributes("D", Z80.Register.D, "D'", Z80.Register.Dp));
		displayRegisters.put(regE, new DisplayRegisterAttributes("E", Z80.Register.E, "E'", Z80.Register.Ep));
		displayRegisters.put(regH, new DisplayRegisterAttributes("H", Z80.Register.H, "H'", Z80.Register.Hp));
		displayRegisters.put(regL, new DisplayRegisterAttributes("L", Z80.Register.L, "L'", Z80.Register.Lp));
		displayRegisters.put(regF, new DisplayRegisterAttributes("F", Z80.Register.F, "F'", Z80.Register.Fp));

		setAllRegisterDisplays(tbMainAux.isSelected());


	}// appInit

	private void initialize() {
		setTitle("Primary Registers");
		setBounds(0, 0, 460, 100);
		setIconifiable(true);
		getContentPane().setLayout(null);

		tbMainAux = new JToggleButton(MAIN);
		tbMainAux.addActionListener(adapterViewPrimaryRegisters);
		tbMainAux.setBounds(10, 19, 84, 23);
		getContentPane().add(tbMainAux);

		regA = new HDNumberBox(0,0xFF,0,false);
		regA.setBorder(new TitledBorder(null, "A", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		regA.setHexDisplay("%02X");
		regA.setName("A");
		regA.setBounds(110, 10, 35, 40);
		regA.setToolTipText("Register A");
		regA.setFont(new Font("Courier New", Font.BOLD, 15));
		regA.addHDNumberValueChangedListener(adapterViewPrimaryRegisters);
		getContentPane().add(regA);

		regB = new HDNumberBox(0,0xFF,0,false);
		regB.setBorder(new TitledBorder(null, "B", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		regB.setHexDisplay("%02X");
		regB.setName("B");
		regB.setBounds(150, 10, 35, 40);
		regB.setToolTipText("Register B");
		regB.setFont(new Font("Courier New", Font.BOLD, 15));
		regB.addHDNumberValueChangedListener(adapterViewPrimaryRegisters);		
		getContentPane().add(regB);

		regC = new HDNumberBox(0,0xFF,0,false);
		regC.setBorder(new TitledBorder(null, "C", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		regC.setHexDisplay("%02X");
		regC.setName("C");
		regC.setBounds(190, 10, 35, 40);
		regC.setToolTipText("Register C");
		regC.setFont(new Font("Courier New", Font.BOLD, 15));
		regC.addHDNumberValueChangedListener(adapterViewPrimaryRegisters);
		getContentPane().add(regC);

		regD = new HDNumberBox(0,0xFF,0,false);
		regD.setBorder(new TitledBorder(null, "D", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		regD.setHexDisplay("%02X");
		regD.setName("D");
		regD.setBounds(230, 10, 35, 40);
		regD.setToolTipText("Register D");
		regD.setFont(new Font("Courier New", Font.BOLD, 15));
		regD.addHDNumberValueChangedListener(adapterViewPrimaryRegisters);
		getContentPane().add(regD);

		regE = new HDNumberBox(0,0xFF,0,false);
		regE.setBorder(new TitledBorder(null, "E", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		regE.setHexDisplay("%02X");
		regE.setName("E");
		regE.setBounds(270, 10, 35, 40);
		regE.setToolTipText("Register E");
		regE.setFont(new Font("Courier New", Font.BOLD, 15));
		regE.addHDNumberValueChangedListener(adapterViewPrimaryRegisters);
		getContentPane().add(regE);

		regH = new HDNumberBox(0,0xFF,0,false);
		regH.setBorder(new TitledBorder(null, "H", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		regH.setHexDisplay("%02X");
		regH.setName("H");
		regH.setBounds(310, 10, 35, 40);
		regH.setToolTipText("Register H");
		regH.setFont(new Font("Courier New", Font.BOLD, 15));
		regH.addHDNumberValueChangedListener(adapterViewPrimaryRegisters);
		getContentPane().add(regH);

		regL = new HDNumberBox(0,0xFF,0,false);
		regL.setBorder(new TitledBorder(null, "L", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		regL.setHexDisplay("%02X");
		regL.setName("L");
		regL.setBounds(350, 10, 35, 40);
		regL.setToolTipText("Register L");
		regL.setFont(new Font("Courier New", Font.BOLD, 15));
		regL.addHDNumberValueChangedListener(adapterViewPrimaryRegisters);
		getContentPane().add(regL);

		regF = new HDNumberBox(0,0xFF,0,false);
		regF.setBorder(new TitledBorder(null, "F", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		regF.setHexDisplay("%02X");
		regF.setName("F");
		regF.setBounds(400, 10, 35, 40);
		regF.setToolTipText("Register F");
		regF.setFont(new Font("Courier New", Font.BOLD, 15));
		regF.addHDNumberValueChangedListener(adapterViewPrimaryRegisters);

		getContentPane().add(regF);

	}// initialize

	private static final String MAIN = "Main";
	private static final String AUXILARY = "Auxilary";

	private static final byte FLAG_MASK = (byte) 0b11010111;
	private HDNumberBox regF;
	private HDNumberBox regL;
	private HDNumberBox regH;
	private HDNumberBox regE;
	private HDNumberBox regD;
	private HDNumberBox regC;
	private HDNumberBox regB;
	private HDNumberBox regA;
	private JToggleButton tbMainAux;

	//////////////////////////////////////////////////////////////////////////

	 private class AdapterV_IF_PrimaryRegisters implements HDNumberValueChangeListener, ActionListener {
		/* HDNumberValueChangeListener */
		@Override
		public void valueChanged(HDNumberValueChangeEvent hDNumberValueChangeEvent) {
			byte newValue = (byte) hDNumberValueChangeEvent.getNewValue();
			doValueChanged(newValue, (HDNumberBox) hDNumberValueChangeEvent.getSource());
		}// valueChanged

		/* ActionListener */
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			tbMainAux.setText(tbMainAux.isSelected() ? AUXILARY : MAIN);
			setAllRegisterDisplays(tbMainAux.isSelected());
		}// actionPerformed

	}// class AdapterV_PrimaryRegisters

	//////////////////////////////////////////////////////////////////////////

	 class DisplayRegisterAttributes {
		private String titleMain;
		private Z80.Register regMain;
		private String titleAlt;
		private Z80.Register regAlt;

		public DisplayRegisterAttributes(String titleMain, Z80.Register regMain, String titleAlt, Z80.Register regAlt) {
			this.setTitleMain(titleMain);
			this.setRegMain(regMain);
			this.setTitleAlt(titleAlt);
			this.setRegAlt(regAlt);
		}// constructor

		public String getTitleMain() {
			return titleMain;
		}// getTitleMain

		public void setTitleMain(String titleMain) {
			this.titleMain = titleMain;
		}// setTitleMain

		public Z80.Register getRegMain() {
			return regMain;
		}// getRegMain

		public void setRegMain(Z80.Register regMain) {
			this.regMain = regMain;
		}// setRegMain

		public String getTitleAlt() {
			return titleAlt;
		}// getTitleAlt

		public void setTitleAlt(String titleAlt) {
			this.titleAlt = titleAlt;
		}// setTitleAlt

		public Z80.Register getRegAlt() {
			return regAlt;
		}// getRegAlt

		public void setRegAlt(Z80.Register regAlt) {
			this.regAlt = regAlt;
		}// setRegAlt

	}// DisplayRegisterAttributes

}// class V_IF_PrimaryRegisters
