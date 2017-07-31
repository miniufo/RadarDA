//
package SC;


// 31 bytes
public final class PerformanceHeader{
	//
	public int antennaG     =0;	// antenna gain (0.01 dB)
	public short beamV      =0;	// vertical width of beam (second)
	public short beamH      =0;	// horizontal width of beam (second)
	public byte polarization=0;	// polarization state 0-horizontal 1-vertical 2-dual 3-circular
	public byte sideLobe    =0;	// first side lobe (dB)
	public int power        =0;	// power (watter)
	public int wavelength   =0;	// wavelength (um)
	public short logA       =0;	// log range (0.01dB)
	public short lineA      =0;	// linear range (0.01dB)
	public short AGCP       =0;	// AGC delay (us)
	public byte clutterT    =0;	// threshold for noise (STC)
	public byte velocityP   =0;	// velocity process
								// 0 = no process
								// 1 = PPP
								// 2 = FFT
	public byte filterP     =0;	// filter of surface reflection 0-none 1-IIR 1 2-IIR 2
								// 3-IIR 3 4-IIR 4	short freqMode		=0;				// frequency mode 1-single repeat freq.
								// 2-double repeat freq. 3:2 3-double repeat freq. 4:3
	public byte noiseT      =0;	// threshold for noise (0-255)
	public byte SQIT        =0;	// SQI threshold (0.01)
	public byte intensityC  =0;	// DVIP intensity estimate channel 1-log 2-linear
	public byte intensityR  =0;	// intensity estimation is range-corrected 0-none 1-corrected
	
	
	/**
	 * used to print out
	 */
	public String toString(){
		StringBuilder sb=new StringBuilder();
		
		sb.append("antennaG: "+antennaG+"\n");
		sb.append("beamV: "+(beamV&0x0FFFF)+"\n");
		sb.append("beamH: "+(beamH&0x0FFFF)+"\n");
		sb.append("polarization: "+polarization+"\n");
		sb.append("sideLobe: "+sideLobe+"\n");
		sb.append("power: "+power+"\n");
		sb.append("wavelength: "+wavelength+"\n");
		
		sb.append("logA: "+(logA&0x0FFFF)+"\n");
		sb.append("lineA: "+(lineA&0x0FFFF)+"\n");
		sb.append("AGCP: "+(AGCP&0x0FFFF)+"\n");
		sb.append("clutterT: "+(clutterT&0x0FF)+"\n");
		sb.append("velocityP: "+(velocityP&0x0FF)+"\n");
		sb.append("filterP: "+filterP+"\n");
		sb.append("noiseT: "+noiseT+"\n");
		sb.append("SQIT: "+SQIT+"\n");
		sb.append("intensityC: "+intensityC+"\n");
		sb.append("intensityR: "+intensityR+"\n");
		
		return sb.toString();
	}
}
