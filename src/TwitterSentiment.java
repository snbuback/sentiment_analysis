import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.StatusStream;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import com.aliasi.classify.Classification;
import com.aliasi.classify.Classified;
import com.aliasi.classify.DynamicLMClassifier;
import com.aliasi.classify.JointClassification;
import com.aliasi.classify.NaiveBayesClassifier;
import com.aliasi.lm.NGramProcessLM;
import com.aliasi.tokenizer.EnglishStopTokenizerFactory;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;

public class TwitterSentiment {
	
	private static final String BASE_PATH = "/data/snbuback/workspace/SentimentAnalysis/scripts/";

	String[] mCategories = { "positivo", "negativo" };
	NaiveBayesClassifier mClassifier;

	TwitterSentiment(String[] args) {
		System.out.println("\nTwitter Sentiment");
		mClassifier = new NaiveBayesClassifier(mCategories, 
				new LowerCaseTokenizerFactory(IndoEuropeanTokenizerFactory.INSTANCE),
				1);
//		mClassifier = DynamicLMClassifier
//		.createNGramProcess(mCategories, nGram);
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
				String review = reader.readLine();
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
		int numTests = 0;
		int numCorrect = 0;
		for (int i = 0; i < mCategories.length; ++i) {
			String category = mCategories[i];
			BufferedReader reader = new BufferedReader(new FileReader(
					BASE_PATH + mCategories[i] + "-teste.txt"));
			while (true) {
				String review = reader.readLine();
				if (review == null) {
					break;
				}
				++numTests;
				JointClassification cls = mClassifier.classify(review);
				if (cls.bestCategory().equals(category)) {
					++numCorrect;
				} else {
					System.out.format("%s - pos(%f) neg(%f): %s\n", cls
							.bestCategory(), cls
							.conditionalProbability("positivo"), cls
							.conditionalProbability("negativo"), review);
				}
			}
		}
		System.out.println("  # Test Cases=" + numTests);
		System.out.println("  # Correct=" + numCorrect);
		System.out.println("  % Correct=" + ((double) numCorrect)
				/ (double) numTests);
	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		final TwitterSentiment ts = new TwitterSentiment(args);
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

}
