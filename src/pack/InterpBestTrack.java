//
package pack;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import miniufo.io.TextReader;
import miniufo.lagrangian.Record;
import miniufo.lagrangian.Typhoon;


/**
 * parse Radar data into binary float format
 *
 * @version 1.0, 2015.01.19
 * @author  MiniUFO
 * @since   MDK1.0
 */
public final class InterpBestTrack{
	
	/*** test ***/
	public static void main(String[] args){
		Typhoon ty1=getTyphoon("d:/Data/RadarDA/DataAssim/Rammasun/ExpHigh/Rammasun_CMA.txt").interpolateAlongT(5);
		try(FileWriter fw=new FileWriter("d:/Data/RadarDA/DataAssim/Rammasun/ExpHigh/Rammasun_CMAInterp.txt")){ fw.write(ty1.toString());}
		catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		Typhoon ty2=getTyphoon("d:/Data/RadarDA/DataAssim/Rammasun/ExpHigh/Rammasun_JMA.txt").interpolateAlongT(5);
		try(FileWriter fw=new FileWriter("d:/Data/RadarDA/DataAssim/Rammasun/ExpHigh/Rammasun_JMAInterp.txt")){ fw.write(ty2.toString());}
		catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	static Typhoon getTyphoon(String fname){
		String[][] data=TextReader.readColumnsS(fname,true,1,2,3,4,5,6);
		
		List<Record> ls=new ArrayList<>();
		
		for(int i=0,I=data[0].length;i<I;i++){
			long time=Long.parseLong(data[2][i]);
			
			float lon=Float.parseFloat(data[0][i]);
			float lat=Float.parseFloat(data[1][i]);
			float wnd=Float.parseFloat(data[3][i]);
			float slp=Float.parseFloat(data[4][i]);
			
			Record r=new Record(time,lon,lat,4);
			
			r.setData(2,wnd);
			r.setData(3,slp);
			
			ls.add(r);
		}
		
		return new Typhoon("1409","Rammasun",ls);
	}
}
