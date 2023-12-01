import edu.uci.ics.crawler4j.crawler.*;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.HashSet;

public class NYTCrawler extends WebCrawler {
    
        private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js"
                                                            +"|mp3|mp4|zip|gz"
                                                            +"|json|webmanifest|ttf"
                                                            +"|svg|wav|avi|mov|mpeg|mpg|ram"
                                                            +"|m4v|wma|wmv|mid|mp2"
                                                            +"|rar|exe|ico))$");
        private final static String siteDomain = "nytimes.com";
        private final HashSet<String> visited = new HashSet<>();

        private String fetchUrls = "";
        private String files = "";
        private String allUrls = "";

    
        @Override
        public boolean shouldVisit(Page referringPage, WebURL url) {
            String href = getHref(url.getURL());
            if(href.startsWith(siteDomain)) {
                allUrls += href + ",OK\n";
            } else {
                allUrls += href + ",N_OK\n";
            }
            return !FILTERS.matcher(href).matches()
                && href.startsWith(siteDomain) && !visited.contains(href);
        }
    
        @Override
        public void visit(Page page) {
            String url = page.getWebURL().getURL().replaceAll(",","_");
            // only visit HTML, doc, pdf and different image format URLs
            String contentType = page.getContentType().split(";")[0];
            if(!contentType.contains("text/html") && 
               !contentType.contains("application/msword") && 
               !contentType.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document") &&
               !contentType.contains("application/pdf") && 
               !contentType.contains("image/")) {
                return;
            }
            int numOutgoingUrls = 0;
            if (page.getParseData() instanceof HtmlParseData) {
                HtmlParseData htmlParseData = (HtmlParseData)page.getParseData();
                Set<WebURL> links = htmlParseData.getOutgoingUrls();
                numOutgoingUrls = links.size();
            }
            files += url + "," + page.getContentData().length + "," + numOutgoingUrls + "," + contentType + "\n";
        }

        @Override
        protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
            String url = webUrl.getURL().replaceAll(",","_");
            String href = getHref(url);
            fetchUrls += url + "," + statusCode + "\n";
            visited.add(href);
        }

        @Override
        public Object getMyLocalData() {
            return new String[] {fetchUrls, files, allUrls};
        }

        private String getHref(String url) {
            return url.toLowerCase().replaceAll(",","_").replaceFirst("^(https?://)?(www.)?", "").replaceAll("/+$", "");
        }
}