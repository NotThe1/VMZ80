package ioSystem.terminals;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

import ioSystem.DeviceZ80;

public class VT100 extends DeviceZ80{

	private JFrame frameVT100;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					VT100 window = new VT100("vt100",IN,OUT,STATUS);
					window.frameVT100.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				} // try
			}// run
		});
	}// main

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}// run

	@Override
	public void byteFromCPU(Byte value) {
		// TODO Auto-generated method stub

	}//byteFromCPU

	@Override
	public void byteToCPU(Byte value) {
		// TODO Auto-generated method stub

	}//byteToCPU

	@Override
	public void close() {
		appClose();
	}//close

	@Override
	public void setVisible(boolean state) {
		frameVT100.setVisible(state);
	}//setVisible

	@Override
	public boolean isVisible() {
		return frameVT100.isVisible();
	}//isVisible

	/**
	 * Create the application.
	 */
	public VT100(String name, Byte addressIn, Byte addressOut, Byte addressStatus) {
		super(name, addressIn, addressOut, addressStatus);
		initialize();
		appInit();
	}// Constructor
	

	private void appClose() {
		Preferences myPrefs = Preferences.userNodeForPackage(VT100.class).node(this.getClass().getSimpleName());
		Dimension dim = frameVT100.getSize();
		myPrefs.putInt("Height", dim.height);
		myPrefs.putInt("Width", dim.width);
		Point point = frameVT100.getLocation();
		myPrefs.putInt("LocX", point.x);
		myPrefs.putInt("LocY", point.y);

		// myPrefs.putBoolean("TruncateColumns", truncateColumns);
		// myPrefs.putInt("MaxColumn", maxColumn);

		// myPrefs.putBoolean("Extended", mnuBehaviorExtend.isSelected());
		// myPrefs.putBoolean("Wrap", mnuBehaviorWrap.isSelected());
		// myPrefs.putBoolean("Truncate", mnuBehaviorTruncate.isSelected());

		// myPrefs.putInt("CaretColor", textScreen.getCaretColor().getRGB());
		// myPrefs.putInt("BackgroundColor", textScreen.getBackground().getRGB());
		// myPrefs.putInt("ForegroundColor", textScreen.getForeground().getRGB());

		// myPrefs.put("FontFamily", textScreen.getFont().getFamily());
		// myPrefs.putInt("FontStyle", textScreen.getFont().getStyle());
		// myPrefs.putInt("FontSize", textScreen.getFont().getSize());

		myPrefs = null;
	}// appClose

	private void appInit() {
		Preferences myPrefs = Preferences.userNodeForPackage(VT100.class).node(this.getClass().getSimpleName());
		frameVT100.setSize(myPrefs.getInt("Width", 761), myPrefs.getInt("Height", 693));
		frameVT100.setLocation(myPrefs.getInt("LocX", 100), myPrefs.getInt("LocY", 100));

		// truncateColumns = myPrefs.getBoolean("TruncateColumns", false);
		// maxColumn = myPrefs.getInt("MaxColumn", 80);

		// mnuBehaviorExtend.setSelected(myPrefs.getBoolean("Extended", true));
		// mnuBehaviorWrap.setSelected(myPrefs.getBoolean("Wrap", false));
		// mnuBehaviorTruncate.setSelected(myPrefs.getBoolean("Truncate", false));
		// setupScreen(myPrefs, textScreen);

		myPrefs = null;
		// tabSize = 9;
		//
		// doColumnBehavior();
		// spinnerColumns.setValue(maxColumn);
		//
		// screen = textScreen.getDocument();
		// clearDoc();
		frameVT100.setVisible(true);
	}// appInit

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frameVT100 = new JFrame();
		frameVT100.setBounds(100, 100, 450, 300);
		frameVT100.setTitle("VT100              Rev 0.0.A");

		frameVT100.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				appClose();
			}//windowClosing
		});

	}// initialize
	// --------------------------------------------------------------------------------------

	// Address is for CP/M COM device
	public static final Byte IN = (byte) 0X02;
	public static final Byte OUT = (byte) 0X02;
	public static final Byte STATUS = (byte) 0X02;
	public static final Byte STATUS_OUT_READY = (byte) 0b1000_0000; // MSB set

}// class VT100
