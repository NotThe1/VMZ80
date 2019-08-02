package ioSystem.terminals;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import codeSupport.AppLogger;

public class VT100properties extends JDialog {

	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private int dialogResultValue;
	private Preferences myPrefs;
	private AppLogger log = AppLogger.getInstance();
	private AdapterVT100Properties adapterDialog = new AdapterVT100Properties();
	/* properties */
	private Font originalFont;
	private Font dialogFont;
	
	private int screenColumns;
	private boolean screenTruncate;
	private boolean screenWrap;
	
	private int foregroundColor;
	private int dialogForegroundColor;
	private int backgroundColor;
	private int dialogBackgroundColor;
	private int caretColor;
	private int dialogCaretColor;


	private void doBtnOK() {
		myPrefs.put("FontFamily", dialogFont.getFamily());
		myPrefs.putInt("FontStyle", dialogFont.getStyle());
		myPrefs.putInt("FontSize", dialogFont.getSize());
		
		myPrefs.putInt("ColorFont", textExample.getForeground().getRGB());
		myPrefs.putInt("ColorBackground", textExample.getBackground().getRGB());
		myPrefs.putInt("ColorCaret", textExample.getCaretColor().getRGB());
		
		myPrefs.putInt("Columns", rb80.isSelected()?80:132);

		myPrefs.putBoolean("ScreenTruncate", rbTruncate.isSelected());
		myPrefs.putBoolean("ScreenWrap", rbWrap.isSelected());

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
	
	private void doRestoreColumns() {
		if (screenColumns==132) {
			rb132.setSelected(true);
			rb80.setSelected(false);
		}else {
			rb132.setSelected(false);
			rb80.setSelected(true);
		}// if
		
		rbWrap.setSelected(screenWrap);
		rbTruncate.setSelected(screenTruncate);
	}//restoreColumns

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
		
		foregroundColor = myPrefs.getInt("ColorFont", -13421773);
		backgroundColor = myPrefs.getInt("ColorBackground", -4144960);
		caretColor = myPrefs.getInt("ColorCaret", -65536);
		
		dialogForegroundColor = foregroundColor;
		dialogBackgroundColor =backgroundColor;
		dialogCaretColor =caretColor;
		
		screenColumns = myPrefs.getInt("Columns", 80);
		screenTruncate = myPrefs.getBoolean("ScreenTruncate", true);
		screenWrap = myPrefs.getBoolean("ScreenWrap", false);
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
		textExample.setFont(dialogFont);
		textExample.getCaret().setVisible(true);
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
	
	private void initColorTab() {
		textExample.setForeground(new Color(dialogForegroundColor));
		textExample.setBackground(new Color(dialogBackgroundColor));
		textExample.setCaretColor(new Color(dialogCaretColor));

		textExample.setFont(dialogFont);
	}//initColorTab
	
	private void doRestoreColors() {
		dialogForegroundColor = foregroundColor;
		dialogBackgroundColor =backgroundColor;
		dialogCaretColor =caretColor;
		initColorTab();
	}//doRestoreColors
	
	private void doColorText() {
		Color color = JColorChooser.showDialog(panelColor, "Font Color", textExample.getForeground());
		if (color != null) {
			textExample.setForeground(color);
			textExample.getCaret().setVisible(true);
		} // if
	}// doColorText

	private void doColorBackground() {
		Color color = JColorChooser.showDialog(panelColor, "Background Color", textExample.getBackground());
		if (color != null) {
			textExample.setBackground(color);
			textExample.getCaret().setVisible(true);
		} // if
	}// doColorBackground

	private void doColorCaret() {
		Color color = JColorChooser.showDialog(panelColor, "Font Color", textExample.getCaretColor());
		if (color != null) {
			textExample.setCaretColor(color);
			textExample.getCaret().setVisible(true);
		} // if
	}// doColorCaret

	public void close() {
		appClose();
	}// close

	private void appClose() {
		
	}// appClose

	private void appInit() {
		getCurrentProperties();
		initFontTab();
		initColorTab();
		doRestoreColumns();
	}// appInit

	public VT100properties(Frame f, Preferences myPrefs) {
		super(f, "Properties Dialog", true);
		setResizable(false);
		this.myPrefs = myPrefs;
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
		gbl_tabColors.columnWidths = new int[] { 0, 0 };
		gbl_tabColors.rowHeights = new int[] { 150, 0, 0, 0 };
		gbl_tabColors.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_tabColors.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		tabColors.setLayout(gbl_tabColors);
		
		JPanel panelExampleTextColors = new JPanel();
		panelExampleTextColors.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Preview", TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panelExampleTextColors = new GridBagConstraints();
		gbc_panelExampleTextColors.insets = new Insets(0, 0, 5, 0);
		gbc_panelExampleTextColors.fill = GridBagConstraints.BOTH;
		gbc_panelExampleTextColors.gridx = 0;
		gbc_panelExampleTextColors.gridy = 0;
		tabColors.add(panelExampleTextColors, gbc_panelExampleTextColors);
		GridBagLayout gbl_panelExampleTextColors = new GridBagLayout();
		gbl_panelExampleTextColors.columnWidths = new int[]{0, 0};
		gbl_panelExampleTextColors.rowHeights = new int[]{0, 0};
		gbl_panelExampleTextColors.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panelExampleTextColors.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panelExampleTextColors.setLayout(gbl_panelExampleTextColors);
		
		JScrollPane scrollPane_3 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_3 = new GridBagConstraints();
		gbc_scrollPane_3.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_3.gridx = 0;
		gbc_scrollPane_3.gridy = 0;
		panelExampleTextColors.add(scrollPane_3, gbc_scrollPane_3);
		
		textExample = new JTextPane();
		scrollPane_3.setViewportView(textExample);
		textExample.setText("01234567890123456789012345\r\nabcdefghijklmnopqrst\r\nABCDEFGHIJKLMNOPQRSSTUVWXYZ\r\n!\"#$%&'()*+,-./{|}~");
		textExample.setEditable(false);
		
		panelColor = new JPanel();
		GridBagConstraints gbc_panelColor = new GridBagConstraints();
		gbc_panelColor.insets = new Insets(0, 0, 5, 0);
		gbc_panelColor.fill = GridBagConstraints.BOTH;
		gbc_panelColor.gridx = 0;
		gbc_panelColor.gridy = 1;
		tabColors.add(panelColor, gbc_panelColor);
		GridBagLayout gbl_panelColor = new GridBagLayout();
		gbl_panelColor.columnWidths = new int[]{0, 0, 0};
		gbl_panelColor.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_panelColor.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panelColor.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panelColor.setLayout(gbl_panelColor);
		
		Component rigidArea = Box.createRigidArea(new Dimension(30, 30));
		GridBagConstraints gbc_rigidArea = new GridBagConstraints();
		gbc_rigidArea.insets = new Insets(0, 0, 5, 5);
		gbc_rigidArea.gridx = 0;
		gbc_rigidArea.gridy = 0;
		panelColor.add(rigidArea, gbc_rigidArea);
		
		JButton btnColorText = new JButton("Text...");
		btnColorText.setActionCommand(BTN_COLOR_TEXT);
		btnColorText.addActionListener(adapterDialog);
		GridBagConstraints gbc_btnColorText = new GridBagConstraints();
		gbc_btnColorText.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnColorText.insets = new Insets(0, 0, 5, 0);
		gbc_btnColorText.gridx = 1;
		gbc_btnColorText.gridy = 1;
		panelColor.add(btnColorText, gbc_btnColorText);
		
		Component rigidArea_1 = Box.createRigidArea(new Dimension(30, 30));
		GridBagConstraints gbc_rigidArea_1 = new GridBagConstraints();
		gbc_rigidArea_1.insets = new Insets(0, 0, 5, 5);
		gbc_rigidArea_1.gridx = 0;
		gbc_rigidArea_1.gridy = 2;
		panelColor.add(rigidArea_1, gbc_rigidArea_1);
		
		JButton btnColorBackground = new JButton("Background ...");
		btnColorBackground.setActionCommand(BTN_COLOR_BACKGROUND);
		btnColorBackground.addActionListener(adapterDialog);
		GridBagConstraints gbc_btnColorBackground = new GridBagConstraints();
		gbc_btnColorBackground.insets = new Insets(0, 0, 5, 0);
		gbc_btnColorBackground.gridx = 1;
		gbc_btnColorBackground.gridy = 3;
		panelColor.add(btnColorBackground, gbc_btnColorBackground);
		
		Component rigidArea_2 = Box.createRigidArea(new Dimension(30, 30));
		GridBagConstraints gbc_rigidArea_2 = new GridBagConstraints();
		gbc_rigidArea_2.insets = new Insets(0, 0, 5, 5);
		gbc_rigidArea_2.gridx = 0;
		gbc_rigidArea_2.gridy = 4;
		panelColor.add(rigidArea_2, gbc_rigidArea_2);
		
		JButton btnColorCaret = new JButton("Caret");
		btnColorCaret.setActionCommand(BTN_COLOR_CARET);
		btnColorCaret.addActionListener(adapterDialog);
		GridBagConstraints gbc_btnColorCaret = new GridBagConstraints();
		gbc_btnColorCaret.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnColorCaret.gridx = 1;
		gbc_btnColorCaret.gridy = 5;
		panelColor.add(btnColorCaret, gbc_btnColorCaret);
		
		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.anchor = GridBagConstraints.EAST;
		gbc_panel_1.fill = GridBagConstraints.VERTICAL;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 2;
		tabColors.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JButton btnRestoreColors = new JButton("Restore");
		btnRestoreColors.setActionCommand(BTN_RESTORE_COLORS);
		btnRestoreColors.addActionListener(adapterDialog);
		GridBagConstraints gbc_btnRestoreColors = new GridBagConstraints();
		gbc_btnRestoreColors.gridx = 0;
		gbc_btnRestoreColors.gridy = 0;
		panel_1.add(btnRestoreColors, gbc_btnRestoreColors);

		JPanel tabColumns = new JPanel();
		tabbedPane.addTab("Columns/Lines", null, tabColumns, null);
		GridBagLayout gbl_tabColumns = new GridBagLayout();
		gbl_tabColumns.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_tabColumns.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_tabColumns.columnWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_tabColumns.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		tabColumns.setLayout(gbl_tabColumns);
		
		Component rigidArea_3 = Box.createRigidArea(new Dimension(30, 30));
		GridBagConstraints gbc_rigidArea_3 = new GridBagConstraints();
		gbc_rigidArea_3.insets = new Insets(0, 0, 5, 5);
		gbc_rigidArea_3.gridx = 0;
		gbc_rigidArea_3.gridy = 0;
		tabColumns.add(rigidArea_3, gbc_rigidArea_3);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Columns", TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(0, 0, 5, 5);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 1;
		gbc_panel_2.gridy = 1;
		tabColumns.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0, 0};
		gbl_panel_2.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel_2.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);
		
		rb80 = new JRadioButton("80");
		GridBagConstraints gbc_rb80 = new GridBagConstraints();
		gbc_rb80.insets = new Insets(0, 0, 5, 0);
		gbc_rb80.gridx = 0;
		gbc_rb80.gridy = 0;
		panel_2.add(rb80, gbc_rb80);
		
		Component verticalStrut_1 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_1 = new GridBagConstraints();
		gbc_verticalStrut_1.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_1.gridx = 0;
		gbc_verticalStrut_1.gridy = 1;
		panel_2.add(verticalStrut_1, gbc_verticalStrut_1);
		
		rb132 = new JRadioButton("132");
		GridBagConstraints gbc_rb132 = new GridBagConstraints();
		gbc_rb132.anchor = GridBagConstraints.SOUTHEAST;
		gbc_rb132.gridx = 0;
		gbc_rb132.gridy = 2;
		panel_2.add(rb132, gbc_rb132);
		
		Component rigidArea_4 = Box.createRigidArea(new Dimension(30, 30));
		GridBagConstraints gbc_rigidArea_4 = new GridBagConstraints();
		gbc_rigidArea_4.insets = new Insets(0, 0, 5, 5);
		gbc_rigidArea_4.gridx = 0;
		gbc_rigidArea_4.gridy = 2;
		tabColumns.add(rigidArea_4, gbc_rigidArea_4);
		
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0), 1, true), "Line Behavior", TitledBorder.CENTER, TitledBorder.ABOVE_TOP, null, null));
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.insets = new Insets(0, 0, 5, 5);
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 1;
		gbc_panel_3.gridy = 3;
		tabColumns.add(panel_3, gbc_panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[]{0, 0};
		gbl_panel_3.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel_3.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panel_3.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_3.setLayout(gbl_panel_3);
		
		rbTruncate = new JRadioButton("Truncate");
		rbTruncate.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_rbTruncate = new GridBagConstraints();
		gbc_rbTruncate.anchor = GridBagConstraints.WEST;
		gbc_rbTruncate.insets = new Insets(0, 0, 5, 0);
		gbc_rbTruncate.gridx = 0;
		gbc_rbTruncate.gridy = 0;
		panel_3.add(rbTruncate, gbc_rbTruncate);
		
		Component verticalStrut = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut.gridx = 0;
		gbc_verticalStrut.gridy = 1;
		panel_3.add(verticalStrut, gbc_verticalStrut);
		
		rbWrap = new JRadioButton("Wrap");
		rbWrap.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_rbWrap = new GridBagConstraints();
		gbc_rbWrap.anchor = GridBagConstraints.WEST;
		gbc_rbWrap.gridx = 0;
		gbc_rbWrap.gridy = 2;
		panel_3.add(rbWrap, gbc_rbWrap);
		
		JButton btnRestoreColumns = new JButton("Restore");
		btnRestoreColumns.setActionCommand(BTN_RESTORE_COLUMNS);
		btnRestoreColumns.addActionListener(adapterDialog);
		btnRestoreColumns.setVerticalAlignment(SwingConstants.BOTTOM);
		btnRestoreColumns.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_btnRestoreColumns = new GridBagConstraints();
		gbc_btnRestoreColumns.anchor = GridBagConstraints.SOUTHEAST;
		gbc_btnRestoreColumns.gridx = 2;
		gbc_btnRestoreColumns.gridy = 4;
		tabColumns.add(btnRestoreColumns, gbc_btnRestoreColumns);

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
		
		ButtonGroup bgBehavior = new ButtonGroup();
		bgBehavior.add(rbWrap);
		bgBehavior.add(rbTruncate);
		
		ButtonGroup bgColumns = new ButtonGroup();
		bgColumns.add(rb132);
		bgColumns.add(rb80);


	}// initialize

	private static final String BTN_OK = "btnOk";
	private static final String BTN_CANCEL = "btnCancel";
	
	private static final String BTN_RESTORE_FONT = "btnRestoreFont";
	private static final String BTN_RESTORE_COLORS = "btnRestoreColors";
	private static final String BTN_RESTORE_COLUMNS = "btnRestoreColumns";

	private static final String BTN_COLOR_TEXT = "btnColorText";
	private static final String BTN_COLOR_BACKGROUND = "btnColorBackground";
	private static final String BTN_COLOR_CARET = "btnColorCaret";

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
	private JTextPane textExample;
	private JPanel panelColor;
	private JRadioButton rb80;
	private JRadioButton rb132;
	private JRadioButton rbTruncate;
	private JRadioButton rbWrap;

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
				
				
			case BTN_COLOR_TEXT:
				doColorText();
				break;
			case BTN_COLOR_BACKGROUND:
				doColorBackground();
				break;
			case BTN_COLOR_CARET:
				doColorCaret();
				break;
			case BTN_RESTORE_COLORS:
				doRestoreColors();
				break;
			case BTN_RESTORE_COLUMNS:
				doRestoreColumns();
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
