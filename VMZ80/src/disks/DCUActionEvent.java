package disks;

import java.util.EventObject;

public class DCUActionEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	int diskIndex;
	int command;

	public DCUActionEvent(Object source, int diskIndex, int actionType) {
		super(source);
		this.diskIndex = diskIndex;
		this.command = actionType;
		// TODO Auto-generated constructor stub
	}// DCUActionEvent

	public int getDiskIndex() {
		return this.diskIndex;
	}// getDiskIndex

	public int getActionType() {
		return this.command;
	}// getActionType

	public static final int COMMAND_READ = 01;
	public static final int COMMAND_Write = 02;

}// class DCUActionEvent
