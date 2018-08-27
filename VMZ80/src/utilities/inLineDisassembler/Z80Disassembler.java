package utilities.inLineDisassembler;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EtchedBorder;
import javax.swing.text.AbstractDocument.BranchElement;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import codeSupport.AppLogger;
import hardware.WorkingRegisterSet;
import memory.Core;

public class Z80Disassembler extends JPanel implements Runnable {

	private static final long serialVersionUID = 1L;

	private AppLogger log = AppLogger.getInstance();
	private static Core core = Core.getInstance();
	private static WorkingRegisterSet wrs = WorkingRegisterSet.getInstance();
	private static StyledDocument doc;
	OpCodeMap opCodeMap = new OpCodeMap();

	SimpleAttributeSet historyAttributes;
	SimpleAttributeSet locationAttributes1;
	SimpleAttributeSet opCodeAttributes1;
	SimpleAttributeSet instructionAttributes1;
	SimpleAttributeSet functionAttributes1;
	SimpleAttributeSet boldAttributes;

	// private static Z80Disassembler instance = new Z80Disassembler();

	// public static Z80Disassembler getInstance() {
	// return instance;
	// }// getInstance

	public void run() {
		refreshDisplay();
	}// run()

	public void refreshDisplay() {
		priorProgramCounter = Integer.MAX_VALUE;
		nextProgramCounter = Integer.MIN_VALUE;
		newDisplay = true;
		updateDisplay();
	}// refreshDisplay

	public void updateDisplay() {
		int programCounter = wrs.getProgramCounter();
		if (programCounter == priorProgramCounter) {
			return;
		} else if (programCounter == nextProgramCounter) {
			nextInstruction(programCounter);
		} else if (!newDisplay) {
			// // updateTheDisplay(programCounter);
			notNextInstruction(programCounter);
		} else {
			try {
				doc.remove(0, doc.getLength());
				currentPosition = doc.createPosition(0);
				// activePosition = doc.createPosition(0);
			} catch (BadLocationException e) {
				JOptionPane.showMessageDialog(null, "Error clearing Disply Document", "UpdateDisplay",
						JOptionPane.ERROR_MESSAGE);
				return; // graceful exit
			} // try clear the contents of doc
			newDisplay = false;
			// nextProgramCounter = processCurrentLine(programCounter);
			futureProgramerCounter = processFutureLines(programCounter, 0);
			processCurrentLine();
			txtInstructions.setCaretPosition(0);
			//
		} // if new display
			//
		priorProgramCounter = programCounter; // remember for next update
		return;
	}// updateDisplay()

	private int getNextProgramCounter(int programCounter) {
		String opCodeMapKey = getOpCodeMapKey(programCounter);
		int opCodeSize = OpCodeMap.getSize(opCodeMapKey);
		// byte opCode = core.read(programCounter);
		// int opCodeSize = OpCodeMap.getSize(opCode);
		return programCounter + opCodeSize;

	}// getNextProgramCounter

	private void nextInstruction(int programCounter) {
		processHistoryLine();
		processCurrentLine();
		nextProgramCounter = getNextProgramCounter(programCounter);
		// processCurrentLine(nextProgramCounter,currentLineNumber+1);
	}// nextInstruction

	private void notNextInstruction(int programCounter) {
		processHistoryLine();
		BranchElement rootElement = (BranchElement) doc.getDefaultRootElement();
		int lineNumber = rootElement.getElementIndex(currentPosition.getOffset());
		int removePoint = currentPosition.getOffset() - 1;
		try {
			doc.remove(removePoint, doc.getLength() - removePoint);
			processFutureLines(programCounter, lineNumber);
			currentPosition = doc.createPosition(removePoint + 1);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		processCurrentLine();
	}// notNextInstruction

	private void processHistoryLine() {
		Element paragraphElement = doc.getParagraphElement(currentPosition.getOffset());
		int paragraphStart = paragraphElement.getStartOffset();
		int paragraphLength = paragraphElement.getEndOffset() - paragraphStart;
		doc.setCharacterAttributes(paragraphStart, paragraphLength, historyAttributes, true);
		try {
			currentPosition = doc.createPosition(paragraphStart + paragraphLength + 1);
			BranchElement rootElement = (BranchElement) doc.getDefaultRootElement();
			if (rootElement.getElementIndex(currentPosition.getOffset()) > LINES_OF_HISTORY) {
				Element paragraphZero = doc.getParagraphElement(0);
				doc.remove(0, paragraphZero.getEndOffset());
				futureProgramerCounter = processFutureLines(futureProgramerCounter, LINES_TO_DISPLAY);
			} // if need to remove
		} catch (BadLocationException e) {
			e.printStackTrace();
		} // try

		return;
	}// processHistoryLine

	private void processCurrentLine() {
		Element paragraphElement = doc.getParagraphElement(currentPosition.getOffset());
		int paragraphStart = paragraphElement.getStartOffset();
		int paragraphLength = paragraphElement.getEndOffset() - paragraphStart;
		doc.setCharacterAttributes(paragraphStart, paragraphLength, boldAttributes, false);
		return;
	}// processCurrentLine

	private int processFutureLines(int programCounter, int lineNumber) {
		int workingProgramCounter = programCounter;
		workingProgramCounter += insertCode(workingProgramCounter, LINE_FUTURE);
		nextProgramCounter = workingProgramCounter;

		for (int i = 0; i < LINES_TO_DISPLAY - (lineNumber); i++) {
			workingProgramCounter += insertCode(workingProgramCounter, LINE_FUTURE);
		} // for
		return workingProgramCounter; // futureProgramCounter.
	}// processCurrentLine

	private String getOpCodeMapKey(int opCodeLocation) {
		byte opCode = core.read(opCodeLocation);
		byte value1 = core.read(opCodeLocation + 1);
		// byte value2 = core.read(opCodeLocation + 2);
		byte value3 = core.read(opCodeLocation + 3);
		String key = "";

		if (opCode == ED | opCode == CB) {
			key = String.format("%02X%02X", opCode, value1);
			// }else if (opCode == 0xED) {
			//
		} else if (opCode == DD | opCode == FD) {
			if (value1 == CB) {
				key = String.format("%02X%02X%02X", opCode, value1, value3);
			} else {
				key = String.format("%02X%02X", opCode, value1);
			} // if extended bit or not
		} else {
			key = String.format("%02X", opCode);
		} // if all of it

		return key;
	}//

	// private String getOpCodeMapKey(byte opCode, byte value1, byte value2, byte value3) {
	// String key = "";
	// if (opCode == 0xED | opCode == 0xCB) {
	// key = String.format("%02X%02X", opCode, value1);
	// } else if (opCode == 0xDD | opCode == 0xFD) {
	// if (value1 == 0xCB) {
	// key = String.format("%02X%02X%02X", opCode, value1, value3);
	// } else {
	// key = String.format("%02X%02X", opCode, value1);
	// } // if extended bit or not
	// } else {
	// key = String.format("%02X", opCode);
	// } // if all of it
	//
	// return key;
	// }// getOpCodeMapKey

	private int insertCode(int workingProgramCounter, int when) {// int
		// thisLineNumber,
		// int
		// workingProgramCounter
		// int workingPosition = thisLineNumber * LINE_WIDTH;
		byte opCode = core.read(workingProgramCounter);
		byte value1 = core.read(workingProgramCounter + 1);
		byte value2 = core.read(workingProgramCounter + 2);
		byte value3 = core.read(workingProgramCounter + 3);

		// String opCodeMapKey = getOpCodeMapKey( opCode, value1, value2, value3);
		String opCodeMapKey = getOpCodeMapKey(workingProgramCounter);

		int opCodeSize = OpCodeMap.getSize(opCodeMapKey);

		String locationPart = null;
		String opCodePart = null;
		String instructionPart = null;
		try {
			locationPart = makeLocationPart(workingProgramCounter);
			// doc.insertString(doc.getLength(), locationPart,
			// locationAttributes[when]);
			doc.insertString(doc.getLength(), locationPart, locationAttributes1);
			switch (opCodeSize) {
			case 1:
				opCodePart = String.format("%02X%8s", opCode, "");
				doc.insertString(doc.getLength(), opCodePart, opCodeAttributes1);
				instructionPart = String.format("%-15s", OpCodeMap.getAssemblerCode(opCodeMapKey));
				doc.insertString(doc.getLength(), instructionPart, instructionAttributes1);
				break;
			case 2:
				opCodePart = String.format("%02X%02X%6s", opCode, value1, "");
				doc.insertString(doc.getLength(), opCodePart, opCodeAttributes1);
				if (opCode == DD || opCode == FD) {
					instructionPart = String.format("%-15s", OpCodeMap.getAssemblerCode(opCodeMapKey));
				} else if (opCode == ED || opCode == CB) {
					instructionPart = String.format("%-15s", OpCodeMap.getAssemblerCode(opCodeMapKey));
				} else {
					instructionPart = String.format("%-15s", OpCodeMap.getAssemblerCode(opCodeMapKey, value1));
				}
				doc.insertString(doc.getLength(), instructionPart, instructionAttributes1);
				break;
			case 3:
				opCodePart = String.format("%02X%02X%02X%4s", opCode, value1, value2, "");
				doc.insertString(doc.getLength(), opCodePart, opCodeAttributes1);
				instructionPart = String.format("%-15s", OpCodeMap.getAssemblerCode(opCodeMapKey, value1, value2));
				doc.insertString(doc.getLength(), instructionPart, instructionAttributes1);
				break;
			case 4:
				opCodePart = String.format("%02X%02X%02X%02X%2s", opCode, value1, value2, value3, "");
				doc.insertString(doc.getLength(), opCodePart, opCodeAttributes1);
				// instructionPart = "instructionPart";
				instructionPart = String.format("%-15s", OpCodeMap.getAssemblerCode(opCodeMapKey, value2, value3));
				doc.insertString(doc.getLength(), instructionPart, instructionAttributes1);

			default:
				log.warnf("Bad Opcode %02X %02X %02X %02X at Location %04X%n", opCode, value1, value2, value3,
						workingProgramCounter);
			}// switch opCode Size
			String functionPart = String.format("    %s", OpCodeMap.getFunction(opCodeMapKey) + LINE_SEPARATOR);
			doc.insertString(doc.getLength(), functionPart, functionAttributes1);
			if (when == LINE_CURRENT) {
				currentPosition = doc.createPosition(doc.getLength() - 1); // current
				// line
				// position
			} // if

		} catch (Exception e) {
			e.printStackTrace();
		} // try

		return opCodeSize;
	}// insertCode

	private String makeLocationPart(int location) {
		return String.format("%04X%s%4s", location, COLON, "");
	}// makeLocationPart

	private void makeStyles() {
		SimpleAttributeSet baseAttributes = new SimpleAttributeSet();
		StyleConstants.setFontFamily(baseAttributes, "Courier New");
		StyleConstants.setFontSize(baseAttributes, 16);

		historyAttributes = new SimpleAttributeSet(baseAttributes);
		StyleConstants.setForeground(historyAttributes, Color.GRAY);

		boldAttributes = new SimpleAttributeSet();
		StyleConstants.setBold(boldAttributes, true);

		locationAttributes1 = new SimpleAttributeSet(baseAttributes);
		StyleConstants.setForeground(locationAttributes1, COLOR_LOCATION);

		opCodeAttributes1 = new SimpleAttributeSet(baseAttributes);
		StyleConstants.setForeground(opCodeAttributes1, COLOR_OPCODE);

		instructionAttributes1 = new SimpleAttributeSet(baseAttributes);
		StyleConstants.setForeground(instructionAttributes1, COLOR_INSTRUCTION);

		functionAttributes1 = new SimpleAttributeSet(baseAttributes);
		StyleConstants.setForeground(functionAttributes1, COLOR_FUNCTION);

	}// makeStyles1

	///////////////////////////////////////////////////////////////////////////////////////
	public Z80Disassembler() {
		super();
		initialize();
		appInit();
	}// Constructor

	private void appInit() {
		newDisplay = true;
		doc = txtInstructions.getStyledDocument();
		makeStyles();
		// updateDisplay();
	}// appInit

	private void initialize() {
		setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setMinimumSize(new Dimension(600, 20));
		scrollPane.setPreferredSize(new Dimension(600, 100));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);

		txtInstructions = new JTextPane();
		txtInstructions.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() > 1) {
					refreshDisplay();
				}
			}
		});
		txtInstructions.setEditable(false);
		scrollPane.setViewportView(txtInstructions);
		JLabel lblNewLabel = new JLabel(
				" Location             OpCode                   Instruction                                               Function\r\n");
		lblNewLabel.setForeground(Color.BLUE);
		scrollPane.setColumnHeaderView(lblNewLabel);

	}// initialize

	private boolean newDisplay;
	private Position currentPosition;

	private int priorProgramCounter = Integer.MAX_VALUE; // value of previous update PC
	private int nextProgramCounter = Integer.MIN_VALUE; // value of future update PC if straight line code
	private int futureProgramerCounter; // instruction +1 from last displayed

	/*-------------------CONSTANTS-------------------------------*/
	private final static Color COLOR_LOCATION = Color.black;
	private final static Color COLOR_OPCODE = Color.red;
	private final static Color COLOR_INSTRUCTION = Color.blue;
	private final static Color COLOR_FUNCTION = Color.green;

	private final static byte ED = (byte) 0xED;
	private final static byte CB = (byte) 0xCB;
	private final static byte DD = (byte) 0xDD;
	private final static byte FD = (byte) 0xFD;

	private final static String COLON = ":";

	private final static int LINES_TO_DISPLAY = 64; // LTD-> Lines To Display
	private final static int LINES_OF_HISTORY = 5; // LTT-> Lines to Trail

	private static final int LINE_CURRENT = 1;
	private static final int LINE_FUTURE = 2;

	private static String LINE_SEPARATOR = System.lineSeparator();
	private JTextPane txtInstructions;

	/*----------------------------------------------------------------*/

}// class Z80Disassembler
