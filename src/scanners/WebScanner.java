package scanners;

import jobs.Job;
import jobs.WebJob;
import utils.ResultRetriever;
import workers.WebTaskWorker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class WebScanner {
    //pravimo pool, koji ce da nam daje niti
    ExecutorService pool = Executors.newCachedThreadPool();
    private Map<String, Integer> map;
    public static List<String> urls;
    //completion service koji ce da nam daje rezultate niti
    ExecutorCompletionService<Map<String,Integer>> results = new ExecutorCompletionService<>(pool);
    private ResultRetriever resultRetriever;
    private List<String> keywords;
    private Integer url_refresh_time;
    private ScheduledExecutorService scheduler;
    private BlockingQueue<Job> blockingQueue;

    public WebScanner(ResultRetriever resultRetriever, List<String> keywords, Integer url_refresh_time, ScheduledExecutorService scheduler, BlockingQueue<Job> blockingQueue) {
        this.resultRetriever = resultRetriever;
        this.keywords = keywords;
        this.scheduler = scheduler;
        this.blockingQueue = blockingQueue;
        this.url_refresh_time = url_refresh_time;
        urls = Collections.synchronizedList(new ArrayList<>());
        this.scheduler.scheduleAtFixedRate(() -> {
//            System.out.println("urls " + urls);
            urls.clear();
            }, this.url_refresh_time, this.url_refresh_time, TimeUnit.MILLISECONDS);
    }

    public void scanWeb(WebJob job){
        map = new ConcurrentHashMap<>();
        results.submit(new WebTaskWorker(job,map, blockingQueue,keywords));

        try {
            //uzimanje rezultata iz queue-a od copletion service-a
            Future<Map<String,Integer>> result = results.take();

            for(int i = 0; i< keywords.size(); i++){ //ispis svih rezultata
                System.out.println(keywords.get(i));
                System.out.println(result.get().get(keywords.get(i)));
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void stopPool() {
        pool.shutdown();
        System.out.println("Ugasio se webScanner Pool");
    }
}
