package hardware;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Observable;
import java.util.Observer;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;

import codeSupport.AppLogger;
import disks.DiskControlUnit;
import disks.diskPanel.V_IF_DiskPanel;
import disks.utility.DiskUtility;
import disks.utility.MakeNewDisk;
import disks.utility.UpdateSystemDisk;
import hardware.View.TabDialog;
import hardware.View.V_IF_CCR;
import hardware.View.V_IF_IndexRegisters;
import hardware.View.V_IF_PrimaryRegisters;
import hardware.View.V_IF_ProgramRegisters;
import hardware.View.V_IF_SpecialRegisters;
import ioSystem.IOController;
import memory.Core;
import memory.Core.Trap;
import memory.CpuBuss;
import memory.MemoryLoaderFromFile;
import memory.MemoryTrapEvent;
import utilities.filePicker.FilePicker;
import utilities.hdNumberBox.HDNumberValueChangeEvent;
import utilities.hdNumberBox.HDNumberValueChangeListener;

public class Z80Machine {

	ApplicationAdapter applicationAdapter = new ApplicationAdapter();

	CentralProcessingUnit cpu = CentralProcessingUnit.getInstance();
	DiskControlUnit dcu = DiskControlUnit.getInstance();
	IOController ioc = IOController.getInstance();

	private AppLogger log = AppLogger.getInstance();
	private TabDialog tabDialog = new TabDialog();
	DiskUtility du;

	Thread t_cpu = new Thread(cpu);
	// private List<Component> frontPanels;

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

	private void loadROM() {
		InputStream in = this.getClass().getResourceAsStream("/workingOS/ROM.mem");
		// InputStream in = this.getClass().getResourceAsStream("/Z80code/ROM.mem");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		MemoryLoaderFromFile.loadMemoryImage(reader);
	}// loadROM

	private void doStep() {
		CentralProcessingUnit.setError(ErrorStatus.NONE);
		CentralProcessingUnit.setRunning(true);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				int stepCount = (int) spinnerStepCount.getValue();
				for (int i = 0; i < stepCount; i++) {
					if (!cpu.startInstruction()) {
						break;
					} // if
				} // for step count
				CentralProcessingUnit.setRunning(false);
				updateDisplaysMaster();
			}// run
		});// EventQueue.invokeLater

	}// doStep

	private void doRunStop() {
		////////////////////////////////////////////////////////////////
		if (tbRunStop.isSelected()) {
			tbRunStop.setToolTipText(BUTTON_STOP_TIP);
			CentralProcessingUnit.setError(ErrorStatus.NONE);
			CentralProcessingUnit.setRunning(true);
			System.out.println("actionPerformed: doRun");
			Thread t = new Thread(cpu);
			t.start();
		} else {
			tbRunStop.setToolTipText(BUTTON_RUN_TIP);
			System.out.println("actionPerformed: doStop");
			CentralProcessingUnit.setError(ErrorStatus.HALT_INSTRUCTION);
			CentralProcessingUnit.setRunning(false);
			// CentralProcessingUnit.setError(ErrorStatus.NONE);
			updateDisplaysMaster();
		} // if

		setDisplaysEnabled(!tbRunStop.isSelected());
		btnBoot.setEnabled(!tbRunStop.isSelected());
	}// doRunStop

	// ----------------------------------------------------------

	private void doFileNew() {
		// System.out.println("** [doFileNew] **");
		// String diskPath = "C:\\Users\\admin\\VMdata\\Disks\\ZZZ.F3HD";
		// File file = new File(diskPath);
		// file.delete();
		// try {
		// file.createNewFile();
		// UpdateSystemDisk.updateDisk(diskPath);
		// } catch (IOException e) {
		// log.error(" Did not create file :" + diskPath);
		// } // try
	}// doFileNew

	private void doFileLoadMemory() {
//		JFileChooser filePicker = FilePicker.getMemories();
//		// JFileChooser filePicker = FilePicker.getMemoryPicker();
//		if (filePicker.showOpenDialog(frameBase) != JFileChooser.OPEN_DIALOG) {
//			log.info("abandoned File Load Memory");
//			return;
//		} // if
//		for (File file : filePicker.getSelectedFiles()) {
//			log.infof("File chosen is %s%n", file.toString());
//			MemoryLoaderFromFile.loadMemoryImage(file);
//			// MemoryLoaderFromFile.loadMemoryImage(new File(filePicker.getSelectedFile().getAbsolutePath()));
//		} // for each file
//		updateDisplaysMaster();
		
		
		JFileChooser filePicker = new JFileChooser("C:\\Users\\admin\\git\\VMZ80\\VMZ80\\resources\\workingOS");
		if (filePicker.showOpenDialog(frameBase) != JFileChooser.OPEN_DIALOG) {
			log.info("abandoned File Load Memory");
			return;
		} // if
		MemoryLoaderFromFile.loadMemoryImage(filePicker.getSelectedFile());
		updateDisplaysMaster();
	}// doFileOpen

	private void doFileSaveMemory() {
		JFileChooser filePicker = FilePicker.getMemories();
		// JFileChooser filePicker = FilePicker.getMemoryPicker();
		if (filePicker.showOpenDialog(frameBase) != JFileChooser.OPEN_DIALOG) {
			log.info("abandoned File Load Memory");
			return;
		} // if
		final String DEFAULT_TYPE = "MEM";
		final String DEFAULT_FILE_TYPE = "." + DEFAULT_TYPE;
		String selectedFilePath = filePicker.getSelectedFile().getAbsolutePath();
		String saveFilePath;
		Pattern memoryFileTypes = Pattern.compile("(?i)\\.(HEX|MEM)$");
		Matcher fileTypeMatcher = memoryFileTypes.matcher(selectedFilePath);
		String fileType = "XXX";
		if (fileTypeMatcher.find()) {// type is either MEM or HEX
			saveFilePath = selectedFilePath;
			fileType = fileTypeMatcher.group(1);
		} else {
			Pattern fileTypePattern = Pattern.compile("\\.([^.]+$)");
			fileTypeMatcher=fileTypePattern.matcher(selectedFilePath);
			if (fileTypeMatcher.find()) {// has other file type
				saveFilePath = fileTypeMatcher.replaceFirst(DEFAULT_FILE_TYPE);
				fileType = DEFAULT_TYPE;
			}else {// has no file type
				saveFilePath = selectedFilePath + DEFAULT_FILE_TYPE;
				fileType = DEFAULT_TYPE;
			}//inner if
		} // if
	
		System.out.printf("[Z80Machine.doFileSaveMemory]fileType: %s, saveFilePath: %s%n", fileType,saveFilePath);

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

	//////////////////////////////////////////////////

	private void doDiskUtility() {
		if (du == null) {
			du = new DiskUtility();
		} // if only want one running copy
		du.setVisible(true);
	}// doDiskUtility

	private void doDiskNew() {
		MakeNewDisk.makeNewDisk(frameBase);
	}// doDiskNew

	//////////////////////////////////////////////////

	private void updateDisplaysMaster() {
		tabDialog.updateDisplay();
		ifPrimaryRegisters.updateDisplay();
		ifProgramRegisters.updateDisplay();
		ifIndexRegisters.updateDisplay();
		ifSpecialRegisters.updateDisplay();
		ifCCR.updateDisplay();
	}// updateDisplaysMaster

	private void doDeviceToggle(String name) {
		String deviceName;
		switch (name) {
		case MNU_WINDOW_LST:
			deviceName = "lst";
			break;
		case MNU_WINDOW_TTY:
			deviceName = "tty";
			break;
		default:
			log.warnf("unknown device: %s for toggle%n", name);
			return;
		}// switch
		ioc.setVisible(deviceName, !ioc.isVisible(deviceName));
	}// doDeviceToggle

	private void doWindowToggle(String name) {
		Component target = null;
		switch (name) {
		case MNU_WINDOW_Z80_SUPPORT:
			target = tabDialog;
			break;
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
		if (target != null) {
			target.setVisible(!target.isVisible());
		} // if null
	}// doWindowToggle

	///////////////////////////////////////

	private void doResetAllRegisterDisplays() {
		int dpWidth = desktopPane.getWidth();
		int prWidth = ifPrimaryRegisters.getWidth();
		int leftPosition = INSET_X;
		if (dpWidth > prWidth) {
			leftPosition = (int) ((dpWidth - prWidth) / 2);
		} // if
		ifPrimaryRegisters.setLocation(leftPosition, INSET_Y);
		ifPrimaryRegisters.setVisible(true);

		ifProgramRegisters.setLocation(getNextLocationY(ifPrimaryRegisters.getBounds()));
		ifProgramRegisters.setVisible(true);

		ifCCR.setLocation(getNextLocationX(ifProgramRegisters.getBounds()));
		ifCCR.setVisible(true);

		Point p = getNextLocationY(ifCCR.getBounds());
		p.x = leftPosition;
		ifIndexRegisters.setLocation(p);
		ifIndexRegisters.setVisible(true);

		ifSpecialRegisters.setLocation(getNextLocationX(ifIndexRegisters.getBounds()));
		ifSpecialRegisters.setVisible(true);

		try {
			ifPrimaryRegisters.setIcon(false);
			ifProgramRegisters.setIcon(false);
			ifCCR.setIcon(false);
			ifIndexRegisters.setIcon(false);
			ifSpecialRegisters.setIcon(false);
		} catch (PropertyVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

	private void restoreTabDialogState(Preferences myPrefs) {
		Rectangle tabDialogBounds = new Rectangle();
		tabDialogBounds.x = myPrefs.getInt("tabDialog.x", 0);
		tabDialogBounds.y = myPrefs.getInt("tabDialog.y", 100);
		tabDialogBounds.height = myPrefs.getInt("tabDialog.height", 650);
		tabDialogBounds.width = myPrefs.getInt("tabDialog.width", 725);
		tabDialog.setBounds(tabDialogBounds);
		tabDialog.setVisible(myPrefs.getBoolean("Visible", true));
	}// getTabDialogState

	private void saveTabDialogState(Preferences myPrefs) {
		Rectangle tabDialogBounds = tabDialog.getBounds();
		myPrefs.putInt("tabDialog.x", tabDialogBounds.x);
		myPrefs.putInt("tabDialog.y", tabDialogBounds.y);
		myPrefs.putInt("tabDialog.height", tabDialogBounds.height);
		myPrefs.putInt("tabDialog.width", tabDialogBounds.width);
		myPrefs.putBoolean("Visible", tabDialog.isVisible());
	}// getTabDialogState

	private void restoreInternalFrameLocations(Preferences myPrefs) {
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
		} // for
	}// getInternalFrameLocations

	private void saveInternalFrameLocations(Preferences myPrefs) { //
		// Point location = new Point();
		JInternalFrame[] internalFrames = desktopPane.getAllFrames();
		for (JInternalFrame internalFrame : internalFrames) {
			String key = internalFrame.getClass().getSimpleName();
			Point location = internalFrame.getLocation();
			myPrefs.putInt(key + ".x", location.x);
			myPrefs.putInt(key + ".y", location.y);
			myPrefs.putBoolean(key + ".isIcon", internalFrame.isIcon());
			myPrefs.putBoolean(key + ".isVisible", internalFrame.isVisible());
		} // for
	}// saveInternalFrameLocations

	private void setDisplaysEnabled(boolean state) {
		ifDiskPanel.setEnabled(state);
		for (JInternalFrame internalFrame : desktopPane.getAllFrames()) {
			internalFrame.setEnabled(state);
		} // for frames
	}// setDisplaysEnabled

	private void doBoot() {
		int numberOfdrives0 = dcu.getMaxNumberOfDrives();
		Core.getInstance().initialize();
		loadROM();
		WorkingRegisterSet.getInstance().initialize();
		tbRunStop.setSelected(true);
		int numberOfdrives1 = dcu.getMaxNumberOfDrives();
		;
		doRunStop();
	}// boot
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
		saveTabDialogState(myPrefs);

		myPrefs.put("VisibleDevices", ioc.getVisibleDevices());
		myPrefs = null;

		dcu.close();
		ioc.close();
	}// appClose

	private void appInit() {
		loadROM();

		// tabDialog = new TabDialog();
		tabDialog.setVisible(true);

		Preferences myPrefs = Preferences.userNodeForPackage(Z80Machine.class).node(this.getClass().getSimpleName());
		Rectangle frameBounds = new Rectangle();
		frameBounds.x = myPrefs.getInt("LocX", 100);
		frameBounds.y = myPrefs.getInt("LocY", 100);
		frameBounds.height = myPrefs.getInt("Height", 848);
		frameBounds.width = myPrefs.getInt("Width", 848);
		frameBase.setBounds(frameBounds);
		restoreInternalFrameLocations(myPrefs);
		restoreTabDialogState(myPrefs);

		ioc.setVisibleDevices(myPrefs.get("VisibleDevices", ""));

		myPrefs = null;

		dcu.setDisplay(ifDiskPanel);
		updateDisplaysMaster();
		du = null;

		ifProgramRegisters.addHDNumberValueChangedListener(applicationAdapter);
		CpuBuss.getInstance().addObserver(applicationAdapter);

		log.addTimeStamp("Starting....");
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
		frameBase.setTitle("Z80 Machine    A.0.2");
		frameBase.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frameBase.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				appClose();
			}
		});
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
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

		btnBoot = new JToggleButton("");
		btnBoot.setName(BUTTON_BOOT);
		btnBoot.addActionListener(applicationAdapter);
		btnBoot.setBorder(null);
		btnBoot.setToolTipText("Boot");
		btnBoot.setSelectedIcon(null);
		btnBoot.setIcon(
				new ImageIcon(Z80Machine.class.getResource("/com/sun/java/swing/plaf/windows/icons/Computer.gif")));
		btnBoot.setHorizontalAlignment(SwingConstants.LEADING);
		toolBar.add(btnBoot);

		Component verticalStrut = Box.createVerticalStrut(20);
		toolBar.add(verticalStrut);

		JButton btnTest = new JButton("Update Disk A");
		btnTest.setToolTipText("C:\\Users\\admin\\Z80Work\\Disks\\A.F3HD");
		btnTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				UpdateSystemDisk.updateDisk("C:\\Users\\admin\\Z80Work\\Disks\\A.F3HD");
			}//
		});
		toolBar.add(btnTest);

		JPanel panelMain = new JPanel();
		GridBagConstraints gbc_panelMain = new GridBagConstraints();
		gbc_panelMain.insets = new Insets(0, 0, 5, 0);
		gbc_panelMain.fill = GridBagConstraints.BOTH;
		gbc_panelMain.gridx = 0;
		gbc_panelMain.gridy = 1;
		frameBase.getContentPane().add(panelMain, gbc_panelMain);
		GridBagLayout gbl_panelMain = new GridBagLayout();
		gbl_panelMain.columnWidths = new int[] { 400, 0 };
		gbl_panelMain.rowHeights = new int[] { 0, 0 };
		gbl_panelMain.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelMain.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelMain.setLayout(gbl_panelMain);

		JPanel leftPanel = new JPanel();
		leftPanel.setBorder(null);
		GridBagConstraints gbc_leftPanel = new GridBagConstraints();
		gbc_leftPanel.fill = GridBagConstraints.VERTICAL;
		gbc_leftPanel.anchor = GridBagConstraints.WEST;
		gbc_leftPanel.gridx = 0;
		gbc_leftPanel.gridy = 0;
		panelMain.add(leftPanel, gbc_leftPanel);
		GridBagLayout gbl_leftPanel = new GridBagLayout();
		gbl_leftPanel.columnWidths = new int[] { 480, 0 };
		gbl_leftPanel.rowHeights = new int[] { 280, 400, 0 };
		gbl_leftPanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_leftPanel.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		leftPanel.setLayout(gbl_leftPanel);

		leftTopPanel = new JPanel();
		leftTopPanel.setPreferredSize(new Dimension(480, 280));
		leftTopPanel.setMinimumSize(new Dimension(480, 280));
		leftTopPanel.setMaximumSize(new Dimension(500, 280));
		leftTopPanel.setBounds(new Rectangle(0, 0, 550, 280));
		leftTopPanel.setLayout(null);
		GridBagConstraints gbc_leftTopPanel = new GridBagConstraints();
		gbc_leftTopPanel.insets = new Insets(0, 0, 5, 0);
		gbc_leftTopPanel.fill = GridBagConstraints.BOTH;
		gbc_leftTopPanel.gridx = 0;
		gbc_leftTopPanel.gridy = 0;
		leftPanel.add(leftTopPanel, gbc_leftTopPanel);

		JPanel runStopPanel = new JPanel();
		runStopPanel.setBounds(0, 0, 100, 280);
		runStopPanel.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		runStopPanel.setLayout(null);
		leftTopPanel.add(runStopPanel);

		tbRunStop = new JToggleButton("");
		tbRunStop.addActionListener(applicationAdapter);
		tbRunStop.setName(BUTTON_RUN_STOP);
		tbRunStop.setBounds(17, 52, 65, 65);
		tbRunStop.setBackground(SystemColor.control);
		tbRunStop.setBorder(null);
		tbRunStop.setSelectedIcon(
				new ImageIcon("C:\\Users\\admin\\git\\VM\\VM\\resources\\Button-Turn-Off-icon-64.png"));
		tbRunStop.setToolTipText(BUTTON_RUN_TIP);
		tbRunStop.setIcon(new ImageIcon("C:\\Users\\admin\\git\\VM\\VM\\resources\\Button-Turn-On-icon-64.png"));
		runStopPanel.add(tbRunStop);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setBounds(0, 0, 0, 0);
		runStopPanel.add(horizontalStrut);

		JButton btnStep = new JButton("");
		btnStep.addActionListener(applicationAdapter);
		btnStep.setName(BUTTON_STEP);
		btnStep.setBounds(25, 160, 50, 50);
		btnStep.setBackground(SystemColor.control);
		btnStep.setBorder(null);
		btnStep.setToolTipText("Step thru the instructions");
		btnStep.setIcon(new ImageIcon("C:\\Users\\admin\\git\\VM\\VM\\resources\\Button-Next-icon-48.png"));
		runStopPanel.add(btnStep);

		spinnerStepCount = new JSpinner();
		spinnerStepCount.setToolTipText("Number of Instructions to step thru");
		spinnerStepCount.setModel(new SpinnerNumberModel(1, 1, 65535, 1));
		spinnerStepCount.setBounds(31, 227, 37, 20);
		runStopPanel.add(spinnerStepCount);

		disksPanel = new JPanel();
		disksPanel.setBounds(125, 0, 330, 280);
		leftTopPanel.add(disksPanel);
		disksPanel.setLayout(null);

		ifDiskPanel = new V_IF_DiskPanel();
		ifDiskPanel.setIconifiable(false);
		ifDiskPanel.setBounds(0, 11, 328, 256);
		disksPanel.add(ifDiskPanel);
		ifDiskPanel.setVisible(true);

		desktopPane = new JDesktopPane();
		desktopPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		desktopPane.setPreferredSize(new Dimension(280, 400));
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
		// ifProgramRegisters.addHDNumberValueChangedListener(applicationAdapter);
		desktopPane.add(ifProgramRegisters);
		ifProgramRegisters.setVisible(true);

		ifIndexRegisters = new V_IF_IndexRegisters();
		ifIndexRegisters.setLocation(241, 105);
		desktopPane.add(ifIndexRegisters);
		ifIndexRegisters.setVisible(true);

		ifSpecialRegisters = new V_IF_SpecialRegisters();
		ifSpecialRegisters.setLocation(0, 216);
		desktopPane.add(ifSpecialRegisters);
		ifSpecialRegisters.setVisible(true);

		ifCCR = new V_IF_CCR();
		ifCCR.setLocation(230, 216);
		desktopPane.add(ifCCR);
		ifCCR.setVisible(true);

		JPanel statusBar = new JPanel();
		GridBagConstraints gbc_statusBar = new GridBagConstraints();
		gbc_statusBar.fill = GridBagConstraints.BOTH;
		gbc_statusBar.gridx = 0;
		gbc_statusBar.gridy = 2;
		frameBase.getContentPane().add(statusBar, gbc_statusBar);
		GridBagLayout gbl_statusBar = new GridBagLayout();
		gbl_statusBar.columnWidths = new int[] { 0, 0 };
		gbl_statusBar.rowHeights = new int[] { 0, 0 };
		gbl_statusBar.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_statusBar.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		statusBar.setLayout(gbl_statusBar);

		JLabel lblNewLabel_1 = new JLabel("Status Bar");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 0;
		statusBar.add(lblNewLabel_1, gbc_lblNewLabel_1);

		JMenuBar menuBar = new JMenuBar();
		frameBase.setJMenuBar(menuBar);

		JMenu mnuFile = new JMenu("File");
		menuBar.add(mnuFile);

		JMenuItem mnuFileNew = new JMenuItem("New");
		mnuFileNew.setName(MNU_FILE_NEW);
		mnuFileNew.addActionListener(applicationAdapter);
		mnuFile.add(mnuFileNew);

		JMenuItem mnuFileLoadMemory = new JMenuItem("Load Memory...");
		mnuFileLoadMemory.setName(MNU_FILE_LOAD_MEMORY);
		mnuFileLoadMemory.addActionListener(applicationAdapter);
		mnuFile.add(mnuFileLoadMemory);

		JSeparator separator99 = new JSeparator();
		mnuFile.add(separator99);

		JMenuItem mnuFileSaveMemory = new JMenuItem("Save Memory...");
		mnuFileSaveMemory.setName(MNU_FILE_SAVE_MEMORY);
		mnuFileSaveMemory.addActionListener(applicationAdapter);
		mnuFile.add(mnuFileSaveMemory);

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

		JMenuItem mnuWindowPrimaryRegisters = new JMenuItem("Primary Registers");
		mnuWindowPrimaryRegisters.addActionListener(applicationAdapter);

		JMenuItem mnuWindowZ80Support = new JMenuItem("Z80 Machine Support");
		mnuWindowZ80Support.setName(MNU_WINDOW_Z80_SUPPORT);
		mnuWindowZ80Support.addActionListener(applicationAdapter);

		JMenuItem mnuWindowsLST = new JMenuItem("List Device");
		mnuWindowsLST.setName(MNU_WINDOW_LST);
		mnuWindowsLST.addActionListener(applicationAdapter);
		mnuWindow.add(mnuWindowsLST);

		JMenuItem mnuWindowsTTY = new JMenuItem("TTY Device");
		mnuWindowsTTY.setName(MNU_WINDOW_TTY);
		mnuWindowsTTY.addActionListener(applicationAdapter);
		mnuWindow.add(mnuWindowsTTY);
		mnuWindow.add(mnuWindowZ80Support);

		JSeparator separator = new JSeparator();
		mnuWindow.add(separator);

		JSeparator separator_4 = new JSeparator();
		mnuWindow.add(separator_4);
		mnuWindowPrimaryRegisters.setName(MNU_WINDOW_PRIMARY_REGISTERS);
		mnuWindow.add(mnuWindowPrimaryRegisters);

		JMenuItem mnuWindowProgramControl = new JMenuItem("Program Control");
		mnuWindowProgramControl.addActionListener(applicationAdapter);
		mnuWindowProgramControl.setName(MNU_WINDOW_PROGRAM_CONTROL);
		mnuWindow.add(mnuWindowProgramControl);

		JMenuItem mnuWindowConditionCodes = new JMenuItem("Condition Codes");
		mnuWindowConditionCodes.addActionListener(applicationAdapter);
		mnuWindowConditionCodes.setName(MNU_WINDOW_CONDITION_CODES);
		mnuWindow.add(mnuWindowConditionCodes);

		JMenuItem mnuWindowIndexRegisters = new JMenuItem("Index Registers");
		mnuWindowIndexRegisters.addActionListener(applicationAdapter);
		mnuWindowIndexRegisters.setName(MNU_WINDOW_INDEX_REGISTERS);
		mnuWindow.add(mnuWindowIndexRegisters);

		JMenuItem mnuWindowSpecialRegisters = new JMenuItem("Special Registers");
		mnuWindowSpecialRegisters.setName(MNU_WINDOW_SPECIAL_REGISTERS);
		mnuWindowSpecialRegisters.addActionListener(applicationAdapter);
		mnuWindow.add(mnuWindowSpecialRegisters);

		JSeparator separator_3 = new JSeparator();
		mnuWindow.add(separator_3);

		JMenuItem mnuWindowsReset = new JMenuItem("Reset All Register Displays");
		mnuWindowsReset.setName(MNU_WINDOW_RESET);
		mnuWindowsReset.addActionListener(applicationAdapter);
		mnuWindow.add(mnuWindowsReset);

		JMenu mnuTools = new JMenu("Tools");
		menuBar.add(mnuTools);

		JMenuItem mnuToolsDiskUtility = new JMenuItem("Disk Utilities");
		mnuToolsDiskUtility.setName(MNU_TOOLS_DISK_UTILITY);
		mnuToolsDiskUtility.addActionListener(applicationAdapter);
		mnuTools.add(mnuToolsDiskUtility);

		JMenuItem mnuToolsDiskNew = new JMenuItem("New Disk");
		mnuToolsDiskNew.setName(MNU_TOOLS_DISK_NEW);
		mnuToolsDiskNew.addActionListener(applicationAdapter);
		mnuTools.add(mnuToolsDiskNew);

	}// initialize

	//////////////////////////////////////////////////////////////////////////

	static final String EMPTY_STRING = "";

	//////////////////////////////////////////////////////////////////////////
	private static final String MNU_FILE_NEW = "mnuFileNew";
	private static final String MNU_FILE_LOAD_MEMORY = "mnuFileLoadMemory";
	private static final String MNU_FILE_SAVE_MEMORY = "mnuFileSaveMemory";
	private static final String MNU_FILE_SAVE_AS = "mnuFileSaveAs";
	private static final String MNU_FILE_PRINT = "mnuFilePrint";
	private static final String MNU_FILE_EXIT = "mnuFileExit";

	private static final String MNU_WINDOW_Z80_SUPPORT = "mnuWindowsZ80Support";
	private static final String MNU_WINDOW_PRIMARY_REGISTERS = "mnuWindowsPrimaryRegisters";
	private static final String MNU_WINDOW_PROGRAM_CONTROL = "mnuWindowsProgramControl";
	private static final String MNU_WINDOW_INDEX_REGISTERS = "mnuWindowsIndexRegisters";
	private static final String MNU_WINDOW_SPECIAL_REGISTERS = "mnuWindowsSpecialRegisters";
	private static final String MNU_WINDOW_CONDITION_CODES = "mnuWindowsConditionCodes";
	private static final String MNU_WINDOW_RESET = "mnuWindowsReset";

	private static final String MNU_WINDOW_TTY = "mnuWindowsTTY";
	private static final String MNU_WINDOW_LST = "mnuWindowsLST";

	private static final String MNU_TOOLS_DISK_UTILITY = "mnuToolsDiskUtility";
	private static final String MNU_TOOLS_DISK_NEW = "mnuToolsDiskNew";

	private static final String BUTTON_RUN_STOP = "tbRunStop";
	private static final String BUTTON_RUN_TIP = "Run";
	private static final String BUTTON_STOP_TIP = "Stop";

	private static final String BUTTON_STEP = "btnStop";

	private static final String BUTTON_BOOT = "btnBoot";

	private static final int INSET_X = 1;
	private static final int INSET_Y = 1;

	private JFrame frameBase;
	private JDesktopPane desktopPane;
	private V_IF_DiskPanel ifDiskPanel;
	// private JTextPane txtLog;
	private V_IF_PrimaryRegisters ifPrimaryRegisters;
	private V_IF_ProgramRegisters ifProgramRegisters;
	private V_IF_IndexRegisters ifIndexRegisters;
	private V_IF_SpecialRegisters ifSpecialRegisters;
	private V_IF_CCR ifCCR;
	private JPanel leftTopPanel;
	private JSpinner spinnerStepCount;
	private JToggleButton tbRunStop;
	private JPanel disksPanel;
	private JToggleButton btnBoot;
	//////////////////////////////////////////////////////////////////////////

	class ApplicationAdapter implements ActionListener, HDNumberValueChangeListener, Observer {// ,
																								// ListSelectionListener
		/* ActionListener */
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			Component source = (Component) actionEvent.getSource();
			String name = source.getName();
			switch (name) {

			case BUTTON_RUN_STOP:
				doRunStop();
				break;
			case BUTTON_STEP:
				doStep();
				break;
			case BUTTON_BOOT:
				doBoot();
				break;
			/* Menu - File */
			case MNU_FILE_NEW:
				doFileNew();
				break;
			case MNU_FILE_LOAD_MEMORY:
				doFileLoadMemory();
				break;
			case MNU_FILE_SAVE_MEMORY:
				doFileSaveMemory();
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

			/* Menu - Windows */

			case MNU_WINDOW_LST:
			case MNU_WINDOW_TTY:
				doDeviceToggle(name);
				break;
			case MNU_WINDOW_PRIMARY_REGISTERS:
			case MNU_WINDOW_PROGRAM_CONTROL:
			case MNU_WINDOW_INDEX_REGISTERS:
			case MNU_WINDOW_SPECIAL_REGISTERS:
			case MNU_WINDOW_CONDITION_CODES:
			case MNU_WINDOW_Z80_SUPPORT:
				doWindowToggle(name);
				break;
			case MNU_WINDOW_RESET:
				doResetAllRegisterDisplays();
				break;

			/* Menu - tools */
			case MNU_TOOLS_DISK_UTILITY:
				doDiskUtility();
				break;

			case MNU_TOOLS_DISK_NEW:
				doDiskNew();
				break;

			}// switch
		}// actionPerformed

		////////////////////////////////
		/* HDNumberValueChangeListener */

		@Override
		public void valueChanged(HDNumberValueChangeEvent hDNumberValueChangeEvent) {
			tabDialog.updateDisplay();
		}// valueChanged

		////////////////////////////////
		/* Observer */

		@Override
		public void update(Observable cpuBuss, Object mte) {
			Trap trap = ((MemoryTrapEvent) mte).getTrap();
			if (trap.equals(Trap.DEBUG)) {
				System.out.printf("[update - DEBUG]  Location %04X%n", ((MemoryTrapEvent) mte).getLocation());
				tbRunStop.setSelected(false);
				doRunStop();
			} else if (trap.equals(Trap.HALT)) {
				tbRunStop.setSelected(false);
				// int newLocation = (((MemoryTrapEvent) mte).getLocation() + 1) & 0xFFFF;
				// WorkingRegisterSet.getInstance().setProgramCounter(newLocation);
				doRunStop();
			} // if - debug
				// stateDisplay.setDisplayComponentsEnabled(true);
				// btnRun.setSelected(false);
				// cpu.setError(ErrorType.STOP);
				// updateView();

		}// update
	}// class AdapterAction
}// class GUItemplate