public class Main {
	public static void main(String[] args) {
		//CalcoloMacc  prova=new CalcoloMacc("Atlanta.txt",50,"NV");
		//SRmatriciDizionari matrici=new SRmatriciDizionari("atlanta.txt",500,20);

		// Faccio dei test parziali solo per velocizzare il processo
		double[] VettoreDeltaTime0=new double[2];
		VettoreDeltaTime0[0]=( 1*05*60);	VettoreDeltaTime0[1]=( 1*10*60);
		/*
		VettoreDeltaTime0[2]=( 1*15*60);	VettoreDeltaTime0[3]=( 1*20*60);	VettoreDeltaTime0[4]=( 1*30*60);
		VettoreDeltaTime0[5]=( 1*60*60);	VettoreDeltaTime0[6]=( 2*60*60);
		VettoreDeltaTime0[7]=( 5*60*60);	VettoreDeltaTime0[8]=(10*60*60);*/

		String[] VettoreDeltaTime1=new String[2];
		VettoreDeltaTime1[0]="5 minuti";	VettoreDeltaTime1[1]="10 minuti";
		/*
		VettoreDeltaTime1[2]="15 minuti";	VettoreDeltaTime1[3]="20 minuti";	VettoreDeltaTime1[4]="30 minuti";
		VettoreDeltaTime1[5]="1 ora";		VettoreDeltaTime1[6]="2 ore";
		VettoreDeltaTime1[7]="5 ore";		VettoreDeltaTime1[8]="10 ore";*/

		//double VettoreNumIterazSRtoMimp=new double{1,2,10,25,50};

		String[] VettoreIndici=new String[5];
		VettoreIndici[0]="NV";	// NV - Number of Values
		VettoreIndici[1]="TT";	// TT - Total Time
		VettoreIndici[2]="AT";	// AT - Avarage Time
		VettoreIndici[3]="CL";	// CL - Combination Line NV & TT
		VettoreIndici[4]="SR";	// SR - Space Rank

		int[] VettorePrecisioneGradi=new int[3];
		VettorePrecisioneGradi[0]=50;	VettorePrecisioneGradi[1]=100;	VettorePrecisioneGradi[2]=300;
		/*VettorePrecisioneGradi[0]=500;	VettorePrecisioneGradi[1]=1000;	VettorePrecisioneGradi[2]=3000;*/

		MediaVarianzaAutomaticaPivotFinale prova=new MediaVarianzaAutomaticaPivotFinale("Atlanta.txt","percorsoAtlanta.txt",VettoreDeltaTime0,VettoreDeltaTime1,VettoreIndici,VettorePrecisioneGradi);

    }
}
