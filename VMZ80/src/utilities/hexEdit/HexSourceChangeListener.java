package utilities.hexEdit;

import java.util.EventListener;


public interface HexSourceChangeListener extends EventListener {
	public void dataChanged(HexSourceChangeEvent hexSourceChangeEvent);
}//HexSourceChangeListener
