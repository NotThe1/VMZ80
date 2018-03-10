package disks.utility;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

class FileCpmModel extends AbstractListModel<String> implements ComboBoxModel<String> {
	private static final long serialVersionUID = 1L;
	
	List<String> modelItemList;
	String selection = null;

	public FileCpmModel() {
		modelItemList = new ArrayList<String>();
	}// Constructor

	public void add(String item) {
		modelItemList.add(item.trim());
	}// add
	
	public void clear() {
		modelItemList.clear();
	}// clear
	
	public boolean isEmpty() {
		return modelItemList.isEmpty();
	}//isEmpty

	@Override
	public String getElementAt(int index) {
		return modelItemList.get(index);
	}// getElementAt

	@Override
	public int getSize() {
		return modelItemList.size();
	}// getSize

	@Override
	public String getSelectedItem() {
		return selection;
	}// getSelectedItem

	@Override
	public void setSelectedItem(Object arg0) {
		if (arg0 instanceof String) {
			selection = (String) arg0;
		}//if
	}// setSelectedItem
	
	public boolean exists(String fileName){
		return modelItemList.contains(fileName);
//		boolean ans = false;
//		for(String de:modelItemList){
//			if(de.fileName.equals(fileName)){
//				ans = true;
//				break;
//			}//if
//		}//for
//		return ans;
	}//exists
	


}// class FileCpmModel
