package jobs;

public class WebJob extends Job {

    private String url;
    private Integer hopCount;

    private String parentDomain;

    public WebJob(ScanType type, String url, Integer hopCount, String parentDomain) {
        super(type);
        this.url = url;
        this.hopCount = hopCount;
        this.parentDomain = parentDomain;
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

    public String getParentDomain() {
        return parentDomain;
    }

    public void setParentDomain(String parentDomain) {
        this.parentDomain = parentDomain;
    }
}
