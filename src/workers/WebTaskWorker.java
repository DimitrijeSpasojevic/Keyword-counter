package workers;

import jobs.Job;
import jobs.ScanType;
import jobs.WebJob;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import scanners.WebScanner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

public class WebTaskWorker implements Callable {

    private String url;
    private WebJob job;
    private Map<String, Map<String, Integer>> mapFinalResult;
    private Map<String, Integer> map;
    private BlockingQueue<Job> blockingQueue;
    public List<String> keywords;

    public WebTaskWorker(WebJob job, Map<String, Map<String, Integer>> map, BlockingQueue<Job> blockingQueue, List<String> keywords) {
        this.url = job.getUrl();
        this.job = job;
        this.mapFinalResult = map;
        this.map = mapFinalResult.get(job.getParentDomain());
        this.blockingQueue = blockingQueue;
        this.keywords = keywords;
    }

    @Override
    public Object call() throws InterruptedException {
        System.out.println("krenuo da radi " + job.getUrl());
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (Exception e) {
            System.out.println("Problem sa url-om");
            map.put("Los url",-1);
            return mapFinalResult;
        }
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            if(!WebScanner.urls.contains(link.attr("abs:href"))){
                if (job.getHopCount() > 0) {
                    String url = link.attr("abs:href");
                    String domainName = null;
                    try {
                        domainName = getDomainName(url);
                        System.out.println("radim domen " + domainName);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                    blockingQueue.put(new WebJob(ScanType.WEB, url, job.getHopCount() - 1, domainName));
                    job.setHopCount(job.getHopCount() - 1);
                }
                WebScanner.urls.add(link.attr("abs:href"));
            }
        }

        String words[] = doc.body().text().split(" ");
        for (String word: words){
            if(keywords.contains(word)){
                if(map.containsKey(word)){
                    map.put(word,map.get(word) + 1);
                }else {
                    map.put(word,1);
                }
            }
        }
        return mapFinalResult;
    }

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }
}
