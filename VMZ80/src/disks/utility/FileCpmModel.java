package disks.utility;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

class FileCpmModel extends AbstractListModel<DirEntry> implements ComboBoxModel<DirEntry> {
	private static final long serialVersionUID = 1L;
	
	List<DirEntry> modelItemList;
	DirEntry selection = null;

	public FileCpmModel() {
		modelItemList = new ArrayList<DirEntry>();
	}// Constructor

	public void add(DirEntry item) {
		modelItemList.add(item);
	}// add
	
	public void clear() {
		modelItemList.clear();
	}// clear
	
	public boolean isEmpty() {
		return modelItemList.isEmpty();
	}//isEmpty

	@Override
	public DirEntry getElementAt(int index) {
		return modelItemList.get(index);
	}// getElementAt

	@Override
	public int getSize() {
		return modelItemList.size();
	}// getSize

	@Override
	public DirEntry getSelectedItem() {
		return selection;
	}// getSelectedItem

	@Override
	public void setSelectedItem(Object arg0) {
		if (arg0 instanceof DirEntry) {
			selection = (DirEntry) arg0;
		} else {
			selection = new DirEntry((String) arg0, -1);
		}// if
	}// setSelectedItem
	
	public boolean exists(String fileName){
		boolean ans = false;
		for(DirEntry de:modelItemList){
			if(de.fileName.equals(fileName)){
				ans = true;
				break;
			}//if
		}//for
		return ans;
	}//exists
	
	public boolean exists(DirEntry dirEntry) {
		return this.exists(dirEntry.fileName);
	}//exists
	

}// class FileCpmModel
