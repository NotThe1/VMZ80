package utilities.hexEdit;

// Known problem - if last line has more than two bytes displayed,

// entering data from the ASCII data on the last full line will skip the first two bytes of the last line.
// You can back arrow to them if needed.

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.BoundedRangeModel;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public abstract class HexEditPanelBase extends JPanel
		implements AdjustmentListener, ComponentListener, ChangeListener, Runnable {
	private static final long serialVersionUID = 1L;

	protected ByteBuffer source;
	private int addressSize;

	protected int currentLineStart;
	private int currentMax;
	private int currentExtent;

	protected StyledDocument doc;
	private HexEditDocumentFilter hexFilter;
	private HexEditNavigationFilter hexNavigationFilter;
	private HexEditMetrics hexMetrics;

	private String addressFormat;
	private String dataFormat = "%-" + ((BYTES_PER_LINE * 3) + 2) + "s";
	private String hexCharacterFormat = "%02X ";

	private SimpleAttributeSet addressAttributes;
	private SimpleAttributeSet dataAttributes;
	private SimpleAttributeSet asciiAttributes;

	private JTextPane textPane;
	private JScrollBar scrollBar;
	private JLabel lblDocHeader;

	protected SortedMap<Integer, Byte> changes;

	// ---------------------------------------------------------------
	public abstract void loadData(Object source);

	public boolean isDataChanged() {
		return !changes.isEmpty();
	}// isDataChanges

	public SortedMap<Integer, Byte> getChangedData() {
		return changes;
	}// getChangedData

	public void run() {
		fillPane();
	}// run

	// -----------------------------------------------------------------------------------------------
	HexEditDocumentFilter loadDataCommon(int sourceSize) {
		changes.clear();
		setUpScrollBar();
		currentLineStart = 0;
		prepareDoc(doc, (long) sourceSize);

//		fillPane();
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fillPane();
			}// run
		});
		
		calcHexMetrics(sourceSize);
		setNavigationFilter(doc);
		HexEditDocumentFilter hexDocumentFilter = setDocumentFilter(doc);

		return hexDocumentFilter;
	}// loadDataCommon

	void fillPane() {
		if (currentExtent == 0) {
			return;
		} // if nothing to display

		clearFilters(); // suspend doc filter
		try {
			doc.remove(0, doc.getLength());
		} catch (BadLocationException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} // try

		int sourceIndex = currentLineStart * BYTES_PER_LINE; // address to
																// display
		byte[] activeLine = new byte[BYTES_PER_LINE];
		int linesToDisplay = Math.min(currentExtent, currentMax - currentLineStart);

		int bytesToRead = BYTES_PER_LINE;
		source.position(sourceIndex);
		for (int i = 0; i < linesToDisplay; i++) {
			source.get(activeLine, 0, bytesToRead);
			byte[] processedData = applyChanges(activeLine, bytesToRead, sourceIndex);
			processLine(processedData, bytesToRead, sourceIndex);
			sourceIndex += bytesToRead;
			if (bytesToRead < BYTES_PER_LINE) {
				// leave withe byte count for the last sector set in bytesToRead
				break;
			} // if
			bytesToRead = Math.min(source.remaining(), BYTES_PER_LINE);
			if (bytesToRead == 0) {
				bytesToRead = BYTES_PER_LINE;
				break;
			} // if
		} // for
		restoreFilters();
		hexNavigationFilter.setLastLine(bytesToRead, linesToDisplay - 1);
		textPane.setCaretPosition(0);
	}// fillPane

	protected byte[] applyChanges(byte[] rawData, int bytesRead, int bufferAddress) {
		byte[] ans = rawData.clone();
		SortedMap<Integer, Byte> rowChanges = changes.subMap(bufferAddress, bufferAddress + bytesRead);

		if (rowChanges.size() != 0) {
			rowChanges.forEach((k, v) -> ans[(int) k - bufferAddress] = (byte) v);
		} // if need to update
		return ans;
	}

	private int processLine(byte[] rawData, int bytesRead, int bufferAddress) {//
		StringBuilder sbData = new StringBuilder();
		for (int i = 0; i < bytesRead; i++) {
			if ((i % 8) == 0) {
				sbData.append(SPACE);
			} // if data extra space
			sbData.append(String.format(hexCharacterFormat, rawData[i]));
		} // for

		String bufferAddressStr = String.format(addressFormat, bufferAddress);
		String dataStr = String.format(dataFormat, sbData.toString());
		String asciiStr = getASCII(rawData, bytesRead);

		try {
			doc.insertString(doc.getLength(), bufferAddressStr, addressAttributes);
			doc.insertString(doc.getLength(), dataStr, dataAttributes);
			doc.insertString(doc.getLength(), asciiStr, asciiAttributes);
		} catch (BadLocationException e) {
			e.printStackTrace();
		} // try
		return bufferAddress + bytesRead;
	}// processLine

	private String getASCII(byte[] rawData, int size) {
		StringBuilder sbASCII = new StringBuilder(ASCII_DATA_SEPARATOR);
		char c;
		for (int i = 0; i < size; i++) {
			c = (char) rawData[i];
			sbASCII.append((c >= 0X20) && (c <= 0X7F) ? c : NON_PRINTABLE_CHAR);
		} // for
		sbASCII.append(System.lineSeparator());
		return sbASCII.toString();
	}// getASCII

	private void setFormats(long addSize) {
		adjustAddressSize(addSize);
		addressFormat = "%0" + this.addressSize + "X: ";
	}// setFormats

	private void adjustAddressSize(long size) {
		this.addressSize = ADDRESS_4;
		int maxValue = ADDRESS_4_MAX;

		if (size <= ADDRESS_4_MAX) {
			maxValue = ADDRESS_4_MAX;
			this.addressSize = ADDRESS_4;
		} else if (size <= ADDRESS_6_MAX) {
			maxValue = ADDRESS_6_MAX;
			this.addressSize = ADDRESS_6;
		} else if (size <= ADDRESS_8_MAX) {
			maxValue = ADDRESS_8_MAX;
			this.addressSize = ADDRESS_8;
		} else {
			maxValue = 0XFFFF;
			this.addressSize = ADDRESS_4;
		} // if fileSize

		adjustHeaderAndSpinner(maxValue);

	}// adjustAddressSize

	private void adjustHeaderAndSpinner(int maxValue) {
		byte[] address = new byte[] { (byte) 0X3A, (byte) 0X20, (byte) 0X20, (byte) 0X00, (byte) 0X00, (byte) 0X00,
				(byte) 0X00, (byte) 0X00, (byte) 0X00, (byte) 0X00, (byte) 0X00 };

		Font font = lblDocHeader.getFont();
		int width = lblDocHeader.getFontMetrics(font).bytesWidth(address, 0, this.addressSize + 2);

		Rectangle recSpinner = spinnerAddress.getBounds();
		recSpinner.width = width;
		spinnerAddress.setBounds(recSpinner);

		int x = recSpinner.x + recSpinner.width + lblDocHeader.getFontMetrics(font).bytesWidth(address, 0, 1);
		Rectangle recLabel = lblDocHeader.getBounds();
		recLabel.x = x;
		lblDocHeader.setBounds(recLabel);

		spinnerAddress.updateUI();
		lblDocHeader.updateUI();

		SpinnerNumberModel model = new SpinnerNumberModel(0, 0, maxValue - 1, 1);
		spinnerAddress.setModel(model);
		JSpinner.DefaultEditor editor = (DefaultEditor) spinnerAddress.getEditor();
		JFormattedTextField ftf = editor.getTextField();
		ftf.setFormatterFactory(new MyFormatterFactory(this.addressSize));

	}// adjustHeaderAndSpinner

	protected void prepareDoc(StyledDocument doc, long srcSize) {
		setFormats(srcSize);
		clearFilters();
		// clearDocument(doc);
	}// prepareDoc

	// private void resetDocumentFilter(StyledDocument doc) {
	// ((AbstractDocument) doc).setDocumentFilter(null);
	// }// resetDocumentFilter

	// protected void setDocumentFilter(StyledDocument doc) {
	//
	// hexFilter = new HexEditDocumentFilter(doc, hexMetrics, changes);
	// hexFilter.setAsciiAttributes(asciiAttributes);
	// hexFilter.setDataAttributes(dataAttributes);
	// // hexFilter = null;
	// ((AbstractDocument) doc).setDocumentFilter(hexFilter);
	// }// setDocumentFilter

	protected HexEditDocumentFilter setDocumentFilter(StyledDocument doc) {

		hexFilter = new HexEditDocumentFilter(doc, hexMetrics, changes);
		hexFilter.setAsciiAttributes(asciiAttributes);
		hexFilter.setDataAttributes(dataAttributes);
		// hexFilter = null;
		((AbstractDocument) doc).setDocumentFilter(hexFilter);
		return hexFilter;
	}// setDocumentFilter

	// private void resetNavigationFilter() {
	// textPane.setNavigationFilter(null);
	// }// resetDocumentFilter

	private void clearFilters() {
		((AbstractDocument) doc).setDocumentFilter(null);
		textPane.setNavigationFilter(null);
	}// clearFilters

	private void restoreFilters() {
		((AbstractDocument) doc).setDocumentFilter(hexFilter);
		textPane.setNavigationFilter(hexNavigationFilter);
	}// restoreFilters

	protected void setNavigationFilter(StyledDocument doc) {
		hexNavigationFilter = new HexEditNavigationFilter(doc, hexMetrics);
		textPane.setNavigationFilter(hexNavigationFilter);
	}// resetDocumentFilter

	protected void calcHexMetrics(long sourceSize) {
		if (hexMetrics != null) {
			hexMetrics = null;
		} // if
		hexMetrics = new HexEditMetrics(sourceSize);
	}// calcHexMetrics

	private void setExtent(int amount, BoundedRangeModel model) {
		model.setExtent(amount);
		currentExtent = amount;
	}// setExtent

	private void setMin(int amount, BoundedRangeModel model) {
		model.setMinimum(amount);
	}// setMin

	private void setMax(int amount, BoundedRangeModel model) {
		model.setMaximum(amount);
		currentMax = amount;
	}// setMax

	private void setValue(int amount, BoundedRangeModel model) {
		model.setValue(amount);
	}// setValue

	protected void setUpScrollBar() {
		if (source == null) {
			return;
		} // if
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				int max = maximumNumberOfRows(textPane);
				int extent = calcExtent(textPane);

				BoundedRangeModel model = scrollBar.getModel();
				setMin(0, model);
				setMax(max, model);
				setValue(0, model);
				setExtent(extent, model);
				scrollBar.setBlockIncrement(extent - 2);
			}// run
		});
	}// setUpScrollBar

	private int calcUsableHeight(JTextPane thisPane) {
		Insets insets = thisPane.getInsets();
		Dimension dimension = thisPane.getSize();
		return dimension.height - (insets.bottom + insets.top);
	}// calcUsableHeight

	private int calcFontHeight(JTextPane thisPane) {
		Font font = textPane.getFont();
		return textPane.getFontMetrics(font).getHeight();
	}// calcFontHeight

	private int calcExtent(JTextPane thisPane) {
		int useableHeight = calcUsableHeight(thisPane);
		int fontHeight = calcFontHeight(thisPane);
		return (int) (useableHeight / fontHeight);
	}// calcExtent

	private int maximumNumberOfRows(JTextPane thisTextPane) {
		// return (source.limit() / BYTES_PER_LINE) + 1;
		return (source.capacity() / BYTES_PER_LINE) + 1;
	}// maximumNumberOfRows

	private void makeStyles() {
		SimpleAttributeSet baseAttributes = new SimpleAttributeSet();
		StyleConstants.setFontFamily(baseAttributes, "Courier New");
		StyleConstants.setFontSize(baseAttributes, 16);

		addressAttributes = new SimpleAttributeSet(baseAttributes);
		StyleConstants.setForeground(addressAttributes, Color.GRAY);

		dataAttributes = new SimpleAttributeSet(baseAttributes);
		StyleConstants.setForeground(dataAttributes, Color.BLACK);

		asciiAttributes = new SimpleAttributeSet(baseAttributes);
		StyleConstants.setForeground(asciiAttributes, Color.BLUE);

	}// makeStyles1
		// -------------------------------------------------------------------------------------------------------

	public HexEditPanelBase() {
		initialize();
		appInit();
	}// Constructor

	private void appInit() {
		doc = textPane.getStyledDocument();
		makeStyles();
		changes = new TreeMap<Integer, Byte>();
		scrollBar.addAdjustmentListener(this);
		textPane.addComponentListener(this);
	}// appInit

	private void initialize() {
		setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 780, 25 };
		gridBagLayout.rowHeights = new int[] { 25, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		panelHeader = new JPanel();
		panelHeader.setLayout(null);
		GridBagConstraints gbc_panelHeader = new GridBagConstraints();
		gbc_panelHeader.fill = GridBagConstraints.BOTH;
		gbc_panelHeader.insets = new Insets(0, 0, 5, 5);
		gbc_panelHeader.gridx = 0;
		gbc_panelHeader.gridy = 0;
		add(panelHeader, gbc_panelHeader);

		lblDocHeader = new JLabel();
		lblDocHeader.setBounds(74, 1, 480, 20);
		panelHeader.add(lblDocHeader);
		lblDocHeader.setText("00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F");
		lblDocHeader.setForeground(new Color(0, 100, 0));
		lblDocHeader.setHorizontalAlignment(SwingConstants.LEFT);
		lblDocHeader.setFont(new Font("Courier New", Font.BOLD, 16));

		spinnerAddress = new JSpinner();
		spinnerAddress.addChangeListener(this);
		spinnerAddress.setToolTipText("Tab out to move to new address");
		spinnerAddress.setAlignmentX(Component.LEFT_ALIGNMENT);
		spinnerAddress.setFont(new Font("Courier New", Font.PLAIN, 16));
		spinnerAddress.setBounds(1, 1, 72, 20);
		panelHeader.add(spinnerAddress);

		textPane = new JTextPane();
		// textPane.addComponentListener(this);

		textPane.setFont(new Font("Courier New", Font.PLAIN, 16));
		GridBagConstraints gbc_textPane = new GridBagConstraints();
		gbc_textPane.insets = new Insets(0, 0, 0, 5);
		gbc_textPane.fill = GridBagConstraints.BOTH;
		gbc_textPane.gridx = 0;
		gbc_textPane.gridy = 1;
		add(textPane, gbc_textPane);

		scrollBar = new JScrollBar();
		// scrollBar.addAdjustmentListener(this);
		scrollBar.setMaximum(0);
		scrollBar.setPreferredSize(new Dimension(25, 48));
		scrollBar.setMinimumSize(new Dimension(25, 5));
		scrollBar.setMaximumSize(new Dimension(25, 32767));
		GridBagConstraints gbc_scrollBar = new GridBagConstraints();
		gbc_scrollBar.fill = GridBagConstraints.VERTICAL;
		gbc_scrollBar.gridx = 1;
		gbc_scrollBar.gridy = 1;
		add(scrollBar, gbc_scrollBar);

	}// initialize

	@Override
	public void componentResized(ComponentEvent comonentEvent) {
		setUpScrollBar();
		// fillPane();
	}// componentResized

	@Override
	public void adjustmentValueChanged(AdjustmentEvent adjustmentEvent) {

		if (adjustmentEvent.getValueIsAdjusting()) {
			return;
		} // if
		if (adjustmentEvent.getAdjustmentType() != AdjustmentEvent.TRACK) {
			return;
		} // if

		currentLineStart = adjustmentEvent.getValue();
		spinnerAddress.setValue(BYTES_PER_LINE * currentLineStart);
		fillPane();
	}// adjustmentValueChanged

	public static final int BYTES_PER_LINE = 16;
	private static final String SPACE = " ";
	private static final String ASCII_DATA_SEPARATOR = SPACE + SPACE;
	private static final String NON_PRINTABLE_CHAR = ".";

	private static final int ADDRESS_4 = 4;
	private static final int ADDRESS_6 = 6;
	private static final int ADDRESS_8 = 8;

	private static final int ADDRESS_4_MAX = 0XFFFF; // FFFF 65535
	private static final int ADDRESS_6_MAX = 0XFFFFFF; // FF FFFF 16,777,215
	private static final int ADDRESS_8_MAX = Integer.MAX_VALUE; // 7FFF FFFF
																// 2,147,483,647

	private JPanel panelHeader;
	private JSpinner spinnerAddress;

	// ----------------------------not
	// implemented--------------------------------------------------------------
	@Override
	public void componentHidden(ComponentEvent arg0) {
		// Auto-generated method stub
	}// componentHidden

	@Override
	public void componentMoved(ComponentEvent arg0) {
		// Auto-generated method stub
	}// componentMoved

	@Override
	public void componentShown(ComponentEvent arg0) {
		// Auto-generated method stub
	}// componentShown

	// ----------------------------------------------------------------------------------------------------------
	// ----------------------------------------------------------------------------------------------------------
	// ----------------------------------------------------------------------------------------------------------
	private static class HexFormatter extends DefaultFormatter {
		private static final long serialVersionUID = 1L;
		private String addressFormat;

		public HexFormatter(int addressLength) {
			this.addressFormat = "%0" + addressLength + "X";
		}// Constructor

		public Object stringToValue(String text) throws ParseException {
			try {
				return Integer.valueOf(text, 16);
			} catch (NumberFormatException nfe) {
				throw new ParseException(text, 0);
			} // try
		}// stringToValue

		public String valueToString(Object value) throws ParseException {
			return String.format(this.addressFormat, value);
		}// valueToString
	}// class HexFormatter

	private static class MyFormatterFactory extends DefaultFormatterFactory {
		private static final long serialVersionUID = 1L;
		private int addressLength;

		public MyFormatterFactory(int addressLength) {
			this.addressLength = addressLength;
		}// Constructor

		public AbstractFormatter getDefaultFormatter() {
			return new HexFormatter(addressLength);
		}// getDefaultFormatter
	}// class MyFormatterFactory

	// ----------------------------------------------------------------------------------------------------------

	@Override
	public void stateChanged(ChangeEvent changeEvent) {
		if (!changeEvent.getSource().equals(spinnerAddress)) {
			return;
		} // if
		int value = (int) spinnerAddress.getValue();
		int targetValue = value / BYTES_PER_LINE;

		if (targetValue == (int) scrollBar.getValue()) {
			return; // same line
		} // if

		scrollBar.setValue(targetValue);

	}// stateChanged

}// class HexEditPanelSB
