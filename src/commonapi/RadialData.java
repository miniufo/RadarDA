//
package commonapi;

import commonapi.AccessRadarBaseData;


//
public final class RadialData{
	//
	public float azm=0;		// azimuth angle
	
	public float[] ref=null;
	public float[] vel=null;
	public float[] spw=null;
	
	
	/**
	 * constructor
	 */
	public RadialData(int len,float azm){
		if(len<1) throw new IllegalArgumentException("invalid length "+len);
		
		if(azm< 0  ) azm+=360;
		if(azm>=360) azm-=360;
		
		this.azm=azm;
		
		ref=new float[len];
		vel=new float[len];
		spw=new float[len];
		
		for(int i=0;i<len;i++){
			ref[i]=AccessRadarBaseData.VALUE_INVALID;
			vel[i]=AccessRadarBaseData.VALUE_INVALID;
			spw[i]=AccessRadarBaseData.VALUE_INVALID;
		}
	}
	
	public RadialData(int lenRef,int lenVel,float azm){
		if(lenRef<0) throw new IllegalArgumentException("invalid length "+lenRef);
		if(lenVel<0) throw new IllegalArgumentException("invalid length "+lenVel);
		
		if(azm< 0  ) azm+=360;
		if(azm>=360) azm-=360;
		
		this.azm=azm;
		
		ref=new float[lenRef];
		vel=new float[lenVel];
		spw=new float[lenVel];
		
		for(int i=0;i<lenRef;i++)
			ref[i]=AccessRadarBaseData.VALUE_INVALID;
		
		for(int i=0;i<lenVel;i++){
			vel[i]=AccessRadarBaseData.VALUE_INVALID;
			spw[i]=AccessRadarBaseData.VALUE_INVALID;
		}
	}
	
	
	/**
	 * used to print out
	 */
	public String toString(){
		return String.format("Azimuth (%7.3f)",azm);
	}
}
