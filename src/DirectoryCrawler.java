import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectoryCrawler extends Thread{

    private String pathToDir;
    private List<String> keywords;
    private String fileCorpusPrefix;
    private Integer dirCrawlerSleepTime;
    private Integer fileScanningSizeLimit;
    private Map<String, Long> mapLastModified;

    public DirectoryCrawler(String pathToDir, List<String> keywords, String fileCorpusPrefix, Integer dirCrawlerSleepTime, Integer fileScanningSizeLimit) {
        this.pathToDir = pathToDir;
        this.keywords = keywords;
        this.fileCorpusPrefix = fileCorpusPrefix;
        this.dirCrawlerSleepTime = dirCrawlerSleepTime;
        this.fileScanningSizeLimit = fileScanningSizeLimit;
        this.mapLastModified = new HashMap<>();
    }

    @Override
    public void run() {
        Path dir = Paths.get(pathToDir);
        try {
            Files.walk(dir).forEach(path -> {
                try {
                    showFile(path.toFile());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void showFile(File file) throws InterruptedException {
        if (file.isDirectory() && file.getName().startsWith(this.fileCorpusPrefix)) {
            boolean dirIsSame = true;
            File[] directoryListing = file.listFiles();
            if (directoryListing != null) {
                for (File child : directoryListing) {
                    if((mapLastModified.get(child.getName()) == null) || (mapLastModified.get(child.getName()) != child.lastModified())){
                        mapLastModified.put(child.getName(),child.lastModified());
                        dirIsSame = false;
                    }
                }
            }
            if(dirIsSame == false){
                FileJob fileJob = new FileJob(file);
                Main.blockingQueue.put(fileJob);
            }
        }
    }
}
