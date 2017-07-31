//
package commonapi;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import miniufo.io.TextReader;


public final class RadarStation{
	//
	private float lon=0;
	private float lat=0;
	private float elv=0;
	
	private String ID=null;
	private String province=null;
	private String city=null;
	
	private Type  type = Type.SB;
	private State state=State.Built;
	
	public static final List<RadarStation> ls=access150RadarStationData();
	
	public enum State{Built,Building,Planning}
	public enum Type {SA,SB,SC,CB,CC,CD,_88D,Undefined}
	
	
	/**
	 * constructor
	 * 
	 * @param lon		longitude of the station
	 * @param lat		latitude of the station
	 * @param elv		elevation of the station
	 * @param ID		ID
	 * @param province	province
	 * @param city		city where the station is in
	 * @param type		which type of radar
	 * @param state		see enum State
	 */
	public RadarStation
	(float lon,float lat,float elv,String ID,String province,String city,Type type,State state){
		this.lon=lon;
		this.lat=lat;
		this.elv=elv;
		this.ID=ID;
		this.province=province;
		this.city=city;
		this.type=type;
		this.state=state;
	}
	
	
	/*** getor and setor ***/
	public float getLon(){ return lon;}
	
	public float getLat(){ return lat;}
	
	public float getElv(){ return elv;}
	
	public String getID(){ return ID;}
	
	public String getProvince(){ return province;}
	
	public String getCity(){ return city;}
	
	public Type getType(){ return type;}
	
	public State getState(){ return state;}
	
	
	/**
	 * return all RadarStation data
	 */
	public static List<RadarStation> getRadarStations(){ return ls;}
	
	/**
	 * return one RadarStation given its ID
	 */
	public static RadarStation getRadarStation(String id){
		Optional<RadarStation> re=ls.stream().filter((station)->id.equals(station.getID())).findFirst();
		
		if(re.isPresent()) return re.get();
		else{
			System.out.println("no RadarStation found for "+id);
			return null;
		}
	}
	
	/**
	 * generate GS
	 */
	public static void generateGS(List<RadarStation> ls,RadarStation.State state){
		StringBuilder sb=new StringBuilder();
		
		sb.append("'open d:/Data/ERAInterim/Data.ctl'\n");
		sb.append("'enable print d:/Data/RadarDA/Stations/Station"+state+".gmf'\n\n");
		
		sb.append("'set lon 73 136'\n");
		sb.append("'set lat 16 55'\n");
		sb.append("'set mpdset cnhimap'\n");
		sb.append("'set grid off'\n");
		sb.append("'set grads off'\n\n");
		
		sb.append("'setlopts 7 0.2 10 10'\n");
		sb.append("'set cmin 999999'\n");
		sb.append("'d u'\n\n");
		
		for(RadarStation s:ls) if(s.state==state) sb.append("'drawmark 3 "+s.lon+" "+s.lat+" 0.5'\n");
		
		sb.append("'draw title "+state+"'\n\n");
		
		sb.append("'print'\n");
		sb.append("'c'\n\n");
		
		sb.append("'disable print'\n");
		sb.append("'close 1'\n");
		sb.append("'reinit'\n");
		
		try(FileWriter fw=new FileWriter("d:/Data/RadarDA/Stations/Station"+state+".gs")){
			fw.write(sb.toString()); fw.close();
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
	
	
	/**
	 * used to print out
	 */
	public String toString(){
		return String.format("%6s %6s %6s %6s lon(%6.4f) lat(%6.4f) elv(%3.1f)",ID,province,city,type,lon,lat,elv);
	}
	
	
	/*** helper method ***/
	private static List<RadarStation> access150RadarStationData(){
		String[][] data=TextReader.readColumnsS("d:/Data/RadarDA/Stations/Radar150.txt",true,1,2,3,4,5,6);
		
		List<RadarStation> ls=new ArrayList<>(150);
		
		for(int i=0;i<150;i++){
			String ID  =data[0][i];
			String city=data[1][i];
			
			Type type=Type.valueOf(data[2][i]);
			
			float elv=Float.parseFloat(data[3][i]);
			float lon=Float.parseFloat(data[4][i]);
			float lat=Float.parseFloat(data[5][i]);
			
			ls.add(new RadarStation(lon,lat,elv,ID,null,city,type,State.Built));
		}
		
		return ls;
	}
	
	
	/*** test **
	public static void main(String[] args){
		List<RadarStation> ls=accessRadarStationData();
		
		//ls.stream().filter(s->s.state==Station.State.Built).forEach(s->System.out.println(s));
		
		generateGS(ls,RadarStation.State.Built);
		generateGS(ls,RadarStation.State.Building);
		generateGS(ls,RadarStation.State.Planning);
	}*/
}
