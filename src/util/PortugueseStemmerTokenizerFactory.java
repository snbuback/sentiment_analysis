package util;

import ptstemmer.Stemmer;
import ptstemmer.exceptions.PTStemmerException;

import com.aliasi.tokenizer.ModifyTokenTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class PortugueseStemmerTokenizerFactory extends ModifyTokenTokenizerFactory {
	
	private static final long serialVersionUID = 1L;
	private final Stemmer st;

	public PortugueseStemmerTokenizerFactory(TokenizerFactory factory) throws PTStemmerException {
		super(factory);
		this.st = Stemmer.StemmerFactory(Stemmer.StemmerType.ORENGO);
		this.st.enableCaching(10000);
	}

	@Override
	public String modifyToken(String token) {
		return st.getWordStem(token);
	}

}
