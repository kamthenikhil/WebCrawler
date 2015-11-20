package com.chinappa.information.retrieval.test;

import com.chinappa.information.retrieval.Indexer.WebIndexer;

public class IndexerTest {

	public static void main(String[] args) {

		long startTime = System.currentTimeMillis();
		WebIndexer indexer = new WebIndexer();
		indexer.buildIndex();
		indexer.searchDocuments("davis");
		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;
		System.out.println("Time elapsed: " + elapsedTime / 1000 + " secs");
	}
}
