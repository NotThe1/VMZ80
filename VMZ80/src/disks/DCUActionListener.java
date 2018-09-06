package disks;

import java.util.EventListener;

public interface DCUActionListener extends EventListener {
	void dcuAction(DCUActionEvent dcuEvent);
}//interface DCUActionListener
