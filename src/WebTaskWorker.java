import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class WebTaskWorker implements Callable {

    private String url;
    private WebJob job;
    private Map<String, Integer> map;
    public WebTaskWorker(WebJob job, Map<String, Integer> map) {
        this.url = job.getUrl();
        this.job = job;
        this.map = map;
    }

    @Override
    public Object call() throws Exception {
        System.out.println("krenuo da radi " + job.getUrl());
        Document doc = Jsoup.connect(url).get();
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            if(!WebScanner.urls.contains(link.attr("abs:href"))){
                if (job.getHopCount() > 0) {
                    Main.blockingQueue.put(new WebJob(ScanType.WEB, link.attr("abs:href"), job.getHopCount() - 1, job));
                    job.setHopCount(job.getHopCount() - 1);
                }
                WebScanner.urls.add(link.attr("abs:href"));
            }
        }

        String words[] = doc.body().text().split(" ");
        for (String word: words){
            if(Main.keywords.contains(word)){
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
