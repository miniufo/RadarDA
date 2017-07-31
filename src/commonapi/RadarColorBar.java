/**
 * @(#)RadarColorBar.java	1.0 2015.10.16
 *
 * Copyright 2007 MiniUFO, All rights reserved.
 * MiniUFO Studio. Use is subject to license terms.
 */
package commonapi;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;


/**
 * parse Radar data into binary float format
 *
 * @version 1.0, 2015.01.19
 * @author  MiniUFO
 * @since   MDK1.0
 */
public final class RadarColorBar{
	//
	private int   cc = 0;	// color count
	private float inc=-1;	// increment
	
	private float[] values=null;
	
	private Color[] colors=null;
	
	private static final Color[] RefColors=getRefColors();
	private static final Color[] VelColors=getVelColors();
	private static final Color[] SpwColors=getSpwColors();
	
	
	public static final RadarColorBar RefBar=new RadarColorBar(5,75,5,RefColors);
	
	public static final RadarColorBar VelBar=new RadarColorBar(new float[]{
		-26,-19,-14,-11,-5,-1,0,5,11,14,19,26,33
	},VelColors);
	
	public static final RadarColorBar SpwBar=new RadarColorBar(new float[]{
		2,4,5,7
	},SpwColors);
	
	
	/**
	 * constructors
	 */
	public RadarColorBar(float min,float max,float inc){
		this(min,max,inc,getRainbowBar(Math.round((max-min)/inc)+2));
	}
	
	public RadarColorBar(float min,float max,float inc,Color[] colors){
		if(inc<=0) throw new IllegalArgumentException("invalid increment :"+inc);
		
		this.inc=inc;
		this.cc=Math.round((max-min)/inc)+2;
		this.colors=colors;
		
		if(cc!=colors.length) throw new IllegalArgumentException("color count != value count + 1");
		
		values=new float[cc-1];
		
		for(int i=0,I=values.length;i<I;i++) values[i]=min+inc*i;
	}
	
	public RadarColorBar(float[] values){
		this.values=values;
		
		cc=values.length+1;
		
		colors=RadarColorBar.getRainbowBar(cc);
	}
	
	public RadarColorBar(float[] values,Color[] colors){
		this.values=values;
		this.colors=colors;
		
		cc=colors.length;
	}
	
	
	/*** getor and setor ***/
	public int getColorCount(){ return cc;}
	
	public float getMin(){ return values[0];}
	
	public float getMax(){ return values[values.length-1];}
	
	public float getIncement(){ return inc;}
	
	public float[] getValues(){ return values;}
	
	public Color[] getColors(){ return colors;}
	
	
	/**
	 * map value to color string
	 */
	public String getColorString(float val){
		if(cc<3) throw new IllegalArgumentException("colorbar should have at least 3 colors");
		
		if(val<values[0]) return toRGBString(colors[0]);
		
		for(int i=0,I=cc-2;i<I;i++) if(val>=values[i]&&val<values[i+1]) return toRGBString(colors[i+1]);
		
		return toRGBString(colors[cc-1]);
	}
	
	public String getColorString(Color c){ return toRGBString(c);}
	
	
	/*** static methods ***/
	public static String getRefColorString(float val){
		if(val>= 0&&val< 5) return toRGBString(RefColors[ 0]);
		if(val>= 5&&val<10) return toRGBString(RefColors[ 1]);
		if(val>=10&&val<15) return toRGBString(RefColors[ 2]);
		if(val>=15&&val<20) return toRGBString(RefColors[ 3]);
		if(val>=20&&val<25) return toRGBString(RefColors[ 4]);
		if(val>=25&&val<30) return toRGBString(RefColors[ 5]);
		if(val>=30&&val<35) return toRGBString(RefColors[ 6]);
		if(val>=35&&val<40) return toRGBString(RefColors[ 7]);
		if(val>=40&&val<45) return toRGBString(RefColors[ 8]);
		if(val>=45&&val<50) return toRGBString(RefColors[ 9]);
		if(val>=50&&val<55) return toRGBString(RefColors[10]);
		if(val>=55&&val<60) return toRGBString(RefColors[11]);
		if(val>=60&&val<65) return toRGBString(RefColors[12]);
		if(val>=65&&val<70) return toRGBString(RefColors[13]);
		if(val>=70&&val<75) return toRGBString(RefColors[14]);
		if(val>=75        ) return toRGBString(RefColors[15]);
		
		throw new IllegalArgumentException("reflectivity data: "+val+" of out color range (>=0)");
	}
	
	public static String getVelColorString(float val){
		if(val< -26         ) return toRGBString(VelColors[ 0]);
		if(val>=-26&&val<-19) return toRGBString(VelColors[ 1]);
		if(val>=-19&&val<-14) return toRGBString(VelColors[ 2]);
		if(val>=-14&&val<-11) return toRGBString(VelColors[ 3]);
		if(val>=-11&&val<-5 ) return toRGBString(VelColors[ 4]);
		if(val>=-5 &&val<-1 ) return toRGBString(VelColors[ 5]);
		if(val>=-1 &&val< 0 ) return toRGBString(VelColors[ 6]);
		if(val>= 0 &&val< 5 ) return toRGBString(VelColors[ 7]);
		if(val>= 5 &&val< 11) return toRGBString(VelColors[ 8]);
		if(val>= 11&&val< 14) return toRGBString(VelColors[ 9]);
		if(val>= 14&&val< 19) return toRGBString(VelColors[10]);
		if(val>= 19&&val< 26) return toRGBString(VelColors[11]);
		if(val>= 26&&val< 33) return toRGBString(VelColors[12]);
		if(val>= 33         ) return toRGBString(VelColors[13]);
		
		throw new IllegalArgumentException("velocity data: "+val+" of out color range [-33 33]");
	}
	
	public static String getSpwColorString(float val){
		if(val>=0&&val<2) return toRGBString(SpwColors[0]);
		if(val>=2&&val<4) return toRGBString(SpwColors[1]);
		if(val>=4&&val<5) return toRGBString(SpwColors[2]);
		if(val>=5&&val<7) return toRGBString(SpwColors[3]);
		if(val>=7       ) return toRGBString(SpwColors[4]);
		
		throw new IllegalArgumentException("spectrum width data: "+val+" of out color range(>0)");
	}
	
	public static Color[] linearInterp(int step,Color str,Color end){
		int rs=str.getRed(),gs=str.getGreen(),bs=str.getBlue();
		int re=end.getRed(),ge=end.getGreen(),be=end.getBlue();
		
		Color[] grad=new Color[step];
		
		for(int i=0;i<step;i++){
			float ratio=(float)i/step;
			
			int r=rs+Math.round((re-rs)*ratio);
			int g=gs+Math.round((ge-gs)*ratio);
			int b=bs+Math.round((be-bs)*ratio);
			
			grad[i]=new Color(r,g,b);
		}
		
		return grad;
	}
	
	public static Color[] linearInterp(int step,Color... cs){
		int N=cs.length-1;
		
		if(step<N) throw new IllegalArgumentException("step < # of the given colors");
		
		int intp=step/N,remn=step%N;
		
		Color[] grad=new Color[step];
		
		if(remn==0){
			for(int i=0;i<N;i++){
				Color[] re=linearInterp(intp,cs[i],cs[i+1]);
				System.arraycopy(re,0,grad,i*intp,re.length);
			}
			
		}else{
			for(int i=0,I=N-1;i<I;i++){
				Color[] re=linearInterp(intp,cs[i],cs[i+1]);
				System.arraycopy(re,0,grad,i*intp,re.length);
			}
			
			Color[] re=linearInterp(intp+(step%N),cs[N-1],cs[N]);
			System.arraycopy(re,0,grad,(N-1)*intp,re.length);
		}
		
		return grad;
	}
	
	public static Color[] getRainbowBar(int clrCount){
		Color[] res=new Color[clrCount];
		
		for(int i=0;i<clrCount;i++) res[i]=new Color(Color.HSBtoRGB(0.765f*(1f-(float)i/(clrCount-2.3f)),1f,1f));
		
		return res;
	}
	
	public static Color[] loadColorbar(String fname){
		Color[] re=null;
		
		try(BufferedReader br=new BufferedReader(new FileReader(fname))){
			String oneline=br.readLine();
			
			int lines=Integer.parseInt(oneline);
			
			re=new Color[lines];
			
			for(int i=0;i<lines;i++){
				String[] tokens=br.readLine().split(" ");
				
				int r=Integer.parseInt(tokens[1].split("=")[1]);
				int g=Integer.parseInt(tokens[2].split("=")[1]);
				int b=Integer.parseInt(tokens[3].split("=")[1]);
				
				re[i]=new Color(r,g,b);
			}
			
		}catch(IOException e){ e.printStackTrace(); System.exit(0);}
		
		return re;
	}
	
	
	/*** helper methods ***/
	private static String toRGBString(Color c){
		return String.format("%3d %3d %3d",c.getRed(),c.getGreen(),c.getBlue());
	}
	
	private static Color[] getRefColors(){
		Color[] bar=new Color[16];
		
		bar[ 0]=new Color( 99, 99, 99);
		bar[ 1]=new Color(  0,236,236);
		bar[ 2]=new Color(  0,160,246);
		bar[ 3]=new Color(  0,  0,246);
		bar[ 4]=new Color(  0,255,  0);
		bar[ 5]=new Color(  0,200,  0);
		bar[ 6]=new Color(  0,144,  0);
		bar[ 7]=new Color(255,255,  0);
		bar[ 8]=new Color(231,192,  0);
		bar[ 9]=new Color(255,144,  0);
		bar[10]=new Color(255,  0,  0);
		bar[11]=new Color(214,  0,  0);
		bar[12]=new Color(192,  0,  0);
		bar[13]=new Color(255,  0,255);
		bar[14]=new Color(153, 85,201);
		bar[15]=new Color(205,205,205);
		
		return bar;
	}
	
	private static Color[] getVelColors(){
		Color[] bar=new Color[14];
		
		bar[ 0]=new Color(  0,255,  0);
		bar[ 1]=new Color(  0,232,  0);
		bar[ 2]=new Color(  0,201,  0);
		bar[ 3]=new Color(  0,177,  0);
		bar[ 4]=new Color(  0,144,  0);
		bar[ 5]=new Color(  0,113,  0);
		bar[ 6]=new Color(120,150,120);
		bar[ 7]=new Color(150,120,120);
		bar[ 8]=new Color(128,  0,  0);
		bar[ 9]=new Color(160,  0,  0);
		bar[10]=new Color(184,  0,  0);
		bar[11]=new Color(220,  0,  0);
		bar[12]=new Color(236,  0,  0);
		bar[13]=new Color(255,  0,  0);
		
		return bar;
	}
	
	private static Color[] getSpwColors(){
		Color[] bar=new Color[5];
		
		bar[0]=new Color(156,156,156);
		bar[1]=new Color(118,118,118);
		bar[2]=new Color(249,171,171);
		bar[3]=new Color(239,139,139);
		bar[4]=new Color(202,112,112);
		
		return bar;
	}
	
	
	/*** test ***/
	public static void main(String[] args){
		System.out.println(Arrays.toString(getRainbowBar(12)));
	}
}
