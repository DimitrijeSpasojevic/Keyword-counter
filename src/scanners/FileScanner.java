package scanners;

import utils.ResultRetriever;
import workers.FileTaskWorker;

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
    private ResultRetriever resultRetriever;

    private Integer file_scanning_size_limit;

    private List<String> keywords;


    public FileScanner(ResultRetriever resultRetriever, Integer file_scanning_size_limit, List<String> keywords) {
        this.resultRetriever = resultRetriever;
        this.file_scanning_size_limit = file_scanning_size_limit;
        this.keywords = keywords;
        this.map = new ConcurrentHashMap<>();
    }

    public void scanDir(File dir){
        map.put(dir.getName(), new ConcurrentHashMap<>());
        File[] directoryListing = dir.listFiles();
        System.out.println("Starting file scan for file|" + dir.getName());
        Future<Map<String, Map<String, Integer>>> corpusResult = pool.submit(new FileTaskWorker(directoryListing,0,directoryListing.length, map, dir, file_scanning_size_limit, keywords));

        resultRetriever.addCorpusResult("file|" + dir.getName(), corpusResult);
    }

    public void stopPool() {
        pool.shutdown();
        System.out.println("Ugasio se fileScanner Pool");
    }
}
