//
package pack;

import miniufo.application.basic.DynamicMethodsInCC;
import miniufo.descriptor.CsmDescriptor;
import miniufo.diagnosis.CylindricalSpatialModel;
import miniufo.diagnosis.DiagnosisFactory;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.Variable;
import miniufo.diagnosis.Variable.Dimension;
import miniufo.io.CtlDataWriteStream;


/**
 * parse Radar data into binary float format
 *
 * @version 1.0, 2015.01.19
 * @author  MiniUFO
 * @since   MDK1.0
 */
public final class SteeringFlow{
	//
	private static final String path="d:/Data/RadarDA/DataAssim/Rammasun/CylindAnaly/";
	
	
	/*** test ***/
	public static void main(String[] args){
		//DataInterpolation di=new DataInterpolation(DiagnosisFactory.getDataDescriptor("H:/fnl_Steering.ctl"));
		//di.temporalInterp("H:/fnl_interp.dat",Type.LINEAR,55); System.exit(0);
		
		Variable[] steer1=getSteeringFlowInZ(path+"RammasunCTRL.csm" ,1);
		Variable[] steer2=getSteeringFlowInZ(path+"RammasunRWGLB.csm",2);
		Variable[] steer3=getSteeringFlowInZ(path+"RammasunRWLCL.csm",3);
		Variable[] steer4=getSteeringFlowInP(path+"RammasunFNL.csm");
		
		for(int l=0;l<steer1[0].getTCount();l++){
			System.out.println(String.format(
				"%10.2f %10.2f   %10.2f %10.2f   %10.2f %10.2f   %10.2f %10.2f",
				steer1[0].getData()[0][0][0][l],steer1[1].getData()[0][0][0][l],
				steer2[0].getData()[0][0][0][l],steer2[1].getData()[0][0][0][l],
				steer3[0].getData()[0][0][0][l],steer3[1].getData()[0][0][0][l],
				steer4[0].getData()[0][0][0][l],steer4[1].getData()[0][0][0][l]
			));
		}
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(path+"steering.dat");
		cdws.writeData(steer1[0],steer1[1],steer2[0],steer2[1],steer3[0],steer3[1],steer4[0],steer4[1]);
	}
	
	static Variable[] getSteeringFlowInZ(String file,int tag){
		DiagnosisFactory df=DiagnosisFactory.parseFile(file);
		CsmDescriptor csd=(CsmDescriptor)df.getDataDescriptor();
		
		Range r=new Range("",csd);
		
		Variable[] vs=df.getVariables(r,"u"+tag,"v"+tag);
		
		Variable u=vs[0];
		Variable v=vs[1];
		
		CylindricalSpatialModel csm=new CylindricalSpatialModel(csd);
		
		DynamicMethodsInCC dm=new DynamicMethodsInCC(csm);
		
		Variable um=dm.cRadialAverage(u,0,149).anomalizeX().averageAlong(Dimension.Z,0,12); // 0-10 km mean
		Variable vm=dm.cRadialAverage(v,0,149).anomalizeX().averageAlong(Dimension.Z,0,12); // 0-10 km mean
		
		return new Variable[]{um,vm};
	}
	
	static Variable[] getSteeringFlowInP(String file){
		DiagnosisFactory df=DiagnosisFactory.parseFile(file);
		CsmDescriptor csd=(CsmDescriptor)df.getDataDescriptor();
		
		Range r=new Range("",csd);
		
		Variable[] vs=df.getVariables(r,"u","v");
		
		Variable u=vs[0];
		Variable v=vs[1];
		
		CylindricalSpatialModel csm=new CylindricalSpatialModel(csd);
		
		DynamicMethodsInCC dm=new DynamicMethodsInCC(csm);
		
		Variable um=dm.cRadialAverage(u,0,149).anomalizeX().averageAlong(Dimension.Z,3,16); // 1000-300 hPa mean
		Variable vm=dm.cRadialAverage(v,0,149).anomalizeX().averageAlong(Dimension.Z,3,16); // 1000-300 hPa mean
		
		return new Variable[]{um,vm};
	}
}
