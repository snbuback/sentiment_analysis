import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.Normalizer;

import ptstemmer.exceptions.PTStemmerException;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.StatusStream;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import util.PortugueseStemmerTokenizerFactory;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.JointClassification;
import com.aliasi.classify.NaiveBayesClassifier;
import com.aliasi.lm.TokenizedLM;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;

@SuppressWarnings(value="unchecked")
public class TwitterSentiment2 {
	
	private static final String BASE_PATH = "/Users/silvano/Documents/workspace/Sentiment/dataset/twitter2/";

	String[] mCategories = { "positivo", "negativo" };
	DynamicLMClassifier mClassifier;

	TwitterSentiment2(String[] args) throws PTStemmerException {
		System.out.println("\nTwitter Sentiment");
		mClassifier = new NaiveBayesClassifier(mCategories, 
				new LowerCaseTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE),
//				new PortugueseStemmerTokenizerFactory(new LowerCaseTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE)),
				0);
//		mClassifier = DynamicLMClassifier
//		.createNGramProcess(mCategories, 8);
	}

	void train() throws IOException {
		int numTrainingCases = 0;
		int numTrainingChars = 0;
		System.out.println("\nTraining.");
		for (int i = 0; i < mCategories.length; ++i) {
			String category = mCategories[i];
			Classification classification = new Classification(category);
			BufferedReader reader = new BufferedReader(new FileReader(
					BASE_PATH + mCategories[i] + "-treino.txt"));
			while (true) {
				++numTrainingCases;
				String review = removeAcentos(reader.readLine());
				if (review == null) {
					break;
				}
				numTrainingChars += review.length();
				Classified<CharSequence> classified = new Classified<CharSequence>(
						review, classification);
				mClassifier.handle(classified);
			}
			reader.close();
		}
		System.out.println("  # Training Cases=" + numTrainingCases);
		System.out.println("  # Training Chars=" + numTrainingChars);
	}

	void evaluate() throws IOException {
		System.out.println("\nEvaluating.");
		int numTests[] = new int[mCategories.length];
		int numCorrect[] = new int[mCategories.length];
		for (int i = 0; i < mCategories.length; ++i) {
			String category = mCategories[i];
			BufferedReader reader = new BufferedReader(new FileReader(
					BASE_PATH + mCategories[i] + "-teste.txt"));
			while (true) {
				String review = removeAcentos(reader.readLine());
				if (review == null) {
					break;
				}
				++numTests[i];
				JointClassification cls = mClassifier.classify(review);
				if (cls.bestCategory().equals(category)) {
					++numCorrect[i];
				}
//				System.out.format("%s - pos(%f) neg(%f): %s\n", 
//						cls.bestCategory(),
//						cls.conditionalProbability("positivo"), 
//						cls.conditionalProbability("negativo"),
//						review);
			}
		}
		
		int totalTests = 0, totalCorrect = 0;
		for (int i=0; i<numTests.length; i++) {
			System.out.format("  # Test Cases for %s=%d\n", mCategories[i], numTests[i]);
			System.out.format("  # Correct=%d\n", numCorrect[i]);
			System.out.format("  %% Correct=%f\n", ((double) numCorrect[i]) / (double) numTests[i]);
			totalTests += numTests[i];
			totalCorrect += numCorrect[i];
		}
		System.out.format("\n  # Total Test Cases =%d\n", totalTests);
		System.out.format("  # Correct=%d\n", totalCorrect);
		System.out.format("  %% Correct=%f\n", ((double) totalCorrect) / (double) totalTests);
	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		
		final TwitterSentiment2 ts = new TwitterSentiment2(args);
		ts.train();
		ts.evaluate();
		StatusListener listener = new StatusListener() {
			public void onStatus(Status status) {
				if (!"pt".equalsIgnoreCase(status.getUser().getLang()))
					return;

				JointClassification cls = ts.mClassifier.classify(status
						.getText());
				System.out.format("%s - pos(%f) neg(%f): %s\n", cls
						.bestCategory(), cls
						.conditionalProbability("positivo"), cls
						.conditionalProbability("negativo"), status
						.getText());
			}

			public void onDeletionNotice(
					StatusDeletionNotice statusDeletionNotice) {
			}

			public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
			}

			public void onException(Exception ex) {
				ex.printStackTrace();
			}
		};
		TwitterStream twitterStream = new TwitterStreamFactory(listener)
				.getInstance("snbuback_test", "mucuxi");
		StatusStream stream = twitterStream.getFilterStream(0, null,
				new String[] { ":)", ":(" });
		/*while (true) {
			stream.next(listener);
		}*/

	}
	
	public static String removeAcentos(String input) { return input; }
//	public static String removeAcentos(String input) {
//		if (input == null) return null;
//		input = Normalizer.normalize(input, Normalizer.Form.NFD);  
//		input = input.replaceAll("[^\\p{ASCII}]", "");  
//		return input; 
//	}

}
