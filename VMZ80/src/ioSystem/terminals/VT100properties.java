package ioSystem.terminals;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;

import codeSupport.AppLogger;

public class VT100properties extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private int dialogResultValue;
	private Preferences myPrefs;
	private AppLogger log = AppLogger.getInstance();
	private AdapterVT100Properties adapterDialog = new AdapterVT100Properties();
	/* properties */
	private Font originalFont;
	private Font dialogFont;
	private JTextComponent component;
	private int screenColumns;
	private int foregroundColor;
	private int backgroundColor;
	private int caretColor;


	private void doBtnOK() {
		myPrefs.put("FontFamily", dialogFont.getFamily());
		myPrefs.putInt("FontStyle", dialogFont.getStyle());
		myPrefs.putInt("FontSize", dialogFont.getSize());
				
		dialogResultValue = JOptionPane.OK_OPTION;
		dispose();
	}// doBtnOK

	private void doBtnCancel() {
		dialogResultValue = JOptionPane.CANCEL_OPTION;
		dispose();
	}// doBtnCancel
	
	private void doRestoreFont() {
		listSize.setSelectedValue(originalFont.getSize(), true);
		listFamily.setSelectedValue(originalFont.getFamily(), true);
		listStyle.setSelectedIndex(originalFont.getStyle());
		doFontSelection() ;
	}//doRestoreFont

	public int showDialog() {
		dialogResultValue = JOptionPane.NO_OPTION;
		this.setLocationRelativeTo(getOwner());

		this.setVisible(true);
		this.dispose();
		return dialogResultValue;
	}// showDialog

	private void getCurrentProperties() {
		dialogFont = new Font(myPrefs.get("FontFamily", "Courier"), myPrefs.getInt("FontStyle", Font.PLAIN),
				myPrefs.getInt("FontSize", 18));
		originalFont = new Font(myPrefs.get("FontFamily", "Courier"), myPrefs.getInt("FontStyle", Font.PLAIN),
				myPrefs.getInt("FontSize", 18));
		screenColumns = myPrefs.getInt("Columns", 80);
		foregroundColor = myPrefs.getInt("ForegroundColor", 80);
		backgroundColor = myPrefs.getInt("backgroundColor", 80);
		caretColor = myPrefs.getInt("caretColor", 80);
	}// getCurrentProperties

	private void initFontTab() {
		DefaultListModel<Integer> sizeModel = new DefaultListModel<Integer>();
		for (Integer i = 5; i < 100; i++) {
			sizeModel.addElement(i);
		} //
		listSize.setModel(sizeModel);
		listSize.setSelectedValue(dialogFont.getSize(), true);

		String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
		DefaultListModel<String> familyModel = new DefaultListModel<String>();
		for (String f : fontNames) {
			familyModel.addElement(f);
		} // for
		listFamily.setModel(familyModel);
		listFamily.setSelectedValue(dialogFont.getFamily(), true);

		String[] styles = new String[] { STYLE_PLAIN, STYLE_BOLD, STYLE_ITALIC, "Bold Italic" };
		DefaultListModel<String> styleModel = new DefaultListModel<String>();
		for (String s : styles) {
			styleModel.addElement(s);
		} // for
		listStyle.setModel(styleModel);
		listStyle.setSelectedValue(styles[dialogFont.getStyle()], true);
//		listStyle.setSelectedValue(dialogFont.getStyle(), true);

		listSize.addListSelectionListener(adapterDialog);
		listFamily.addListSelectionListener(adapterDialog);
		listStyle.addListSelectionListener(adapterDialog);

		doFontSelection();

	}// initFontTab

	private void doFontSelection() {
		textFamily.setText((String) listFamily.getSelectedValue());
		textStyle.setText((String) listStyle.getSelectedValue());
		textSize.setText(Integer.toString(listSize.getSelectedValue()));
		int style = getStyleFromText(textStyle.getText());

		dialogFont = new Font(textFamily.getText(), style, Integer.parseInt(textSize.getText()));
		lblSelectedFont.setFont(dialogFont);

		String display = String.format("%s ,%s, %s", textFamily.getText(), textStyle.getText(), textSize.getText());
		lblSelectedFont.setText(display);

	}// doSelection

	public static int getStyleFromText(String textStyle) {
		int styleFromTextDisplay = Font.PLAIN;
		switch (textStyle) {
		case STYLE_PLAIN:
			styleFromTextDisplay = Font.PLAIN;
			break;
		case STYLE_BOLD:
			styleFromTextDisplay = Font.BOLD;
			break;
		case STYLE_ITALIC:
			styleFromTextDisplay = Font.ITALIC;
			break;
		case STYLE_BOLD_ITALIC:
			styleFromTextDisplay = Font.BOLD | Font.ITALIC;
			break;
		}// switch
		return styleFromTextDisplay;
	}// getStyleFromText
	
	
	private void doSetTextColor() {
//		Color color = JColorChooser.showDialog(frameTTY, "Font Color", textScreen.getForeground());
//		if (color != null) {
//			textScreen.setForeground(color);
//			textScreen.getCaret().setVisible(true);
//		} // if
	}// doSetTextColor

	private void doSetBackgroundColor() {
//		Color color = JColorChooser.showDialog(frameTTY, "Font Color", textScreen.getBackground());
//		if (color != null) {
//			textScreen.setBackground(color);
//			textScreen.getCaret().setVisible(true);
//		} // if
	}// doSetBackgroundColor

	private void doSetCaretColor() {
//		Color color = JColorChooser.showDialog(frameTTY, "Font Color", textScreen.getCaretColor());
//		if (color != null) {
//			textScreen.setCaretColor(color);
//			textScreen.getCaret().setVisible(true);
//		} // if
	}// doSetCaretColor


	public void close() {
		appClose();
	}// close

	private void appClose() {
		

	}// appClose

	private void appInit() {
		getCurrentProperties();
		initFontTab();
		// loadFontChooser();
	}// appInit

	public VT100properties(Frame f, Preferences myPrefs) {
//		public VT100properties(Frame f, Preferences myPrefs,JTextComponent component) {
		super(f, "Properties Dialog", true);
		setResizable(false);
		this.myPrefs = myPrefs;
//		this.component=component;
		initialize();
		appInit();
	}// Constructor

	private void initialize() {
		setBounds(100, 100, 492, 560);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 0, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		contentPanel.add(tabbedPane, gbc_tabbedPane);

		tabFont = new JPanel();
		tabbedPane.addTab("Font", null, tabFont, null);
		tabFont.setLayout(null);

		JPanel contentPanelFont = new JPanel();
		contentPanelFont.setBounds(0, 0, 478, 428);
		tabFont.add(contentPanelFont);
		contentPanelFont.setLayout(null);

		JPanel Selected = new JPanel();
		Selected.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Selected Font",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		Selected.setBounds(10, 11, 458, 77);
		contentPanelFont.add(Selected);
		Selected.setLayout(new BorderLayout(0, 0));

		lblSelectedFont = new JLabel("Selected Font");
		lblSelectedFont.setPreferredSize(new Dimension(90, 26));
		lblSelectedFont.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblSelectedFont.setHorizontalAlignment(SwingConstants.CENTER);
		Selected.add(lblSelectedFont, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setBackground(Color.WHITE);
		panel.setBounds(10, 108, 458, 290);
		contentPanelFont.add(panel);

		JLabel label_1 = new JLabel("Family:");
		label_1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		label_1.setBounds(10, 11, 46, 14);
		panel.add(label_1);

		JLabel label_2 = new JLabel("Style:");
		label_2.setFont(new Font("Tahoma", Font.PLAIN, 12));
		label_2.setBounds(244, 11, 46, 14);
		panel.add(label_2);

		JLabel label_3 = new JLabel("Size:");
		label_3.setFont(new Font("Tahoma", Font.PLAIN, 12));
		label_3.setBounds(373, 11, 46, 14);
		panel.add(label_3);

		textFamily = new JTextField();
		textFamily.setEditable(false);
		textFamily.setFont(new Font("Tahoma", Font.PLAIN, 12));
		textFamily.setBackground(UIManager.getColor("Panel.background"));
		textFamily.setBounds(10, 34, 221, 20);
		textFamily.setColumns(10);
		panel.add(textFamily);

		textStyle = new JTextField();
		textStyle.setFont(new Font("Tahoma", Font.PLAIN, 12));
		textStyle.setEditable(false);
		textStyle.setColumns(10);
		textStyle.setBackground(SystemColor.menu);
		textStyle.setBounds(244, 34, 106, 20);
		panel.add(textStyle);

		textSize = new JTextField();
		textSize.setFont(new Font("Tahoma", Font.PLAIN, 12));
		textSize.setEditable(false);
		textSize.setColumns(10);
		textSize.setBackground(SystemColor.menu);
		textSize.setBounds(373, 34, 75, 20);
		panel.add(textSize);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 65, 221, 214);
		panel.add(scrollPane);

		listFamily = new JList<String>();
		scrollPane.setViewportView(listFamily);
		listFamily.setBorder(new LineBorder(new Color(0, 0, 0)));
		listFamily.setFont(new Font("Tahoma", Font.PLAIN, 11));
		listFamily.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(254, 65, 96, 214);
		panel.add(scrollPane_1);

		listStyle = new JList<String>();
		listStyle.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listStyle.setFont(new Font("Tahoma", Font.PLAIN, 12));
		listStyle.setBorder(new LineBorder(new Color(0, 0, 0)));
		scrollPane_1.setViewportView(listStyle);

		JScrollPane scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(373, 65, 75, 214);
		panel.add(scrollPane_2);

		listSize = new JList<Integer>();
		listSize.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listSize.setFont(new Font("Tahoma", Font.PLAIN, 11));
		listSize.setBorder(new LineBorder(new Color(0, 0, 0)));
		scrollPane_2.setViewportView(listSize);

		JPanel buttonPaneFont = new JPanel();
		buttonPaneFont.setBounds(0, 428, 478, 33);
		tabFont.add(buttonPaneFont);
		GridBagLayout gbl_buttonPaneFont = new GridBagLayout();
		gbl_buttonPaneFont.columnWidths = new int[] { 146, 90, 90, 0 };
		gbl_buttonPaneFont.rowHeights = new int[] { 26, 0 };
		gbl_buttonPaneFont.columnWeights = new double[] { 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_buttonPaneFont.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		buttonPaneFont.setLayout(gbl_buttonPaneFont);

		JButton buttonRestoreFont = new JButton("Restore");
		buttonRestoreFont.addActionListener(adapterDialog);
		buttonRestoreFont.setActionCommand(BTN_RESTORE_FONT);
		buttonRestoreFont.setPreferredSize(new Dimension(90, 26));
		buttonRestoreFont.setName("btnRestoreFont");
		buttonRestoreFont.setMinimumSize(new Dimension(80, 23));
		buttonRestoreFont.setMaximumSize(new Dimension(80, 23));
		buttonRestoreFont.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_buttonRestoreFont = new GridBagConstraints();
		gbc_buttonRestoreFont.anchor = GridBagConstraints.NORTHEAST;
		gbc_buttonRestoreFont.gridx = 2;
		gbc_buttonRestoreFont.gridy = 0;
		buttonPaneFont.add(buttonRestoreFont, gbc_buttonRestoreFont);

		JPanel tabColors = new JPanel();
		tabbedPane.addTab("Colors", null, tabColors, null);
		GridBagLayout gbl_tabColors = new GridBagLayout();
		gbl_tabColors.columnWidths = new int[] { 0 };
		gbl_tabColors.rowHeights = new int[] { 0 };
		gbl_tabColors.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_tabColors.rowWeights = new double[] { Double.MIN_VALUE };
		tabColors.setLayout(gbl_tabColors);

		JPanel tabColumns = new JPanel();
		tabbedPane.addTab("Columns", null, tabColumns, null);
		GridBagLayout gbl_tabColumns = new GridBagLayout();
		gbl_tabColumns.columnWidths = new int[] { 0 };
		gbl_tabColumns.rowHeights = new int[] { 0 };
		gbl_tabColumns.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_tabColumns.rowWeights = new double[] { Double.MIN_VALUE };
		tabColumns.setLayout(gbl_tabColumns);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton okButton = new JButton("OK");
		okButton.setActionCommand(BTN_OK);
		okButton.addActionListener(adapterDialog);
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(adapterDialog);
		cancelButton.setActionCommand(BTN_CANCEL);
		buttonPane.add(cancelButton);

	}// initialize

	private static final String BTN_OK = "btnOk";
	private static final String BTN_CANCEL = "btnCancel";
	
	private static final String BTN_RESTORE_FONT = "btnRestoreFont";
	
	private final static String STYLE_PLAIN = "Plain";
	private final static String STYLE_BOLD = "Bold";
	private final static String STYLE_ITALIC = "Italic";
	private final static String STYLE_BOLD_ITALIC = "Bold Italic";
	
	private JTextField textFamily;
	private JTextField textStyle;
	private JTextField textSize;
	private JLabel lblSelectedFont;
	private JList<String> listFamily;
	private JList<String> listStyle;
	private JList<Integer> listSize;
	
	private JPanel tabFont;

	///////////////////////////////////////////////////////////////////////////

	class AdapterVT100Properties implements ListSelectionListener, ActionListener {
		/* ActionListener */

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String cmd = actionEvent.getActionCommand();
			switch (cmd) {
			case BTN_OK:
				doBtnOK();
				break;
			case BTN_CANCEL:
				doBtnCancel();
				break;
			case BTN_RESTORE_FONT:
				doRestoreFont();
				break;
			default:
				log.errorf("bad actionEvent cmd : [%s]%n", cmd);
			}// switch
		}// actionPerformed

		/* ListSelectionListener */

		@Override
		public void valueChanged(ListSelectionEvent listSelectionEvent) {
			doFontSelection();
		}// valueChanged

	}// class AdapterVT100Properties
}// class VT100properties
