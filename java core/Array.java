import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class Array{

	public static void main (String args[]) {

		int n=3;
		int m=5;

		ArrayList<ArrayList<ArrayList<Integer>>> arr = new ArrayList<ArrayList<ArrayList<Integer>>> ();

		for(int i=0;i<n;i++){
			ArrayList<ArrayList<Integer>> riga = new ArrayList<ArrayList<Integer>>();
			for(int j=0;j<m;j++){
				riga.add(new ArrayList<Integer>());
			}
			arr.add(riga);
		}


		for(int i=0;i<n;i++){
			for(int j=0;j<m;j++){
				ArrayList<Integer> prova = new ArrayList<Integer>();
				for(int w=0;w<(i+j);w++){
					int k=(i+j);
					prova.add(k);
					arr.get(i).get(j).add(k);
				}
				//System.out.println(i+"+"+j+" "+"->"+prova.size()+" "+prova);
			}
			//System.out.println();
		}

		//System.out.println("\n array di prova\n");

		//System.out.println("\n arr -> "+arr.size()+"\n");
		//System.out.println("\n arr.get() -> "+arr.get(0).size()+"\n");
		//System.out.println("\n arr.get().get() -> "+arr.get(2).get(4).size()+"\n");

		ArrayList<String> elem1 = new ArrayList<String> ();
		elem1.add("1");
		//System.out.println("\n arr -> "+elem1);
		elem1.clear();
		//System.out.println("\n arr -> "+elem1);

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy|HH:mm:ss");
		String s1 = "10/12/2001|10:25:36";
		String s2 = "10/12/2001|10:53:17";
		String st = "10/12/2001|10:32:31";

		GregorianCalendar c1 = new GregorianCalendar();
		GregorianCalendar c2 = new GregorianCalendar();
		GregorianCalendar c3 = new GregorianCalendar();
		GregorianCalendar ct = new GregorianCalendar();

		try{
			c1.setTime(sdf.parse(s1));
			c2.setTime(sdf.parse(s2));
			ct.setTime(sdf.parse(st));
		}
		catch(ParseException e){
			e.printStackTrace();
		}

		c3.setTimeInMillis(c1.getTimeInMillis()+((c2.getTimeInMillis()-c1.getTimeInMillis())/4));
		String s3=sdf.format(c3.getTime());

		//System.out.println(s3);

		/*
		if(c3.getTimeInMillis()<ct.getTimeInMillis())
			System.out.println(s3+" < "+st);
		else
			System.out.println(s3+" > "+st);

		double diff=c2.getTimeInMillis()-c1.getTimeInMillis();
		System.out.println(diff/1000);
		*/

		ArrayList<ArrayList<Double>> d1= new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> d2= new ArrayList<ArrayList<Double>>();
		ArrayList<Double> previsione= new ArrayList<Double>();
		previsione.add(2.0); previsione.add(3.0);
		d1.add(previsione);
		d1.add(previsione);
		System.out.println(d2+" - "+d1);
		previsione.clear();
		d2.add(previsione);
		d2.add(previsione);
		System.out.println(d2+" - "+d1);

		/*
		ArrayList<String> elem1 = new ArrayList<String> ();
		elem1.add("1");
		elem1.add("2");
		elem1.add("3");

		ArrayList<String> elem2 = new ArrayList<String> ();
		elem2.add ("Ciao");


		ArrayList<ArrayList<String>> arr = new ArrayList<ArrayList<String>> ();
		arr.add (elem1);
		arr.add (elem2);

		System.out.println("Prova \n"+arr.get(0));
		*/
	}
}
