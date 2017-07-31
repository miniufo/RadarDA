//
package commonapi;

import java.util.List;
import commonapi.ElevationCut;
import commonapi.RadarStation;
import commonapi.RadarStation.Type;


//
public final class VCPScan{
	//
	private int validVelCuts=0;
	private int validRefCuts=0;
	
	private RadarStation rs=null;
	
	private Type type=Type.Undefined;
	
	private List<ElevationCut> cuts=null;
	
	
	/**
	 * constructor
	 */
	public VCPScan(List<ElevationCut> cuts,RadarStation rs,Type type){
		if(cuts.size()>20) throw new IllegalArgumentException("more than 20 cuts");
		
		this.rs=rs;
		this.type=type;
		this.cuts=cuts;
		
		for(ElevationCut cut:cuts){
			if(cut.isValidRefCut()) validRefCuts++;
			if(cut.isValidVelCut()) validVelCuts++;
		}
	}
	
	
	/*** getor and setor ***/
	public int getTotalCut(){ return cuts.size();}
	
	public int getValidRefCuts(){ return validRefCuts;}
	
	public int getValidVelCuts(){ return validVelCuts;}
	
	public Type getRadarType(){ return type;}
	
	public RadarStation getRadarStation(){ return rs;}
	
	public ElevationCut getElev(int idx){ return cuts.get(idx);}
	
	public ElevationCut getValidRefElev(int idx){ return cuts.get(getValidRefIndex(idx));}
	
	public ElevationCut getValidVelElev(int idx){ return cuts.get(getValidVelIndex(idx));}
	
	
	/*** helper methods ***/
	private int getValidRefIndex(int level){
		int zidx=0;
		
		for(int k=0,ec=0,K=cuts.size();k<K;k++) if(cuts.get(k).isValidRefCut()){
			ec++; if(ec==level+1){ zidx=k; break;}
		}
		
		return zidx;
	}
	
	private int getValidVelIndex(int level){
		int zidx=0;
		
		for(int k=0,ec=0,K=cuts.size();k<K;k++) if(cuts.get(k).isValidVelCut()){
			ec++; if(ec==level+1){ zidx=k; break;}
		}
		
		return zidx;
	}
	
	
	/**
	 * used to print out
	 */
	public String toString(){
		StringBuilder sb=new StringBuilder();
		
		sb.append(rs.toString()+"\n");
		sb.append("VCP has "+validRefCuts+" validRef and "+validVelCuts+" validVel ("+cuts.size()+" total) Elevation Cuts{\n");
		
		for(int i=0,I=cuts.size();i<I;i++) sb.append("  "+cuts.get(i)+"\n");
		
		sb.append("}\n");
		
		return sb.toString();
	}
}
