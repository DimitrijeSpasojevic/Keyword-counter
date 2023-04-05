package utils;

import jobs.FileJob;
import jobs.Job;
import jobs.ScanType;
import jobs.WebJob;
import scanners.FileScanner;
import scanners.WebScanner;

import java.util.concurrent.BlockingQueue;

public class JobDispatcher extends Thread{

    private FileScanner fileScanner;
    private WebScanner webScanner;

    private BlockingQueue<Job> blockingQueue;

    public JobDispatcher(FileScanner fileScanner, WebScanner webScanner, BlockingQueue<Job> blockingQueue) {
        this.fileScanner = fileScanner;
        this.webScanner = webScanner;
        this.blockingQueue = blockingQueue;
    }

    @Override
    public void run() {
        Job job = null;
        try {
            job = blockingQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        while (!job.getType().equals(ScanType.STOP)){

            if(job.getType().equals(ScanType.FILE)){
                fileScanner.scanDir(((FileJob)job).getDir());
            }else if(job.getType().equals(ScanType.WEB)){
                webScanner.scanWeb(((WebJob)job));
            }
            try {
                job = blockingQueue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        webScanner.stopPool();
        fileScanner.stopPool();
        System.out.println("Ugasio se job dispatcher");
    }
}
