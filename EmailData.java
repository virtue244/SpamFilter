import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

public class EmailData {
	
	//private String[] commonWords = {"and", "_", "-", "*", "=", ",", "the", "you", "to", "/", "$", "of", "a", "i"};
	private String name;
	private String subject;
	public Map<String, Integer> wordList = new HashMap<String,Integer>();
	public ArrayList<String> allWords = new ArrayList<String>();
	public ArrayList<String> words = new ArrayList<String>(); // has allWords with no dups
	private boolean isSpam;
	private int totalWordCount;
	private double spamScore = 0;
	private boolean classifiedSpam;
	
	EmailData(String name, String subject){
		this.name = name;
		this.subject = subject;
		Scanner input = new Scanner(subject);
		totalWordCount = 0;
		while (input.hasNextLine()) {
			String line = input.nextLine();
			StringTokenizer st = new StringTokenizer(line);
			//System.out.println(line);
			while (st.hasMoreTokens()) {
				
				String word = st.nextToken(); // st.nextToken("!.;?\t\n\r\f
												// ") Modifies
												// the
												// delimiters of
												// the
												// StringTokenizer
//				if(Arrays.asList(commonWords).contains(word.toLowerCase())){
//					continue;
//				}
				totalWordCount += 1;
				int count;
				if(wordList.containsKey(word)){
					count = wordList.get(word);
				}else{
					count = 0;
					words.add(word);
				}
				allWords.add(word);
				wordList.put(word, ++count);
			}
		}
		//System.out.println(totalWordCount);
		input.close();
		if(name.contains("sp")){
			isSpam = true;
		}else{
			isSpam = false;
		}
	}

	public String getName() {
		return name;
	}

	public String getSubject() {
		return subject;
	}

	public boolean getIsSpam() {
		return isSpam;
	}

	public int getTotalWordCount() {
		return totalWordCount;
	}
	public void setTotalWordCount(int totalWordCount) {
		this.totalWordCount = totalWordCount;
	}

	public double getSpamScore() {
		return spamScore;
	}

	public void setSpamScore(double d) {
		this.spamScore = d;
	}

	public boolean isClassifiedSpam() {
		return classifiedSpam;
	}

	public void setClassifiedSpam(boolean classifiedSpam) {
		this.classifiedSpam = classifiedSpam;
	}
}
