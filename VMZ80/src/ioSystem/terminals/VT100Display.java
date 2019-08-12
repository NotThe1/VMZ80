package ioSystem.terminals;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

import codeSupport.AppLogger;

public class VT100Display extends DefaultStyledDocument {
	private static final long serialVersionUID = 1L;
	private AppLogger log = AppLogger.getInstance();

	private int currentRow, currentColumn, currentPosition;
	private int screenColumns;
	private boolean wrap, truncate;

	private String aLine = null;
	private int lineLength = 0;

	private JTextPane textPane;
	
//	ApplicationAdapter applicationAdapter = new ApplicationAdapter();
	public VT100Display(JTextPane textPane) {
		this.textPane = textPane;
		textPane.setStyledDocument(this);
	}// Constructor

	public void displayOnScreen(String textToAppend) {
		displayOnScreen(textToAppend, null);
	}// appendToDocASM

	private void displayOnScreen(String textToInsert, AttributeSet attributeSet) {
		int position = (currentRow * lineLength) + currentColumn;
		try {
			// ((AbstractDocument) this).replace(position, 1, textToInsert, null);
			this.replace(position, 1, textToInsert, null);
		} catch (BadLocationException e) {
			log.errorf("Failed to insert text: %s at row %d, column: %d%n", textToInsert, currentRow, currentColumn);
			e.printStackTrace();
		} // try

		incrementCurrentPosition();
		// textPane.updateUI();
	}// appendToDocASM
		////////////////////////////////////////////////////////////////////////////

//	public void asciiInFromCPU(byte value) {
//
//		switch (value) {
//		case ASCII_LF: // Line Feed 0x0A
//			nextLine();
//			break;
//		case ASCII_CR: // Carriage Return 0x0D
//			carriageReturn();
//			break;
//		case ASCII_BS: // Backspace 0x08
//			backSpace();
//			break;
//		default:
//			displayOnScreen(Character.toString((char) (value)));
//		}// switch ASCII_BS
//	}// asciiInFromCPU

	///////////////////////////////////////////////////////////////////////////////////
	public void doDel() {// 0x7F
		
	}//doDel
	
	public void doTab() {// 0x09
		
	}//doTab
	
	
	public void backSpace() {
		if (currentRow + currentColumn == 0) {
			return;
		} // if at top of page
		if (--currentColumn < 0) {
			currentRow--;
			currentColumn = screenColumns - 1;
		} // if else
		fixCurrentPosition();

	}// backSpace

	public void carriageReturn() {
		currentColumn = 0;
		fixCurrentPosition();
	}// carriageReturn

	////////////////////////////////////////////////////////////////////////////

	public void makeNewScreen() {
		makeEmptyLine();
		StringBuilder sb = new StringBuilder(this.getLineSize() * SCREEN_ROWS);
		for (int i = 0; i < SCREEN_ROWS; i++) {
			sb.append(aLine);
		} // for

		try {
			this.remove(0, this.getLength());
			this.insertString(0, sb.toString(), null);
		} catch (Exception e) {
			log.error("Failed to makeNewScreen");
		} // try
		this.currentRow = 0;
		this.currentColumn = 0;
		currentPosition = this.getPosition(currentRow, currentColumn);

		textPane.setCaretPosition(currentPosition);
		textPane.getCaret().setVisible(true);

	}// makeNewScreen

	private void makeEmptyLine() {
		aLine = String.format("%" + screenColumns + "s%s", ASCII_SPACE, EOL);
		lineLength = aLine.length();
		log.infof("[makeEmptyLine]  lineLength = %s, screenSize = %d%n", lineLength, SCREEN_ROWS * lineLength);
	}// makeEmptyLine

	private void scrollScreen() {
		log.info("Need to Scroll");
		currentRow = 23;
		currentColumn = 0;
		try {
			this.remove(0, lineLength);
			this.insertString(getPosition(currentRow, currentColumn), aLine, null);
		} catch (Exception e) {
			log.error("Failed to Scroll the Screen");
		} // try
		return;
	}// scrollScreen

	public void nextLine() {
		int priorColumn = currentColumn;
		if (++currentRow >= SCREEN_ROWS) {
			scrollScreen();
		} // if
		currentColumn = priorColumn;
		fixCurrentPosition();
	}// nextLine

	public void moveCursorUp(int count) {
		int row = currentRow - count;
		currentRow = row > 0 ? row : 0;
		moveCursor(currentRow, currentColumn);
		// log.infof("Escape Sequence %s%n", "moveCursorUp");
	}// moveCursorUp

	public void moveCursorDown(int count) {
		int row = currentRow + count;
		currentRow = row >= SCREEN_ROWS ? SCREEN_ROWS - 1 : row;
		moveCursor(currentRow, currentColumn);
		// log.infof("Escape Sequence %s%n", "moveCursorDown");
	}// moveCursorUp

	public void moveCursorRight(int count) {
		int column = currentColumn + count;
		currentColumn = column >= screenColumns ? screenColumns - 1 : column;
		moveCursor(currentRow, currentColumn);
		// log.infof("Escape Sequence %s%n", "moveCursorRight");
	}// moveCursorUp

	public void moveCursorLeft(int count) {
		int column = currentColumn - count;
		currentColumn = column > 0 ? column : screenColumns - 1;
		moveCursor(currentRow, currentColumn);
		// log.infof("Escape Sequence %s%n", "moveCursorLeft");
	}// moveCursorUp

	private void incrementCurrentPosition() {
		if (currentColumn < screenColumns - 1) {
			currentColumn++;
		} else {
			if (currentRow < SCREEN_ROWS - 1) {
				currentRow++;
				currentColumn = 0;
			} else {
				scrollScreen();
			} // if rows
		} // if columns
		fixCurrentPosition();
	}// updateCursorPosition

	private void fixCurrentPosition() {
		currentPosition = this.getPosition(currentRow, currentColumn);
		textPane.setCaretPosition(currentPosition);
		textPane.getCaret().setVisible(true);
	}// fixCurrentPosition

	public void moveCursor(int row, int column) {
		this.setRow(row);
		this.setColumn(column);
		fixCurrentPosition();
	}// moveCursor
	
	public void clearFromCursorUp() {
		int originalRow = currentRow;
		int originalColumn = currentColumn;
		
		clearLeft();
		while (--currentRow >= 0) {
			clearEntireLine();
		}//while
		currentRow = originalRow;
		currentColumn= originalColumn;
		fixCurrentPosition();
	}//clearFromCursorDown

	
	public void clearFromCursorDown() {
		int originalRow = currentRow;
		int originalColumn = currentColumn;
		clearRight();
		while (++currentRow < SCREEN_ROWS) {
			clearEntireLine();
		}//while
		currentRow = originalRow;
		currentColumn= originalColumn;
		fixCurrentPosition();
	}//clearFromCursorDown

	public void clearEntireLine() {
		this.setColumn(0);
		clearRight();
	}// clearEntireLine

	public void clearRight() {
		int pos = getPosition(currentRow, currentColumn);
		try {
			for (int c = currentColumn; c < screenColumns; c++) {
				this.replace(pos++, 1, ASCII_SPACE, null);
			} // for
		} catch (Exception e) {
			log.infof("[VT100Display.clearRight] $s%n", e.getMessage());
		} // try
		fixCurrentPosition();
	}// clearRight

	public void clearLeft() {
		try {
			for (int pos = getPosition(currentRow,0); pos < currentPosition; pos++) {
				this.replace(pos, 1, ASCII_SPACE, null);
			} // for
		} catch (Exception e) {
			log.infof("[VT100Display.clearRight] $s%n", e.getMessage());
		} // try
		fixCurrentPosition();
	}// clearRight

	public Dimension getSize() {
		return new Dimension(calcWidth(), calcHeight());
	}// getSize

	private int calcWidth() {
		int columnCount = screenColumns + EOL_SIZE;
		char[] data = new char[columnCount];
		for (int i = 0; i < columnCount; i++) {
			data[i] = 'W';
		} // for
		Font font = textPane.getFont();
		return textPane.getFontMetrics(font).charsWidth(data, 0, columnCount);
	}// calcScreenWidth

	private int calcHeight() {
		Font font = textPane.getFont();
		return (int) ((SCREEN_ROWS + 1.3) * textPane.getFontMetrics(font).getHeight());
	}// calcScreenHeight

	public void setRow(int row) {
		this.currentRow = row;
	}// setRow

	public int getRow() {
		return currentRow;
	}// getRow

	public void setColumn(int column) {
		this.currentColumn = column;
	}// setColumn

	public int getColumn() {
		return currentColumn;
	}// getColumn

	public void setPosition(int position) {
		this.currentPosition = position;
	}// setPosition

	public int getPosition() {
		return getPosition(currentRow, currentColumn);
	}// getPosition

	private int getPosition(int row, int column) {
		return (row * this.getLineSize()) + column;
	}// getCurrentPosition

	public void setScreenColumns(int screenColumns) {
		this.screenColumns = screenColumns;
	}// setColumn

	public int getScreenColumns() {
		return screenColumns;
	}// getColumn

	public void setWrap(boolean state) {
		this.wrap = state;
		this.truncate = !state;
	}// setWrap

	public boolean getWrap() {
		return this.wrap;
	}// getWrap

	public void setTruncate(boolean state) {
		this.truncate = state;
		this.wrap = !state;
	}// setWrap

	public boolean getTruncate() {
		return this.truncate;
	}// getWrap

//	private String getEmptyLine() {
//		return this.aLine;
//	}// getEmptyLine

	private int getLineSize() {
		return this.lineLength;
	}// getLineSize

	
	private static final int SCREEN_ROWS = 24;
	private static final String EOL = System.lineSeparator();
	private static final int EOL_SIZE = EOL.length();

	private static final String ASCII_SPACE = " ";



//	private static final byte ASCII_BS = (byte) 0x08; // Backspace
//	private static final byte ASCII_LF = (byte) 0x0A; // Line Feed
//	private static final byte ASCII_CR = (byte) 0x0D;// Carriage Return
//	private static final byte ASCII_ESC = (byte) 0x1B;// Escape

}// class VT100Display
