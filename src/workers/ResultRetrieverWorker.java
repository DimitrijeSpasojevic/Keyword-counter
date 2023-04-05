package workers;

import jobs.ScanType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ResultRetrieverWorker implements Callable {

    private ScanType summaryType;
    private Map<String, Future<Map<String, Map<String, Integer>>>> corpusResults;
    private Map<String, Map<String, Integer>> mapResult;
    private boolean async;
    public ResultRetrieverWorker(Map<String, Future<Map<String, Map<String, Integer>>>> corpusResults, ScanType summaryType, boolean async) {
        this.summaryType = summaryType;
        this.corpusResults = corpusResults;
        this.mapResult = new HashMap<>();
        this.async = async;
    }

    @Override
    public Object call() throws Exception {
        Map<String, Future<Map<String, Map<String, Integer>>>> filteredCorpusResults = null;
        if (summaryType.equals(ScanType.FILE)){
            filteredCorpusResults = filterCorpusResultsByPrefix("file|");
        }else if (summaryType.equals(ScanType.WEB)){
            filteredCorpusResults = filterCorpusResultsByPrefix("web|");
        }

        for (Future<Map<String, Map<String, Integer>>> mapFuture : filteredCorpusResults.values())
        {
            if (async && !mapFuture.isDone()){
                mapResult.put("Nezavrsen", new HashMap<>());
                continue;
            }
            Map<String, Map<String, Integer>> map = mapFuture.get();
            for (String s : map.keySet()){ //samo jedna
                mapResult.put(s, map.get(s));

//                for (String keyWord : kvMap.keySet()){
//                    if(!mapResult.containsKey(keyWord)){
//                        mapResult.put(keyWord, kvMap.get(keyWord));
//                    }else {
//                        mapResult.put(keyWord, mapResult.get(keyWord) + kvMap.get(keyWord));
//                    }
//                }
            }
        }
        return mapResult;
    }
    
    private Map<String, Future<Map<String, Map<String, Integer>>>> filterCorpusResultsByPrefix(String prefix){
        return corpusResults.entrySet().stream()
                .filter(stringFutureEntry -> stringFutureEntry.getKey().startsWith(prefix))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
