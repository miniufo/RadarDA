//
package SC;


//
public final class RawBin{
	//
	public byte ref=0;		// reflectivity (dBz), 0 is no echo
	public byte vel=0;		// radial velocity unit: maxObservableVel/127
	public byte unc=0;		// uncorrected intensity (dBz)
	public byte spw=0;		// spectral width
	
	
	/**
	 * used to print out
	 */
	public String toString(){
		StringBuilder sb=new StringBuilder();
		
		sb.append("ref: "+ref+"\n");
		sb.append("vel: "+vel+"\n");
		sb.append("unc: "+unc+"\n");
		sb.append("spw: "+spw+"\n");
		
		return sb.toString();
	}
}
