package disks.diskPanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import disks.Disk;
import disks.DiskDrive;

public class V_IF_DiskPanel extends JInternalFrame implements Runnable {
	private static final long serialVersionUID = 1L;

	private Adapter_V_IF_DiskPanel adapterDiskPanel = new Adapter_V_IF_DiskPanel();
	// private AppLogger log = AppLogger.getInstance();

	DiskDrive[] disks = new DiskDrive[Disk.NUMBER_OF_DISKS];
	JToggleButton[] buttons = new JToggleButton[Disk.NUMBER_OF_DISKS];
	HashMap<JToggleButton, Integer> buttonMap;

	@Override
	public void run() {
		updateDisplay(this.disks);
	}// run

	@Override
	public void setEnabled(boolean state) {
		super.setEnabled(state);
		tbDiskA.setEnabled(state);
		tbDiskB.setEnabled(state);
		tbDiskC.setEnabled(state);
		tbDiskD.setEnabled(state);
	}// setEnabled

	public void updateDisks(DiskDrive[] disks) {
		this.disks = disks;
	}// updateDisks

	public void updateDisplay(DiskDrive[] disks) {
		// String name, toolTip;
		boolean haveDisk;
		for (int i = 0; i < Disk.NUMBER_OF_DISKS; i++) {
			haveDisk = disks[i] == null ? false : true;
			buttons[i].setText(haveDisk ? disks[i].getFileName() : NO_DISK);
			buttons[i].setToolTipText(haveDisk ? disks[i].getFilePath() : NO_DISK_HELP);
			buttons[i].setSelected(haveDisk ? true : false);
			buttons[i].setForeground(haveDisk ? FORE_SELECTED : FORE_DESELECTED);
			buttons[i].setBackground(haveDisk ? BACK_SELECTED : BACK_DESELECTED);
		} // for each disk
	}// updateDisplay

	public V_IF_DiskPanel() {
		initialize();
		appInit();
	}// Constructor

	private void appInit() {
		buttonMap = new HashMap<>();
		setName("V_IF_DiskPanel");
		buttons[0] = tbDiskA;
		buttonMap.put(tbDiskA, 0);
		buttons[1] = tbDiskB;
		buttonMap.put(tbDiskB, 1);
		buttons[2] = tbDiskC;
		buttonMap.put(tbDiskC, 2);
		buttons[3] = tbDiskD;
		buttonMap.put(tbDiskD, 3);
		updateDisplay(this.disks);
	}// appInit

	private void initialize() {
		// setBounds(100, 100, 328, 257);
		setBounds(100, 100, 328, 256);
		setTitle("Disks");
		setIconifiable(true);
		getContentPane().setLayout(null);

		JPanel panelAB = new JPanel();
		panelAB.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panelAB.setBounds(20, 20, 265, 75);
		getContentPane().add(panelAB);
		panelAB.setLayout(null);

		JLabel lblA = new JLabel("A:");
		lblA.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblA.setHorizontalAlignment(SwingConstants.CENTER);
		lblA.setBounds(10, 15, 20, 14);
		panelAB.add(lblA);

		tbDiskA = new JToggleButton("A");
		tbDiskA.setName(DISK_A);
		tbDiskA.addActionListener(adapterDiskPanel);
		tbDiskA.setFont(new Font("Tahoma", Font.PLAIN, 14));
		tbDiskA.setHorizontalAlignment(SwingConstants.CENTER);
		tbDiskA.setBackground(new Color(250, 240, 230));
		tbDiskA.setForeground(new Color(255, 127, 80));
		tbDiskA.setBounds(46, 12, 200, 20);
		panelAB.add(tbDiskA);

		JLabel lblB = new JLabel("B:");
		lblB.setHorizontalAlignment(SwingConstants.CENTER);
		lblB.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblB.setBounds(10, 44, 20, 14);
		panelAB.add(lblB);

		tbDiskB = new JToggleButton("B");
		tbDiskB.setName(DISK_B);
		tbDiskB.addActionListener(adapterDiskPanel);
		tbDiskB.setHorizontalAlignment(SwingConstants.CENTER);
		tbDiskB.setForeground(new Color(255, 127, 80));
		tbDiskB.setFont(new Font("Tahoma", Font.PLAIN, 14));
		tbDiskB.setBackground(new Color(250, 240, 230));
		tbDiskB.setBounds(46, 41, 200, 20);
		panelAB.add(tbDiskB);

		JPanel panelCD = new JPanel();
		panelCD.setLayout(null);
		panelCD.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		panelCD.setBounds(20, 127, 265, 75);
		getContentPane().add(panelCD);

		JLabel lblC = new JLabel("C:");
		lblC.setHorizontalAlignment(SwingConstants.CENTER);
		lblC.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblC.setBounds(10, 15, 20, 14);
		panelCD.add(lblC);

		tbDiskC = new JToggleButton("C");
		tbDiskC.setName(DISK_C);
		tbDiskC.addActionListener(adapterDiskPanel);
		tbDiskC.setHorizontalAlignment(SwingConstants.CENTER);
		tbDiskC.setForeground(new Color(255, 127, 80));
		tbDiskC.setFont(new Font("Tahoma", Font.PLAIN, 14));
		tbDiskC.setBackground(new Color(250, 240, 230));
		tbDiskC.setBounds(46, 12, 200, 20);
		panelCD.add(tbDiskC);

		JLabel lblD = new JLabel("D:");
		lblD.setHorizontalAlignment(SwingConstants.CENTER);
		lblD.setFont(new Font("Tahoma", Font.PLAIN, 15));
		lblD.setBounds(10, 44, 20, 14);
		panelCD.add(lblD);

		tbDiskD = new JToggleButton("D");
		tbDiskD.setName(DISK_D);
		tbDiskD.addActionListener(adapterDiskPanel);
		tbDiskD.setHorizontalAlignment(SwingConstants.CENTER);
		tbDiskD.setForeground(new Color(255, 127, 80));
		tbDiskD.setFont(new Font("Tahoma", Font.PLAIN, 14));
		tbDiskD.setBackground(new Color(250, 240, 230));
		tbDiskD.setBounds(46, 41, 200, 20);
		panelCD.add(tbDiskD);

	}// initialize

	/* \/ Event Handling Routines \/ */

	private Vector<DiskPanelEventListener> diskPanelActionListeners = new Vector<DiskPanelEventListener>();

	public synchronized void addDiskPanelActionListener(DiskPanelEventListener diskPanelActionListener) {
		if (!diskPanelActionListeners.contains(diskPanelActionListener)) {
			diskPanelActionListeners.add(diskPanelActionListener);
		} // if
		return;
	}// addDiskPanelActionListener

	public synchronized void removeDiskPanelActionListener(DiskPanelEventListener diskPanelActionListener) {
		diskPanelActionListeners.remove(diskPanelActionListener);
	}// removeDiskPanelActionListener

	public void fireDiskPanelAction(boolean selected, int diskIndex) {
//		Vector<DiskPanelEventListener> actionListeners;
//		synchronized (this) {
//			actionListeners = (Vector<DiskPanelEventListener>) diskPanelActionListeners.clone();
//		} // synchronized
//
//		int size = actionListeners.size();
//		if (size == 0) {
//			return; // no listeners
//		} // if

		DiskPanelEvent diskPanelEvent = new DiskPanelEvent(this, selected, diskIndex);
		for (DiskPanelEventListener listener : diskPanelActionListeners) {
			listener.diskPanelAction(diskPanelEvent);
		} // for each listener

	}// fireDiskPanelAction

	/* \/ Event Handling Routines \/ */

	private static final String NO_DISK = "< No Disk >";
	public static final String NO_DISK_HELP = "Click to mount a disk";

	private static final String DISK_A = "diskA";
	private static final String DISK_B = "diskB";
	private static final String DISK_C = "diskC";
	private static final String DISK_D = "diskD";

	private static final Color FORE_DESELECTED = new Color(46, 139, 87);
	private static final Color BACK_DESELECTED = new Color(204, 204, 255);

	private static final Color FORE_SELECTED = new Color(0, 0, 0);
	private static final Color BACK_SELECTED = new Color(250, 240, 230);

	private JToggleButton tbDiskA;
	private JToggleButton tbDiskB;
	private JToggleButton tbDiskC;
	private JToggleButton tbDiskD;

	///////////////////////////////////////////////////////////
	private class Adapter_V_IF_DiskPanel implements ActionListener {
		/* ActionListener */
		@Override
		public void actionPerformed(ActionEvent actionEvent) {
			JToggleButton button = (JToggleButton) actionEvent.getSource();
			fireDiskPanelAction(button.isSelected(), buttonMap.get(button));
		}// actionPerformed

	}// class Adapter_V_IF_DiskPanel
}//
