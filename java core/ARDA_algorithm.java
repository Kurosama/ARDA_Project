public class ARDA_algorithm {

    public void ARDA_algorithm(){}


    //Crea un array con la coppia di coordinate
    public double[] get_position(int x,int y){
    	double [] pos=new double[2];
    	pos[0]=x; pos[1]=y;
    	return pos;
    }

    //Crea una lista con dei valori uniformemente distribuiti
    public double[][] get_points_array(double x, double y){
    	double[][] points_array=new double[81][2];
    	for(int i=0;i<9;i++){
    		for(int j=0;j<9;j++){
    			points_array[(i*9)+j][0]= x+((float)(j-4)/(float)100);
    			points_array[(i*9)+j][1]= y+((float)(i-4)/(float)100);
    		}
    	}
    	return points_array;
    }

    //Copia dentro una lista le coppie di valori listaTriple
    public double[][] get_points_array2(double[][] listaTriple){
		double[][] points_array=new double[81][2];
    	for(int i=0;i<81;i++){
			points_array[i][0]= listaTriple[i][0];
			points_array[i][1]= listaTriple[i][1];
		}
    	return points_array;
	}

    //Crea una lista di probabilita usando la distribuzione binomiale
    public double[] get_points_prob_dist_array(){
    	double[] points_array=new double[81];
    	double[] binom_dist=new double[9];
    	binom_dist[0]=0.010; binom_dist[1]=0.044; binom_dist[2]=0.118; binom_dist[3]=0.205;
    	binom_dist[4]=0.246; binom_dist[5]=0.205; binom_dist[6]=0.118; binom_dist[7]=0.044; binom_dist[8]=0.010;
    	for(int i=0;i<9;i++){
    		for(int j=0;j<9;j++){
    			points_array[(i*9)+j]=binom_dist[i]*binom_dist[j];
    		}
    	}
    	return points_array;
    }

    //Restituisce il valore di accelerazione della relativa matrice
    public double[] get_acc_value_in_point(double[] pos,double[][][] grid,int[] grid_info){
    	int x_loc=(int)(Math.floor(pos[0]));
    	int y_loc=(int)(Math.floor(pos[1]));
    	if(x_loc>=0 && x_loc<grid_info[0] && y_loc>=0 && y_loc<grid_info[1]){
    		return grid[x_loc][y_loc];
    	}
    	else{
    		//System.out.println("----> Errore: Coordinate matrice di accelerazione fuori range");
    		pos[0]=0; pos[1]=0;
    		return pos;
    	}
    }

    //restituisce il valore di frenata della relativa matrice
    public double get_brake_value_in_point(double[] pos,double[][] grid,int[] grid_info){
    	int x_loc=(int)(Math.floor(pos[0]));
    	int y_loc=(int)(Math.floor(pos[1]));
    	if(x_loc>=0 && x_loc<grid_info[0] && y_loc>=0 && y_loc<grid_info[1]){
    		return grid[x_loc][y_loc];
    	}
    	else{
    		//System.out.println("----> Errore: Coordinate matrice di frenata fuori range");
    		return 0;
    	}
    }

    //Calcola la prossima posizione dell'oggetto
    public double[] get_next(double[] actual_point,double[] prev_point, double[] acceleration, double sampling_time, double friction){
    	double[] next_point=new double[2];
    	next_point[0]=((2-friction)*actual_point[0])-((1-friction)*prev_point[0])+(acceleration[0]*sampling_time*sampling_time);
    	next_point[1]=((2-friction)*actual_point[1])-((1-friction)*prev_point[1])+(acceleration[1]*sampling_time*sampling_time);
    	return next_point;
    }


    /* 	Stima della prossima posizione dopo "after_time" secondi per l'utente
     *
     *	"actual_point"   	[2]	: Posizione attuale dell'oggetto
     *	"prev_point"	 	[2] : Posizione precedente dell'oggetto
     *	"acc_info" 	  [n][m][2] : Matrice contenente le informazioni sull'accelerazione data all'oggetto dalla zona di importanza
     *	"brake_info"	  [][2] : Matrice contenente le informazioni sulla frenata (attrito) data dalla zona di importanza
     *	"grid_info"    	  [][2] : informazioni sulla matrice
     *	"after_time"    	  d	: Tempo prossima previsione
     *	"brake_max" 		  d : Valore massimo di frenta
     */

     public double[][] single_object_prediction(double[] actual_point,double[] prev_point, double [][][] acc_info, double [][] brake_info, int[] grid_info, double after_time, double brake_max){
     	int T_campione=1;
     	int T_corrente=0;

     	double[] accelerazione=new double[2];
     	double frenata,attrito;
     	double[] nuovo_p=new double[2];
     	double[][] result=new double[2][2];

     	//Ricalcolo la posizione finche non raggiungo il tempo after_time

     	while(T_corrente< after_time){
     		T_corrente+=T_campione;			//incremento il contatore
     		accelerazione= get_acc_value_in_point(actual_point,acc_info,grid_info);	//Reperisco i dati da Macc[][][]
     		frenata=get_brake_value_in_point(actual_point,brake_info,grid_info);	//Reperisco i dati da Mimp[][]
     		attrito=(((float)frenata)/((float)brake_max)*0.085)+0.015;
     		nuovo_p=get_next(actual_point,prev_point,accelerazione,T_campione,attrito); //Calcolo la nuova posizione
     		prev_point=actual_point;
     		actual_point=nuovo_p;
     	}
     	result[0]=actual_point;
     	result[1]=prev_point;
     	return result;
	}//single_object_prediction

	public double[][] predict_next_for(double[] actual_point, double[] prev_point,double [][][] acc_info,double[][] brake_info,int[] grid_info, double after_time, double brake_max){

		/*-Creare l'insieme di oggetti che descrivono la distribuzione di probabilità della posizione attuale e precedente dell'utente.
		 *-Distribuzione binomiale della posizione corrente.
		 */

		double[] points_prob_dist_array=new double[81];
		double[][] points_array=new double[81][2];
		//invece di usare 2 matrici separate per i risulati amplio di 2 posti tendo conto che il posto 2 e 3 sono i punti precedenti
		double[][] getted_results=new double[81][5];

		points_prob_dist_array=get_points_prob_dist_array();
		points_array=get_points_array(actual_point[0],actual_point[1]);

		double[] element=new double[2];
		double[][] element_result=new double[2][2];
		double actual_prob_dist;

		for(int i=0;i<81;i++){
			element=points_array[i];
			element_result=single_object_prediction(element,prev_point,acc_info,brake_info,grid_info,after_time,brake_max);
			actual_prob_dist=points_prob_dist_array[i];
			getted_results[i][0]=element_result[0][0];
			getted_results[i][1]=element_result[0][1];
			getted_results[i][2]=element_result[1][0];
			getted_results[i][3]=element_result[1][1];
			getted_results[i][4]=actual_prob_dist;
		}
    	return getted_results;

	}//predict_next_for


    public double[][] predict_next_for(double[][] TriplaUltimaPrevisione, double[][] TriplaPenultimaPrevisione,double [][][] acc_info,double[][] brake_info,int[] grid_info, double after_time, double brake_max){

		double[] points_prob_dist_array=new double[81];
		double[][] points_array=new double[81][2];
		double[][] prev_points_array=new double[81][2];
		//invece di usare 2 matrici separate per i risulati amplio di 2 posti tendo conto che il posto 2 e 3 sono i punti precedenti
		double[][] getted_results=new double[81][5];

		points_prob_dist_array=get_points_prob_dist_array();
		points_array=get_points_array2(TriplaUltimaPrevisione);
		prev_points_array=get_points_array2(TriplaPenultimaPrevisione);

		double[] element=new double[2];
		double[] prev_element=new double[2];
		double[][] element_result=new double[2][2];
		double actual_prob_dist;
		for(int i=0;i<9;i++){
			element=points_array[i];
			prev_element=prev_points_array[i];
			element_result=single_object_prediction(element,prev_element,acc_info,brake_info,grid_info,after_time,brake_max);
			actual_prob_dist=points_prob_dist_array[i];
			getted_results[i][0]=element_result[0][0];
			getted_results[i][1]=element_result[0][1];
			getted_results[i][2]=element_result[1][0];
			getted_results[i][3]=element_result[1][1];
			getted_results[i][4]=actual_prob_dist;
    	}
    	return getted_results;
    }//predict_next_for2

}
