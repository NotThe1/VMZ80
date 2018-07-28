package hardware.View;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
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

	private String title = "?";

	@Override
	public void run() {
		// TODO Auto-generated method stub
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
		if(numberBox.equals(regF)) {
		 value = (byte) (numberBox.equals(regF)?(newValue & FLAG_MASK):newValue);
		 numberBox.setValueQuiet(value);
		}// if Condition Code Register
			
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
		setLayout(new GridLayout(1, 10, 1, 0));

		JPanel panel = new JPanel();
		add(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 90, 40, 40, 40, 40, 40, 40, 40, 40, 40 };
		gbl_panel.rowHeights = new int[] { 0 };// 70
		gbl_panel.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		gbl_panel.rowWeights = new double[] { 0.0 };
		panel.setLayout(gbl_panel);

		tbMainAux = new JToggleButton(MAIN);
		tbMainAux.setSelected(false);
		tbMainAux.setMinimumSize(new Dimension(90, 23));
		tbMainAux.setPreferredSize(new Dimension(90, 23));
		tbMainAux.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tbMainAux.setText(tbMainAux.isSelected() ? AUXILARY : MAIN);
				setAllRegisterDisplays(tbMainAux.isSelected());
			}// actionPerformed
		});

		GridBagConstraints gbc_tbMainAux = new GridBagConstraints();
		gbc_tbMainAux.fill = GridBagConstraints.VERTICAL;
		gbc_tbMainAux.insets = new Insets(0, 0, 0, 5);
		gbc_tbMainAux.gridx = 0;
		gbc_tbMainAux.gridy = 0;
		panel.add(tbMainAux, gbc_tbMainAux);

		regA = new HDNumberBox();
		regA.setName("A");
		regA.addHDNumberValueChangedListener(adapterVPR);
		regA.setFont(new Font("Courier New", Font.BOLD, 15));
		regA.setPreferredSize(new Dimension(30, 40));
		regA.setHexDisplay("%02X");
		regA.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "A", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_regA = new GridBagConstraints();
		gbc_regA.anchor = GridBagConstraints.NORTHWEST;
		gbc_regA.insets = new Insets(0, 0, 0, 5);
		gbc_regA.gridx = 1;
		gbc_regA.gridy = 0;
		panel.add(regA, gbc_regA);

		regB = new HDNumberBox();
		regB.setName("B");
		regB.addHDNumberValueChangedListener(adapterVPR);
		regB.setFont(new Font("Courier New", Font.BOLD, 15));
		regB.setPreferredSize(new Dimension(30, 40));
		regB.setHexDisplay("%02X");
		regB.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "B", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_regB = new GridBagConstraints();
		gbc_regB.anchor = GridBagConstraints.NORTHWEST;
		gbc_regB.insets = new Insets(0, 0, 0, 5);
		gbc_regB.gridx = 2;
		gbc_regB.gridy = 0;
		panel.add(regB, gbc_regB);

		regC = new HDNumberBox();
		regC.setName("C");
		regC.addHDNumberValueChangedListener(adapterVPR);
		regC.setFont(new Font("Courier New", Font.BOLD, 15));
		regC.setPreferredSize(new Dimension(30, 40));
		regC.setHexDisplay("%02X");
		regC.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "C", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_regC = new GridBagConstraints();
		gbc_regC.anchor = GridBagConstraints.NORTHWEST;
		gbc_regC.insets = new Insets(0, 0, 0, 5);
		gbc_regC.gridx = 3;
		gbc_regC.gridy = 0;
		panel.add(regC, gbc_regC);

		regD = new HDNumberBox();
		regD.setName("D");
		regD.addHDNumberValueChangedListener(adapterVPR);
		regD.setFont(new Font("Courier New", Font.BOLD, 15));
		regD.setPreferredSize(new Dimension(30, 40));
		regD.setHexDisplay("%02X");
		regD.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "D", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_regD = new GridBagConstraints();
		gbc_regD.anchor = GridBagConstraints.NORTHWEST;
		gbc_regD.insets = new Insets(0, 0, 0, 5);
		gbc_regD.gridx = 4;
		gbc_regD.gridy = 0;
		panel.add(regD, gbc_regD);

		regE = new HDNumberBox();
		regE.setName("E");
		regE.addHDNumberValueChangedListener(adapterVPR);
		regE.setFont(new Font("Courier New", Font.BOLD, 15));
		regE.setPreferredSize(new Dimension(30, 40));
		regE.setHexDisplay("%02X");
		regE.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "E", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_regE = new GridBagConstraints();
		gbc_regE.anchor = GridBagConstraints.NORTHWEST;
		gbc_regE.insets = new Insets(0, 0, 0, 5);
		gbc_regE.gridx = 5;
		gbc_regE.gridy = 0;
		panel.add(regE, gbc_regE);

		regH = new HDNumberBox();
		regH.setName("H");
		regH.addHDNumberValueChangedListener(adapterVPR);
		regH.setFont(new Font("Courier New", Font.BOLD, 15));
		regH.setPreferredSize(new Dimension(30, 40));
		regH.setHexDisplay("%02X");
		regH.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "H", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_regH = new GridBagConstraints();
		gbc_regH.anchor = GridBagConstraints.NORTHWEST;
		gbc_regH.insets = new Insets(0, 0, 0, 5);
		gbc_regH.gridx = 6;
		gbc_regH.gridy = 0;
		panel.add(regH, gbc_regH);

		regL = new HDNumberBox();
		regL.setName("L");
		regL.addHDNumberValueChangedListener(adapterVPR);
		regL.setFont(new Font("Courier New", Font.BOLD, 15));
		regL.setPreferredSize(new Dimension(30, 40));
		regL.setHexDisplay("%02X");
		regL.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "L", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_regL = new GridBagConstraints();
		gbc_regL.anchor = GridBagConstraints.NORTHWEST;
		gbc_regL.insets = new Insets(0, 0, 0, 5);
		gbc_regL.gridx = 7;
		gbc_regL.gridy = 0;
		panel.add(regL, gbc_regL);
		regL.setMinimumSize(new Dimension(38, 54));
		regL.setMaximumSize(new Dimension(38, 54));

		regF = new HDNumberBox();
		regF.setName("F");
		regF.addHDNumberValueChangedListener(adapterVPR);
		regF.setFont(new Font("Courier New", Font.BOLD, 15));
		regF.setPreferredSize(new Dimension(30, 40));
		regF.setHexDisplay("%02X");
		regF.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "F", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_regF = new GridBagConstraints();
		gbc_regF.insets = new Insets(0, 0, 0, 5);
		gbc_regF.anchor = GridBagConstraints.NORTHWEST;
		gbc_regF.gridx = 8;
		gbc_regF.gridy = 0;
		panel.add(regF, gbc_regF);

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
