import java.io.*;
import java.util.Calendar;

/*
 *      Reperisce i dati di tutti gli spostamenti effetuati contenuti su dei file opportunamente strutturati
 *      e genera le 4 matrici con i vari indici di importanza chiedendo di impostare la grandezza
 *      delle zone (per comodita e' quadrate) e il PATH del file da caricare.
 *
 *      La terra non e' quadrata ed è difficile ottenere celle perfettamente quadrate sopratutto
 *      se valuto celle molto grandi (centinai di chilometri) e a delle latitudini vicine ai poli
 * 		(dove tendono ad essere triangolari e non quadrate)
 *
 *      FORMATTAZIONE DEI FILE CONTENENTI I DATI
 *
 *		   lat	      long
 *          y          x        date      time
 *		000000000011111111112222222222333333333344
 *		012345678901234567890123456789012345678901
 *	  ||0033.93885|-084.33697|25/09/2001|11:09:00-(EOL)
 */

public class CalcolaIndiciLocalita {

    private int n,m;

    private double[][]   MnumOfVisits;
    private double[][]   MtotalTime;
    private double[][]   MavgTime;
    private double[][]   McombLinearNumVisitsTotTime;
    private double[][]   Mimp;
    private double[][]   Mpot;
	private int[][]	 MvisPunti;

	private double Ymax,Xmax;
    private double Ymin,Xmin;
    private double GradiLatoX, GradiLatoY;

    public CalcolaIndiciLocalita(String PATH, int metrilato,String indice){

		//imposto la grandezza in gradi del lato di ogni area quadrata

        GradiLatoX=0.0;
        GradiLatoY=0.0;
        switch (metrilato) {
			case 50:  GradiLatoX=0.000535;		GradiLatoY=0.00045;		break;
			case 100: GradiLatoX=0.001077;		GradiLatoY=0.0009;		break;
			case 300: GradiLatoX=0.001077*3;	GradiLatoY=0.0009*3;	break;
			case 500: GradiLatoX=0.00535;		GradiLatoY=0.0045;		break;
			case 1000:GradiLatoX=0.01077;		GradiLatoY=0.009;		break;
			case 3000:GradiLatoX=0.01077*3;		GradiLatoY=0.009*3;		break;
			default: break;
		}

		/* leggo dal file tutte le coordinate e cerco i punti con coordinate minime (minX,minY) e massime (maxX,maxY)
		 * e li uso per determinare la grandezza delle matrici(n,m):
		 */

		double y,x;
		Ymax=-1000.0;	Xmax=-1000.0;
		Ymin=1000.0;	Xmin=1000.0;
		try{
		    FileInputStream fstream = new FileInputStream(PATH);
		    DataInputStream in = new DataInputStream(fstream);
		    BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    String strLine;

		    while ((strLine = br.readLine()) != null){
		    	y=Double.parseDouble(strLine.substring(0,10));
		    	x=Double.parseDouble(strLine.substring(11,21));
		    	if(Xmax<x) Xmax=x;
		    	if(Xmin>x) Xmin=x;
		    	if(Ymax<y) Ymax=y;
		    	if(Ymin>y) Ymin=y;
		    }
            in.close();
		}catch (Exception e){
		    System.err.println("Errore: " + e.getMessage());
		}

		n=(int)((Xmax-Xmin)/GradiLatoX)+1;  //larghezza matrice
		m=(int)((Ymax-Ymin)/GradiLatoY)+1;  //altezza matrice

		// imposto le varie matrici di importanza

		MnumOfVisits=new double[n][m];
		MtotalTime=new double[n][m];
		MavgTime=new double[n][m];
		McombLinearNumVisitsTotTime=new double[n][m];
		MvisPunti=new int[n][m];

        for(int i=0;i<n;i++){
            for(int j=0;j<m;j++){
        		MnumOfVisits[i][j]=0;
        		MtotalTime[i][j]=0;
        		MavgTime[i][j]=0;
        		McombLinearNumVisitsTotTime[i][j]=0;
        		MvisPunti[i][j]=0;
            }
        }

        //riempio le varie matrici

        try{
            FileInputStream fstream = new FileInputStream(PATH);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            int day,month,year,hour,minute,second;			//data presente
            int Pday,Pmonth,Pyear,Phour,Pminute,Psecond;	//data precedente
            Pday=1; Pmonth=1; Pyear=1900; Phour=0; Pminute=0; Psecond=1;

            Calendar secondDate = Calendar.getInstance();
            Calendar PsecondDate = Calendar.getInstance();

            PsecondDate.set(Calendar.YEAR,1900);
            PsecondDate.set(Calendar.MONTH,0);
            PsecondDate.set(Calendar.DAY_OF_MONTH,1);
            PsecondDate.set(Calendar.HOUR_OF_DAY,0);
            PsecondDate.set(Calendar.MINUTE,0);
            PsecondDate.set(Calendar.SECOND,1);

            int x_zona_prec=-1, y_zona_prec=-1;
            double x_prec=0.0, y_prec=0.0;

            while ((strLine = br.readLine()) != null){
            	y=Double.parseDouble(strLine.substring(0,10));
            	x=Double.parseDouble(strLine.substring(11,21));
            	day=Integer.parseInt(strLine.substring(22,24));
            	month=Integer.parseInt(strLine.substring(25,27));
            	year=Integer.parseInt(strLine.substring(28,32));
            	hour=Integer.parseInt(strLine.substring(33,35));
            	minute=Integer.parseInt(strLine.substring(36,38));
            	second=Integer.parseInt(strLine.substring(39,41));

                secondDate = Calendar.getInstance();
                secondDate.set(Calendar.YEAR,year);
                secondDate.set(Calendar.MONTH,month-1);
                secondDate.set(Calendar.DAY_OF_MONTH,day);
                secondDate.set(Calendar.HOUR_OF_DAY,hour);
                secondDate.set(Calendar.MINUTE,minute);
                secondDate.set(Calendar.SECOND,second);

            	//calcolo della differenza in secondi
                int diff_in_sec =(int)(secondDate.getTimeInMillis()-PsecondDate.getTimeInMillis())/1000;

				//calcolo della zona in qui risiede la posizione valutata
            	int x_zona=(int)((x-Xmin)/GradiLatoX);
            	int y_zona=(int)((y-Ymin)/GradiLatoY);

            	//tengo conto di quanti punti fanno riferimento a quella zona
            	MvisPunti[x_zona][y_zona]++;

            	//Ogni volta che cambio zona aggiungo 1 sulla nuova zona
            	if(x_zona!=x_zona_prec || y_zona!=y_zona_prec)
            		MnumOfVisits[x_zona][y_zona]+=1;

            	//Aggiungo le tempistiche nella matrice dei tempi totali
            	if (diff_in_sec<172800){
					if (x_zona_prec!=-1 && y_zona_prec!=-1)
						MtotalTime[x_zona_prec][y_zona_prec]+=diff_in_sec;
            	}

            	Pday=day;	Pmonth=month;	Pyear=year;
            	Phour=hour;	Pminute=minute;	Psecond=second;

                PsecondDate.set(Calendar.YEAR,year);
                PsecondDate.set(Calendar.MONTH,month-1);
                PsecondDate.set(Calendar.DAY_OF_MONTH,day);
                PsecondDate.set(Calendar.HOUR_OF_DAY,hour);
                PsecondDate.set(Calendar.MINUTE,minute);
                PsecondDate.set(Calendar.SECOND,second);

            	x_zona_prec=x_zona;		y_zona_prec=y_zona;
            	x_prec=x;	y_prec=y;
            }
            in.close();
	    }catch (Exception e){
            System.err.println("Errore: " + e.getMessage());
	    }

	    //Calcolo la matrice con i tempi medi
	    for(int i=0;i<n;i++){
	       	for(int j=0;j<m;j++){
               	if(MnumOfVisits[i][j]!=0)
                   	MavgTime[i][j]=MtotalTime[i][j]/MnumOfVisits[i][j];
	        }
	    }

	    //Calcolo la matrice della combinazione lineare tra il totale della visite e il tempo totale
	    //(50% di peso per ognusa sul valore finale)
	    for(int i=0;i<n;i++){
	       	for(int j=0;j<m;j++){
	    		McombLinearNumVisitsTotTime[i][j]=(MtotalTime[i][j]*0.5)+(MnumOfVisits[i][j]/60*0.5);
	       	}
	    }

	    //calcolo Mpot-Mimp
	    Mimp=new double[n][m];
    	Mpot=new double[n][m];
	    double maxElemMimp=1;

    	if(indice=="NV"){
			for(int i=0;i<n;i++){
    			for(int j=0;j<m;j++){
    				if (maxElemMimp<MnumOfVisits[i][j])
    					maxElemMimp=MnumOfVisits[i][j];
    			}
    		}

    		for(int i=0;i<n;i++){
    			for(int j=0;j<m;j++){
    				Mimp[i][j]=(float)MnumOfVisits[i][j]/(float)maxElemMimp;
    				Mpot[i][j]=-Mimp[i][j];
    			}
    		}
    	}//caso NV

    	if(indice=="TT"){
    		for(int i=0;i<n;i++){
    			for(int j=0;j<m;j++){
    				if (maxElemMimp<MtotalTime[i][j])
    					maxElemMimp=MtotalTime[i][j];
    			}
    		}
    		for(int i=0;i<n;i++){
    			for(int j=0;j<m;j++){
    				Mimp[i][j]=(float)MtotalTime[i][j]/(float)maxElemMimp;
    				Mpot[i][j]=-Mimp[i][j];
    			}
    		}
    	}//caso TT

    	if(indice=="AT"){
    		for(int i=0;i<n;i++){
    			for(int j=0;j<m;j++){
    				if (maxElemMimp<MavgTime[i][j])
    					maxElemMimp=MavgTime[i][j];
    			}
    		}
    		for(int i=0;i<n;i++){
    			for(int j=0;j<m;j++){
    				Mimp[i][j]=(float)MavgTime[i][j]/(float)maxElemMimp;
    				Mpot[i][j]=-Mimp[i][j];
    			}
    		}
    	}//caso AT

    	if(indice=="CL"){
    		for(int i=0;i<n;i++){
    			for(int j=0;j<m;j++){
    				if (maxElemMimp<McombLinearNumVisitsTotTime[i][j])
    					maxElemMimp=McombLinearNumVisitsTotTime[i][j];
    			}
    		}
    		for(int i=0;i<n;i++){
    			for(int j=0;j<m;j++){
    				Mimp[i][j]=(float)McombLinearNumVisitsTotTime[i][j]/(float)maxElemMimp;
    				Mpot[i][j]=-Mimp[i][j];
    			}
    		}

    	}//caso CL


    }//CalcolaIndiciLocalita'

    //metodi per il passaggio di paramatri alle altre classi

    public int get_n(){return n;}

    public int get_m(){return m;}

    public double[][] get_MnumOfVisits(){return MnumOfVisits;}

    public double[][] get_MtotalTime(){return MtotalTime;}

    public double[][] get_MavgTime(){return MavgTime;}

    public double[][] get_McombLin(){return McombLinearNumVisitsTotTime;}

    public double[][] get_Mimp(){return Mimp;}

    public double[][] get_Mpot(){return Mpot;}

    public double get_Xmax(){return Xmax;}

    public double get_Ymax(){return Ymax;}

    public double get_Xmin(){return Xmin;}

    public double get_Ymin(){return Ymin;}

    public double get_LatoX(){return GradiLatoX;}

    public double get_LatoY(){return GradiLatoY;}

    public int get_maxVisite(){
		int maxVisite=0;
		for(int i=0;i<n;i++){
			for(int j=0;j<m;j++){
				if(maxVisite<MvisPunti[i][j])
					maxVisite=MvisPunti[i][j];
			}
		}
		return maxVisite;
	}

}//class
