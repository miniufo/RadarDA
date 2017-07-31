//
package commonapi;

import java.io.FileWriter;
import java.io.IOException;
import commonapi.RadarStation.Type;
import miniufo.diagnosis.SpatialModel;
import miniufo.io.IOUtil;
import static miniufo.diagnosis.SpatialModel.EARTH_RADIUS;


//
public final class PlotGS{
	//
	private VCPScan scan=null;
	
	
	/**
	 * constructor
	 */
	public PlotGS(VCPScan scan){ this.scan=scan;}
	
	
	/**
	 * write radar data at different level to gs for plotting
	 */
	public void refDataToGS(int level,RadarColorBar bar,String fname){ refsDataToGS(bar,fname,scan.getValidRefElev(level));}
	
	public void refDataToGS(RadarColorBar bar,String fname){
		int valid=scan.getValidRefCuts();
		
		ElevationCut[] cuts=new ElevationCut[valid];
		
		for(int k=0,K=valid;k<K;k++) cuts[k]=scan.getValidRefElev(k);
		
		refsDataToGS(bar,fname,cuts);
	}
	
	public void velDataToGS(int level,RadarColorBar bar,String fname){ velsDataToGS(bar,fname,scan.getValidVelElev(level));}
	
	public void velDataToGS(RadarColorBar bar,String fname){
		int valid=scan.getValidVelCuts();
		
		ElevationCut[] cuts=new ElevationCut[valid];
		
		for(int k=0,K=valid;k<K;k++) cuts[k]=scan.getValidVelElev(k);
		
		velsDataToGS(bar,fname,cuts);
	}
	
	public void spwDataToGS(int level,RadarColorBar bar,String fname){ spwsDataToGS(bar,fname,scan.getValidVelElev(level));}
	
	public void spwDataToGS(RadarColorBar bar,String fname){
		int valid=scan.getValidVelCuts();
		
		ElevationCut[] cuts=new ElevationCut[valid];
		
		for(int k=0,K=valid;k<K;k++) cuts[k]=scan.getValidVelElev(k);
		
		spwsDataToGS(bar,fname,cuts);
	}
	
	
	public static void dataToGS(float olon,float olat,float[] vels,float[] lons,float[] lats,boolean largeRng,RadarColorBar bar,String dname,String fname){
		StringBuilder sb=new StringBuilder();
		
		addGSHeader(sb,olon,olat,fname,largeRng);
		addGSBGMap(sb);
		
		float dlon=0.02f;
		float dlat=0.02f;
		
		for(int l=0,L=vels.length;l<L;l++)
		if(isValidData(vels[l])){
			String pos=String.format(
				"%9.5f %9.5f   %9.5f %9.5f   %9.5f %9.5f   %9.5f %9.5f",
				lons[l]-dlon,lats[l]-dlat,lons[l]+dlon,lats[l]-dlat,lons[l]+dlon,lats[l]+dlat,lons[l]-dlon,lats[l]+dlat
			);
			
			sb.append("'drawpolyfrgb  "+pos+"  "+bar.getColorString(vels[l])+"'\n");
		}
		
		sb.append("'draw title "+dname+"'\n\n");
		
		addGSCircles(sb,olon,olat,largeRng);
		addGSStation(sb,olon,olat);
		addColorbar(sb,bar,olon,olat,largeRng);
		
		addGSPrint(sb);
		addGSEnder(sb);
		
		try(FileWriter fw=new FileWriter(fname)){ fw.write(sb.toString());
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	public static void dataToGS(float[][] data,float[] Azms,float[] Rads,float delRad,float elv,boolean largeRng,RadarStation rs,RadarColorBar bar,String dname,String fname){
		StringBuilder sb=new StringBuilder();
		
		addGSHeader(sb,rs.getLon(),rs.getLat(),fname,largeRng);
		addGSBGMap(sb);
		
		double cosElv=Math.cos(Math.toRadians(elv));
		double azmWidth=0.5; if(rs.getType()==Type.SC) azmWidth=0.6;
		
		for(int j=0,J=Azms.length;j<J;j++)
		for(int i=0,I=data[j].length;i<I;i++)
		if(isValidData(data[j][i])){
			double Rad=Rads[i]*cosElv;
			
			float[] pos1=SpatialModel.cLatLon(rs.getLon(),rs.getLat(),360.0-Azms[j]-azmWidth,Math.toDegrees((Rad-delRad/2.0)/EARTH_RADIUS));
			float[] pos2=SpatialModel.cLatLon(rs.getLon(),rs.getLat(),360.0-Azms[j]+azmWidth,Math.toDegrees((Rad-delRad/2.0)/EARTH_RADIUS));
			float[] pos3=SpatialModel.cLatLon(rs.getLon(),rs.getLat(),360.0-Azms[j]+azmWidth,Math.toDegrees((Rad+delRad/2.0)/EARTH_RADIUS));
			float[] pos4=SpatialModel.cLatLon(rs.getLon(),rs.getLat(),360.0-Azms[j]-azmWidth,Math.toDegrees((Rad+delRad/2.0)/EARTH_RADIUS));
			
			String pos=String.format(
				"%9.5f %9.5f   %9.5f %9.5f   %9.5f %9.5f   %9.5f %9.5f",
				pos1[0],pos1[1],pos2[0],pos2[1],pos3[0],pos3[1],pos4[0],pos4[1]
			);
			
			if(isValidData(data[j][i])) sb.append("'drawpolyfrgb  "+pos+"  "+bar.getColorString(data[j][i])+"'\n");
		}
		
		sb.append("'draw title "+dname+" at Elv. angle "+String.format("%5.2f",elv)+"'\n\n");
		
		addGSCircles(sb,rs.getLon(),rs.getLat(),largeRng);
		addGSStation(sb,rs.getLon(),rs.getLat());
		addColorbar(sb,bar,rs.getLon(),rs.getLat(),largeRng);
		
		addGSPrint(sb);
		addGSEnder(sb);
		
		try(FileWriter fw=new FileWriter(fname)){ fw.write(sb.toString());
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	public static boolean isValidData(float data){
		return data!=AccessRadarBaseData.VALUE_INVALID&&data!=AccessRadarBaseData.VALUE_RANFOLD;
	}
	
	
	/*** helper methods ***/
	private void refsDataToGS(RadarColorBar bar,String fname,ElevationCut... cuts){
		RadarStation rs=scan.getRadarStation();
		boolean largeRng=true;
		
		StringBuilder sb=new StringBuilder();
		
		addGSHeader(sb,rs.getLon(),rs.getLat(),fname,largeRng);
		
		for(ElevationCut cut:cuts){
			addGSBGMap(sb);
			
			double elv=cut.getElAngle();
			double cosElv=Math.cos(Math.toRadians(elv));
			double azmWidth=0.5; if(rs.getType()==Type.SC) azmWidth=0.6;
			double radWidth=cut.getRefBinWidth()/2.0;
			
			float[][] data=cut.getRefData();
			float[]   Azms=cut.getAzms();
			float[]   Rads=cut.getRefRads();
			
			for(int j=0,J=Azms.length;j<J;j++)
			for(int i=0,I=data[j].length;i<I;i++)
			if(isValidData(data[j][i])){
				double Rad=Rads[i]*cosElv;
				
				float[] pos1=SpatialModel.cLatLon(rs.getLon(),rs.getLat(),360.0-Azms[j]-azmWidth,Math.toDegrees((Rad-radWidth)/EARTH_RADIUS));
				float[] pos2=SpatialModel.cLatLon(rs.getLon(),rs.getLat(),360.0-Azms[j]+azmWidth,Math.toDegrees((Rad-radWidth)/EARTH_RADIUS));
				float[] pos3=SpatialModel.cLatLon(rs.getLon(),rs.getLat(),360.0-Azms[j]+azmWidth,Math.toDegrees((Rad+radWidth)/EARTH_RADIUS));
				float[] pos4=SpatialModel.cLatLon(rs.getLon(),rs.getLat(),360.0-Azms[j]-azmWidth,Math.toDegrees((Rad+radWidth)/EARTH_RADIUS));
				
				String pos=String.format(
					"%9.5f %9.5f   %9.5f %9.5f   %9.5f %9.5f   %9.5f %9.5f",
					pos1[0],pos1[1],pos2[0],pos2[1],pos3[0],pos3[1],pos4[0],pos4[1]
				);
				
				if(isValidData(data[j][i])) sb.append("'drawpolyfrgb  "+pos+"  "+bar.getColorString(data[j][i])+"'\n");
			}
			
			sb.append("'draw title reflectivity at Elv. angle "+String.format("%5.2f",elv)+"'\n\n");
			
			addGSCircles(sb,rs.getLon(),rs.getLat(),largeRng);
			addGSStation(sb,rs.getLon(),rs.getLat());
			addColorbar(sb,bar,rs.getLon(),rs.getLat(),largeRng);
			
			addGSPrint(sb);
		}
		
		addGSEnder(sb);
		
		try(FileWriter fw=new FileWriter(fname)){ fw.write(sb.toString());
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	private void velsDataToGS(RadarColorBar bar,String fname,ElevationCut... cuts){
		RadarStation rs=scan.getRadarStation();
		boolean largeRng=rs.getType()!=Type.SA;
		
		StringBuilder sb=new StringBuilder();
		
		addGSHeader(sb,rs.getLon(),rs.getLat(),fname,largeRng);
		
		for(ElevationCut cut:cuts){
			addGSBGMap(sb);
			
			double elv=cut.getElAngle();
			double cosElv=Math.cos(Math.toRadians(elv));
			double azmWidth=0.5; if(rs.getType()==Type.SC) azmWidth=0.6;
			double radWidth=cut.getVelBinWidth()/2.0;
			
			float[][] data=cut.getVelData();
			float[]   Azms=cut.getAzms();
			float[]   Rads=cut.getVelRads();
			
			for(int j=0,J=Azms.length;j<J;j++)
			for(int i=0,I=data[j].length;i<I;i++)
			if(isValidData(data[j][i])){
				double Rad=Rads[i]*cosElv;
				
				float[] pos1=SpatialModel.cLatLon(rs.getLon(),rs.getLat(),360.0-Azms[j]-azmWidth,Math.toDegrees((Rad-radWidth)/EARTH_RADIUS));
				float[] pos2=SpatialModel.cLatLon(rs.getLon(),rs.getLat(),360.0-Azms[j]+azmWidth,Math.toDegrees((Rad-radWidth)/EARTH_RADIUS));
				float[] pos3=SpatialModel.cLatLon(rs.getLon(),rs.getLat(),360.0-Azms[j]+azmWidth,Math.toDegrees((Rad+radWidth)/EARTH_RADIUS));
				float[] pos4=SpatialModel.cLatLon(rs.getLon(),rs.getLat(),360.0-Azms[j]-azmWidth,Math.toDegrees((Rad+radWidth)/EARTH_RADIUS));
				
				String pos=String.format(
					"%9.5f %9.5f   %9.5f %9.5f   %9.5f %9.5f   %9.5f %9.5f",
					pos1[0],pos1[1],pos2[0],pos2[1],pos3[0],pos3[1],pos4[0],pos4[1]
				);
				
				if(isValidData(data[j][i])) sb.append("'drawpolyfrgb  "+pos+"  "+bar.getColorString(data[j][i])+"'\n");
			}
			
			sb.append("'draw title radial velocity at Elv. angle "+String.format("%5.2f",elv)+"'\n\n");
			
			addGSCircles(sb,rs.getLon(),rs.getLat(),largeRng);
			addGSStation(sb,rs.getLon(),rs.getLat());
			addColorbar(sb,bar,rs.getLon(),rs.getLat(),largeRng);
			
			addGSPrint(sb);
		}
		
		addGSEnder(sb);
		
		try(FileWriter fw=new FileWriter(fname)){ fw.write(sb.toString());
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	private void spwsDataToGS(RadarColorBar bar,String fname,ElevationCut... cuts){
		RadarStation rs=scan.getRadarStation();
		boolean largeRng=rs.getType()!=Type.SA;
		
		StringBuilder sb=new StringBuilder();
		
		addGSHeader(sb,rs.getLon(),rs.getLat(),fname,largeRng);
		
		for(ElevationCut cut:cuts){
			addGSBGMap(sb);
			
			double elv=cut.getElAngle();
			double cosElv=Math.cos(Math.toRadians(elv));
			double azmWidth=0.5; if(rs.getType()==Type.SC) azmWidth=0.6;
			double radWidth=cut.getVelBinWidth()/2.0;
			
			float[][] data=cut.getSpwData();
			float[]   Azms=cut.getAzms();
			float[]   Rads=cut.getVelRads();
			
			for(int j=0,J=Azms.length;j<J;j++)
			for(int i=0,I=data[j].length;i<I;i++)
			if(isValidData(data[j][i])){
				double Rad=Rads[i]*cosElv;
				
				float[] pos1=SpatialModel.cLatLon(rs.getLon(),rs.getLat(),360.0-Azms[j]-azmWidth,Math.toDegrees((Rad-radWidth)/EARTH_RADIUS));
				float[] pos2=SpatialModel.cLatLon(rs.getLon(),rs.getLat(),360.0-Azms[j]+azmWidth,Math.toDegrees((Rad-radWidth)/EARTH_RADIUS));
				float[] pos3=SpatialModel.cLatLon(rs.getLon(),rs.getLat(),360.0-Azms[j]+azmWidth,Math.toDegrees((Rad+radWidth)/EARTH_RADIUS));
				float[] pos4=SpatialModel.cLatLon(rs.getLon(),rs.getLat(),360.0-Azms[j]-azmWidth,Math.toDegrees((Rad+radWidth)/EARTH_RADIUS));
				
				String pos=String.format(
					"%9.5f %9.5f   %9.5f %9.5f   %9.5f %9.5f   %9.5f %9.5f",
					pos1[0],pos1[1],pos2[0],pos2[1],pos3[0],pos3[1],pos4[0],pos4[1]
				);
				
				if(isValidData(data[j][i])) sb.append("'drawpolyfrgb  "+pos+"  "+bar.getColorString(data[j][i])+"'\n");
			}
			
			sb.append("'draw title spectrum width at Elv. angle "+String.format("%5.2f",elv)+"'\n\n");
			
			addGSCircles(sb,rs.getLon(),rs.getLat(),largeRng);
			addGSStation(sb,rs.getLon(),rs.getLat());
			addColorbar(sb,bar,rs.getLon(),rs.getLat(),largeRng);
			
			addGSPrint(sb);
		}
		
		addGSEnder(sb);
		
		try(FileWriter fw=new FileWriter(fname)){ fw.write(sb.toString());
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	private static String cCrossLineCoords(float olon,float olat,boolean largeRng){
		int azicnt=12;	// azimuthal count
		int radcnt= 4;	// 50*9 radial count
		
		if(largeRng) radcnt=6;
		
		StringBuilder sb=new StringBuilder();
		
		sb.append("'set line 1 1 4'\n");
		
		for(int i=0;i<azicnt;i++){
			float[] pos=SpatialModel.cLatLon(olon,olat,i*30,Math.toDegrees(50000*radcnt/EARTH_RADIUS));
			sb.append(String.format("'drawline %9.5f %9.5f %9.5f %9.5f'\n",pos[0],pos[1],olon,olat));
			
			for(int j=1;j<=radcnt;j++){
				float[] pos1=SpatialModel.cLatLon(olon,olat,i*30-2,Math.toDegrees((50000*j)/EARTH_RADIUS));
				float[] pos2=SpatialModel.cLatLon(olon,olat,i*30+2,Math.toDegrees((50000*j)/EARTH_RADIUS));
				sb.append(String.format("'drawline %9.5f %9.5f %9.5f %9.5f'\n",pos1[0],pos1[1],pos2[0],pos2[1]));
			}
		}
		
		sb.append("\n");
		
		return sb.toString();
	}
	
	private static void addGSHeader(StringBuilder sb,float olon,float olat,String fname,boolean largeRng){
		sb.append("'open d:/Data/Bathymetry/ETOPO/ETOPO5.ctl'\n");
		sb.append("'enable print "+IOUtil.getCompleteFileNameWithoutExtension(fname)+".gmf'\n\n");
		
		if(largeRng){
			//sb.append("'set lon "+(olon-3.4)+" "+(olon+3.4)+"'\n");
			//sb.append("'set lat "+(olat-3.3)+" "+(olat+3.3)+"'\n");
			sb.append("'set lon "+(olon-2.9)+" "+(olon+2.9)+"'\n");
			sb.append("'set lat "+(olat-2.8)+" "+(olat+2.8)+"'\n");
		}else{
			sb.append("'set lon "+(olon-2.2)+" "+(olon+2.2)+"'\n");
			sb.append("'set lat "+(olat-2.1)+" "+(olat+2.1)+"'\n");
		}
		sb.append("'set grid off'\n");
		sb.append("'set mpdraw off'\n\n");
	}
	
	private static void addGSBGMap(StringBuilder sb){
		sb.append("'setvpage 1 1 1 1'\n");
		sb.append("'setlopts 5 0.16 1 1'\n");
		sb.append("'set cmin 999999'\n");
		sb.append("'d bath'\n\n");
	}
	
	private static void addGSStation(StringBuilder sb,float olon,float olat){
		sb.append("'drawmark 1 "+olon+" "+olat+" 0.2'\n\n");
	}
	
	private static void addGSCircles(StringBuilder sb,float olon,float olat,boolean largeRng){ sb.append(cCrossLineCoords(olon,olat,largeRng));}
	
	private static void addGSPrint(StringBuilder sb){
		sb.append("'set mpdraw on'\n");
		sb.append("'set mpdset hires'\n");
		sb.append("'set map 1 1 9'\n");
		sb.append("'basemap L 0 15 H'\n\n");
		sb.append("'print'\n");
		sb.append("'c'\n\n");
	}
	
	private static void addColorbar(StringBuilder sb,RadarColorBar bar,float olon,float olat,boolean largeRng){
		int cc=bar.getColorCount();
		
		float dx=0.1f,dy=0.175f,Rlat=2.1f,Rlon=2.1f;
		
		if(largeRng){ Rlon=2.9f; Rlat=2.8f; dx=0.13f; dy=0.235f;}
		
		float lon=olon+Rlon+0.2f,lat=olat-Rlat;
		
		float[] values=bar.getValues();
		
		////// for i==0 //////
		sb.append("'drawpolyfrgb "+
			(lon+dx/2f)+" "+lat+" "+(lon+dx/2f)+" "+(lat)+" "+(lon+dx)+" "+(lat+dy)+" "+(lon)+" "+(lat+dy)+" "+
			bar.getColorString(bar.getColors()[0])+"'\n"
		); lat+=dy;
		
		////// for i==1 -- length-2 //////
		for(int i=1,I=cc-1;i<I;i++){
			sb.append("'drawpolyfrgb "+
				lon+" "+lat+" "+(lon+dx)+" "+(lat)+" "+(lon+dx)+" "+(lat+dy)+" "+(lon)+" "+(lat+dy)+" "+
				bar.getColorString(bar.getColors()[i])+"'\n"
			); lat+=dy;
		}
		
		////// for i==length-1 //////
		sb.append("'drawpolyfrgb "+
			lon+" "+lat+" "+(lon+dx)+" "+(lat)+" "+(lon+dx/2f)+" "+(lat+dy)+" "+(lon+dx/2f)+" "+(lat+dy)+" "+
			bar.getColorString(bar.getColors()[cc-1])+"'\n"
		); lat+=dy;
		
		
		////// for i==0 //////
		lat=olat-Rlat;
		sb.append("'drawline "+lon+" "+(lat+dy)+" "+(lon+dx/2f)+" "+lat     +"'\n");
		sb.append("'drawline "+(lon+dx/2f)+" "+lat+" "+(lon+dx)+" "+(lat+dy)+"'\n");
		sb.append("'drawstring "+(lon+dx*1.2)+" "+(lat+dy*0.9)+" "+String.format("%.1f",values[0])+"'\n");
		lat+=dy;
		
		////// for i==1 -- length-2 //////
		for(int i=1,I=cc-1;i<I;i++){
			sb.append("'drawrect "+lon+" "+lat+" "+(lon+dx)+" "+(lat+dy)+"'\n");
			sb.append("'drawstring "+(lon+dx*1.2)+" "+(lat+dy*0.9)+" "+String.format("%.1f",values[i])+"'\n");
			lat+=dy;
		}
		
		////// for i==length-1 //////
		sb.append("'drawline "+lon+" "+lat+" "+(lon+dx/2f)+" "+(lat+dy)+"'\n");
		sb.append("'drawline "+(lon+dx/2f)+" "+(lat+dy)+" "+(lon+dx)+" "+lat+"'\n");
		lat+=dy;
	}
	
	private static void addGSEnder(StringBuilder sb){
		sb.append("'disable print'\n");
		sb.append("'close 1'\n");
		sb.append("'reinit'\n");
	}
	
	
	/*** test **
	public static void main(String[] args){
		String fname="D:/Data/RadarDA/DataAssim/Z_RADR_I_Z9070_20140718000500_O_DOR_SC_CAP.bin";
		
		RadarStation rs=RadarStation.getRadarStation("Z9898");
		
		VCPScan scan=AccessRadarBaseData.parseSC(fname);
		
		System.out.println(scan);
		
		PlotGS plt=new PlotGS(scan);
		
		plt.refDataToGS(0,RadarColorBar.RefBar,"d:/testRef.gs");
		plt.velDataToGS(0,RadarColorBar.VelBar,"d:/testVel.gs");
		plt.spwDataToGS(0,RadarColorBar.SpwBar,"d:/testSpw.gs");
	}*/
}
