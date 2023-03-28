public class JobDispatcher extends Thread{
    @Override
    public void run() {
        Job job = null;
        try {
            job = Main.blockingQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        while (!job.getType().equals(ScanType.STOP)){

            if(job.getType().equals(ScanType.FILE)){
                Main.fileScanner.scanDir(((FileJob)job).getDir());
            }else if(job.getType().equals(ScanType.WEB)){
                Main.webScanner.scanWeb(((WebJob)job));
            }

            try {
                job = Main.blockingQueue.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Zavrsio job dispatcher");
    }
}
