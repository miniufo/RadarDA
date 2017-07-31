//
package SC;


// 660 bytes 
public final class ObservationHeader{
	//
	public byte scanMode   =0;		// scan mode  1  - RHI
							//           10  - PPI and ZPPI
							//           1XX = VPPI (XX is scan number)
	public short sYear     =0;		// start observation year  (2000-)
	public byte sMonth     =0;		// start observation month (01-12)
	public byte sDay       =0;		// start observation day   (01-31)
	public byte sHour      =0;		// start observation hour  (00-23)
	public byte sMinute    =0;		// start observation minute(00-59)
	public byte sSecond    =0;		// start observation month (00-59)
	public byte timeP      =0;		// time type 0-computer clock (no correction within one day)
							//           1-computer clock (corrected within one day)
							//           2-GPS
							//           3-other
	public int sMilliSecond=0;		// milli-second
	public byte calibration=0;		// calibration state 0-none 1-auto 2-manually within one week
							// 3-manually within a month	byte eYear1			=0;				// end observation year  (19-20)
	public byte intensityI =0;		// integration count of intensity (32-128)
	public byte velocityP  =0;		// velocity process sample (32-128)
	public short RHIA      =0;		// azimuth angle (1/100 deg) for RHI
	public short RHIL      =0;		// lowest elevation angle (1/100 deg)
	public short RHIH      =0;		// highest elevation angle (1/100 deg)
	public short eYear     =0;		// end observation year  (00-99)
	public byte eMonth     =0;		// end observation month (01-12)
	public byte eDay       =0;		// end observation day   (01-31)
	public byte eHour      =0;		// end observation hour  (00-23)
	public byte eMinute    =0;		// end observation minute(00-59)
	public byte eSecond    =0;		// end observation month (00-59)
	public byte eTenth     =0;		// end time in 1/100 second (00-99)
	
	public LayerHeader[] layers=new LayerHeader[30];
	
	
	/**
	 * used to print out
	 */
	public String toString(){
		StringBuilder sb=new StringBuilder();
		
		sb.append("scanMode: "+scanMode+"\n");
		sb.append("sYear: "+sYear+"\n");
		sb.append("sMonth: "+sMonth+"\n");
		sb.append("sDay: "+sDay+"\n");
		sb.append("sHour: "+sHour+"\n");
		sb.append("sMinute: "+sMinute+"\n");
		sb.append("sSecond: "+sSecond+"\n");
		sb.append("timeP: "+timeP+"\n");
		sb.append("sMilliSecond: "+sMilliSecond+"\n");
		sb.append("calibration: "+calibration+"\n");
		sb.append("intensityI: "+intensityI+"\n");
		sb.append("velocityP: "+velocityP+"\n\n");
		
		//for(int i=0;i<30;i++) sb.append("layer "+i+"\n"+layers[i].toString()+"\n");
		
		sb.append("RHIA: "+(RHIA&0x0FFFF)+"\n");
		sb.append("RHIL: "+RHIL+"\n");
		sb.append("RHIH: "+RHIH+"\n");
		sb.append("eYear: "+eYear+"\n");
		sb.append("eMonth: "+eMonth+"\n");
		sb.append("eDay: "+eDay+"\n");
		sb.append("eHour: "+eHour+"\n");
		sb.append("eMinute: "+eMinute+"\n");
		sb.append("eSecond: "+eSecond+"\n");
		sb.append("eTenth: "+eTenth+"\n");
		
		return sb.toString();
	}
	
}
