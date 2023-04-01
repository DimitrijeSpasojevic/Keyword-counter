package utils;

import jobs.ScanType;

import java.util.*;
import java.util.concurrent.*;

public class ResultRetrieverImpl implements ResultRetriever{

    ExecutorService pool = Executors.newCachedThreadPool();
    ExecutorCompletionService<Map<String,Integer>> results = new ExecutorCompletionService<>(pool);
    Map<String, Future<Map<String, Map<String, Integer>>>> corpusResults = new ConcurrentHashMap<>();

    private Map<String,Integer> mapSummaryFile = new ConcurrentHashMap<>();
    private Map<String,Integer> mapSummaryWeb = new ConcurrentHashMap<>();

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

    }

    @Override
    public Map<String, Map<String, Integer>> getSummary(ScanType summaryType) {
        if (summaryType.equals(ScanType.FILE)){
            for (var entry : corpusResults.entrySet()) {

            }
        }else if (summaryType.equals(ScanType.WEB)){
            for (var entry : corpusResults.entrySet()) {

            }
        }


        return null;
    }

    @Override
    public Map<String, Map<String, Integer>> querySummary(ScanType summaryType) {
        return null;
    }

    @Override
    public void addCorpusResult(String query, Future<Map<String, Map<String, Integer>>> corpusResult) {
        this.corpusResults.put(query, corpusResult);
    }
}
