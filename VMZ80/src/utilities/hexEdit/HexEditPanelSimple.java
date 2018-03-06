package utilities.hexEdit;

import java.nio.ByteBuffer;
import java.util.SortedMap;

public class HexEditPanelSimple extends HexEditPanelBase {

	private static final long serialVersionUID = 1L;

	// ---------------------------------------------------------------
	public void loadData(Object src) {
		this.source = ByteBuffer.wrap((byte[]) src);
		loadDataCommon(source.capacity());
	}// loadDocument
	
	public void loadData(ByteBuffer src){
		loadDataCommon(source.capacity());
	}//

	public byte[] unloadData() {
		return applyChanges(source.array(), source.limit(), 0);
	}// unloadDocument

	public boolean isDataChanged() {
		return !changes.isEmpty();
	}// isDataChanges

	public SortedMap<Integer, Byte> getChangedData() {
		return changes;
	}// getChangedData

}// class hexEditPanelSimple
