package ioSystem.terminals;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.JTextComponent;

public class VT100Display extends DefaultStyledDocument {
	private static final long serialVersionUID = 1L;
	private int currentRow, currentColumn, currentPosition;
	private int screenColumns;
	private boolean wrap,truncate;

	private JTextComponent textComponent;

	public VT100Display(JTextComponent textComponent) {
		this.textComponent = textComponent;
	}// Constructor

	public Dimension getSize() {
		return new Dimension(calcWidth(), calcHeight());
	}// getSize

	private int calcWidth() {
		int columnCount = screenColumns + EOL_SIZE;
		char[] data = new char[columnCount];
		for (int i = 0; i < columnCount; i++) {
			data[i] = 'W';
		} // for
		Font font = textComponent.getFont();
		return textComponent.getFontMetrics(font).charsWidth(data, 0, columnCount);
	}// calcScreenWidth

	private int calcHeight() {
		Font font = textComponent.getFont();
		return (int) ((SCREEN_ROWS + 1.3) * textComponent.getFontMetrics(font).getHeight());
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
		return currentPosition;
	}// getPosition

	public void setScreenColumns(int screenColumns) {
		this.screenColumns = screenColumns;
	}// setColumn

	public int getScreenColumns() {
		return screenColumns;
	}// getColumn
	
	public void setWrap(boolean state) {
		this.wrap=state;
	}//setWrap
	
	public boolean getWrap() {
		return this.wrap;
	}//getWrap
	
	public void setTruncate(boolean state) {
		this.truncate=state;
	}//setWrap
	
	public boolean getTruncate() {
		return this.truncate;
	}//getWrap
	
	

	private static final int SCREEN_ROWS = 24;
	private static final String EOL = System.lineSeparator();
	private static final int EOL_SIZE = EOL.length();

}// class VT100Display
