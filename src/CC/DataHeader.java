//
package CC;


//
public final class DataHeader{
	//
	public String toString(){
		StringBuilder sb=new StringBuilder();
		
		//sb.append("fileType: "+String.valueOf(fileType)+"\n");
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
		sb.append("sYear1: "+sYear1+"\n");
		sb.append("sYear2: "+sYear2+"\n");
		sb.append("sMonth: "+sMonth+"\n");
		sb.append("sDay: "+sDay+"\n");
		sb.append("sHour: "+sHour+"\n");
		sb.append("sMinute: "+sMinute+"\n");
		sb.append("sSecond: "+sSecond+"\n");
		sb.append("timeFrom: "+timeFrom+"\n");
		sb.append("eYear1: "+eYear1+"\n");
		sb.append("eYear2: "+eYear2+"\n");
		sb.append("eMonth: "+eMonth+"\n");
		sb.append("eDay: "+eDay+"\n");
		sb.append("eHour: "+eHour+"\n");
		sb.append("eMinute: "+eMinute+"\n");
		sb.append("eSecond: "+eSecond+"\n");
		sb.append("scanMode: "+scanMode+"\n");
		
		return sb.toString();
	}
	
	//char[] fileType		=new char[16];	// 3830 data identifier (CINRAD C)
	String country		=null;	// country name
	String province		=null;	// province name
	String station		=null;	// station name
	String stationNumber=null;	// station number
	String radarType	=null;	// radar type
	String longitude	=null;	// longitude of station
	String latitude		=null;	// latitude of station
	long longitudeValue	=0;				// longitude of station
	long latitudeValue	=0;				// latitude of station
	long height			=0;				// height of station
	short maxAngle		=0;				// maximum angle of obstacle
	short optAngle		=0;				// optimal observation angle
	byte sYear1			=0;				// start observation year  (19-20)
	byte sYear2			=0;				// start observation year  (00-99)
	byte sMonth			=0;				// start observation month (01-12)
	byte sDay			=0;				// start observation day   (01-31)
	byte sHour			=0;				// start observation hour  (00-23)
	byte sMinute		=0;				// start observation minute(00-59)
	byte sSecond		=0;				// start observation month (00-59)
	byte timeFrom		=0;				// time type 0-computer clock (no correction within one day)
										//           1-computer clock (corrected within one day)
										//           2-GPS
										//           3-other
	byte eYear1			=0;				// end observation year  (19-20)
	byte eYear2			=0;				// end observation year  (00-99)
	byte eMonth			=0;				// end observation month (01-12)
	byte eDay			=0;				// end observation day   (01-31)
	byte eHour			=0;				// end observation hour  (00-23)
	byte eMinute		=0;				// end observation minute(00-59)
	byte eSecond		=0;				// end observation month (00-59)
	byte scanMode		=0;				// scan mode  1  - RHI
										//           10  - PPI and ZPPI
										//           1XX = VPPI (XX is scan number)
	long sMilliSecond	=0;				// milli-second
	short RHIA			=0;				// azimuthal angle of RHI (unit in 0.01)
										// FFFF for PPI and VPPI
	short RHIL			=0;				// lowest elevation angle of RHI
										// FFFF for PPI and VPPI
	short echoType		=0;				// type of echo 0x405a-Z 0x406a-V 0x407a-W 0x408-ZVW
	short prodCode		=0;				// data type
										// 0x8001-PPI data 0x8002-RHI data 0x8003-VPPI data
										// 0x8004-single reflectivity RHI data 0x8005-CAPPI data
	char calibration	=0;				// calibration state 0-none 1-auto 2-manually within one week
										// 3-manually within a month
	char[] remain		=new char[  3];	// preserved
	char[] remain2		=new char[660];	// preserved for VPPI SCAN PARAMETER data
	long antennaG		=0;				// antenna gain (0.01 dB)
	long power			=0;				// power (watter)
	long wavelength		=0;				// wavelength (um)
	short beamH			=0;				// beam height (second)
	short beamL			=0;				// beam long (second)
	short polarization	=0;				// polarization state 0-horizontal 1-vertical 2-dual 3-circular
	short logA			=0;				// log range (0.01dB)
	short lineA			=0;				// linear range (0.01dB)
	short AGCP			=0;				// AGC delay (us)
	short freqMode		=0;				// frequency mode 1-single repeat freq.
										// 2-double repeat freq. 3:2 3-double repeat freq. 4:3
	short freqRepeat	=0;				// frequency of repeat
	short PPPPulse		=0;				// PPP pulse
	short FFTPoint		=0;				// fft point
	short processType	=0;				// signal-process type 1-PPP 2-full FFT 3-single FFT
	char clutterT		=0;				// threshold for noise (STC)
	char sideLobe		=0;				// first side lobe (dB)
	char velocityT		=0;				// threshold for velocity
	char filterP		=0;				// filter of surface reflection 0-none 1-IIR 1 2-IIR 2
										// 3-IIR 3 4-IIR 4
	char noiseT			=0;				// threshold for noise
	char SQIT			=0;				// SQI threshold
	char intensityC		=0;				// DVIP intensity estimate channel 1-log 2-linear
	char intensityR		=0;				// intensity estimation is range-corrected 0-none 1-corrected
	char calNoise		=0;				// noise calibration
	char calPower		=0;				// power calibration
	char calPluseWidth	=0;				// pulse width calibration
	char calWorkFreq	=0;				// work frequency calibration
	char calLog			=0;				// log calibration
	char[] remain3		=new char[92];	// preserved
	int dataOffset		=0;				// data offset
	
	
}
