//
package pack;

import miniufo.application.advanced.CoordinateTransformation;
import miniufo.application.diagnosticModel.EliassenModelInCC;
import miniufo.descriptor.CsmDescriptor;
import miniufo.diagnosis.CylindricalSpatialModel;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.SphericalSpatialModel;
import miniufo.diagnosis.Variable;
import miniufo.io.DataIOFactory;
import miniufo.io.DataWrite;


/**
 * parse Radar data into binary float format
 *
 * @version 1.0, 2015.01.19
 * @author  MiniUFO
 * @since   MDK1.0
 */
public final class CylindAnaly{
	//
	private static final String path="d:/Data/RadarDA/DataAssim/Rammasun/CylindAnaly/";
	
	
	/*** test ***/
	public static void main(String[] args){
		DiagnosisFactory df=DiagnosisFactory.parseFile(path+"RammasunFNL.csm");
		CsmDescriptor csd=(CsmDescriptor)df.getDataDescriptor();
		
		Range r=new Range("",csd);
		
		Variable[] vs=df.getVariables(r,"u","v","w","T");
		
		Variable u=vs[0];
		Variable v=vs[1];
		Variable w=vs[2];
		Variable T=vs[3];
		
		/***************** building models ******************/
		SphericalSpatialModel   ssm=new SphericalSpatialModel(csd.getCtlDescriptor());
		CylindricalSpatialModel csm=new CylindricalSpatialModel(csd);
		
		EliassenModelInCC emdl=new EliassenModelInCC(csm);
		CoordinateTransformation ct=new CoordinateTransformation(ssm,csm);
		
		Variable[] vel=ct.reprojectToCylindrical(u,v);
		u=vel[0];
		v=vel[1];
		
		emdl.cStormRelativeAziRadVelocity(u,v);
		
		
		/************** variable calculation ****************/
		Variable um=u.anomalizeX();
		Variable vm=v.anomalizeX();
		Variable wm=w.anomalizeX();
		Variable Tm=T.anomalizeX();
		Variable Ta=Tm.copy(); Ta.setName("Ta");
		Variable uv=u.multiply(v).anomalizeX(); uv.setName("uv");
		
		float[][][][] Tdata=Ta.getData();
		
		for(int l=0;l<Tm.getTCount();l++)
		for(int k=0;k<Tm.getZCount();k++)
		for(int j=0;j<Tm.getYCount();j++) Tdata[k][j][0][l]-=Tdata[k][Tm.getYCount()-1][0][l];
		
		DataWrite dw=DataIOFactory.getDataWrite(csd.getCtlDescriptor(),path+"fnl.dat");
		dw.writeData(csd.getCtlDescriptor(),um,vm,wm,Tm,Ta,uv); dw.closeFile();
	}
}
