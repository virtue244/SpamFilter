import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("rawtypes")
public class SpamFilter {

	private static TreeMap<String, Integer> trainingWordList;
	private static TreeMap<String, Integer> trainingSpamWordList;
	private static TreeMap<String, Integer> trainingNonSpamWordList;

	private static ArrayList<String> trainingWords;

	// words that have a higher probability to occur in spam
	private static ArrayList<String> trainingSpamWords = new ArrayList<String>();

	// words that have a higher probability to occur in non-spam
	private static ArrayList<String> trainingNonSpamWords = new ArrayList<String>();

	private static int totalTrainingWordCount;
	private static int totalTrainingSpamWordCount;
	private static int totalTrainingNonSpamWordCount;

	static Scanner input = null;

	private static ArrayList<EmailData> trainingEmails = new ArrayList<EmailData>();
	private static ArrayList<EmailData> spamTrainingEmails = new ArrayList<EmailData>();
	private static ArrayList<EmailData> nonSpamTrainingEmails = new ArrayList<EmailData>();

	private static ArrayList<EmailData> testingEmails = new ArrayList<EmailData>();
	private static ArrayList<EmailData> spamTestingEmails = new ArrayList<EmailData>();
	private static ArrayList<EmailData> nonSpamTestingEmails = new ArrayList<EmailData>();

	private static final double naiveBound = .145;
	private static final double scrubbedNaiveBound = .145;

	public static void main(String[] args) throws InterruptedException {

		// load training data
		loadData("training", trainingEmails);
		sortEmails(trainingEmails, spamTrainingEmails, nonSpamTrainingEmails);
		// System.out.println("trainingEmails.size() -> " +
		// trainingEmails.size());
		// System.out.println("spamTrainingEmails.size() -> " +
		// spamTrainingEmails.size());
		// System.out.println("nonSpamTrainingEmails.size() -> " +
		// nonSpamTrainingEmails.size());
		// System.out.println();

		// load testing data
		loadData("testing", testingEmails);
		sortEmails(testingEmails, spamTestingEmails, nonSpamTestingEmails);
		// System.out.println("testingingEmails.size() -> " +
		// testingEmails.size());
		// System.out.println("spamTestingEmails.size() -> " +
		// spamTestingEmails.size());
		// System.out.println("nonSpamTestingEmails.size() -> " +
		// nonSpamTestingEmails.size());
		// System.out.println();

		// set up lists based on training data
		setTotalTrainingWordCount(calculateTotalWordCount(trainingEmails));
		setTotalTrainingSpamWordCount(calculateTotalWordCount(spamTrainingEmails));
		setTotalTrainingNonSpamWordCount(calculateTotalWordCount(nonSpamTrainingEmails));

		// System.out.println("total count of words in all training emails = " +
		// totalTrainingWordCount);
		// System.out.println("total count of words in spam training emails = "
		// + totalTrainingSpamWordCount);
		// System.out.println("total count of words in non-spam training emails
		// = " + totalTrainingNonSpamWordCount);

		// System.out.println();

		trainingWordList = buildWordList(trainingEmails);
		System.out.println("number of word in all training emails - dups = " + trainingWordList.size());

		trainingSpamWordList = buildWordList(spamTrainingEmails);
		System.out.println("number of word in training spam emails - dups = " + trainingSpamWordList.size());

		trainingNonSpamWordList = buildWordList(nonSpamTrainingEmails);
		System.out.println("number of word in training nonSpam emails - dups = " + trainingNonSpamWordList.size());
		//
		System.out.println("Naive Threshhold = " + naiveBound);

		// trainingWords = allWords(trainingWordList);
		// System.out.println("total words - dups = " + trainingWords.size());

		// niave bayes
		 System.out.println("----------------------------\nLoading Training data into naiveBayes");
		 naiveBayes(trainingEmails, spamTrainingEmails,nonSpamTrainingEmails, naiveBound);
		 System.out.println();
		 System.out.println("----------------------------\nLoading Testing data into naiveBayes");
		 naiveBayes(testingEmails, spamTestingEmails, nonSpamTestingEmails, naiveBound);

		System.out.println();
		System.out.println("Scrubbing");
		System.out.println();
		scrub(trainingEmails);
		scrub(testingEmails);

		setTotalTrainingWordCount(calculateTotalWordCount(trainingEmails));
		setTotalTrainingSpamWordCount(calculateTotalWordCount(spamTrainingEmails));
		setTotalTrainingNonSpamWordCount(calculateTotalWordCount(nonSpamTrainingEmails));

		trainingWordList = buildWordList(trainingEmails);
		System.out.println("number of word in all training emails - dups = " + trainingWordList.size());

		trainingSpamWordList = buildWordList(spamTrainingEmails);
		System.out.println("number of word in training spam emails - dups = " + trainingSpamWordList.size());

		trainingNonSpamWordList = buildWordList(nonSpamTrainingEmails);
		System.out.println("number of word in training nonSpam emails - dups = " + trainingNonSpamWordList.size());

//		removeBelow50(trainingWordList);
//		removeBelow50(trainingSpamWordList);
//		removeBelow50(trainingNonSpamWordList);
		// 
		
		
		System.out.println("Naive Threshhold = " + scrubbedNaiveBound);
		System.out.println("----------------------------\nLoading Training data into naiveBayes");
		naiveBayes(trainingEmails, spamTrainingEmails, nonSpamTrainingEmails, scrubbedNaiveBound);
		System.out.println();
		System.out.println("----------------------------\nLoading Testing data into naiveBayes");
		naiveBayes(testingEmails, spamTestingEmails, nonSpamTestingEmails, scrubbedNaiveBound);

		// k-nn
		// k_nn();
	}

	private static void scrub(ArrayList<EmailData> emails) {
		Scanner input = null;
		String[] commonWords = {"and", "_", "-", "*", "=", ",", "the", "you", "to", "/", "$", "of", "a", "i"};
		for (int i = 0; i < emails.size(); i++) {
			EmailData email = emails.get(i);
			String subject = emails.get(i).getSubject();
			input = new Scanner(subject);
			trainingSpamWords.clear();
			trainingNonSpamWords.clear();
			email.wordList.clear();
			email.allWords.clear();
			email.words.clear();
			email.setSpamScore(0);
			email.setTotalWordCount(0);
			while (input.hasNextLine()) {
				String line = input.nextLine();
				StringTokenizer st = new StringTokenizer(line);
				// System.out.println(line);
				while (st.hasMoreTokens()) {

					String word = st.nextToken("@#$%^&*_-|()[]{}<>,!.;?\t\n\r\f "); 
					if (Arrays.asList(commonWords).contains(word.toLowerCase())) {
						continue;
					}
					email.setTotalWordCount(email.getTotalWordCount() + 1);
					int count;
					if (email.wordList.containsKey(word)) {
						count = email.wordList.get(word);
					} else {
						count = 0;
						email.words.add(word);
					}
					email.allWords.add(word);
					email.wordList.put(word, ++count);
				}
			}
			input.close();
		}
	}

	public static void removeBelow50(TreeMap<String, Integer> wordList) {
		Set keys = wordList.keySet();
		for (Iterator i = keys.iterator(); i.hasNext();) {
			String key = (String) i.next();
			if (wordList.get(key) < 50) {
				wordList.remove(key);
				keys = wordList.keySet();
				i = keys.iterator();
			}
		}
	}

	private static void naiveBayes(ArrayList<EmailData> emails, ArrayList<EmailData> spamEmails,
			ArrayList<EmailData> nonSpamEmails, double bound) {
		trainingProbabilitySort();
		countSpamScore(emails);
		calcAccuracy(emails, spamEmails, nonSpamEmails, bound);
	}

	public static void calcAccuracy(ArrayList<EmailData> emails, ArrayList<EmailData> spamEmails,
			ArrayList<EmailData> nonSpamEmails, double bound) {
		System.out.println("((Correct Spam + Corrent NonSpam) / emails.size())*100)");
		System.out.println("(" + (int) numberSpamCorrect_NaiveBayes(spamEmails, bound) + " + "
				+ (int) numberNonSpamCorrect_NaiveBayes(nonSpamEmails, bound) + ") / " + (int) emails.size()
				+ ") * 100)");
		System.out.println("accuracy = " + ((numberSpamCorrect_NaiveBayes(spamEmails, bound)
				+ numberNonSpamCorrect_NaiveBayes(nonSpamEmails, bound)) / emails.size()) * 100);

	}

	public static double numberNonSpamCorrect_NaiveBayes(ArrayList<EmailData> nonSpam, double bound) {
		int correct = 0;
		for (int i = 0; i < nonSpam.size(); i++) {
			EmailData email = nonSpam.get(i);
			if (email.getSpamScore() < bound) {
				correct++;
			}
		}
		// System.out.println("number corrent spam predictions: " + correct);
		return correct;
	}

	public static double numberSpamCorrect_NaiveBayes(ArrayList<EmailData> spam, double bound) {
		int correct = 0;
		for (int i = 0; i < spam.size(); i++) {
			EmailData email = spam.get(i);
			if (email.getSpamScore() > bound) {
				correct++;
			}
		}
		// System.out.println("number of correct nonspam predictions: "+
		// correct);
		return correct;
	}

	public static double avgScamScore(ArrayList<EmailData> emails) {
		double score = 0;
		for (int i = 0; i < emails.size(); i++) {
			score = score + emails.get(i).getSpamScore();
		}
		return score / (emails.size());
	}

	public static void countSpamScore(ArrayList<EmailData> emails) {
		for (int i = 0; i < emails.size(); i++) {
			EmailData email = emails.get(i);
			for (int j = 0; j < email.getTotalWordCount(); j++) {
				String word = email.allWords.get(j);
				// consider adding more for a higher frequency spam word
				if (trainingSpamWords.contains(word)) {
					// System.out.println();
					if (trainingSpamWordList.containsKey(word)) {
						email.setSpamScore((email.getSpamScore()
								+ (Math.log10(trainingSpamWordList.get(word))) / (double) email.getTotalWordCount()));
					}else{
						System.out.println(word + " is not a spamWord");
					}

				}
			}
		}
	}

	public static void trainingProbabilitySort() {
		Set<String> keys = trainingWordList.keySet();
		for (String key : keys) {
			// does this word occur more in trainingSpamWordList or
			// trainingNonSpamWordList
			int spam;
			int nonSpam;
			if (trainingSpamWordList.containsKey(key)) {
				spam = trainingSpamWordList.get(key);
			} else {
				spam = 0;
			}
			if (trainingNonSpamWordList.containsKey(key)) {
				nonSpam = trainingNonSpamWordList.get(key);
			} else {
				nonSpam = 0;
			}
			if (spam > nonSpam) {
				trainingSpamWords.add(key);
			} else {
				// occurs more in non-spam
				trainingNonSpamWords.add(key);
			}
		}
	}

	public static ArrayList<String> allWords(TreeMap<String, Integer> wordList) {
		ArrayList<String> words = new ArrayList<String>();
		Set<String> keys = wordList.keySet();
		for (String key : keys) {
			words.add(key);
		}
		return words;
	}

	/*
	 * Builds a word list with frequency of that word in all emails given;
	 */
	public static TreeMap<String, Integer> buildWordList(ArrayList<EmailData> emails) {
		TreeMap<String, Integer> build = new TreeMap<String, Integer>();
		for (int i = 0; i < emails.size(); i++) {
			EmailData email = emails.get(i);
			for (int j = 0; j < email.words.size(); j++) {
				int count;
				String word = emails.get(i).words.get(j);
				if (email.wordList.containsKey(word)) {
					count = emails.get(i).wordList.get(word);
				} else {
					count = 0;
				}
				build.put(word, ++count);
			}
		}
		return build;
	}

	public static int calculateTotalWordCount(ArrayList<EmailData> emails) {
		int count = 0;
		for (int i = 0; i < emails.size(); i++) {
			count += emails.get(i).getTotalWordCount();
		}
		return count;
	}

	public static void sortEmails(ArrayList<EmailData> data, ArrayList<EmailData> spam, ArrayList<EmailData> nonSpam) {
		for (int i = 0; i < data.size(); i++) {
			if (data.get(i).getIsSpam()) {
				spam.add(data.get(i));
			} else {
				nonSpam.add(data.get(i));
			}
		}
	}

	public static void loadData(String fileName, ArrayList<EmailData> emails) {
		File dir = new File(fileName);
		File[] listOfFiles = dir.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			String subject = "";
			String name = listOfFiles[i].getName();
			try {
				input = new Scanner(listOfFiles[i]);
			} catch (FileNotFoundException e) {
				System.out.println("Error opening file.");
				System.exit(1);
			}
			try {

				while (input.hasNextLine()) {
					subject = subject + "\n" + input.nextLine();
				}
			} catch (NoSuchElementException e) {

			}
			emails.add(new EmailData(name, subject));
		}
	}

	public static int getTotalTrainingWordCount() {
		return totalTrainingWordCount;
	}

	public static void setTotalTrainingWordCount(int totalTrainingWordCount) {
		SpamFilter.totalTrainingWordCount = totalTrainingWordCount;
	}

	public static int getTotalTrainingSpamWordCount() {
		return totalTrainingSpamWordCount;
	}

	public static void setTotalTrainingSpamWordCount(int totalTrainingSpamWordCount) {
		SpamFilter.totalTrainingSpamWordCount = totalTrainingSpamWordCount;
	}

	public static int getTotalTrainingNonSpamWordCount() {
		return totalTrainingNonSpamWordCount;
	}

	public static void setTotalTrainingNonSpamWordCount(int totalTrainingNonSpamWordCount) {
		SpamFilter.totalTrainingNonSpamWordCount = totalTrainingNonSpamWordCount;
	}

}