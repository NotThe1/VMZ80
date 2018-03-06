package utilities.hdNumberBox;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

//import utilities.seekPanel.SeekValueChangeListener;

/**
 * 
 * @author Frank Martyn. This is a text box that only accepts either decimal or hexadecimal numbers. It supports a
 *         JSpinnerNumberModel to manage the values range of input data. The value input is limited byte the
 *         NumberModel. if value is greater than Max the value will be fixed to Max Value. Similarly if less than Min
 *         the value will be fixed at Min Value.
 * 
 *         The range of the value is Integer.Min to Integer.MAX.
 * 
 *         This class supports a HDNumberValueChangeListener that will fire a HDNumberValueChangeEvent when the value
 *         changes
 * 
 *         the method mute(boolean state) disables/enables the fireSeekValueChanged() method, so values can be changed
 *         without triggering events
 *
 */
public class HDNumberBox extends JPanel {
	private static final long serialVersionUID = 1L;

	SpinnerNumberModel numberModel;
	int currentValue, priorValue;
	JFormattedTextField txtValueDisplay;
	EventListenerList hdNumberValueChangeListenerList;
	String decimalDisplayFormat = "%d";
	String hexDisplayFormat = "%X";
	boolean showDecimal = true;
	SeekDocument displayDoc;
	boolean muteNumberChangeEvent;

	public void setNumberModel(SpinnerNumberModel numberModel) {
		this.numberModel = numberModel;
		int newValue = (int) numberModel.getValue();
		priorValue = newValue;
		currentValue = newValue;
		setNewValue(newValue);
	}// setNumberModel

	public SpinnerNumberModel getNumberModel() {
		return this.numberModel;
	}// getNumberModel

	public int getValue() {
		return currentValue;
	}// getValue
	
	public String getStringValue(){
		String displayFormat = showDecimal ? decimalDisplayFormat : hexDisplayFormat;
		currentValue = (int) numberModel.getValue();
		return String.format(displayFormat, currentValue);

	}//getStringValue

	public int getPriorValue() {
		return (int) numberModel.getPreviousValue();
	}// getPriorValue

	public void setValue(int newValue) {
		setNewValue(newValue);
		return;
	}// setValue

	public void setMaxValue(int newMaxValue) {
		numberModel.setMaximum(newMaxValue);
	}// setMaxValue

	public void setDecimalDisplay() {
		setDecimalDisplay(true);
	}// setDecimalDisplay

	public void setHexDisplay() {
		setDecimalDisplay(false);
	}// setHexDisplay

	public void setDecimalDisplay(boolean displayDecimal) {
		String tipText = "";
		showDecimal = displayDecimal;
		if (displayDecimal) {
			displayDoc.displayDecimal();
			tipText = "Display is Decimal";
		} else {
			displayDoc.displayHex();
			tipText = "Display is Hex";
		} // if
		displayValue();
		txtValueDisplay.setToolTipText(tipText);
	}// setHexDisplay

	public boolean isDecimalDisplay() {
		return showDecimal;
	}// isDecimalDisplay

	public void mute(boolean state) {
		muteNumberChangeEvent = state;
	}// mute

	// ---------------------------------------

	private void displayValue() {
		String displayFormat = showDecimal ? decimalDisplayFormat : hexDisplayFormat;
		currentValue = (int) numberModel.getValue();

		String stringValue = String.format(displayFormat, currentValue);
		txtValueDisplay.setText(stringValue);
		txtValueDisplay.repaint();
	}// showValue

	void setNewValue(int newValue) {
		newValue = Math.min(newValue, (int) numberModel.getMaximum()); // upper
		newValue = Math.max(newValue, (int) numberModel.getMinimum()); // lower

		priorValue = (int) numberModel.getValue();
		currentValue = (newValue);
		numberModel.setValue(newValue);
		displayValue();
		if (muteNumberChangeEvent) {
			return;
		} // if
		if (priorValue != currentValue) {
			fireSeekValueChanged();
		} // if
	}// newValue

	// -------------------------------------------------------

	public HDNumberBox() {
		this(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1), true);

	}// Constructor

	public HDNumberBox(boolean decimalDisplay) {
		this(new SpinnerNumberModel(0, Integer.MIN_VALUE, Integer.MAX_VALUE, 1), decimalDisplay);
	}// Constructor

	public HDNumberBox(SpinnerNumberModel numberModel) {
		this(numberModel, true);
	}// Constructor

	public HDNumberBox(SpinnerNumberModel numberModel, boolean decimalDisplay) {
		this.numberModel = numberModel;

		appInit0();
		Initialize();
		appInit();

		if (decimalDisplay) {
			setDecimalDisplay();
		} else {
			setHexDisplay();
		} // if
	}// Constructor

	private void appInit0() {
		displayDoc = new SeekDocument(true);
	}// appInit0

	private void appInit() {
		currentValue = (int) numberModel.getValue();
		txtValueDisplay.setDocument(displayDoc);
		txtValueDisplay.setPreferredSize(new Dimension(100, 23));
		hdNumberValueChangeListenerList = new EventListenerList();
		muteNumberChangeEvent = false;
	}// appInit

	private void Initialize() {
		setPreferredSize(new Dimension(286, 35));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 100, 0 };
		gridBagLayout.rowHeights = new int[] { 23, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		setBorder(UIManager.getBorder("TextField.border"));

		txtValueDisplay = new JFormattedTextField();
		txtValueDisplay.setMinimumSize(new Dimension(75, 20));
		txtValueDisplay.setBackground(UIManager.getColor("TextArea.background"));
		txtValueDisplay.setFont(new Font("Courier New", Font.PLAIN, 13));
		txtValueDisplay.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				if (txtValueDisplay.getText().equals("")) {
					return;
				} // if null
				int radix = showDecimal ? 10 : 16;
				try {
					setNewValue(Integer.valueOf(txtValueDisplay.getText(), radix));
				} catch (Exception e) {
					setNewValue(getPriorValue());
				}//try
			}//focusLost
		});

		txtValueDisplay.setHorizontalAlignment(SwingConstants.RIGHT);
		txtValueDisplay.setPreferredSize(new Dimension(100, 23));
		GridBagConstraints gbc_txtValueDisplay = new GridBagConstraints();
		gbc_txtValueDisplay.fill = GridBagConstraints.BOTH;
		gbc_txtValueDisplay.gridx = 0;
		gbc_txtValueDisplay.gridy = 0;
		add(txtValueDisplay, gbc_txtValueDisplay);
	}// Constructor

	// ---------------------------
	public void addHDNumberValueChangedListener(HDNumberValueChangeListener seekValueChangeListener) {
		hdNumberValueChangeListenerList.add(HDNumberValueChangeListener.class, seekValueChangeListener);
	}// addSeekValueChangedListener

	public void removeHDNumberValueChangedListener(HDNumberValueChangeListener seekValueChangeListener) {
		hdNumberValueChangeListenerList.remove(HDNumberValueChangeListener.class, seekValueChangeListener);
	}// addSeekValueChangedListener

	protected void fireSeekValueChanged() {
		Object[] listeners = hdNumberValueChangeListenerList.getListenerList();
		// process
		HDNumberValueChangeEvent hdNumberValueChangeEvent = new HDNumberValueChangeEvent(this, priorValue,
				currentValue);

		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == HDNumberValueChangeListener.class) {
				((HDNumberValueChangeListener) listeners[i + 1]).valueChanged(hdNumberValueChangeEvent);
			} // if
		} // for

	}// fireSeekValueChanged

	// ---------------------------
	class SeekDocument extends PlainDocument {
		private static final long serialVersionUID = 1L;

		private String inputPattern;

		SeekDocument(boolean decimalDisplay) {
			if (decimalDisplay == true) {
				displayDecimal();
			} else {
				displayHex();
			} // if
		}// Constructor

		public void displayDecimal() {
			inputPattern = "-??[0-9]*";

		}// displayDecimal

		public void displayHex() {
			inputPattern = "[A-F|a-f|0-9]+";
		}// displayHex

		public void insertString(int offSet, String string, AttributeSet attributeSet) throws BadLocationException {
			if (string == null) {
				return;
			} // if

			if (!string.matches(inputPattern)) {
				return;
			} // for

			super.insertString(offSet, string, attributeSet);
		}// insertString
	}// class SeekDocument
		// ______________________________

}// class HDNumberBox
