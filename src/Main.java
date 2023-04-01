import jobs.Job;
import jobs.ScanType;
import jobs.WebJob;
import scanners.FileScanner;
import scanners.WebScanner;
import utils.DirectoryCrawler;
import utils.JobDispatcher;
import utils.ResultRetriever;
import utils.ResultRetrieverImpl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.*;

public class Main {
    public static List<String> keywords;
    public static String file_corpus_prefix;
    public static Integer dir_crawler_sleep_time;
    public static Integer file_scanning_size_limit;
    public static Integer hop_count;
    public static Integer url_refresh_time;
    public static FileScanner fileScanner;
    public static WebScanner webScanner;
    public static ResultRetriever resultRetriever;
    public static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    public static BlockingQueue<Job> blockingQueue = new LinkedBlockingDeque<>(10);

    public static void main(String[] args) throws IOException {
        readConfig();
        resultRetriever = new ResultRetrieverImpl();
        fileScanner = new FileScanner(resultRetriever,file_scanning_size_limit, keywords);
        webScanner = new WebScanner(resultRetriever,keywords,url_refresh_time, scheduler,blockingQueue);
        JobDispatcher jobDispatcher = new JobDispatcher(fileScanner, webScanner, blockingQueue);
        jobDispatcher.start();
        Scanner sc = new Scanner(System.in);
        System.out.println("Unesi komandu");

        String line = sc.nextLine();
        String command = "";
        while (!line.split(" ")[0].equals("stop")){
            String parts[] = line.split(" ");
            command = parts[0];
//            System.out.println("komanda je " + command);

            if(command.equals("ad") && parts.length == 2){
                addDirectory(parts[1]);
            }else if(command.equals("aw") && parts.length == 2){
                addWeb(parts[1]);
            }else if(command.equals("get") && parts.length == 2){
                String actions[] = parts[1].split("\\|");
                if(actions[0].equals("file") && actions[1].equals("summary")){
                    resultRetriever.getSummary(ScanType.FILE);
                } else if (actions[0].equals("file")) {
                    Map<String, Integer> map = resultRetriever.getResult(parts[1]);
                    System.out.println(map);
                } else if (actions[0].equals("web")){

                }
            } else if(command.equals("query") && parts.length == 2){
                String actions[] = parts[1].split("\\|");
                if(actions[0].equals("file") && actions[1].equals("summary")){
                    resultRetriever.querySummary(ScanType.FILE);
                } else if (actions[0].equals("file")) {
                    Map<? extends Object, ? extends Object> map = resultRetriever.queryResult(parts[1]);
                    System.out.println(map);
                } else if (actions[0].equals("web")){

                }
            }else if(command.equals("cws")){

            }else if(command.equals("cfs")){

            }else{
                System.out.println("Komanda -> " + command + ", ne postoji ili nije zadat odgovarajuci broj parametara");
            }
            line = sc.nextLine();
        }
        blockingQueue.add(new Job(ScanType.STOP));
        scheduler.shutdown();
    }

    private static void addWeb(String url) {
        try {
            blockingQueue.put(new WebJob(ScanType.WEB,url,hop_count,null));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addDirectory(String relativPathToDir) {
        System.out.println("Adding dir " + relativPathToDir);
        DirectoryCrawler directoryCrawler = new DirectoryCrawler(blockingQueue,relativPathToDir,keywords,
                file_corpus_prefix,
                dir_crawler_sleep_time,
                file_scanning_size_limit);
        scheduler.scheduleAtFixedRate(directoryCrawler, 0, dir_crawler_sleep_time, TimeUnit.MILLISECONDS);

    }

    static void readConfig(){
        try (InputStream input = Main.class.getClassLoader().getResourceAsStream("app.properties")) {

            Properties prop = new Properties();

            if (input == null) {
                System.out.println("Sorry, unable to find app.properties");
                return;
            }
            //load a properties file from class path, inside static method
            prop.load(input);

            keywords = List.of(prop.getProperty("keywords").split(","));
            file_corpus_prefix = prop.getProperty("file_corpus_prefix");
            dir_crawler_sleep_time = Integer.valueOf(prop.getProperty("dir_crawler_sleep_time"));
            file_scanning_size_limit = Integer.valueOf(prop.getProperty("file_scanning_size_limit"));
            hop_count = Integer.valueOf(prop.getProperty("hop_count"));
            url_refresh_time = Integer.valueOf(prop.getProperty("url_refresh_time"));

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}