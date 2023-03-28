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
                System.out.println(((FileJob)job).getDir().getName());
                Main.fileScanner.scanDir(((FileJob)job).getDir());
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
