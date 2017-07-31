//
package SC;


// 170 bytes
public final class RadarSiteHeader{
	//
	public String country      =null;	// country name
	public String province     =null;	// province name
	public String station      =null;	// station name
	public String stationNumber=null;	// station number
	public String radarType    =null;	// radar type
	public String longitude    =null;	// longitude of station
	public String latitude     =null;	// latitude of station
	public int longitudeValue  =0;		// longitude of station (1/1000 deg)
	public int latitudeValue   =0;		// latitude  of station (1/1000 deg)
	public int height          =0;		// height of station
	public short maxAngle      =0;		// maximum angle of obstacle (1/100 deg)
	public short optAngle      =0;		// optimal observation angle (Ref < 10dBZ, 1/100 deg)
	public short mangFreq      =0;		// manage frequency (used to calculate wavelength)
	
	
	/**
	 * used to print out
	 */
	public String toString(){
		StringBuilder sb=new StringBuilder();
		
		sb.append("country: "+country+"\n");
		sb.append("province: "+province+"\n");
		sb.append("station: "+station+"\n");
		sb.append("stationNumber: "+stationNumber+"\n");
		sb.append("radarType: "+radarType+"\n");
		sb.append("longitude: "+longitude+" "+longitudeValue+"\n");
		sb.append("latitude: "+latitude+" "+latitudeValue+"\n");
		sb.append("height: "+height+"\n");
		
		sb.append("maxAngle: "+maxAngle+"\n");
		sb.append("optAngle: "+optAngle+"\n");
		sb.append("mangFreq: "+mangFreq+"\n");
		
		return sb.toString();
	}
}
