//
package CC;


//
public final class Elevation{
	//
	short maxV				=0;	// maximum observable velocity (cm/s)
	short maxL				=0;	// maximum observable range (10m)
	short binWidth			=0;	// bin width
	short binNumber			=0;	// bin number in each radial scan
	short recordNumber		=0;	// number of record in a circle
	short arotate			=0;	// rotation speed (0.01deg/s)
	short prf1				=0;	// first  repeat freq. (0.1Hz)
	short prf2				=0;	// second repeat freq. (0.1Hz)
	short spulseW			=0;	// pulse width (us)
	short angle				=0;	// elevation angle (0.01deg)
	char sweepStatus		=0;	// 1-one var 2-three vars (single frequency)
								// 3-three vars (dual frequency)
	char ambiguous			=0;	// 0-no software-dealiasing 1-software dealiasing
	
	
	public String toString(){
		StringBuilder sb=new StringBuilder();
		
		return sb.toString();
	}
}
