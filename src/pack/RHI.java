//
package pack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import miniufo.diagnosis.Range;
import miniufo.diagnosis.SpatialModel;
import miniufo.diagnosis.Variable;
import miniufo.io.CsmDataWriteStream;
import miniufo.io.StationRecord;


//
public class RHI{
	/*** test ***/
	public static void main(String[] args){
		double r=Math.toDegrees(420000/SpatialModel.EARTH_RADIUS);
		System.out.println(r);
		float[] re=SpatialModel.cLatLon(110.2469f,19.9978f,360-114,r);
		System.out.println(Arrays.toString(re)); System.exit(0);
		
		try{
			int[] idx=new int[]{0};
			
			PointSample[] recs=parseFile("d:/Data/RadarDA/DataAssim/Rammasun/ExpHigh/201407180001_CR_RHI_114.0.txt")
				//.map(s->toSR(s,idx[0]++))
				.toArray(PointSample[]::new);
			
			System.out.println(idx[0]);
			
			writeAll(recs,"d:/Data/RadarDA/DataAssim/Rammasun/ExpHigh/RHI.dat");
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	static StationRecord toSR(PointSample ps,int idx){
		StationRecord sr=new StationRecord();
		sr.sid=(50000+idx)+"";
		sr.lat=ps.rpos/100f;
		sr.lon=ps.zpos/10f;
		sr.tim=0;
		sr.nlev=0;
		sr.sdata=new float[]{ps.refl};
		sr.flag=1;
		return sr;
	}
	
	static void writeAll(PointSample[] pss,String fname){
		int len=pss.length;
		
		Variable v=new Variable("v",new Range(1,1,1,len));
		v.setUndef(-9999);
		
		float[] zdef=new float[]{1};
		float[] vdata=v.getData()[0][0][0];
		
		float[][][] lons=new float[1][1][len];
		float[][][] lats=new float[1][1][len];
		
		for(int i=0;i<len;i++){
			lons[0][0][i]=pss[i].rpos/100f;
			lats[0][0][i]=pss[i].zpos/10f;
			vdata[i]=pss[i].refl<0?Float.NaN:pss[i].refl;
		}
		
		v.changeNaNToUndef();
		
		CsmDataWriteStream cdws=new CsmDataWriteStream(fname);
		cdws.writeData(lons,lats,zdef,v); cdws.closeFile();
	}
	
	static Stream<PointSample> parseFile(String fname) throws IOException{
		return Files.lines(Paths.get(fname)).map(oneline->{
			String[] tokens=oneline.trim().split("\\s+");
			return new PointSample(
				Float.parseFloat(tokens[0]),
				Float.parseFloat(tokens[1]),
				Float.parseFloat(tokens[2])
			);
		});
	}
	
	static final class PointSample{
		//
		float rpos=0;
		float zpos=0;
		float refl=0;
		
		//
		public PointSample(float rpos,float zpos,float refl){
			this.rpos=rpos;
			this.zpos=zpos;
			this.refl=refl;
		}
	}
}
