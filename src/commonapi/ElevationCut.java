//
package commonapi;

import miniufo.diagnosis.MDate;


//
public final class ElevationCut{
	//
	private CutHeader head=null;
	private CutData   data=null;
	
	
	/**
	 * constructor
	 */
 	public ElevationCut(CutHeader head,CutData data){
 		this.head=head;
		this.data=data;
		
		if(data.rads.size()>380)
		throw new IllegalArgumentException("more than 380 RadialData "+data.rads.size());
	}
	
	
	/*** getor and setor ***/
	public boolean isValidRefCut(){ return head.validRefCut;}
	
	public boolean isValidVelCut(){ return head.validVelCut;}
	
	public int getRefBinNumber(){ return head.refBinNo;}
	
	public int getVelBinNumber(){ return head.velBinNo;}
	
	public int getRefRadialNumber(){ return head.refRadNo;}
	
	public int getVelRadialNumber(){ return head.velRadNo;}
	
	public float getRefBinWidth(){ return head.refBinWidth;}
	
	public float getVelBinWidth(){ return head.velBinWidth;}
	
	public float getNyquistVelocity(){ return head.Nyquist;}
	
	public float getElAngle(){ return head.ElAngle;}
	
	public float[] getAzms(){
		int min=Math.min(head.refRadNo,head.velRadNo);
		int max=Math.max(head.refRadNo,head.velRadNo);
		
		if(max!=min&&min!=0) throw new IllegalArgumentException(
			"invalid Ref ("+head.refRadNo+") and Vel ("+head.velRadNo+") radial scans"
		);
		
		float[] azms=new float[max];
		
		for(int i=0;i<max;i++) azms[i]=data.rads.get(i).azm;
		
		return azms;
	}
	
	public float[] getRefRads(){ return head.radiusRef;}
	
	public float[] getVelRads(){ return head.radiusVel;}
	
	public float[][] getRefData(){
		int J=head.refRadNo,I=head.refBinNo;
		
		float[][] ref=new float[J][I];
		
		for(int j=0;j<J;j++)
		for(int i=0;i<I;i++) ref[j][i]=data.rads.get(j).ref[i];
		
		return ref;
	}
	
	public float[][] getVelData(){
		int J=head.velRadNo,I=head.velBinNo;
		
		float[][] vel=new float[J][I];
		
		for(int j=0;j<J;j++)
		for(int i=0;i<I;i++) vel[j][i]=data.rads.get(j).vel[i];
		
		return vel;
	}
	
	public float[][] getSpwData(){
		int J=head.velRadNo,I=head.velBinNo;
		
		float[][] spw=new float[J][I];
		
		for(int j=0;j<J;j++)
		for(int i=0;i<I;i++) spw[j][i]=data.rads.get(j).spw[i];
		
		return spw;
	}
	
	public MDate getObsTime(){ return head.obsTime;}
	
	public RadialData getRadialData(int idx){ return data.rads.get(idx);}
	
	public void setDataValue(int azmIdx,int binIdx,float value){ data.rads.get(azmIdx).vel[binIdx]=value;}
	
	
	/**
	 * used to print out
	 */
	public String toString(){ return head.toString();}
}
