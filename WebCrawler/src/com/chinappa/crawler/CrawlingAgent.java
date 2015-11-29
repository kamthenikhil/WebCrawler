package com.chinappa.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.chinappa.crawler.configuration.WebCrawlerConfiguration;
import com.chinappa.crawler.constant.CrawlerConstants;
import com.chinappa.crawler.util.DuplicateDocumentDetectionUtil;
import com.chinappa.crawler.util.RobotExclusionUtil;
import com.chinappa.information.retrieval.constant.CommonConstants;
import com.chinappa.information.retrieval.util.FileHandlerUtil;

public class CrawlingAgent implements Runnable {

	public static AtomicInteger fileno = new AtomicInteger(0);

	private ConcurrentHashMap<String, String> urlMap;
	/**
	 * Stores the list of incoming URLs
	 */
	private ConcurrentHashMap<String, List<String>> inLinkMap = null;
	/**
	 * Stores count of all the outgoing links for all the urls being crawled.
	 */
	private ConcurrentHashMap<String, Integer> outLinkCount = null;

	private ConcurrentLinkedQueue<Runnable> threadQueue;

	public String urlString;

	private int level;

	private boolean isSuccess = false;

	/**
	 * Parameterized Constructor
	 * 
	 * @param urlMap
	 * @param threadQueue
	 * @param url
	 * @param level
	 */
	public CrawlingAgent(ConcurrentHashMap<String, String> urlMap,
			ConcurrentHashMap<String, List<String>> inLinkMap,
			ConcurrentHashMap<String, Integer> outLinkMap,
			ConcurrentLinkedQueue<Runnable> threadQueue, String url, int level) {
		this.urlMap = urlMap;
		this.inLinkMap = inLinkMap;
		this.outLinkCount = outLinkMap;
		this.threadQueue = threadQueue;
		this.urlString = url;
		this.level = level;
	}

	@Override
	public void run() {
		if (level > 0
				&& WebCrawlerConfiguration.getInstance().getPageLimit()
						.longValue() > 0) {
			level--;
			if (urlString != null) {
				try {
					urlString = urlString.replaceAll("(/*)$", "");
					URL url = new URL(urlString);
					boolean isNewURL = false;
					if (!url.getHost().toLowerCase()
							.endsWith(CrawlerConstants.EDU_SUFFIX)) {
						return;
					}
					synchronized (urlMap) {
						if (!urlMap.containsKey(urlString)) {
							urlMap.put(urlString, CommonConstants.EMPTY_STRING);
							isNewURL = true;
						}
					}
					if (isNewURL) {
						ArrayList<String> childURLs = writeFileAndfetchHyperLinks();
						if (childURLs != null && childURLs.size() > 0) {
							for (String childURL : childURLs) {
								threadQueue.add(new CrawlingAgent(urlMap,
										inLinkMap, outLinkCount, threadQueue,
										childURL, level));
								if (!inLinkMap.containsKey(childURLs)) {
									synchronized (inLinkMap) {
										if (!inLinkMap.containsKey(childURLs)) {
											inLinkMap.put(childURL,
													new ArrayList<String>());
										}
									}
								}
								inLinkMap.get(childURL).add(urlString);
							}
						}
					} else {
					}
				} catch (MalformedURLException e) {
				}
			}
		}
		if (isSuccess) {
			WebCrawlerConfiguration.getInstance().getPageLimit()
					.decrementAndGet();
		}
	}

	/**
	 * The following method is used to fetch the doc using jsoup and returns a
	 * list of child URLs. It also writes down the content of the HTML page in a
	 * file on a local system.
	 * 
	 * @param url
	 * @param level
	 * @return
	 */
	private ArrayList<String> writeFileAndfetchHyperLinks() {

		if (isValidURLProtocol(urlString)
				&& RobotExclusionUtil.robotsShouldFollow(urlString)) {
			WebCrawlerConfiguration webCrawlerConfiguration = WebCrawlerConfiguration
					.getInstance();
			String extension = fetchDocumentType(urlString);
			switch (extension) {
			case CrawlerConstants.HTTP_PROTOCOL_PREFIX:
				Document doc = null;
				ArrayList<String> childURLs = new ArrayList<String>();
				try {
					Connection jsoupConnection = Jsoup.connect(urlString)
							.timeout(5000);
					jsoupConnection.followRedirects(true);
					doc = jsoupConnection.get();
					String content = FileHandlerUtil.fetchDocumentText(doc)
							+ FileHandlerUtil.fetchAnchorText(doc);
					boolean processDoc = true;
					if (webCrawlerConfiguration.isDuplicateDetectionEnabled()) {
						// DuplicateDocumentDetectionUtil util = new
						// DuplicateDocumentDetectionUtil();
						if (DuplicateDocumentDetectionUtil.isDuplicateDocument(
								content, urlString)) {
							processDoc = false;
						}
					}
					if (processDoc) {
						int temp = fileno.addAndGet(1);
						urlMap.put(urlString, level + CommonConstants.FULL_STOP
								+ temp
								+ CrawlerConstants.OUTPUT_FILE_EXTENTION_HTML);
						isSuccess = true;
						if (webCrawlerConfiguration.isCompressionEnabled()) {
							FileHandlerUtil
									.writeToCompressedHTMLFile(
											doc.html(),
											webCrawlerConfiguration
													.getOutputDirectory(),
											level + CommonConstants.FULL_STOP
													+ temp,
											urlString,
											CrawlerConstants.OUTPUT_FILE_EXTENTION_HTML);
						} else {
							FileHandlerUtil
									.writeToFile(
											doc.html(),
											webCrawlerConfiguration
													.getOutputDirectory(),
											level + CommonConstants.FULL_STOP
													+ temp,
											urlString,
											CrawlerConstants.OUTPUT_FILE_EXTENTION_HTML);
						}
						if (level > 0)
							childURLs = fetchChildURLs(urlString, doc);
					} else {
					}
				} catch (IOException e) {
				} catch (IllegalArgumentException e) {
				}
				return childURLs;
			default:
				int temp = fileno.addAndGet(1);
				FileHandlerUtil.writeURLFileToDisk(urlString,
						webCrawlerConfiguration.getOutputDirectory(), level
								+ CommonConstants.FULL_STOP + temp, extension);
				urlMap.put(urlString, level + CommonConstants.FULL_STOP + temp
						+ extension);
				isSuccess = true;
				return null;
			}
		} else {
			inLinkMap.remove(urlString);
			outLinkCount.remove(urlString);
			return null;
		}
	}

	/**
	 * The following method is used to determine the document type from the URL
	 * string.
	 * 
	 * @param url
	 * @return
	 */
	private String fetchDocumentType(String url) {

		if (url.toLowerCase().endsWith(CrawlerConstants.PDF_SUFFIX)) {
			return CrawlerConstants.PDF_SUFFIX;
		} else if (url.toLowerCase().endsWith(CrawlerConstants.JPEG_SUFFIX)) {
			return CrawlerConstants.JPEG_SUFFIX;
		} else if (url.toLowerCase().endsWith(CrawlerConstants.JPG_SUFFIX)) {
			return CrawlerConstants.JPG_SUFFIX;
		} else if (url.toLowerCase().endsWith(CrawlerConstants.BMP_SUFFIX)) {
			return CrawlerConstants.BMP_SUFFIX;
		} else if (url.toLowerCase().endsWith(CrawlerConstants.GIF_SUFFIX)) {
			return CrawlerConstants.GIF_SUFFIX;
		} else {
			return CrawlerConstants.HTTP_PROTOCOL_PREFIX;
		}
	}

	/**
	 * The following method check if URL with a given protocol should be parsed.
	 * Currently we only parse HTTP URLs.
	 * 
	 * @param urlString
	 * @return
	 */
	private boolean isValidURLProtocol(String urlString) {

		try {
			URL url = new URL(urlString);
			switch (url.getProtocol().toString().toLowerCase()) {
			case CrawlerConstants.HTTP_PROTOCOL_PREFIX:
				return true;
			case CrawlerConstants.HTTPS_PROTOCOL_PREFIX:
				return false;
			case CrawlerConstants.FTP_PROTOCOL_PREFIX:
				return false;
			default:
				return false;
			}
		} catch (MalformedURLException e) {
			return false;
		}
	}

	/**
	 * The following method extracts all the hyper-links in the HTML document.
	 * It then runs the child URLs through a bunch of filters and finally
	 * returns the list eligible child URLs.
	 * 
	 * @param url
	 * @param doc
	 * @return
	 */
	private ArrayList<String> fetchChildURLs(String url, Document doc) {
		Elements elements = doc.select(CommonConstants.HTML_LINKS_HREF);
		ArrayList<String> childURLs = new ArrayList<String>();
		if (elements != null && elements.size() > 0) {
			for (Element element : elements) {
				String childURLString = element
						.attr(CrawlerConstants.HTML_HREF).trim();
				childURLString = childURLString.replaceAll("^/", "");
				if (!checkIfBookmark(childURLString)) {
					if (!childURLString
							.startsWith(CrawlerConstants.HTTP_PROTOCOL_PREFIX)) {
						if (childURLString.length() <= 1) {
							try {
								URL childURL = new URL(childURLString);
							} catch (MalformedURLException e) {
								break;
							}
						} else {
							childURLString = url
									+ CommonConstants.FORWARD_SLASH
									+ childURLString;
						}
					}
					childURLString.replaceAll("/$", "");
					if (isValidURLProtocol(childURLString)
							&& RobotExclusionUtil
									.robotsShouldFollow(childURLString)) {
						childURLs.add(childURLString);
					}
				}
			}
			outLinkCount.put(url, childURLs.size());
		}
		return childURLs;
	}

	/**
	 * The following method checks if the URL is a HTML bookmark.
	 * 
	 * @param childURL
	 * @return
	 */
	private boolean checkIfBookmark(String childURL) {

		if (childURL.startsWith(CommonConstants.HASH)) {
			return true;
		} else {
			return false;
		}
	}

}