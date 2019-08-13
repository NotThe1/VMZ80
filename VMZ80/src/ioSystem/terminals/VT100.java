package ioSystem.terminals;

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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.text.JTextComponent;

import codeSupport.AppLogger;
import codeSupport.RoundIcon1;
import codeSupport.Z80;
import ioSystem.DeviceZ80;

public class VT100 extends DeviceZ80 {

	private JFrame frameVT100;
	private JTextPane txtScreen = new JTextPane();
	private VT100Display screen;
	private RoundIcon1 ledON = new RoundIcon1(Color.RED);
	private RoundIcon1 ledOFF = new RoundIcon1(Color.BLACK);

	private Queue<Byte> internalBuffer = new LinkedList<Byte>();
	private Queue<Byte> escapeBuffer = new LinkedList<Byte>();
	private AdapterVT100 adapterVT100 = new AdapterVT100();
	private AppLogger log = AppLogger.getInstance();

	private int screenColumns;
//	private int lineLength; // accounts for System.lineSeparator
	private String screenTitle = "VT100            Rev 1.0";


	private InputState inputState;

	@Override
	public void run() {
		while (true) {
			if (statusFromCPU.size() > 0) {
				statusFromCPU.poll();
				byte charsInBuffer = (byte) (dataToCPU.size() & 0x7F);
				// Set MSB if ready for data from CPU
				byte statusValue = (byte) (((charsInBuffer) & Z80.BYTE_MASK) | STATUS_OUT_READY);
				statusToCPU.offer(statusValue);
			} // if Status request

			if (dataFromCPU.size() > 0) {
				byteFromCPU(dataFromCPU.poll());
			} // if byte to read

			try {
				TimeUnit.MICROSECONDS.sleep(100);// 1000 = i milli
			} catch (InterruptedException e) {
				e.printStackTrace();
			} // try
			while (internalBuffer.size() != 0) {
				dataToCPU.offer(internalBuffer.poll());
			} // while data
		} // while - true }// run
	}// run

	private void setFrameSize(JTextComponent component, int columns) {
		Insets insetsFrame = frameVT100.getInsets();
		Insets insetsContentPane = frameVT100.getContentPane().getInsets();
		Insets insetsPanelMain = panelMain.getInsets();
		Insets insetsTextPane = txtScreen.getInsets();

		int horizontalInsets = insetsFrame.left + insetsFrame.right + insetsContentPane.left + insetsContentPane.right
				+ insetsPanelMain.left + insetsPanelMain.right + insetsTextPane.left + insetsTextPane.right;

		int verticalInsets = insetsFrame.top + insetsFrame.bottom + insetsContentPane.top + insetsContentPane.bottom
				+ insetsPanelMain.top + insetsPanelMain.bottom + insetsTextPane.top + insetsTextPane.bottom
				+ panelStatus.getHeight() + frameVT100.getJMenuBar().getHeight();

		Dimension screenSize = screen.getSize();

		frameVT100.setSize(screenSize.width + horizontalInsets, screenSize.height + verticalInsets);

	}// setScreenSize

	@Override
	public void byteFromCPU(Byte value) {
		showInChar((char) (byte) value);
		InputState state = getState();
		switch (state) {
		case Text:
			if (value == ASCII_ESC) {// Escape 0x1B
				setInputState(InputState.ESC_PART0);
			} else {
				asciiInFromCPU((byte) (value & 0x7F));
			} // if
			break;
		case ESC_PART0:
			setInputState(value == ASCII_LEFT_PARENTHSIS ? InputState.ESC_PART1 : InputState.Text);
			break;
		case ESC_PART1:
			escape1(value);
			break;

		case ESC_SemiColon:
			escapeSemiColon(value);
			break;
		case ESC_QMark:
			escapeQMark(value);
			break;
		case ESC_NUMBER:
			escapeNumber(value);
			break;
		case ESC_Q1:
			escapeQ1(value);
			break;
		case ESC_Q3:
			escapeQ3(value);
			break;
		case ESC_Q4:
			escapeQ4(value);
			break;
		case ESC_Q5:
			escapeQ5(value);
			break;
		case ESC_Q7:
			escapeQ7(value);
			break;
		default:
			log.errorf("InputState error: %s%n", state.toString());
		}// switch
		showCursorPosition();

	}// byteFromCPU

	private void asciiInFromCPU(byte value) {
		int usi = Byte.toUnsignedInt(value);

		if ((usi >= ASCII_SPACE) && (usi < ASCII_DEL)) {
			screen.displayOnScreen(Character.toString((char) (value)));
		} else {
			switch (value) {
			case ASCII_LF:
				screen.nextLine();
			case ASCII_CR:
				screen.carriageReturn();
				break;
			case ASCII_BS:
				screen.backSpace();
				break;
			case ASCII_TAB:
				screen.doTab();
				break;
			case ASCII_DEL:
				screen.doDel();
				break;
			default:
				/* Ignore the rest */
			}// switch
		} // if else

	}// asciiFromCPU

	private void escape1(byte value) {
		switch (value) {
		case ASCII_m:
			log.infof("Escape Sequence %s%n", "Turn off character attributes");
			setInputState(InputState.Text);
			break;
		case ASCII_A:
			screen.moveCursorUp(1);
			setInputState(InputState.Text);
			break;
		case ASCII_B:
			screen.moveCursorDown(1);
			setInputState(InputState.Text);
			break;
		case ASCII_C:
			screen.moveCursorRight(1);
			setInputState(InputState.Text);
			break;
		case ASCII_D:
			screen.moveCursorLeft(1);
			setInputState(InputState.Text);
			break;
		case ASCII_H:
		case ASCII_f: // move cursor to upper left.
			screen.moveCursor(0, 0);
			showCursorPosition();
			setInputState(InputState.Text);
			break;
		case ASCII_g:
			log.infof("Escape Sequence %s%n", "Clear Tab at current column");
			setInputState(InputState.Text);
			break;
		case ASCII_J:
			screen.clearFromCursorDown();
			setInputState(InputState.Text);
			break;
		case ASCII_K:
			screen.clearRight();
			setInputState(InputState.Text);
			break;
		case ASCII_SEMI_COLON:
			setInputState(InputState.ESC_SemiColon);
			break;
		case ASCII_QMARK:
			setInputState(InputState.ESC_QMark);
			break;

		case ASCII_0:
		case ASCII_1:
		case ASCII_2:
		case ASCII_3:
		case ASCII_4:
		case ASCII_5:
		case ASCII_6:
		case ASCII_7:
		case ASCII_8:
		case ASCII_9:
			escapeBuffer.add(value);
			setInputState(InputState.ESC_NUMBER);
			break;
		default:
			setInputState(InputState.Text);
		}// switch
	}// escape1

	private void escapeSemiColon(byte value) {
		if ((value == ASCII_f) || (value == ASCII_H)) {
			screen.moveCursor(0, 0);
			showCursorPosition();
		} // if
		setInputState(InputState.Text);
	}// escapeSemiColon

	private void escapeQMark(byte value) {
		switch (value) {
		case ASCII_1:
			setInputState(InputState.ESC_Q1);
			break;
		case ASCII_3:
			setInputState(InputState.ESC_Q3);
			break;
		case ASCII_4:
			setInputState(InputState.ESC_Q4);
			break;
		case ASCII_5:
			setInputState(InputState.ESC_Q5);
			break;
		case ASCII_7:
			setInputState(InputState.ESC_Q7);
			break;
		default:
			setInputState(InputState.Text);
		}// switch
	}// escapeQMark

	private void escapeQ1(byte value) {
		if (value == ASCII_h) {
			log.infof("Escape Sequence %s%n", "Set cursor key to application");
		} else if (value == ASCII_l) {
			log.infof("Escape Sequence %s%n", "Set cursor key to cusor");
		} // if
		setInputState(InputState.Text);
	}// escapeQ1

	private void escapeQ3(byte value) {
		int originalScreenColumns = screenColumns;

		if (value == ASCII_h) {
			screenColumns = 132;
		} else if (value == ASCII_l) {
			screenColumns = 80;
		} // if
		if (screenColumns != originalScreenColumns) {
			screen.setScreenColumns(screenColumns);
			setFrameSize(txtScreen, screenColumns);
			screen.makeNewScreen();
		} // if
		setInputState(InputState.Text);
	}// escapeQ3

	private void escapeQ4(byte value) {
		if (value == ASCII_h) {
			log.infof("Escape Sequence %s%n", "Set smooth scrolling");
		} else if (value == ASCII_l) {
			log.infof("Escape Sequence %s%n", "Set jump scrolling");
		} // if
		setInputState(InputState.Text);
	}// escapeQ4

	private void escapeQ5(byte value) {
		if (value == ASCII_h) {
			log.infof("Escape Sequence %s%n", "Set reverse video on screen");
		} else if (value == ASCII_l) {
			log.infof("Escape Sequence %s%n", "Set normal video on screen");
		} // if
		setInputState(InputState.Text);
	}// escapeQ5

	private void escapeQ7(byte value) {
		if (value == ASCII_h) {
			screen.setWrap(true);
			log.infof("Escape Sequence %s%n", "Set auto-wrap mode");
		} else if (value == ASCII_l) {
			screen.setWrap(false);
			log.infof("Escape Sequence %s%n", "Reset auto-wrap mode");
		} // if
		setInputState(InputState.Text);
	}// escapeQ7

	private void escapeNumber(byte value) {
		// int escapeValue;
		switch (value) {
		case ASCII_SEMI_COLON:
		case ASCII_0:
		case ASCII_1:
		case ASCII_2:
		case ASCII_3:
		case ASCII_4:
		case ASCII_5:
		case ASCII_6:
		case ASCII_7:
		case ASCII_8:
		case ASCII_9:
			escapeBuffer.add(value);
			setInputState(InputState.ESC_NUMBER);
			break;

		case ASCII_H:
		case ASCII_f:
			// check for the;
			if (escapeBuffer.contains(ASCII_SEMI_COLON)) {
				String escapeValuesString = getEscapeValuesString();
				String[] lineRow = escapeValuesString.split(SEMI_COLON);
				int row = lineRow[0] == null ? 0 : Integer.valueOf(new String(lineRow[0]));
				int col = lineRow[1] == null ? 0 : Integer.valueOf(new String(lineRow[1]));
				screen.moveCursor(row, col);
			} // if
			setInputState(InputState.Text);
			break;

		case ASCII_A:
			screen.moveCursorUp(getEscapeValue());
			setInputState(InputState.Text);
			break;
		case ASCII_B:
			screen.moveCursorDown(getEscapeValue());
			setInputState(InputState.Text);
			break;
		case ASCII_C:
			screen.moveCursorRight(getEscapeValue());
			setInputState(InputState.Text);
			break;
		case ASCII_D:
			screen.moveCursorLeft(getEscapeValue());
			setInputState(InputState.Text);
			break;

		case ASCII_h:
			if (getEscapeValue() == 20) {
				log.infof("Escape Sequence %s%n", "Set new line mode");
			} // if
			setInputState(InputState.Text);
			break;
		case ASCII_l:
			if (getEscapeValue() == 20) {
				log.infof("Escape Sequence %s%n", "Set line feed mode");
			} // if
			setInputState(InputState.Text);
			break;
		case ASCII_g:
			if (getEscapeValue() == 3) {
				log.infof("Escape Sequence %s%n", "Clear all tabs");
			} // if
			setInputState(InputState.Text);
			break;

		case ASCII_J:
			switch (getEscapeValue()) {
			case 0:
				screen.clearFromCursorDown();
				setInputState(InputState.Text);
				break;
			case 1:
				screen.clearFromCursorUp();
				setInputState(InputState.Text);
				break;
			case 2:
				screen.makeNewScreen();
				setInputState(InputState.Text);
				break;
			default:
				log.infof("Bad Escape Sequence %s - %02X%n", "ASCII_J", getEscapeValue());
			}// switch

			setInputState(InputState.Text);
			break;
		case ASCII_K:
			switch (getEscapeValue()) {
			case 0:
				screen.clearRight();
				setInputState(InputState.Text);
				break;
			case 1:
				screen.clearLeft();
				setInputState(InputState.Text);
				break;
			case 2:
				screen.clearEntireLine();
				setInputState(InputState.Text);
				break;
			default:
				log.infof("Bad Escape Sequence %s - %02X%n", "ASCII_K", getEscapeValue());
			}// switch

			setInputState(InputState.Text);
			break;

		case ASCII_q:
			switch (getEscapeValue()) {
			case 0:
				rbLED1.setIcon(ledOFF);
				rbLED2.setIcon(ledOFF);
				rbLED3.setIcon(ledOFF);
				rbLED4.setIcon(ledOFF);
				log.infof("Escape Sequence %s%n", "Turn off all four LEDS");
				break;
			case 1:
				rbLED1.setIcon(ledON);
				log.infof("Escape Sequence %s%n", "Turn on LED #1");
				break;
			case 2:
				rbLED2.setIcon(ledON);
				log.infof("Escape Sequence %s%n", "Turn on LED #2");
				break;
			case 3:
				rbLED3.setIcon(ledON);
				log.infof("Escape Sequence %s%n", "Turn on LED #3");
				break;
			case 4:
				rbLED4.setIcon(ledON);
				log.infof("Escape Sequence %s%n", "Turn on LED #4");
				break;
			default:
				log.infof("Bad Escape Sequence %s - %02X%n", "ASCII_K", getEscapeValue());
			}// switch
			setInputState(InputState.Text);
			break;

		case ASCII_m:
			// if (escapeBuffer.contains(ASCII_SEMI_COLON)) {
			// String escapeValuesString = getEscapeValuesString();
			// String[] attributes = escapeValuesString.split(SEMI_COLON);
			// for(String attribute:attributes) {
			// setCharacterAttribute(Integer.valueOf(attribute));
			// }//for
			// } else {
			// setCharacterAttribute(getEscapeValue());
			// } // if

			String escapeValuesString = getEscapeValuesString();
			String[] attributes = escapeValuesString.split(SEMI_COLON);
			for (String attribute : attributes) {
				setCharacterAttribute(Integer.valueOf(attribute));
			} // for
		}// switch

	}// escapeNumber

	private void setCharacterAttribute(int m_value) {
		switch (m_value) {
		case 0:
			log.infof("Escape Sequence %s%n", "Turn off all character attributes");
			break;
		case 1:
			log.infof("Escape Sequence %s%n", "Turn bold mode on");
			break;
		case 2:
			log.infof("Escape Sequence %s%n", "Turn Low intensity  mode on");
			break;
		case 3:
			log.infof("Escape Sequence %s%n", "Turn italics  mode on");
			break;
		case 4:
			log.infof("Escape Sequence %s%n", "Turn underline mode on");
			break;
		case 5:
			log.infof("Escape Sequence %s%n", "Turn blinking mode on");
			break;
		case 6:
			log.infof("Escape Sequence %s%n", "Turn rapid blinking mode on");
			break;
		case 7:
			log.infof("Escape Sequence %s%n", "Turn reverse video on");
			break;
		case 8:
			log.infof("Escape Sequence %s%n", "Turn invisible text mode on");
			break;
		default:
			log.infof("Bad Escape Sequence %s - %02X%n", "ASCII_K", getEscapeValue());
		}// switch
		setInputState(InputState.Text);

	}// getEscapeNumber_m

	private String getEscapeValuesString() {
		int bufferSize = escapeBuffer.size();
		byte[] values = new byte[bufferSize];

		for (int i = 0; i < bufferSize; i++) {
			values[i] = escapeBuffer.remove();
		} // while
		return new String(values);
	}// getEscapeValuesString

	private int getEscapeValue() {
		if (escapeBuffer.size() < 1) {
			return -1;
		} else {
			return Integer.valueOf(getEscapeValuesString());
		} // if

	}// getEscapeValue

	////////////////////////////////////////////////////////
	@Override
	public void byteToCPU(Byte value) {
		dataToCPU.offer(value);
	}// byteToCPU

	@Override
	public void close() {
		appClose();
	}// close

	@Override
	public void setVisible(boolean state) {
		frameVT100.setVisible(state);
	}// setVisible

	@Override
	public boolean isVisible() {
		return frameVT100.isVisible();
	}// isVisible

	private void showCursorPosition() {
		String msg = String.format("Row: %4d, Col: %5d", screen.getRow(), screen.getColumn());
		lblCursorPosition.setText(msg);
	}// showCursorPosition

	private void doKeyboardIn(KeyEvent keyEvent) {
		internalBuffer.offer((byte) keyEvent.getKeyChar());
		showKeyChar(keyEvent.getKeyChar());
	}// doKeyboardIn

	// private void showInputState() {
	// lblState.setText(inputState.toString());
	// }// showInputState

	private void showInChar(char keyChar) {// From CPU
		String msg = String.format("In Char = %s     [0x%02X]", keyChar, (byte) keyChar);
		lblInChar.setText(msg);
	}// showStatus

	private void showKeyChar(char keyChar) {// From Keyboard
		String msg = String.format("KB Char = %s     [0x%02X]", keyChar, (byte) keyChar);
		lblKeyChar.setText(msg);
	}// showStatus

	private void showSetPropertiesDialog() {
		Preferences myPrefs = getMyPrefs();
		VT100properties dialogProperties = new VT100properties(frameVT100, getMyPrefs());

		if (dialogProperties.showDialog() == JOptionPane.OK_OPTION) {
			int oldScreenColumns = screenColumns;
			setProperties(myPrefs);
			if (oldScreenColumns != screenColumns) {
				setFrameSize(txtScreen, screenColumns);
				screen.makeNewScreen();
			} // if screenColumns changed
		} // if
		closeMyPrefs(myPrefs);
		dialogProperties.close();
		dialogProperties = null;
		return;
	}//show SetPropertiesDialog
	
	private void doSetPanelVisible(AbstractButton source) {
		boolean state = source.isSelected();
		switch(source.getActionCommand()) {
		case MNU_SETTINGS_LEDS:	
			panelLeds.setVisible(state);
			break;
		case MNU_SETTINGS_KEY_IN:
			panelKey.setVisible(state);
			break;
		case MNU_SETTINGS_FROM_CPU:
			panelInChar.setVisible(state);
			break;
		case MNU_SETTINGS_ROW_COLUMN:
			panelRowColumn.setVisible(state);
			break;
		case MNU_SETTINGS_STATE:
			panelState.setVisible(state);
			break;
			default:
				log.errorf("[VT100.doSetPanelVisible] error %s%n",source.getActionCommand());
		}//switch
	}//doSetPanelVisible
	
	private void doClearScreen() {
		screen.makeNewScreen();
	}//doClearScreen

	private void setProperties(Preferences myPrefs) {
		Font font = new Font(myPrefs.get("FontFamily", "Courier"), myPrefs.getInt("FontStyle", Font.PLAIN),
				myPrefs.getInt("FontSize", 18));
		txtScreen.setFont(font);

		txtScreen.setForeground(new Color(myPrefs.getInt("ColorFont", -13421773)));
		txtScreen.setBackground(new Color(myPrefs.getInt("ColorBackground", -4144960)));
		txtScreen.setCaretColor(new Color(myPrefs.getInt("ColorCaret", -65536)));

		screenColumns = myPrefs.getInt("Columns", 80);

		screen.setScreenColumns(myPrefs.getInt("Columns", 80));
		screen.setTruncate(myPrefs.getBoolean("ScreenTruncate", true));
		screen.setWrap(myPrefs.getBoolean("ScreenWrap", false));
		
		panelLeds.setVisible(myPrefs.getBoolean("PanelLeds", true));
		panelKey.setVisible(myPrefs.getBoolean("PanelKey", true));
		panelInChar.setVisible(myPrefs.getBoolean("PanelInChar", true));
		panelRowColumn.setVisible(myPrefs.getBoolean("PanelRowColumn", true));
		panelState.setVisible(myPrefs.getBoolean("PanelState", true));
		


	}// setProperties

	/**
	 * Create the application.
	 */
	public VT100(String name, Byte addressIn, Byte addressOut, Byte addressStatus) {
		super(name, addressIn, addressOut, addressStatus);
		initialize();
		appInit();
	}// Constructor

	private void appClose() {
		Preferences myPrefs = getMyPrefs();
		Dimension dim = frameVT100.getSize();
		myPrefs.putInt("Height", dim.height);
		myPrefs.putInt("Width", dim.width);
		Point point = frameVT100.getLocation();
		myPrefs.putInt("LocX", point.x);
		myPrefs.putInt("LocY", point.y);

		myPrefs.putInt("Columns", screenColumns);

		myPrefs.putInt("FontStyle", txtScreen.getFont().getStyle());
		myPrefs.putInt("FontSize", txtScreen.getFont().getSize());
		myPrefs.put("FontFamily", txtScreen.getFont().getFamily());

		myPrefs.putInt("ColorFont", txtScreen.getForeground().getRGB());
		myPrefs.putInt("ColorBackground", txtScreen.getBackground().getRGB());
		myPrefs.putInt("ColorCaret", txtScreen.getCaretColor().getRGB());

		myPrefs.putBoolean("ScreenTruncate", screen.getTruncate());
		myPrefs.putBoolean("ScreenWrap", screen.getWrap());

		myPrefs.putBoolean("PanelLeds", panelLeds.isVisible());
		myPrefs.putBoolean("PanelInChar", panelInChar.isVisible());
		myPrefs.putBoolean("PanelKey", panelKey.isVisible());
		myPrefs.putBoolean("PanelRowColumn", panelRowColumn.isVisible());
		myPrefs.putBoolean("PanelState", panelState.isVisible());
		closeMyPrefs(myPrefs);
	}// appClose

	private void appInit() {
		screen = new VT100Display(txtScreen);
		txtScreen.getCaret().setVisible(true);

		Preferences myPrefs = getMyPrefs();
		frameVT100.setSize(myPrefs.getInt("Width", 761), myPrefs.getInt("Height", 693));
		frameVT100.setLocation(myPrefs.getInt("LocX", 100), myPrefs.getInt("LocY", 100));

		setProperties(myPrefs);

		closeMyPrefs(myPrefs);
		// screen = txtScreen.getStyledDocument();
		mnuSettingsLeds.setSelected(panelLeds.isVisible());
		mnuSettingsKeyboardCharacter.setSelected(panelKey.isVisible());
		mnuSettingsFromCPU.setSelected(panelInChar.isVisible());
		mnuSettingsState.setSelected(panelState.isVisible());
		mnuSettingsRowColumn.setSelected(panelRowColumn.isVisible());

		setFrameSize(txtScreen, screenColumns);
		screen.makeNewScreen();
		// txtScreen.setFont(new Font("Courier New", Font.BOLD, 24));

		escapeBuffer.clear();
		internalBuffer.clear();

		rbLED1.setIcon(ledOFF);
		rbLED2.setIcon(ledOFF);
		rbLED3.setIcon(ledOFF);
		rbLED4.setIcon(ledOFF);
		setInputState(InputState.Text);
		showCursorPosition();

		frameVT100.setVisible(true);
		
		
//		frameVT100.setSize(900, 693);
//		frameVT100.setForeground(Color.RED);
//		frameVT100.setTitle("frameVT100.setSize(900, 693);");
		
	}// appInit

	private Preferences getMyPrefs() {
		Preferences pref = Preferences.userNodeForPackage(VT100.class).node(this.getClass().getSimpleName());
		// pref.addPreferenceChangeListener(adapterVT100);
		return pref;
	}// getMyPrefs

	private void closeMyPrefs(Preferences pref) {
		// pref.removePreferenceChangeListener(adapterVT100);
		pref = null;
	}// closeMyPrefs

	private void setInputState(InputState state) {
		if (state.equals(InputState.Text)) {
			escapeBuffer.clear();
		} // if

		inputState = state;
		lblState.setText(inputState.toString());
		showCursorPosition();
	}// setState

	private InputState getState() {
		return inputState;
	}// InputState

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frameVT100 = new JFrame();
		frameVT100.setTitle(screenTitle);
		// frameVT100.setSize(700, 600);
		frameVT100.setResizable(false);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		frameVT100.getContentPane().setLayout(gridBagLayout);
		panelMain = new JPanel();
		GridBagConstraints gbc_panelMain = new GridBagConstraints();
		gbc_panelMain.insets = new Insets(0, 0, 5, 0);
		gbc_panelMain.fill = GridBagConstraints.BOTH;
		gbc_panelMain.gridx = 0;
		gbc_panelMain.gridy = 0;
		frameVT100.getContentPane().add(panelMain, gbc_panelMain);
		GridBagLayout gbl_panelMain = new GridBagLayout();
		gbl_panelMain.columnWidths = new int[] { 0, 0 };
		gbl_panelMain.rowHeights = new int[] { 0, 0 };
		gbl_panelMain.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelMain.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelMain.setLayout(gbl_panelMain);

		txtScreen = new JTextPane();
		txtScreen.setEditable(false);
		txtScreen.setCaretColor(Color.RED);
		txtScreen.addKeyListener(adapterVT100);
		txtScreen.addMouseListener(adapterVT100);
		txtScreen.setBackground(Color.LIGHT_GRAY);
		GridBagConstraints gbc_txtScreen = new GridBagConstraints();
		gbc_txtScreen.fill = GridBagConstraints.BOTH;
		panelMain.add(txtScreen, gbc_txtScreen);

		panelStatus = new JPanel();
		panelStatus.setBackground(UIManager.getColor("Panel.background"));
		panelStatus.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelStatus = new GridBagConstraints();
		gbc_panelStatus.anchor = GridBagConstraints.WEST;
		gbc_panelStatus.gridx = 0;
		gbc_panelStatus.gridy = 1;
		frameVT100.getContentPane().add(panelStatus, gbc_panelStatus);
		GridBagLayout gbl_panelStatus = new GridBagLayout();
		gbl_panelStatus.columnWidths = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelStatus.rowHeights = new int[] { 18 };
		gbl_panelStatus.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				0.0 };
		gbl_panelStatus.rowWeights = new double[] { 0.0 };
		panelStatus.setLayout(gbl_panelStatus);

//		rigidArea = Box.createRigidArea(new Dimension(30, 15));
//		GridBagConstraints gbc_rigidArea = new GridBagConstraints();
//		gbc_rigidArea.insets = new Insets(0, 0, 0, 5);
//		gbc_rigidArea.gridx = 0;
//		gbc_rigidArea.gridy = 0;
//		panelStatus.add(rigidArea, gbc_rigidArea);

		panelLeds = new JPanel();
		panelLeds.setPreferredSize(new Dimension(190, 30));
		panelLeds.setMinimumSize(new Dimension(190, 30));
		panelLeds.setMaximumSize(new Dimension(190, 30));
		panelLeds.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelLeds = new GridBagConstraints();
		gbc_panelLeds.anchor = GridBagConstraints.WEST;
		gbc_panelLeds.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelLeds.insets = new Insets(0, 0, 0, 5);
		gbc_panelLeds.gridx = 1;
		gbc_panelLeds.gridy = 0;
		panelStatus.add(panelLeds, gbc_panelLeds);
		GridBagLayout gbl_panelLeds = new GridBagLayout();
		gbl_panelLeds.columnWidths = new int[] { 50, 45, 45, 45 };
		gbl_panelLeds.rowHeights = new int[] {18};
		gbl_panelLeds.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
		gbl_panelLeds.rowWeights = new double[] { 0.0 };
		panelLeds.setLayout(gbl_panelLeds);

		rbLED1 = new JRadioButton("1");
		GridBagConstraints gbc_rbLED1 = new GridBagConstraints();
		gbc_rbLED1.insets = new Insets(0, 0, 0, 5);
		gbc_rbLED1.gridx = 0;
		gbc_rbLED1.gridy = 0;
		panelLeds.add(rbLED1, gbc_rbLED1);

		rbLED2 = new JRadioButton("2");
		GridBagConstraints gbc_rbLED2 = new GridBagConstraints();
		gbc_rbLED2.insets = new Insets(0, 0, 0, 5);
		gbc_rbLED2.gridx = 1;
		gbc_rbLED2.gridy = 0;
		panelLeds.add(rbLED2, gbc_rbLED2);

		rbLED3 = new JRadioButton("3");
		GridBagConstraints gbc_rbLED3 = new GridBagConstraints();
		gbc_rbLED3.insets = new Insets(0, 0, 0, 5);
		gbc_rbLED3.gridx = 2;
		gbc_rbLED3.gridy = 0;
		panelLeds.add(rbLED3, gbc_rbLED3);

		rbLED4 = new JRadioButton("4");
		GridBagConstraints gbc_rbLED4 = new GridBagConstraints();
		gbc_rbLED4.gridx = 3;
		gbc_rbLED4.gridy = 0;
		panelLeds.add(rbLED4, gbc_rbLED4);

//		rigidArea_4 = Box.createRigidArea(new Dimension(20, 20));
//		GridBagConstraints gbc_rigidArea_4 = new GridBagConstraints();
//		gbc_rigidArea_4.insets = new Insets(0, 0, 0, 5);
//		gbc_rigidArea_4.gridx = 2;
//		gbc_rigidArea_4.gridy = 0;
//		panelStatus.add(rigidArea_4, gbc_rigidArea_4);

		panelKey = new JPanel();
		panelKey.setPreferredSize(new Dimension(110, 30));
		panelKey.setMinimumSize(new Dimension(110, 30));
		panelKey.setMaximumSize(new Dimension(110, 30));
		panelKey.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelKey = new GridBagConstraints();
		gbc_panelKey.anchor = GridBagConstraints.CENTER;
		gbc_panelKey.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelKey.insets = new Insets(0, 0, 0, 5);
		gbc_panelKey.gridx = 2;
		gbc_panelKey.gridy = 0;
		panelStatus.add(panelKey, gbc_panelKey);
		GridBagLayout gbl_panelKey = new GridBagLayout();
		gbl_panelKey.columnWidths = new int[] {20};
		gbl_panelKey.rowHeights = new int[] {18};
		gbl_panelKey.columnWeights = new double[] { 0.0};
		gbl_panelKey.rowWeights = new double[] { 0.0 };
		panelKey.setLayout(gbl_panelKey);

		lblKeyChar = new JLabel("KB Char =   [    ]");
		GridBagConstraints gbc_lblKeyChar = new GridBagConstraints();
		gbc_lblKeyChar.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblKeyChar.gridx = 1;
		gbc_lblKeyChar.gridy = 0;
		panelKey.add(lblKeyChar, gbc_lblKeyChar);

//		rigidArea_1 = Box.createRigidArea(new Dimension(30, 15));
//		GridBagConstraints gbc_rigidArea_1 = new GridBagConstraints();
//		gbc_rigidArea_1.insets = new Insets(0, 0, 0, 5);
//		gbc_rigidArea_1.gridx = 4;
//		gbc_rigidArea_1.gridy = 0;
//		panelStatus.add(rigidArea_1, gbc_rigidArea_1);
//
		panelInChar = new JPanel();
		panelInChar.setMaximumSize(new Dimension(110, 30));
		panelInChar.setMinimumSize(new Dimension(110, 30));
		panelInChar.setPreferredSize(new Dimension(110, 30));
		panelInChar.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelInChar = new GridBagConstraints();
		gbc_panelInChar.anchor = GridBagConstraints.WEST;
		gbc_panelInChar.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelInChar.insets = new Insets(0, 0, 0, 5);
		gbc_panelInChar.gridx = 3;
		gbc_panelInChar.gridy = 0;
		panelStatus.add(panelInChar, gbc_panelInChar);
		GridBagLayout gbl_panelInChar = new GridBagLayout();
		gbl_panelInChar.columnWidths = new int[] { 100 };
		gbl_panelInChar.rowHeights = new int[] {18};
		gbl_panelInChar.columnWeights = new double[] { 0.0 };
		gbl_panelInChar.rowWeights = new double[] { 0.0 };
		panelInChar.setLayout(gbl_panelInChar);

		lblInChar = new JLabel("");
		GridBagConstraints gbc_lblInChar = new GridBagConstraints();
		gbc_lblInChar.gridx = 0;
		gbc_lblInChar.gridy = 0;
		panelInChar.add(lblInChar, gbc_lblInChar);//

//		rigidArea_2 = Box.createRigidArea(new Dimension(30, 15));
//		GridBagConstraints gbc_rigidArea_2 = new GridBagConstraints();
//		gbc_rigidArea_2.insets = new Insets(0, 0, 0, 5);
//		gbc_rigidArea_2.gridx = 6;
//		gbc_rigidArea_2.gridy = 0;
//		panelStatus.add(rigidArea_2, gbc_rigidArea_2);

		panelRowColumn = new JPanel();
		panelRowColumn.setPreferredSize(new Dimension(110, 30));
		panelRowColumn.setMinimumSize(new Dimension(110, 30));
		panelRowColumn.setMaximumSize(new Dimension(110, 30));
		panelRowColumn.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelRowColumn = new GridBagConstraints();
		gbc_panelRowColumn.anchor = GridBagConstraints.WEST;
		gbc_panelRowColumn.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelRowColumn.insets = new Insets(0, 0, 0, 5);
		gbc_panelRowColumn.gridx = 4;
		gbc_panelRowColumn.gridy = 0;
		panelStatus.add(panelRowColumn, gbc_panelRowColumn);
		GridBagLayout gbl_panelRowColumn = new GridBagLayout();
		gbl_panelRowColumn.columnWidths = new int[] { 100 };
		gbl_panelRowColumn.rowHeights = new int[] {18};
		gbl_panelRowColumn.columnWeights = new double[] { 0.0 };
		gbl_panelRowColumn.rowWeights = new double[] { 0.0 };
		panelRowColumn.setLayout(gbl_panelRowColumn);

		lblCursorPosition = new JLabel("Row: rr,  Column: ccc");
		GridBagConstraints gbc_lblCursorPosition = new GridBagConstraints();
		gbc_lblCursorPosition.gridx = 0;
		gbc_lblCursorPosition.gridy = 0;
		panelRowColumn.add(lblCursorPosition, gbc_lblCursorPosition);

		panelState = new JPanel();
		panelState.setMaximumSize(new Dimension(110, 30));
		panelState.setMinimumSize(new Dimension(110, 30));
		panelState.setPreferredSize(new Dimension(110, 30));
		panelState.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelState = new GridBagConstraints();
		gbc_panelState.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelState.anchor = GridBagConstraints.WEST;
		gbc_panelState.insets = new Insets(0, 0, 0, 5);
		gbc_panelState.gridx = 5;
		gbc_panelState.gridy = 0;
		panelStatus.add(panelState, gbc_panelState);
		GridBagLayout gbl_panelState = new GridBagLayout();
		gbl_panelState.columnWidths = new int[] { 100 };
		gbl_panelState.rowHeights = new int[] {18};
		gbl_panelState.columnWeights = new double[] { 0.0 };
		gbl_panelState.rowWeights = new double[] { 0.0 };
		panelState.setLayout(gbl_panelState);

		lblState = new JLabel("Text");
		GridBagConstraints gbc_lblState = new GridBagConstraints();
		gbc_lblState.gridx = 0;
		gbc_lblState.gridy = 0;
		panelState.add(lblState, gbc_lblState);

		menuBar = new JMenuBar();
		frameVT100.setJMenuBar(menuBar);

		JMenu mnuMenu = new JMenu("Settings");
		menuBar.add(mnuMenu);

		JMenuItem mnuMenuProperties = new JMenuItem("Properties...");
		mnuMenuProperties.addActionListener(adapterVT100);
		mnuMenuProperties.setActionCommand(MNU_SETTINGS_PROPERTIES);
		mnuMenu.add(mnuMenuProperties);

		separator = new JSeparator();
		mnuMenu.add(separator);

		mnuSettingsLeds = new JRadioButtonMenuItem("LEDs");
		mnuSettingsLeds.setActionCommand(MNU_SETTINGS_LEDS);
		mnuSettingsLeds.addActionListener(adapterVT100);
		mnuMenu.add(mnuSettingsLeds);

		mnuSettingsKeyboardCharacter = new JRadioButtonMenuItem("Keyboard Character");
		mnuSettingsKeyboardCharacter.setActionCommand(MNU_SETTINGS_KEY_IN);
		mnuSettingsKeyboardCharacter.addActionListener(adapterVT100);
		mnuMenu.add(mnuSettingsKeyboardCharacter);

		mnuSettingsFromCPU = new JRadioButtonMenuItem("Byte From CPU");
		mnuSettingsFromCPU.setActionCommand(MNU_SETTINGS_FROM_CPU);
		mnuSettingsFromCPU.addActionListener(adapterVT100);
		mnuMenu.add(mnuSettingsFromCPU);

		mnuSettingsState = new JRadioButtonMenuItem("State");
		mnuSettingsState.setActionCommand(MNU_SETTINGS_STATE);
		mnuSettingsState.addActionListener(adapterVT100);
		mnuMenu.add(mnuSettingsState);

		mnuSettingsRowColumn = new JRadioButtonMenuItem("Cursor position");
		mnuSettingsRowColumn.setActionCommand(MNU_SETTINGS_ROW_COLUMN);
		mnuSettingsRowColumn.addActionListener(adapterVT100);
		mnuMenu.add(mnuSettingsRowColumn);
		
		horizontalGlue = Box.createHorizontalGlue();
		menuBar.add(horizontalGlue);
		
		btnClearScreen = new JButton("Clear Screen");
		btnClearScreen.setActionCommand(BTN_CLEAR_SCREEN);
		btnClearScreen.addActionListener(adapterVT100);
		menuBar.add(btnClearScreen);

		frameVT100.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				appClose();
			}// windowClosing
		});
		frameVT100.setVisible(true);

	}// initialize

	class AdapterVT100 implements KeyListener, ActionListener, MouseListener {// ,PreferenceChangeListener
		/* KeyListener */

		@Override
		public void keyTyped(KeyEvent keyEvent) {
			doKeyboardIn(keyEvent);
		}// keyTyped

		@Override
		public void keyPressed(KeyEvent keyEvent) {
		}// keyPressed

		@Override
		public void keyReleased(KeyEvent keyEvent) {
		}// keyReleased

		@Override

		/* ActionListener */
		public void actionPerformed(ActionEvent actionEvent) {
			AbstractButton source = (AbstractButton) actionEvent.getSource();
			String cmd = actionEvent.getActionCommand();
			switch (cmd) {
			case MNU_SETTINGS_PROPERTIES:
				showSetPropertiesDialog();
				break;
			case BTN_CLEAR_SCREEN:
				doClearScreen();
				break;
			case MNU_SETTINGS_LEDS:	
			case MNU_SETTINGS_KEY_IN:				
			case MNU_SETTINGS_FROM_CPU:				
			case MNU_SETTINGS_ROW_COLUMN:				
			case MNU_SETTINGS_STATE:
				doSetPanelVisible(source);
			default:
				log.errorf("bad actionEvent cmd : [%s]%n", cmd);
			}// switch
		}// actionPerformed

		/* KeyListener */

		@Override
		public void mouseClicked(MouseEvent mouseEvent) {
			if (mouseEvent.getSource().equals(txtScreen)) {
				txtScreen.setCaretPosition(screen.getPosition());
				txtScreen.getCaret().setVisible(true);
			} // if

		}// mouseClicked

		@Override
		public void mouseEntered(MouseEvent mouseEvent) {
		}// mouseEntered

		@Override
		public void mouseExited(MouseEvent mouseEvent) {
		}// mouseExited

		@Override
		public void mousePressed(MouseEvent mouseEvent) {
		}// mousePressed

		@Override
		public void mouseReleased(MouseEvent mouseEvent) {
		}// mouseReleased

		/* PreferenceChangeListener */

		// @Override
		// public void preferenceChange(PreferenceChangeEvent pce) {
		// log.infof("[PCE] Source = %s, Key = %s, New Value = %s%n",
		// pce.getSource().toString(), pce.getKey(),pce.getNewValue());
		//
		// }//preferenceChange

	}// class AdapterVT100
/* @formatter:off */
	public enum InputState {
		ESC_m, ESC_f, ESC_H,ESC_g, ESC_J, ESC_K, 
		Text, ESC_PART0, ESC_PART1,
		ESC_SemiColon, ESC_QMark,  ESC_NUMBER,
		ESC_Q1,ESC_Q3,ESC_Q4,ESC_Q5,ESC_Q7,ESC_Q,
		ESC_N0,ESC_N
	}// enum InputState
/* @formatter:on  */

	// --------------------------------------------------------------------------------------

	// Address is for CP/M COM device
	public static final Byte IN = (byte) 0X01;
	public static final Byte OUT = (byte) 0X01;
	public static final Byte STATUS = (byte) 0X02;
	public static final Byte STATUS_OUT_READY = (byte) 0b1000_0000; // MSB set

	private static final String MNU_SETTINGS_PROPERTIES = "mnuSettingsProperties";
	private static final String MNU_SETTINGS_LEDS = "mnuSettingsLEDs";
	private static final String MNU_SETTINGS_KEY_IN = "mnuSettingsKeyboardCharacter";
	private static final String MNU_SETTINGS_FROM_CPU = "mnuSettingsFromCPU";
	private static final String MNU_SETTINGS_ROW_COLUMN = "mnuSettingsRowColumns";
	private static final String MNU_SETTINGS_STATE = "mnuSettingsState";
	
	private static final String BTN_CLEAR_SCREEN = "btnClearScreen";

	private static final String SEMI_COLON = ";";

	private static final byte ASCII_BS = (byte) 0x08;// Backspace
	private static final byte ASCII_TAB = (byte) 0x09;// Tab
	private static final byte ASCII_LF = (byte) 0x0A;// Linefeed
	private static final byte ASCII_CR = (byte) 0x0D;// Carriage Return
	private static final byte ASCII_SPACE = (byte) 0x20;// Space
	private static final byte ASCII_DEL = (byte) 0x7F;// Delete

	private static final byte ASCII_ESC = (byte) 0x1B;// Escape
	private static final byte ASCII_SEMI_COLON = (byte) 0x3B;// ;
	private static final byte ASCII_QMARK = (byte) 0x3F;// ?
	private static final byte ASCII_f = (byte) 0x66;// f
	private static final byte ASCII_g = (byte) 0x67;// g
	private static final byte ASCII_h = (byte) 0x68;// h
	private static final byte ASCII_l = (byte) 0x6C;// l
	private static final byte ASCII_m = (byte) 0x6D;// m
	private static final byte ASCII_q = (byte) 0x71;// q
	private static final byte ASCII_A = (byte) 0x41;// A
	private static final byte ASCII_B = (byte) 0x42;// B
	private static final byte ASCII_C = (byte) 0x43;// C
	private static final byte ASCII_D = (byte) 0x44;// D
	private static final byte ASCII_H = (byte) 0x48;// H
	private static final byte ASCII_J = (byte) 0x4A;// J
	private static final byte ASCII_K = (byte) 0x4B;// K
	private static final byte ASCII_LEFT_PARENTHSIS = (byte) 0x5B; // [

	private static final byte ASCII_0 = (byte) 0x30;// 0
	private static final byte ASCII_1 = (byte) 0x31;// 1
	private static final byte ASCII_2 = (byte) 0x32;// 2
	private static final byte ASCII_3 = (byte) 0x33;// 3
	private static final byte ASCII_4 = (byte) 0x34;// 4
	private static final byte ASCII_5 = (byte) 0x35;// 5
	private static final byte ASCII_6 = (byte) 0x36;// 6
	private static final byte ASCII_7 = (byte) 0x37;// 7
	private static final byte ASCII_8 = (byte) 0x38;// 8
	private static final byte ASCII_9 = (byte) 0x39;// 9

	private JLabel lblKeyChar;
	private JMenuBar menuBar;
	private JPanel panelStatus;
	private JPanel panelLeds;
	private JPanel panelInChar;
	private JPanel panelState;
	private JPanel panelKey;
	private JPanel panelRowColumn;
	
	private JLabel lblInChar;
	private JLabel lblCursorPosition;
	private JLabel lblState;
	private JPanel panelMain;
	private JRadioButton rbLED1;
	private JRadioButton rbLED2;
	private JRadioButton rbLED3;
	private JRadioButton rbLED4;
	private JSeparator separator;
	private JRadioButtonMenuItem mnuSettingsLeds;
	private JRadioButtonMenuItem mnuSettingsKeyboardCharacter;
	private JRadioButtonMenuItem mnuSettingsFromCPU;
	private JRadioButtonMenuItem mnuSettingsState;
	private JRadioButtonMenuItem mnuSettingsRowColumn;
	private Component horizontalGlue;
	private JButton btnClearScreen;

}// class VT100
