import java.io.*;

public class Tuttofare {
	
    public static void main(String[] args) {
		//MatricePersonalizzata(10,10,0,20,4,500,"NV");
		//Stefano=0, Atlanta=1
		ConvertiFile("prova.txt",1);
    }
    
	/*
	*	INFO FLAG indice:
	*	indice=='NV'  ->  calcolo Mimp usando indice Number Of Visits
	*	indice=='TT'  ->  calcolo Mimp usando indice Total Time
	*	indice=='AT'  ->  calcolo Mimp usando indice Average Time
	*	indice=='CL'  ->  calcolo Mimp usando indice Combinazione Lineare NoV e TT
	*/
    
    
    
    public static void MatricePersonalizzata(int n,int m,int min,int max,int spar,int metrilato,String indice){
		double[][] matrice=new double[n][m];	
		
		min=(int)(min-max*((float)(11-spar)/(float)10));
		System.out.println(min+"  ");
		// popolo la matrice
		for(int i=0;i<n;i++){
			for(int j=0;j<m;j++){
				int valore=min+(int)(Math.random()*max);
				if(valore>=0)
					matrice[i][j]=valore;
				else
					matrice[i][j]=0;
			}
		}
		
		for(int i=0;i<n;i++){
			for(int j=0;j<m;j++){
				System.out.print(matrice[i][j]+"\t");
			}
			System.out.println();
		}
		
		double GradiLatoX=0,GradiLatoY=0;
		switch (metrilato) {
			case 50:  GradiLatoX=0.000535;		GradiLatoY=0.00045;		break;
			case 100: GradiLatoX=0.001077;		GradiLatoY=0.0009;		break;
			case 300: GradiLatoX=0.001077*3;	GradiLatoY=0.0009*3;	break;
			case 500: GradiLatoX=0.00535;		GradiLatoY=0.0045;		break;
			case 1000:GradiLatoX=0.01077;		GradiLatoY=0.009;		break;
			case 3000:GradiLatoX=0.01077*3;		GradiLatoY=0.009*3;		break;
			default: break;
		}
		
		try{
			FileOutputStream file = new FileOutputStream("file.txt");
			PrintStream Output = new PrintStream(file);
			int gg=1,mm=1,aa=2000,ore=0,minuti=0,secondi=0,riga=0;
			double x=0,y=0;
			for(int z=0;z<max;z++){
				for(int i=0;i<n;i++){
					for(int j=0;j<m;j++){
						if(matrice[i][j]!=0){
							x=GradiLatoX*i;
							y=GradiLatoY*j;
							//  00000000001111111111222222222233333333334
							//  01234567890123456789012345678901234567890
							//||0033.93885|-084.33697|09/25/2001|11:09:00||
							if(riga==1)
								Output.println();
							else
								riga=1;
							String s = String.format("%010.5f|%010.5f|%02d/%02d/%04d|%02d:%02d:%02d",y,x,gg,mm,aa,ore,minuti,secondi);
							s = ""+s.substring(0,4)+"."+s.substring(5,15)+"."+s.substring(16,39);
							Output.print(s);
							if(++secondi>59){secondi=0; minuti++;}
							minuti+=4;
							if(minuti>59){minuti-=60; ore++;}
							if(ore>24){ore=0; gg++;}
							if(gg>31){gg=1; mm++;}
							if(mm>12){mm=1; aa++;}
							matrice[i][j]--;
						}
					}
				}
			}
		}catch (Exception e) {
		  System.out.println("Errore: " + e.getMessage());
		}
		
	}
	
	public static void ConvertiFile(String PATH,int value){
		String newFile=""+PATH.substring(0,PATH.length()-4)+"(1)"+PATH.substring(PATH.length()-4,PATH.length());
		System.out.println("Lettura file "+PATH+"e inizio scrittura file "+newFile);
		try{
			//File in lettura
		    FileInputStream fstream = new FileInputStream(PATH);
		    DataInputStream in = new DataInputStream(fstream);
		    BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    
		    //File in scrittura
		    FileOutputStream file = new FileOutputStream(newFile);
			PrintStream Output = new PrintStream(file);
		    String strLine;
		    
		    int riga=0;
		    int contatore=0;
		    int gg=1,mm=1,aa=0,hour=0,minute=0,second=0;
			double x=0,y=0;
		    while ((strLine = br.readLine()) != null){
				riga++;
				if(value==0){
					//stefano
					y=Double.parseDouble(strLine.substring(20,29));
					x=Double.parseDouble(strLine.substring(30,39));
					gg=Integer.parseInt(strLine.substring(0,2));
					mm=Integer.parseInt(strLine.substring(3,5));
					aa=Integer.parseInt(strLine.substring(6,10));
					hour=Integer.parseInt(strLine.substring(11,13));
					minute=Integer.parseInt(strLine.substring(14,16));
					second=Integer.parseInt(strLine.substring(17,19));
				}
				if(value==1){
					//atlanta
					y=Double.parseDouble(strLine.substring(0,8));
					x=Double.parseDouble(strLine.substring(10,19));
					gg=Integer.parseInt(strLine.substring(24,26));
					mm=Integer.parseInt(strLine.substring(21,23));
					aa=Integer.parseInt("20"+strLine.substring(27,29));
					hour=Integer.parseInt(strLine.substring(31,33));
					minute=Integer.parseInt(strLine.substring(34,36));
					second=Integer.parseInt(strLine.substring(37,39));
				}
            	if(contatore==(riga%500)){
					contatore=riga%10;
					System.out.print("[]");
				}
				if(riga>1)
					Output.println();
				else
					riga=1;
				String s = String.format("%010.5f|%010.5f|%02d/%02d/%04d|%02d:%02d:%02d",y,x,gg,mm,aa,hour,minute,second);
				s = ""+s.substring(0,4)+"."+s.substring(5,15)+"."+s.substring(16,41);
				Output.print(s);
		    }
		in.close();
		System.out.println("\nOPERAZIONE COMPLETATA");
		System.out.println("La traduzione delle "+riga+" righe ha avuto successo");
		}catch (Exception e){
		    System.err.println("Errore: " + e.getMessage());
		}
		
	}
}
