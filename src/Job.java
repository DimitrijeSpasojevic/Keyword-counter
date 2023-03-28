import java.util.Map;
import java.util.concurrent.Future;

public class Job implements ScanningJob{

    private ScanType type;
    private String query;

    public Job(ScanType type) {
        this.type = type;
    }

    @Override
    public ScanType getType() {
        return this.type;
    }

    @Override
    public String getQuery() {
        return this.query;
    }

    @Override
    public Future<Map<String, Integer>> initiate() {
        return null;
    }
}
