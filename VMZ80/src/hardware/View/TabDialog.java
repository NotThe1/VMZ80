package hardware.View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import appLogger.AppLogger;
import codeSupport.DebugFrame;
import memory.Core;
import utilities.hexEditPanel.EditAtom;
import utilities.hexEditPanel.EditType;
//import utilities.hexEditPanel.HDNumberBox;
import utilities.hexEditPanel.HexEditDisplayPanel;
import utilities.inLineDisassembler.Z80Disassembler;

public class TabDialog extends JDialog implements Runnable {
	private static final long serialVersionUID = 1L;

	private AppLogger log = AppLogger.getInstance();
	private JPopupMenu popupLog;
	private AdapterTabDialog adapterTabDialog = new AdapterTabDialog();
	private HexEditDisplayPanel hexDisplayMemory = new HexEditDisplayPanel();
	private Z80Disassembler disassembler = new Z80Disassembler();

	@Override
	public void run() {
		// TODO Auto-generated method stub
	}// run
	
	
	private void refreshMemory() {
		hexDisplayMemory.setData(Core.getInstance().getStorage());
		hexDisplayMemory.refresh();
		disassembler.updateDisplay();
	}// refreshMemory

	public void updateDisplay() {
		if (isVisible()) {
			refreshMemory();
			tabDebug.run();
		}//if only do if the dialog is visible
	}// refreshViews

//	private void appClose() {
//
//	}// appClose

	private void appInit() {
		tabbedPane.addChangeListener(adapterTabDialog);
		tabbedPane.setSelectedComponent(tabDebug);

		txtLog.setText(EMPTY_STRING);
		log.setDoc(txtLog.getStyledDocument());

		refreshMemory();
		Thread t_disassembler = new Thread(disassembler);
		t_disassembler.start();
	}// appInit

	public TabDialog() {
		initialize();
		appInit();
	}// Constructor

	private void initialize() {

		setTitle("Z80 Machine Support");
		setBounds(100, 100, 1269, 584);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[] { 358, 0 };
		gbl_contentPanel.rowHeights = new int[] { 28, 0 };
		gbl_contentPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_contentPanel.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		contentPanel.setLayout(gbl_contentPanel);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		contentPanel.add(tabbedPane, gbc_tabbedPane);

		tabDebug = new DebugFrame();
		tabDebug.setName(TAB_DEBUG);
		tabbedPane.addTab("Debug", null, tabDebug, null);
//		GridBagConstraints gbc_debug = new GridBagConstraints();
//		gbc_debug.fill = GridBagConstraints.BOTH;
//		gbc_debug.gridx = 0;
//		gbc_debug.gridy = 0;
		tabDebug.setLayout(new GridLayout(0, 1, 0, 0));
		//////////////////////////////////////////////////////////////

		tabDisasssembler = new JPanel();
		tabDisasssembler.setName(TAB_DISASSEMBLER);
		tabbedPane.addTab("Disassembler", null, tabDisasssembler, null);
		GridBagConstraints gbc_disAl = new GridBagConstraints();
		gbc_disAl.fill = GridBagConstraints.BOTH;
		gbc_disAl.gridx = 0;
		gbc_disAl.gridy = 0;
		tabDisasssembler.setLayout(new GridLayout(0, 1, 0, 0));
		tabDisasssembler.add(disassembler, gbc_disAl);// gbc_disAl);

		JPanel tabApplicationLog = new JPanel();
		tabApplicationLog.setName(TAB_APP_LOG);
		tabbedPane.addTab("Application Log", null, tabApplicationLog, null);
		GridBagLayout gbl_tabApplicationLog = new GridBagLayout();
		gbl_tabApplicationLog.columnWidths = new int[] { 0, 0 };
		gbl_tabApplicationLog.rowHeights = new int[] { 0, 0 };
		gbl_tabApplicationLog.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_tabApplicationLog.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		tabApplicationLog.setLayout(gbl_tabApplicationLog);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		tabApplicationLog.add(scrollPane, gbc_scrollPane);

		txtLog = new JTextPane();
		scrollPane.setViewportView(txtLog);
		popupLog = new JPopupMenu();
		addPopup(txtLog, popupLog);

		JMenuItem popupLogClear = new JMenuItem("Clear Log");
		popupLogClear.setName(PUM_LOG_CLEAR);
		popupLogClear.addActionListener(adapterTabDialog);
		popupLog.add(popupLogClear);

		JSeparator separator = new JSeparator();
		popupLog.add(separator);

		JMenuItem popupLogPrint = new JMenuItem("Print Log");
		popupLogPrint.setName(PUM_LOG_PRINT);
		popupLogPrint.addActionListener(adapterTabDialog);
		popupLog.add(popupLogPrint);

		JLabel label = new JLabel("Application Log");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setForeground(new Color(30, 144, 255));
		label.setFont(new Font("Times New Roman", Font.BOLD | Font.ITALIC, 14));
		scrollPane.setColumnHeaderView(label);

		tabMemoryDisplay = new JPanel();
		tabMemoryDisplay.setName(TAB_MEMORY);
		tabbedPane.addTab("Memory", null, tabMemoryDisplay, null);
		GridBagConstraints gbc_hexPanel = new GridBagConstraints();
		gbc_hexPanel.fill = GridBagConstraints.BOTH;
		gbc_hexPanel.gridx = 0;
		gbc_hexPanel.gridy = 0;
		tabMemoryDisplay.setLayout(new GridLayout(0, 1, 0, 0));
		tabMemoryDisplay.add(hexDisplayMemory, gbc_hexPanel);

		////////////////////////////////////////////////////////////////
		JPanel buttonPane = new JPanel();
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		GridBagLayout gbl_buttonPane = new GridBagLayout();
		gbl_buttonPane.columnWidths = new int[] { 0, 46, 0 };
		gbl_buttonPane.rowHeights = new int[] { 14, 0 };
		gbl_buttonPane.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_buttonPane.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		buttonPane.setLayout(gbl_buttonPane);

		JLabel lblNewLabel = new JLabel("New label");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		buttonPane.add(lblNewLabel, gbc_lblNewLabel);

	}// initialize

	private void doTabChanged(JTabbedPane source) {
		String name = source.getSelectedComponent().getName();
		if (!name.equals(TAB_MEMORY)) {
			// System.out.printf("data changed - %s%n",hexDisplayMemory.isDataChanged());
			if (hexDisplayMemory.isDataChanged()) {
				List<EditAtom> allEdits = hexDisplayMemory.getAllEdits();
				Core core = Core.getInstance();

				for (EditAtom edit : allEdits) {
					if (edit.getEditType().equals(EditType.REPLACE)) {
						core.write(edit.getLocation(), edit.getTo());
						// System.out.printf(" Location %04X, value 02X%n",edit.getLocation(), edit.getTo());
					} // for each edit
				} // if data changed

			} // if data changed
			hexDisplayMemory.clearAllEdits();
		} // if
		switch (name) {
		case TAB_DISASSEMBLER:
			disassembler.refreshDisplay();
			break;
		case TAB_DEBUG:
			tabDebug.run();
			break;
		}// switch
	}// doTabChanged

	private void doLogClear() {
		log.clear();
	}// doLogClear

	private void doLogPrint() {

		Font originalFont = txtLog.getFont();
		try {
			// textPane.setFont(new Font("Courier New", Font.PLAIN, 8));
			txtLog.setFont(originalFont.deriveFont(8.0f));
			MessageFormat header = new MessageFormat("Identic Log");
			MessageFormat footer = new MessageFormat(new Date().toString() + "           Page - {0}");
			txtLog.print(header, footer);
			// textPane.setFont(new Font("Courier New", Font.PLAIN, 14));
			txtLog.setFont(originalFont);
		} catch (PrinterException e) {
			e.printStackTrace();
		} // try

	}// doLogPrint
		// +/+/+/+/+/+/+/+/+/+/+/+/+/+/+/ DEBUG /+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+
//
//	private void doDebugEnable() {
//
//	}// doDebugEnable
//
//	private void doDebugReset() {
//
//	}// doDebugReset
//
//	private void doDebugRemove() {
//
//	}// doDebugRemove
//
//	private void doDebugClear() {
//
//	}// doDebugClear
//		// +/+/+/+/+/+/+/+/+/+/+/+/+/+/+/ DEBUG /+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+/+

	//////////////////////////////////////////////////////////////////////////

	class AdapterTabDialog implements ActionListener, ChangeListener {// , ListSelectionListener
		/* ActionListener */
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			String name = ((Component) actionEvent.getSource()).getName();
			switch (name) {
			case PUM_LOG_PRINT:
				doLogPrint();
				break;
			case PUM_LOG_CLEAR:
				doLogClear();
				break;
			}// switch
		}// actionPerformed

		/* ChangeListener */
		@Override
		public void stateChanged(ChangeEvent changeEvent) {
			JTabbedPane source = (JTabbedPane) changeEvent.getSource();
			doTabChanged(source);
		}// stateChanged

	}// class AdapterAction
	/////////////////////////////

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				} // if popup Trigger
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}// addPopup

	private static final String EMPTY_STRING = "";
	private static final String PUM_LOG_PRINT = "popupLogPrint";
	private static final String PUM_LOG_CLEAR = "popupLogClear";

//	private static final String RB_ENABLE = "rbEnabled";
//	private static final String BTN_RESET = "btnReset";
//	private static final String BTN_REMOVE = "btnRemovet";
//	private static final String BTN_CLEAR = "btnCLEAR";

	private static final String TAB_APP_LOG = "tabApplicationLog";
	private static final String TAB_MEMORY = "tabMemoryDisplay";
	private static final String TAB_DISASSEMBLER = "tabDisassembler";
	private static final String TAB_DEBUG = "tabDebug";

	private JTextPane txtLog;
	private JPanel tabMemoryDisplay;
	private JTabbedPane tabbedPane;
	private JPanel tabDisasssembler;
	private final JPanel contentPanel = new JPanel();
	private DebugFrame tabDebug;

}// class TabDialog
