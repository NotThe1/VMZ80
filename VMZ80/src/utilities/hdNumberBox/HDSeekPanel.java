package utilities.hdNumberBox;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;

public class HDSeekPanel extends HDNumberBox {

	private void stepValue(int direction) {
		int changeAmount = (int) numberModel.getStepSize() * direction;

		long newValue = currentValue;
		newValue += changeAmount;

		if (newValue > (int) numberModel.getMaximum()) {
			setNewValue((int) numberModel.getMaximum());
		} else if (newValue < (int) numberModel.getMinimum()) {
			setNewValue((int) numberModel.getMinimum());
		} else {
			setNewValue((int) newValue);
		} // if
	}//

	// -------------------------------------------------------

	public HDSeekPanel() {
		super(new SpinnerNumberModel(12, Integer.MIN_VALUE, Integer.MAX_VALUE, 1), true);
		initialize();
	}// Constructor

	public HDSeekPanel(boolean decimalDisplay) {
		super(new SpinnerNumberModel(12, Integer.MIN_VALUE, Integer.MAX_VALUE, 1), decimalDisplay);
		initialize();
	}// Constructor

	public HDSeekPanel(SpinnerNumberModel numberModel) {
		super(numberModel, true);
		initialize();
	}// Constructor

	public HDSeekPanel(SpinnerNumberModel numberModel, boolean decimalDisplay) {
		super(numberModel, decimalDisplay);
		initialize();
	}// Constructor

	public void initialize() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mouseEvent) {
				if (mouseEvent.getClickCount() > 1) {
					if (showDecimal) {
						setHexDisplay();
					} else {
						setDecimalDisplay();
					} // if inner
				} // if click count
			}// mouseClicked
		});

		setPreferredSize(new Dimension(390, 30));

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 65, 65, 100, 65, 65, 0 };
		gridBagLayout.rowHeights = new int[] { 23, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JButton btnFirst = new JButton("<<");
		btnFirst.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setNewValue((int) numberModel.getMinimum());
			}
		});
		GridBagConstraints gbc_btnFirst = new GridBagConstraints();
		gbc_btnFirst.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnFirst.insets = new Insets(5, 1, 5, 5);
		gbc_btnFirst.gridx = 0;
		gbc_btnFirst.gridy = 0;
		add(btnFirst, gbc_btnFirst);

		JButton btnPrior = new JButton("<");
		btnPrior.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				stepValue(DOWN);
			}
		});
		GridBagConstraints gbc_btnPrior = new GridBagConstraints();
		gbc_btnPrior.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnPrior.insets = new Insets(5, 0, 5, 0);
		gbc_btnPrior.gridx = 1;
		gbc_btnPrior.gridy = 0;
		add(btnPrior, gbc_btnPrior);

		txtValueDisplay.setFont(new Font("Courier New", Font.PLAIN, 13));
		txtValueDisplay.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				if (txtValueDisplay.getText().equals("")) {
					return;
				} // if null
				int radix = showDecimal ? 10 : 16;
				setNewValue(Integer.valueOf(txtValueDisplay.getText(), radix));
			}
		});
		GridBagConstraints gbc_txtValueDisplay = new GridBagConstraints();
		gbc_txtValueDisplay.anchor = GridBagConstraints.CENTER;
		gbc_txtValueDisplay.insets = new Insets(5, 2, 5, 2);
		gbc_txtValueDisplay.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtValueDisplay.gridx = 2;
		gbc_txtValueDisplay.gridy = 0;
		add(txtValueDisplay, gbc_txtValueDisplay);

		JButton btnNext = new JButton(">");
		btnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stepValue(UP);
			}
		});
		GridBagConstraints gbc_btnNext = new GridBagConstraints();
		gbc_btnNext.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNext.insets = new Insets(5, 0, 5, 0);
		gbc_btnNext.gridx = 3;
		gbc_btnNext.gridy = 0;
		add(btnNext, gbc_btnNext);

		JButton btnLast = new JButton(">>");
		btnLast.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setNewValue((int) numberModel.getMaximum());
			}
		});
		GridBagConstraints gbc_btnLast = new GridBagConstraints();
		gbc_btnLast.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnLast.insets = new Insets(5, 5, 5, 2);
		gbc_btnLast.gridx = 4;
		gbc_btnLast.gridy = 0;
		add(btnLast, gbc_btnLast);

		setBorder(UIManager.getBorder("Spinner.border"));
	}// Constructor

	// ---------------------------


	private static final int UP = 1;
	private static final int DOWN = -1;
}// class HDSeekPanel
