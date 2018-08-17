package hardware;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

import codeSupport.AppLogger;
import disks.DiskControlUnit;
import disks.diskPanel.V_IF_DiskPanel;
import disks.utility.UpdateSystemDisk;
import hardware.View.V_IF_CCR;
import hardware.View.V_IF_IndexRegisters;
import hardware.View.V_IF_PrimaryRegisters;
import hardware.View.V_IF_ProgramRegisters;
import hardware.View.V_IF_SpecialRegisters;

public class Z80Machine {

	ApplicationAdapter applicationAdapter = new ApplicationAdapter();

	DiskControlUnit dcu = DiskControlUnit.getInstance();

	private AppLogger log = AppLogger.getInstance();
	private JPopupMenu popupLog;
	private AdapterLog logAdaper = new AdapterLog();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Z80Machine window = new Z80Machine();
					window.frameBase.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				} // try
			}// run
		});
	}// main

	// ---------------------------------------------------------

	private void doFileNew() {
		System.out.println("** [doFileNew] **");
		String diskPath = "C:\\Users\\admin\\VMdata\\Disks\\ZZZ.F3HD";
		File file = new File(diskPath);
		file.delete();
		try {
			file.createNewFile();
			UpdateSystemDisk.updateDisk(diskPath);
		} catch (IOException e) {
			// log.addError(" Did not create file :" + diskPath);
		} // try
	}// doFileNew

	private void doFileOpen() {
		System.out.println("** [doFileOpen] **");

	}// doFileOpen

	private void doFileSave() {
		System.out.println("** [doFileSave] **");

	}// doFileSave

	private void doFileSaveAs() {
		System.out.println("** [doFileSaveAs] **");

	}// doFileSaveAs

	private void doFilePrint() {
		System.out.println("** [doFilePrint] **");

	}// doFilePrint

	private void doFileExit() {
		appClose();
		System.exit(0);
	}// doFileExit

	private void doWindowToggle(String name) {
		Component target = null;
		switch (name) {
		case MNU_WINDOW_PRIMARY_REGISTERS:
			target = ifPrimaryRegisters;
			break;
		case MNU_WINDOW_PROGRAM_CONTROL:
			target = ifProgramRegisters;
			break;
		case MNU_WINDOW_INDEX_REGISTERS:
			target = ifIndexRegisters;
			break;
		case MNU_WINDOW_SPECIAL_REGISTERS:
			target = ifSpecialRegisters;
			break;
		case MNU_WINDOW_CONDITION_CODES:
			target = ifCCR;
			break;
		}// switch
		target.setVisible(!target.isVisible());
	}// doWindowToggle

	private void doResetAllRegisterDisplays() {
		ifPrimaryRegisters.setLocation(INSET_X, INSET_Y);
		ifPrimaryRegisters.setVisible(true);

		ifProgramRegisters.setLocation(getNextLocationY(ifPrimaryRegisters.getBounds()));
		ifProgramRegisters.setVisible(true);

		ifCCR.setLocation(getNextLocationX(ifProgramRegisters.getBounds()));
		ifCCR.setVisible(true);

		Point p = getNextLocationY(ifCCR.getBounds());
		p.x = INSET_X;
		ifIndexRegisters.setLocation(p);
//		ifIndexRegisters.setLocation(getNextLocationY(ifCCR.getBounds()));
		ifIndexRegisters.setVisible(true);

		ifSpecialRegisters.setLocation(getNextLocationX(ifIndexRegisters.getBounds()));
		ifSpecialRegisters.setVisible(true);
	}// resetAllRegisterDisplays

	private Point getNextLocationY(Rectangle bounds) {
		Point result = getNextLocation(bounds);
		result.x = bounds.x;
		return result;
	}// getNextLocationY

	private Point getNextLocationX(Rectangle bounds) {
		Point result = getNextLocation(bounds);
		result.y = bounds.y;
		return result;
	}// getNextLocationX

	private Point getNextLocation(Rectangle bounds) {
		int x = bounds.x;
		int y = bounds.y;
		int width = bounds.width;
		int height = bounds.height;

		Point result = new Point();
		result.x = x + width + INSET_X;
		result.y = y + height + INSET_Y;
		return result;
	}// getNextLocationY

	private void getInternalFrameLocations(Preferences myPrefs) {
		Point location = new Point();
		JInternalFrame[] internalFrames = desktopPane.getAllFrames();
		for (JInternalFrame internalFrame : internalFrames) {
			String key = internalFrame.getClass().getSimpleName();
			location.x = myPrefs.getInt(key + ".x", 0);
			location.y = myPrefs.getInt(key + ".y", 0);
			internalFrame.setLocation(location);
			internalFrame.setVisible(myPrefs.getBoolean(key + ".isVisible", true));
			try {
				internalFrame.setIcon(myPrefs.getBoolean(key + ".isIcon", false));
			} catch (PropertyVetoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // try
			// log.infof("x = %d, y = %d for frame %s%n",
			// location.x,location.y,internalFrame.getClass().getSimpleName());
		} // for
	}// getInternalFrameLocations

	private void saveInternalFrameLocations(Preferences myPrefs) { //
		Point location = new Point();
		JInternalFrame[] internalFrames = desktopPane.getAllFrames();
		for (JInternalFrame internalFrame : internalFrames) {
			String key = internalFrame.getClass().getSimpleName();
			location = internalFrame.getLocation();
			myPrefs.putInt(key + ".x", location.x);
			myPrefs.putInt(key + ".y", location.y);
			myPrefs.putBoolean(key + ".isIcon", internalFrame.isIcon());
			myPrefs.putBoolean(key + ".isVisible", internalFrame.isVisible());
			// log.infof("x = %d, y = %d for frame %s%n",
			// location.x,location.y,internalFrame.getClass().getSimpleName());
		} // for
	}// saveInternalFrameLocations

	private void doLogClear() {
		log.clear();
	}// doLogClear

	private void doLogPrint() {

		Font originalFont = txtLog.getFont();
		try {
			// textPane.setFont(new Font("Courier New", Font.PLAIN, 8));
			txtLog.setFont(originalFont.deriveFont(8.0f));
			MessageFormat header = new MessageFormat("Identic Log");
			MessageFormat footer = new MessageFormat(new Date().toString() + "           Page - {0}");
			txtLog.print(header, footer);
			// textPane.setFont(new Font("Courier New", Font.PLAIN, 14));
			txtLog.setFont(originalFont);
		} catch (PrinterException e) {
			e.printStackTrace();
		} // try

	}// doLogPrint

	////////////////////////////////////////////////////////////////////////////////////////
	private void appClose() {
		Preferences myPrefs = Preferences.userNodeForPackage(Z80Machine.class).node(this.getClass().getSimpleName());
		Dimension dim = frameBase.getSize();
		myPrefs.putInt("Height", dim.height);
		myPrefs.putInt("Width", dim.width);
		Point point = frameBase.getLocation();
		myPrefs.putInt("LocX", point.x);
		myPrefs.putInt("LocY", point.y);
		saveInternalFrameLocations(myPrefs);

		myPrefs = null;
	}// appClose

	private void appInit() {
		Preferences myPrefs = Preferences.userNodeForPackage(Z80Machine.class).node(this.getClass().getSimpleName());
		frameBase.setSize(myPrefs.getInt("Width", 761), myPrefs.getInt("Height", 693));
		frameBase.setLocation(myPrefs.getInt("LocX", 100), myPrefs.getInt("LocY", 100));
		getInternalFrameLocations(myPrefs);

		myPrefs = null;

		txtLog.setText(EMPTY_STRING);

		log.setDoc(txtLog.getStyledDocument());
		log.info("Starting....");

		dcu.setDisplay(ifDiskPanel);

	}// appInit

	public Z80Machine() {
		initialize();
		appInit();
	}// Constructor

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frameBase = new JFrame();
		frameBase.setTitle("Z80 Machine    0.0");
		frameBase.setBounds(100, 100, 450, 300);
		frameBase.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frameBase.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				appClose();
			}
		});
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 25, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		frameBase.getContentPane().setLayout(gridBagLayout);

		JToolBar toolBar = new JToolBar("still");
		toolBar.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		toolBar.setFloatable(false);
		GridBagConstraints gbc_toolBar = new GridBagConstraints();
		gbc_toolBar.anchor = GridBagConstraints.NORTHWEST;
		gbc_toolBar.insets = new Insets(0, 0, 5, 0);
		gbc_toolBar.gridx = 0;
		gbc_toolBar.gridy = 0;
		frameBase.getContentPane().add(toolBar, gbc_toolBar);

		JToggleButton b = new JToggleButton("");
		b.setBorder(null);
		b.setToolTipText("Run/Stop");
		b.setSelectedIcon(null);
		b.setIcon(new ImageIcon(Z80Machine.class.getResource("/com/sun/java/swing/plaf/windows/icons/Computer.gif")));
		b.setHorizontalAlignment(SwingConstants.LEADING);
		toolBar.add(b);
		JToolBar.Separator s1 = new JToolBar.Separator(new Dimension(20, 20));
		toolBar.add(s1);

		Icon iconC = UIManager.getIcon("FileView.fileIcon");
		JButton c = new JButton("");
		c.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (rightPanel.isVisible()){
					
				}else {
					
				}//if visible
			}
		});
		c.setBorder(null);
		c.setToolTipText("Step");

		c.setIcon(new ImageIcon(Z80Machine.class.getResource("/javax/swing/plaf/metal/icons/ocean/collapsed.gif")));
		c.setHorizontalAlignment(SwingConstants.LEADING);
		toolBar.add(c);

		mainPanel = new JSplitPane();
		mainPanel.setDividerLocation(480);
		GridBagConstraints gbc_mainPanel = new GridBagConstraints();
		gbc_mainPanel.insets = new Insets(0, 0, 5, 0);
		gbc_mainPanel.fill = GridBagConstraints.BOTH;
		gbc_mainPanel.gridx = 0;
		gbc_mainPanel.gridy = 1;
		frameBase.getContentPane().add(mainPanel, gbc_mainPanel);

		JPanel leftPanel = new JPanel();
		mainPanel.setLeftComponent(leftPanel);
		GridBagLayout gbl_leftPanel = new GridBagLayout();
		gbl_leftPanel.columnWidths = new int[] { 0, 0 };
		gbl_leftPanel.rowHeights = new int[] { 0, 0, 0 };
		gbl_leftPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_leftPanel.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		leftPanel.setLayout(gbl_leftPanel);

		JPanel leftTopPanel = new JPanel();
		leftTopPanel.setPreferredSize(new Dimension(550, 280));
		leftTopPanel.setMinimumSize(new Dimension(550, 280));
		leftTopPanel.setMaximumSize(new Dimension(550, 280));
		leftTopPanel.setBounds(new Rectangle(0, 0, 550, 280));
		leftTopPanel.setLayout(null);
		GridBagConstraints gbc_leftTopPanel = new GridBagConstraints();
		gbc_leftTopPanel.insets = new Insets(0, 0, 5, 0);
		gbc_leftTopPanel.fill = GridBagConstraints.BOTH;
		gbc_leftTopPanel.gridx = 0;
		gbc_leftTopPanel.gridy = 0;
		leftPanel.add(leftTopPanel, gbc_leftTopPanel);

		JPanel runStopPanel = new JPanel();
		runStopPanel.setBounds(0, 0, 140, 280);
		runStopPanel.setBorder(null);
		runStopPanel.setLayout(null);
		leftTopPanel.add(runStopPanel);

		JToggleButton tbRunStop = new JToggleButton("");
		tbRunStop.setBounds(37, 52, 65, 65);
		tbRunStop.setBackground(SystemColor.control);
		tbRunStop.setBorder(null);
		tbRunStop.setSelectedIcon(
				new ImageIcon("C:\\Users\\admin\\git\\VM\\VM\\resources\\Button-Turn-Off-icon-64.png"));
		tbRunStop.setToolTipText("Run");
		tbRunStop.setIcon(new ImageIcon("C:\\Users\\admin\\git\\VM\\VM\\resources\\Button-Turn-On-icon-64.png"));
		runStopPanel.add(tbRunStop);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setBounds(0, 0, 0, 0);
		runStopPanel.add(horizontalStrut);

		JButton btnStep = new JButton("");
		btnStep.setBounds(45, 160, 50, 50);
		btnStep.setBackground(SystemColor.control);
		btnStep.setBorder(null);
		btnStep.setIcon(new ImageIcon("C:\\Users\\admin\\git\\VM\\VM\\resources\\Button-Next-icon-48.png"));
		runStopPanel.add(btnStep);

		JSpinner spinnerStepCount = new JSpinner();
		spinnerStepCount.setModel(new SpinnerNumberModel(1, 1, 65535, 1));
		spinnerStepCount.setBounds(51, 227, 37, 20);
		runStopPanel.add(spinnerStepCount);

		JPanel disksPanel = new JPanel();
		disksPanel.setBounds(150, 0, 330, 280);
		leftTopPanel.add(disksPanel);
		disksPanel.setLayout(null);

		ifDiskPanel = new V_IF_DiskPanel();
		ifDiskPanel.setIconifiable(false);
		ifDiskPanel.setBounds(0, 11, 328, 256);
		disksPanel.add(ifDiskPanel);
		ifDiskPanel.setVisible(true);

		desktopPane = new JDesktopPane();
		desktopPane.setLayout(null);
		GridBagConstraints gbc_desktopPane = new GridBagConstraints();
		gbc_desktopPane.fill = GridBagConstraints.BOTH;
		gbc_desktopPane.gridx = 0;
		gbc_desktopPane.gridy = 1;
		leftPanel.add(desktopPane, gbc_desktopPane);

		ifPrimaryRegisters = new V_IF_PrimaryRegisters();
		desktopPane.add(ifPrimaryRegisters);
		ifPrimaryRegisters.setVisible(true);

		ifProgramRegisters = new V_IF_ProgramRegisters();
		desktopPane.add(ifProgramRegisters);
		ifProgramRegisters.setVisible(true);

		ifIndexRegisters = new V_IF_IndexRegisters();
		desktopPane.add(ifIndexRegisters);
		ifIndexRegisters.setVisible(true);

		ifSpecialRegisters = new V_IF_SpecialRegisters();
		desktopPane.add(ifSpecialRegisters);
		ifSpecialRegisters.setVisible(true);

		ifCCR = new V_IF_CCR();
		desktopPane.add(ifCCR);
		ifCCR.setVisible(true);

		rightPanel = new JPanel();
		mainPanel.setRightComponent(rightPanel);
		GridBagLayout gbl_rightPanel = new GridBagLayout();
		gbl_rightPanel.columnWidths = new int[] { 0, 0 };
		gbl_rightPanel.rowHeights = new int[] { 0, 0 };
		gbl_rightPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_rightPanel.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		rightPanel.setLayout(gbl_rightPanel);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		rightPanel.add(tabbedPane, gbc_tabbedPane);

		JPanel tabAppLog = new JPanel();
		tabbedPane.addTab("Application Log", null, tabAppLog, null);
		GridBagLayout gbl_tabAppLog = new GridBagLayout();
		gbl_tabAppLog.columnWidths = new int[] { 0, 0 };
		gbl_tabAppLog.rowHeights = new int[] { 0, 0 };
		gbl_tabAppLog.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_tabAppLog.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		tabAppLog.setLayout(gbl_tabAppLog);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		tabAppLog.add(scrollPane, gbc_scrollPane);

		txtLog = new JTextPane();
		scrollPane.setViewportView(txtLog);

		popupLog = new JPopupMenu();
		addPopup(txtLog, popupLog);

		JMenuItem popupLogClear = new JMenuItem("Clear Log");
		popupLogClear.setName(PUM_LOG_CLEAR);
		popupLogClear.addActionListener(logAdaper);
		popupLog.add(popupLogClear);

		JSeparator separator = new JSeparator();
		popupLog.add(separator);

		JMenuItem popupLogPrint = new JMenuItem("Print Log");
		popupLogPrint.setName(PUM_LOG_PRINT);
		popupLogPrint.addActionListener(logAdaper);
		popupLog.add(popupLogPrint);

		JLabel lblNewLabel = new JLabel("Application Log");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setForeground(new Color(30, 144, 255));
		lblNewLabel.setFont(new Font("Times New Roman", Font.BOLD | Font.ITALIC, 14));
		scrollPane.setColumnHeaderView(lblNewLabel);

		JPanel panelStatus = new JPanel();
		panelStatus.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_panelStatus = new GridBagConstraints();
		gbc_panelStatus.anchor = GridBagConstraints.WEST;
		gbc_panelStatus.fill = GridBagConstraints.VERTICAL;
		gbc_panelStatus.gridx = 0;
		gbc_panelStatus.gridy = 2;
		frameBase.getContentPane().add(panelStatus, gbc_panelStatus);

		JMenuBar menuBar = new JMenuBar();
		frameBase.setJMenuBar(menuBar);

		JMenu mnuFile = new JMenu("File");
		menuBar.add(mnuFile);

		JMenuItem mnuFileNew = new JMenuItem("New");
		mnuFileNew.setName(MNU_FILE_NEW);
		mnuFileNew.addActionListener(applicationAdapter);
		mnuFile.add(mnuFileNew);

		JMenuItem mnuFileOpen = new JMenuItem("Open...");
		mnuFileOpen.setName(MNU_FILE_OPEN);
		mnuFileOpen.addActionListener(applicationAdapter);
		mnuFile.add(mnuFileOpen);

		JSeparator separator99 = new JSeparator();
		mnuFile.add(separator99);

		JMenuItem mnuFileSave = new JMenuItem("Save...");
		mnuFileSave.setName(MNU_FILE_SAVE);
		mnuFileSave.addActionListener(applicationAdapter);
		mnuFile.add(mnuFileSave);

		JMenuItem mnuFileSaveAs = new JMenuItem("Save As...");
		mnuFileSaveAs.setName(MNU_FILE_SAVE_AS);
		mnuFileSaveAs.addActionListener(applicationAdapter);
		mnuFile.add(mnuFileSaveAs);

		JSeparator separator_2 = new JSeparator();
		mnuFile.add(separator_2);

		JMenuItem mnuFilePrint = new JMenuItem("Print...");
		mnuFilePrint.setName(MNU_FILE_PRINT);
		mnuFilePrint.addActionListener(applicationAdapter);
		mnuFile.add(mnuFilePrint);

		JSeparator separator_1 = new JSeparator();
		mnuFile.add(separator_1);

		JMenuItem mnuFileExit = new JMenuItem("Exit");
		mnuFileExit.setName(MNU_FILE_EXIT);
		mnuFileExit.addActionListener(applicationAdapter);
		mnuFile.add(mnuFileExit);

		JMenu mnuWindow = new JMenu("Windows");
		menuBar.add(mnuWindow);

		mnuWindowPrimaryRegisters = new JMenuItem("Primary Registers");
		mnuWindowPrimaryRegisters.addActionListener(applicationAdapter);
		mnuWindowPrimaryRegisters.setName(MNU_WINDOW_PRIMARY_REGISTERS);
		mnuWindow.add(mnuWindowPrimaryRegisters);

		mnuWindowProgramControl = new JMenuItem("Program Control");
		mnuWindowProgramControl.addActionListener(applicationAdapter);
		mnuWindowProgramControl.setName(MNU_WINDOW_PROGRAM_CONTROL);
		mnuWindow.add(mnuWindowProgramControl);

		mnuWindowConditionCodes = new JMenuItem("Condition Codes");
		mnuWindowConditionCodes.addActionListener(applicationAdapter);
		mnuWindowConditionCodes.setName(MNU_WINDOW_CONDITION_CODES);
		mnuWindow.add(mnuWindowConditionCodes);

		mnuWindowIndexRegisters = new JMenuItem("Index Registers");
		mnuWindowIndexRegisters.addActionListener(applicationAdapter);
		mnuWindowIndexRegisters.setName(MNU_WINDOW_INDEX_REGISTERS);
		mnuWindow.add(mnuWindowIndexRegisters);

		mnuWindowSpecialRegisters = new JMenuItem("Special Registers");
		mnuWindowSpecialRegisters.setName(MNU_WINDOW_SPECIAL_REGISTERS);
		mnuWindowSpecialRegisters.addActionListener(applicationAdapter);
		mnuWindow.add(mnuWindowSpecialRegisters);

		JSeparator separator_3 = new JSeparator();
		mnuWindow.add(separator_3);

		mnuWindowsReset = new JMenuItem("Reset All Register Displays");
		mnuWindowsReset.setName(MNU_WINDOW_RESET);
		mnuWindowsReset.addActionListener(applicationAdapter);
		mnuWindow.add(mnuWindowsReset);

	}// initialize

	static final String EMPTY_STRING = "";
	private static final String PUM_LOG_PRINT = "popupLogPrint";
	private static final String PUM_LOG_CLEAR = "popupLogClear";

	//////////////////////////////////////////////////////////////////////////
	private JFrame frameBase;
	private JSplitPane mainPanel;

	//////////////////////////////////////////////////////////////////////////
	private static final String MNU_FILE_NEW = "mnuFileNew";
	private static final String MNU_FILE_OPEN = "mnuFileOpen";
	private static final String MNU_FILE_SAVE = "mnuFileSave";
	private static final String MNU_FILE_SAVE_AS = "mnuFileSaveAs";
	private static final String MNU_FILE_PRINT = "mnuFilePrint";
	private static final String MNU_FILE_EXIT = "mnuFileExit";

	private static final String MNU_WINDOW_PRIMARY_REGISTERS = "mnuWindowsPrimaryRegisters";
	private static final String MNU_WINDOW_PROGRAM_CONTROL = "mnuWindowsProgramControl";
	private static final String MNU_WINDOW_INDEX_REGISTERS = "mnuWindowsIndexRegisters";
	private static final String MNU_WINDOW_SPECIAL_REGISTERS = "mnuWindowsSpecialRegisters";
	private static final String MNU_WINDOW_CONDITION_CODES = "mnuWindowsConditionCodes";
	private static final String MNU_WINDOW_RESET = "mnuWindowsReset";
	
	private static final int INSET_X = 1;
	private static final int INSET_Y = 1;

	private JPanel rightPanel;
	private JDesktopPane desktopPane;
	private V_IF_DiskPanel ifDiskPanel;
	private JTextPane txtLog;
	private JMenuItem mnuWindowPrimaryRegisters;
	private JMenuItem mnuWindowProgramControl;
	private JMenuItem mnuWindowConditionCodes;
	private JMenuItem mnuWindowIndexRegisters;
	private JMenuItem mnuWindowSpecialRegisters;
	private V_IF_PrimaryRegisters ifPrimaryRegisters;
	private V_IF_ProgramRegisters ifProgramRegisters;
	private V_IF_IndexRegisters ifIndexRegisters;
	private V_IF_SpecialRegisters ifSpecialRegisters;
	private V_IF_CCR ifCCR;
	private JMenuItem mnuWindowsReset;
	//////////////////////////////////////////////////////////////////////////

	class ApplicationAdapter implements ActionListener {// , ListSelectionListener
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			Component source = (Component) actionEvent.getSource();
			String name = source.getName();
			switch (name) {
			case MNU_FILE_NEW:
				doFileNew();
				break;
			case MNU_FILE_OPEN:
				doFileOpen();
				break;
			case MNU_FILE_SAVE:
				doFileSave();
				break;
			case MNU_FILE_SAVE_AS:
				doFileSaveAs();
				break;
			case MNU_FILE_PRINT:
				doFilePrint();
				break;
			case MNU_FILE_EXIT:
				doFileExit();
				break;
			case MNU_WINDOW_PRIMARY_REGISTERS:
			case MNU_WINDOW_PROGRAM_CONTROL:
			case MNU_WINDOW_INDEX_REGISTERS:
			case MNU_WINDOW_SPECIAL_REGISTERS:
			case MNU_WINDOW_CONDITION_CODES:
				doWindowToggle(name);
				break;
			case MNU_WINDOW_RESET:
				doResetAllRegisterDisplays();
				break;
			}// switch
		}// actionPerformed
	}// class AdapterAction

	//////////////////////////////////////////////////////////////////////////

	class AdapterLog implements ActionListener {// , ListSelectionListener
		/* ActionListener */
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String name = ((Component) actionEvent.getSource()).getName();
			switch (name) {
			case PUM_LOG_PRINT:
				doLogPrint();
				break;
			case PUM_LOG_CLEAR:
				doLogClear();
				break;
			}// switch
		}// actionPerformed

	}// class AdapterAction

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				} // if popup Trigger
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}// addPopup
}// class GUItemplate