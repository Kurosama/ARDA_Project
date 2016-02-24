import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.RoundingMode;



/*	(1)serve per trovare tutti i test che desidero in maniera automatica
 *  (2)serve per trovare tutti i test che desidero in maniera automatica ma solo relativamente all'utilizzo dell'indice di SpaceRank
 *
 * questo script costruisce due files con i risultati delle previsioni delle destintazioni: uno suddiviso per cluster tempotali,
 * l'altro per percentuali di percorso da notare che i due file ottenuti per essere importati in Excel.
 */

public class MediaVarianzaAutomaticaPivotFinale {

	CalcoloMacc Calc_Macc;
	SRmatriciDizionari SR_matrici;
	ARDA_algorithm ARDA;

	int n,m,i,j;
	double[][] Mimp;
	double[][][] Macc;
	double[][] Mpot;
	double Xmin,Ymin,Xmax,Ymax;
	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy|HH:mm:ss");

	NumberFormat df = DecimalFormat.getInstance();

	public MediaVarianzaAutomaticaPivotFinale(String pathFile,String pathFilePercorsoTest,
											  double[] VettoreDeltaTime0, String[] VettoreDeltaTime1, String[] VettoreIndici,int[] VettorePrecisioneGradi){

		// Setup formato decimali per export csv
		df.setMinimumFractionDigits(2);
		df.setMaximumFractionDigits(4);
		df.setRoundingMode(RoundingMode.DOWN);

		//##############################################################################################################

		String risTemp="";
		String risPerc="";

		int numeroIterazioni=20;

		int metrilato = 0;
		//System.out.println("\n\n############################################ INIZIO DEI TEST M.V.A.Pivot Finale  (1) ###############################################");

		// Ciclo Indici
		for(int i=0;i<VettoreIndici.length;i++){

			risTemp="";
			risPerc="";
			String indice=VettoreIndici[i];
			System.out.println("TEST indice "+indice+" iniziato");
			if(indice == "SR" ){
				int[] VettoreNumIterazSRtoMimp=new int[4];	//numero di iterazioni per calcolo autovettore dominante in space rank
				VettoreNumIterazSRtoMimp[0]=1;	VettoreNumIterazSRtoMimp[1]=10;	VettoreNumIterazSRtoMimp[2]=25;	VettoreNumIterazSRtoMimp[3]=50;
				for(int w=0;w<VettoreNumIterazSRtoMimp.length;w++){
					int numeroIterazioniSRtoMimp=VettoreNumIterazSRtoMimp[w];
					indice = "SR_"+numeroIterazioniSRtoMimp;

					for(w=0;w<VettorePrecisioneGradi.length;w++){
						metrilato=VettorePrecisioneGradi[w];

						System.out.println("- TEST indice "+indice+" - "+metrilato);
						SR_matrici=new SRmatriciDizionari(pathFile,metrilato,numeroIterazioni); 		//(2)

						n=SR_matrici.get_n();			m=SR_matrici.get_m();

						//ricavo i dati dai percorsi rilevati
						n=SR_matrici.get_n();
						m=SR_matrici.get_m();
						Mimp=new double[n][m];
						Mimp=SR_matrici.get_Mimp();
						Macc=new double[n][m][2];
						Macc=SR_matrici.get_Macc();

						//System.out.println("\n\n############################################ INIZIO DEI TEST M.V.A.Pivot Finale (2) ###############################################");


						for(j=0;j<VettoreDeltaTime0.length;j++){
							double deltaTimeMassimo=VettoreDeltaTime0[j];
							String stringaDeltaTime=VettoreDeltaTime1[j];
							String[] risultato=new String[2];
							System.out.println(" -- TEST indice "+indice+" - "+metrilato+" - "+deltaTimeMassimo);
							risultato=esecuzioneTest(risTemp,risPerc,pathFilePercorsoTest,pathFile,metrilato,deltaTimeMassimo,indice,stringaDeltaTime);
							risTemp=risultato[0]; risPerc=risultato[1];
						}
					}
				}

				try{
					FileOutputStream file = new FileOutputStream("..\\Risultati\\risultatiTempCasuale_"+pathFile+"_SR.csv");
					PrintStream Output = new PrintStream(file);
					Output.print(risTemp);
					file.close();
				}catch (Exception e){
					System.err.println(" (MV 3) Errore: " + e.getMessage());
				}

				try{
					FileOutputStream file = new FileOutputStream("..\\Risultati\\risultatiPercCasuale_"+pathFile+"_SR.csv");
					PrintStream Output = new PrintStream(file);
					Output.print(risPerc);
					file.close();
				}catch (Exception e){
					System.err.println(" (MV 4) Errore: " + e.getMessage());
				}

			}else{
				// Ciclo Precisione
				for(int w=0;w<VettorePrecisioneGradi.length;w++){
					metrilato=VettorePrecisioneGradi[w];

					// Spostato fuori del ciclo j perché verrebbe calcolato j-1 volte di troppo ;)

					Calc_Macc=new CalcoloMacc(pathFile,metrilato,indice);						//(1)
					n=Calc_Macc.get_n();			m=Calc_Macc.get_m();
					Xmin=Calc_Macc.get_Xmin();		Xmax=Calc_Macc.get_Xmax();
					Ymin=Calc_Macc.get_Ymin();		Ymax=Calc_Macc.get_Ymax();

					//ricavo i dati dai percorsi rilevati
					Mimp=new double[n][m];		Mimp=Calc_Macc.get_Mimp();
					Macc=new double[n][m][2];	Macc=Calc_Macc.get_Macc();
					Mpot=new double[n][m];		Mpot=Calc_Macc.get_Mpot();

					// Ciclo DeltaTime
					for(int j=0;j<VettoreDeltaTime0.length;j++){

						double deltaTimeMassimo=VettoreDeltaTime0[j];
						String stringaDeltaTime=VettoreDeltaTime1[j];
						String[] risultato=new String[2];

						//######################################################################################

						//System.out.println("\n----------------------------------------------------");
						//System.out.println("Test nr: "+(i+1)+" -> |"+indice+"|"+metrilato+"|"+(deltaTimeMassimo/60));

						risultato=esecuzioneTest(risTemp,risPerc,pathFilePercorsoTest,pathFile,metrilato,deltaTimeMassimo,indice,stringaDeltaTime);

						//######################################################################################

						risTemp=risultato[0]; risPerc=risultato[1];

					} // Ciclo DeltaTime
				} // Ciclo

				try{
					FileOutputStream file = new FileOutputStream("..\\Risultati\\risultatiTemp_"+pathFile+"_"+indice+".csv");
					PrintStream Output = new PrintStream(file);
					Output.print(risTemp);
					file.close();
				}catch (Exception e){
					System.err.println(" (MV 1) Errore: " + e.getMessage());
				}

				try{
					FileOutputStream file = new FileOutputStream("..\\Risultati\\risultatiPerc_"+pathFile+"_"+indice+".csv");
					PrintStream Output = new PrintStream(file);
					Output.print(risPerc);
					file.close();
				}catch (Exception e){
					System.err.println(" (MV 2) Errore: " + e.getMessage());
				}
			} // Controllo SR
			System.out.println("TEST indice "+indice+" concluso");
			System.gc();
		}// Ciclo Indici

		//System.out.println("\n##########################FINE TEST (1) ##########################\n\n");

	}// MediaVarianzaAutomaticaPivotFinale_Mimp

	public String[] esecuzioneTest(String risTemp,String risPerc,String pathFilePercorsoTest,String pathFile,int metriLato, double deltaTimeMassimo,String indice,String stringaDeltaTime){
		double afterTime=1;

		//SCRIVO IL CODICE MA DEVO CONTROLLARE SE EFFETTIVAMENTE LE MATRICI SONO IDENTICHE, ALTRIMENTI MI CONVIENE LANCIARE 2 METODI DIFFERENTI.

		ARDA= new ARDA_algorithm();

		double GradiLatoX=0.0, GradiLatoY=0.0;
		switch (metriLato) {
			case 50:  GradiLatoX=0.000535;		GradiLatoY=0.00045;		break;
			case 100: GradiLatoX=0.001077;		GradiLatoY=0.0009;		break;
			case 300: GradiLatoX=0.001077*3;	GradiLatoY=0.0009*3;	break;
			case 500: GradiLatoX=0.00535;		GradiLatoY=0.0045;		break;
			case 1000:GradiLatoX=0.01077;		GradiLatoY=0.009;		break;
			case 3000:GradiLatoX=0.01077*3;		GradiLatoY=0.009*3;		break;
			default: break;
		}

		//pongo maxBrakeValue = al valore massimo presente in Mimp
		double maxBrakeValue=0;
		for(int i=0;i<n;i++){
			for(int j=0;j<m;j++){
				if (Mimp[i][j]>maxBrakeValue)
	                maxBrakeValue=Mimp[i][j];
			}
		}

		//Inizializzo i vettori per le varie previsioni

		int[] valoriOttimizati= new int[2];

		ArrayList<ArrayList<ArrayList<String>>> vettorePercorsi = new ArrayList<ArrayList<ArrayList<String>>>();

		ArrayList<ArrayList<Double>>     vettorePrevisioni=new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>>   vettorePrevisioni_0=new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>>  vettorePrevisioni_25=new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>>  vettorePrevisioni_50=new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>>  vettorePrevisioni_75=new ArrayList<ArrayList<Double>>();

		ArrayList<ArrayList<Double>>    vettoreDiConfronto=new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>>  vettoreDiConfronto_0=new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> vettoreDiConfronto_25=new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> vettoreDiConfronto_50=new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> vettoreDiConfronto_75=new ArrayList<ArrayList<Double>>();

		String s=" ";

		try{
            FileInputStream fstream = new FileInputStream(pathFilePercorsoTest);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            double ultimoX,ultimoY;
            int day,month,year,hour,minute,second;			//data attuale
            int Pday,Pmonth,Pyear,Phour,Pminute,Psecond;	//data precedente
            Pday=1; Pmonth=1; Pyear=2000; Phour=0; Pminute=0; Psecond=0;

            int penultimaPrevisione_X=0, penultimaPrevisione_Y=0;
            int ultimaPrevisione_X=0,ultimaPrevisione_Y=0;
            double[] previsionePunto=new double[2];
           	double[] previsionePesata=new double[2];

           	int numDiRiga=0;
           	int numRiga=0;
           	int totaleAfterTime=0;
           	int numPercorso=-1;
           	int numPunto=0;

            while ((strLine = br.readLine()) != null){
            	numRiga++;
				// Da capire perché arrotonda
            	ultimoY=(int)((Double.parseDouble(strLine.substring(00,10))-Ymin)/GradiLatoY);
            	ultimoX=(int)((Double.parseDouble(strLine.substring(11,21))-Xmin)/GradiLatoX);
            	    day=   Integer.parseInt(strLine.substring(22,24));
            	  month=   Integer.parseInt(strLine.substring(25,27));
            	   year=   Integer.parseInt(strLine.substring(28,32));
            	   hour=   Integer.parseInt(strLine.substring(33,35));
            	 minute=   Integer.parseInt(strLine.substring(36,38));
            	 second=   Integer.parseInt(strLine.substring(39,41));

            	double diff_in_sec=(second-Psecond)+(minute-Pminute)*60+(hour-Phour)*3600+(day-Pday)*86400;

            	if(diff_in_sec>deltaTimeMassimo){
					numPunto=0;
					numPercorso++;
					ArrayList<ArrayList<String>> percorso = new ArrayList<ArrayList<String>>();
					ArrayList<String> punto= new ArrayList<String>();
					punto.add(""+ultimoX);
					punto.add(""+ultimoY);
					punto.add(strLine.substring(22,41)); //data attuale
            		percorso.add(punto);
            		vettorePercorsi.add(percorso);

            	}else{
					numPunto++;
					vettorePercorsi.get(numPercorso).add(new ArrayList<String>());
            		vettorePercorsi.get(numPercorso).get(numPunto).add(""+ultimoX);
            		vettorePercorsi.get(numPercorso).get(numPunto).add(""+ultimoY);
            		vettorePercorsi.get(numPercorso).get(numPunto).add(strLine.substring(22,41)); //data attuale
            	}
            	Pday=day;	Pmonth=month;	Pyear=year;
				Phour=hour;	Pminute=minute;	Psecond=second;
            }
            in.close();
		}catch (Exception e){
			System.err.println(" (MV 5) Errore: " + e.toString());
		}//fine try-catch

		//Qui devo mettere il codice che fa le previsioni
		int numRiga=0;
		GregorianCalendar tempo_da_trovare = new GregorianCalendar();
		GregorianCalendar tempo_vettore = new GregorianCalendar();

		// Valuto tutti i percorsi generati
		for(int i=0; i<vettorePercorsi.size();i++){
			int numeroPunti=vettorePercorsi.get(i).size();

			// Prendo l'ultimo punto del percorso
	        String       X_da_prevedere=vettorePercorsi.get(i).get(numeroPunti-1).get(0);
	        String       Y_da_prevedere=vettorePercorsi.get(i).get(numeroPunti-1).get(1);
	        GregorianCalendar tempo_dove_prevedere = new GregorianCalendar();
			try{
			  tempo_dove_prevedere.setTime(sdf.parse(vettorePercorsi.get(i).get(numeroPunti-1).get(2)));
			}
			catch(ParseException e){ e.printStackTrace(); }

	        // Prendo il primo punto del percorso
	        String       X_primo=vettorePercorsi.get(i).get(0).get(0);
	        String       Y_primo=vettorePercorsi.get(i).get(0).get(1);
	        GregorianCalendar tempo_primo = new GregorianCalendar();
			try{
			  tempo_primo.setTime(sdf.parse(vettorePercorsi.get(i).get(0).get(2)));
			}
			catch(ParseException e){ e.printStackTrace(); }

			// Prendo il secondo punto del percorso
	        String     X_secondo=vettorePercorsi.get(i).get(1).get(0);
	        String     Y_secondo=vettorePercorsi.get(i).get(1).get(1);
	        GregorianCalendar tempo_secondo = new GregorianCalendar();
			try{
			  tempo_secondo.setTime(sdf.parse(vettorePercorsi.get(i).get(1).get(2)));
			}
			catch(ParseException e){ e.printStackTrace(); }

			// test sul 25% del percorso
			tempo_da_trovare.setTimeInMillis(tempo_primo.getTimeInMillis()+((tempo_dove_prevedere.getTimeInMillis()-tempo_primo.getTimeInMillis())/4));
            int w=numeroPunti-1;
            try{
			  tempo_vettore.setTime(sdf.parse(vettorePercorsi.get(i).get(w).get(2)));
			}catch(ParseException e){e.printStackTrace();}

            while(tempo_da_trovare.getTimeInMillis()<tempo_vettore.getTimeInMillis()){
				w--;
				try{
					tempo_vettore.setTime(sdf.parse(vettorePercorsi.get(i).get(w).get(2)));
				}catch(ParseException e){e.printStackTrace();}
			}

            String X_25per100_primo=vettorePercorsi.get(i).get(w).get(0);
            String Y_25per100_primo=vettorePercorsi.get(i).get(w).get(1);
            GregorianCalendar tempo_25per100_primo = new GregorianCalendar();
			try{
			  tempo_25per100_primo.setTime(sdf.parse(vettorePercorsi.get(i).get(w).get(2)));
			}
			catch(ParseException e){ e.printStackTrace(); }

            String X_25per100_secondo=vettorePercorsi.get(i).get(w+1).get(0);
            String Y_25per100_secondo=vettorePercorsi.get(i).get(w+1).get(1);
            GregorianCalendar tempo_25per100_secondo = new GregorianCalendar();
			try{
			  tempo_25per100_secondo.setTime(sdf.parse(vettorePercorsi.get(i).get(w+1).get(2)));
			}
			catch(ParseException e){ e.printStackTrace(); }

			// test sul 50% del percorso
			tempo_da_trovare.setTimeInMillis(tempo_primo.getTimeInMillis()+((tempo_dove_prevedere.getTimeInMillis()-tempo_primo.getTimeInMillis())/2));
            w=numeroPunti-1;
            try{
			  tempo_vettore.setTime(sdf.parse(vettorePercorsi.get(i).get(w).get(2)));
			}catch(ParseException e){e.printStackTrace();}

            while(tempo_da_trovare.getTimeInMillis()<tempo_vettore.getTimeInMillis()){
				w--;
				try{
					tempo_vettore.setTime(sdf.parse(vettorePercorsi.get(i).get(w).get(2)));
				}catch(ParseException e){e.printStackTrace();}
			}

            String X_50per100_primo=vettorePercorsi.get(i).get(w).get(0);
            String Y_50per100_primo=vettorePercorsi.get(i).get(w).get(1);
            GregorianCalendar tempo_50per100_primo = new GregorianCalendar();
			try{
			  tempo_50per100_primo.setTime(sdf.parse(vettorePercorsi.get(i).get(w).get(2)));
			}
			catch(ParseException e){ e.printStackTrace(); }

            String X_50per100_secondo=vettorePercorsi.get(i).get(w+1).get(0);
            String Y_50per100_secondo=vettorePercorsi.get(i).get(w+1).get(1);
            GregorianCalendar tempo_50per100_secondo = new GregorianCalendar();
			try{
			  tempo_50per100_secondo.setTime(sdf.parse(vettorePercorsi.get(i).get(w+1).get(2)));
			}
			catch(ParseException e){ e.printStackTrace(); }

			// test sul 75% del percorso
            tempo_da_trovare.setTimeInMillis(tempo_primo.getTimeInMillis()+((tempo_dove_prevedere.getTimeInMillis()-tempo_primo.getTimeInMillis())*((long)3/(long)4)));
            w=numeroPunti-1;
            try{
			  tempo_vettore.setTime(sdf.parse(vettorePercorsi.get(i).get(w).get(2)));
			}catch(ParseException e){e.printStackTrace();}

            while(tempo_da_trovare.getTimeInMillis()<tempo_vettore.getTimeInMillis()){
				w--;
				try{
					tempo_vettore.setTime(sdf.parse(vettorePercorsi.get(i).get(w).get(2)));
				}catch(ParseException e){e.printStackTrace();}
			}

            String X_75per100_primo=vettorePercorsi.get(i).get(w).get(0);
            String Y_75per100_primo=vettorePercorsi.get(i).get(w).get(1);
            GregorianCalendar tempo_75per100_primo = new GregorianCalendar();
			try{
			  tempo_75per100_primo.setTime(sdf.parse(vettorePercorsi.get(i).get(w).get(2)));
			}
			catch(ParseException e){ e.printStackTrace(); }

            String X_75per100_secondo=vettorePercorsi.get(i).get(w+1).get(0);
            String Y_75per100_secondo=vettorePercorsi.get(i).get(w+1).get(1);
            GregorianCalendar tempo_75per100_secondo = new GregorianCalendar();
			try{
			  tempo_75per100_secondo.setTime(sdf.parse(vettorePercorsi.get(i).get(w+1).get(2)));
			}
			catch(ParseException e){ e.printStackTrace(); }


			// Dopo aver calcolato i punti per le statistiche procedo con i test.

            int[] grid_info=new int[2];				grid_info[0]=n;				grid_info[1]=m;
            double[][] risultato=new double[81][5];
            double[] previsionePesata=new double[2];

            //eseguo la previsione 1 su 4 (partendo dal 0% del percorso)
            double[] actual_point=new double[2];	actual_point[0]=Double.parseDouble(X_secondo);	actual_point[1]=Double.parseDouble(Y_secondo);
            double[] prev_point=new double[2];	  prev_point[0]=Double.parseDouble(X_primo);	  prev_point[1]=Double.parseDouble(Y_primo);
            afterTime=(tempo_dove_prevedere.getTimeInMillis()-tempo_secondo.getTimeInMillis())/1000;
            risultato=ARDA.predict_next_for(actual_point,prev_point,Macc,Mimp,grid_info,afterTime,maxBrakeValue);

            //Mostro la previsione che si ottiene come pesatura di tutti i punti dell'intorno
            for(int j=0;j<81;j++){
                previsionePesata[0]=previsionePesata[0]+risultato[j][0]*risultato[j][4];
                previsionePesata[1]=previsionePesata[1]+risultato[j][1]*risultato[j][4];
            }

			ArrayList<Double> previsione= new ArrayList<Double>();
			previsione.add(previsionePesata[0]);	previsione.add(previsionePesata[1]);
            vettorePrevisioni_0.add(previsione);

            ArrayList<Double> confronto= new ArrayList<Double>();
            confronto.add(Double.parseDouble(X_da_prevedere));	confronto.add(Double.parseDouble(Y_da_prevedere));	confronto.add(afterTime);
            vettoreDiConfronto_0.add(confronto);

            vettorePrevisioni.add(previsione);
			vettoreDiConfronto.add(confronto);


            //eseguo la previsione 2 su 4 (partendo dal 25% del percorso)
            actual_point[0]=Double.parseDouble(X_25per100_secondo);	actual_point[1]=Double.parseDouble(Y_25per100_secondo);
              prev_point[0]=Double.parseDouble(X_25per100_primo);	  prev_point[1]=Double.parseDouble(Y_25per100_primo);

            afterTime=(tempo_dove_prevedere.getTimeInMillis()-tempo_25per100_secondo.getTimeInMillis())/1000;
            risultato=ARDA.predict_next_for(actual_point,prev_point,Macc,Mimp,grid_info,afterTime,maxBrakeValue);

            //Mostro la previsione che si ottiene come pesatura di tutti i punti dell'intorno
            for(int j=0;j<81;j++){
                previsionePesata[0]=previsionePesata[0]+risultato[j][0]*risultato[j][4];
                previsionePesata[1]=previsionePesata[1]+risultato[j][1]*risultato[j][4];
            }

			ArrayList<Double> previsione25= new ArrayList<Double>();
			previsione25.add(previsionePesata[0]);	previsione25.add(previsionePesata[1]);
            vettorePrevisioni_25.add(previsione25);

            ArrayList<Double> confronto25= new ArrayList<Double>();
            confronto25.add(Double.parseDouble(X_da_prevedere));	confronto25.add(Double.parseDouble(Y_da_prevedere));	confronto25.add(afterTime);
            vettoreDiConfronto_25.add(confronto25);

            vettorePrevisioni.add(previsione25);
			vettoreDiConfronto.add(confronto25);


			//eseguo la previsione 3 su 4 (dal 50% del percorso)
            actual_point[0]=Double.parseDouble(X_50per100_secondo);	actual_point[1]=Double.parseDouble(Y_50per100_secondo);
              prev_point[0]=Double.parseDouble(X_50per100_primo);     prev_point[1]=Double.parseDouble(Y_50per100_primo);
            afterTime=(tempo_dove_prevedere.getTimeInMillis()-tempo_50per100_secondo.getTimeInMillis())/1000;
            risultato=ARDA.predict_next_for(actual_point,prev_point,Macc,Mimp,grid_info,afterTime,maxBrakeValue);

            //Mostro la previsione che si ottiene come pesatura di tutti i punti dell'intorno
            for(int j=0;j<81;j++){
                previsionePesata[0]=previsionePesata[0]+risultato[j][0]*risultato[j][4];
                previsionePesata[1]=previsionePesata[1]+risultato[j][1]*risultato[j][4];
            }

			ArrayList<Double> previsione50= new ArrayList<Double>();
			previsione50.add(previsionePesata[0]);	previsione50.add(previsionePesata[1]);
            vettorePrevisioni_50.add(previsione50);

			ArrayList<Double> confronto50= new ArrayList<Double>();
            confronto50.add(Double.parseDouble(X_da_prevedere));	confronto50.add(Double.parseDouble(Y_da_prevedere));	confronto50.add(afterTime);
            vettoreDiConfronto_50.add(confronto50);

            vettorePrevisioni.add(previsione50);
			vettoreDiConfronto.add(confronto50);

            //eseguo la previsione 4 su 4 (dal 75% del percorso)
            actual_point[0]=Double.parseDouble(X_75per100_secondo);	actual_point[1]=Double.parseDouble(Y_75per100_secondo);
              prev_point[0]=Double.parseDouble(X_75per100_primo);	  prev_point[1]=Double.parseDouble(Y_75per100_primo);
            afterTime=(tempo_dove_prevedere.getTimeInMillis()-tempo_75per100_secondo.getTimeInMillis())/1000;
            risultato=ARDA.predict_next_for(actual_point,prev_point,Macc,Mimp,grid_info,afterTime,maxBrakeValue);

            //Mostro la previsione che si ottiene come pesatura di tutti i punti dell'intorno
            for(int j=0;j<81;j++){
                previsionePesata[0]=previsionePesata[0]+risultato[j][0]*risultato[j][4];
                previsionePesata[1]=previsionePesata[1]+risultato[j][1]*risultato[j][4];
            }

            ArrayList<Double> previsione75= new ArrayList<Double>();
			previsione75.add(previsionePesata[0]);	previsione75.add(previsionePesata[1]);
            vettorePrevisioni_75.add(previsione75);

            ArrayList<Double> confronto75= new ArrayList<Double>();
            confronto75.add(Double.parseDouble(X_da_prevedere));	confronto75.add(Double.parseDouble(Y_da_prevedere));	confronto75.add(afterTime);
            vettoreDiConfronto_75.add(confronto75);

            vettorePrevisioni.add(previsione75);
			vettoreDiConfronto.add(confronto75);

		}//for per tutti i percorsi trovati

		double[][] vettoreDeiRisultatiClusterizzato=new double[13][5000];
	    for(int i=0;i<13;i++){
	    	vettoreDeiRisultatiClusterizzato[i][0]=0;
	    }

		//if(vettoreDiConfronto.size()!=vettorePrevisioni.size())
		//	System.out.println("Errore nei vettori vettoreDiConfronto e vettorePrevisioni");

        for(int i=0;i<vettoreDiConfronto.size();i++){

			//System.out.println("\n-----> "+i+" di "+vettoreDiConfronto.size());

        	int nPunti;
        	//valuto di ricalcolare la distanza nel mio modo (quella sferica)
        	double errore_in_caselle=Math.pow(Math.pow(vettoreDiConfronto.get(i).get(0)-vettorePrevisioni.get(i).get(0),2)
        									  +Math.pow(vettoreDiConfronto.get(i).get(1)-vettorePrevisioni.get(i).get(1),2),0.5);

			/*
        	if(errore_in_caselle!=0){
				System.out.println(" (MV 6) errore_in_casella "+errore_in_caselle);
				System.out.println("("+vettoreDiConfronto.get(i).get(0)+" - "+vettorePrevisioni.get(i).get(0)+")^2 -");
				System.out.println("("+vettoreDiConfronto.get(i).get(1)+" - "+vettorePrevisioni.get(i).get(1)+")^2");
			}
			*/

	        //introduco la clusterizzazione per tempo
	        if(vettoreDiConfronto.get(i).get(2)>=0 && vettoreDiConfronto.get(i).get(2)<=1){
	        	nPunti=(int)vettoreDeiRisultatiClusterizzato[0][0];
	            vettoreDeiRisultatiClusterizzato[0][nPunti]=errore_in_caselle;
	            vettoreDeiRisultatiClusterizzato[0][0]++;
	        }
	        else{
	        	if(vettoreDiConfronto.get(i).get(2)>1 && vettoreDiConfronto.get(i).get(2)<=5){
	        		nPunti=(int)++vettoreDeiRisultatiClusterizzato[1][0];
		            vettoreDeiRisultatiClusterizzato[1][nPunti]=errore_in_caselle;
	        	}
	        	else{
	        		if(vettoreDiConfronto.get(i).get(2)>5 && vettoreDiConfronto.get(i).get(2)<=10){
		        		nPunti=(int)++vettoreDeiRisultatiClusterizzato[2][0];
			            vettoreDeiRisultatiClusterizzato[2][nPunti]=errore_in_caselle;
		        	}
	        		else{
	        			if(vettoreDiConfronto.get(i).get(2)>10 && vettoreDiConfronto.get(i).get(2)<=15){
	    	        		nPunti=(int)++vettoreDeiRisultatiClusterizzato[3][0];
	    		            vettoreDeiRisultatiClusterizzato[3][nPunti]=errore_in_caselle;
	    	        	}
	        			else{
	        				if(vettoreDiConfronto.get(i).get(2)>15 && vettoreDiConfronto.get(i).get(2)<=30){
	        	        		nPunti=(int)++vettoreDeiRisultatiClusterizzato[4][0];
	        		            vettoreDeiRisultatiClusterizzato[4][nPunti]=errore_in_caselle;
	        	        	}
	        				else{
	        					if(vettoreDiConfronto.get(i).get(2)>30 && vettoreDiConfronto.get(i).get(2)<=60){
	        		        		nPunti=(int)++vettoreDeiRisultatiClusterizzato[5][0];
	        			            vettoreDeiRisultatiClusterizzato[5][nPunti]=errore_in_caselle;
	        		        	}
	        					else{
	        						if(vettoreDiConfronto.get(i).get(2)>60 && vettoreDiConfronto.get(i).get(2)<=300){
	        			        		nPunti=(int)++vettoreDeiRisultatiClusterizzato[6][0];
	        				            vettoreDeiRisultatiClusterizzato[6][nPunti]=errore_in_caselle;
	        			        	}
	        						else{
	        							if(vettoreDiConfronto.get(i).get(2)>300 && vettoreDiConfronto.get(i).get(2)<=600){
	        				        		nPunti=(int)++vettoreDeiRisultatiClusterizzato[7][0];
	        					            vettoreDeiRisultatiClusterizzato[7][nPunti]=errore_in_caselle;
	        				        	}
	        							else{
	        								if(vettoreDiConfronto.get(i).get(2)>600 && vettoreDiConfronto.get(i).get(2)<=900){
	        					        		nPunti=(int)++vettoreDeiRisultatiClusterizzato[8][0];
	        						            vettoreDeiRisultatiClusterizzato[8][nPunti]=errore_in_caselle;
	        					        	}
	        								else{
	        									if(vettoreDiConfronto.get(i).get(2)>900 && vettoreDiConfronto.get(i).get(2)<=1800){
	        						        		nPunti=(int)++vettoreDeiRisultatiClusterizzato[9][0];
	        							            vettoreDeiRisultatiClusterizzato[9][nPunti]=errore_in_caselle;
	        						        	}
	        									else{
	        										if(vettoreDiConfronto.get(i).get(2)>1800 && vettoreDiConfronto.get(i).get(2)<=3600){
	        							        		nPunti=(int)++vettoreDeiRisultatiClusterizzato[10][0];
	        								            vettoreDeiRisultatiClusterizzato[10][nPunti]=errore_in_caselle;
	        							        	}
	        										else{
	        											if(vettoreDiConfronto.get(i).get(2)>3600 && vettoreDiConfronto.get(i).get(2)<=18000){
	        								        		nPunti=(int)++vettoreDeiRisultatiClusterizzato[11][0];
	        									            vettoreDeiRisultatiClusterizzato[11][nPunti]=errore_in_caselle;
	        								        	}
	        											else{
	        												if(vettoreDiConfronto.get(i).get(2)>18000){
	        									        		nPunti=(int)++vettoreDeiRisultatiClusterizzato[12][0];
	        										            vettoreDeiRisultatiClusterizzato[12][nPunti]=errore_in_caselle;
	        									        	}
	        											}}}}}}}}}}}}
				if(vettoreDiConfronto.get(i).get(2)<0)
					System.out.println(" (MV 7) numero negativo->"+vettoreDiConfronto.get(i).get(2));
			}

			/*
			System.out.print("Valori del vettore ");
			for(int i=0;i<13;i++)
				System.out.print(vettoreDeiRisultatiClusterizzato[i][0]+";");
			*/

			for(int j=0;j<vettoreDeiRisultatiClusterizzato.length;j++){
				int afferenze=(int)vettoreDeiRisultatiClusterizzato[j][0];
				int sommaTot=0;
				for(int k=1;k<=afferenze;k++){
					sommaTot+=vettoreDeiRisultatiClusterizzato[j][k];
				}

			//System.out.println();
			//System.out.println("sommaTot "+sommaTot+" afferenze"+afferenze+"\n");
			double mediaInCaselle=(float)sommaTot/(float)afferenze;
			double varianzaInCaselle=varianza(vettoreDeiRisultatiClusterizzato[j],mediaInCaselle);

			//Qui campia ed utilizza: (1) l'indice  (2) il numero  di iterazioni

			switch(j){
				case 0:
					//System.out.println("Media errore in caselle range [0s  1s]: "+ mediaInCaselle);
					//System.out.println( "Varianza errore in caselle range [0s  1s]: "+varianzaInCaselle);
					//System.out.println( "Numero previsioni afferenti: "+afferenze+"\n");
					s=s+"Media errore in caselle range ]0s  1s]: "+df.format(mediaInCaselle)+"\n";
					s=s+"Varianza errore in caselle range ]0s  1s]: "+df.format(varianzaInCaselle)+"\n";
					s=s+"Numero previsioni afferenti: "+afferenze+"\n\n";
					risTemp=risTemp+indice+";"+metriLato+";"+stringaDeltaTime+";"+"[0s  1s]"+";"+df.format(mediaInCaselle)+";"+df.format(varianzaInCaselle)+";"+afferenze+"\n"; break;
				case 1:
					//System.out.println( "Media errore in caselle range ]1s  5s]: "+mediaInCaselle);
					//System.out.println( "Varianza errore in caselle range ]1s  5s]: "+varianzaInCaselle);
					//System.out.println( "Numero previsioni afferenti: "+afferenze+"\n");
					s=s+"Media errore in caselle range ]1s  5s]: "+df.format(mediaInCaselle)+"\n";
					s=s+"Varianza errore in caselle range ]1s  5s]: "+df.format(varianzaInCaselle)+"\n";
					s=s+"Numero previsioni afferenti: "+afferenze+"\n\n";
					risTemp=risTemp+indice+";"+metriLato+";"+stringaDeltaTime+";"+"[1s  5s]"+";"+df.format(mediaInCaselle)+";"+df.format(varianzaInCaselle)+";"+afferenze+"\n"; break;
				case 2:
					//System.out.println( "Media errore in caselle range ]5s  10s]: "+mediaInCaselle);
					//System.out.println( "Varianza errore in caselle range ]5s  10s]: "+varianzaInCaselle);
					//System.out.println( "Numero previsioni afferenti: "+afferenze+"\n");
					s=s+"Media errore in caselle range ]5s  10s]: "+df.format(mediaInCaselle)+"\n";
					s=s+"Varianza errore in caselle range ]5s  10s]: "+df.format(varianzaInCaselle)+"\n";
					s=s+"Numero previsioni afferenti: "+afferenze+"\n\n";
					risTemp=risTemp+indice+";"+metriLato+";"+stringaDeltaTime+";"+"]5s  10s]"+";"+df.format(mediaInCaselle)+";"+df.format(varianzaInCaselle)+";"+afferenze+"\n"; break;
				case 3:
					//System.out.println( "Media errore in caselle range ]10s  15s]: "+mediaInCaselle);
					//System.out.println( "Varianza errore in caselle range ]10s  15s]: "+varianzaInCaselle);
					//System.out.println( "Numero previsioni afferenti: "+afferenze+"\n");
					s=s+"Media errore in caselle range ]10s  15s]: "+df.format(mediaInCaselle)+"\n";
					s=s+"Varianza errore in caselle range ]10s  15s]: "+df.format(varianzaInCaselle)+"\n";
					s=s+"Numero previsioni afferenti: "+afferenze+"\n\n";
					risTemp=risTemp+indice+";"+metriLato+";"+stringaDeltaTime+";"+"]10s  15s]"+";"+df.format(mediaInCaselle)+";"+df.format(varianzaInCaselle)+";"+afferenze+"\n"; break;
				case 4:
					//System.out.println( "Media errore in caselle range ]15s  30s]: "+mediaInCaselle);
					//System.out.println( "Varianza errore in caselle range ]15s  30s]: "+varianzaInCaselle);
					//System.out.println( "Numero previsioni afferenti: "+afferenze+"\n");
					s=s+"Media errore in caselle range ]15s  30s]: "+df.format(mediaInCaselle)+"\n";
					s=s+"Varianza errore in caselle range ]15s  30s]: "+df.format(varianzaInCaselle)+"\n";
					s=s+"Numero previsioni afferenti: "+afferenze+"\n\n";
					risTemp=risTemp+indice+";"+metriLato+";"+stringaDeltaTime+";"+"]15s  30s]"+";"+df.format(mediaInCaselle)+";"+df.format(varianzaInCaselle)+";"+afferenze+"\n"; break;
				case 5:
					//System.out.println( "Media errore in caselle range ]30s  1m]: "+mediaInCaselle);
					//System.out.println( "Varianza errore in caselle range ]30s  1m]: "+varianzaInCaselle);
					//System.out.println( "Numero previsioni afferenti: "+afferenze+"\n");
					s=s+"Media errore in caselle range ]30s  1m]: "+df.format(mediaInCaselle)+"\n";
					s=s+"Varianza errore in caselle range ]30s  1m]: "+df.format(varianzaInCaselle)+"\n";
					s=s+"Numero previsioni afferenti: "+afferenze+"\n\n";
					risTemp=risTemp+indice+";"+metriLato+";"+stringaDeltaTime+";"+"]30s  1m]"+";"+df.format(mediaInCaselle)+";"+df.format(varianzaInCaselle)+";"+afferenze+"\n"; break;
				case 6:
					//System.out.println( "Media errore in caselle range ]1m  5m]: "+mediaInCaselle);
					//System.out.println( "Varianza errore in caselle range ]1m  5m]: "+varianzaInCaselle);
					//System.out.println( "Numero previsioni afferenti: "+afferenze+"\n");
					s=s+"Media errore in caselle range ]1m  5m]: "+df.format(mediaInCaselle)+"\n";
					s=s+"Varianza errore in caselle range ]1m  5m]: "+df.format(varianzaInCaselle)+"\n";
					s=s+"Numero previsioni afferenti: "+afferenze+"\n\n";
					risTemp=risTemp+indice+";"+metriLato+";"+stringaDeltaTime+";"+"]1m  5m]"+";"+df.format(mediaInCaselle)+";"+df.format(varianzaInCaselle)+";"+afferenze+"\n"; break;
				case 7:
					//System.out.println( "Media errore in caselle range ]5m  10m]: "+mediaInCaselle);
					//System.out.println( "Varianza errore in caselle range ]5m  10m]: "+varianzaInCaselle);
					//System.out.println( "Numero previsioni afferenti: "+afferenze+"\n");
					s=s+"Media errore in caselle range ]5m  10m]: "+df.format(mediaInCaselle)+"\n";
					s=s+"Varianza errore in caselle range ]5m  10m]: "+df.format(varianzaInCaselle)+"\n";
					s=s+"Numero previsioni afferenti: "+afferenze+"\n\n";
					risTemp=risTemp+indice+";"+metriLato+";"+stringaDeltaTime+";"+"]5m  10m]"+";"+df.format(mediaInCaselle)+";"+df.format(varianzaInCaselle)+";"+afferenze+"\n"; break;
				case 8:
					//System.out.println( "Media errore in caselle range ]10m  15m]: "+mediaInCaselle);
					//System.out.println( "Varianza errore in caselle range ]10m  15m]: "+varianzaInCaselle);
					//System.out.println( "Numero previsioni afferenti: "+afferenze+"\n");
					s=s+"Media errore in caselle range ]10m  15m]: "+df.format(mediaInCaselle)+"\n";
					s=s+"Varianza errore in caselle range ]10m  15m]: "+df.format(varianzaInCaselle)+"\n";
					s=s+"Numero previsioni afferenti: "+afferenze+"\n\n";
					risTemp=risTemp+indice+";"+metriLato+";"+stringaDeltaTime+";"+"]10m  15m]"+";"+df.format(mediaInCaselle)+";"+df.format(varianzaInCaselle)+";"+afferenze+"\n"; break;
				case 9:
					//System.out.println( "Media errore in caselle range ]15m  30m]: "+mediaInCaselle);
					//System.out.println( "Varianza errore in caselle range ]15m  30m]: "+varianzaInCaselle);
					//System.out.println( "Numero previsioni afferenti: "+afferenze+"\n");
					s=s+"Media errore in caselle range ]15m  30m]: "+df.format(mediaInCaselle)+"\n";
					s=s+"Varianza errore in caselle range ]15m  30m]: "+df.format(varianzaInCaselle)+"\n";
					s=s+"Numero previsioni afferenti: "+afferenze+"\n\n";
					risTemp=risTemp+indice+";"+metriLato+";"+stringaDeltaTime+";"+"]15m  30m]"+";"+df.format(mediaInCaselle)+";"+df.format(varianzaInCaselle)+";"+afferenze+"\n"; break;
				case 10:
					//System.out.println( "Media errore in caselle range ]30m  1h]: "+mediaInCaselle);
					//System.out.println( "Varianza errore in caselle range ]30m  1h]: "+varianzaInCaselle);
					//System.out.println( "Numero previsioni afferenti: "+afferenze+"\n");
					s=s+"Media errore in caselle range ]30m  1h]: "+df.format(mediaInCaselle)+"\n";
					s=s+"Varianza errore in caselle range ]30m  1h]: "+df.format(varianzaInCaselle)+"\n";
					s=s+"Numero previsioni afferenti: "+afferenze+"\n\n";
					risTemp=risTemp+indice+";"+metriLato+";"+stringaDeltaTime+";"+"]30m  1h]"+";"+df.format(mediaInCaselle)+";"+df.format(varianzaInCaselle)+";"+afferenze+"\n"; break;
				case 11:
					//System.out.println( "Media errore in caselle range ]1h  5h]: "+mediaInCaselle);
					//System.out.println( "Varianza errore in caselle range ]1h  5h]: "+varianzaInCaselle);
					//System.out.println( "Numero previsioni afferenti: "+afferenze+"\n");
					s=s+"Media errore in caselle range ]1h  5h]: "+df.format(mediaInCaselle)+"\n";
					s=s+"Varianza errore in caselle range ]1h  5h]: "+df.format(varianzaInCaselle)+"\n";
					s=s+"Numero previsioni afferenti: "+afferenze+"\n\n";
					risTemp=risTemp+indice+";"+metriLato+";"+stringaDeltaTime+";"+"]1h  5h]"+";"+df.format(mediaInCaselle)+";"+df.format(varianzaInCaselle)+";"+afferenze+"\n"; break;
				case 12:
					//System.out.println( "Media errore in caselle range ]5h  inf]: "+mediaInCaselle);
					//System.out.println( "Varianza errore in caselle range ]5h  inf]: "+varianzaInCaselle);
					//System.out.println( "Numero previsioni afferenti: "+afferenze+"\n");
					s=s+"Media errore in caselle range ]5h  inf]: "+df.format(mediaInCaselle)+"\n";
					s=s+"Varianza errore in caselle range ]5h  inf]: "+df.format(varianzaInCaselle)+"\n";
					s=s+"Numero previsioni afferenti: "+afferenze+"\n\n";
					risTemp=risTemp+indice+";"+metriLato+";"+stringaDeltaTime+";"+"]5h  inf]"+";"+df.format(mediaInCaselle)+";"+df.format(varianzaInCaselle)+";"+afferenze+"\n"; break;
				default:
					//errore
					System.out.println("(MV 8) errore nel select case");
			}//fine SC
		}//fine for

		//############################################################
		//#      Ora mi occupo della rilevazione delle percentuali   #
		//############################################################


		double[] vettoreTempErrori=new double[vettorePercorsi.size()];
		double distanza,sommaDist=0;
		for(int i=0;i<vettorePrevisioni_0.size();i++){
			distanza=Math.pow(Math.pow(vettoreDiConfronto_0.get(i).get(0)-vettorePrevisioni_0.get(i).get(0),2)
					+Math.pow(vettoreDiConfronto_0.get(i).get(1)-vettorePrevisioni_0.get(i).get(1),2),0.5);
			vettoreTempErrori[i]=distanza;
			sommaDist+=distanza;
		}

		double mediaErroreTemp=(float)sommaDist/(float)vettorePrevisioni_0.size();
		double varianzaErroreTemp=varianza(vettoreTempErrori,mediaErroreTemp);
		risPerc+= indice+";"+metriLato+";"+stringaDeltaTime+";0%;"+
				  df.format(mediaErroreTemp)+";"+df.format(varianzaErroreTemp)+";"+vettorePrevisioni_0.size()+
				  df.format(metriLato*mediaErroreTemp)+";"+df.format(metriLato*varianzaErroreTemp)+";"+df.format(Math.pow(metriLato*varianzaErroreTemp,0.5))+"\n";

		for(int i=1;i<vettorePrevisioni_25.size();i++){
			distanza=Math.pow(Math.pow(vettoreDiConfronto_25.get(i).get(0)-vettorePrevisioni_25.get(i).get(0),2)
					+Math.pow(vettoreDiConfronto_25.get(i).get(1)-vettorePrevisioni_25.get(i).get(1),2),0.5);
			vettoreTempErrori[i]=distanza;
			sommaDist+=distanza;
		}

		mediaErroreTemp=(float)sommaDist/(float)vettorePrevisioni_25.size();
		varianzaErroreTemp=varianza(vettoreTempErrori,mediaErroreTemp);
		risPerc+= indice+";"+metriLato+";"+stringaDeltaTime+";25%;"+
				  df.format(mediaErroreTemp)+";"+df.format(varianzaErroreTemp)+";"+vettorePrevisioni_25.size()+
				  df.format(metriLato*mediaErroreTemp)+";"+df.format(metriLato*varianzaErroreTemp)+";"+df.format(Math.pow(metriLato*varianzaErroreTemp,0.5))+"\n";


		for(int i=0;i<vettorePrevisioni_50.size();i++){
			distanza=Math.pow(Math.pow(vettoreDiConfronto_50.get(i).get(0)-vettorePrevisioni_50.get(i).get(0),2)
					+Math.pow(vettoreDiConfronto_50.get(i).get(1)-vettorePrevisioni_50.get(i).get(1),2),0.5);
			vettoreTempErrori[i]=distanza;
			sommaDist+=distanza;
        }

		mediaErroreTemp=(float)sommaDist/(float)vettorePrevisioni_50.size();
		varianzaErroreTemp=varianza(vettoreTempErrori,mediaErroreTemp);
		risPerc+= indice+";"+metriLato+";"+stringaDeltaTime+";50%;"+
				  df.format(mediaErroreTemp)+";"+df.format(varianzaErroreTemp)+";"+vettorePrevisioni_50.size()+
				  df.format(metriLato*mediaErroreTemp)+";"+df.format(metriLato*varianzaErroreTemp)+";"+df.format(Math.pow(metriLato*varianzaErroreTemp,0.5))+"\n";

		for(int i=0;i<vettoreDiConfronto_75.size();i++){
			distanza=Math.pow(Math.pow(vettoreDiConfronto_75.get(i).get(0)-vettorePrevisioni_75.get(i).get(0),2)
					+Math.pow(vettoreDiConfronto_75.get(i).get(1)-vettorePrevisioni_75.get(i).get(1),2),0.5);
			vettoreTempErrori[i]=distanza;
			sommaDist+=distanza;
		}

		mediaErroreTemp=(float)sommaDist/(float)vettoreDiConfronto_75.size();
		varianzaErroreTemp=varianza(vettoreTempErrori,mediaErroreTemp);
		risPerc+= indice+";"+metriLato+";"+stringaDeltaTime+";75%;"+
				  df.format(mediaErroreTemp)+";"+df.format(varianzaErroreTemp)+";"+vettoreDiConfronto_75.size()+
				  df.format(metriLato*mediaErroreTemp)+";"+df.format(metriLato*varianzaErroreTemp)+";"+df.format(Math.pow(metriLato*varianzaErroreTemp,0.5))+"\n";

		String[] risultato=new String[2];
		risultato[0]=risTemp;	risultato[1]=risPerc;

		return risultato;

	}//esecuzionetest

	public double varianza(double[] array,double media){
		int valori=array.length;
		double somma=0,risultato;
		for(int i=0;i<valori;i++){
			somma+=Math.pow((array[i]-media),2);
		}
		risultato=(float)somma/(float)valori;
		return risultato;
	}

}//class
