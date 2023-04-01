package workers;

import jobs.Job;
import jobs.ScanType;
import jobs.WebJob;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import scanners.WebScanner;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

public class WebTaskWorker implements Callable {

    private String url;
    private WebJob job;
    private Map<String, Integer> map;
    private BlockingQueue<Job> blockingQueue;
    public List<String> keywords;
    public WebTaskWorker(WebJob job, Map<String, Integer> map, BlockingQueue<Job> blockingQueue, List<String> keywords) {
        this.url = job.getUrl();
        this.job = job;
        this.map = map;
        this.blockingQueue = blockingQueue;
        this.keywords = keywords;
    }

    @Override
    public Object call() throws Exception {
        System.out.println("krenuo da radi " + job.getUrl());
        Document doc = Jsoup.connect(url).get();
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            if(!WebScanner.urls.contains(link.attr("abs:href"))){
                if (job.getHopCount() > 0) {
                    blockingQueue.put(new WebJob(ScanType.WEB, link.attr("abs:href"), job.getHopCount() - 1, job));
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
        return map;
    }
}
