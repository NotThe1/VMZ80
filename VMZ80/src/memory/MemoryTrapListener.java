package memory;

import java.util.EventListener;

public interface MemoryTrapListener extends EventListener {
	public void memoryTrap(MemoryTrapEvent MemoryTrapEvent);
}// interface MemoryTrapListener
