####################################################################################################################
# How to run
####################################################################################################################
- Download JDK 8
- Make sure you have added java in your PATH
- Go to the crawler folder and run crawler.sh in linux or crawler.bat in windows
  (can simply execute the executable jar in windows)
- root_en.properties file inside the config folder contains all the input parameters
	|- threads: number of crawler threads (Default value: 16)
	|- levels: distance from the root node (Default value: 5)
	|- page.limit: page limit (Default value: max limit of Long in JAVA)
	|- input.directory: Set the path of input directory with seed file (Default value: input)
	|- output.directory: Set the path of output directory (Default value: output)
	|- compression.enabled: set true if compression needs to be enabled (Default value: false)
	|- duplicate.detection.enabled: set true if duplicate detection needs to be enabled (Default value: false)

In case the values are not set or in incorrect format default vaues are assumed.
Each entry in the seed file needs to be on a new line.
