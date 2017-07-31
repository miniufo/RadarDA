//
package commonapi;

import java.util.List;
import miniufo.diagnosis.MDate;
import SASB.RadialScan;
import SC.LayerHeader;
import SC.ObservationHeader;


//
public final class CutHeader{
	//
	public boolean validRefCut=false;
	public boolean validVelCut=false;
	
	public int  refBinNo=0;
	public int  velBinNo=0;
	public int  refRadNo=0;
	public int  velRadNo=0;
	
	public float refBinWidth=0;
	public float velBinWidth=0;
	public float Nyquist=0;
	public float ElAngle=0;
	public float fstRefBinRng=0;
	public float fstVelBinRng=0;
	
	public float[] radiusRef=null;
	public float[] radiusVel=null;
	
	public MDate obsTime=null;
	
	
	/**
	 * constructor
	 */
	public CutHeader(List<RadialScan> ls){
		RadialScan rs=ls.get(0);
		
		if(rs.PtrOfReflectivity!=0) validRefCut=true;
		if(rs.PtrOfVelocity    !=0) validVelCut=true;
		
		refBinNo=rs.GateNumberOfReflectivity;
		velBinNo=rs.GateNumberOfDoppler;
		if(validRefCut) refRadNo=ls.size();
		if(validVelCut) velRadNo=ls.size();
		Nyquist=rs.Nyquist/100f;
		ElAngle=rs.El/8f*(180f/4096f);
		refBinWidth=rs.GateSizeOfReflectivity;
		velBinWidth=rs.GateSizeOfDoppler;
		fstRefBinRng=rs.RangeToFirstGateOfRef;
		fstVelBinRng=rs.RangeToFirstGateOfDop;
		obsTime=new MDate(1970,1,1).addDays(rs.JulianDate-1).addSeconds(Math.round(rs.mSeconds/1000f));
		
		radiusRef=new float[refBinNo];
		radiusVel=new float[velBinNo];
		
		for(int i=0;i<refBinNo;i++) radiusRef[i]=fstRefBinRng+i*refBinWidth;
		for(int i=0;i<velBinNo;i++) radiusVel[i]=fstVelBinRng+(i+0.5f)*velBinWidth;
	}
	
	public CutHeader(LayerHeader lh,ObservationHeader oh){
		validRefCut=validVelCut=true;
		
		refBinNo=velBinNo=998;
		refRadNo=velRadNo=lh.recordNumber;
		
		refBinWidth=velBinWidth=lh.binWidth/10;
		
		Nyquist=lh.maxV/100f;
		ElAngle=lh.angles/100f;
		
		fstRefBinRng=fstVelBinRng=-refBinWidth/2f;
		
		obsTime=new MDate(oh.sYear,oh.sMonth,oh.sDay,oh.sHour-8,oh.sMinute,oh.sSecond);
		
		radiusRef=new float[refBinNo];
		
		for(int i=0;i<refBinNo;i++) radiusRef[i]=fstRefBinRng+(i+0.5f)*refBinWidth;
		
		radiusVel=radiusRef;	// shallow copy
	}
	
	
	/**
	 * used to print out
	 */
	public String toString(){
		return String.format(
			"Elev (%10.6f deg) has %3d Rads, "+
			"1stGateDist (Ref %4.1f; Vel %4.1f), Gate No. (Ref %4d; Vel %4d), "+
			"Gate Size (Ref %4.1f; Vel %4.1f), NyVel:%6.2f, validRef:%5s, validVel:%5s",
			ElAngle,Math.max(refRadNo,velRadNo),fstRefBinRng,fstVelBinRng,refBinNo,
			velBinNo,refBinWidth,velBinWidth,Nyquist,validRefCut,validVelCut
		);
	}
}
