import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class FileScanner {

    ForkJoinPool pool = new ForkJoinPool();
    private Map<String, Map<String, Integer>> map;

    public void scanDir(File dir){
        map = new ConcurrentHashMap<>();
        map.put(dir.getName(), new ConcurrentHashMap<>());
        File[] directoryListing = dir.listFiles();
        System.out.println("Starting file scan for file|" + dir.getName());
        Future<Map<String, Map<String, Integer>>> corpusResult = pool.submit(new FileTaskWorker(directoryListing,0,directoryListing.length, map, dir));

        Main.resultRetriever.addCorpusResult(dir.getName(), corpusResult);
    }
}
