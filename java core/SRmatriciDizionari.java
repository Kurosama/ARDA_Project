import java.io.*;
import java.util.Map;
import java.util.HashMap;

public class SRmatriciDizionari{

	private int n,m,maxVisite;
	private double GradiLatoY,GradiLatoX,Xmax,Ymax,Xmin,Ymin;

	private double[][] Mimp;
	private double[][] Mpot;
	private double[][] Mvisite;
	private double[][][] Macc;

	private String hashkey;
	private Map<String, Float> DizA;
	private Map<String, Float> DizSR;

	//Trovo il giusto peso da porre in B[a][b][c][d] per i punti adiacenti (a,b)(c,d)
	public double PesaB(int a, int b, int n, int m){
		if(a==0 && b==0) return (float)1/(float)4;
		if(a==0 && b==(m-1)) return (float)1/(float)4;
		if(a==(n-1) && b==0) return (float)1/(float)4;
		if(a==(n-1) && b==(m-1)) return (float)1/(float)4;
		if(a==0) return (float)1/(float)6;
		if(a==(n-1)) return (float)1/(float)6;
		if(b==0) return (float)1/(float)6;
		if(b==(m-1)) return (float)1/(float)6;
		return (float)1/(float)9;
	}//PesaB

	public Map IncrHash(Map<String, Float> mapLoc, int a, int b, int c, int d,float value){
		hashkey = "("+a+","+b+")("+c+","+d+")";
		if(mapLoc.containsValue(hashkey)){
			mapLoc.replace(hashkey,mapLoc.get(hashkey)+value);
		}else{
			mapLoc.put(hashkey,value);
		}
		return mapLoc;
	}

	public float GetHash(Map<String, Float> mapLoc, int a, int b, int c, int d){
		hashkey = "("+a+","+b+")("+c+","+d+")";
		if(mapLoc.containsValue(hashkey)){
			return mapLoc.get(hashkey);
		}else{
			return (float)0;
		}
	}

	public Map SetHash(Map<String, Float> mapLoc, int a, int b, int c, int d,float value){
		hashkey = "("+a+","+b+")("+c+","+d+")";
		if(mapLoc.containsValue(hashkey)){
			mapLoc.replace(hashkey,value);
		}else{
			mapLoc.put(hashkey,(float)0);
		}
		return mapLoc;
	}

	public SRmatriciDizionari(String PATH, int metrilato, int numeroiterazioniSRtoMimp){

		// pathFile= percorso dove trovare il file che contiene i punti (i percorsi) raccolti dal ricevitore GPS
		// precisioneGradi= indica la precisione con la quale andare ad analizzare i dati
		// cmd da eseguire= SetupStructuresSR('atlanta3.txt',????)
		// ATTENZIONE: durata esecuzione 26 minuti per costruire il modello

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

		/* leggo dal file tutte le coordinate e cerco i punti con
		 * coordinate minime (minX,minY) e massime (maxX,maxY) e li
		 * uso per determinare la grandezza delle matrici(n,m):
		 */
		double y,x;
		Ymax=-1000.0;	Xmax=-1000.0;
		Ymin=1000.0;	Xmin=1000.0;

		double startIniziale=(System.nanoTime()/(float)1000000000);

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
		m=(int)((Ymax-Ymin)/GradiLatoY)+1;  //altezza   matrice

		System.out.println("N:"+n+" M:"+m);

		int gg2=172800;
		int min5=300;

		// Definisco i dizionari di A mentre calcolo la somma per righe e li inizializzo a 0
		DizA  = new HashMap();
	   	DizSR = new HashMap();

		double[][] DizA_sommaRighe=new double[n][m];
		double[][] Mvisite=new double[n][m];
		double[][] Mimp=new double[n][m];
		double[][] Mpot=new double[n][m];

		for(int i=0;i<n;i++){
			for(int j=0;j<m;j++){
				DizA_sommaRighe[i][j]=0;
				Mvisite[i][j]=0;
			}
		}

		System.out.println("Read file");
		try{
			FileInputStream fstream = new FileInputStream(PATH);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			int day,month,year,hour,minute,second;
			int Pday,Pmonth,Pyear,Phour,Pminute,Psecond;
			Pday=1; Pmonth=1; Pyear=2000; Phour=0; Pminute=0; Psecond=0;

			int x_zona_prec=-1, y_zona_prec=-1;
			double x_prec=0.0, y_prec=0.0;
			int sec_nuova_entry=10;
			int numero=0,y_zona,x_zona;

			while ((strLine = br.readLine()) != null){
				//Estraggo i vari dati dalle righe
				     y=Double.parseDouble(strLine.substring(0,10));
				     x=Double.parseDouble(strLine.substring(11,21));
				   day=Integer.parseInt(strLine.substring(22,24));
				 month=Integer.parseInt(strLine.substring(25,27));
				  year=Integer.parseInt(strLine.substring(28,32));
				  hour=Integer.parseInt(strLine.substring(33,35));
				minute=Integer.parseInt(strLine.substring(36,38));
				second=Integer.parseInt(strLine.substring(39,41));

				//calcolo della differenza in secondi
				int diff_sec=second-Psecond;
				int diff_gg=(day-Pday)*86400;
				int diff_in_sec=diff_sec+(minute-Pminute)*60+(hour-Phour)*3600+diff_gg;
				//calcolo della zona in qui risiede la posizione valutata
				x_zona=(int)((x-Xmin)/GradiLatoX);
				y_zona=(int)((y-Ymin)/GradiLatoY);

				Mvisite[x_zona][y_zona]++;

        		if(numero==0)
					numero=1;
				else{
					if(diff_in_sec<=min5){
						DizA = IncrHash(DizA,x_zona_prec,y_zona_prec,x_zona,y_zona,1);
						DizA_sommaRighe[x_zona_prec][y_zona_prec]+=1;
					}
					else{
						if(diff_in_sec>min5 && diff_in_sec<gg2){
							//int permanenza=(int)( diff_in_sec/sec_nuova_entry);
							int permanenza=(int)((float)(diff_gg+diff_sec)/(float)sec_nuova_entry);
							DizA = IncrHash(DizA,x_zona_prec,y_zona_prec,x_zona,y_zona,1);
							DizA = IncrHash(DizA,x_zona,y_zona,x_zona,y_zona,(permanenza-1));
							DizA_sommaRighe[x_zona_prec][y_zona_prec]+=1;
							DizA_sommaRighe[x_zona][y_zona]+=(permanenza-1);
						}
					}
				}

				Pday=day;	Pmonth=month;	Pyear=year;
				Phour=hour;	Pminute=minute;	Psecond=second;
				x_zona_prec=x_zona;		y_zona_prec=y_zona;
				x_prec=x;	y_prec=y;
			}

			in.close();
		}catch (Exception e){
			System.err.println("Errore: " + e.getMessage());
		}
		System.out.println("End Read file");
		// normalizzazione di A con moltiplicazione per il futuro merge con B
		double mergerD=0.15;

		System.out.println("Calculate DizA");
		for(int i=0;i<n;i++){
			for(int j=0;j<m;j++){
				if(DizA_sommaRighe[i][j]!=0){
					for(int z=0;z<n;z++){
						for(int w=0;w<m;w++){
							System.out.println("("+i+")("+j+"),("+z+")("+w+")");
							DizA = SetHash(DizA,i,j,z,w,
								(GetHash(DizA,i,j,z,w)/(float)DizA_sommaRighe[i][j])*(float)(1-mergerD)
							);
						}
					}
				}
			}
		}

		//======================================================================

		/*	Costruzione di SR facendo attenzione prima a curare B pesata,
		 *	poi a mettere A pesata:
		 * 	Questa parte è abbastanza lenta [...]
		 */
		System.out.println("Calculate DizSR");
		for(int a=0;a<n;a++){
			for(int b=0;b<m;b++){
				for(int c=Math.max(0,(a-1));c<Math.min(n,(a+1));c++){
					for(int d=Math.max(0,(b-1));d<Math.min(m,(b+1));d++){
						DizSR = SetHash(DizSR,a,b,c,d,(float)(PesaB(a,b,n,m)*mergerD));
					}
				}
			}
		}

		// Inserisco la parte relativa alla matrice A normalizzata
		System.out.println("Normalize DizSR");
		for(int a=0;a<n;a++){
			for(int b=0;b<m;b++){
				if(DizA_sommaRighe[a][b]!=0){
					for(int c=0;c<n;c++){
						for(int d=0;d<m;d++){
							DizSR = IncrHash(DizSR,a,b,c,d,GetHash(DizA,a,b,c,d));
						}
					}
				}
			}
		}

//======================================================================

		//Calcolo matrice Mimp
		double[] pagerank_step=new double[(n*m)];
		for(int i=0;i<(n*m);i++)
			pagerank_step[i]=1;

		double stopIniziale=(System.nanoTime()/(float)1000000000);
		System.out.printf("Tempo preparazione iniziale= %5.3f secondi\n",(stopIniziale-startIniziale));
		System.out.printf("Tempo preparazione iniziale= %5.3f minuti\n\n\n",((stopIniziale-startIniziale)/60));

		double time0,time1,time2,time3,time4,time5;

		numeroiterazioniSRtoMimp=1;
		for(int numero=0;numero<numeroiterazioniSRtoMimp;numero++){
			time0=(System.nanoTime()/(float)1000000000);
			double[] new_pagerank_step=new double[(n*m)];
			for(int i=0;i<(n*m);i++)
				new_pagerank_step[i]=0;

			for(int a=0;a<n;a++){
				for(int b=0;b<m;b++){
					for(int c=0;c<n;c++){
						for(int d=0;d<m;d++){
							if(GetHash(DizSR,a,b,c,d)!=0){
								new_pagerank_step[(c*m)+d]+=(GetHash(DizSR,a,b,c,d)*pagerank_step[(c*m)+d]);
							}
						}
					}
				}
			}

			time2=System.nanoTime()/(float)1000000000;
			if(numero==0){
				System.out.printf("Tempo necessario per visionare il dizionario= %5.3f secondi\n",(time2-time0));
				System.out.printf("Tempo necessario per visionare il dizionario= %5.3f minuti\n\n\n",((time2-time0)/60));
			}

			double norma=0;
			for(int i=0;i<(n*m);i++)
				norma+=Math.pow(new_pagerank_step[i],2);

			norma=Math.sqrt(norma);

			time3=System.nanoTime()/(float)1000000000;
			if(numero==0){
				System.out.printf("Tempo necessario per calcolare la norma= %5.3f secondi\n",(time3-time2));
				System.out.printf("Tempo necessario per calcolare la norma= %5.3f minuti\n\n\n",((time3-time2)/60));
			}

			for(int i=0;i<(n*m);i++)
				new_pagerank_step[i]=new_pagerank_step[i]/(float)norma;

			time4=System.nanoTime()/(float)1000000000;
			if(numero==0){
				System.out.printf("Tempo necessario per dividere il vettore per la norma= %5.3f secondi\n",(time4-time3));
				System.out.printf("Tempo necessario per dividere il vettore per la norma= %5.3f minuti\n\n\n",((time4-time3)/60));
			}

			for(int i=0;i<(n*m);i++)
				pagerank_step[i]=new_pagerank_step[i];

			time5=System.nanoTime()/(float)1000000000;
			if(numero==0){
				System.out.printf("Tempo necessario copiare il vettore temporaneo= %5.3f secondi\n",(time5-time4));
				System.out.printf("Tempo necessario copiare il vettore temporaneo= %5.3f minuti\n\n\n",((time5-time4)/60));

				System.out.printf("Tempo necessario per fare un iterazione= %5.3f secondi\n",(time5-time0));
				System.out.printf("Tempo necessario per fare un iterazione= %5.3f minuti\n\n\n",((time5-time0)/60));
				System.out.println("FINALE:");
				System.out.printf("Tempo previsto per eseguire tutte le iterazioni= %5.3f minuti\n",(numeroiterazioniSRtoMimp*(time5-time0)/60));
				System.out.printf("Tempo previsto per eseguire tutte le iterazioni= %5.3f ore\n ",(numeroiterazioniSRtoMimp*(time5-time0)/3600));
				System.out.printf("Tempo previsto per eseguire tutte le iterazioni= %5.3f giorni\n\n\n",(numeroiterazioniSRtoMimp*(time5-time0)/86400));
			}

		}//for

		for(int i=0;i<n;i++){
			for(int j=0;j<m;j++){
				if(maxVisite<Mvisite[i][j])
					maxVisite=(int)Mvisite[i][j];
			}
		}

		for(int i=0;i<n;i++)
			for(int j=0;j<m;j++)
				Mimp[i][j]=pagerank_step[(i*m)+j];

		// "costruzione" matrice Mpot
		for(int i=0;i<n;i++)
			for(int j=0;j<m;j++)
				Mpot[i][j]=-Mimp[i][j];

		CalcoloMacc calcoloMacc;
		calcoloMacc=new CalcoloMacc(PATH,metrilato,Mpot,n,m,Xmax,Ymax,Xmin,Ymin,GradiLatoX,GradiLatoY,maxVisite);

		//da qui posso caricare tutte le matrici da calcoloMacc

		Macc=new double[n][m][2];
		Macc=calcoloMacc.get_Macc();

		double stopFinale=System.nanoTime()/(float)1000000000;
		System.out.printf("Tempo totale di esecuzione= %5.3f secondi\n",(stopFinale-startIniziale));
		System.out.printf("Tempo totale di esecuzione= %5.3f minuti\n\n\n",((stopFinale-startIniziale)/60));

    //qui di seguito mette una funzione da copia-incolla per stampare Macc in matlab

	}//SRmatriciDizionari(...)

	public int get_n(){return n;}
	public int get_m(){return m;}
	public int get_maxVisite(){return maxVisite;}

	public double[][] get_Mimp(){return Mimp;}
	public double[][] get_Mpot(){return Mpot;}

	public double[][][] get_Macc(){return Macc;}

}//end
