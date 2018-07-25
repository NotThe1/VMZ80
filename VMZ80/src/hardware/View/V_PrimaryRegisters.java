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

import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import codeSupport.Z80.Register;
import hardware.WorkingRegisterSet;

public class V_PrimaryRegisters extends JPanel  implements Runnable {
	private static final long serialVersionUID = 1L;

	WorkingRegisterSet wrs = WorkingRegisterSet.getInstance();

	private String title = "?";
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		setRegisterDisplay(tbMainAux.isSelected());
	}//run	


	public void setTitle(String title) {
		this.title = title;
		setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), title, TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
	}// setTitle

	public String getTitle() {
		return title;
	}// ;

	private void setRegisterDisplay(boolean auxRegisters) {
		if (auxRegisters) {
			regA.setTitle("A'");
			regA.setValue(wrs.getReg(Register.Ap));
			
			regB.setTitle("B'");
			regB.setValue(wrs.getReg(Register.Bp));

			regC.setTitle("C'");
			regC.setValue(wrs.getReg(Register.Cp));

			regD.setTitle("D'");
			regD.setValue(wrs.getReg(Register.Dp));

			regE.setTitle("E'");
			regE.setValue(wrs.getReg(Register.Hp));

			regH.setTitle("H'");
			regH.setValue(wrs.getReg(Register.Hp));

			regL.setTitle("L'");
			regL.setValue(wrs.getReg(Register.Lp));

			regF.setTitle("F'");
			regF.setValue(wrs.getReg(Register.Fp));
		} else {
			regA.setTitle("A");
			regA.setValue(wrs.getReg(Register.A));
			
			regB.setTitle("B");
			regB.setValue(wrs.getReg(Register.B));

			regC.setTitle("C");
			regC.setValue(wrs.getReg(Register.C));

			regD.setTitle("D");
			regD.setValue(wrs.getReg(Register.D));

			regE.setTitle("E");
			regH.setValue(wrs.getReg(Register.H));

			regH.setTitle("H");
			regH.setValue(wrs.getReg(Register.H));

			regL.setTitle("L");
			regL.setValue(wrs.getReg(Register.L));

			regF.setTitle("F");
			regF.setValue(wrs.getReg(Register.F));
			

		} // if
	}//

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
		setRegisterDisplay(tbMainAux.isSelected());
	}// appInit

	private void initialize() {
		setLayout(new GridLayout(1, 10, 1, 0));

		JPanel panel = new JPanel();
		add(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 40, 40, 40, 40, 40, 40, 40, 40, 40 };
		// gbl_panel.columnWidths = new int[]{50, 50,50, 50, 50, 50, 50, 50,50};
		gbl_panel.rowHeights = new int[] { 0 };// 70
		gbl_panel.columnWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		// gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE, 0.0, 0.0, 0.0, 0.0, 0.0};
		gbl_panel.rowWeights = new double[] { 0.0 };
		panel.setLayout(gbl_panel);

		tbMainAux = new JToggleButton(MAIN);
		tbMainAux.setSelected(false);
		tbMainAux.setMinimumSize(new Dimension(90, 23));
		tbMainAux.setPreferredSize(new Dimension(90, 23));
		tbMainAux.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tbMainAux.setText(tbMainAux.isSelected() ? AUXILARY : MAIN);
				setRegisterDisplay(tbMainAux.isSelected());
			}// actionPerformed
		});

		GridBagConstraints gbc_tbMainAux = new GridBagConstraints();
		gbc_tbMainAux.fill = GridBagConstraints.BOTH;
		gbc_tbMainAux.insets = new Insets(0, 0, 0, 5);
		gbc_tbMainAux.gridx = 0;
		gbc_tbMainAux.gridy = 0;
		panel.add(tbMainAux, gbc_tbMainAux);

		regA = new V_Register8Bit("A");
		GridBagConstraints gbc_regA = new GridBagConstraints();
		gbc_regA.fill = GridBagConstraints.HORIZONTAL;
		gbc_regA.anchor = GridBagConstraints.NORTH;
		gbc_regA.insets = new Insets(0, 0, 0, 5);
		gbc_regA.gridx = 1;
		gbc_regA.gridy = 0;
		panel.add(regA, gbc_regA);
		regA.setFont(new Font("Courier New", Font.BOLD, 15));
		regA.setMaximumSize(new Dimension(30, 54));
		regA.setMinimumSize(new Dimension(30, 54));
		GridBagLayout gbl_regA = new GridBagLayout();
		gbl_regA.columnWidths = new int[] { 0 };
		gbl_regA.rowHeights = new int[] { 0 };
		gbl_regA.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_regA.rowWeights = new double[] { Double.MIN_VALUE };
		regA.setLayout(gbl_regA);

		regB = new V_Register8Bit("B");
		GridBagConstraints gbc_regB = new GridBagConstraints();
		gbc_regB.fill = GridBagConstraints.HORIZONTAL;
		gbc_regB.anchor = GridBagConstraints.NORTH;
		gbc_regB.insets = new Insets(0, 0, 0, 5);
		gbc_regB.gridx = 2;
		gbc_regB.gridy = 0;
		panel.add(regB, gbc_regB);
		regB.setMinimumSize(new Dimension(30, 54));
		regB.setMaximumSize(new Dimension(30, 54));
		GridBagLayout gbl_regB = new GridBagLayout();
		gbl_regB.columnWidths = new int[] { 0 };
		gbl_regB.rowHeights = new int[] { 0 };
		gbl_regB.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_regB.rowWeights = new double[] { Double.MIN_VALUE };
		regB.setLayout(gbl_regB);

		regC = new V_Register8Bit("C");
		GridBagConstraints gbc_regC = new GridBagConstraints();
		gbc_regC.fill = GridBagConstraints.HORIZONTAL;
		gbc_regC.anchor = GridBagConstraints.NORTH;
		gbc_regC.insets = new Insets(0, 0, 0, 5);
		gbc_regC.gridx = 3;
		gbc_regC.gridy = 0;
		panel.add(regC, gbc_regC);
		regC.setMinimumSize(new Dimension(38, 54));
		regC.setMaximumSize(new Dimension(38, 54));
		GridBagLayout gbl_regC = new GridBagLayout();
		gbl_regC.columnWidths = new int[] { 0 };
		gbl_regC.rowHeights = new int[] { 0 };
		gbl_regC.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_regC.rowWeights = new double[] { Double.MIN_VALUE };
		regC.setLayout(gbl_regC);
		regD = new V_Register8Bit("D");
		GridBagConstraints gbc_regD = new GridBagConstraints();
		gbc_regD.fill = GridBagConstraints.HORIZONTAL;
		gbc_regD.anchor = GridBagConstraints.NORTH;
		gbc_regD.insets = new Insets(0, 0, 0, 5);
		gbc_regD.gridx = 4;
		gbc_regD.gridy = 0;
		panel.add(regD, gbc_regD);
		regD.setMinimumSize(new Dimension(38, 54));
		regD.setMaximumSize(new Dimension(38, 54));
		GridBagLayout gbl_regD = new GridBagLayout();
		gbl_regD.columnWidths = new int[] { 0 };
		gbl_regD.rowHeights = new int[] { 0 };
		gbl_regD.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_regD.rowWeights = new double[] { Double.MIN_VALUE };
		regD.setLayout(gbl_regD);
		regE = new V_Register8Bit("E");
		GridBagConstraints gbc_regE = new GridBagConstraints();
		gbc_regE.fill = GridBagConstraints.HORIZONTAL;
		gbc_regE.anchor = GridBagConstraints.NORTH;
		gbc_regE.insets = new Insets(0, 0, 0, 5);
		gbc_regE.gridx = 5;
		gbc_regE.gridy = 0;
		panel.add(regE, gbc_regE);
		regE.setMinimumSize(new Dimension(38, 54));
		regE.setMaximumSize(new Dimension(38, 54));
		GridBagLayout gbl_regE = new GridBagLayout();
		gbl_regE.columnWidths = new int[] { 0 };
		gbl_regE.rowHeights = new int[] { 0 };
		gbl_regE.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_regE.rowWeights = new double[] { Double.MIN_VALUE };
		regE.setLayout(gbl_regE);
		regH = new V_Register8Bit("H");
		GridBagConstraints gbc_regH = new GridBagConstraints();
		gbc_regH.fill = GridBagConstraints.HORIZONTAL;
		gbc_regH.anchor = GridBagConstraints.NORTH;
		gbc_regH.insets = new Insets(0, 0, 0, 5);
		gbc_regH.gridx = 6;
		gbc_regH.gridy = 0;
		panel.add(regH, gbc_regH);
		regH.setMinimumSize(new Dimension(38, 54));
		regH.setMaximumSize(new Dimension(38, 54));
		GridBagLayout gbl_regH = new GridBagLayout();
		gbl_regH.columnWidths = new int[] { 0 };
		gbl_regH.rowHeights = new int[] { 0 };
		gbl_regH.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_regH.rowWeights = new double[] { Double.MIN_VALUE };
		regH.setLayout(gbl_regH);
		regL = new V_Register8Bit("L");
		GridBagConstraints gbc_regL = new GridBagConstraints();
		gbc_regL.fill = GridBagConstraints.HORIZONTAL;
		gbc_regL.anchor = GridBagConstraints.NORTH;
		gbc_regL.insets = new Insets(0, 0, 0, 5);
		gbc_regL.gridx = 7;
		gbc_regL.gridy = 0;
		panel.add(regL, gbc_regL);
		regL.setMinimumSize(new Dimension(38, 54));
		regL.setMaximumSize(new Dimension(38, 54));
		regL.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "L", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagLayout gbl_regL = new GridBagLayout();
		gbl_regL.columnWidths = new int[] { 0 };
		gbl_regL.rowHeights = new int[] { 0 };
		gbl_regL.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_regL.rowWeights = new double[] { Double.MIN_VALUE };
		regL.setLayout(gbl_regL);
		regF = new V_Register8Bit("F");
		GridBagConstraints gbc_regF = new GridBagConstraints();
		gbc_regF.insets = new Insets(0, 0, 0, 5);
		gbc_regF.fill = GridBagConstraints.HORIZONTAL;
		gbc_regF.anchor = GridBagConstraints.NORTH;
		gbc_regF.gridx = 8;
		gbc_regF.gridy = 0;
		panel.add(regF, gbc_regF);
		regF.setMinimumSize(new Dimension(38, 54));
		regF.setMaximumSize(new Dimension(38, 54));
		GridBagLayout gbl_regF = new GridBagLayout();
		gbl_regF.columnWidths = new int[] { 0 };
		gbl_regF.rowHeights = new int[] { 0 };
		gbl_regF.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_regF.rowWeights = new double[] { Double.MIN_VALUE };
		regF.setLayout(gbl_regF);
	}// initialize

	private static final String MAIN = "Main";
	private static final String AUXILARY = "Auxilary";
	private static final String[] REGS_MAIN = new String[] { "A", "B", "C", "D", "E", "H", "L", "F" };
	private static final String[] REGS_AUX = new String[] { "A'", "B'", "C'", "D'", "E'", "H'", "L'", "F'" };
	private V_Register8Bit regA;
	private V_Register8Bit regB;
	private V_Register8Bit regC;
	private V_Register8Bit regD;
	private V_Register8Bit regE;
	private V_Register8Bit regH;
	private V_Register8Bit regL;
	private V_Register8Bit regF;
	private JToggleButton tbMainAux;

}// class V_PrimaryRegisters
