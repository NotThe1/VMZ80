package ioSystem.terminals;

import java.awt.Color;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import codeSupport.AppLogger;
import codeSupport.Z80;
import ioSystem.DeviceZ80;

public class VT100 extends DeviceZ80 {

	private JFrame frameVT100;
	private Document screen;
	private Queue<Byte> internalBuffer = new LinkedList<Byte>();
	private Queue<Byte> escapeBuffer = new LinkedList<Byte>();
	private AdapterVT100 adapterVT100 = new AdapterVT100();
	private AppLogger log = AppLogger.getInstance();
	private int screenColumns;

	private boolean escapeMode;
	// /**
	// * Launch the application.
	// */
	// public static void main(String[] args) {
	// EventQueue.invokeLater(new Runnable() {
	// public void run() {
	// try {
	// VT100 window = new VT100("vt100", IN, OUT, STATUS);
	// window.frameVT100.setVisible(true);
	// } catch (Exception e) {
	// e.printStackTrace();
	// } // try
	// }// run
	// });
	// }// main

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
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // try
			while (internalBuffer.size() != 0) {
				dataToCPU.offer(internalBuffer.poll());
			} // while data
		} // while - true }// run
	}// run

	private void asciiInFromCPU(byte value) {
		if (value == ESC) {
			setEscapeMode(true);
			return;
		} // if escape char
		appendToDoc(screen, Byte.toString(value));
	}// asciiInput

	private void escapeInFromCPU(byte value) {
		escapeBuffer.add(value);

	}// escapeInput

	private void setEscapeMode(boolean state) {
		escapeMode = state;
		escapeBuffer.clear();
	}// setEscapeMode

	private void fillScreen(int columns) {
		clearDoc(screen);

		setColumns(columns);
		// StringBuilder sb = new StringBuilder();
		for (int i = 2; i < SCREEN_ROWS - 1; i++) {
			appendToDoc(screen, Integer.toString(i) + System.lineSeparator());
		} // for
		setColumns(columns);
	}// fillScreen

	private void setColumns(int columns) {
		StringBuilder sb = new StringBuilder();
		int bias = 0;
		for (int i = 0; i < columns; i++) {
			if (i % 10 != 0) {
				sb.append(SPACE);
			} else {
				sb.append(Integer.toString((i - bias) / 10));
			} // if
			bias = i >= 99 ? 100 : 0;
		} // for
		sb.append(System.lineSeparator());

		for (int i = 0; i < columns; i++) {
			sb.append(Integer.toString(i % 10));
		} // for
		sb.append(System.lineSeparator());

		appendToDoc(screen, sb.toString());
	}// setColumns

	private void setScreenSize(JTextComponent component, int columns) {
		Insets insetsContentPane = frameVT100.getContentPane().getInsets();
		Insets insetsFrame = frameVT100.getInsets();
		Insets insets = component.getMargin();
		int extraW = insets.left + insets.right + insetsContentPane.left + insetsContentPane.right + insetsFrame.left
				+ insetsFrame.right;
		int extraH = insets.top + insets.bottom + insetsContentPane.top + insetsContentPane.bottom + insetsFrame.top
				+ insetsFrame.bottom;
		extraH += menuBar.getHeight() + toolBar.getHeight() + panelStatus.getHeight();
		int w = calcScreenWidth(component, columns) + extraW;
		int h = calcScreenHeight(component) + extraH;
		frameVT100.setSize(w, h);
	}// setScreenSize

	private int calcScreenWidth(JComponent component, int columns) {
		char[] data = new char[columns];
		for (int i = 0; i < columns; i++) {
			data[i] = 'X';
		} // for

		Font font = component.getFont();
		return component.getFontMetrics(font).charsWidth(data, 0, columns);
	}// calcScreenWidth

	private int calcScreenHeight(JComponent component) {
		Font font = component.getFont();
		return (int) ((SCREEN_ROWS + 1.3) * component.getFontMetrics(font).getHeight());
	}// calcScreenHeight

	@Override
	public void byteFromCPU(Byte value) {
		if (!escapeMode) {
			asciiInFromCPU(value);
		} else {
			escapeInFromCPU(value);
		} // if

		// appendToDoc(screen, Byte.toString(value));
	}// byteFromCPU

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

	private void doKeyboardIn(KeyEvent keyEvent) {
		internalBuffer.offer((byte) keyEvent.getKeyChar());
		showStatus(keyEvent.getKeyChar());
	}// doKeyboardIn

	private void showStatus(char keyChar) {
		String msg = String.format("Last Char = %s     [0x%02X]", keyChar, (byte) keyChar);
		lblKeyChar.setText(msg);
	}// showStatus

	private void appendToDoc(Document doc, String textToAppend) {
		appendToDoc(doc, textToAppend, null);
	}// appendToDocASM

	private void appendToDoc(Document doc, String textToAppend, AttributeSet attributeSet) {
		try {
			doc.insertString(doc.getLength(), textToAppend, attributeSet);
		} catch (BadLocationException e) {
			log.errorf("Failed to append text: %s %n", textToAppend);
			e.printStackTrace();
		} // try
	}// appendToDocASM

	private void clearDoc(Document doc) {
		try {
			doc.remove(0, doc.getLength());
		} catch (BadLocationException e) {
			log.errorf("Failed to clear screen: " + e.getMessage());
		} // try
	}// clearDoc

	private void showSetPropertiesDialog() {
		Preferences myPrefs = getMyPrefs();
		VT100properties dialogProperties = new VT100properties(frameVT100, getMyPrefs());

		if (dialogProperties.showDialog() == JOptionPane.OK_OPTION) {
			setProperties( myPrefs);
			setScreenSize(txtScreen,  screenColumns);
		} // if
		closeMyPrefs(myPrefs);
		dialogProperties.close();
		dialogProperties = null;
		return;
	}//

	private void setProperties(Preferences myPrefs) {
		Font font = new Font(myPrefs.get("FontFamily", "Courier"), myPrefs.getInt("FontStyle", Font.PLAIN),
				myPrefs.getInt("FontSize", 18));
		txtScreen.setFont(font);
		screenColumns = myPrefs.getInt("Columns", 80);

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

		// myPrefs.putBoolean("TruncateColumns", truncateColumns);
		// myPrefs.putInt("MaxColumn", maxColumn);

		// myPrefs.putBoolean("Extended", mnuBehaviorExtend.isSelected());
		// myPrefs.putBoolean("Wrap", mnuBehaviorWrap.isSelected());
		// myPrefs.putBoolean("Truncate", mnuBehaviorTruncate.isSelected());

		// myPrefs.putInt("CaretColor", textScreen.getCaretColor().getRGB());
		// myPrefs.putInt("BackgroundColor", textScreen.getBackground().getRGB());
		// myPrefs.putInt("ForegroundColor", textScreen.getForeground().getRGB());

		// myPrefs.put("FontFamily", textScreen.getFont().getFamily());
		// myPrefs.putInt("FontStyle", textScreen.getFont().getStyle());
		// myPrefs.putInt("FontSize", textScreen.getFont().getSize());

		closeMyPrefs(myPrefs);
	}// appClose

	private void appInit() {
		Preferences myPrefs = getMyPrefs();
		frameVT100.setSize(myPrefs.getInt("Width", 761), myPrefs.getInt("Height", 693));
		frameVT100.setLocation(myPrefs.getInt("LocX", 100), myPrefs.getInt("LocY", 100));

		setProperties(myPrefs);
		// truncateColumns = myPrefs.getBoolean("TruncateColumns", false);
		// maxColumn = myPrefs.getInt("MaxColumn", 80);

		// mnuBehaviorExtend.setSelected(myPrefs.getBoolean("Extended", true));
		// mnuBehaviorWrap.setSelected(myPrefs.getBoolean("Wrap", false));
		// mnuBehaviorTruncate.setSelected(myPrefs.getBoolean("Truncate", false));
		// setupScreen(myPrefs, textScreen);
		closeMyPrefs(myPrefs);

		escapeMode = false;
		escapeBuffer.clear();
		internalBuffer.clear();

//		txtScreen.setFont(new Font("Courier New", Font.BOLD, 24));
		screen = txtScreen.getDocument();
		clearDoc(screen);

		appendToDoc(screen, "Starting...\n");

		frameVT100.setVisible(true);

		testStuff();
	}// appInit

	private Preferences getMyPrefs() {
		Preferences pref = Preferences.userNodeForPackage(VT100.class).node(this.getClass().getSimpleName());
//		pref.addPreferenceChangeListener(adapterVT100);
		return pref;
	}// getMyPrefs
	
	private void closeMyPrefs(Preferences pref) {
//		pref.removePreferenceChangeListener(adapterVT100);
		pref = null;
	}//closeMyPrefs

	private void testStuff() {
		String fmt = "Columns = %d, Screen width: %d%n";
		log.infof(String.format(fmt, 80, calcScreenWidth(txtScreen, 80)));
		log.infof(String.format(fmt, 132, calcScreenWidth(txtScreen, 132)));

		char[] data = new char[80];
		for (int i = 0; i < 80; i++) {
			data[i] = 'X';
		} // for
			// appendToDoc(screen, new String(data));

		log.info("Character Height: " + calcScreenHeight(txtScreen) + System.lineSeparator());
		// calcScreenHeight
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frameVT100 = new JFrame();
		// frameVT100.setBounds(100, 100, 450, 300);

		frameVT100.setTitle("VT100              Rev 0.0.A");
		frameVT100.setResizable(false);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		frameVT100.getContentPane().setLayout(gridBagLayout);

		toolBar = new JToolBar();
		toolBar.setBackground(Color.GRAY);
		GridBagConstraints gbc_toolBar = new GridBagConstraints();
		gbc_toolBar.fill = GridBagConstraints.VERTICAL;
		gbc_toolBar.insets = new Insets(0, 0, 5, 0);
		gbc_toolBar.gridx = 0;
		gbc_toolBar.gridy = 0;
		frameVT100.getContentPane().add(toolBar, gbc_toolBar);

		JButton btnRats = new JButton("RATS");
		btnRats.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				screenColumns = screenColumns == 132 ? 80 : 132;

				setScreenSize(txtScreen, screenColumns);
				fillScreen(screenColumns);

			}
		});

		toolBar.add(btnRats);
		JPanel panelMain = new JPanel();
		GridBagConstraints gbc_panelMain = new GridBagConstraints();
		gbc_panelMain.insets = new Insets(0, 0, 5, 0);
		gbc_panelMain.fill = GridBagConstraints.BOTH;
		gbc_panelMain.gridx = 0;
		gbc_panelMain.gridy = 1;
		frameVT100.getContentPane().add(panelMain, gbc_panelMain);
		GridBagLayout gbl_panelMain = new GridBagLayout();
		gbl_panelMain.columnWidths = new int[] { 0, 0 };
		gbl_panelMain.rowHeights = new int[] { 0, 0 };
		gbl_panelMain.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelMain.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelMain.setLayout(gbl_panelMain);

		txtScreen = new JTextPane();
		txtScreen.addKeyListener(adapterVT100);
		txtScreen.setBackground(Color.LIGHT_GRAY);
		// scrollPane.setViewportView(txtScreen);
		GridBagConstraints gbc_txtScreen = new GridBagConstraints();
		gbc_txtScreen.fill = GridBagConstraints.BOTH;
		panelMain.add(txtScreen, gbc_txtScreen);

		panelStatus = new JPanel();
		GridBagConstraints gbc_panelStatus = new GridBagConstraints();
		gbc_panelStatus.fill = GridBagConstraints.BOTH;
		gbc_panelStatus.gridx = 0;
		gbc_panelStatus.gridy = 2;
		frameVT100.getContentPane().add(panelStatus, gbc_panelStatus);
		GridBagLayout gbl_panelStatus = new GridBagLayout();
		gbl_panelStatus.columnWidths = new int[] { 0, 0 };
		gbl_panelStatus.rowHeights = new int[] { 0, 0 };
		gbl_panelStatus.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelStatus.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelStatus.setLayout(gbl_panelStatus);

		lblKeyChar = new JLabel("<KeyChar>");
		GridBagConstraints gbc_lblKeyChar = new GridBagConstraints();
		gbc_lblKeyChar.anchor = GridBagConstraints.NORTH;
		gbc_lblKeyChar.gridx = 0;
		gbc_lblKeyChar.gridy = 0;
		panelStatus.add(lblKeyChar, gbc_lblKeyChar);

		menuBar = new JMenuBar();
		frameVT100.setJMenuBar(menuBar);

		JMenu mnuMenu = new JMenu("Settings");
		menuBar.add(mnuMenu);

		JMenuItem mnuMenuProperties = new JMenuItem("Properties...");
		mnuMenuProperties.addActionListener(adapterVT100);
		mnuMenuProperties.setActionCommand(MNU_PROPERTIES);
		mnuMenu.add(mnuMenuProperties);

		frameVT100.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				appClose();
			}// windowClosing
		});

	}// initialize

	class AdapterVT100 implements KeyListener, ActionListener {//,PreferenceChangeListener
		/* KeyListener */

		@Override
		public void keyTyped(KeyEvent keyEvent) {
			doKeyboardIn(keyEvent);
			// char c = keyEvent.getKeyChar();
			// int kc = keyEvent.getKeyCode();
			// int ekc = keyEvent.getExtendedKeyCode();
			// String fmt = "typed - getKeyChar(): %s (%02X),getKeyCode(): %02X,getExtendedKeyCode(): %02X%n";
			// log.infof(fmt, c,(byte)c,kc,ekc);
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
			String cmd = actionEvent.getActionCommand();
			switch (cmd) {
			case MNU_PROPERTIES:
				showSetPropertiesDialog();
				break;
			default:
				log.errorf("bad actionEvent cmd : [%s]%n", cmd);
			}// switch
		}// actionPerformed

		/* PreferenceChangeListener */

//		@Override
//		public void preferenceChange(PreferenceChangeEvent pce) {
//			log.infof("[PCE] Source = %s, Key = %s, New Value = %s%n",
//					pce.getSource().toString(), pce.getKey(),pce.getNewValue());
//			
//		}//preferenceChange

	}// class AdapterVT100

	// --------------------------------------------------------------------------------------

	// Address is for CP/M COM device
	public static final Byte IN = (byte) 0X01;
	public static final Byte OUT = (byte) 0X01;
	public static final Byte STATUS = (byte) 0X02;
	public static final Byte STATUS_OUT_READY = (byte) 0b1000_0000; // MSB set

	private static final String MNU_PROPERTIES = "mnuProperties";

	private static final byte ESC = (byte) 0x1B;
	private static final String SPACE = " ";
	private static final int SCREEN_ROWS = 24;

	private JTextPane txtScreen = new JTextPane();
	private JLabel lblKeyChar;
	private JMenuBar menuBar;
	private JToolBar toolBar;
	private JPanel panelStatus;

}// class VT100
