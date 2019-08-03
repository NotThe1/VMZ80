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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.BevelBorder;
import javax.swing.text.JTextComponent;

import codeSupport.AppLogger;
import codeSupport.Z80;
import ioSystem.DeviceZ80;

public class VT100 extends DeviceZ80 {

	private JFrame frameVT100;
	private JTextPane txtScreen = new JTextPane();
	private VT100Display screen; 

	private Queue<Byte> internalBuffer = new LinkedList<Byte>();
	private Queue<Byte> escapeBuffer = new LinkedList<Byte>();
	private AdapterVT100 adapterVT100 = new AdapterVT100();
	private AppLogger log = AppLogger.getInstance();

	private int screenColumns;
	private int lineLength; // accounts for System.lineSeparator
//	private boolean screenWrap;
//	private boolean screenTruncate;
	private boolean escapeMode;

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

	private void setEscapeMode(InputState inState) {
		inputState = inState;
		showInputState();
		escapeBuffer.clear();
	}// setEscapeMode

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

		switch (inputState) {
		case Text:
			if (value == ASCII_ESC) {// Escape 0x1B
				setEscapeMode(InputState.ESC_0);
			} else {
				screen.asciiInFromCPU((byte) (value & 0x7F));
				showCursorPosition();
			} // if
			break;
		default:
			log.errorf("InputState error: %s%n", inputState.toString());
		}// switch
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


	private void showCursorPosition() {
		String msg = String.format("Row; %d,Column: %d", screen.getRow(), screen.getColumn());
		lblCursorPosition.setText(msg);
	}// showCursorPosition

	private void doKeyboardIn(KeyEvent keyEvent) {
		internalBuffer.offer((byte) keyEvent.getKeyChar());
		showKeyChar(keyEvent.getKeyChar());
	}// doKeyboardIn

	private void showInputState() {
		lblState.setText(inputState.toString());
	}// showInputState

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
	}//

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

		closeMyPrefs(myPrefs);
	}// appClose

	private void appInit() {
		 screen = new VT100Display(txtScreen);

		
		Preferences myPrefs = getMyPrefs();
		frameVT100.setSize(myPrefs.getInt("Width", 761), myPrefs.getInt("Height", 693));
		frameVT100.setLocation(myPrefs.getInt("LocX", 100), myPrefs.getInt("LocY", 100));

		setProperties(myPrefs);

		closeMyPrefs(myPrefs);
		// screen = txtScreen.getStyledDocument();

		setFrameSize(txtScreen, screenColumns);
		screen.makeNewScreen();
		// txtScreen.setFont(new Font("Courier New", Font.BOLD, 24));

		escapeMode = false;
		escapeBuffer.clear();
		internalBuffer.clear();

		inputState = InputState.Text;
		showInputState();
		showCursorPosition();
		
		// frameVT100.setSize(761, 693);

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

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frameVT100 = new JFrame();
		frameVT100.setTitle("VT100              Rev 0.0.B");
		frameVT100.setSize(700, 600);
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
		txtScreen.setBackground(Color.LIGHT_GRAY);
		// scrollPane.setViewportView(txtScreen);
		GridBagConstraints gbc_txtScreen = new GridBagConstraints();
		gbc_txtScreen.fill = GridBagConstraints.BOTH;
		panelMain.add(txtScreen, gbc_txtScreen);

		panelStatus = new JPanel();
		panelStatus.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelStatus = new GridBagConstraints();
		gbc_panelStatus.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelStatus.gridx = 0;
		gbc_panelStatus.gridy = 1;
		frameVT100.getContentPane().add(panelStatus, gbc_panelStatus);
		GridBagLayout gbl_panelStatus = new GridBagLayout();
		gbl_panelStatus.columnWidths = new int[] { 0, 150, 0, 150, 0, 0, 0, 0, 0 };
		gbl_panelStatus.rowHeights = new int[] { 18, 0 };
		gbl_panelStatus.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelStatus.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panelStatus.setLayout(gbl_panelStatus);

		rigidArea = Box.createRigidArea(new Dimension(30, 15));
		GridBagConstraints gbc_rigidArea = new GridBagConstraints();
		gbc_rigidArea.insets = new Insets(0, 0, 0, 5);
		gbc_rigidArea.gridx = 0;
		gbc_rigidArea.gridy = 0;
		panelStatus.add(rigidArea, gbc_rigidArea);

		panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 0, 5);
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 0;
		panelStatus.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 100 };
		gbl_panel.rowHeights = new int[] { 15 };
		gbl_panel.columnWeights = new double[] { 0.0 };
		gbl_panel.rowWeights = new double[] { 0.0 };
		panel.setLayout(gbl_panel);

		lblKeyChar = new JLabel("");
		GridBagConstraints gbc_lblKeyChar = new GridBagConstraints();
		gbc_lblKeyChar.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblKeyChar.anchor = GridBagConstraints.NORTH;
		gbc_lblKeyChar.gridx = 0;
		gbc_lblKeyChar.gridy = 0;
		panel.add(lblKeyChar, gbc_lblKeyChar);

		rigidArea_1 = Box.createRigidArea(new Dimension(30, 15));
		GridBagConstraints gbc_rigidArea_1 = new GridBagConstraints();
		gbc_rigidArea_1.insets = new Insets(0, 0, 0, 5);
		gbc_rigidArea_1.gridx = 2;
		gbc_rigidArea_1.gridy = 0;
		panelStatus.add(rigidArea_1, gbc_rigidArea_1);

		panel_1 = new JPanel();
		panel_1.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 0, 5);
		gbc_panel_1.gridx = 3;
		gbc_panel_1.gridy = 0;
		panelStatus.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 100 };
		gbl_panel_1.rowHeights = new int[] { 15 };
		gbl_panel_1.columnWeights = new double[] { 0.0 };
		gbl_panel_1.rowWeights = new double[] { 0.0 };
		panel_1.setLayout(gbl_panel_1);

		lblInChar = new JLabel("");
		GridBagConstraints gbc_lblInChar = new GridBagConstraints();
		gbc_lblInChar.gridx = 0;
		gbc_lblInChar.gridy = 0;
		panel_1.add(lblInChar, gbc_lblInChar);

		rigidArea_2 = Box.createRigidArea(new Dimension(30, 15));
		GridBagConstraints gbc_rigidArea_2 = new GridBagConstraints();
		gbc_rigidArea_2.insets = new Insets(0, 0, 0, 5);
		gbc_rigidArea_2.gridx = 4;
		gbc_rigidArea_2.gridy = 0;
		panelStatus.add(rigidArea_2, gbc_rigidArea_2);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.anchor = GridBagConstraints.WEST;
		gbc_panel_2.insets = new Insets(0, 0, 0, 5);
		gbc_panel_2.gridx = 5;
		gbc_panel_2.gridy = 0;
		panelStatus.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 200 };
		gbl_panel_2.rowHeights = new int[] { 15 };
		gbl_panel_2.columnWeights = new double[] { 0.0 };
		gbl_panel_2.rowWeights = new double[] { 0.0 };
		panel_2.setLayout(gbl_panel_2);

		lblCursorPosition = new JLabel("Row: rr,  Column: ccc");
		GridBagConstraints gbc_lblCursorPosition = new GridBagConstraints();
		gbc_lblCursorPosition.gridx = 0;
		gbc_lblCursorPosition.gridy = 0;
		panel_2.add(lblCursorPosition, gbc_lblCursorPosition);

		rigidArea_3 = Box.createRigidArea(new Dimension(30, 15));
		GridBagConstraints gbc_rigidArea_3 = new GridBagConstraints();
		gbc_rigidArea_3.insets = new Insets(0, 0, 0, 5);
		gbc_rigidArea_3.gridx = 6;
		gbc_rigidArea_3.gridy = 0;
		panelStatus.add(rigidArea_3, gbc_rigidArea_3);

		panel_3 = new JPanel();
		panel_3.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_3.gridx = 7;
		gbc_panel_3.gridy = 0;
		panelStatus.add(panel_3, gbc_panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[] { 100 };
		gbl_panel_3.rowHeights = new int[] { 15 };
		gbl_panel_3.columnWeights = new double[] { 0.0 };
		gbl_panel_3.rowWeights = new double[] { 0.0 };
		panel_3.setLayout(gbl_panel_3);

		lblState = new JLabel("Text");
		GridBagConstraints gbc_lblState = new GridBagConstraints();
		gbc_lblState.gridx = 0;
		gbc_lblState.gridy = 0;
		panel_3.add(lblState, gbc_lblState);

		menuBar = new JMenuBar();
		frameVT100.setJMenuBar(menuBar);

		JMenu mnuMenu = new JMenu("Settings");
		menuBar.add(mnuMenu);

		JMenuItem mnuMenuProperties = new JMenuItem("Properties...");
		mnuMenuProperties.addActionListener(adapterVT100);
		mnuMenuProperties.setActionCommand(MNU_PROPERTIES);
		mnuMenu.add(mnuMenuProperties);

		mnNewMenu = new JMenu("New menu");
		menuBar.add(mnNewMenu);

		mntmNewMenuItem = new JMenuItem("make screen");
		mntmNewMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				screen.makeNewScreen();
			}
		});
		mnNewMenu.add(mntmNewMenuItem);

		mntmFillscreen = new JMenuItem("FillScreen");
		mntmFillscreen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				 final int SCREEN_ROWS = 24;

				String line132 = "01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
				String thisLine = line132.substring(0, screenColumns);
				StringBuilder sb = new StringBuilder(lineLength * SCREEN_ROWS);
				for (int i = 0; i < SCREEN_ROWS; i++) {
					sb.append(thisLine + System.lineSeparator());
				} // for

				try {
					screen.remove(0, screen.getLength());
					screen.insertString(0, sb.toString(), null);
				} catch (Exception e) {
					log.error("Failed to makeNewScreen");
				} // try
				
				


			}
		});
		mnNewMenu.add(mntmFillscreen);

		frameVT100.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				appClose();
			}// windowClosing
		});
		frameVT100.setVisible(true);

	}// initialize

	class AdapterVT100 implements KeyListener, ActionListener {// ,PreferenceChangeListener
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

		// @Override
		// public void preferenceChange(PreferenceChangeEvent pce) {
		// log.infof("[PCE] Source = %s, Key = %s, New Value = %s%n",
		// pce.getSource().toString(), pce.getKey(),pce.getNewValue());
		//
		// }//preferenceChange

	}// class AdapterVT100

	public enum InputState {
		Text, ESC_0, ESC_1
	}// enum InputState

	// --------------------------------------------------------------------------------------

	// Address is for CP/M COM device
	public static final Byte IN = (byte) 0X01;
	public static final Byte OUT = (byte) 0X01;
	public static final Byte STATUS = (byte) 0X02;
	public static final Byte STATUS_OUT_READY = (byte) 0b1000_0000; // MSB set

	private static final String MNU_PROPERTIES = "mnuProperties";

	private static final byte ASCII_ESC = (byte) 0x1B;// Escape

	private JLabel lblKeyChar;
	private JMenuBar menuBar;
	private JPanel panelStatus;
	private JPanel panel;
	private JPanel panel_1;
	private JPanel panel_3;
	private JLabel lblState;
	private Component rigidArea;
	private Component rigidArea_1;
	private Component rigidArea_2;
	private Component rigidArea_3;
	private JLabel lblInChar;
	private JLabel lblCursorPosition;
	private JMenu mnNewMenu;
	private JMenuItem mntmNewMenuItem;
	private JMenuItem mntmFillscreen;
	private JPanel panelMain;

}// class VT100
