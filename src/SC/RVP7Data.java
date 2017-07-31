//
package SC;


// 8+4*998 = 4000 bytes
public final class RVP7Data{
	//
	public short strAz=0;		// start azimuth
	public short strEl=0;		// start elevation
	public short endAz=0;		// end azimuth
	public short endEl=0;		// end elevation
	
	public float Nyquist=0;	// maximum observable velocity (m/s)
	
	public RawBin[] rawData=new RawBin[998];
	
	
	/**
	 * used to print out
	 */
	public String toString(){
		return String.format(
		"Azimuth [%7.3f, %7.3f], Elevation [%7.3f, %7.3f]",
		(strAz&0x0FFFF)*360f/65536f,(endAz&0x0FFFF)*360f/65536f,
		(strEl&0x0FFFF)*120f/65536f,(endEl&0x0FFFF)*120f/65536f
		);
	}
}
