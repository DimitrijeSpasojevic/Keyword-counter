import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class WebScanner {
    //pravimo pool, koji ce da nam daje niti
    ExecutorService pool = Executors.newCachedThreadPool();
    private Map<String, Integer> map;
    static List<String> urls;
    //completion service koji ce da nam daje rezultate niti
    ExecutorCompletionService<Map<String,Integer>> results = new ExecutorCompletionService<>(pool);

    public WebScanner() {
        urls = Collections.synchronizedList(new ArrayList<>());
        Main.scheduler.scheduleAtFixedRate(() -> {
//            System.out.println("urls " + urls);
            urls.clear();
            }, Main.url_refresh_time, Main.url_refresh_time, TimeUnit.MILLISECONDS);
    }

    public void scanWeb(WebJob job){
        map = new ConcurrentHashMap<>();
        results.submit(new WebTaskWorker(job,map));

        try {
            //uzimanje rezultata iz queue-a od copletion service-a
            Future<Map<String,Integer>> result = results.take();

            for(int i =0; i<Main.keywords.size(); i++){ //ispis svih rezultata
                System.out.println(Main.keywords.get(i));
                System.out.println(result.get().get(Main.keywords.get(i)));
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
//        pool.shutdown();
}
