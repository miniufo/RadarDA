/**
 * @(#)Decode.java	1.0 2016.11.08
 *
 * Copyright 2007 MiniUFO, All rights reserved.
 * MiniUFO Studio. Use is subject to license terms.
 */
package pack;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import commonapi.AccessRadarBaseData;
import commonapi.ElevationCut;
import commonapi.PlotGS;
import commonapi.RadarColorBar;
import commonapi.RadarStation;
import commonapi.RadarStation.Type;
import commonapi.VCPScan;
import miniufo.basic.ArrayUtil;
import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.MDate;
import miniufo.diagnosis.SpatialModel;
import miniufo.diagnosis.Variable;
import miniufo.io.IOUtil;
import miniufo.io.TextReader;
import miniufo.util.GridDataFetcher;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static miniufo.diagnosis.SpatialModel.EARTH_RADIUS;


/**
 * parse Radar data into binary float format for GSI
 *
 * @version 1.0, 2016.11.08
 * @author  MiniUFO
 * @since   MDK1.0
 */
public final class Decode{
	//
	private VCPScan scan=null;
	
	private DealiaseWay way=null;
	
	private static String path="D:/Data/RadarDA/DataAssim/";
	
	public enum DealiaseWay{Haikou,Sanya}
	
	
	/**
	 * constructor
	 */
	public Decode(VCPScan scan,DealiaseWay way){ this.scan=scan; this.way=way;}
	
	
	void dealiasing(){
		switch(way){
		case Haikou: for(int k=0;k<scan.getValidVelCuts();k++) dealiasingHaikou(k); break;
		case Sanya : for(int k=0;k<scan.getValidVelCuts();k++) dealiasingSanya (k); break;
		default: throw new IllegalArgumentException("unsupported way "+way);
		}
	}
	
	void innovation(String pname){
		DiagnosisFactory df=DiagnosisFactory.parseFile(pname+"ExpHigh/ctrl/data2014071800.ctl");
		DataDescriptor dd=df.getDataDescriptor();
		
		float undef=dd.getUndef(null);
		
		GridDataFetcher gdf=new GridDataFetcher(dd);
		RadarStation rs=scan.getRadarStation();
		
		Variable uo=gdf.prepareXYZBuffer("u",1,1,23,5);
		Variable vo=gdf.prepareXYZBuffer("v",1,1,23,5);
		
		for(int k=0,K=scan.getValidVelCuts();k<K;k++){
			ElevationCut ec=scan.getValidVelElev(k);
			
			float elvVel =ec.getElAngle();
			float width  =ec.getVelBinWidth();
			
			float[][] vel=ec.getVelData();
			float[]   azm=ec.getAzms();
			float[]   rad=ec.getVelRads();
			
			float[][] bgv=new float[vel.length][];
			float[][] inc=new float[vel.length][];
			
			for(int i=0,I=vel.length;i<I;i++){
				bgv[i]=vel[i].clone();
				inc[i]=vel[i].clone();
			}
			
			for(int j=0,J=   vel.length;j<J;j++)
			for(int i=0,I=vel[j].length;i<I;i++){
				double azimuth=azm[j];
				double radial =rad[i];
				
				float[] lonlat=SpatialModel.cLatLon(rs.getLon(),rs.getLat(),360-azimuth,Math.toDegrees(radial/EARTH_RADIUS));
				float elv=(float)(radial*Math.sin(Math.toRadians(elvVel)))+rs.getElv();
				
				if(elv>=250&&elv<=20000){ // in ctl zdef range
					float u=gdf.fetchXYZBuffer(lonlat[0],lonlat[1],elv/1000f,uo);
					float v=gdf.fetchXYZBuffer(lonlat[0],lonlat[1],elv/1000f,vo);
					
					float vr=(u!=undef&&v!=undef)?
						(float)(-u*sin(Math.toRadians(lonlat[2]))+v*cos(Math.toRadians(lonlat[2]))):
						AccessRadarBaseData.VALUE_INVALID;
					
					bgv[j][i]=vr;
					if(PlotGS.isValidData(inc[j][i])) inc[j][i]-=vr;
				}
			}
			
			boolean largeRng=scan.getRadarType()==Type.SA?false:true;
			
			PlotGS.dataToGS(
				bgv,azm,rad,width,elvVel,largeRng,rs,new RadarColorBar(-44,44,4),
				"background",pname+"innov/BKG_"+way+k+".gs"
			);
			
			PlotGS.dataToGS(
				inc,azm,rad,width,elvVel,largeRng,rs,new RadarColorBar(-20,20,2),
				"increment",pname+"innov/Inno_"+way+k+".gs"
			);
		}
	}
	
	void baseData2SuperObsInput(String fname){
		RadarStation rs=scan.getRadarStation();
		StringBuilder sb=new StringBuilder();
		
		for(int k=0,K=scan.getValidVelCuts();k<K;k++){
			ElevationCut ec=scan.getValidVelElev(k);
			
			MDate md=ec.getObsTime();
			
			float elvVel =ec.getElAngle();
			
			float[][] vel=ec.getVelData();
			float[]   azm=ec.getAzms();
			float[]   rad=ec.getVelRads();
			
			sb.append(" "+rs.getID()+"\n");
			sb.append(" 22\n");
			sb.append(" "+md.getYear()+" "+md.getMonth ()+" "+md.getDate()+
					  " "+md.getHour()+" "+md.getMinute()+" "+md.getSecond()+"\n");
			sb.append(" "+rs.getLat()+" "+rs.getLon()+" "+rs.getElv()+"\n");
			sb.append(" "+rad[0]+" "+ec.getVelBinWidth()+"\n");
			sb.append(" "+elvVel+"\n");
			sb.append(" "+vel.length+" "+ec.getVelBinNumber()+"\n");
			sb.append(" "+ec.getNyquistVelocity()+"\n");
			
			for(int j=0,J=vel.length;j<J;j++){
				sb.append(String.format("%6.1f",azm[j]));
				if((j+1)%15==0) sb.append("\n");
			}
			
			if(vel.length%15!=0) sb.append("\n");
			
			for(int j=0,cc=0,J=vel.length;j<J;j++)
			for(int i=0,I=vel[j].length;i<I;i++){
				sb.append(String.format("%6.1f",PlotGS.isValidData(vel[j][i])?vel[j][i]:-64.5f));
				if((cc+1)%20==0) sb.append("\n");
				cc++;
			}
			
			//boolean largeRng=scan.getRadarType()==Type.SA?false:true;
			
			//float binWidth=scan.getValidVelElev(0).getVelBinWidth();
			
			//PlotGS.dataToGS(
			//	vel,azm,rad,elvVel,binWidth,largeRng,rs,new RadarColorBar(-44,44,4),
			//	"radial velocity",path+"Rammasun/Decode/dealiased"+k+".gs"
			//);
			
			if(k!=K-1) sb.append("\n");
		}
		
		try(FileWriter fw=new FileWriter(fname)){ fw.write(sb.toString());}
		catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	void superObsInput2GS(String fin,String fout){
		try(BufferedReader br=new BufferedReader(new FileReader(fin))){
			int kk=0;
			
			while(true){
				String fstLine=br.readLine();
				
				if(fstLine==null||"".equals(fstLine)) break;
				
				br.readLine(); br.readLine(); br.readLine();
				
				String[] tokens=br.readLine().trim().split(" ");
				
				float fstRange=Float.parseFloat(tokens[0]);
				float gateSize=Float.parseFloat(tokens[1]);
				
				float elv=Float.parseFloat(br.readLine().trim());
				
				tokens=br.readLine().trim().split(" ");
				
				int xc=Integer.parseInt(tokens[0]);
				int yc=Integer.parseInt(tokens[1]);
				
				br.readLine();
				
				float[]   azms=new float[xc];
				float[]   rads=new float[yc];
				float[][] vels=new float[xc][yc];
				
				for(int j=0;j<yc;j++) rads[j]=fstRange+j*gateSize;
				
				for(int i=0,I=xc/15+(xc%15==0?0:1),ptr=0;i<I;i++){
					tokens=ArrayUtil.splitByLength(br.readLine(),6);
					
					for(int tt=0,TT=tokens.length;tt<TT;tt++,ptr++)
					azms[ptr]=Float.parseFloat(tokens[tt]);
				}
				
				for(int i=0,I=xc*yc/20+(xc*yc%20==0?0:1),ptr=0;i<I;i++){
					tokens=ArrayUtil.splitByLength(br.readLine(),6);
					
					for(int tt=0,TT=tokens.length;tt<TT;tt++,ptr++){
						float val=Float.parseFloat(tokens[tt]);
						
						int aIdx=ptr/yc,rIdx=ptr%yc;
						vels[aIdx][rIdx]=val==-64.5?AccessRadarBaseData.VALUE_INVALID:val;
					}
				}
				
				br.readLine();
				
				SuperObsInput asc=new SuperObsInput(vels,azms,rads,elv,gateSize);
				
				boolean largeRng=scan.getRadarType()==Type.SA?false:true;
				
				PlotGS.dataToGS(asc.vels,asc.azms,asc.rads,asc.dRad,asc.elv,largeRng,scan.getRadarStation(),
				new RadarColorBar(-44,44,4),"radial velocity",IOUtil.getCompleteFileNameWithoutExtension(fout)+kk+".gs");
				
				System.out.println("finished the "+(kk++)+" th elevation");
			}
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	void superObsOutput2GS(String fin,String fout){
		Map<Float,List<float[]>> map=null;
		RadarStation rs=scan.getRadarStation();
		
		try(Stream<float[]> lns=Files.lines(Paths.get(fin)).map(Decode::parseOneLine)){
			map=lns.collect(Collectors.groupingBy(floats->floats[floats.length-1]));
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		int kk=0;
		for(Map.Entry<Float,List<float[]>> entry:map.entrySet()){
			float key=entry.getKey();System.out.println(key);
			List<float[]> lst=entry.getValue();
			
			float[] lons=new float[lst.size()];
			float[] lats=new float[lst.size()];
			float[] vels=new float[lst.size()];
			
			for(int l=0,L=lst.size();l<L;l++){
				float[] f=lst.get(l);
				
				lons[l]=f[0];
				lats[l]=f[1];
				vels[l]=f[2];
			}
			
			boolean largeRng=scan.getRadarType()==Type.SA?false:true;
			
			PlotGS.dataToGS(rs.getLon(),rs.getLat(),vels,lons,lats,largeRng,new RadarColorBar(-44,44,4),
			"radial velocity at Elv "+key,IOUtil.getCompleteFileNameWithoutExtension(fout)+(kk++)+".gs");
		}
	}
	
	void setuprw2GS(String fin,String foutpath){
		//  1	observation type
		//	2	observation subtype
		//	3	observation latitude (degree)
		//	4	observation longitude (degree)
		//	5	station elevation (meters)
		//	6	observation pressure (hPa)
		//	7	observation height (meters)
		//	8	obs time (hours relative to analysis time)
		//	9	observation subtype
		//	10	setup qc or even mark
		//	11	read_prepbufr data usage flag
		//	12	analysis usage flag (1=use, -1=not used)
		//	13	nonlinear qc relative weight
		//	14	prepbufr inverse obs error (m/s)**-1
		//	15	read_prepbufr inverse obs error (m/s)**-1
		//	16	final inverse observation error (m/s)**-1
		//	17	radial wind speed observation (m/s)
		//	18	obs-ges used in analysis (m/s)
		//	19	obs-ges w/o bias correction (m/s) (future slot)
		//	20	azimuth angle
		//	21	tilt angle
		//	22	10m wind reduction factor
		
		RadarStation rs=scan.getRadarStation();
		
		float olon=rs.getLon(),olat=rs.getLat();
		
		float[][] data=TextReader.readColumnsF(fin,true,3,4,12,17,18,19);
		
		boolean largeRng=scan.getRadarType()==Type.SA?false:true;
		
		//PlotGS.dataToGS(olon,olat,data[2],data[1],data[0],new RadarColorBar(-2, 2,1  ),"usage flag" ,foutpath+"setuprwFlag.gs");
		PlotGS.dataToGS(olon,olat,data[3],data[1],data[0],largeRng,new RadarColorBar(-44,44,4),"radial wind",foutpath+"setuprwVel.gs");
		PlotGS.dataToGS(olon,olat,data[4],data[1],data[0],largeRng,new RadarColorBar(-8 ,8 ,1),"obs-ges"    ,foutpath+"setuprwInnov.gs");
		PlotGS.dataToGS(olon,olat,data[5],data[1],data[0],largeRng,new RadarColorBar(-8 ,8 ,1),"obs-ges WO" ,foutpath+"setuprwInnovWO.gs");
	}
	
	
	/*** helper methods ***/
	private void dealiasingHaikou(int k){
		ElevationCut cut=scan.getValidVelElev(k);
		
		float Nyquist=cut.getNyquistVelocity();
		
		float[]   azm=cut.getAzms();
		float[][] vel=cut.getVelData();
		
		for(int j=0,J=cut.getVelRadialNumber();j<J;j++){
			/////////// velocity de-aliasing ///////////
			double azimuth=azm[j];
			
			if(k==0||k==1){
				/////////// for TC core de-aliasing ///////////
				if(!(azimuth>150&&azimuth<180))
				for(int i=560,I=cut.getVelBinNumber();i<I;i++){
					float mean=getMean(vel[j],i-1);
					
					if(mean!=AccessRadarBaseData.VALUE_INVALID&&PlotGS.isValidData(vel[j][i])){
						if(     vel[j][i]-mean> Nyquist*1.2f) vel[j][i]-=2f*Nyquist;
						else if(vel[j][i]-mean<-Nyquist*1.2f) vel[j][i]+=2f*Nyquist;
						
						if(     vel[j][i]-mean> Nyquist*0.46f&&vel[j][i]-mean< Nyquist*1.2f) vel[j][i]-=Nyquist;
						else if(vel[j][i]-mean<-Nyquist*0.46f&&vel[j][i]-mean>-Nyquist*1.2f) vel[j][i]+=Nyquist;
					}
				}
				
				for(int ii=0;ii<360;ii++){
					if(PlotGS.isValidData(vel[j][ii])&&vel[j][ii]> Nyquist*0.8f) vel[j][ii]-=Nyquist;
					if(PlotGS.isValidData(vel[j][ii])&&vel[j][ii]<-Nyquist*0.8f) vel[j][ii]+=Nyquist;
				}
				
				/////////// for specific section de-aliasing ///////////
				if(azimuth>0&&azimuth<105)
				for(int ii=0;ii<560;ii++){
					if(PlotGS.isValidData(vel[j][ii])&&vel[j][ii]> Nyquist*0.4f) vel[j][ii]-=Nyquist;
					if(PlotGS.isValidData(vel[j][ii])&&vel[j][ii]<-Nyquist*1.0f) vel[j][ii]+=Nyquist;
				}
				
				if((azimuth>110&&azimuth<310))
				for(int ii=0;ii<560;ii++){
					if(PlotGS.isValidData(vel[j][ii])&&vel[j][ii]> Nyquist*0.8f) vel[j][ii]-=Nyquist;
					if(PlotGS.isValidData(vel[j][ii])&&vel[j][ii]<-Nyquist*0.4f) vel[j][ii]+=Nyquist;
				}
			}
			
			if(k>=2&&k<=5){
				if(azimuth>30&&azimuth<115)
				for(int i=0,I=vel[j].length;i<I;i++) if(PlotGS.isValidData(vel[j][i])){
					if(vel[j][i]>Nyquist*0.5f) vel[j][i]-=Nyquist*2f;
				}
				
				if(azimuth>115&&azimuth<130)
				for(int i=0,I=vel[j].length;i<I;i++) if(PlotGS.isValidData(vel[j][i])){
					if(vel[j][i]<-Nyquist*0.4f) vel[j][i]+=Nyquist*2f;
				}
				
				if(azimuth>120&&azimuth<130)
				for(int i=800,I=vel[j].length;i<I;i++) if(PlotGS.isValidData(vel[j][i])){
					if(vel[j][i]<-Nyquist*0.2f) vel[j][i]+=Nyquist*2f;
				}
			}
			
			if(k==7||k==8)
			for(int i=0,I=vel[j].length;i<I;i++) vel[j][i]=AccessRadarBaseData.VALUE_INVALID;
		}
		
		// copy back to ElevationCut
		for(int j=0,J=cut.getVelRadialNumber();j<J;j++)
		for(int i=0,I=cut.getVelBinNumber()   ;i<I;i++) cut.setDataValue(j,i,vel[j][i]);
	}
	
	private void dealiasingSanya(int k){
		ElevationCut cut=scan.getValidVelElev(k);
		
		float Nyquist=cut.getNyquistVelocity();
		
		float[]   azm=cut.getAzms();
		float[][] vel=cut.getVelData();
		
		for(int j=0,J=cut.getVelRadialNumber();j<J;j++){
			/////////// velocity de-aliasing ///////////
			double azimuth=azm[j];
			
			if(k==0){
				/////////// for TC core de-aliasing ///////////
				if((azimuth>10&&azimuth<110&&!(azimuth>40&&azimuth<44)))
				for(int i=450,I=cut.getVelBinNumber();i<I;i++){
					float mean=getMean(vel[j],i-1);
					
					if(mean!=AccessRadarBaseData.VALUE_INVALID&&PlotGS.isValidData(vel[j][i])){
						if(     vel[j][i]-mean> Nyquist*1.2f) vel[j][i]-=2f*Nyquist;
						else if(vel[j][i]-mean<-Nyquist*1.2f) vel[j][i]+=2f*Nyquist;
						
						if(     vel[j][i]-mean> Nyquist*0.46f&&vel[j][i]-mean< Nyquist*1.2f) vel[j][i]-=Nyquist;
						else if(vel[j][i]-mean<-Nyquist*0.46f&&vel[j][i]-mean>-Nyquist*1.2f) vel[j][i]+=Nyquist;
					}
				}
				
				if((azimuth>0&&azimuth<80))
				for(int i=5,I=300;i<I;i++){
					if(PlotGS.isValidData(vel[j][i])){
						if(     vel[j][i]> Nyquist*0.46f&&vel[j][i]< Nyquist*1.2f) vel[j][i]-=Nyquist;
					}
				}
				
				if((azimuth>150&&azimuth<180))
				for(int i=550,I=cut.getVelBinNumber();i<I;i++){
					if(PlotGS.isValidData(vel[j][i])){
						if(     vel[j][i]-5> Nyquist*0.46f&&vel[j][i]-5< Nyquist*1.2f) vel[j][i]-=Nyquist;
						else if(vel[j][i]+5<-Nyquist*0.46f&&vel[j][i]+5>-Nyquist*1.2f) vel[j][i]+=Nyquist;
					}
				}
				
				if((azimuth>180&&azimuth<330))
				for(int i=20,I=cut.getVelBinNumber();i<I;i++){
					if(PlotGS.isValidData(vel[j][i])){
						if(     vel[j][i]> Nyquist*0.46f&&vel[j][i]< Nyquist*1.2f) vel[j][i]-=Nyquist;
						else if(vel[j][i]<-Nyquist*0.46f&&vel[j][i]>-Nyquist*1.2f) vel[j][i]+=Nyquist;
					}
				}
				
				if((azimuth>60&&azimuth<90))
				for(int i=4,I=500;i<I;i++){
					if(PlotGS.isValidData(vel[j][i])){
						if(     vel[j][i]-0> Nyquist*0.46f&&vel[j][i]-0< Nyquist*1.2f) vel[j][i]-=Nyquist;
						else if(vel[j][i]-0<-Nyquist*0.46f&&vel[j][i]-0>-Nyquist*1.2f) vel[j][i]+=Nyquist;
					}
				}
			}
			
			if(k==1){
				/////////// for TC core de-aliasing ///////////
				if((azimuth>45&&azimuth<110)||(azimuth>120&&azimuth<150))
				for(int i=450,I=cut.getVelBinNumber();i<I;i++){
					float mean=getMean(vel[j],i-1);
					
					if(mean!=AccessRadarBaseData.VALUE_INVALID&&PlotGS.isValidData(vel[j][i])){
						if(     vel[j][i]-mean> Nyquist*1.2f) vel[j][i]-=2f*Nyquist;
						else if(vel[j][i]-mean<-Nyquist*1.2f) vel[j][i]+=2f*Nyquist;
						
						if(     vel[j][i]-mean> Nyquist*0.46f&&vel[j][i]-mean< Nyquist*1.2f) vel[j][i]-=Nyquist;
						else if(vel[j][i]-mean<-Nyquist*0.46f&&vel[j][i]-mean>-Nyquist*1.2f) vel[j][i]+=Nyquist;
					}
				}
				
				if((azimuth>0&&azimuth<60))
				for(int i=250,I=cut.getVelBinNumber();i<I;i++){
					if(PlotGS.isValidData(vel[j][i])){
						if(     vel[j][i]> Nyquist*0.8f) vel[j][i]-=Nyquist;
					}
				}
				
				if((azimuth>180&&azimuth<330))
				for(int i=20,I=cut.getVelBinNumber();i<I;i++){
					if(PlotGS.isValidData(vel[j][i])){
						if(     vel[j][i]-4> Nyquist*0.46f&&vel[j][i]-4< Nyquist*1.2f) vel[j][i]-=Nyquist;
						else if(vel[j][i]+4<-Nyquist*0.46f&&vel[j][i]+4>-Nyquist*1.2f) vel[j][i]+=Nyquist;
					}
				}
			}
			
			if(k==2){
				/////////// for TC core de-aliasing ///////////
				if((azimuth>120&&azimuth<153))
				for(int i=400,I=cut.getVelBinNumber();i<I;i++){
					float mean=getMean(vel[j],i-1);
					
					if(mean!=AccessRadarBaseData.VALUE_INVALID&&PlotGS.isValidData(vel[j][i])){
						if(     vel[j][i]-mean> Nyquist*0.8f) vel[j][i]-=1f*Nyquist;
						else if(vel[j][i]-mean<-Nyquist*0.8f) vel[j][i]+=1f*Nyquist;
					}
				}
				
				if((azimuth>60&&azimuth<100))
				for(int i=200,I=cut.getVelBinNumber();i<I;i++){
					if(PlotGS.isValidData(vel[j][i])){
						if(     vel[j][i]> Nyquist*0.8f) vel[j][i]-=Nyquist;
						if(     vel[j][i]<-Nyquist*0.8f) vel[j][i]+=Nyquist;
					}
				}
				
				if((azimuth>180&&azimuth<330))
				for(int i=20,I=cut.getVelBinNumber();i<I;i++){
					if(PlotGS.isValidData(vel[j][i])){
						if(     vel[j][i]-4> Nyquist*0.46f&&vel[j][i]-4< Nyquist*1.2f) vel[j][i]-=Nyquist;
						else if(vel[j][i]+4<-Nyquist*0.46f&&vel[j][i]+4>-Nyquist*1.2f) vel[j][i]+=Nyquist;
					}
				}
			}
			
			if(k==3){
				if((azimuth>60&&azimuth<150))
				for(int i=100,I=cut.getVelBinNumber();i<I;i++){
					if(PlotGS.isValidData(vel[j][i])){
						//if(     vel[j][i]> Nyquist*0.8f) vel[j][i]-=Nyquist;
						if(     vel[j][i]<-Nyquist*0.8f) vel[j][i]+=Nyquist;
					}
				}
				
				if((azimuth>210&&azimuth<330))
				for(int i=20,I=cut.getVelBinNumber();i<I;i++){
					if(PlotGS.isValidData(vel[j][i])){
						if(     vel[j][i]-4> Nyquist*0.46f&&vel[j][i]-4< Nyquist*1.2f) vel[j][i]-=Nyquist;
						else if(vel[j][i]+4<-Nyquist*0.46f&&vel[j][i]+4>-Nyquist*1.2f) vel[j][i]+=Nyquist;
					}
				}
			}
			
			if(k==4){
				if((azimuth>60&&azimuth<100))
				for(int i=100,I=cut.getVelBinNumber();i<I;i++){
					if(PlotGS.isValidData(vel[j][i])){
						//if(     vel[j][i]> Nyquist*0.8f) vel[j][i]-=Nyquist;
						if(     vel[j][i]<-Nyquist*0.8f) vel[j][i]+=Nyquist;
					}
				}
				
				if((azimuth>120&&azimuth<150))
				for(int i=300,I=cut.getVelBinNumber();i<I;i++){
					if(PlotGS.isValidData(vel[j][i])){
						//if(     vel[j][i]> Nyquist*0.8f) vel[j][i]-=Nyquist;
						if(     vel[j][i]<-Nyquist*0.8f) vel[j][i]+=Nyquist;
					}
				}
				
				if((azimuth>180&&azimuth<330))
				for(int i=20,I=cut.getVelBinNumber();i<I;i++){
					if(PlotGS.isValidData(vel[j][i])){
						if(     vel[j][i]-4> Nyquist*0.46f&&vel[j][i]-4< Nyquist*1.2f) vel[j][i]-=Nyquist;
						else if(vel[j][i]+4<-Nyquist*0.46f&&vel[j][i]+4>-Nyquist*1.2f) vel[j][i]+=Nyquist;
					}
				}
			}
		}
		
		// copy back to ElevationCut
		for(int j=0,J=cut.getVelRadialNumber();j<J;j++)
		for(int i=0,I=cut.getVelBinNumber()   ;i<I;i++) cut.setDataValue(j,i,vel[j][i]);
	}
	
	private static float getMean(float[] data,int idx){
		double mean=0;
		int count=0;
		
		for(int i=idx;i>=0;i--) if(PlotGS.isValidData(data[i])){ mean+=data[i]; count++; if(count==35) break;}
		
		if(count!=0) return (float)(mean/count);
		else return AccessRadarBaseData.VALUE_INVALID;
	}
	
	private static float[] parseOneLine(String oneline){
		float[] re=new float[4];
		
		String[] tokens=oneline.trim().split("[\\s\\t]+");
		
		re[0]=Float.parseFloat(tokens[6]);	// lon
		re[1]=Float.parseFloat(tokens[5]);	// lat
		re[2]=Float.parseFloat(tokens[8]);	// vel
		re[3]=Float.parseFloat(tokens[11]);	// elv
		
		return re;
	}
	
	
	private static final class SuperObsInput{
		float elv =0;
		float dRad=0;
		
		float[][] vels=null;
		float[]   azms=null;
		float[]   rads=null;
		
		//
		public SuperObsInput(float[][] vels,float[] azms,float[] rads,float elv,float dRad){
			this.vels=vels;
			this.azms=azms;
			this.rads=rads;
			this.elv =elv;
			this.dRad=dRad;
		}
	}
	
	
	/*** test ***/
	public static void main(String[] args){
		Haikou();
		Sanya();
	}
	
	static void Sanya(){
		/*** Sanya ***/
		String id="Z9070";
		String time="20140718000500";
		String fname="Z_RADR_I_"+id+"_"+time+"_O_DOR_SC_CAP.bin";
		
		VCPScan scan=AccessRadarBaseData.parseSC(path+fname);
		System.out.println(scan);
		
		//PlotGS plt=new PlotGS(scan);
		Decode dcd=new Decode(scan,DealiaseWay.Sanya);
		//plt.velDataToGS(new RadarColorBar(-44,44,4),"d:/SanyaVel.gs");
		
		//dcd.dealiasing();
		
		//plt.refDataToGS(RadarColorBar.RefBar       ,"d:/SanyaRef.gs");
		//plt.velDataToGS(new RadarColorBar(-44,44,4),"d:/SanyaVel_de.gs");
		//plt.spwDataToGS(RadarColorBar.SpwBar       ,"d:/SanyaSpw.gs");
		
		dcd.innovation(path+"Rammasun/");
		//dcd.baseData2SuperObsInput(path+"Rammasun/nceptest_vel_Sanya.txt");
		//dcd.superObsInput2GS(path+"Rammasun/nceptest_vel_Sanya.txt",path+"Rammasun/Decode/superObsInput_Sanya.gs");
		//dcd.superObsOutput2GS(path+"Rammasun/radar_supobs_from_level2.txt",path+"Rammasun/Decode/superObsOutput_All.gs");
		//dcd.setuprw2GS(path+"Rammasun/ExpHigh/rwLclm2/radarobs",path+"Rammasun/ExpHigh/rwLclm2/");
	}
	
	static void Haikou(){
		/*** Haikou, Hainan ***/
		String id="Z9898";
		String time="20140718000100";
		String fname="Z_RADR_I_"+id+"_"+time+"_O_DOR_SA_CAP.bin";
		
		RadarStation rs=RadarStation.getRadarStation(id);
		
		VCPScan scan=AccessRadarBaseData.parseSA(path+fname,rs);
		System.out.println(scan);
		System.out.println(rs);
		
		//PlotGS plt=new PlotGS(scan);
		Decode dcd=new Decode(scan,DealiaseWay.Haikou);
		
		//plt.velDataToGS(new RadarColorBar(-44,44,4),"d:/HaikouVel.gs");
		//dcd.dealiasing();
		//plt.refDataToGS(RadarColorBar.RefBar       ,"d:/HaikouRef.gs");
		//plt.velDataToGS(new RadarColorBar(-44,44,4),"d:/HaikouVel_de.gs");
		//plt.spwDataToGS(RadarColorBar.SpwBar       ,"d:/HaikouSpw.gs");
		
		dcd.innovation(path+"Rammasun/");
		//dcd.baseData2SuperObsInput(path+"Rammasun/nceptest_vel_Haikou.txt");
		//dcd.superObsInput2GS(path+"Rammasun/nceptest_vel_Haikou.txt",path+"Rammasun/Decode/superObsInput_Haikou.gs");
		//dcd.superObsOutput2GS(path+"Rammasun/radar_supobs_from_level2.txt",path+"Rammasun/Decode/superObsOutput_All.gs");
		//dcd.setuprw2GS(path+"Rammasun/radarobs",path+"Haikou/");
		
		//VCPVel2SuperObsInput(scan,rs,path+"Rammasun/Experiments/Compare/Data.ctl",path+"Rammasun/ncepIdealizedTest.txt",true);
		//superObsOutput2GS(3450,path+"Rammasun/radar_supobs_from_level2Test.txt",path+"Rammasun/haikou3Test.gs");
		//setuprw2GS(3305,path+"Rammasun/radarobs",path+"Haikou/");
	}
}
