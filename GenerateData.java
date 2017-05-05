//Class to generate data.
//Change variable N below to control how many records you generate.
//Change the filename to reflect the number of records it will hold.

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;

public class GenerateData {

	public static String randomString(String candidateChars, int length) {
	    StringBuilder sb = new StringBuilder();
	    Random random = new Random();
	    for (int i = 0; i < length; i++)
	        sb.append(candidateChars.charAt(random.nextInt(candidateChars.length())));
	    return sb.toString();
	}

	public static void main(String[] args) throws Exception {
		int N = 3_000_000; //number of rows to generate
		BufferedWriter out = new BufferedWriter( new FileWriter("taxpayers_3M.txt") );

		BufferedReader in_first = new BufferedReader( new FileReader("popular-first.txt") );
		BufferedReader in_last  = new BufferedReader( new FileReader("popular-last.txt") );

		ArrayList<String> firstnames = new ArrayList<>();
		ArrayList<String> lastnames  = new ArrayList<>();
		String line;
		while ( (line = in_first.readLine()) != null )
			firstnames.add(line);
		while ( (line = in_last.readLine()) != null )
			lastnames.add(line);

		Random random = new Random();
		for(int i=0; i<N; i++) {

			if((i+1)%1_000_000 == 0)
				System.out.println(i+1);

			String SIN = randomString("0123456789", 3) + "-" +
						 randomString("0123456789", 3) + "-" +
						 randomString("0123456789", 3);

			String firstname = firstnames.get( random.nextInt(firstnames.size()) );
			String lastname = lastnames.get( random.nextInt(lastnames.size()) );
			int salary = (int) (random.nextGaussian()*10_000+50_000); //mean 50_000, std = 10_000

			out.write(SIN +"\t"+firstname+"\t"+lastname+"\t"+salary);
			out.newLine();
		}

		in_first.close();
		in_last.close();
		out.close();
	}

}
