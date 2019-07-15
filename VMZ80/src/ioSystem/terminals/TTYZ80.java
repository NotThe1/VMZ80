package ioSystem.terminals;

/*
 * 		2018-12-06  Changed Status return Value:
 *                  MSB == 1, Output ready, Bits0-7 contains number of bytes in Keybord buffer
 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;

import codeSupport.ASCII_CODES;
import codeSupport.AppLogger;
import codeSupport.Z80;
import ioSystem.DeviceZ80;
import utilities.fontChooser.FontChooser;

public class TTYZ80 extends DeviceZ80 {

	private AdapterTTY adapterTTY = new AdapterTTY();

	AppLogger log = AppLogger.getInstance();

	private Document screen;
	private int maxColumn;
	private boolean truncateColumns;
	private int tabSize;
	private Queue<Byte> internalBuffer = new LinkedList<Byte>();

	public void run() {
		while (true) {
			if (statusFromCPU.size() > 0) {
				statusFromCPU.poll();
				byte charsInBuffer = (byte) (dataToCPU.size() & 0x7F);
				// Set MSB if  ready for data from CPU
				byte statusValue = (byte) (((charsInBuffer) & Z80.BYTE_MASK)|STATUS_OUT_READY );
				statusToCPU.offer( statusValue);
			} // if Status request

			if (dataFromCPU.size() > 0) {
				byteFromCPU(dataFromCPU.poll());
			} // if byte to read

			try {
				TimeUnit.MICROSECONDS.sleep(100);//1000 = i milli
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // try
			while (internalBuffer.size() != 0) {
				dataToCPU.offer(internalBuffer.poll());
			} // while data
		} // while - true
	}// run

	@Override
	public void byteToCPU(Byte value) {
		dataToCPU.offer(value);
	}// byteToCPU

	@Override
	public void byteFromCPU(Byte value) {
		char c = (char) ((byte) value);
		int usi = Byte.toUnsignedInt(value);

		if (usi < 0x20) {// Non printables
			switch (c) {
			case ASCII_CODES.BS:
				doBackSpace();
				break;
			case ASCII_CODES.TAB:
				doTab();
				break;
			case ASCII_CODES.LF: // 0x0A
				display(Character.toString(c));
				break;
			case ASCII_CODES.CR: // 0x0D
				/* Ignore CR */
				break;
			default:
				/* Ignore the rest */
				break;

			}// switch

		} else if ((usi >= 0x20) && (usi <= 0x7F)) {
			/* all the printable characters */
			displayPrintable(Character.toString(c));
		} else {
			/* above ascii into EBDIC */
		} // if

	}// byteFromCPU

	private void displayPrintable(String s) {
		Element lastElement = getLastElement();

		if (!truncateColumns) { // drop anything beyond the max column
			display(s);
		} else if ((lastElement.getEndOffset() - lastElement.getStartOffset()) < this.maxColumn) {
			display(s);
		} else {
			/* ignore this string */
		} // if

		textScreen.setCaretPosition(screen.getLength());
	}// displayPrintable

	private void display(String s) {
		// Element[] elements = doc.getRootElements();
		try {
			screen.insertString(screen.getLength(), s, null);
		} catch (BadLocationException e) {
			log.error("[TTYZ80.display()]  insert out of bounds");
		} // try
	}// display

	private void doTab() {
		Element lastElement = getLastElement();
		int column = lastElement.getEndOffset() - lastElement.getStartOffset();
		int numberOfSpaces = tabSize - (column % tabSize);
		for (int i = 0; i < numberOfSpaces; i++) {
			displayPrintable(SPACE);
		} // for
	}// doTab;

	private void doBackSpace() {
		int currentPosition = screen.getLength();
		Element lastElement = getLastElement();
		if (currentPosition > lastElement.getStartOffset()) {
			try {
				screen.remove(currentPosition - 1, 1);
			} catch (Exception e) {
				log.error("[TTYZ80.doBackSpace] - backspace failure");
			} // try
		} // if
	}// doBackSpace

	private Element getLastElement() {
		Element root = screen.getDefaultRootElement();
		return root.getElement(root.getElementCount() - 1);
	}// getLastElement

	private void showStatus(char keyChar) {
		String msg = String.format("Last Char = %s     [0x%02X]", keyChar, (byte) keyChar);
		lblKeyChar.setText(msg);
	}// showStatus

	private void clearDoc() {
		try {
			screen.remove(0, screen.getLength());
		} catch (BadLocationException e) {
			log.error("Failed to clear screen: " + e.getMessage());
		} // try
	}// clearDoc

	///////////////////////////////////////////////////////////////////////////////////////

	private void doClearScreen() {
		clearDoc();
	}// doClearScreen

	private void doClearInBuffer() {
		dataFromCPU.clear();
	}// doClearInBuffer

	private void doColumnBehavior() {
		if (mnuBehaviorTruncate.isSelected()) {
			truncateColumns = true;
			textScreen.setLineWrap(false);
		} else if (mnuBehaviorWrap.isSelected()) {
			truncateColumns = false;
			textScreen.setLineWrap(true);
		} else if (mnuBehaviorExtend.isSelected()) {
			truncateColumns = false;
			textScreen.setLineWrap(false);
		} else {

		} // if
	}// doColumnBehavior

	private void doSetFont() {
		FontChooser fontChooser = new FontChooser(frameTTY, textScreen.getFont());

		if (fontChooser.showDialog() == JOptionPane.OK_OPTION) {
			textScreen.setFont(fontChooser.selectedFont());
		} // if

		fontChooser = null;
		textScreen.getCaret().setVisible(true);
	}// doSetFont

	private void doSetTextColor() {
		Color color = JColorChooser.showDialog(frameTTY, "Font Color", textScreen.getForeground());
		if (color != null) {
			textScreen.setForeground(color);
			textScreen.getCaret().setVisible(true);
		} // if
	}// doSetTextColor

	private void doSetBackgroundColor() {
		Color color = JColorChooser.showDialog(frameTTY, "Font Color", textScreen.getBackground());
		if (color != null) {
			textScreen.setBackground(color);
			textScreen.getCaret().setVisible(true);
		} // if
	}// doSetBackgroundColor

	private void doSetCaretColor() {
		Color color = JColorChooser.showDialog(frameTTY, "Font Color", textScreen.getCaretColor());
		if (color != null) {
			textScreen.setCaretColor(color);
			textScreen.getCaret().setVisible(true);
		} // if
	}// doSetCaretColor

	private void doColumnsChanged() {
		maxColumn = (int) spinnerColumns.getValue();
	}// doColumnsChanged

	private void setupScreen(Preferences myPrefs, JTextArea textScreen) {
		textScreen.setCaretColor(new Color(myPrefs.getInt("CaretColor", Color.RED.getRGB())));
		textScreen.setBackground(new Color(myPrefs.getInt("BackgroundColor", Color.BLACK.getRGB())));
		textScreen.setForeground(new Color(myPrefs.getInt("ForegroundColor", Color.GREEN.getRGB())));

		Font screenFont = new Font(myPrefs.get("FontFamily", "Courier New"), myPrefs.getInt("FontStyle", Font.PLAIN),
				myPrefs.getInt("FontSize", 13));
		textScreen.setFont(screenFont);
		textScreen.setEditable(false);
		textScreen.getCaret().setVisible(false);

	}// setupScreen

	@Override
	public void setVisible(boolean state) {
		frameTTY.setVisible(state);
	}// setVisible

	@Override
	public boolean isVisible() {
		return frameTTY.isVisible();
	}// isVisible

	public void close() {
		appClose();
	}// close

	//////////////////////////////////////////////////////////////////////////////////////
	private void appClose() {
		Preferences myPrefs = Preferences.userNodeForPackage(TTYZ80.class).node(this.getClass().getSimpleName());
		Dimension dim = frameTTY.getSize();
		myPrefs.putInt("Height", dim.height);
		myPrefs.putInt("Width", dim.width);
		Point point = frameTTY.getLocation();
		myPrefs.putInt("LocX", point.x);
		myPrefs.putInt("LocY", point.y);

		myPrefs.putBoolean("TruncateColumns", truncateColumns);
		myPrefs.putInt("MaxColumn", maxColumn);

		myPrefs.putBoolean("Extended", mnuBehaviorExtend.isSelected());
		myPrefs.putBoolean("Wrap", mnuBehaviorWrap.isSelected());
		myPrefs.putBoolean("Truncate", mnuBehaviorTruncate.isSelected());

		myPrefs.putInt("CaretColor", textScreen.getCaretColor().getRGB());
		myPrefs.putInt("BackgroundColor", textScreen.getBackground().getRGB());
		myPrefs.putInt("ForegroundColor", textScreen.getForeground().getRGB());

		myPrefs.put("FontFamily", textScreen.getFont().getFamily());
		myPrefs.putInt("FontStyle", textScreen.getFont().getStyle());
		myPrefs.putInt("FontSize", textScreen.getFont().getSize());

		myPrefs = null;

	}// appClose

	private void appInit() {
		Preferences myPrefs = Preferences.userNodeForPackage(TTYZ80.class).node(this.getClass().getSimpleName());
		frameTTY.setSize(myPrefs.getInt("Width", 761), myPrefs.getInt("Height", 693));
		frameTTY.setLocation(myPrefs.getInt("LocX", 100), myPrefs.getInt("LocY", 100));

		truncateColumns = myPrefs.getBoolean("TruncateColumns", false);
		maxColumn = myPrefs.getInt("MaxColumn", 80);

		mnuBehaviorExtend.setSelected(myPrefs.getBoolean("Extended", true));
		mnuBehaviorWrap.setSelected(myPrefs.getBoolean("Wrap", false));
		mnuBehaviorTruncate.setSelected(myPrefs.getBoolean("Truncate", false));
		setupScreen(myPrefs, textScreen);

		myPrefs = null;
		tabSize = 9;

		doColumnBehavior();
		spinnerColumns.setValue(maxColumn);

		screen = textScreen.getDocument();
		clearDoc();
		frameTTY.setVisible(true);

	}// appInit

	/**
	 * Create the application.
	 * 
	 * @wbp.parser.entryPoint
	 */
	/* @formatter:off */
	public TTYZ80(String name,Byte addressIn,Byte addressOut,Byte addressStatus){
		super(name,addressIn,addressOut,addressStatus);
		initialize();
		appInit();
	}// Constructor
/* @formatter:on  */

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frameTTY = new JFrame();
		frameTTY.setTitle("TTYZ80              Rev 1.0.1");
		frameTTY.setBounds(100, 100, 450, 300);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		frameTTY.getContentPane().setLayout(gridBagLayout);

		JPanel panelForButtons = new JPanel();
		GridBagConstraints gbc_panelForButtons = new GridBagConstraints();
		gbc_panelForButtons.insets = new Insets(0, 0, 5, 0);
		gbc_panelForButtons.fill = GridBagConstraints.BOTH;
		gbc_panelForButtons.gridx = 0;
		gbc_panelForButtons.gridy = 0;
		frameTTY.getContentPane().add(panelForButtons, gbc_panelForButtons);
		GridBagLayout gbl_panelForButtons = new GridBagLayout();
		gbl_panelForButtons.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelForButtons.rowHeights = new int[] { 0, 0 };
		gbl_panelForButtons.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelForButtons.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelForButtons.setLayout(gbl_panelForButtons);

		JPanel panelClear = new JPanel();
		panelClear.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Clear", TitledBorder.CENTER,
				TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panelClear = new GridBagConstraints();
		gbc_panelClear.insets = new Insets(0, 0, 0, 5);
		gbc_panelClear.fill = GridBagConstraints.BOTH;
		gbc_panelClear.gridx = 0;
		gbc_panelClear.gridy = 0;
		panelForButtons.add(panelClear, gbc_panelClear);
		GridBagLayout gbl_panelClear = new GridBagLayout();
		gbl_panelClear.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelClear.rowHeights = new int[] { 0, 0 };
		gbl_panelClear.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelClear.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelClear.setLayout(gbl_panelClear);

		JButton btnClearScreen = new JButton("Screen");
		btnClearScreen.setName(BTN_CLEAR_SCREEN);
		btnClearScreen.addActionListener(adapterTTY);
		GridBagConstraints gbc_btnClearScreen = new GridBagConstraints();
		gbc_btnClearScreen.insets = new Insets(0, 0, 0, 5);
		gbc_btnClearScreen.gridx = 0;
		gbc_btnClearScreen.gridy = 0;
		panelClear.add(btnClearScreen, gbc_btnClearScreen);

		JButton btnClearInBuffer = new JButton("Input Buffer");
		btnClearInBuffer.setName(BTN_CLEAR_IN_BUFFER);
		btnClearInBuffer.addActionListener(adapterTTY);
		GridBagConstraints gbc_btnClearInBuffer = new GridBagConstraints();
		gbc_btnClearInBuffer.gridx = 1;
		gbc_btnClearInBuffer.gridy = 0;
		panelClear.add(btnClearInBuffer, gbc_btnClearInBuffer);

		JPanel panelColumns = new JPanel();
		panelColumns.setBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "Columns", TitledBorder.CENTER,
				TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panelColumns = new GridBagConstraints();
		gbc_panelColumns.fill = GridBagConstraints.BOTH;
		gbc_panelColumns.gridx = 1;
		gbc_panelColumns.gridy = 0;
		panelForButtons.add(panelColumns, gbc_panelColumns);
		GridBagLayout gbl_panelColumns = new GridBagLayout();
		gbl_panelColumns.columnWidths = new int[] { 0, 0 };
		gbl_panelColumns.rowHeights = new int[] { 0, 0 };
		gbl_panelColumns.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelColumns.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelColumns.setLayout(gbl_panelColumns);

		spinnerColumns = new JSpinner();
		spinnerColumns.setName(SPINNER_COLUMNS);
		spinnerColumns.addChangeListener(adapterTTY);
		spinnerColumns.setPreferredSize(new Dimension(90, 20));
		GridBagConstraints gbc_spinnerColumns = new GridBagConstraints();
		gbc_spinnerColumns.gridx = 0;
		gbc_spinnerColumns.gridy = 0;
		panelColumns.add(spinnerColumns, gbc_spinnerColumns);

		JPanel panelScreen = new JPanel();
		GridBagConstraints gbc_panelScreen = new GridBagConstraints();
		gbc_panelScreen.insets = new Insets(0, 0, 5, 0);
		gbc_panelScreen.fill = GridBagConstraints.BOTH;
		gbc_panelScreen.gridx = 0;
		gbc_panelScreen.gridy = 1;
		frameTTY.getContentPane().add(panelScreen, gbc_panelScreen);
		GridBagLayout gbl_panelScreen = new GridBagLayout();
		gbl_panelScreen.columnWidths = new int[] { 0, 0 };
		gbl_panelScreen.rowHeights = new int[] { 0, 0 };
		gbl_panelScreen.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelScreen.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelScreen.setLayout(gbl_panelScreen);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panelScreen.add(scrollPane, gbc_scrollPane);

		textScreen = new JTextArea();
		textScreen.addKeyListener(adapterTTY);
		textScreen.addFocusListener(adapterTTY);
		scrollPane.setViewportView(textScreen);

		JPanel panelStatus = new JPanel();
		GridBagConstraints gbc_panelStatus = new GridBagConstraints();
		gbc_panelStatus.anchor = GridBagConstraints.SOUTH;
		gbc_panelStatus.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelStatus.gridx = 0;
		gbc_panelStatus.gridy = 2;
		frameTTY.getContentPane().add(panelStatus, gbc_panelStatus);
		GridBagLayout gbl_panelStatus = new GridBagLayout();
		gbl_panelStatus.columnWidths = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panelStatus.rowHeights = new int[] { 25, 0 };
		gbl_panelStatus.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelStatus.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelStatus.setLayout(gbl_panelStatus);

		lblKeyChar = new JLabel("<Start>");
		GridBagConstraints gbc_lblKeyChar = new GridBagConstraints();
		gbc_lblKeyChar.insets = new Insets(0, 0, 0, 5);
		gbc_lblKeyChar.gridx = 0;
		gbc_lblKeyChar.gridy = 0;
		panelStatus.add(lblKeyChar, gbc_lblKeyChar);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
		gbc_horizontalStrut.insets = new Insets(0, 0, 0, 5);
		gbc_horizontalStrut.gridx = 1;
		gbc_horizontalStrut.gridy = 0;
		panelStatus.add(horizontalStrut, gbc_horizontalStrut);

		lblKeyText = new JLabel("<Start>");
		lblKeyText.setVisible(false);
		GridBagConstraints gbc_lblKeyText = new GridBagConstraints();
		gbc_lblKeyText.insets = new Insets(0, 0, 0, 5);
		gbc_lblKeyText.gridx = 2;
		gbc_lblKeyText.gridy = 0;
		panelStatus.add(lblKeyText, gbc_lblKeyText);

		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		GridBagConstraints gbc_horizontalStrut_1 = new GridBagConstraints();
		gbc_horizontalStrut_1.insets = new Insets(0, 0, 0, 5);
		gbc_horizontalStrut_1.gridx = 3;
		gbc_horizontalStrut_1.gridy = 0;
		panelStatus.add(horizontalStrut_1, gbc_horizontalStrut_1);

		lblReleased = new JLabel("<Start>");
		lblReleased.setVisible(false);
		GridBagConstraints gbc_lblReleased = new GridBagConstraints();
		gbc_lblReleased.gridx = 4;
		gbc_lblReleased.gridy = 0;
		panelStatus.add(lblReleased, gbc_lblReleased);

		JMenuBar menuBar = new JMenuBar();
		frameTTY.setJMenuBar(menuBar);

		JMenu mnuBehavior = new JMenu("Behavior");
		menuBar.add(mnuBehavior);

		mnuBehaviorTruncate = new JRadioButtonMenuItem("Truncate");
		mnuBehaviorTruncate.setName(MNU_BEHAVIOR_TRUNCATE);
		mnuBehaviorTruncate.addActionListener(adapterTTY);
		mnuBehavior.add(mnuBehaviorTruncate);

		mnuBehaviorWrap = new JRadioButtonMenuItem("Wrap");
		mnuBehaviorWrap.setName(MNU_BEHAVIOR_WRAP);
		mnuBehaviorWrap.addActionListener(adapterTTY);
		mnuBehavior.add(mnuBehaviorWrap);

		mnuBehaviorExtend = new JRadioButtonMenuItem("Extend");
		mnuBehaviorExtend.setName(MNU_BEHAVIOR_EXTEND);
		mnuBehaviorExtend.addActionListener(adapterTTY);
		mnuBehavior.add(mnuBehaviorExtend);

		ButtonGroup bgBehavior = new ButtonGroup();
		bgBehavior.add(mnuBehaviorExtend);
		bgBehavior.add(mnuBehaviorWrap);
		bgBehavior.add(mnuBehaviorTruncate);

		JMenu mnuProperties = new JMenu("Properties");
		menuBar.add(mnuProperties);

		JMenuItem mnuPropertiesFont = new JMenuItem("Font...");
		mnuPropertiesFont.setName(MNU_PROPERTIES_FONT);
		mnuPropertiesFont.addActionListener(adapterTTY);
		mnuProperties.add(mnuPropertiesFont);

		JSeparator separator = new JSeparator();
		mnuProperties.add(separator);

		JMenuItem mnuPropertiesTextColor = new JMenuItem("Text Color");
		mnuPropertiesTextColor.setName(MNU_PROPERTIES_TEXT_COLOR);
		mnuPropertiesTextColor.addActionListener(adapterTTY);
		mnuProperties.add(mnuPropertiesTextColor);

		JMenuItem mnuPropertiesBackgroundColor = new JMenuItem("Background Color");
		mnuPropertiesBackgroundColor.setName(MNU_PROPERTIES_BACKGROUND_COLOR);
		mnuPropertiesBackgroundColor.addActionListener(adapterTTY);
		mnuProperties.add(mnuPropertiesBackgroundColor);

		JMenuItem mnuPropertiesCaretColor = new JMenuItem("Caret Color");
		mnuPropertiesCaretColor.setName(MNU_PROPERTIES_CARET_COLOR);
		mnuPropertiesCaretColor.addActionListener(adapterTTY);
		mnuProperties.add(mnuPropertiesCaretColor);
		frameTTY.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frameTTY.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				appClose();
			}
		});

	}// initialize
		/////////////////////////////////////////////////////////

	class AdapterTTY implements ActionListener, ChangeListener, KeyListener, FocusListener {
		/* ActionListener */

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String name = ((Component) actionEvent.getSource()).getName();
			switch (name) {
			case BTN_CLEAR_SCREEN:
				doClearScreen();
				break;
			case BTN_CLEAR_IN_BUFFER:
				doClearInBuffer();
				break;
			case MNU_BEHAVIOR_TRUNCATE:
			case MNU_BEHAVIOR_WRAP:
			case MNU_BEHAVIOR_EXTEND:
				doColumnBehavior();
				break;
			case MNU_PROPERTIES_FONT:
				doSetFont();
				break;
			case MNU_PROPERTIES_TEXT_COLOR:
				doSetTextColor();
				break;
			case MNU_PROPERTIES_BACKGROUND_COLOR:
				doSetBackgroundColor();
				break;
			case MNU_PROPERTIES_CARET_COLOR:
				doSetCaretColor();
				break;
			}// switch
		}// actionPerformed

		/* ChangeListener */

		@Override
		public void stateChanged(ChangeEvent changeEvent) {
			String name = ((Component) changeEvent.getSource()).getName();
			if (name.equals(SPINNER_COLUMNS)) {
				doColumnsChanged();
			} else {
				log.error("Bad State change, name is: " + name + ".");
			} // if

		}// stateChanged

		/* KeyListener */

		@Override
		public void keyPressed(KeyEvent keyEvent) {
			/* not implemented */
		}// keyPressed

		@Override
		public void keyReleased(KeyEvent keyEvent) {
			/* not implemented */
		}// keyReleased

		@Override
		public void keyTyped(KeyEvent keyEvent) {
			internalBuffer.offer((byte) keyEvent.getKeyChar());
			// byteToCPU((byte) keyEvent.getKeyChar());byteToCPU(internalBuffer.poll();
			showStatus(keyEvent.getKeyChar());
		}// keyTyped

		/* FocusListener */

		@Override
		public void focusGained(FocusEvent focusEvent) {
			textScreen.getCaret().setVisible(true);
		}// focusGained

		@Override
		public void focusLost(FocusEvent focusEvent) {
			textScreen.getCaret().setVisible(false);
		}// focusLost

	}// class AdapterTTY

	public static final Byte IN = (byte) 0X0EC;
	public static final Byte OUT = (byte) 0X0EC;
	public static final Byte STATUS = (byte) 0X0ED;
	public static final Byte STATUS_OUT_READY = (byte) 0b1000_0000;  // MSB set
//	private static final Byte STATUS_RESPONSE = (byte) 0X03;

	// private static final String EMPTY_STRING = "";
	private static final String SPACE = " ";

	private static final String BTN_CLEAR_SCREEN = "btnClearScreen";
	private static final String BTN_CLEAR_IN_BUFFER = "btnClearKeyboardBuffer";

	private static final String MNU_BEHAVIOR_TRUNCATE = "mnuBehaviorTruncate";
	private static final String MNU_BEHAVIOR_WRAP = "mnuBehaviorWrap";
	private static final String MNU_BEHAVIOR_EXTEND = "mnuBehaviorExtend";

	private static final String MNU_PROPERTIES_FONT = "mnuPropertiesFont";
	private static final String MNU_PROPERTIES_TEXT_COLOR = "mnuPropertiesTextColor";
	private static final String MNU_PROPERTIES_BACKGROUND_COLOR = "mnuPropertiesBackgroundColor";
	private static final String MNU_PROPERTIES_CARET_COLOR = "mnuPropertiesCaretColor";

	private static final String SPINNER_COLUMNS = "spinnerColumns";
	

	private JFrame frameTTY;
	private JLabel lblKeyChar;
	private JLabel lblKeyText;
	private JLabel lblReleased;
	private JRadioButtonMenuItem mnuBehaviorTruncate;
	private JRadioButtonMenuItem mnuBehaviorWrap;
	private JRadioButtonMenuItem mnuBehaviorExtend;
	private JTextArea textScreen;
	private JSpinner spinnerColumns;

//	@Override
//	public Byte getAddressIn() {
//		return IN;
//	}// getAddressIn
//
//	@Override
//	public Byte getAddressOut() {
//		return OUT;
//	}// getAddressOut
//
//	@Override
//	public Byte getAddressStatus() {
//		return STATUS;
//	}// getAddressStatus


	

}// class TTY
