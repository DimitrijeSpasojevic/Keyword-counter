package jobs;

public class WebJob extends Job {

    private String url;
    private Integer hopCount;
    private WebJob parentJob;

    public WebJob(ScanType type, String url, Integer hopCount, WebJob parentJob) {
        super(type);
        this.url = url;
        this.hopCount = hopCount;
        this.parentJob = parentJob;
    }

    public String getUrl() {
        return url;
    }

    public Integer getHopCount() {
        return hopCount;
    }

    public void setHopCount(Integer hopCount) {
        this.hopCount = hopCount;
    }
}
