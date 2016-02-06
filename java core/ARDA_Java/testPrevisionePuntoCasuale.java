import java.io.*;

/*
 * Teoricamente serve a fare tutti i test in maniera automatica
 * 
 * Questo Script serve a rilevare i test di previsione naive a comportamento casuale
 * 
 */

public class testPrevisionePuntoCasuale {
	
	private int n,m;
	private double Ymax,Xmax;
    private double Ymin,Xmin;
    private double GradiLatoX, GradiLatoY;
    
    
    
    CalcoloMacc Calc_Macc;
	private double[][][] MelencoVelocita;
	private double precisionecelle;
	
	public testPrevisionePuntoCasuale(String PATH, int metrilato){
		Calc_Macc=new CalcoloMacc(PATH,metrilato,"NV");
	}
	
	public double retMediaVelocita(String PATH, int metrilato){
		/*	Fa le previsioni per l'algoritmo naive a punto casuale
		 * 	PS: LE MISURE SONO ESPESSE IN caselle/secondo
		 * 
		 * 	Es:	retMediaVelocita('atlanta.txt',50)
		 */
		
		System.out.println("INIZIO calcolo media velocita in celle/secondo");
		precisionecelle=(float)(Calc_Macc.GradiLatoX+Calc_Macc.GradiLatoY)/(float)2;
		MelencoVelocita=Calc_Macc.get_MelencoVelocita();
		
		double totaleVelocita=0.0,totaleAfferentiVelocita=0.0,mediaVelocita=0.0;
		for(int i=0;i<n;i++){
			for(int j=0;j<m;j++){
				if(MelencoVelocita[i][j][0]>0){
					for(int z=1;z<=MelencoVelocita[i][j][0];z++){
						totaleVelocita=totaleVelocita+MelencoVelocita[i][j][z];
						totaleAfferentiVelocita++;
					}
				}
			}
		}
		mediaVelocita=(float)totaleVelocita/(float)totaleAfferentiVelocita;
		System.out.println("FINE calcolo media velocita in celle/secondo => "+mediaVelocita);
		return mediaVelocita;
	}

	public double[] previsione(double mediaVelocita,double xUltimo,double yUltimo,double afterTime){
		double ris[]=new double[2];
		double alpha=Math.random()*2*Math.PI;
		double magnitudo=Math.random()*mediaVelocita;
		double xSpost=magnitudo*afterTime*Math.cos(alpha);
		double ySpost=magnitudo*afterTime*Math.sin(alpha);
		ris[0]=xUltimo+xSpost;
		ris[1]=yUltimo+ySpost;
		return ris;
	}
}
