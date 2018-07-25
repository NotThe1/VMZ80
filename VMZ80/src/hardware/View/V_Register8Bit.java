package hardware.View;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import utilities.hdNumberBox.HDNumberBox;

public class V_Register8Bit extends JPanel {

	private static final long serialVersionUID = 1L;
	private String title = "?";
	private HDNumberBox hdn8Bit;
	
	private void appInit() {

	}// appInit

	public V_Register8Bit() {
		setMaximumSize(new Dimension(0, 0));
		initialize();
		appInit();
	}// Constructor

	public V_Register8Bit(String title) {
		this();
		setTitle(title);
	}// Constructor

	public void setTitle(String title) {
		this.title = title;
		setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), title, TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
	}// setTitle
	
	public String getTitle() {
		return title;
	}//getTitle
	
	public int getValue() {
		return hdn8Bit.getValue();
	}//getValue

	public void setValue(byte value) {
		hdn8Bit.setValueQuiet(value & 0XFF);
	}//getValue

	private void initialize() {
//		 setLayout(new GridBagLayout());
		setPreferredSize(new Dimension(25, 40));
		setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), title, TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		hdn8Bit = new HDNumberBox(0,255,0,false);
		hdn8Bit.setAlignmentX(0.0f);
		hdn8Bit.setHexDisplay("%02X");
		hdn8Bit.setPreferredSize(new Dimension(20, 30));
		hdn8Bit.setMinimumSize(new Dimension(20, 30));
		hdn8Bit.setBorder(null);
		GridBagConstraints gbc_hdn16Bit = new GridBagConstraints();
		gbc_hdn16Bit.anchor = GridBagConstraints.NORTHWEST;
		gbc_hdn16Bit.gridx = 1;
		gbc_hdn16Bit.gridy = 0;
		setLayout(new GridLayout(0, 1, 0, 0));
		add(hdn8Bit);
		GridBagLayout gbl_hdn8Bit = new GridBagLayout();
		gbl_hdn8Bit.columnWidths = new int[]{0};
		gbl_hdn8Bit.rowHeights = new int[]{0};
		gbl_hdn8Bit.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_hdn8Bit.rowWeights = new double[]{Double.MIN_VALUE};
		hdn8Bit.setLayout(gbl_hdn8Bit);
	}// initialize

}// class ViewPrimaryRegisters
