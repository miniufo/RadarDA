//
package SASB;


//
public final class RadialScan{
	//
	public int[] temp1                   =null;	// preserved
	public int   RadarStatus             =0;	// 1 - for radar data
	public int[] temp2                   =null;	// preserved
	public int   mSeconds                =0;	// the time of collecting radial data
	public int   JulianDate              =0;	// start from 1 Jan. 1970
	public int   URange                  =0;	// unambiguous range (unit: 100 m)
	public int   Az                      =0;	// azimuthal angle
	public int   RadialNumber            =0;	// radial number
	public int   RadialStatus            =0;	// radial status
	public int   El                      =0;	// elevation angle
	public int   ElNumber                =0;	// elevation number
	public int   RangeToFirstGateOfRef   =0;	// ��һ�����������ݱ�ʾ��ʵ�ʾ���(m)
	public int   RangeToFirstGateOfDop   =0;	// ��һ�����������ݱ�ʾ��ʵ�ʾ���(m)
	public int   GateSizeOfReflectivity  =0;	// ���������ݵľ���ⳤ(m)
	public int   GateSizeOfDoppler       =0;	// ���������ݵľ���ⳤ(m)
	public int   GateNumberOfReflectivity=0;	// ���������ݵľ���ⳤ(m)
	public int   GateNumberOfDoppler     =0;	// ���������ݵľ���ⳤ(m)
	public int   CutSectorNumber         =0;	// sector number
	public float CalibrationConst        =0;	// �궨����
	public int   PtrOfReflectivity       =0;	// pointer of reflectivity
	public int   PtrOfVelocity           =0;	// pointer of velocity
	public int   PtrOfSpectrumWidth      =0;	// pointer of spectrum width
	public int   ResolutionOfVelocity    =0;	// �������ٶȷֱ���
	public int   VcpNumber               =0;	// ��ɨ��
	public int[] temp4                   =null;	// preserved
	public int   PtrOfArcReflectivity    =0;	// pointer of reflectivity data
	public int   PtrOfArcVelocity        =0;	// pointer of velocity data
	public int   PtrOfArcWidth           =0;	// pointer of spectrum width data
	public int   Nyquist                 =0;	// ��ģ���ٶ� (cm/s)
	public int   temp46                  =0;	// preserved
	public int   temp47                  =0;	// preserved
	public int   temp48                  =0;	// preserved
	public int   CircleTotal             =0;	// ������
	
	public byte[] temp5                  =null;	// preserved
	public byte[] Echodata               =null;	// 129- 588��  460�ֽڷ�������
												// 129-1508��1380�ֽ��ٶ�����
												// 129-2428��2300�ֽ��׿�����
	public byte[] temp                   =null;	// preserved
	
	
	public String toString(){
		StringBuilder sb=new StringBuilder();
		
		sb.append(String.format("RadarStatus:              %6d\n",RadarStatus));
		sb.append(String.format("mSeconds:                 %6d\n",mSeconds));
		sb.append(String.format("JulianDate:               %6d\n",JulianDate));
		sb.append(String.format("URange:                   %10.4f\n",URange/10f));
		sb.append(String.format("Az(deg):                  %10.4f\n",Az/8f*(180f/4096f)));
		sb.append(String.format("RadialNumber:             %6d\n",RadialNumber));
		sb.append(String.format("RadialStatus:             %6d\n",RadialStatus));
		sb.append(String.format("El(deg):                  %10.4f\n",El/8f*(180f/4096f)));
		sb.append(String.format("ElNumber:                 %6d\n",ElNumber));
		sb.append(String.format("RangeToFirstGateOfRef(m): %6d\n",RangeToFirstGateOfRef));
		sb.append(String.format("RangeToFirstGateOfDop(m): %6d\n",RangeToFirstGateOfDop));
		sb.append(String.format("GateSizeOfReflectivity(m):%6d\n",GateSizeOfReflectivity));
		sb.append(String.format("GateSizeOfDoppler(m):     %6d\n",GateSizeOfDoppler));
		sb.append(String.format("GateNumberOfReflectivity: %6d\n",GateNumberOfReflectivity));
		sb.append(String.format("GateNumberOfDoppler:      %6d\n",GateNumberOfDoppler));
		sb.append(String.format("CutSectorNumber:          %6d\n",CutSectorNumber));
		sb.append(String.format("CalibrationConst:         %8.4f\n",CalibrationConst));
		sb.append(String.format("PtrOfReflectivity:        %6d\n",PtrOfReflectivity));
		sb.append(String.format("PtrOfVelocity:            %6d\n",PtrOfVelocity));
		sb.append(String.format("PtrOfSpectrumWidth:       %6d\n",PtrOfSpectrumWidth));
		sb.append(String.format("ResolutionOfVelocity:     %6d\n",ResolutionOfVelocity));
		sb.append(String.format("VcpNumber:                %6d\n",VcpNumber));
		sb.append(String.format("PtrOfArcReflectivity:     %6d\n",PtrOfArcReflectivity));
		sb.append(String.format("PtrOfArcVelocity:         %6d\n",PtrOfArcVelocity));
		sb.append(String.format("PtrOfArcWidth:            %6d\n",PtrOfArcWidth));
		sb.append(String.format("Nyquist:                  %6d\n",Nyquist));
		sb.append(String.format("CircleTotal:              %6d\n",CircleTotal));
		sb.append(String.format("Echodata:                 %s\n",Echodata));
		
		return sb.toString();
	}
}
