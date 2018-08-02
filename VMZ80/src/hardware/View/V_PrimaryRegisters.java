package hardware.View;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import codeSupport.Z80;
import hardware.WorkingRegisterSet;
import utilities.hdNumberBox.HDNumberBox;
import utilities.hdNumberBox.HDNumberValueChangeEvent;
import utilities.hdNumberBox.HDNumberValueChangeListener;

public class V_PrimaryRegisters extends JPanel implements Runnable {
	private static final long serialVersionUID = 1L;

	WorkingRegisterSet wrs = WorkingRegisterSet.getInstance();
	AdapterV_PrimaryRegisters adapterVPR = new AdapterV_PrimaryRegisters();

	HashMap<HDNumberBox, DisplayRegisterAttributes> displayRegisters = new HashMap<>();

	private String title = "Primary Registers";

	@Override
	public void run() {
		setAllRegisterDisplays(tbMainAux.isSelected());
	}// run

	public void setTitle(String title) {
		this.title = title;
		setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), title, TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
	}// setTitle

	public String getTitle() {
		return title;
	}// ;

	private void doValueChanged(byte newValue, HDNumberBox numberBox) {
		byte value = newValue;
		if (numberBox.equals(regF)) {
			value = (byte) (numberBox.equals(regF) ? (newValue & FLAG_MASK) : newValue);
			numberBox.setValueQuiet(value);
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
		regDisplay.setValueQuiet(wrs.getReg(regCCR));
	}// setRegisterValue

	public V_PrimaryRegisters(String title) {
		this();
		this.title = title;
	}// Constructor

	public V_PrimaryRegisters() {
		setPreferredSize(new Dimension(467, 88));
		setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Primary Registers", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		initialize();
		appInit();
	}// Constructor

	private void appInit() {
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

		tbMainAux = new JToggleButton(MAIN);
		tbMainAux.setBounds(26, 23, 90, 50);
		tbMainAux.setSelected(false);
		tbMainAux.setMinimumSize(new Dimension(90, 23));
		tbMainAux.setPreferredSize(new Dimension(90, 23));
		tbMainAux.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tbMainAux.setText(tbMainAux.isSelected() ? AUXILARY : MAIN);
				setAllRegisterDisplays(tbMainAux.isSelected());
			}// actionPerformed
		});
		setLayout(null);
		add(tbMainAux);

		regA = new HDNumberBox(0,0xFF,0,false);
		regA.setToolTipText("Register A");
		regA.setBounds(121, 23, 30, 40);
		regA.setName("A");
		regA.addHDNumberValueChangedListener(adapterVPR);
		regA.setFont(new Font("Courier New", Font.BOLD, 15));
		regA.setPreferredSize(new Dimension(30, 40));
		regA.setHexDisplay("%02X");
		regA.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "A", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		add(regA);

		regB = new HDNumberBox(0,0xFF,0,false);
		regB.setToolTipText("Register B");
		regB.setBounds(161, 23, 30, 40);
		regB.setName("B");
		regB.addHDNumberValueChangedListener(adapterVPR);
		regB.setFont(new Font("Courier New", Font.BOLD, 15));
		regB.setPreferredSize(new Dimension(30, 40));
		regB.setHexDisplay("%02X");
		regB.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "B", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		add(regB);

		regC = new HDNumberBox(0,0xFF,0,false);
		regC.setToolTipText("Register C");
		regC.setBounds(201, 23, 30, 40);
		regC.setName("C");
		regC.addHDNumberValueChangedListener(adapterVPR);
		regC.setFont(new Font("Courier New", Font.BOLD, 15));
		regC.setPreferredSize(new Dimension(30, 40));
		regC.setHexDisplay("%02X");
		regC.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "C", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		add(regC);

		regD = new HDNumberBox(0,0xFF,0,false);
		regD.setToolTipText("Register D");
		regD.setBounds(241, 23, 30, 40);
		regD.setName("D");
		regD.addHDNumberValueChangedListener(adapterVPR);
		regD.setFont(new Font("Courier New", Font.BOLD, 15));
		regD.setPreferredSize(new Dimension(30, 40));
		regD.setHexDisplay("%02X");
		regD.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "D", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		add(regD);

		regE =  new HDNumberBox(0,0xFF,0,false);
		regE.setToolTipText("Register E");
		regE.setBounds(281, 23, 30, 40);
		regE.setName("E");
		regE.addHDNumberValueChangedListener(adapterVPR);
		regE.setFont(new Font("Courier New", Font.BOLD, 15));
		regE.setPreferredSize(new Dimension(30, 40));
		regE.setHexDisplay("%02X");
		regE.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "E", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		add(regE);

		regH = new HDNumberBox(0,0xFF,0,false);
		regH.setToolTipText("Register H");
		regH.setBounds(321, 23, 30, 40);
		regH.setName("H");
		regH.addHDNumberValueChangedListener(adapterVPR);
		regH.setFont(new Font("Courier New", Font.BOLD, 15));
		regH.setPreferredSize(new Dimension(30, 40));
		regH.setHexDisplay("%02X");
		regH.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "H", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		add(regH);

		regL = new HDNumberBox(0,0xFF,0,false);
		regL.setToolTipText("Register L");
		regL.setBounds(361, 23, 30, 40);
		regL.setName("L");
		regL.addHDNumberValueChangedListener(adapterVPR);
		regL.setFont(new Font("Courier New", Font.BOLD, 15));
		regL.setPreferredSize(new Dimension(30, 40));
		regL.setHexDisplay("%02X");
		regL.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "L", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		add(regL);

		regF = new HDNumberBox(0,0xFF,0,false);
		regF.setToolTipText("Register F (Flags)");
		regF.setBounds(401, 23, 30, 40);
		regF.setName("F");
		regF.addHDNumberValueChangedListener(adapterVPR);
		regF.setFont(new Font("Courier New", Font.BOLD, 15));
		regF.setPreferredSize(new Dimension(30, 40));
		regF.setHexDisplay("%02X");
		regF.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "F", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		add(regF);

	}// initialize

	private static final String MAIN = "Main";
	private static final String AUXILARY = "Auxilary";

	private static final byte FLAG_MASK = (byte) 0b11010111;
	private HDNumberBox regA;
	private HDNumberBox regB;
	private HDNumberBox regC;
	private HDNumberBox regD;
	private HDNumberBox regE;
	private HDNumberBox regH;
	private HDNumberBox regL;
	private HDNumberBox regF;
	private JToggleButton tbMainAux;

	private class AdapterV_PrimaryRegisters implements HDNumberValueChangeListener {

		@Override
		public void valueChanged(HDNumberValueChangeEvent hDNumberValueChangeEvent) {
			byte newValue = (byte) hDNumberValueChangeEvent.getNewValue();
			doValueChanged(newValue, (HDNumberBox) hDNumberValueChangeEvent.getSource());
		}// valueChanged

	}// class AdapterV_PrimaryRegisters

	private class DisplayRegisterAttributes {
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

		private String getTitleMain() {
			return titleMain;
		}

		private void setTitleMain(String titleMain) {
			this.titleMain = titleMain;
		}

		private Z80.Register getRegMain() {
			return regMain;
		}

		private void setRegMain(Z80.Register regMain) {
			this.regMain = regMain;
		}

		private String getTitleAlt() {
			return titleAlt;
		}

		private void setTitleAlt(String titleAlt) {
			this.titleAlt = titleAlt;
		}

		private Z80.Register getRegAlt() {
			return regAlt;
		}

		private void setRegAlt(Z80.Register regAlt) {
			this.regAlt = regAlt;
		}

	}// DisplayRegisterAttributed

}// class V_PrimaryRegisters
