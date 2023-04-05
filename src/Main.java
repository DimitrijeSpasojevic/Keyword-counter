import jobs.Job;
import jobs.ScanType;
import jobs.WebJob;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import scanners.FileScanner;
import scanners.WebScanner;
import utils.DirectoryCrawler;
import utils.JobDispatcher;
import utils.ResultRetriever;
import utils.ResultRetrieverImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
        fileScanner = new FileScanner(resultRetriever, file_scanning_size_limit, keywords);
        webScanner = new WebScanner(resultRetriever, keywords, url_refresh_time, scheduler, blockingQueue);
        JobDispatcher jobDispatcher = new JobDispatcher(fileScanner, webScanner, blockingQueue);
        jobDispatcher.start();
        Scanner sc = new Scanner(System.in);
        System.out.println("Unesi komandu");

        String line = sc.nextLine();
        String command = "";
        while (!line.split(" ")[0].equals("stop")) {
            String parts[] = line.split(" ");
            command = parts[0];
//            System.out.println("komanda je " + command);

            if (command.equals("ad") && parts.length == 2) {
                addDirectory(parts[1]);
            } else if (command.equals("aw") && parts.length == 2) {
                addWeb(parts[1]);
            } else if (command.equals("get") && parts.length == 2) {
                String actions[] = parts[1].split("\\|");

                if (actions[0].equals("file") && actions[1].equals("summary")) {
                    System.out.println(resultRetriever.summary(ScanType.FILE, false));
                } else if (actions[0].equals("web") && actions[1].equals("summary")) {
                    System.out.println(resultRetriever.summary(ScanType.WEB, false));
                } else if (actions[0].equals("file")) {
                    Map<String, Integer> map = resultRetriever.getResult(parts[1]);
                    System.out.println(map);
                } else if (actions[0].equals("web")) {
                    Map<String, Integer> map = resultRetriever.getResult(parts[1]);
                    System.out.println(map);
                }
            } else if (command.equals("query") && parts.length == 2) {
                String actions[] = parts[1].split("\\|");
                if (actions[0].equals("file") && actions[1].equals("summary")) {
                    System.out.println(resultRetriever.summary(ScanType.FILE, true));
                } else if (actions[0].equals("web") && actions[1].equals("summary")) {
                    System.out.println(resultRetriever.summary(ScanType.WEB, true));
                } else if (actions[0].equals("file")) {
                    Map<? extends Object, ? extends Object> map = resultRetriever.queryResult(parts[1]);
                    System.out.println(map);
                } else if (actions[0].equals("web")) {
                    Map<? extends Object, ? extends Object> map = resultRetriever.queryResult(parts[1]);
                    System.out.println(map);
                }
            } else if (command.equals("cws")) {
                resultRetriever.clearSummary(ScanType.WEB);
            } else if (command.equals("cfs")) {
                resultRetriever.clearSummary(ScanType.FILE);
            } else {
                System.out.println("Komanda -> " + command + ", ne postoji ili nije zadat odgovarajuci broj parametara");
            }
            line = sc.nextLine();
        }
        blockingQueue.add(new Job(ScanType.STOP));
        scheduler.shutdown();
        resultRetriever.stop();
        System.out.println("Directory crawler stopiran");
    }

    private static void addWeb(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
        } catch (Exception e) {
            System.out.println("Problem sa url-om");
            return;
        }
        try {
            blockingQueue.put(new WebJob(ScanType.WEB, url, hop_count, getDomainName(url)));
        } catch (InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addDirectory(String relativPathToDir) {
        System.out.println("Adding dir " + relativPathToDir);
        File tmpDir = new File(relativPathToDir);
        if (!tmpDir.exists()) {
            System.out.println("Nepostojeći direktorijum");
        } else {
            DirectoryCrawler directoryCrawler = new DirectoryCrawler(blockingQueue, relativPathToDir, keywords,
                    file_corpus_prefix,
                    dir_crawler_sleep_time,
                    file_scanning_size_limit);
            scheduler.scheduleAtFixedRate(directoryCrawler, 0, dir_crawler_sleep_time, TimeUnit.MILLISECONDS);
        }
    }

    static void readConfig() {
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

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }
}