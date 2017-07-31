/**
 * @(#)AccessRadarBaseData.java	1.0 2015.01.19
 *
 * Copyright 2007 MiniUFO, All rights reserved.
 * MiniUFO Studio. Use is subject to license terms.
 */
package commonapi;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import SASB.RadialScan;
import SC.LayerHeader;
import SC.ObservationHeader;
import SC.PerformanceHeader;
import SC.RVP7Data;
import SC.RadarDataFileHeader;
import SC.RadarSiteHeader;
import SC.RawBin;
import commonapi.CutData;
import commonapi.CutHeader;
import commonapi.ElevationCut;
import commonapi.RadialData;
import commonapi.VCPScan;
import commonapi.RadarStation.State;
import commonapi.RadarStation.Type;


/**
 * parse Radar data into binary float format
 *
 * @version 1.0, 2015.01.19
 * @author  MiniUFO
 * @since   MDK1.0
 */
public final class AccessRadarBaseData{
	//
	public static final int RGates_S      =460;		// S Band reflectivity gates
	public static final int VGates_S      =920;		// S Band velocity gates
	public static final int WGates_S      =920;		// S Band spectrum width gates
	
	public static final int RGates_C      =800;		// C Band reflectivity gates
	public static final int VGates_C      =1600;	// C Band velocity gates
	public static final int WGates_C      =1600;	// C Band spectrum width gates
	
	public static final int MaxCuts       =20;		// maximum cut
	public static final int MaxRads       =375;		// azimuthal number at each cut, one radial data per ~degree
	
	public static final int CODE_INVALID  =0;		// invalid code
	public static final int CODE_RANFOLD  =1;		// range fold code
	
	public static final int VALUE_INVALID =-999;	// invalid data
	public static final int VALUE_RANFOLD = 999;	// range fold data
	
	public static final int RES_POINT_FIVE=2;		// 0.5 m/s velocity resolution
	public static final int RES_ONE_POINT =4;		// 1.0 m/s velocity resolution
	
	public static final int VOL_BEG       =3;		// volume scan begin flag
	public static final int VOL_END       =4;		// volume scan end flag
	
	public static final int ELV_BEG       =0;		// elevation begin flag
	public static final int ELV_MID       =1;		// elevation middle flag
	public static final int ELV_END       =2;		// elevation end flag
	
	public static final int Capacity_SB   =2432;	// size of one RadialScan in bytes for SBand Radar
	public static final int Capacity_CB   =4132;	// size of one RadialScan in bytes for CBand Radar
	
	public static final int HeadSize_SC   =1024;	// header size of one SC-type data file
	public static final int DataSize_SC   =4000;	// data   size of one SC-type data file
	
	
	/**
	 * prevent from construction
	 */
	private AccessRadarBaseData(){}
	
	
	/**
	 * factory method, assuming one VCP scan per file
	 */
	public static VCPScan parseSA(String fname,RadarStation rs){
		List<RadialScan> currRS=null;
		List<List<RadialScan>> allScan=new ArrayList<>();
		
		try(FileInputStream fis=new FileInputStream(fname)){
			FileChannel fc=fis.getChannel();
			
			int capacity=Capacity_SB;
			
			ByteBuffer buf=ByteBuffer.allocate(capacity);
			buf.order(ByteOrder.LITTLE_ENDIAN);
			
			while(true){
				buf.clear(); int status=fc.read(buf); buf.clear();
				
				if(status==-1) break;
				else if(status!=capacity) throw new IllegalArgumentException("incomplete volume scan");
				else{
					RadialScan rd=parseSBandData(buf);
					
					// start a volume scan
					switch(rd.RadialStatus){
					case VOL_BEG:
					case ELV_BEG: currRS=new ArrayList<>(); currRS.add(rd); break;
					case ELV_MID: currRS.add(rd); break;
					case ELV_END:
					case VOL_END: currRS.add(rd); allScan.add(currRS); currRS=null; break;
					default: throw new IllegalArgumentException("invalid radial status: "+rd.RadialStatus);
					}
				}
			}
			
			validRadialScans(allScan);
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		List<ElevationCut> cuts=new ArrayList<>(20);
		
		for(List<RadialScan> lsRS:allScan){
			List<RadialData> currRD=new ArrayList<>();
			
			for(RadialScan scan:lsRS) currRD.add(toRadialData(scan));
			
			CutHeader head=new CutHeader(lsRS);
			CutData data=new CutData(currRD);
			
			cuts.add(new ElevationCut(head,data));
		}
		
		VCPScan scan=new VCPScan(cuts,rs,Type.SA);
		
		return scan;
	}
	
	public static VCPScan parseSC(String fname){
		try(FileInputStream fis=new FileInputStream(fname)){
			FileChannel fc=fis.getChannel();
			
			int headSize=1024;
			int dataSize=4000;
			
			ByteBuffer headBuf=ByteBuffer.allocate(headSize);
			ByteBuffer dataBuf=ByteBuffer.allocate(dataSize);
			
			headBuf.order(ByteOrder.LITTLE_ENDIAN);
			dataBuf.order(ByteOrder.LITTLE_ENDIAN);
			
			int status=fc.read(headBuf); headBuf.clear();
				
			if(status!=headSize) throw new IllegalArgumentException("incomplete volume scan");
			
			RadarDataFileHeader rdfh=parseRadarDataFileHeader(headBuf);
			
			int layerNo=rdfh.oh.scanMode-100;
			
			if(layerNo<=0) throw new IllegalArgumentException("no elevation");
			
			List<ElevationCut> cuts=new ArrayList<>();
			
			for(int k=0;k<layerNo;k++){
				int radNo=rdfh.oh.layers[k].recordNumber;
				
				if(radNo!=360) throw new IllegalArgumentException("invalid radial number "+radNo);
				
				List<RadialData> rads=new ArrayList<>();
				
				float nyquist=(rdfh.oh.layers[k].maxV&0xFFFF)/100f;
				
				for(int j=0;j<radNo;j++){
					status=fc.read(dataBuf); dataBuf.clear();
					
					if(status!=dataSize) throw new IllegalArgumentException("incomplete data: "+status);
					
					RVP7Data rd=parseRVP7Data(dataBuf); rd.Nyquist=nyquist;
					
					rads.add(toRadialData(rd)); dataBuf.clear();
				}
				
				CutHeader head=new CutHeader(rdfh.oh.layers[k],rdfh.oh);
				CutData   data=new CutData(rads);
				cuts.add(new ElevationCut(head,data));
			}
			
			RadarStation rs=new RadarStation(
				rdfh.rsh.longitudeValue/100f,rdfh.rsh.latitudeValue/100f,rdfh.rsh.height/1000f,rdfh.rsh.stationNumber.trim(), 
				rdfh.rsh.province.trim(),rdfh.rsh.station.trim(),Type.SC,State.Built);
			
			return new VCPScan(cuts,rs,Type.SC);
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		throw new IllegalArgumentException("cannot reach here");
	}
	
	
	/*** helper methods ***/
	static RadialScan parseCBandData(ByteBuffer oneRadial){
		RadialScan rs=new RadialScan();
		
		rs.RadarStatus             =oneRadial.getShort(14);
		rs.mSeconds                =oneRadial.getInt(28);
		rs.JulianDate              =oneRadial.getShort(32);
		rs.URange                  =oneRadial.getShort(34);
		rs.Az                      =oneRadial.getShort(36);
		rs.RadialNumber            =oneRadial.getShort(38);
		rs.RadialStatus            =oneRadial.getShort(40);
		rs.El                      =oneRadial.getShort(42);
		rs.ElNumber                =oneRadial.getShort(44);
		rs.RangeToFirstGateOfRef   =oneRadial.getShort(46);
		rs.RangeToFirstGateOfDop   =oneRadial.getShort(48);
		rs.GateSizeOfReflectivity  =oneRadial.getShort(50);
		rs.GateSizeOfDoppler       =oneRadial.getShort(52);
		rs.GateNumberOfReflectivity=oneRadial.getShort(54);
		rs.GateNumberOfDoppler     =oneRadial.getShort(56);
		rs.CutSectorNumber         =oneRadial.getShort(58);
		rs.CalibrationConst        =oneRadial.getFloat(60);
		rs.PtrOfReflectivity       =oneRadial.getShort(64);
		rs.PtrOfVelocity           =oneRadial.getShort(66);
		rs.PtrOfSpectrumWidth      =oneRadial.getShort(68);
		rs.ResolutionOfVelocity    =oneRadial.getShort(70);
		rs.VcpNumber               =oneRadial.getShort(72);
		rs.PtrOfArcReflectivity    =oneRadial.getShort(82);
		rs.PtrOfArcVelocity        =oneRadial.getShort(84);
		rs.PtrOfArcWidth           =oneRadial.getShort(86);
		rs.Nyquist                 =oneRadial.getShort(88);
		//rd.CircleTotal             =oneRadial.getShort(36);
		
		rs.Echodata=new byte[4004];
		oneRadial.position(128);
		oneRadial.get(rs.Echodata,0,4004);
		
		return rs;
	}
	
	private static RadialScan parseSBandData(ByteBuffer oneRadial){
		RadialScan rs=new RadialScan();
		
		rs.RadarStatus             =Short.toUnsignedInt(oneRadial.getShort(14));
		rs.mSeconds                =oneRadial.getInt(28);
		rs.JulianDate              =oneRadial.getShort(32);
		rs.URange                  =Short.toUnsignedInt(oneRadial.getShort(34));
		rs.Az                      =Short.toUnsignedInt(oneRadial.getShort(36));
		rs.RadialNumber            =Short.toUnsignedInt(oneRadial.getShort(38));
		rs.RadialStatus            =Short.toUnsignedInt(oneRadial.getShort(40));
		rs.El                      =oneRadial.getShort(42);
		rs.ElNumber                =oneRadial.getShort(44);
		rs.RangeToFirstGateOfRef   =oneRadial.getShort(46);
		rs.RangeToFirstGateOfDop   =oneRadial.getShort(48);
		rs.GateSizeOfReflectivity  =oneRadial.getShort(50);
		rs.GateSizeOfDoppler       =oneRadial.getShort(52);
		rs.GateNumberOfReflectivity=oneRadial.getShort(54);
		rs.GateNumberOfDoppler     =oneRadial.getShort(56);
		rs.CutSectorNumber         =oneRadial.getShort(58);
		rs.CalibrationConst        =oneRadial.getFloat(60);
		rs.PtrOfReflectivity       =Short.toUnsignedInt(oneRadial.getShort(64));
		rs.PtrOfVelocity           =Short.toUnsignedInt(oneRadial.getShort(66));
		rs.PtrOfSpectrumWidth      =Short.toUnsignedInt(oneRadial.getShort(68));
		rs.ResolutionOfVelocity    =oneRadial.getShort(70);
		rs.VcpNumber               =oneRadial.getShort(72);
		rs.PtrOfArcReflectivity    =Short.toUnsignedInt(oneRadial.getShort(82));
		rs.PtrOfArcVelocity        =Short.toUnsignedInt(oneRadial.getShort(84));
		rs.PtrOfArcWidth           =Short.toUnsignedInt(oneRadial.getShort(86));
		rs.Nyquist                 =oneRadial.getShort(88);
		//rd.CircleTotal             =oneRadial.getShort(36);
		
		rs.Echodata=new byte[2304];
		oneRadial.position(128);
		oneRadial.get(rs.Echodata,0,2304);
		
		return rs;
	}
	
	private static RadialData toRadialData(RadialScan rs){
		int lenRef=rs.GateNumberOfReflectivity;
		int lenVel=rs.GateNumberOfDoppler;
		
		RadialData rd=new RadialData(lenRef,lenVel,dataToDegree(rs.Az));
		
		for(int i=0;i<lenRef;i++)
			rd.ref[i]=decodeSARef(rs.Echodata[rs.PtrOfReflectivity-100+i]);
		
		for(int i=0;i<lenVel;i++){
			rd.vel[i]=decodeSAVel(rs.Echodata[rs.PtrOfVelocity-100+i],rs.ResolutionOfVelocity);
			rd.spw[i]=decodeSASpw(rs.Echodata[rs.PtrOfSpectrumWidth-100+i]);
		}
		
		return rd;
	}
	
	private static RadialData toRadialData(RVP7Data rs){
		int len=rs.rawData.length;
		
		RadialData rd=new RadialData(len,rs.strAz*360f/65536f);
		
		for(int i=0;i<len;i++){
			rd.ref[i]=decodeSCRef(rs.rawData[i].ref&0xFF);
			rd.vel[i]=decodeSCVel(rs.rawData[i].vel&0xFF,rs.Nyquist);
			rd.spw[i]=decodeSCSpw(rs.rawData[i].spw&0xFF,rs.Nyquist);
		}
		
		return rd;
	}
	
	private static RadarSiteHeader parseRadarSiteHeader(ByteBuffer buf){
		RadarSiteHeader rsh=new RadarSiteHeader();
		
		byte[] bytes=null;
		
		bytes=new byte[30]; buf.get(bytes); rsh.country      =new String(bytes);
		bytes=new byte[20]; buf.get(bytes); rsh.province     =new String(bytes);
		bytes=new byte[40]; buf.get(bytes); rsh.station      =new String(bytes);
		bytes=new byte[10]; buf.get(bytes); rsh.stationNumber=new String(bytes);
		bytes=new byte[20]; buf.get(bytes); rsh.radarType    =new String(bytes);
		bytes=new byte[16]; buf.get(bytes); rsh.longitude    =new String(bytes);
		bytes=new byte[16]; buf.get(bytes); rsh.latitude     =new String(bytes);
		
		rsh.longitudeValue=buf.getInt();
		rsh.latitudeValue =buf.getInt();
		rsh.height        =buf.getInt();
		
		rsh.maxAngle  =buf.getShort();
		rsh.optAngle  =buf.getShort();
		rsh.mangFreq  =buf.getShort();
		
		return rsh;
	}
	
	private static PerformanceHeader parsePerformanceHeader(ByteBuffer buf){
		PerformanceHeader ph=new PerformanceHeader();
		
		ph.antennaG=buf.getInt();
		ph.beamV=buf.getShort();
		ph.beamH=buf.getShort();
		ph.polarization=buf.get();
		ph.sideLobe=buf.get();
		ph.power=buf.getInt();
		ph.wavelength=buf.getInt();
		ph.logA=buf.getShort();
		ph.lineA=buf.getShort();
		ph.AGCP=buf.getShort();
		ph.clutterT=buf.get();
		ph.velocityP=buf.get();
		ph.filterP=buf.get();
		ph.noiseT=buf.get();
		ph.SQIT=buf.get();
		ph.intensityC=buf.get();
		ph.intensityR=buf.get();
		
		return ph;
	}
	
	private static LayerHeader parseLayerHeader(ByteBuffer buf){
		LayerHeader lh=new LayerHeader();
		
		lh.ambiguousP=buf.get();
		lh.arotate=buf.getShort();
		lh.prf1=buf.getShort();
		lh.prf2=buf.getShort();
		lh.pulseW=buf.getShort();
		lh.maxV=buf.getShort();
		lh.maxL=buf.getShort();
		lh.binWidth=buf.getShort();
		lh.binNumber=buf.getShort();
		lh.recordNumber=buf.getShort();
		lh.angles=buf.getShort();
		
		return lh;
	}
	
	private static ObservationHeader parseObservationHeader(ByteBuffer buf){
		ObservationHeader oh=new ObservationHeader();
		
		oh.scanMode=buf.get();
		oh.sYear=buf.getShort();
		oh.sMonth=buf.get();
		oh.sDay=buf.get();
		oh.sHour=buf.get();
		oh.sMinute=buf.get();
		oh.sSecond=buf.get();
		oh.timeP=buf.get();
		oh.sMilliSecond=buf.getInt();
		oh.calibration=buf.get();
		oh.intensityI=buf.get();
		oh.velocityP=buf.get();
		
		oh.layers=new LayerHeader[30];
		for(int i=0;i<30;i++) oh.layers[i]=parseLayerHeader(buf);
		
		oh.RHIA=buf.getShort();
		oh.RHIL=buf.getShort();
		oh.RHIH=buf.getShort();
		oh.eYear=buf.getShort();
		oh.eMonth=buf.get();
		oh.eDay=buf.get();
		oh.eHour=buf.get();
		oh.eMinute=buf.get();
		oh.eSecond=buf.get();
		oh.eTenth=buf.get();
		
		return oh;
	}
	
	private static RadarDataFileHeader parseRadarDataFileHeader(ByteBuffer buf){
		RadarDataFileHeader rdfh=new RadarDataFileHeader();
		
		rdfh.rsh=parseRadarSiteHeader(buf);
		rdfh.ph=parsePerformanceHeader(buf);
		rdfh.oh=parseObservationHeader(buf);
		
		buf.get(rdfh.reserved);
		
		return rdfh;
	}
	
	private static RVP7Data parseRVP7Data(ByteBuffer buf){
		RVP7Data data=new RVP7Data();
		
		data.strAz=buf.getShort();
		data.strEl=buf.getShort();
		data.endAz=buf.getShort();
		data.endEl=buf.getShort();
		
		for(int i=0,I=data.rawData.length;i<I;i++){
			RawBin rb=new RawBin();
			
			rb.ref=buf.get();
			rb.vel=buf.get();
			rb.unc=buf.get();
			rb.spw=buf.get();
			
			data.rawData[i]=rb;
		}
		
		return data;
	}
	
	
	private static float decodeSARef(byte code){
		if(code==CODE_INVALID) return VALUE_INVALID;
		else if(code==CODE_RANFOLD) return VALUE_RANFOLD;
		else return (Byte.toUnsignedInt(code)-2f)/2f-32.5f;
	}
	
	private static float decodeSCRef(int code){
		if(code==0) return VALUE_INVALID;
		else return ((float)code-64f)/2f;
	}
	
	private static float decodeSAVel(byte code,int resType){
		if(code==CODE_INVALID) return VALUE_INVALID;
		else if(code==CODE_RANFOLD) return VALUE_RANFOLD;
		else{
			if(resType==RES_POINT_FIVE) return (Byte.toUnsignedInt(code)-2f)/2f-63.5f;
			else return (Byte.toUnsignedInt(code)-2f)/2f-127f;
		}
	}
	
	private static float decodeSCVel(int code,float maxVel){
		if(code==0) return VALUE_INVALID;
		else return (code-128f)/128f*maxVel;
	}
	
	private static float decodeSASpw(byte code){
		if(code==CODE_INVALID) return VALUE_INVALID;
		else if(code==CODE_RANFOLD) return VALUE_RANFOLD;
		else return (Byte.toUnsignedInt(code)-2f)/2f-63.5f;
	}
	
	private static float decodeSCSpw(int code,float maxVel){
		if(code==0) return VALUE_INVALID;
		else return code/256f*maxVel;
	}
	
	private static float dataToDegree(int data){ return data/8f*(180f/4096f);}
	
	private static void validRadialScans(List<List<RadialScan>> allScan){
		if(allScan.get(0).get(0).RadialStatus!=VOL_BEG)
			throw new IllegalArgumentException("no volume begin found");
		if(allScan.get(allScan.size()-1).get(allScan.get(allScan.size()-1).size()-1).RadialStatus!=VOL_END)
			throw new IllegalArgumentException("no volume end found");
		
		for(int i=1,I=allScan.size()-1;i<I;i++){
			List<RadialScan> tmp=allScan.get(i);
			
			if(tmp.get(0).RadialStatus!=ELV_BEG)
				throw new IllegalArgumentException("no elevation begin found");
			if(tmp.get(tmp.size()-1).RadialStatus!=ELV_END)
				throw new IllegalArgumentException("no elevation end found");
		}
	}
	
	
	/*** test ***/
	public static void main(String[] args){
		String path="D:/";
		String fname=path+"DOR_SA_CAP.bin";
		
		RadarStation rs=RadarStation.getRadarStation("Z9791");
		
		VCPScan scan=AccessRadarBaseData.parseSA(fname,rs);
		
		System.out.println(scan);
		
		PlotGS plt=new PlotGS(scan);
		//plt.refDataToGS(RadarColorBar.RefBar,"d:/Ref.gs");
		plt.velDataToGS(RadarColorBar.VelBar,"d:/Vel.gs");
		//plt.spwDataToGS(RadarColorBar.SpwBar,"d:/Spw.gs");
	}
}
