package com.chinappa.information.retrieval.Indexer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;

import com.chinappa.information.retrieval.configuration.WebCrawlerConfiguration;
import com.chinappa.information.retrieval.constants.CrawlerConstants;
import com.chinappa.information.retrieval.util.FileHandlerUtil;

public class WebIndexer {

	public void init() {

		WebCrawlerConfiguration.getInstance();
		StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_41);
		Directory index = new RAMDirectory();
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_41,
				analyzer);
		createIndexForDocuments(index, config);
		Query query = prepareQuery(analyzer);
		searchDocuments(index, query);
	}

	private void searchDocuments(Directory index, Query query) {
		try {
			int hitsPerPage = 10;
			IndexReader reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			TopScoreDocCollector collector = TopScoreDocCollector.create(
					hitsPerPage, true);
			searcher.search(query, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;
			System.out.println("Found " + hits.length + " hits.");
			for (int i = 0; i < hits.length; ++i) {
				int docId = hits[i].doc;
				Document d = searcher.doc(docId);
				System.out.println((i + 1) + ". "
						+ d.get(CrawlerConstants.INDEX_FIELD) + ": "
						+ d.get(CrawlerConstants.TITLE_FIELD));
			}

			// reader can only be closed when there
			// is no need to access the documents any more.
			reader.close();
		} catch (IOException e) {
		}
	}

	private Query prepareQuery(StandardAnalyzer analyzer) {
		String querystr = "davis";
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
					analyzer, fieldBoost).parse(querystr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return query;
	}

	private void createIndexForDocuments(Directory index,
			IndexWriterConfig config) {
		IndexWriter indexWriter;
		try {
			indexWriter = new IndexWriter(index, config);
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
					addDoc(indexWriter, doc.title(), metadata, content,
							filename);
				}
			}
			indexWriter.close();
		} catch (IOException e) {
		}
	}

	private void addDoc(IndexWriter indexWriter, String title, String metadata,
			String content, String filename) throws IOException {
		Document doc = new Document();
		doc.add(new TextField(CrawlerConstants.TITLE_FIELD, title,
				Field.Store.YES));
		doc.add(new TextField(CrawlerConstants.METADATA_FIELD, metadata,
				Field.Store.NO));
		doc.add(new TextField(CrawlerConstants.CONTENT_FIELD, content,
				Field.Store.NO));
		doc.add(new StringField(CrawlerConstants.INDEX_FIELD, filename,
				Field.Store.YES));
		indexWriter.addDocument(doc);
	}
}
