package com.chinappa.information.retrieval.Indexer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;

import com.chinappa.information.retrieval.configuration.WebCrawlerConfiguration;
import com.chinappa.information.retrieval.constants.CrawlerConstants;
import com.chinappa.information.retrieval.dto.SearchResultDTO;
import com.chinappa.information.retrieval.util.FileHandlerUtil;

public class WebIndexer {

	public WebIndexer() {
		init();
	}

	public void init() {

		WebCrawlerConfiguration.getInstance();
	}

	public void buildIndex() {

		Directory directory = null;
		try {
			directory = FSDirectory.open(new File(
					CrawlerConstants.INDEX_DIRECTORY));
			IndexWriterConfig config = getIndexWriterConfig();
			createIndexForDocuments(directory, config);
		} catch (IOException e) {
		} finally {
			if (directory != null) {
				try {
					directory.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public SearchResultDTO[] searchDocuments(String queryString) {
		
		SearchResultDTO[] searchResults = null;
		try {
			int hitsPerPage = 10;
			searchResults = new SearchResultDTO[hitsPerPage];
			IndexSearcher searcher = new IndexSearcher(
					DirectoryReader.open(FSDirectory.open(new File(
							CrawlerConstants.INDEX_DIRECTORY))));
			Query query = prepareQuery(queryString, getAnalyzer());
			TopScoreDocCollector collector = TopScoreDocCollector.create(
					hitsPerPage, true);
			searcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			System.out.println("Found " + hits.length + " hits.");
			for (int i = 0; i < hits.length; ++i) {
				SearchResultDTO searchResultDTO = new SearchResultDTO();
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				searchResultDTO.setUrl(d.get(CrawlerConstants.INDEX_FIELD));
				searchResultDTO.setTitle(d.get(CrawlerConstants.TITLE_FIELD));
				searchResults[i] = searchResultDTO;
			}
		} catch (IOException e) {
		}
		return searchResults;
	}

	private Query prepareQuery(String queryString, Analyzer analyzer) {
		Query query = null;
		try {
			String[] fields = new String[] { CrawlerConstants.TITLE_FIELD,
					CrawlerConstants.METADATA_FIELD,
					CrawlerConstants.CONTENT_FIELD };
			Map<String, Float> fieldBoost = new HashMap<String, Float>();
			fieldBoost.put(CrawlerConstants.TITLE_FIELD, 4f);
			fieldBoost.put(CrawlerConstants.METADATA_FIELD, 2f);
			fieldBoost.put(CrawlerConstants.CONTENT_FIELD, 1f);
			query = new MultiFieldQueryParser(Version.LUCENE_41, fields,
					analyzer, fieldBoost).parse(queryString);
		} catch (ParseException e) {
		}
		return query;
	}

	private void createIndexForDocuments(Directory directory,
			IndexWriterConfig config) {
		IndexWriter indexWriter = null;
		try {
			if (IndexWriter.isLocked(directory)) {
				IndexWriter.unlock(directory);
			}
			indexWriter = new IndexWriter(directory, config);
			Properties properties = FileHandlerUtil.readFromPropertiesFile(
					WebCrawlerConfiguration.getInstance().getOutputDirectory(),
					CrawlerConstants.DEFAULT_MAPPINGS_FILENAME);
			for (Object object : properties.keySet()) {
				String url = (String) object;
				String filename = properties.getProperty(url);
				File file = new File(WebCrawlerConfiguration.getInstance()
						.getOutputDirectory() + File.separator + filename);
				if (file.exists()) {
					org.jsoup.nodes.Document doc = null;
					if (WebCrawlerConfiguration.getInstance()
							.isCompressionEnabled()) {
						String decompressedContent = FileHandlerUtil
								.fetchFromCompressedHTMLFile(
										WebCrawlerConfiguration.getInstance()
												.getOutputDirectory(), filename);
						doc = Jsoup.parse(decompressedContent);
					} else {
						doc = Jsoup.parse(file,
								CrawlerConstants.ENCODING_CHARSET);
					}

					String content = FileHandlerUtil.fetchDocumentText(doc);
					String metadata = FileHandlerUtil
							.fetchDocumentMetadata(doc);
					addDocumentFieldsToIndex(indexWriter, doc.title(),
							metadata, content, url);
				}
			}
		} catch (IOException e) {
		} finally {
			try {
				if (indexWriter != null)
					indexWriter.close();
			} catch (IOException e) {
			}
		}
	}

	private void addDocumentFieldsToIndex(IndexWriter indexWriter,
			String title, String metadata, String content, String url)
			throws IOException {
		Document doc = new Document();
		doc.add(new TextField(CrawlerConstants.TITLE_FIELD, title,
				Field.Store.YES));
		doc.add(new TextField(CrawlerConstants.METADATA_FIELD, metadata,
				Field.Store.NO));
		doc.add(new TextField(CrawlerConstants.CONTENT_FIELD, content,
				Field.Store.NO));
		doc.add(new StringField(CrawlerConstants.INDEX_FIELD, url,
				Field.Store.YES));
		indexWriter.addDocument(doc);
	}

	private IndexWriterConfig getIndexWriterConfig() {
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_41,
				getAnalyzer());
		return config;
	}

	private Analyzer getAnalyzer() {
		Analyzer analyzer = new StopAnalyzer(Version.LUCENE_41);
		return analyzer;
	}
}
