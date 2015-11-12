package com.chinappa.information.retrieval.configuration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicLong;

import com.chinappa.information.retrieval.constants.CrawlerConstants;

public class WebCrawlerConfiguration {

	private static WebCrawlerConfiguration uniqueInstance = null;

	/**
	 * The following attribute stores the name of the input directory. This is
	 * the place where the seed file should be present.
	 */
	private String inputDirectory = CrawlerConstants.DEFAULT_INPUT_DIRECTORY;
	/**
	 * The following attribute stores the name of the output directory where the
	 * files will be downloaded.
	 */
	private String outputDirectory = CrawlerConstants.DEFAULT_OUTPUT_DIRECTORY;
	/**
	 * The following attribute stores the name of the seed file
	 */
	private String seedFileName = CrawlerConstants.DEFAULT_SEED_FILENAME;
	/**
	 * The following attribute represents the total number of threads used for
	 * crawling.
	 */
	private int threadCount = CrawlerConstants.DEFAULT_NUMBER_OF_THREADS;
	/**
	 * The following attribute denotes the height from the root node.
	 */
	private int levels = CrawlerConstants.DEFAULT_LEVELS;
	/**
	 * The following attribute represents if the compression is enabled.
	 */
	private boolean isCompressionEnabled = false;
	/**
	 * The following attribute represents if the document detection is enabled.
	 */
	private boolean isDuplicateDetectionEnabled = false;
	/**
	 * The following attribute represents the number of pages to be crawled.
	 */
	private AtomicLong pageLimit = new AtomicLong(
			CrawlerConstants.DEFAULT_MAXIMUM_NUMBER_OF_PAGES_TO_DOWNLOAD);

	/**
	 * Default constructor
	 */
	private WebCrawlerConfiguration() {
		init();
	}

	/**
	 * The following method returns the unique instance.
	 * 
	 * @return
	 */
	public static WebCrawlerConfiguration getInstance() {
		if (uniqueInstance == null) {
			synchronized (WebCrawlerConfiguration.class) {
				if (uniqueInstance == null) {
					uniqueInstance = new WebCrawlerConfiguration();
				}
			}
		}
		return uniqueInstance;
	}

	/**
	 * The following method initializes the parameters required to run the
	 * crawler. The parameters are read from a property file stored in config
	 * folder. In case of a missing parameter or any errors while reading, the
	 * parameters are initialized with their default values.
	 */
	private void init() {
		ResourceBundle rb = ResourceBundle.getBundle("root");

		Integer intValue = null;
		String stringValue = null;
		Boolean booleanValue = null;

		String param = CrawlerConstants.RB_NUMBER_OF_THREADS;
		intValue = readIntegerFromResourceBundle(rb, param);
		if (intValue != null) {
			threadCount = intValue;
		}

		param = CrawlerConstants.RB_LEVELS;
		intValue = readIntegerFromResourceBundle(rb, param);
		if (intValue != null) {
			levels = intValue;
		}

		param = CrawlerConstants.RB_PAGE_LIMIT;
		Long longValue = readLongFromResourceBundle(rb, param);
		if (longValue != null) {
			pageLimit.set(longValue);
		}

		param = CrawlerConstants.RB_INPUT_DIRECTORY;
		stringValue = readStringFromResourceBundle(rb, param);
		if (stringValue != null) {
			inputDirectory = stringValue;
		}

		File directory = new File(inputDirectory);
		if (!directory.exists()) {
			directory.mkdir();
		}

		param = CrawlerConstants.RB_OUTPUT_DIRECTORY;
		stringValue = readStringFromResourceBundle(rb, param);
		if (stringValue != null) {
			outputDirectory = stringValue;
		}

		directory = new File(outputDirectory);
		if (!directory.exists()) {
			directory.mkdir();
		}

		param = CrawlerConstants.RB_SEED_FILENAME;
		stringValue = readStringFromResourceBundle(rb, param);
		if (stringValue != null) {
			seedFileName = stringValue;
		}

		File seedFile = new File(inputDirectory + File.separator + seedFileName);
		if (!seedFile.exists()) {
			try {
				seedFile.createNewFile();
				FileWriter fileWriter = new FileWriter(
						seedFile.getAbsoluteFile());
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				bufferedWriter.write(CrawlerConstants.DEFAULT_SEED_VALUE);
				bufferedWriter.close();
			} catch (IOException e) {
			}
		}

		param = CrawlerConstants.RB_COMPRESSION_ENABLED;
		booleanValue = readBooleanFromResourceBundle(rb, param);
		if (booleanValue != null) {
			isCompressionEnabled = booleanValue;
		}

		param = CrawlerConstants.RB_DUPLICATE_DETECTION_ENABLED;
		booleanValue = readBooleanFromResourceBundle(rb, param);
		if (booleanValue != null) {
			isDuplicateDetectionEnabled = booleanValue;
		}
	}

	/**
	 * The following method reads the property file and returns corresponding
	 * {@link Long} value.
	 * 
	 * @param rb
	 * @param param
	 * @return
	 */
	private Long readLongFromResourceBundle(ResourceBundle rb, String param) {
		Long variable = null;
		try {
			String temp = rb.getString(param);
			if (temp != null) {
				if (!temp.trim().isEmpty()) {
					try {
						variable = Long.parseLong(temp);
						return variable;
					} catch (IllegalArgumentException e) {
						showInfoMessage(param);
					}
				} else {
					showInfoMessage(param);
				}
			} else {
				showInfoMessage(param);
			}
		} catch (MissingResourceException e) {
			showInfoMessage(param);
		}
		return variable;
	}

	/**
	 * The following method reads the property file and returns corresponding
	 * {@link Integer} value.
	 * 
	 * @param rb
	 * @param param
	 * @return
	 */
	private Integer readIntegerFromResourceBundle(ResourceBundle rb,
			String param) {
		Integer variable = null;
		try {
			String temp = rb.getString(param);
			if (temp != null) {
				if (!temp.trim().isEmpty()) {
					try {
						variable = Integer.parseInt(temp);
						return variable;
					} catch (IllegalArgumentException e) {
						showInfoMessage(param);
					}
				} else {
					showInfoMessage(param);
				}
			} else {
				showInfoMessage(param);
			}
		} catch (MissingResourceException e) {
			showInfoMessage(param);
		}
		return variable;
	}

	/**
	 * The following method reads the property file and returns corresponding
	 * {@link String} value.
	 * 
	 * @param rb
	 * @param param
	 * @return
	 */
	private String readStringFromResourceBundle(ResourceBundle rb, String param) {

		String variable = null;
		try {
			String temp = rb.getString(param);
			if (temp != null) {
				if (!temp.trim().isEmpty()) {
					variable = temp;
					return variable;
				} else {
					showInfoMessage(param);
				}
			} else {
				showInfoMessage(param);
			}
		} catch (MissingResourceException e) {
			showInfoMessage(param);
		}
		return variable;
	}

	/**
	 * The following method reads the property file and returns corresponding
	 * {@link Boolean} value.
	 * 
	 * @param rb
	 * @param param
	 * @return
	 */
	private Boolean readBooleanFromResourceBundle(ResourceBundle rb,
			String param) {

		Boolean variable = null;
		try {
			String temp = rb.getString(param);
			if (temp != null) {
				if (!temp.trim().isEmpty()) {
					variable = Boolean.parseBoolean(temp);
				} else {
					showInfoMessage(param);
				}
			} else {
				showInfoMessage(param);
			}
		} catch (MissingResourceException e) {
			showInfoMessage(param);
		}
		return variable;
	}

	/**
	 * The following method is used to display messages for information purpose
	 * only.
	 * 
	 * @param param
	 */
	private void showInfoMessage(String param) {
		System.out.println("Incorrect value found for " + param + " parameter");
		System.out.println("Setting it to dafault value..");
	}

	/**
	 * Getter for inputDirectory
	 * 
	 * @return
	 */
	public String getInputDirectory() {
		return inputDirectory;
	}

	/**
	 * Getter for outputDirectory
	 * 
	 * @return
	 */
	public String getOutputDirectory() {
		return outputDirectory;
	}

	/**
	 * Getter for seedFileName
	 * 
	 * @return
	 */
	public String getSeedFileName() {
		return seedFileName;
	}

	/**
	 * Getter for threadCount
	 * 
	 * @return
	 */
	public int getThreadCount() {
		return threadCount;
	}

	/**
	 * Getter for levels
	 * 
	 * @return
	 */
	public int getLevels() {
		return levels;
	}

	/**
	 * Getter for isCompressionEnabled
	 * 
	 * @return
	 */
	public boolean isCompressionEnabled() {
		return isCompressionEnabled;
	}

	/**
	 * Getter for isDuplicateDetectionEnabled
	 * 
	 * @return
	 */
	public boolean isDuplicateDetectionEnabled() {
		return isDuplicateDetectionEnabled;
	}

	public AtomicLong getPageLimit() {
		return pageLimit;
	}
}
