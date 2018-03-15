//
package pack;

import miniufo.descriptor.DataDescriptor;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.Variable;
import miniufo.io.DataIOFactory;
import miniufo.io.DataWrite;


//
public class DomainMask{
	/*** test ***/
	public static void main(String[] args){
		DiagnosisFactory df=DiagnosisFactory.parseFile("D:/Data/RadarDA/DataAssim/Rammasun/ExpHigh/ctrl/mask.ctl");
		DataDescriptor dd=df.getDataDescriptor();
		
		Range r=new Range("",dd);
		
		Variable v=df.getVariables(r,"u")[0];
		
		cMask(v);
		
		DataWrite dw=DataIOFactory.getDataWrite(dd,"D:/Data/RadarDA/DataAssim/Rammasun/ExpHigh/ctrl/test.dat");
		dw.writeData(dd,v); dw.closeFile();
	}
	
	static void cMask(Variable v){
		int x=v.getXCount(),y=v.getYCount();
		
		float[][] vdata=v.getData()[0][0];
		float[][] tmp  =new float[y][x];
		
		for(int j=1;j<y-1;j++)
		for(int i=1;i<x-1;i++)
		tmp[j][i]=(float)Math.hypot(vdata[j][i+1]-vdata[j][i-1],vdata[j+1][i]-vdata[j-1][i]);
		
		for(int j=0;j<y;j++)
		for(int i=0;i<x;i++)
		if(tmp[j][i]>1e5) vdata[j][i]=1;
		else vdata[j][i]=0;
	}
}
