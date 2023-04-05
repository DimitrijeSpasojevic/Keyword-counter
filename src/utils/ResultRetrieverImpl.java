package utils;

import jobs.ScanType;
import workers.ResultRetrieverWorker;
import java.util.*;
import java.util.concurrent.*;

public class ResultRetrieverImpl implements ResultRetriever{

    ExecutorService pool = Executors.newCachedThreadPool();
    ExecutorCompletionService<Map<String, Map<String,Integer>>> results = new ExecutorCompletionService<>(pool);
    Map<String, Future<Map<String, Map<String, Integer>>>> corpusResults = new ConcurrentHashMap<>();

    private Map<String, Map<String,Integer>> webSumRes = new ConcurrentHashMap<>();
    private volatile boolean changedWebSum = false;
    private volatile boolean changedFileSum = false;
    private Map<String, Map<String,Integer>> fileSumRes = new ConcurrentHashMap<>();

    @Override
    public Map<String, Integer> getResult(String query) {
        Map<String, Map<String, Integer>> res;
        try {
            if(corpusResults.containsKey(query))
                res = corpusResults.get(query).get();
            else {
                System.out.println("Ne postoji corpus za zadatim imenom");
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        if (query.startsWith("file")){
            query = query.substring(5);
        } else if (query.startsWith("web")){
            query = query.substring(4);
        }
        return res.get(query);
    }

    @Override
    public Map<? extends Object, ? extends Object> queryResult(String query) {
        Map<String, Map<String, Integer>> res = null;
        try {
            if (corpusResults.containsKey(query) && corpusResults.get(query).isDone()){
                res = corpusResults.get(query).get();
            }else {
                Map<Object, Object> notDoneRes = new HashMap<>();

                if (corpusResults.containsKey(query)) notDoneRes.put(-1, "Postoji, ali jos nije zavrseno racunanje!");
                else notDoneRes.put(-1, "Korpus sa zadatom putanjom ne postoji");

                return notDoneRes;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        if (query.startsWith("file")){
            query = query.substring(5);
        } else if (query.startsWith("web")){
            query = query.substring(4);
        }
        return res.get(query);
    }

    @Override
    public void clearSummary(ScanType summaryType) {
        if (summaryType.equals(ScanType.WEB)){
            webSumRes.clear();
            changedWebSum = true;
            System.out.println("obrisan web summary");
        }else {
            fileSumRes.clear();
            changedFileSum = true;
            System.out.println("obrisan file summary");
        }
    }

    @Override
    public Map<String, Map<String, Integer>> summary(ScanType summaryType, boolean async){
        if(summaryType.equals(ScanType.FILE) && !changedFileSum){
            return fileSumRes;
        }else if (summaryType.equals(ScanType.WEB) && !changedWebSum){
            return webSumRes;
        }
        results.submit(new ResultRetrieverWorker(corpusResults, summaryType, async));
        try {
            Future<Map<String, Map<String, Integer>>> mapFuture = results.take();
            if(summaryType.equals(ScanType.FILE))
            {
                fileSumRes = mapFuture.get();
                changedFileSum = false;
                return fileSumRes;
            }
            else
            {
                webSumRes = mapFuture.get();
                changedWebSum = false;
                return webSumRes;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addCorpusResult(String query, Future<Map<String, Map<String, Integer>>> corpusResult) {
        if(query.startsWith("web|"))
            changedWebSum = true;
        else if (query.startsWith("file|")) {
            changedFileSum = true;
        }
        this.corpusResults.put(query, corpusResult);
    }

    @Override
    public void stop() {
        pool.shutdown();
        System.out.println("Result retriever stopiran");
    }

}
