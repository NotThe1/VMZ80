package codeSupport;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;

import hardware.WorkingRegisterSet;
//import codeSupport.debug.ShowCode.Limits;
import memory.Core.Trap;
import memory.CpuBuss;
import utilities.filePicker.FilePicker;
//import utilities.filePicker.FilePicker;
import utilities.hdNumberBox.HDNumberBox;
import utilities.hdNumberBox.HDNumberValueChangeEvent;
import utilities.hdNumberBox.HDNumberValueChangeListener;
import utilities.menus.MenuUtility;

public class DebugFrame extends JInternalFrame implements Runnable {

	private static final long serialVersionUID = 1L;
	// private AppLogger log = AppLogger.getInstance();
	private AdapterDebug adapterDebug = new AdapterDebug();
	private DefaultListModel<String> trapModel = new DefaultListModel<String>();
	private CpuBuss cpuBuss = CpuBuss.getInstance();
	private WorkingRegisterSet wrs = WorkingRegisterSet.getInstance();

	private Path newListPath;
	private HashMap<String, Limits> fileList = new HashMap<String, Limits>();
	private HashMap<String, String> listings = new HashMap<String, String>();

	private int programCounter;
	private int currentStart, currentEnd;
	private String currentFilePath = null;
	private boolean fileIsCurrent;

	private void loadList() {
		trapModel.clear();
		List<Integer> locs = cpuBuss.getTraps(Trap.DEBUG);
		Collections.sort(locs);
		for (Integer loc : locs) {
			trapModel.addElement(String.format("%04X", loc));
		} // for

	}// loadList

	private void clearCurrentIndicaters() {
		currentFilePath = null;
		currentStart = -1;
		currentEnd = -1;
		fileIsCurrent = false;
	}// clearCurrentIndicaters

	@Override
	public void run() {
		setProgramCounter(wrs.getProgramCounter());
	}// run

	/////////////////////////////////////////////////////////////////////////////////
	/**
	 * Create the frame.
	 */
	public DebugFrame() {
		initialize();
		appInit();
	}// Constructor

	public void close() {
		appClose();
	}// close

	private void appClose() {

	}// appClose

	private void appInit() {
		hdNumber.setMaxValue(0xFFFF);
		hdNumber.setMinValue(0);
		hdNumber.setValue(0);
		hdNumber.setHexDisplay();

		hdNumber.addHDNumberValueChangedListener(adapterDebug);
		trapModel.clear();
		listTraps.setModel(trapModel);
		//
		taListing.setSelectedTextColor(Color.BLUE);
		clearCurrentIndicaters();

	}// appInit

	private void initialize() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		mnuFiles = new JMenu("Files");
		menuBar.add(mnuFiles);

		JMenuItem mnuFileAddFile = new JMenuItem("Add File");
		mnuFileAddFile.setName(MNU_FILE_ADD_FILE);
		mnuFileAddFile.addActionListener(adapterDebug);
		mnuFiles.add(mnuFileAddFile);

		JMenuItem mnuFileAddFilesFromList = new JMenuItem("Add Files from List");
		mnuFileAddFilesFromList.setName(MNU_FILE_ADD_FILES_FROM_LIST);
		mnuFileAddFilesFromList.addActionListener(adapterDebug);
		mnuFiles.add(mnuFileAddFilesFromList);

		JMenuItem mnuFileSaveSelectedToList = new JMenuItem("Save Selected to List");
		mnuFileSaveSelectedToList.setName(MNU_FILE_SAVE_SELECTED_TO_LIST);
		mnuFileSaveSelectedToList.addActionListener(adapterDebug);

		JSeparator separator = new JSeparator();
		mnuFiles.add(separator);
		mnuFiles.add(mnuFileSaveSelectedToList);

		JSeparator separatorStart = new JSeparator();
		separatorStart.setName(RF_START);
		mnuFiles.add(separatorStart);

		JSeparator separatorEnd = new JSeparator();
		separatorEnd.setName(RF_END);
		mnuFiles.add(separatorEnd);

		JMenuItem mnuRemoveSelectedFiles = new JMenuItem("Remove Selected Files");
		mnuRemoveSelectedFiles.setName(MNU_REMOVE_SELECTED_FILES);
		mnuRemoveSelectedFiles.addActionListener(adapterDebug);
		mnuFiles.add(mnuRemoveSelectedFiles);

		JMenuItem mnuClearAllFiles = new JMenuItem("Clear All Files");
		mnuClearAllFiles.setName(MNU_CLEAR_ALL_FILES);
		mnuClearAllFiles.addActionListener(adapterDebug);
		mnuFiles.add(mnuClearAllFiles);
		contentPane = new JPanel();
		contentPane.setBorder(null);
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0 };// { 424, 0 }
		gbl_contentPane.rowHeights = new int[] { 0 };// { 239, 0 }
		gbl_contentPane.columnWeights = new double[] { 1.0 };
		gbl_contentPane.rowWeights = new double[] { 1.0 };
		contentPane.setLayout(gbl_contentPane);

		JPanel panelMain = new JPanel();
		panelMain.setBorder(new LineBorder(Color.BLUE));
		GridBagConstraints gbc_panelMain = new GridBagConstraints();
		gbc_panelMain.anchor = GridBagConstraints.SOUTHEAST;
		gbc_panelMain.gridx = 0;
		gbc_panelMain.gridy = 0;
		contentPane.add(panelMain, gbc_panelMain);
		GridBagLayout gbl_panelMain = new GridBagLayout();
		gbl_panelMain.columnWidths = new int[] { 0, 0 };
		gbl_panelMain.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelMain.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelMain.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		panelMain.setLayout(gbl_panelMain);

		JSplitPane splitPane = new JSplitPane();
		splitPane.setEnabled(false);
		splitPane.setOneTouchExpandable(true);
		GridBagConstraints gbc_splitPane = new GridBagConstraints();
		gbc_splitPane.insets = new Insets(0, 0, 5, 0);
		gbc_splitPane.fill = GridBagConstraints.BOTH;
		gbc_splitPane.gridx = 0;
		gbc_splitPane.gridy = 0;
		panelMain.add(splitPane, gbc_splitPane);

		JPanel panelBreaks = new JPanel();
		panelBreaks.setBorder(null);
		panelBreaks.setAlignmentX(10.0f);
		splitPane.setLeftComponent(panelBreaks);
		GridBagLayout gbl_panelBreaks = new GridBagLayout();
		gbl_panelBreaks.columnWidths = new int[] { 0, 0, 0 };
		gbl_panelBreaks.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelBreaks.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelBreaks.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0,
				Double.MIN_VALUE };
		panelBreaks.setLayout(gbl_panelBreaks);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setPreferredSize(new Dimension(10, 0));
		horizontalStrut.setMinimumSize(new Dimension(10, 0));
		horizontalStrut.setMaximumSize(new Dimension(10, 0));
		GridBagConstraints gbc_horizontalStrut = new GridBagConstraints();
		gbc_horizontalStrut.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalStrut.gridx = 0;
		gbc_horizontalStrut.gridy = 0;
		panelBreaks.add(horizontalStrut, gbc_horizontalStrut);

		Component vStrut1 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_vStrut1 = new GridBagConstraints();
		gbc_vStrut1.insets = new Insets(0, 0, 5, 0);
		gbc_vStrut1.gridx = 1;
		gbc_vStrut1.gridy = 0;
		panelBreaks.add(vStrut1, gbc_vStrut1);

		tbEnable = new JToggleButton(ENABLE);
		tbEnable.setName(RB_ENABLE);
		tbEnable.addActionListener(adapterDebug);
		tbEnable.setMaximumSize(new Dimension(85, 23));
		tbEnable.setMinimumSize(new Dimension(85, 23));
		tbEnable.setPreferredSize(new Dimension(85, 23));
		GridBagConstraints gbc_tbEnable = new GridBagConstraints();
		gbc_tbEnable.insets = new Insets(0, 0, 5, 0);
		gbc_tbEnable.gridx = 1;
		gbc_tbEnable.gridy = 1;
		panelBreaks.add(tbEnable, gbc_tbEnable);

		Component verticalStrut = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut.gridx = 1;
		gbc_verticalStrut.gridy = 2;
		panelBreaks.add(verticalStrut, gbc_verticalStrut);

		JButton btnReset = new JButton("Reset");
		btnReset.setName(BTN_RESET);
		btnReset.addActionListener(adapterDebug);
		btnReset.setMinimumSize(new Dimension(85, 23));
		btnReset.setMaximumSize(new Dimension(85, 23));
		btnReset.setPreferredSize(new Dimension(85, 23));
		GridBagConstraints gbc_btnReset = new GridBagConstraints();
		gbc_btnReset.insets = new Insets(0, 0, 5, 0);
		gbc_btnReset.gridx = 1;
		gbc_btnReset.gridy = 3;
		panelBreaks.add(btnReset, gbc_btnReset);

		Component verticalStrut_1 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_1 = new GridBagConstraints();
		gbc_verticalStrut_1.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_1.gridx = 1;
		gbc_verticalStrut_1.gridy = 4;
		panelBreaks.add(verticalStrut_1, gbc_verticalStrut_1);

		JButton btnRemove = new JButton("Remove");
		btnRemove.setName(BTN_REMOVE);
		btnRemove.addActionListener(adapterDebug);
		btnRemove.setMaximumSize(new Dimension(85, 23));
		btnRemove.setMinimumSize(new Dimension(85, 23));
		btnRemove.setPreferredSize(new Dimension(85, 23));
		GridBagConstraints gbc_btnRemove = new GridBagConstraints();
		gbc_btnRemove.insets = new Insets(0, 0, 5, 0);
		gbc_btnRemove.gridx = 1;
		gbc_btnRemove.gridy = 5;
		panelBreaks.add(btnRemove, gbc_btnRemove);

		Component verticalStrut_2 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_2 = new GridBagConstraints();
		gbc_verticalStrut_2.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_2.gridx = 1;
		gbc_verticalStrut_2.gridy = 6;
		panelBreaks.add(verticalStrut_2, gbc_verticalStrut_2);

		JButton btnClear = new JButton("Clear");
		btnClear.setName(BTN_CLEAR);
		btnClear.addActionListener(adapterDebug);
		GridBagConstraints gbc_btnClear = new GridBagConstraints();
		gbc_btnClear.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnClear.insets = new Insets(0, 0, 5, 0);
		gbc_btnClear.gridx = 1;
		gbc_btnClear.gridy = 7;
		panelBreaks.add(btnClear, gbc_btnClear);

		Component verticalStrut_3 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_3 = new GridBagConstraints();
		gbc_verticalStrut_3.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_3.gridx = 1;
		gbc_verticalStrut_3.gridy = 8;
		panelBreaks.add(verticalStrut_3, gbc_verticalStrut_3);

		hdNumber = new utilities.hdNumberBox.HDNumberBox();// utilities.hdNumberBox.HDNumberBox
		hdNumber.setMinimumSize(new Dimension(71, 23));
		hdNumber.setMaximumSize(new Dimension(71, 23));
		hdNumber.setPreferredSize(new Dimension(71, 23));
		GridBagConstraints gbc_hdNumber = new GridBagConstraints();
		gbc_hdNumber.insets = new Insets(0, 0, 5, 0);
		gbc_hdNumber.fill = GridBagConstraints.BOTH;
		gbc_hdNumber.gridx = 1;
		gbc_hdNumber.gridy = 9;
		panelBreaks.add(hdNumber, gbc_hdNumber);
		GridBagLayout gbl_hdNumber = new GridBagLayout();
		gbl_hdNumber.columnWidths = new int[] { 0 };
		gbl_hdNumber.rowHeights = new int[] { 0 };
		gbl_hdNumber.columnWeights = new double[] { Double.MIN_VALUE };
		gbl_hdNumber.rowWeights = new double[] { Double.MIN_VALUE };
		hdNumber.setLayout(gbl_hdNumber);

		Component verticalStrut_4 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_4 = new GridBagConstraints();
		gbc_verticalStrut_4.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut_4.gridx = 1;
		gbc_verticalStrut_4.gridy = 10;
		panelBreaks.add(verticalStrut_4, gbc_verticalStrut_4);

		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.gridx = 1;
		gbc_scrollPane_1.gridy = 11;
		panelBreaks.add(scrollPane_1, gbc_scrollPane_1);

		listTraps = new JList<String>();
		scrollPane_1.setViewportView(listTraps);
		listTraps.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		listTraps.setPreferredSize(new Dimension(75, 0));

		JLabel lblNewLabel_1 = new JLabel("Breaks Set");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setHorizontalTextPosition(SwingConstants.CENTER);
		scrollPane_1.setColumnHeaderView(lblNewLabel_1);

		Component verticalStrut_5 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_5 = new GridBagConstraints();
		gbc_verticalStrut_5.gridx = 1;
		gbc_verticalStrut_5.gridy = 12;
		panelBreaks.add(verticalStrut_5, gbc_verticalStrut_5);

		JPanel panelListing = new JPanel();
		panelListing.setPreferredSize(new Dimension(400, 0));
		panelListing.setMinimumSize(new Dimension(400, 0));
		panelListing.setMaximumSize(new Dimension(0, 0));
		panelListing.setBorder(null);
		splitPane.setRightComponent(panelListing);
		GridBagLayout gbl_panelListing = new GridBagLayout();
		gbl_panelListing.columnWidths = new int[] { 0, 0 };
		gbl_panelListing.rowHeights = new int[] { 0, 0 };
		gbl_panelListing.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panelListing.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		panelListing.setLayout(gbl_panelListing);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panelListing.add(scrollPane, gbc_scrollPane);

		taListing = new JTextArea();
		taListing.setEditable(false);
		taListing.addMouseListener(adapterDebug);
		taListing.setFont(new Font("Courier New", Font.PLAIN, 14));
		scrollPane.setViewportView(taListing);

		lblLisingName = new JLabel(NO_ACTIVE_FILE);
		lblLisingName.setForeground(Color.BLUE);
		lblLisingName.setFont(new Font("Courier New", Font.BOLD, 17));
		lblLisingName.setHorizontalAlignment(SwingConstants.CENTER);
		scrollPane.setColumnHeaderView(lblLisingName);
		splitPane.setDividerLocation(110);

		//////////////////////////
		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		panel.setPreferredSize(new Dimension(0, 25));
		panel.setMinimumSize(new Dimension(0, 25));
		panel.setMaximumSize(new Dimension(0, 25));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		panelMain.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		lblStatus = new JLabel(NO_ACTIVE_FILE);
		lblStatus.setForeground(Color.RED);
		lblStatus.setFont(new Font("Arial", Font.PLAIN, 14));
		lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblStatus = new GridBagConstraints();
		gbc_lblStatus.gridx = 0;
		gbc_lblStatus.gridy = 0;
		panel.add(lblStatus, gbc_lblStatus);
	}// initialize

	private static final String NO_ACTIVE_FILE = "<< No Active File >>";
	private static final String EMPTY_STRING = "";
	private static final String DOT = ".";
	// private static final String PUM_LOG_PRINT = "popupLogPrint";
	// private static final String PUM_LOG_CLEAR = "popupLogClear";

	private static final String ENABLE = "Enable";
	private static final String DISABLE = "Disable";
	private static final String RB_ENABLE = "rbEnabled";
	private static final String BTN_RESET = "btnReset";
	private static final String BTN_REMOVE = "btnRemovet";
	private static final String BTN_CLEAR = "btnCLEAR";

	private static final String MNU_FILE_ADD_FILE = "mnuFileAddFile";
	private static final String MNU_FILE_ADD_FILES_FROM_LIST = "mnuFileAddFilesFromList";
	private static final String MNU_FILE_SAVE_SELECTED_TO_LIST = "mnuFilSaveSelectedToList";
	private static final String MNU_REMOVE_SELECTED_FILES = "mnuRemoveSelectedFiles";
	private static final String MNU_CLEAR_ALL_FILES = "mnuClearAllFiles";

	private static final String RF_START = MenuUtility.RECENT_FILES_START;
	private static final String RF_END = MenuUtility.RECENT_FILES_END;

	private JLabel lblStatus;
	private JToggleButton tbEnable;
	private JPanel contentPane;
	private JList<String> listTraps;
	private HDNumberBox hdNumber;
	private JTextArea taListing;
	private JLabel lblLisingName;
	private JMenu mnuFiles;

	private void setProgramCounter(int programCounter) {
		this.programCounter = programCounter & 0XFFFF;

		setFileToShow(programCounter);

		if (currentFilePath == null) {
			// notified user already.
			return; // not much do do
		} // if

		if (!fileIsCurrent) { // file is not current file
			loadDisplay(currentFilePath);
		} // if

		selectTheCorrectLine();
	}// setProgramCounter

	private void setFileToShow(int lineNumber) {
		// returns true if number is in the currently loaded file
		fileIsCurrent = false;
		if (isLineInCurrentFile(lineNumber)) {
			fileIsCurrent = true;
			return; // everything is in place
		} // if its in current file

		// Limits thisFilesLimit = new Limits();
		boolean weHaveAFile = false;

		for (Map.Entry<String, Limits> entry : fileList.entrySet()) {
			if (isLineInThisFile(lineNumber, entry.getKey())) {
				weHaveAFile = true;
				currentFilePath = entry.getKey();
				Limits thisLimits = fileList.get(entry.getKey());
				currentStart = thisLimits.start;
				currentEnd = thisLimits.end;
				break;
			} // if
		} // for each

		lblStatus.setText("-");
		if (!weHaveAFile) {
			clearCurrentIndicaters();
			String status = String.format("Target line: %04X Not In Any Currently Loaded Files%n", lineNumber);
			lblStatus.setText(status);
		} // if file not found
		return;
	}// getFileToShow

	private void selectTheCorrectLine() {
		String targetAddressRegex = String.format("[0-9]{4}: %04X [A-Fa-f|[0-9]]{2}.*\r", programCounter);
		Pattern targetAddressPattern = Pattern.compile(targetAddressRegex);
		Matcher targetAddressMatcher;
		targetAddressMatcher = targetAddressPattern.matcher(taListing.getText());
		if (targetAddressMatcher.find()) {

			taListing.setSelectionStart(targetAddressMatcher.start());
			taListing.setSelectionEnd(targetAddressMatcher.end());
			lblStatus.setText(String.format("Program Counter at %04X", programCounter));
			lblStatus.updateUI();
		} else {
			String status = String.format("Target line: %04X Not Start of Instruction%n", programCounter);
			lblStatus.setText(status);
		} // if found ?

	}// selectTheCorrectLine

	private boolean isLineInCurrentFile(int lineNumber) {
		return ((lineNumber >= currentStart) && (lineNumber <= currentEnd));
	}// isLineInCurrentFile

	private boolean isLineInThisFile(int lineNumber, String filePath) {
		boolean isLineInThisFile = false;
		Limits thisFilesLimit = fileList.get(filePath);
		if ((lineNumber >= thisFilesLimit.start) && (lineNumber <= thisFilesLimit.end)) {
			currentFilePath = filePath;
			currentStart = thisFilesLimit.start;
			currentEnd = thisFilesLimit.end;
			isLineInThisFile = true;
		} // if
		return isLineInThisFile;
	}// isLineInThisFile

	// +/+/+/+/+/+/+/+/+/+/+/+/+/+/+/ DEBUG /+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+

	private void doAddFile() {

		JFileChooser fc = (newListPath) == null ? FilePicker.getListings() : FilePicker.getListings(newListPath);
		if (fc.showOpenDialog(this) == JFileChooser.CANCEL_OPTION) {
			System.out.println("Bailed out of the open");
			return;
		} // if - open
		for (File file : fc.getSelectedFiles()) {
			newListPath = Paths.get(file.getParent());
			addFile(file.toString());
		} // for each file

	}// doAddFile

	private void addFile(String listFileFullPath) {
		int endAddress = -1, startAddress = -1;
		Pattern hotLineRegex = Pattern
				.compile("(?<lineNumber>\\d{4}: )(?<address>[A-Fa-f\\d]{4})(?<junk> [A-Fa-f\\d]{2})");
		Matcher matcherForHotLine;
		StringBuilder stringBuilder = new StringBuilder();

		try {
			FileReader fileReader;
			fileReader = new FileReader(listFileFullPath);
			BufferedReader reader = new BufferedReader(fileReader);
			String line;
			int addressOnThisLine;
			while ((line = reader.readLine()) != null) {
				matcherForHotLine = hotLineRegex.matcher(line);
				if (matcherForHotLine.find()) {
					// String addressStr = matcherForHotLine.group("address");
					addressOnThisLine = Integer.valueOf(matcherForHotLine.group("address"), 16);
					endAddress = addressOnThisLine;
					startAddress = startAddress == -1 ? addressOnThisLine : startAddress;
				} // if - its a hot line
				stringBuilder.append(line + System.lineSeparator());
			} // while
			reader.close();
			taListing.setText(stringBuilder.toString());
		} catch (FileNotFoundException fnfe) {
			JOptionPane.showMessageDialog(null, listFileFullPath + "not found", "unable to locate",
					JOptionPane.ERROR_MESSAGE);
			return; // exit gracefully
		} catch (IOException ie) {
			JOptionPane.showMessageDialog(null, listFileFullPath + ie.getMessage(), "IO error",
					JOptionPane.ERROR_MESSAGE);
			return; // exit gracefully
		} // try
//		boolean newFile = !fileList.containsKey(listFileFullPath);
		fileList.put(listFileFullPath, new Limits(startAddress, endAddress));
		listings.put(listFileFullPath, stringBuilder.toString());
		loadDisplay(listFileFullPath);
		taListing.setCaretPosition(0);
//		if (newFile) {
			MenuUtility.addItemToList(mnuFiles, new File(listFileFullPath), new JCheckBoxMenuItem());
//			System.out.printf("[DebugFrame.addFile] %s%n", "New File");
//		} // if new File
	}// addFile

	private void loadDisplay(String filePath) {
		lblLisingName.setText(new File(filePath).getName());
		lblLisingName.setToolTipText(filePath);

		taListing.setText(listings.get(filePath));
		Limits limits = fileList.get(filePath);
		currentStart = limits.start;
		currentEnd = limits.end;
		currentFilePath = filePath;
	}// loadDisplay

	private void doAddFilesFromList() {
		JFileChooser fc = FilePicker.getListingCollection();
		// JFileChooser fc = FilePicker.getAllListPicker();// FilePicker.getAnyListPicker();
		if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			System.out.println("You cancelled the Load Asm from File List...");
		} else {
			FileReader fileReader;
			String filePathName = null;
			try {
				fileReader = new FileReader((fc.getSelectedFile().getAbsolutePath()));
				BufferedReader reader = new BufferedReader(fileReader);
				while ((filePathName = reader.readLine()) != null) {
					// filePathName = filePathName.replaceFirst("(?i)\\.mem$", "\\.list");
					addFile(filePathName);
				} // while
				reader.close();
			} catch (IOException e1) {
				System.out.printf(e1.getMessage() + "%n", "");
			} // try
		} // if

		return;
	}// doAddFilesFromList

	private void doSaveSelectedToList() {
		JFileChooser fc = FilePicker.getListingCollection();
		// JFileChooser fc = FilePicker.getListAsmPicker();
		if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
			System.out.println("You cancelled Save Selected as Collection...");
			return;
		} // if
		String listFile = fc.getSelectedFile().getAbsolutePath();
		String completeSuffix = DOT + FilePicker.COLLECTIONS_LISTING;
		listFile = listFile.replaceFirst("\\" + completeSuffix + "$", EMPTY_STRING);
		try {
			FileWriter fileWriter = new FileWriter(listFile + completeSuffix);
			BufferedWriter writer = new BufferedWriter(fileWriter);

			ArrayList<String> selectedFiles = MenuUtility.getFilePathsSelected(mnuFiles);
			for (String selectedFile : selectedFiles) {
				writer.write(selectedFile + System.lineSeparator());
			} // for
			writer.close();

		} catch (Exception e1) {
			e1.printStackTrace();
			return;
		} // try
	}// doSaveSelectedToList

	private void doClearSelectedFiles() {
		ArrayList<String> filesToBeCleared = MenuUtility.getFilePathsSelected(mnuFiles);
		for (String fileToBeCleared : filesToBeCleared) {
			clearFile(fileToBeCleared);
		} // for each
		MenuUtility.clearListSelected(mnuFiles);
		adjustTheDisplay();

	}// doClearSelectedFiles

	private void doClearAllFiles() {
		ArrayList<String> filesToBeCleared = MenuUtility.getFilePaths(mnuFiles);
		for (String fileToBeCleared : filesToBeCleared) {
			clearFile(fileToBeCleared);
		} // for each
		MenuUtility.clearList(mnuFiles);
		adjustTheDisplay();

	}// doClearAllFiles

	private void clearFile(String filePath) {
		if (filePath.equals(currentFilePath)) {
			clearCurrentIndicaters();
		} // if current path
		fileList.remove(filePath);
		listings.remove(filePath);
	}// clearFile

	private void adjustTheDisplay() {
		if (fileList.isEmpty()) {
			lblLisingName.setText(NO_ACTIVE_FILE);
			lblStatus.setText(NO_ACTIVE_FILE);
			lblLisingName.setText(EMPTY_STRING);
			listings.clear();
			taListing.setText("");
		} else if (!fileList.containsKey(currentFilePath)) {
			/** does not have current path , but does have some other path to display **/
			Set<String> keys = fileList.keySet();
			/** get one valid entry **/
			for (String filePath : keys) {
				loadDisplay(filePath);
				break;
			} // for get a valid filePath

		} // if
		else {
			// leave it alone.
		} //

	}// adjustTheDisplay

	private void doDebugEnable() {
		if (tbEnable.getText().equals(ENABLE)) {
			tbEnable.setText(DISABLE);
			cpuBuss.setDebugTrapEnabled(true);
		} else {
			tbEnable.setText(ENABLE);
			cpuBuss.setDebugTrapEnabled(false);
		} // if
	}// doDebugEnable

	private void doDebugReset() {
		tbEnable.setText(ENABLE);
		cpuBuss.setDebugTrapEnabled(false);
		cpuBuss.removeTraps(Trap.DEBUG);
		loadList();

	}// doDebugReset

	private void doDebugRemove() {
		Integer loc = Integer.valueOf((String) listTraps.getSelectedValue(), 16);
		System.out.printf("[actionPerformed]  %04X -  %n", loc);
		cpuBuss.removeTrap(loc, Trap.DEBUG);
		loadList();
	}// doDebugRemove

	private void doDebugClear() {
		cpuBuss.removeTraps(Trap.DEBUG);
		loadList();
	}// doDebugClear

	// +/+/+/+/+/+/+/+/+/+/+/+/+/+/+/ DEBUG /+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+

	///////////////////////////
	static class Limits {
		public int start;
		public int end;

		public Limits() {
			this(-1, -1);
		}// Constructor

		public Limits(int start, int end) {
			this.start = start;
			this.end = end;
		}// Constructor
	}// class Limits

	///////////////////////////
	class AdapterDebug implements ActionListener, HDNumberValueChangeListener, MouseListener {
		/* ActionListener */

		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String name = ((Component) actionEvent.getSource()).getName();
			switch (name) {
			case RB_ENABLE:
				doDebugEnable();
				break;
			case BTN_RESET:
				doDebugReset();
				break;
			case BTN_REMOVE:
				doDebugRemove();
				break;
			case BTN_CLEAR:
				doDebugClear();
				break;

			case MNU_FILE_ADD_FILE:
				doAddFile();
				break;
			case MNU_FILE_ADD_FILES_FROM_LIST:
				doAddFilesFromList();
				break;
			case MNU_FILE_SAVE_SELECTED_TO_LIST:
				doSaveSelectedToList();
				break;
			case MNU_REMOVE_SELECTED_FILES:
				doClearSelectedFiles();
				break;
			case MNU_CLEAR_ALL_FILES:
				doClearAllFiles();
				break;
			}// switch

		}// actionPerformed

		/* HDNumberValueChangeListener */

		@Override
		public void valueChanged(HDNumberValueChangeEvent hDNumberValueChangeEvent) {
			int newValue = hDNumberValueChangeEvent.getNewValue();
			cpuBuss.addTrap(newValue, Trap.DEBUG);
			loadList();
		}// valueChanged

		/* MouseListener */

		@Override
		public void mouseClicked(MouseEvent mouseEvent) {
			if (mouseEvent.getClickCount() > 1)
				setProgramCounter(programCounter);
		}// mouseClicked

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
		}//

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
		}//

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
		}//

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
		}//

	}// class AdapterDebug
}// class DebugFrame
