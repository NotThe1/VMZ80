package hardware.View;

import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import utilities.hdNumberBox.HDNumberBox;

public class V_ProgramRegisters extends JPanel implements Runnable{
	private static final long serialVersionUID = 1L;

	public V_ProgramRegisters() {

		initialize() ;
		appInit();
	}//Constructor
	
	private void appInit() {
		
	}//appInit
	
	private void initialize() {
		setBorder(new TitledBorder(null, "Program Registers", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		setLayout(null);
		
		JLabel lblPc = new JLabel("PC");
		lblPc.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblPc.setHorizontalAlignment(SwingConstants.TRAILING);
		lblPc.setToolTipText("Program Counter");
		lblPc.setBounds(10, 32, 46, 14);
		add(lblPc);
		
		HDNumberBox regPC = new HDNumberBox(0,0xFFFF,00,false);
		regPC.setToolTipText("Program Counter");
		regPC.setName("regPC");
//		regPC.addHDNumberValueChangedListener(adapterVPR);
		regPC.setFont(new Font("Courier New", Font.BOLD, 15));
		regPC.setHexDisplay("%04X");
		regPC.setBounds(66, 22, 40, 35);
		add(regPC);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0};
		gbl_panel.rowHeights = new int[]{0};
		gbl_panel.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{Double.MIN_VALUE};
		regPC.setLayout(gbl_panel);
		
		JLabel lblSp = new JLabel("SP");
		lblSp.setFont(new Font("Tahoma", Font.BOLD, 15));
		lblSp.setToolTipText("StackPointer");
		lblSp.setHorizontalAlignment(SwingConstants.TRAILING);
		lblSp.setBounds(116, 32, 46, 14);
		add(lblSp);
		
		HDNumberBox regSP = new HDNumberBox(0, 65535, 0, false);
		regSP.setToolTipText("StackPointer");
		regSP.setName("regPC");
		regSP.setHexDisplay("%04X");
		regSP.setFont(new Font("Courier New", Font.BOLD, 15));
		regSP.setBounds(172, 22, 40, 35);
		add(regSP);
		GridBagLayout gbl_regSP = new GridBagLayout();
		gbl_regSP.columnWidths = new int[]{0};
		gbl_regSP.rowHeights = new int[]{0};
		gbl_regSP.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_regSP.rowWeights = new double[]{Double.MIN_VALUE};
		regSP.setLayout(gbl_regSP);
	}//initialize

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}//run
}//class V_ProgramRegisters
