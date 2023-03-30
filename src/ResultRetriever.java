import java.util.Map;
import java.util.concurrent.Future;

public interface ResultRetriever {
    public Map<String, Integer> getResult(String corpusName);
    public Map<? extends Object, ? extends Object> queryResult(String corpusName);
    public void clearSummary(ScanType summaryType);
    public Map<String, Map<String, Integer>> getSummary(ScanType summaryType);
    public Map<String, Map<String, Integer>> querySummary(ScanType summaryType);
    public void addCorpusResult(String corpusName, Future<Map<String, Map<String, Integer>>> corpusResult);
}

