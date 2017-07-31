//
package CC;


// 408 bytes
public final class DataRecord{
	//
	short strAz=0;		// start azimuth
	short strEl=0;		// start elevation
	short endAz=0;		// end azimuth
	short endEl=0;		// end elevation
	
	Data[] rawData=new Data[100];
	
	
	/**
	 * used to print out
	 */
	public String toString(){
		StringBuilder sb=new StringBuilder();
		
		sb.append("strAz: "+strAz+"\n");
		sb.append("strEl: "+strEl+"\n");
		sb.append("endAz: "+endAz+"\n");
		sb.append("endEl: "+endEl+"\n");
		
		return sb.toString();
	}
}
