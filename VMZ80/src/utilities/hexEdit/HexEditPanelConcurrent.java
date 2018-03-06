package utilities.hexEdit;

import java.nio.ByteBuffer;

public class HexEditPanelConcurrent extends HexEditPanelBase implements HexSourceChangeListener{
	
	private static final long serialVersionUID = 1L;
	private byte[] sourceArray;

	//---------------------------------------------------------------
	
	public void loadData(Object src){
		this.sourceArray = (byte[])src;
		this.source = ByteBuffer.wrap(sourceArray);
		
		HexEditDocumentFilter hexFilter =loadDataCommon(source.capacity()-1);
		hexFilter.addHexSourceChangeListener(this);

	}// loadDocument
			
	@Override
	public void dataChanged(HexSourceChangeEvent hexSourceChangeEvent) {
		int location = hexSourceChangeEvent.getLocation();
		byte value = hexSourceChangeEvent.getValue();
//		System.out.printf("[] location = %04X, value = %02X%n", location,value);
		sourceArray[location] = value;
		
	}//dataChanged


}//class hexEditPanelSimple
