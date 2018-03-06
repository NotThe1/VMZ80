package utilities.hexEdit;

import java.util.SortedMap;
import java.util.Vector;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

/**
 * used for hexEdit. it constructs a set of tables to control what will be
 * placed in the document based on which doc column the replace references. It
 * skips the part of the line that is used for the address. In the data section
 * it will allow only hex digits. In the ascii section it will accept any byte
 * value and show its printable representation.
 * 
 * It will perform mutual updates of the corresponding data and ascii sections.
 * 
 * The last changes original data are recorded in a SortedMap and applied only
 * in the document, not in the source These changes are available to
 * constructing classes in the - SortedMap<Integer, Byte> changes- argument of
 * the constructor
 * 
 * @author Frank Martyn
 *
 */
public class HexEditDocumentFilter extends DocumentFilter {

	private StyledDocument doc;
	private int row; // , column, elementPosition;
	private int addressSize;

	private Integer[] columnTypeTable;
	private Integer[] dataToAsciiTable;
	private Integer[] sourceColumnTable;

	private AttributeSet attrData;
	private AttributeSet attrASCII;

	private HexEditMetrics hexMetrics;
	private SortedMap<Integer, Byte> changes;
	// private boolean innerFlag = false;
	

	public HexEditDocumentFilter(StyledDocument doc, HexEditMetrics hexMetrics, SortedMap<Integer, Byte> changes) {
		this.doc = doc;
		this.hexMetrics = hexMetrics;
		this.changes = changes;

		this.attrData = null;
		this.attrASCII = null;

		this.addressSize = hexMetrics.getAddressSize();
		appInit();

	}// Constructor

	public void setDataAttributes(AttributeSet attrData) {
		this.attrData = attrData;
	}// setDataAttributes

	public void setAsciiAttributes(AttributeSet attrASCII) {
		this.attrASCII = attrASCII;
	}// setDataAttributes

	private void appInit() {
		columnTypeTable = makeColumnTable();
		dataToAsciiTable = makeDataToAsciiTable();
		sourceColumnTable = makeSourceColumnTable();
	}// appInit

	public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr)
			throws BadLocationException {
		System.out.printf("[insertString]\tattr: %s \toffset: %4d , string: %s %n", attr.toString(), offset, string);
		fb.insertString(offset, string, attr);
	}// insertString

	public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
		System.out.printf("[remove]\toffset: %d ,length: %d, %n", offset, length);
		// Disable the delete function
	}// remove

	@SuppressWarnings("unused")
	public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
			throws BadLocationException {
		// System.out.printf("[replace]\t offset: %d ,length: %d, text: %s %n",
		// offset, length, text);

		// length of 0 equals a insert length of 1 equals a replace
		Integer columnPosition = getColumnPosition(offset);
		if (columnPosition == null) {
			return;
		} // if - do nothing

		int netLength = length == 0 ? 1 : length;

		int docOffset = 0;
		Integer sourceColumn = sourceColumnTable[columnPosition];

		Integer columnType = columnTypeTable[columnPosition];

		byte newValue = 0;

		// System.out.printf("replace:: position: offset: %d, netLength: %d,
		// text: %s, columnType: %d, innerFlag %s%n",
		// offset, netLength, text, columnType, innerFlag);

		Integer newCharacterIndex = dataToAsciiTable[columnPosition];
		;

		if (columnType == null) {
			return;
		} // if null columnType

		switch (columnType) {

		case HEX1:
		case HEX2:
			if (!isTextHex(text)) {
				return;
			} // if - want only hex characters
			text = text.toUpperCase();

			fb.replace(offset, netLength, text, attrs);

			int targetOffset = (columnType == HEX1) ? offset : offset - 1;
			// String newChar = convertToPrintable(targetOffset);
			int value = getValueAtOffset(targetOffset);
			String newChar = convertToPrintable1(value);
			newValue = (byte) (value & 0XFF);
			docOffset = offset + newCharacterIndex;

			fb.replace(docOffset, 1, newChar, attrASCII);
			break;
		case ASCII:
			String displayText = isTextPrintable(text)? text:NON_PRINTABLE_CHAR;
			fb.replace(offset, netLength, displayText, attrs);

			byte[] hexValues = text.getBytes();
			newValue = hexValues[0];
			String hexData = String.format("%02X", newValue);
			docOffset = offset + newCharacterIndex;

			fb.replace(docOffset, 2, hexData, attrData);
			break;
		default:
			return;
		}// switch - columnType

		recordTheChange(offset,sourceColumn,newValue);
		
		// if (changes.size() != 0) {
		// System.out.printf("[replace]: %n");
		// changes.forEach((k, v) -> System.out.printf("\t\tIndex = %4d, vlaue =
		// %02X%n", k, v));
		// } // if need to update

	}// replace
	private void recordTheChange(int offset,int sourceColumn,byte value){
		int location = getSourceIndex(offset, sourceColumn);
		changes.put(location, value);
		fireHexSourceChange(location, value);
	}//recordTheChange

	private int getSourceIndex(int docPosition, int columnIndex) {
		int ans = 0;
		Element rowElement = doc.getParagraphElement(docPosition);
		try {
			String addressStr = doc.getText(rowElement.getStartOffset(), this.addressSize);
			ans = columnIndex + Integer.valueOf(addressStr, 16);
		} catch (BadLocationException e) {
			e.printStackTrace();
		} // try
		return ans;
	}// getSourceIndex

	private int getColumnPosition(int offset) {
		Element rootElement = doc.getDefaultRootElement();
		row = rootElement.getElementIndex(offset);
		Element paragraphElement = rootElement.getElement(row);
		return offset - paragraphElement.getStartOffset();
	}// getColumnPosition

	private int getValueAtOffset(int offset) {
		String HexString = "";
		try {
			HexString = doc.getText(offset, 2).trim();
		} catch (BadLocationException e) {
			e.printStackTrace();
		} // try
		return Integer.valueOf(HexString, 16);
	}// getValueAtOffset

	private String convertToPrintable1(int value) {
		char[] c = new char[1];
		c[0] = (char) (value & 0XFF);
		return ((c[0] >= 0X20) && (c[0] <= 0X7F)) ? new String(c) : NON_PRINTABLE_CHAR;
	}// convertToPrintable1

	private boolean isTextHex(String text) {
		return text.matches(PATTERN_HEX);
	}// isTextHex

	private boolean isTextPrintable(String text) {
		return text.matches(PATTERN_PRINTABLE);
	}// isTextPrintable

	private Integer[] makeColumnTable() {
		Integer[] ans = new Integer[hexMetrics.getAsciiEnd() + 4];
		int colPosition = 0;
		for (; colPosition < hexMetrics.getAddressEnd() + 1; colPosition++) {
			ans[colPosition] = null;
		} // for Address

		for (; colPosition < hexMetrics.getDataStart(); colPosition++) {
			ans[colPosition] = null;
		} // for colon and Address PAD

		// ans[colPosition++] = BLANK; // make space after colon move the cursor

		Integer[] dataKinds = new Integer[] { HEX1, HEX2, null, HEX1, HEX2, null, HEX1, HEX2, null, HEX1, HEX2, null,
				HEX1, HEX2, null, HEX1, HEX2, null, HEX1, HEX2, null, HEX1, HEX2, null, null, HEX1, HEX2, null, HEX1,
				HEX2, null, HEX1, HEX2, null, HEX1, HEX2, null, HEX1, HEX2, null, HEX1, HEX2, null, HEX1, HEX2, null,
				HEX1, HEX2, };
		System.arraycopy(dataKinds, 0, ans, colPosition, dataKinds.length);
		colPosition += dataKinds.length;

		for (; colPosition < hexMetrics.getAsciiStart(); colPosition++) {
			ans[colPosition] = null;
		} // for Data PAD

		Integer[] asciiKinds = new Integer[] { ASCII, ASCII, ASCII, ASCII, ASCII, ASCII, ASCII, ASCII, ASCII, ASCII,
				ASCII, ASCII, ASCII, ASCII, ASCII, ASCII, null, null };
		System.arraycopy(asciiKinds, 0, ans, colPosition, asciiKinds.length);

		return ans;
	}// makeColumnTable

	/**
	 * dataToAsciiTable maps the doc index relationship between the hex and
	 * ASCII representation of the data on a line
	 * 
	 * @return dataToAsciiTable
	 */

	private Integer[] makeDataToAsciiTable() {
		Integer[] ans = new Integer[hexMetrics.getAsciiEnd() + 2];
		int colPosition = 0;
		for (; colPosition < hexMetrics.getAddressBlockEnd(); colPosition++) {
			ans[colPosition] = null;
		} // for Address

		int gap = hexMetrics.getDataBlockEnd() - hexMetrics.getDataStart() + 1;
		Integer[] dataToASCII = new Integer[] { null, gap - 0, gap - 1, null, gap - 2, gap - 3, null, gap - 4, gap - 5,
				null, gap - 6, gap - 7, null, gap - 8, gap - 9, null, gap - 10, gap - 11, null, gap - 12, gap - 13,
				null, gap - 14, gap - 15, null, null, gap - 17, gap - 18, null, gap - 19, gap - 20, null, gap - 21,
				gap - 22, null, gap - 23, gap - 24, null, gap - 25, gap - 26, null, gap - 27, gap - 28, null, gap - 29,
				gap - 30, null, gap - 31, gap - 32, null };

		for (int i = 0; i < dataToASCII.length - 1; i++) {
			ans[i + colPosition] = dataToASCII[i];
		} // for dataToASCII

		colPosition += dataToASCII.length;
		Integer[] asciiToData = new Integer[] { null, null, -51, -49, -47, -45, -43, -41, -39, -37, -34, -32, -30, -28,
				-26, -24, -22, -20 };
		for (int j = 0; j < asciiToData.length; j++) {
			ans[j + colPosition] = asciiToData[j];
		} // for asciiToData

		return ans;
	}// makeDataToAsciiTable

	/**
	 * sourceColumnTable has the values used to map the doc column to the source
	 * index by giving the index to add to the lines address
	 * 
	 * @return sourceColumnTable
	 */

	private Integer[] makeSourceColumnTable() {
		Integer[] ans = new Integer[] { null, null, null, null, null, null, null, 0, 0, null, 1, 1, null, 2, 2, null, 3,
				3, null, 4, 4, null, 5, 5, null, 6, 6, null, 7, 7, null, null, 8, 8, null, 9, 9, null, 10, 10, null, 11,
				11, null, 12, 12, null, 13, 13, null, 14, 14, null, 15, 15, null, null, null, 0, 1, 2, 3, 4, 5, 6, 7, 8,
				9, 10, 11, 12, 13, 14, 15, null, null };
		return ans;
	}// makeSourceColumnTable
	
	//---------------------------------------------------
	//---------------------------------------------------
	private Vector<HexSourceChangeListener> hexSourceChangeListeners = new Vector<HexSourceChangeListener>();
	
	public synchronized void addHexSourceChangeListener(HexSourceChangeListener hexSourceChangeListener){
		if (hexSourceChangeListeners.contains(hexSourceChangeListener)){
			return; 	// already on list
		}//if
		hexSourceChangeListeners.add(hexSourceChangeListener);
	}//addHexSourceChangeListener
	
	public synchronized void removeHexSourceChangeListener(HexSourceChangeListener hexSourceChangeListener){
		hexSourceChangeListeners.remove(hexSourceChangeListener);
	}//removeHexSourceChangeListener
	
	private void fireHexSourceChange(int location, byte value){
		if(hexSourceChangeListeners.size() == 0){
			return;		// no listeners
		}
		Vector<HexSourceChangeListener> workingSourceChangeListeners;
		synchronized (this){
			workingSourceChangeListeners =(Vector<HexSourceChangeListener>) hexSourceChangeListeners.clone();
			HexSourceChangeEvent hexSourceChangeEvent = new HexSourceChangeEvent(this,location,value);
			
			for(HexSourceChangeListener listener:hexSourceChangeListeners){
				listener.dataChanged(hexSourceChangeEvent);
			}//for
		}//sync
	}//fireHexSourceChange
	
	//---------------------------------------------------
	//---------------------------------------------------
	
	

	private final static String PATTERN_HEX = "[A-F|a-f|0-9]+";
	private static String PATTERN_PRINTABLE = "^([a-zA-Z0-9!@#$%^&amp;*()-_=+;:'&quot;|~`&lt;&gt;?/{}]{1,1})$";

	private final static String NON_PRINTABLE_CHAR = ".";
	// private final static String TAB = "\t";

	private final static int HEX1 = 1;
	private final static int HEX2 = 2;
	private final static int ASCII = 3;

}// class HexEditDocumentFilter
