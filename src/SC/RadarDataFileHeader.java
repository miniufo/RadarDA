//
package SC;


// 170+31+660+163 = 1024 bytes
public final class RadarDataFileHeader{
	//
	public RadarSiteHeader  rsh=null;
	public PerformanceHeader ph=null;
	public ObservationHeader oh=null;
	
	public byte[] reserved=new byte[163];
	
	
	/**
	 * used to print out
	 */
	public String toString(){
		return rsh.toString()+"\n"+ph.toString()+"\n"+oh.toString();
	}
}
