//
package pack;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import miniufo.database.AccessBestTrack;
import miniufo.database.AccessBestTrack.DataSets;
import miniufo.diagnosis.MDate;
import miniufo.diagnosis.Range;
import miniufo.diagnosis.Variable;
import miniufo.io.CtlDataWriteStream;
import miniufo.lagrangian.Typhoon;


public class BST2Binary{
	//
	static final Predicate<Typhoon> cond=ty->{
		int year=new MDate(ty.getTime(0)).getYear();
		return year==2014&&ty.getName().equalsIgnoreCase("Rammasun");
	};
	
	static final DataSets ds=DataSets.JMA;
	static final String path="D:/Data/RadarDA/DataAssim/Rammasun/";
	
	static final List<Typhoon> all=AccessBestTrack.getTyphoons("d:/Data/Typhoons/"+ds+"/"+ds+".txt","",ds);
	
	
	/*** test ***/
	public static void main(String[] args){
		Typhoon ty=all.stream().filter(cond).findFirst().get();
		
		int tcount=ty.getTCount();
		
		Variable slp=new Variable("slp",false,new Range(tcount,1,1,1));
		Variable wnd=new Variable("wnd",false,new Range(tcount,1,1,1));
		
		slp.setValue(-9999); wnd.setUndef(-9999);
		wnd.setValue(-9999); wnd.setUndef(-9999);
		
		float[] pdata=slp.getData()[0][0][0];
		float[] wdata=wnd.getData()[0][0][0];
		
		float[] wind=ty.getWinds();
		float[] pres=ty.getPressures();
		
		if(pres!=null){ System.out.println(" has PRS data"); for(int l=0;l<tcount;l++) pdata[l]=pres[l];}
		if(wind!=null){ System.out.println(" has WND data"); for(int l=0;l<tcount;l++) wdata[l]=wind[l];}
		
		CtlDataWriteStream cdws=new CtlDataWriteStream(path+ty.getName()+"_"+ds+".dat");
		cdws.writeData(slp,wnd); cdws.closeFile();
		
		
		////// write ctl //////
		StringBuilder sb=new StringBuilder();
		
		sb.append("dset ^"+ty.getName()+"_"+ds+".dat\n");
		sb.append("title "+ds+" best track\n");
		sb.append("undef -9999\n");
		sb.append("xdef  1 linear 0 1\n");
		sb.append("ydef  1 linear 0 1\n");
		sb.append("zdef  1 linear 0 1\n");
		sb.append("tdef "+tcount+" linear "+new MDate(ty.getTime(0)).toGradsDate()+" 6hr\n");
		sb.append("vars 2\n");
		sb.append("prs 0 99 SLP\n");
		sb.append("wnd 0 99 WND\n");
		sb.append("endvars\n");
		
		try(FileWriter fw=new FileWriter(path+ty.getName()+"_"+ds+".ctl")){ fw.write(sb.toString());}
		catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		try(FileWriter fw=new FileWriter(path+ty.getName()+"_"+ds+".txt")){ fw.write(ty.toString());}
		catch(IOException e){ e.printStackTrace(); System.exit(0);}
	}
}
