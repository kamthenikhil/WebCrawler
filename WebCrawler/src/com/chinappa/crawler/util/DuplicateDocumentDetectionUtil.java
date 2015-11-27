package com.chinappa.crawler.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;
import org.tartarus.snowball.ext.EnglishStemmer;

import com.chinappa.crawler.constant.CrawlerConstants;
import com.chinappa.information.retrieval.util.FileHandlerUtil;

public class DuplicateDocumentDetectionUtil {

	private static HashMap<Integer, String> documentSimHashMap = new HashMap<Integer, String>();

	/**
	 * The following method is used to determine if the document is a duplicate
	 * document. It first removes all the stop words and then uses stemming to
	 * store the tokens along with their frequencies. This data is then used to
	 * calculate the SimHash for the document.
	 * 
	 * @param document
	 * @param url
	 * @return
	 */
	public static boolean isDuplicateDocument(String document, String url) {

		HashMap<String, Integer> tokenMap = new HashMap<String, Integer>();
		ArrayList<String> listOfEnglishStopWords = FileHandlerUtil
				.readFile(CrawlerConstants.STOP_WORDS_FILE_PATH_EN);
		CharArraySet stopWordsSet = new CharArraySet(Version.LUCENE_41,
				listOfEnglishStopWords, true);
		Tokenizer tokenizer = new StandardTokenizer(Version.LUCENE_41,
				new StringReader(document));

		StandardFilter standardFilter = new StandardFilter(Version.LUCENE_41,
				tokenizer);
		StopFilter stopFilter = new StopFilter(Version.LUCENE_41,
				standardFilter, stopWordsSet);
		CharTermAttribute charTermAttribute = tokenizer
				.addAttribute(CharTermAttribute.class);
		try {
			stopFilter.reset();
			while (stopFilter.incrementToken()) {
				String token = charTermAttribute.toString();
				EnglishStemmer stemmer = new EnglishStemmer();
				stemmer.setCurrent(token);
				stemmer.stem();
				String current = stemmer.getCurrent();
				Integer frequency = null;
				if (tokenMap.containsKey(current)) {
					frequency = tokenMap.get(current) + 1;
				} else {
					frequency = new Integer(1);
				}
				tokenMap.put(current, frequency);
			}
		} catch (IOException e1) {
		} finally {
			try {
				stopFilter.close();
			} catch (IOException e) {
			}
		}
		int simHash = SimHashUtil.simHash32(tokenMap);
		synchronized (documentSimHashMap) {
			if (documentSimHashMap.containsKey(simHash)) {
				return true;
			} else {
				documentSimHashMap.put(simHash, url);
				return false;
			}
		}
	}

}
