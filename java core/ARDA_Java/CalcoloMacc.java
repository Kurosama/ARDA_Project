import java.io.*;

/*	Calcola la matrice di accellerazione estraendo i punti dal file e calcolando le velocità spaziale tra 2 rilevazioni
 *  consecutive.
 *  Ne ricava le medie per zona, la media totale e una costante moltiplicativa.
 * 
 *  Mpot gestisce come caricare le matrici di importanza
 * 	 -CalcoloMacc(PATH,metrilato,indice)     		 -> ricava Mpot tramite CalcolaIndiciLocalita
 * 	 -CalcoloMacc(PATH,metrilato,Mpot,n,m,maxVisite) -> utilizza la matrice passata come argomento
 *                                                             (Chiamata effettuata da SRmatriciDizionario).
 * 
 *	PARAMETRI:
 *	pathfile  =	il file che contiene i dati del GPS
 *	metriLato =	la granularita con la quale eseguire la parcellazione
 *	indice	  =	indica il grafico che si vuole ricavare
 *
 *	INFO FLAG indice:
 *	indice=='NV'  ->  calcolo Mimp usando indice Number Of Visits
 *	indice=='TT'  ->  calcolo Mimp usando indice Total Time
 *	indice=='AT'  ->  calcolo Mimp usando indice Average Time
 *	indice=='CL'  ->  calcolo Mimp usando indice Combinazione Lineare NoV e TT
 *
 *	INFO FLAG metriLato:
 * 	metriLato==50   ->  calcolo Mimp usando parcellazione con quadrati con lato di metri 50
 *	metriLato==100  ->  calcolo Mimp usando parcellazione con quadrati con lato di metri 100
 *	metriLato==500  ->  calcolo Mimp usando parcellazione con quadrati con lato di metri 500
 *	metriLato==1000 ->  calcolo Mimp usando parcellazione con quadrati con lato di metri 1000
 */


public class CalcoloMacc{
    
    CalcolaIndiciLocalita ind_loc;
    public int n,m,maxVisite;
    public double Xmax,Ymax,Xmin,Ymin,GradiLatoX,GradiLatoY;
    
    private double[][] Mimp;
    private double[][] Mpot;

    private double[][][] Macc;
    private double[][]   MmediaAccelerazioni;
    private double[][][] MelencoAccelerazioni;
    private double[][][] MelencoVelocita;
    
    private double valoreMediaAccelerazioneReale;
    private double valoremediaAccelerazioni;
    private double costanteMoltiplicativa;
	
    public CalcoloMacc(String PATH, int metrilato,String indice){
        System.out.println("Inizio del calcolare la matrice di importanza");
		ind_loc= new CalcolaIndiciLocalita(PATH,metrilato,indice);
		n=ind_loc.get_n();
		m=ind_loc.get_m();
		maxVisite=ind_loc.get_maxVisite();

		//importo le variabili sopra stanti;
		System.out.println("Termine del calcolare la matrice di importanza");
   	 	Mimp=new double[n][m];
   	 	Mimp=ind_loc.get_Mimp();
    	Mpot=new double[n][m];
    	Mpot=ind_loc.get_Mpot();
    	
    	GradiLatoX=ind_loc.get_LatoX();
        GradiLatoY=ind_loc.get_LatoY();
        Xmax=ind_loc.get_Xmax();
        Ymax=ind_loc.get_Ymax();
        Xmin=ind_loc.get_Xmin();
        Ymin=ind_loc.get_Ymin();
    	
    	retMacc(PATH,metrilato);
    }
    
    public CalcoloMacc(String PATH, int metrilato, double [][] Mpot,int n,int m,double Xmax,double Ymax,double Xmin,double Ymin ,double GradiLatoX, double GradiLatoY, int maxVisite){
        System.out.println("Inizio del calcolare la matrice di importanza");
        System.out.println("Inizio calcolo Macc "+maxVisite);
		this.maxVisite=maxVisite;
		this.Mpot=Mpot;
		double[][] Mimp=new double[n][m];
		for(int i=0;i<n;i++)
			for(int j=0;j<m;j++)
				Mimp[i][j]=-Mpot[i][j];
		this.Mimp=Mimp;
		this.n=n;
		this.m=m;
		this.Xmax=Xmax;
		this.Ymax=Ymax;
		this.Xmin=Xmin;
		this.Ymin=Ymin;
		this.GradiLatoX=GradiLatoX;
		this.GradiLatoY=GradiLatoY;
		System.out.println("Termine del calcolare la matrice di importanza");
    	
    	retMacc(PATH,metrilato);
    }
    
    public void retMacc(String PATH, int metrilato){
    	
    	//Uso li stessi valori per il calcolo delle dimensioni delle zone
        
        System.out.println("Inizio calcolo Macc");
	
		MelencoAccelerazioni=new double[n][m][maxVisite+2];
		MmediaAccelerazioni=new double[n][m];
		
		MelencoVelocita=new double[n][m][maxVisite+2];
	
		//Creo la matrici Macc accoppiando i valori di accelerazione di yMacc e xMacc calcolata 
		//tramite l'operatore di Sobel con Sobel(Mimp,fatt)
		//altrimenti con il calcolo del gradiente di python gradiente(Mpot)
		Macc=new double[n][m][2];
		Macc=gradiente(Mpot);
		//[0]=yMacc; [1]=xMacc
		
		try{
            FileInputStream fstream = new FileInputStream(PATH);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            
            double x,y;
            int day,month,year,hour,minute,second;			//data presente
            int Pday,Pmonth,Pyear,Phour,Pminute,Psecond;	//data precedente
            Pday=1; Pmonth=1; Pyear=2000; Phour=0; Pminute=0; Psecond=0;
            
            int x_zona_prec=-1, y_zona_prec=-1;
            double x_prec=0.0, y_prec=0.0;
            int sec_nuova_entry=10, numero=0;
            
	    	int gg2=172800;
	    	int min5=300;
            double velocita,Pvelocita=0.0,accelerazione;
            
            while ((strLine = br.readLine()) != null){
            	y=Double.parseDouble(strLine.substring(0,10));
            	x=Double.parseDouble(strLine.substring(11,21));
            	day=Integer.parseInt(strLine.substring(22,24));
            	month=Integer.parseInt(strLine.substring(25,27));
            	year=Integer.parseInt(strLine.substring(28,32));
            	hour=Integer.parseInt(strLine.substring(33,35));
            	minute=Integer.parseInt(strLine.substring(36,38));
            	second=Integer.parseInt(strLine.substring(39,41));
				//calcolo della differenza in secondi
				int diff_in_sec=(second-Psecond)+(minute-Pminute)*60+(hour-Phour)*3600+(day-Pday)*86400;
				//calcolo della zona in qui risiede la posizione valutata
				int x_zona=(int)((x-Xmin)/(float)GradiLatoX);
				int y_zona=(int)((y-Ymin)/(float)GradiLatoY);
				int i;
				//Aggiungo la velocita nelle zona
				if(diff_in_sec<min5 && diff_in_sec!=0){
					if(numero==0){
						numero++;velocita=0.0;
					}
					else{
						velocita=distanza(y_prec,x_prec,y,x)/(double)diff_in_sec;
						i=(int)MelencoVelocita[x_zona_prec][y_zona_prec][0]+1;
						MelencoVelocita[x_zona_prec][y_zona_prec][0]=i;
						MelencoVelocita[x_zona_prec][y_zona_prec][i]=velocita;
						
						accelerazione=Pvelocita-velocita;
						if(accelerazione!=0.0 && Math.abs(accelerazione)>0.000000001){
							i=(int)MelencoAccelerazioni[x_zona_prec][y_zona_prec][0]+1;
							MelencoAccelerazioni[x_zona_prec][y_zona_prec][0]=i;
							MelencoAccelerazioni[x_zona_prec][y_zona_prec][i]=accelerazione;
						}
					}
				}
				else{velocita=0.0;numero++;}
	
				
				Pday=day;	Pmonth=month;	Pyear=year;
				Phour=hour;	Pminute=minute;	Psecond=second;
				x_zona_prec=x_zona;		y_zona_prec=y_zona;
				x_prec=x;	y_prec=y;
				Pvelocita=velocita;
		}
	    in.close();
		}catch (Exception e){
			System.err.println("Errore: " + e.getMessage());
		}
		
		// calcolo dell'accellerazione media in ogni zona
		double somma=0.0;
		
		for(int i=0;i<n;i++){
			for(int j=0;j<m;j++){
				if(MelencoAccelerazioni[i][j][0]>0){
					for(int z=1;z<=MelencoAccelerazioni[i][j][0];z++){
						somma=somma+MelencoAccelerazioni[i][j][z];
					}
					MmediaAccelerazioni[i][j]=(float)somma/(float)(MelencoAccelerazioni[i][j][0]);
				}
				else MmediaAccelerazioni[i][j]=0.0;
				somma=0;
			}
		}
		
		//calcolo il valore medio totale della matrice MmediaAccellerazioni
		somma=0;
		int contatore=0;
		for(int i=0;i<n;i++){
			for(int j=0;j<m;j++){
				if(MmediaAccelerazioni[i][j]!=0.0 && MmediaAccelerazioni[i][j]>0.000000001 && MmediaAccelerazioni[i][j]<=1.0){
					somma+=MmediaAccelerazioni[i][j];
					contatore++;
				}
			}
		}
		double valoreMediaAccelerazioneReale=(float)somma/(float)contatore;
		
		
		somma=0.0;
		contatore=0;
		for(int i=0;i<n;i++){
			for(int j=0;j<m;j++){
				if(Macc[i][j][0]!=0.0 || Macc[i][j][1]!=0.0){
					double valore=Math.sqrt(Math.pow(Macc[i][j][0],2.0)+Math.pow(Macc[i][j][1],2.0));
					somma+=valore;
					contatore++;
				}
			}
		}
		double valoremediaAccelerazioni=(float)somma/(float)contatore;
		
		double costanteMoltiplicativa=(float)valoreMediaAccelerazioneReale/(float)valoremediaAccelerazioni;
		
		//Visualizzazione dati calcolati
		System.out.println("Valore medio accelerazione reale= "+valoreMediaAccelerazioneReale);
		System.out.println(" Valore medio accelerazione Macc= "+valoremediaAccelerazioni);
		System.out.println("  Valore Costante moltiplicativa= "+costanteMoltiplicativa);
		
		//Ricalcolo della matrice di accelerazione con la costante moltiplicativa
		for(int i=0;i<n;i++){
			for(int j=0;j<m;j++){
				Macc[i][j][0]=Macc[i][j][0]*costanteMoltiplicativa;
				Macc[i][j][1]=Macc[i][j][1]*costanteMoltiplicativa;
			}
		}
		System.out.println("Finito di calcolare Macc");

    }//retMpot
	
	public double distanza(double lat1, double lon1, double lat2, double lon2) {
		double res;
		res=Math.sqrt(((lon1-lon2)*(lon1-lon2))+((lat1-lat2)*(lat1-lat2)));
		return res;
	}
	
    public double distanza2(double lat1, double lon1, double lat2, double lon2) {
        double EARTH_RADIUS_KM=6378.7;
        double TO_RAD=57.29577951;
        double CELL_SIZE=0.5;
        double DIST_SLC=EARTH_RADIUS_KM*Math.acos(Math.sin((lat1)/TO_RAD) * Math.sin((lat2)/TO_RAD) + Math.cos((lat1)/TO_RAD)*Math.cos((lat2)/TO_RAD)*Math.cos((lon2 - lon1)/TO_RAD));
        DIST_SLC/=(double)100;
        return DIST_SLC;
    }//distanza
    
    private double[][][] Sobel(double [][] Mimp,double fatt){
		double[][] Sx=new double[3][3];
		double[][] Sy=new double[3][3];
		double[][][] ris=new double [n][m][2];
		Sx[0][0]=-1*fatt; Sx[0][1]=0; Sx[0][2]=1*fatt; Sy[0][0]=1*fatt;  Sy[0][1]=2*fatt;	Sy[0][2]=1*fatt;
		Sx[1][0]=-2*fatt; Sx[1][1]=0; Sx[1][2]=2*fatt; Sy[1][0]=0;  	 Sy[1][1]=0;  		Sy[1][2]=0;
		Sx[2][0]=-1*fatt; Sx[2][1]=0; Sx[2][2]=1*fatt; Sy[2][0]=-1*fatt; Sy[2][1]=-2*fatt; 	Sy[2][2]=-1*fatt;
		
		for(int itot=0;itot<n;itot++){
			for(int jtot=0;jtot<m;jtot++){
				//valuto sul punto [itot][jtot]
				for(int i=0;i<3;i++){
					for(int j=0;j<3;j++){
						int vali=itot+(i-1);
						int valj=jtot+(j-1);
						if(vali>=0 && vali<n && valj>=0 && valj<m){
							//riempio Gx
								ris[itot][jtot][0]+=Mimp[vali][valj]*Sx[i][j];
							//riempio Gy
								ris[itot][jtot][1]+=Mimp[vali][valj]*Sy[i][j];
						}
					}
				}
			}
		}
		return ris;
	}
	
	private double[][][] gradiente (double [][] Mimp){
		double[][][] ris=new double [n][m][2];
		//calcolo Gy
		for(int i=0;i<n;i++){
			for(int j=1;j<(m-1);j++){
				ris[i][j][0]=(Mimp[i][j+1]-Mimp[i][j-1])/2.0;
			}
		}
		for(int i=0;i<n;i++){
			ris[i][0][0]=Mimp[i][1]-Mimp[i][0];
			ris[i][m-1][0]=Mimp[i][m-1]-Mimp[i][m-2];
		}
		//calcolo Gx
		for(int i=1;i<(n-1);i++){
			for(int j=0;j<m;j++){
				ris[i][j][1]=(Mimp[i+1][j]-Mimp[i-1][j])/2.0;
			}
		}
		for(int j=0;j<m;j++){
			ris[0][j][1]=Mimp[1][j]-Mimp[0][j];
			ris[n-1][j][1]=Mimp[n-1][j]-Mimp[n-2][j];
		}
		return ris;
	}
    
    public int get_n(){return n;}
	public int get_m(){return m;}
	public int get_maxVisite(){return maxVisite;}

    public double[][] get_Mimp(){return Mimp;}
    public double[][] get_Mpot(){return Mpot;}

    public double[][][] get_Macc(){return Macc;}
    public double[][]   get_MmediaAccelerazioni(){return MmediaAccelerazioni;}
    public double[][][] get_MelencoAccelerazioni(){return MelencoAccelerazioni;}
    public double[][][] get_MelencoVelocita(){return MelencoVelocita;}
    
    public double get_valoreMediaAccelerazioneReale(){return valoreMediaAccelerazioneReale;}
    public double get_valoremediaAccelerazioni(){return valoremediaAccelerazioni;}
    public double get_costanteMoltiplicativa(){return costanteMoltiplicativa;}
    
    
}//class CalcoloMpotMacc
