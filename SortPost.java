import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.spi.ResourceBundleControlProvider;

public class SortPost {
	String filename_in;
	String filename_out;
	int M; //size of chunk to sort in number of lines, also the size of a sorted sublist
	int B; //size of buffer in characters (1 char = 2 bytes), if B==8192 char, it is 16K bytes
	int c; //sort column
	String sep; //column separator
	String tmpfileprefix = "tmpfile";

	int numChunks = 0;

	public SortPost(String filename_in, String filename_out, int M, int B, int c, String sep) {
		this.filename_in = filename_in;
		this.filename_out = filename_out;

		this.M = M;
		this.B = B;

		this.c = c; // which colomn to sort on
		this.sep = sep;
	}

	public void doSort() throws Exception {
		//Phase 1
		long startTime = System.currentTimeMillis();
		System.out.println("Phase 1 started");
		BufferedReader in = new BufferedReader(new FileReader(filename_in));

		int cnt = 0;
		String[] chunk = new String[M];
		String line;
		while ((line = in.readLine()) != null) {
			chunk[cnt] = line;
			cnt++;
			if (cnt == M) { // Buffer is full, output file
				String fileName = tmpfileprefix + numChunks;
				sortAndSaveChunk(chunk, fileName);
				numChunks++;
				chunk = new String[M];
				cnt = 0;
			}
		}

		in.close();
		System.out.println("Phase 1 Time elapsed (sec) = " + (System.currentTimeMillis() - startTime) / 1000.0);

		//Phase 2
		startTime = System.currentTimeMillis();
		System.out.println("Phase 2 started");

		//We have numChunks sorted sublists that we need to merge.
		//We don't need to do buffer management directly.
		//Buffer management is done by BufferedReader.
		BufferedReader[] readers = new BufferedReader[numChunks];

		//We will use a Priority Queue to implement efficiently
		//the competition among the heads of the sorted sublists (chunks).
		//We create a head-index pair to be used as the type for
		//the elements of the Priority Queue.
		//The index is the number of the sorted sublist the head belongs to.
		//Why do we need the index?
		//When a head "wins" (being the smallest among heads),
		//it "migrates" to the output buffer. As such, we need to
		//insert a new head from the corresponding sublist (chunk),
		//and the index tells us which sublist that is.
		class HeadIndexPair {
			String head;
			int i;

			HeadIndexPair(String head, int i) {
				this.head = head;
				this.i = i;
			}
		}
		PriorityQueue<HeadIndexPair> heads = new PriorityQueue<>(numChunks, (a, b) -> compare(a.head, b.head));

		for (int i = 0; i < numChunks; i++)
			readers[i] = new BufferedReader(new FileReader(tmpfileprefix + i), B);

		BufferedWriter out = new BufferedWriter(new FileWriter(filename_out), B);

		for (int i = 0; i < numChunks; i++)
			heads.add(new HeadIndexPair(readers[i].readLine(), i));

		int whichList = 0;
		while (true) {
			HeadIndexPair minh = heads.poll();

			if (minh.head != null) {
				whichList = minh.i;
				out.write(minh.head);
				out.newLine();
				out.flush();
				heads.add(new HeadIndexPair(readers[whichList].readLine(), whichList));
			} else {
				break;
			}
		}

		for (int i = 0; i < numChunks; i++)
			readers[i].close();

		out.close();
		System.out.println("Phase 2 Time elapsed (sec) = " + (System.currentTimeMillis() - startTime) / 1000.0);
		System.out.println("Sort complete");
	}

	void sortAndSaveChunk(String[] chunk, String filename) throws Exception {
		System.out.println("sorting and saving " + filename);

		Arrays.parallelSort(chunk, (a, b) -> compare(a, b));

		BufferedWriter out = new BufferedWriter(new FileWriter(filename));

		for (int i = 0; i < chunk.length; i++) {
			if (chunk[i] != null) {
				out.write(chunk[i]);
				out.newLine();
			}
		}

		out.close();
	}

	String extractCol(String line) {
		String[] columns = line.split(sep);
		return columns[c];
	}

	int compare(String a, String b) {
		if (a == null && b == null)
			return 0;
		if (a == null)
			return 1;
		if (b == null)
			return -1;
		return extractCol(a).compareTo(extractCol(b));
	}

	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();

		//taxpayers_30.txt is a file containing only 30 records.
		//This is to make sure your program works correctly.
		// SortPost mysort = new SortPost("taxpayers_30.txt", "taxpayers_30_sorted.txt", 10, 8192, 0, "\t");

		//Only do this when you are sure the program works fine.
		//The sort of such a file could take several minutes
		//depending on your machine (be patient).
		SortPost mysort = new SortPost("taxpayers_3M.txt", //input file
				"taxpayers_3M_sorted.txt", //output sorted file
				300_000, //M, size of chunk to sort in number of lines, also the size of a sorted sublist
				8192, //B, size of buffer in characters (1 char = 2 bytes), if B==8192 char, it is 16K bytes
				1, //c, sort column
				"\t" //sep, column separator
		);

		mysort.doSort();

		System.out.println("Time elapsed (sec) = " + (System.currentTimeMillis() - startTime) / 1000.0);
	}
}