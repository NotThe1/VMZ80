package ioSystem.listDevice;

/*
 * 2019-09-07 Fixed MyPrefs problem...in Generic Printer also
 */
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import fontChooser.FontChooser;

public class ListDevicePropertyDialog extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;

	private final JPanel panelColumns = new JPanel();
	Component c;

	private JRadioButton rbLimitColumns;
	private JSpinner spinnerTab;
	private JSpinner spinnerColumns;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	private JLabel lblColumns;
	private JPanel panelFont;
	private JLabel lblFontFamily;
	private JLabel lblFontStyle;
	private JLabel lblFontSize;

	private int dialogResultValue;

	public int showDialog() {
		dialogResultValue = JOptionPane.CANCEL_OPTION;
		this.setLocationRelativeTo(this.getOwner());

		this.setVisible(true);
		this.dispose();
		return dialogResultValue;
	}// showDialog
	
	public Preferences getMyPrefs() {
		return Preferences.userNodeForPackage(GenericPrinter.class).node("GenericPrinter");
	}//getMyPrefs


	private void saveProperties() {
		Preferences myPrefs =getMyPrefs();

		myPrefs.putInt("tabSize", (int) spinnerTab.getValue());

		myPrefs.putInt("maxColumns", (int) spinnerColumns.getValue());
		myPrefs.putBoolean("limitColumns", rbLimitColumns.isSelected());

		myPrefs.put("fontFamily", lblFontFamily.getText());
		myPrefs.put("fontStyle", lblFontStyle.getText());
		myPrefs.put("fontSize", lblFontSize.getText());

		try {
			myPrefs.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		} // try

		myPrefs = null;
	}// saveProperties

	private void readProperties() {
		Preferences myPrefs =getMyPrefs();

		spinnerTab.setValue(myPrefs.getInt("tabSize", 1)); // default for CP/M

		spinnerColumns.setValue(myPrefs.getInt("maxColumns", 80));
		rbLimitColumns.setSelected(myPrefs.getBoolean("limitColumns", false));
		doRbLimitColumns();

		lblFontFamily.setText(myPrefs.get("fontFamily", "Courier New"));
		lblFontStyle.setText(myPrefs.get("fontStyle", "Plain"));
		lblFontSize.setText(myPrefs.get("fontSize", "13"));

		myPrefs = null;
	}// readProperties

	private void doBtnOK() {
		saveProperties();
		dialogResultValue = JOptionPane.OK_OPTION;
		dispose();
	}// doBtnOK

	private void doBtnCancel() {
		dialogResultValue = JOptionPane.CANCEL_OPTION;
		dispose();
	}// doBtnCancel

	private void doBtnNewFont() {

		FontChooser fontChooser = new FontChooser(this, lblFontFamily.getText(), lblFontStyle.getText(),
				Integer.valueOf(lblFontSize.getText()));

		if (fontChooser.showDialog() == JOptionPane.OK_OPTION) {
			Font newFont = fontChooser.selectedFont();

			String currentStyle;
			switch (newFont.getStyle()) {
			case Font.PLAIN:
				currentStyle = "Plain";
				break;
			case Font.BOLD:
				currentStyle = "Bold";
				break;
			case Font.ITALIC:
				currentStyle = "Italic";
				break;
			case Font.BOLD | Font.ITALIC:
				currentStyle = "Bold Italic";
				break;
			default:
				currentStyle = "Plain";
				break;
			}// switch

			lblFontFamily.setText(newFont.getFamily());
			lblFontStyle.setText(currentStyle);

			lblFontSize.setText(Integer.toString(newFont.getSize()));
		} // if OK
		fontChooser = null;
	}// doBtnNewFont

	private void doRbLimitColumns() {
		spinnerColumns.setEnabled(rbLimitColumns.isSelected());
		lblColumns.setEnabled(rbLimitColumns.isSelected());
	}// doRbColumnLimits

	private void appInit() {
		readProperties();
	}// appInit

	// private void appClose() {
	//
	// }// applClose
	//
	// private void appCancel() {
	//
	// }// appCancel

	/**
	 * Create the dialog.
	 */
	public ListDevicePropertyDialog(Component c) {
		super(SwingUtilities.getWindowAncestor(c), "List Device Propert Dialog", Dialog.DEFAULT_MODALITY_TYPE);
		this.c = c; // save if calling FontChooser ??
		initialize();
		appInit();
	}// Constructor

	private void initialize() {
		// setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
		setBounds(100, 100, 420, 253);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 50, 300, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 100, 30, 0, 33, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		getContentPane().setLayout(gridBagLayout);

		panelFont = new JPanel();
		panelFont.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Font", TitledBorder.CENTER,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panelFont = new GridBagConstraints();
		gbc_panelFont.insets = new Insets(0, 0, 5, 5);
		gbc_panelFont.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelFont.gridx = 0;
		gbc_panelFont.gridy = 0;
		getContentPane().add(panelFont, gbc_panelFont);
		GridBagLayout gbl_panelFont = new GridBagLayout();
		gbl_panelFont.columnWidths = new int[] { 0, 0 };
		gbl_panelFont.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panelFont.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelFont.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panelFont.setLayout(gbl_panelFont);

		lblFontFamily = new JLabel("Font Family");
		GridBagConstraints gbc_lblFontFamily = new GridBagConstraints();
		gbc_lblFontFamily.insets = new Insets(0, 0, 5, 0);
		gbc_lblFontFamily.gridx = 0;
		gbc_lblFontFamily.gridy = 0;
		panelFont.add(lblFontFamily, gbc_lblFontFamily);

		lblFontStyle = new JLabel("Font Style");
		GridBagConstraints gbc_lblFontStyle = new GridBagConstraints();
		gbc_lblFontStyle.insets = new Insets(0, 0, 5, 0);
		gbc_lblFontStyle.gridx = 0;
		gbc_lblFontStyle.gridy = 1;
		panelFont.add(lblFontStyle, gbc_lblFontStyle);

		lblFontSize = new JLabel("Size");
		GridBagConstraints gbc_lblFontSize = new GridBagConstraints();
		gbc_lblFontSize.insets = new Insets(0, 0, 5, 0);
		gbc_lblFontSize.gridx = 0;
		gbc_lblFontSize.gridy = 2;
		panelFont.add(lblFontSize, gbc_lblFontSize);

		JButton btnNewFont = new JButton("New Font");
		btnNewFont.setName(BTN_NEW_FONT);
		btnNewFont.addActionListener(this);
		GridBagConstraints gbc_btnNewFont = new GridBagConstraints();
		gbc_btnNewFont.gridx = 0;
		gbc_btnNewFont.gridy = 4;
		panelFont.add(btnNewFont, gbc_btnNewFont);
		panelColumns
				.setBorder(new TitledBorder(null, "Column Limits", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panelColumns = new GridBagConstraints();
		gbc_panelColumns.insets = new Insets(0, 0, 5, 5);
		gbc_panelColumns.gridx = 1;
		gbc_panelColumns.gridy = 0;
		getContentPane().add(panelColumns, gbc_panelColumns);
		GridBagLayout gbl_panelColumns = new GridBagLayout();
		gbl_panelColumns.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelColumns.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelColumns.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelColumns.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelColumns.setLayout(gbl_panelColumns);

		rbLimitColumns = new JRadioButton("");
		rbLimitColumns.setName(RB_COLUMN_LIMITS);
		rbLimitColumns.addActionListener(this);
		rbLimitColumns.setToolTipText("Will truncate lines at max column");
		GridBagConstraints gbc_rbLimitColumns = new GridBagConstraints();
		gbc_rbLimitColumns.anchor = GridBagConstraints.EAST;
		gbc_rbLimitColumns.insets = new Insets(0, 0, 5, 5);
		gbc_rbLimitColumns.gridx = 0;
		gbc_rbLimitColumns.gridy = 0;
		panelColumns.add(rbLimitColumns, gbc_rbLimitColumns);

		lblNewLabel = new JLabel("Limit Width");
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 0;
		panelColumns.add(lblNewLabel, gbc_lblNewLabel);

		spinnerColumns = new JSpinner();
		spinnerColumns
				.setModel(new SpinnerNumberModel(Integer.valueOf(120), Integer.valueOf(10), null, Integer.valueOf(1)));
		// spinnerColumns.setModel(new SpinnerNumberModel(new Integer(120), new Integer(10), null, new Integer(1)));
		GridBagConstraints gbc_spinnerColumns = new GridBagConstraints();
		gbc_spinnerColumns.anchor = GridBagConstraints.EAST;
		gbc_spinnerColumns.insets = new Insets(0, 0, 0, 5);
		gbc_spinnerColumns.gridx = 0;
		gbc_spinnerColumns.gridy = 1;
		panelColumns.add(spinnerColumns, gbc_spinnerColumns);

		lblColumns = new JLabel("Columns");
		lblColumns.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblColumns = new GridBagConstraints();
		gbc_lblColumns.anchor = GridBagConstraints.WEST;
		gbc_lblColumns.gridx = 1;
		gbc_lblColumns.gridy = 1;
		panelColumns.add(lblColumns, gbc_lblColumns);

		JPanel panelTabs = new JPanel();
		panelTabs.setBorder(new TitledBorder(null, "Tab Spacing", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panelTabs = new GridBagConstraints();
		gbc_panelTabs.insets = new Insets(0, 0, 5, 0);
		gbc_panelTabs.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelTabs.gridx = 2;
		gbc_panelTabs.gridy = 0;
		getContentPane().add(panelTabs, gbc_panelTabs);
		GridBagLayout gbl_panelTabs = new GridBagLayout();
		gbl_panelTabs.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelTabs.rowHeights = new int[] { 0, 0 };
		gbl_panelTabs.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelTabs.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelTabs.setLayout(gbl_panelTabs);

		spinnerTab = new JSpinner();
		GridBagConstraints gbc_spinnerTab = new GridBagConstraints();
		gbc_spinnerTab.anchor = GridBagConstraints.SOUTH;
		gbc_spinnerTab.insets = new Insets(0, 0, 0, 5);
		gbc_spinnerTab.gridx = 0;
		gbc_spinnerTab.gridy = 0;
		panelTabs.add(spinnerTab, gbc_spinnerTab);
		spinnerTab.setModel(new SpinnerNumberModel(1, 1, 40, 1));

		lblNewLabel_1 = new JLabel("Tab Size");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.fill = GridBagConstraints.VERTICAL;
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 0;
		panelTabs.add(lblNewLabel_1, gbc_lblNewLabel_1);
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.LEFT);

		JPanel panelButtons = new JPanel();
		panelButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		GridBagConstraints gbc_panelButtons = new GridBagConstraints();
		gbc_panelButtons.anchor = GridBagConstraints.NORTH;
		gbc_panelButtons.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelButtons.gridx = 2;
		gbc_panelButtons.gridy = 3;
		getContentPane().add(panelButtons, gbc_panelButtons);

		JButton btnOk = new JButton("OK");
		btnOk.setName(BTN_OK);
		btnOk.addActionListener(this);
		btnOk.setActionCommand("OK");
		panelButtons.add(btnOk);
		getRootPane().setDefaultButton(btnOk);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.setName(BTN_CANCEL);
		btnCancel.addActionListener(this);
		btnCancel.setActionCommand("Cancel");
		panelButtons.add(btnCancel);

	}// initialize

	private static final String BTN_OK = "btnOk";
	private static final String BTN_CANCEL = "btnCancel";
	private static final String BTN_NEW_FONT = "btnNewFont";
	private static final String RB_COLUMN_LIMITS = "rbColumnLimits";

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		switch (((Component) actionEvent.getSource()).getName()) {
		case BTN_OK:
			doBtnOK();
			break;
		case BTN_CANCEL:
			doBtnCancel();
			break;
		case BTN_NEW_FONT:
			doBtnNewFont();
			break;
		case RB_COLUMN_LIMITS:
			doRbLimitColumns();
			break;
		default:
			System.err.printf("[ListDevicePropertyDialog] - actionPerformed %n Unknown Action %s%n%n",
					((Component) actionEvent.getSource()).getName());
		}// switch

	}// actionPerformed

}// class ListDevicePropertyDialog
