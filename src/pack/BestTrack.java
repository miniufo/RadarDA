//
package pack;

import miniufo.basic.ArrayUtil;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.SpatialModel;
import miniufo.diagnosis.Variable;
import miniufo.io.CtlDataWriteStream;
import miniufo.io.TextReader;


/**
 * parse Radar data into binary float format
 *
 * @version 1.0, 2015.01.19
 * @author  MiniUFO
 * @since   MDK1.0
 */
public final class BestTrack{
	//
	private static final String path="d:/Data/RadarDA/DataAssim/Rammasun/ExpHigh/";
	
	
	/*** test ***/
	public static void main(String[] args){
		Variable[] cma    =getIntensity(path+"Rammasun_CMAInterp.txt",0,49,5,4);
		Variable[] jma    =getIntensity(path+"Rammasun_JMAInterp.txt",0,49,5,4);
		Variable[] ctrl   =getIntensity(path+"Rammasun_ctrl.txt"     ,0,49,5,4);
		Variable[] rwGlb  =getIntensity(path+"Rammasun_rwGlb.txt"    ,6,49,5,4);
		Variable[] rwLcl  =getIntensity(path+"Rammasun_rwLcl.txt"    ,6,49,5,4);
		Variable[] rwLcld2=getIntensity(path+"Rammasun_rwLcld2.txt"  ,6,49,5,4);
		Variable[] rwLclm2=getIntensity(path+"Rammasun_rwLclm2.txt"  ,6,49,5,4);
		
		Variable    ctrlErrJ=getTrackErrors(path+"Rammasun_ctrl.txt"   ,path+"Rammasun_JMAInterp.txt",0);
		Variable   rwGlbErrJ=getTrackErrors(path+"Rammasun_rwGlb.txt"  ,path+"Rammasun_JMAInterp.txt",6);
		Variable   rwLclErrJ=getTrackErrors(path+"Rammasun_rwLcl.txt"  ,path+"Rammasun_JMAInterp.txt",6);
		Variable rwLcld2ErrJ=getTrackErrors(path+"Rammasun_rwLcld2.txt",path+"Rammasun_JMAInterp.txt",6);
		Variable rwLclm2ErrJ=getTrackErrors(path+"Rammasun_rwLclm2.txt",path+"Rammasun_JMAInterp.txt",6);
		
		Variable    ctrlErrC=getTrackErrors(path+"Rammasun_ctrl.txt"   ,path+"Rammasun_CMAInterp.txt",0);
		Variable   rwGlbErrC=getTrackErrors(path+"Rammasun_rwGlb.txt"  ,path+"Rammasun_CMAInterp.txt",6);
		Variable   rwLclErrC=getTrackErrors(path+"Rammasun_rwLcl.txt"  ,path+"Rammasun_CMAInterp.txt",6);
		Variable rwLcld2ErrC=getTrackErrors(path+"Rammasun_rwLcld2.txt",path+"Rammasun_CMAInterp.txt",6);
		Variable rwLclm2ErrC=getTrackErrors(path+"Rammasun_rwLclm2.txt",path+"Rammasun_CMAInterp.txt",6);
		
		    cma[0].setName("slpcma"    );     cma[1].setName("wndcma"    );
		    jma[0].setName("slpjma"    );     jma[1].setName("wndjma"    );
		   ctrl[0].setName("slpctrl"   );    ctrl[1].setName("wndctrl"   );
		  rwGlb[0].setName("slprwGlb"  );   rwGlb[1].setName("wndrwGlb"  );
		  rwLcl[0].setName("slprwLcl"  );   rwLcl[1].setName("wndrwLcl"  );
		rwLcld2[0].setName("slprwLcld2"); rwLcld2[1].setName("wndrwLcld2");
		rwLclm2[0].setName("slprwLclm2"); rwLclm2[1].setName("wndrwLclm2");
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(path+"Intensity.dat");
		cdws.writeData(ArrayUtil.concatAll(Variable.class,
			cma,jma,ctrl,rwGlb,rwLcl,rwLcld2,rwLclm2,
			new Variable[]{ctrlErrJ,rwGlbErrJ,rwLclErrJ,rwLcld2ErrJ,rwLclm2ErrJ},
			new Variable[]{ctrlErrC,rwGlbErrC,rwLclErrC,rwLcld2ErrC,rwLclm2ErrC}
		));
		cdws.closeFile();
	}
	
	static Variable getTrackErrors(String expName,String stdName,int strIdx){
		String[][] dataExp=TextReader.readColumnsS(expName,true,1,2,3);
		String[][] dataStd=TextReader.readColumnsS(stdName,true,1,2,3);
		
		int lines=dataExp[0].length;
		
		Variable diff=new Variable("diff",false,new Range(dataStd[0].length,1,1,1));
		
		diff.setValue(-9999f);
		diff.setUndef(-9999f);
		
		float[] data=diff.getData()[0][0][0];
		
		for(int i=0;i<lines;i++){
			float lon1=Float.parseFloat(dataExp[0][i]);
			float lat1=Float.parseFloat(dataExp[1][i]);
			float lon2=Float.parseFloat(dataStd[0][strIdx+i]);
			float lat2=Float.parseFloat(dataStd[1][strIdx+i]);
			
			long tim1=Long.parseLong(dataExp[2][i]);
			long tim2=Long.parseLong(dataStd[2][i+strIdx]);
			
			if(tim1!=tim2) throw new IllegalArgumentException(tim1+" != "+tim2);
			
			data[i+strIdx]=SpatialModel.cSphericalDistanceByDegree(lon1,lat1,lon2,lat2);
		}
		
		return diff;
	}
	
	static Variable[] getIntensity(String fname,int strIdx,int maxT,int slpCol,int wndCol){
		float[][] data=TextReader.readColumnsF(fname,true,slpCol,wndCol);
		
		int lines=data[0].length;
		
		Variable slp=new Variable("slp",false,new Range(maxT,1,1,1));
		Variable wnd=new Variable("wnd",false,new Range(maxT,1,1,1));
		
		slp.setValue(-9999f); slp.setUndef(-9999f);
		wnd.setValue(-9999f); wnd.setUndef(-9999f);
		
		System.arraycopy(data[0],0,slp.getData()[0][0][0],strIdx,lines);
		System.arraycopy(data[1],0,wnd.getData()[0][0][0],strIdx,lines);
		
		return new Variable[]{slp,wnd};
	}
}
