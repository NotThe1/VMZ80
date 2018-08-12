package disks.diskPanel;

import java.util.EventObject;

public class DiskPanelEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	int diskIndex;
	boolean selected;

	public DiskPanelEvent(Object source,boolean selected, int diskIndex) {
		super(source);
		this.diskIndex = diskIndex;
		this.selected= selected;
	}// Constructor

	public int getDiskIndex() {
		return this.diskIndex;
	}// getDiskIndex

	public boolean isSelected() {
		return this.selected;
	}// getActionType


}// DiskPanelEvent
