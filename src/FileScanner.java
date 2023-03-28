import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class FileScanner {

    ForkJoinPool pool = new ForkJoinPool();
    private Map<String, Integer> map;

    public void scanDir(File dir){
        map = new ConcurrentHashMap<>();
        //future, koji nam je ustvari ceka i cuva rezultat, i tu submitujemo zadatke u pool
        File[] directoryListing = dir.listFiles();
        Future<Map<String, Integer>> corpusResult = pool.submit(new FileTaskWorker(directoryListing,0,directoryListing.length, map));

        try {
            //dohvatamo rezultat, i posto je future, blokiramo se ako jos nije gotov
            Map<String, Integer> corpus = corpusResult.get();

            for(int i =0; i<Main.keywords.size(); i++){ //ispis svih rezultata
                System.out.println(Main.keywords.get(i));
                System.out.println(corpus.get(Main.keywords.get(i)));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
