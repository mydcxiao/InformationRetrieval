import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.lang.StringBuilder;
import java.lang.String;

public class Controller {
    public static void main(String[] args) throws Exception {
        String crawlStorageFolder = "data/crawl";
        int numberOfCrawlers = 16;
        int maxDepthOfCrawling = 16;
        int maxPagesToFetch = 20000;
        int politenessDelay = 50;
        String seed = "https://www.nytimes.com/";

        CrawlConfig config = new CrawlConfig();
        config.setUserAgentString("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
        config.setIncludeBinaryContentInCrawling(true);
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setMaxDepthOfCrawling(maxDepthOfCrawling);
        config.setMaxPagesToFetch(maxPagesToFetch);
        config.setPolitenessDelay(politenessDelay);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        controller.addSeed(seed);
        controller.start(NYTCrawler.class, numberOfCrawlers);

        StringBuilder fetchUrls = new StringBuilder("URL,Status\n");
        StringBuilder files = new StringBuilder("URL,Size(Bytes),#Outlinks,Type\n");
        StringBuilder allUrls = new StringBuilder("URL,Indicator\n");

        for (Object o : controller.getCrawlersLocalData()) {
            String[] data = (String[]) o;
            fetchUrls.append(data[0]);
            files.append(data[1]);
            allUrls.append(data[2]);
        }

        writeToCSV(fetchUrls, "fetch_nytimes.csv");
        writeToCSV(files, "visit_nytimes.csv");
        writeToCSV(allUrls, "urls_nytimes.csv");
    }
        private static void writeToCSV(StringBuilder sb, String filename) throws IOException {
            PrintWriter writer = new PrintWriter(filename, StandardCharsets.UTF_8);
            writer.print(sb.toString().trim());
            writer.flush();
            writer.close();
        }
    }