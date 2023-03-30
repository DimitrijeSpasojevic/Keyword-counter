import java.util.*;
import java.util.concurrent.*;

public class ResultRetrieverImpl implements ResultRetriever{

    ExecutorService pool = Executors.newCachedThreadPool();
    ExecutorCompletionService<Map<String,Integer>> results = new ExecutorCompletionService<>(pool);
    Map<String, Future<Map<String, Map<String, Integer>>>> corpusResults = new HashMap<>();
    @Override
    public Map<String, Integer> getResult(String corpusName) {
        Map<String, Map<String, Integer>> res;
        try {
            if(corpusResults.containsKey(corpusName))
                res = corpusResults.get(corpusName).get();
            else {
                System.out.println("Ne postoji corpus za zadatim imenom");
                return null;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return res.get(corpusName);
    }

    @Override
    public Map<? extends Object, ? extends Object> queryResult(String corpusName) {
        Map<String, Map<String, Integer>> res = null;
        try {
            if (corpusResults.containsKey(corpusName) && corpusResults.get(corpusName).isDone()){
                res = corpusResults.get(corpusName).get();
            }else {
                Map<Object, Object> notDoneRes = new HashMap<>();

                if (corpusResults.containsKey(corpusName)) notDoneRes.put(-1, "Postoji, ali jos nije zavrseno racunanje!");
                else notDoneRes.put(-1, "Korpus sa zadatom putanjom ne postoji");

                return notDoneRes;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return res.get(corpusName);
    }

    @Override
    public void clearSummary(ScanType summaryType) {

    }

    @Override
    public Map<String, Map<String, Integer>> getSummary(ScanType summaryType) {
        return null;
    }

    @Override
    public Map<String, Map<String, Integer>> querySummary(ScanType summaryType) {
        return null;
    }

    @Override
    public void addCorpusResult(String corpusName, Future<Map<String, Map<String, Integer>>> corpusResult) {
        this.corpusResults.put(corpusName, corpusResult);
    }
}
