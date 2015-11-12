package com.chinappa.information.retrieval.crawler;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.chinappa.information.retrieval.configuration.WebCrawlerConfiguration;
import com.chinappa.information.retrieval.constants.CrawlerConstants;
import com.chinappa.information.retrieval.util.FileHandlerUtil;

public class WebCrawler {

	/**
	 * The following attribute is used to store the url's which are already
	 * parsed.
	 */
	private ConcurrentHashMap<String, String> urlMap = null;
	/**
	 * The following attribute is used to store the url's which are to be
	 * parsed.
	 */
	private ConcurrentLinkedQueue<Runnable> threadQueue = null;
	/**
	 * The following attribute acts as a pool for all the threads used for
	 * crawling.
	 */
	private ThreadPoolExecutor executor = null;

	/**
	 * The following method is the entry point for the crawler.
	 */
	public void init() {

		WebCrawlerConfiguration webCrawlerConfiguration = WebCrawlerConfiguration
				.getInstance();
		ArrayList<String> listOfUrls = FileHandlerUtil
				.readFile(webCrawlerConfiguration.getInputDirectory()
						+ File.separator
						+ webCrawlerConfiguration.getSeedFileName());
		if (listOfUrls != null && !listOfUrls.isEmpty()) {
			urlMap = new ConcurrentHashMap<String, String>();
			threadQueue = new ConcurrentLinkedQueue<Runnable>();
			executor = (ThreadPoolExecutor) Executors
					.newFixedThreadPool(webCrawlerConfiguration
							.getThreadCount());
			for (String url : listOfUrls) {
				threadQueue.add(new CrawlingAgent(urlMap, threadQueue, url,
						webCrawlerConfiguration.getLevels()));
			}
			boolean isActive = true;
			while (isActive) {
				if (threadQueue.peek() != null) {
					if (WebCrawlerConfiguration.getInstance().getPageLimit()
							.longValue()
							- executor.getActiveCount() > (long) 0) {
						executor.execute(threadQueue.poll());
					} else {
						isActive = false;
						executor.shutdownNow();
					}
				}
				if (executor.getActiveCount() == 0) {
					if (threadQueue.peek() != null) {
						if (WebCrawlerConfiguration.getInstance()
								.getPageLimit().longValue()
								- executor.getActiveCount() > (long) 0) {
							executor.execute(threadQueue.poll());
						} else {
							isActive = false;
							executor.shutdownNow();
						}
					} else {
						isActive = false;
						executor.shutdownNow();
					}
				}
			}
			try {
				executor.awaitTermination(10000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
			}
			FileHandlerUtil.writeIntoPropertiesFile(urlMap,
					webCrawlerConfiguration.getOutputDirectory(),
					CrawlerConstants.DEFAULT_MAPPINGS_FILENAME);
		}
	}
}
