package jobs;


import java.io.File;

public class FileJob extends Job {
    private File dir;

    public FileJob(File dir) {
        super(ScanType.FILE);
        this.dir = dir;
    }

    public File getDir() {
        return dir;
    }
}
