package ioSystem.listDevice;
/*
 * 2019-09-07 Fixed MyPrefs problem...in Generic Printer also
 */

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;

import codeSupport.ASCII_CODES;
import codeSupport.AppLogger;
import ioSystem.DeviceZ80;
import utilities.filePicker.FilePicker;
import utilities.fontChooser.FontChooser;

public class GenericPrinter extends DeviceZ80{

	private ApplicationAdapter adapterApplication = new ApplicationAdapter();
	AppLogger log = AppLogger.getInstance();

	private int linesPerPage = 66;
	private int maxColumn;
	private boolean limitColumns;

	private int tabSize; // default for CP/M is 9
	private final JPanel contentPanel = new JPanel();
	private JTextArea textAreaList;
	private Document doc;
	private JFrame frameLST;
	// private Path newListingPath = Paths.get("."); // location to save listing to file

	public void run() {
		long delay = 5;
		while (true) {
			if (statusFromCPU.size() > 0) {
				statusFromCPU.poll();
				statusToCPU.offer(STATUS_RESPONSE);
			} // if Status request
			while (dataFromCPU.size() != 0) {
				byteFromCPU(dataFromCPU.poll());
			} // while
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				log.errorf("Timeout - Generic Printer%n");
			} // try
		} // while

	}// run

	public void lineFeed() {
		displayPrintable(Character.toString(ASCII_CODES.LF));
	}// lineFeed

	public void formFeed() {
		// Element rootElement = doc.getDefaultRootElement();
		int lineCount = (doc.getDefaultRootElement().getElementCount() - 1);
		int linesToSkip = linesPerPage - (lineCount % linesPerPage);

		for (int i = 0; i < linesToSkip; i++) {
			lineFeed();
		} // for

		log.infof("lineCount = %d, lines to skip = %d%n", lineCount, linesToSkip);
	}// formFeed

	@Override
	public void byteFromCPU(Byte value) {
		char c = (char) ((byte) value);

		if (c < 0X20) {
			switch (c) {
			case ASCII_CODES.TAB:
				doTab();
				break;
			case ASCII_CODES.LF: // 0X0A:
				display(Character.toString(c));
				break;
			case ASCII_CODES.CR: // ignore CR
				// Ignore CR
				break;
			default:
				// Ignore
				break;
			}// switch

		} else if ((c >= 0X20) && (c <= 0X7F)) {
			// Printable characters
			displayPrintable(Character.toString(c));
		} else {
			// above ASCII
		} // if

	}// byteFromCPU

	private void doTab() {
		Element lastElement = getLastElement();
		int column = lastElement.getEndOffset() - lastElement.getStartOffset();
		int numberOfSpaces = tabSize - (column % tabSize);
		for (int i = 0; i < numberOfSpaces; i++) {
			displayPrintable(SPACE);
		} // for

	}// doTab

	@Override
	public void byteToCPU(Byte value) {
		// Printers do not send data to the CPU
	}// byteToCPU

	public void doProperties() {
		ListDevicePropertyDialog listDevicePropertyDialog = new ListDevicePropertyDialog(textAreaList);

		if (listDevicePropertyDialog.showDialog() == JOptionPane.OK_OPTION) {
			loadProperties();
		} // if

		listDevicePropertyDialog = null;

	}// showProperties

	private void loadProperties() {
		Preferences myPrefs = getMyPrefs();
		tabSize = myPrefs.getInt("tabSize", 9); // default for CP/M

		maxColumn = (myPrefs.getInt("maxColumns", 80));
		limitColumns = myPrefs.getBoolean("limitColumns", false);
		int style = FontChooser.getStyleFromText(myPrefs.get("fontStyle", "Plain"));

		Font newFont = new Font(myPrefs.get("fontFamily", "Courier New"), style, myPrefs.getInt("fontSize", 13));
		textAreaList.setFont(newFont);
		myPrefs = null;
	}// loadProperties

	private void displayPrintable(String s) {
		Element lastElement = getLastElement();

		if (!limitColumns) {// drop anything beyond the max column ??
			display(s);
		} else if ((lastElement.getEndOffset() - lastElement.getStartOffset()) < this.maxColumn) {
			display(s);
		} // if
		textAreaList.setCaretPosition(doc.getDefaultRootElement().getEndOffset() - 1);
	}// displayPrintable

	private Element getLastElement() {
		Element rootElement = doc.getDefaultRootElement();
		return rootElement.getElement(rootElement.getElementCount() - 1);
	}// getLastElement

	private void display(String s) {
		// Element[] elements = doc.getRootElements();
		try {
			doc.insertString(doc.getLength(), s, null);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}// display

	public void clear() {
		clearDoc();
	}// clear

	private void clearDoc() {
		try {
			doc.remove(0, doc.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}// clearDoc

	public void doPrint() {
		String headerString = JOptionPane.showInputDialog(SwingUtilities.getRoot(textAreaList),
				"Input header (optional)\n Canel for no header");
		Font originalFont = textAreaList.getFont();
		try {
			textAreaList.setFont(originalFont.deriveFont(originalFont.getSize2D() * 0.75f));
			if (headerString == null) {
				textAreaList.print();
			} else {
				MessageFormat header = new MessageFormat(headerString);
				MessageFormat footer = new MessageFormat(new Date().toString() + "           Page - {0}");
				textAreaList.print(header, footer);
			} // if
			textAreaList.setFont(originalFont);
		} catch (PrinterException e) {
			e.printStackTrace();
		} // try
	}// print

	public void doSaveToFile() {
		JFileChooser fc = FilePicker.getPrinterOutput();
		if (fc.showSaveDialog(frameLST) == JFileChooser.CANCEL_OPTION) {
			System.out.println("Bailed out of the open");
			return;
		} // if - open
		// newListingPath = Paths.get(fc.getSelectedFile().getParent());

		String listingFile = fc.getSelectedFile().getAbsolutePath();

		ElementIterator elementIterator = new ElementIterator(doc.getDefaultRootElement());

		try {
			FileWriter fileWriter = new FileWriter(listingFile);
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			Element aLine = elementIterator.next();
			aLine = elementIterator.next(); // skip the rootElement
			int start, end;
			String theLine;
			while (aLine != null) {
				start = aLine.getStartOffset();
				end = aLine.getEndOffset() - 1;
				theLine = doc.getText(start, end - start); // Strip LF
				bufferedWriter.write(theLine + System.lineSeparator());
				// System.out.printf("start = %d, end = %d, text = %s%n", start, end, doc.getText(start, end - start));
				aLine = elementIterator.next();
			} // while
			bufferedWriter.close();

		} catch (Exception e1) {
			e1.printStackTrace();
			return;
		} // try

	}// saveToFile

	@Override
	public void setVisible(boolean state) {
		frameLST.setVisible(state);
	}// setVisible

	@Override
	public boolean isVisible() {
		return frameLST.isVisible();
	}// isVisible

	//////////////////////////////////////////////////////////
	public void close() {
		appClose();
	}// close

	public Preferences getMyPrefs() {
		return Preferences.userNodeForPackage(GenericPrinter.class).node("GenericPrinter");
	}//getMyPrefs
	
	private void appInit() {
		Preferences myPrefs = getMyPrefs();
		frameLST.setSize(myPrefs.getInt("Width", 761), myPrefs.getInt("Height", 693));
		frameLST.setLocation(myPrefs.getInt("LocX", 100), myPrefs.getInt("LocY", 100));
		myPrefs = null;
		
		loadProperties();
		doc = textAreaList.getDocument();
		frameLST.setVisible(true);
	}// appInit

	private void appClose() {
		Preferences myPrefs = getMyPrefs();
		Dimension dim = frameLST.getSize();
		myPrefs.putInt("Height", dim.height);
		myPrefs.putInt("Width", dim.width);
		Point point = frameLST.getLocation();
		myPrefs.putInt("LocX", point.x);
		myPrefs.putInt("LocY", point.y);

		myPrefs.putInt("tabSize", tabSize);

		myPrefs.putInt("maxColumns", maxColumn);
		myPrefs.putBoolean("limitColumns", limitColumns);

		Font currentFont = textAreaList.getFont();
		String currentStyle = "Plain";
		;
		switch (currentFont.getStyle()) {
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

		myPrefs.put("fontFamily", currentFont.getFamily());
		myPrefs.put("fontStyle", currentStyle);
		myPrefs.put("fontSize", Integer.toString(currentFont.getSize()));

		myPrefs = null;
	}// appClose

	/* @formatter:off */
	public GenericPrinter(String name,Byte addressIn,Byte addressOut,Byte addressStatus) {
		super(name,addressIn,addressOut,addressStatus);
		initialize();
		appInit();
	}// Constructor
/* @formatter:on  */

	public void initialize() {
		frameLST = new JFrame();
		frameLST.setTitle(" Z80   List Device");
		frameLST.setBounds(100, 100, 575, 492);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 40, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		frameLST.getContentPane().setLayout(gridBagLayout);

		GridBagConstraints gbc_contentPanel = new GridBagConstraints();
		gbc_contentPanel.fill = GridBagConstraints.BOTH;
		gbc_contentPanel.insets = new Insets(0, 10, 5, 5);
		gbc_contentPanel.gridx = 0;
		gbc_contentPanel.gridy = 0;
		frameLST.getContentPane().add(contentPanel, gbc_contentPanel);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 0, 0 };
		gbl_contentPanel.rowHeights = new int[] { 0, 0 };
		gbl_contentPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		contentPanel.add(scrollPane, gbc_scrollPane);

		textAreaList = new JTextArea();
		textAreaList.setEditable(false);
		scrollPane.setViewportView(textAreaList);

		JPanel panelButtons = new JPanel();
		GridBagConstraints gbc_panelButtons = new GridBagConstraints();
		gbc_panelButtons.insets = new Insets(5, 0, 5, 10);
		gbc_panelButtons.fill = GridBagConstraints.BOTH;
		gbc_panelButtons.gridx = 1;
		gbc_panelButtons.gridy = 0;
		frameLST.getContentPane().add(panelButtons, gbc_panelButtons);
		GridBagLayout gbl_panelButtons = new GridBagLayout();
		gbl_panelButtons.columnWidths = new int[] { 100, 0 };
		gbl_panelButtons.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelButtons.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panelButtons.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		panelButtons.setLayout(gbl_panelButtons);

		Component verticalStrut = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut.gridx = 0;
		gbc_verticalStrut.gridy = 0;
		panelButtons.add(verticalStrut, gbc_verticalStrut);

		JButton btnLineFeed = new JButton("Line Feed");
		btnLineFeed.setName(BTN_LINE_FEED);
		btnLineFeed.addActionListener(adapterApplication);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 1;
		panelButtons.add(btnLineFeed, gbc_btnNewButton);

		Component verticalStrut1 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut1 = new GridBagConstraints();
		gbc_verticalStrut.fill = GridBagConstraints.HORIZONTAL;
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut.gridx = 0;
		gbc_verticalStrut.gridy = 2;
		panelButtons.add(verticalStrut1, gbc_verticalStrut1);

		JButton btnFormFeed = new JButton("Form Feed");
		btnFormFeed.setName(BTN_FORM_FEED);
		btnFormFeed.addActionListener(adapterApplication);
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_1.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton_1.gridx = 0;
		gbc_btnNewButton_1.gridy = 3;
		panelButtons.add(btnFormFeed, gbc_btnNewButton_1);

		Component verticalStrut2 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut2 = new GridBagConstraints();
		gbc_verticalStrut.fill = GridBagConstraints.HORIZONTAL;
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut.gridx = 0;
		gbc_verticalStrut.gridy = 4;
		panelButtons.add(verticalStrut2, gbc_verticalStrut2);

		JButton btnSaveToFile = new JButton("Save to File ...");
		btnSaveToFile.setName(BTN_SAVE_TO_FILE);
		btnSaveToFile.addActionListener(adapterApplication);
		GridBagConstraints gbc_btnNewButton_2 = new GridBagConstraints();
		gbc_btnNewButton_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_2.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton_2.gridx = 0;
		gbc_btnNewButton_2.gridy = 5;
		panelButtons.add(btnSaveToFile, gbc_btnNewButton_2);

		Component verticalStrut3 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut3 = new GridBagConstraints();
		gbc_verticalStrut.fill = GridBagConstraints.HORIZONTAL;
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut.gridx = 0;
		gbc_verticalStrut.gridy = 6;
		panelButtons.add(verticalStrut3, gbc_verticalStrut3);

		JButton btnPrint = new JButton("Print ...");
		btnPrint.setName(BTN_PRINT);
		btnPrint.addActionListener(adapterApplication);
		GridBagConstraints gbc_btnNewButton_3 = new GridBagConstraints();
		gbc_btnNewButton_3.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_3.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton_3.gridx = 0;
		gbc_btnNewButton_3.gridy = 7;
		panelButtons.add(btnPrint, gbc_btnNewButton_3);

		Component verticalStrut4 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut4 = new GridBagConstraints();
		gbc_verticalStrut.fill = GridBagConstraints.HORIZONTAL;
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut.gridx = 0;
		gbc_verticalStrut.gridy = 8;
		panelButtons.add(verticalStrut4, gbc_verticalStrut4);

		JButton btnProperties = new JButton("Properties ...");
		btnProperties.setName(BTN_PROPERTIES);
		btnProperties.addActionListener(adapterApplication);
		GridBagConstraints gbc_btnNewButton_4 = new GridBagConstraints();
		gbc_btnNewButton_4.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_4.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton_4.gridx = 0;
		gbc_btnNewButton_4.gridy = 9;
		panelButtons.add(btnProperties, gbc_btnNewButton_4);

		Component verticalStrut5 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut5 = new GridBagConstraints();
		gbc_verticalStrut.fill = GridBagConstraints.HORIZONTAL;
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut.gridx = 0;
		gbc_verticalStrut.gridy = 10;
		panelButtons.add(verticalStrut5, gbc_verticalStrut5);

		JButton btnClear = new JButton("Clear");
		btnClear.setName(BTN_CLEAR);
		btnClear.addActionListener(adapterApplication);
		GridBagConstraints gbc_btnNewButton_5 = new GridBagConstraints();
		gbc_btnNewButton_5.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton_5.gridx = 0;
		gbc_btnNewButton_5.gridy = 11;
		panelButtons.add(btnClear, gbc_btnNewButton_5);

		JPanel buttonPane = new JPanel();
		buttonPane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_buttonPane = new GridBagConstraints();
		gbc_buttonPane.gridwidth = 2;
		gbc_buttonPane.anchor = GridBagConstraints.SOUTH;
		gbc_buttonPane.fill = GridBagConstraints.HORIZONTAL;
		gbc_buttonPane.gridx = 0;
		gbc_buttonPane.gridy = 1;
		frameLST.getContentPane().add(buttonPane, gbc_buttonPane);
		buttonPane.setLayout(new GridLayout(1, 0, 0, 0));

	}// Initialize

	private static final String SPACE = " "; // Space

	public static final Byte IN = null;
	public static final Byte OUT = (byte) 0X010;
	public static final Byte STATUS = (byte) 0X011;
	public static final Byte STATUS_RESPONSE = (byte) 0XFF;

	private static final String BTN_LINE_FEED = "btnLineFeed";
	private static final String BTN_FORM_FEED = "btnFormFeed";
	private static final String BTN_SAVE_TO_FILE = "btnSaveToFile";
	private static final String BTN_PRINT = "btnPrint";
	private static final String BTN_PROPERTIES = "btnProperties";
	private static final String BTN_CLEAR = "btnClear";

	class ApplicationAdapter implements ActionListener {
		/* ActionListener */
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String name = ((Component) actionEvent.getSource()).getName();
			switch (name) {
			case BTN_LINE_FEED:
				lineFeed();
				break;
			case BTN_FORM_FEED:
				formFeed();
				break;
			case BTN_SAVE_TO_FILE:
				doSaveToFile();
				break;
			case BTN_PRINT:
				doPrint();
				break;
			case BTN_PROPERTIES:
				doProperties();
				break;
			case BTN_CLEAR:
				clearDoc();
				break;

			}// switch

		}// actionPerformed

	}// class ApplicationAdapter

}// class ListDevice
