package utilities.hexEdit;

import javax.swing.text.Element;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;

public class HexEditNavigationFilter extends NavigationFilter {

	
	private HexEditMetrics hexMetrics;


	private int dataStart = 0;
	private int asciiStart = 0;
	
	private StyledDocument doc;
	private int[] columnTable;
	
	private int priorColumn;
	private int priorPosition;
	
	private int lastDataEnd = 0;
	private int lastAsciiStart = 0;
	private int lastAsciiEnd = 0;
	private int lineLength;
	
//	private int asciiCount = 0;
	

	public HexEditNavigationFilter(StyledDocument doc, HexEditMetrics hexMetrics) {
		this.doc = doc;
		this.hexMetrics= hexMetrics;
		
		this.dataStart = hexMetrics.getDataStart();
		this.asciiStart = hexMetrics.getAsciiStart();
		
		columnTable = makeColumnTable();
		
		lineLength = hexMetrics.getLineLength();
		
	}//Constructor
	
	public void setLastLine(int bytesRead, int linesRead){
		int bytesBeforeLastLine = (linesRead * lineLength);
		
		lastAsciiStart = bytesBeforeLastLine + this.asciiStart;
		lastAsciiEnd = lastAsciiStart + bytesRead -1 ;
		
		lastDataEnd = bytesBeforeLastLine + this.dataStart + (3 * (bytesRead-1))+2;
		lastDataEnd = bytesRead < 8 ? lastDataEnd: lastDataEnd + 1;
		
//		System.out.printf("%n[setLastLine] bytesRead: %d,linesRead: %d %n",bytesRead,linesRead);
//		System.out.printf("[setLastLine] lineLength: %d %n",lineLength);
//		System.out.printf("[setLastLine] bytesBeforeLastLine: %d %n",bytesBeforeLastLine);
//		System.out.printf("[setLastLine] lastAsciiStart: %d,lastAsciiEnd: %d %n",lastAsciiStart,lastAsciiEnd);
//		System.out.printf("[setLastLine] lastDataEnd: %d %n%n",lastDataEnd);
		
	}//setLastLine
	


	public void setDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias) {
		// check if past end of document
		
		if (dot > lastAsciiEnd) {
			return; // past the ASCII
		} // if
		
		if ((dot >= lastDataEnd) & (dot < lastAsciiStart - 1)) {	
			return;
		} // past the last data , before the ASCII

		Element paragraphElement = doc.getParagraphElement(dot);
		int column = dot - paragraphElement.getStartOffset();
		int columnType = columnTable[column];
		int position = dot;
		
//		System.out.printf("[setDot] dot:  %d,columnType:  %d,position:  %d%n" , dot, columnType, position);
		
			
		switch (columnType) {
		case ADDR:		//0
			position = paragraphElement.getStartOffset() + this.dataStart;
			break;
		case NORMAL:	//1
			position = dot;
			break;
		case BLANK_1:	//7
			//fix double call to setPos from DocumentFilter.FilteBypass
			if((priorColumn == (hexMetrics.getAsciiStart() +1)) && (column ==(hexMetrics.getDataBlockEnd()) )){
				position = priorPosition-2;
			}else{
				position = dot + 1;
			}//if		
			break;
		case BLANK_2:	//8
			position = dot + 2;
			break;
		case DATA_WRAP:	//3
			position = paragraphElement.getEndOffset() + this.dataStart;
			break;
		case ASCII_WRAP:	//4
//			System.out.printf("%s%n", "ASCII Wrap");
			position = paragraphElement.getEndOffset() + this.asciiStart;
//			asciiCount = 1;
			break;
		default:
			position = dot;
			break;
		}// switch
		priorColumn = column;
		priorPosition = position;
		fb.setDot(position, bias);

	}// setDot

	public void moveDot(NavigationFilter.FilterBypass fb, int dot, Position.Bias bias) {
	//	System.out.printf("[moveDot] **** dot: %d, dataStart: %d%n", dot, this.dataStart);
	}// moveDot
		// -----------------------------------------------------------
/**
 * columnTable maps the actions to be taken as a function of the column in the doc.
 * 
 * @return
 */
	private int[] makeColumnTable() {
		int[] ans = new int[hexMetrics.getAsciiEnd()+3];
		int columnPosition = 0;
		for (; columnPosition < hexMetrics.getDataStart(); columnPosition++) {
			ans[columnPosition] = ADDR;
		} // Address
		
		int[] dataPattern = new int[] { NORMAL, NORMAL, BLANK_1 };
		int dataPatternLength = dataPattern.length;
		for (int i = 0; i < 8; i++) {
			System.arraycopy(dataPattern, 0, ans, columnPosition, dataPatternLength);
			columnPosition += dataPatternLength;
		} // first 8 data

		// adjust for extra space
		columnPosition -= 2;
		ans[columnPosition++] = NORMAL;
		ans[columnPosition++] = BLANK_2;
		ans[columnPosition++] = BLANK_1;

		for (int i = 0; i < 8; i++) {
			System.arraycopy(dataPattern, 0, ans, columnPosition, dataPatternLength);
			columnPosition += dataPatternLength;
		} // second 8 data

		// adjust for end of data
		columnPosition -= 2;
		ans[columnPosition++] = NORMAL;
		ans[columnPosition++] = DATA_WRAP;
		ans[columnPosition++] = BLANK_2;
		ans[columnPosition++] = BLANK_1;
		
		for (int i = 0; i < BYTES_PER_LINE; i++) {
			ans[columnPosition++] = NORMAL;
		} // ASCII
			// End of Line
				
		ans[columnPosition++] = ASCII_WRAP;
		ans[columnPosition++] = ASCII_WRAP;

		return ans;
	}// makeColumnTable

	private static final int ADDR = 0;
	private static final int NORMAL = 1;

	private static final int DATA_WRAP = 3;
	private static final int ASCII_WRAP = 4;

	private static final int BLANK_1 = 7;
	private static final int BLANK_2 = 8;
	
	private final static int BYTES_PER_LINE = HexEditMetrics.BYTES_PER_LINE;


}// class HexEditNavigationFilter
