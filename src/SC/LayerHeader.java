//
package SC;


// 21 bytes
public final class LayerHeader{
	//
	public byte ambiguousP   =0;	// how to de-aliasing
									//     0 = no de-aliasing
									//     1 = software de-aliasing
									//     2 = dual-T de-aliasing
									//     3 = batch de-aliasing
									//     4 = dual-T + software de-aliasing
									//     5 = batch  + software de-aliasing
									//     6 = dual PPI de-aliasing
									//     9 = others
	public short arotate     =0;	// rotation speed  (0.01 deg/s)
	public short prf1        =0;	// first  pulse repeat frequency (1/10 Hz)
	public short prf2        =0;	// second pulse repeat frequency (1/10 Hz)
	public short pulseW      =0;	// pulse width (us)
	public short maxV        =0;	// maximum observable velocity (cm/s)
	public short maxL        =0;	// maximum observable distance (10 m)
	public short binWidth    =0;	// bin width (0.1 m)
	public short binNumber   =0;	// number of bins
	public short recordNumber=0;	// count of radial scan
	public short angles      =0;	// elevation angle (1/100 degree)
	
	
	/**
	 * used to print out
	 */
	public String toString(){
		StringBuilder sb=new StringBuilder();
		
		sb.append("ambiguousP: "+ambiguousP+"\n");
		sb.append("arotate: "+arotate+"\n");
		sb.append("prf1: "+prf1+"\n");
		sb.append("prf2: "+prf2+"\n");
		sb.append("pulseW: "+pulseW+"\n");
		sb.append("maxV: "+maxV+"\n");
		sb.append("maxL: "+maxL+"\n");
		
		sb.append("binWidth: "+binWidth+"\n");
		sb.append("binnumber: "+binNumber+"\n");
		sb.append("recordNumber: "+recordNumber+"\n");
		sb.append("angles: "+angles+"\n");
		
		return sb.toString();
	}
}
