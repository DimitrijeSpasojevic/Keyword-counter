import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

public class FileTaskWorker extends RecursiveTask {

    private int start; //pocetak opsega
    private int end; //kraj opsega
    private File[] files;
    private static final int MAX = Main.file_scanning_size_limit; //max posla koliko smemo da imamo
    private  Map<String, Map<String, Integer>> map;
    private File dir;

    public FileTaskWorker(File[] files, int start, int end, Map<String, Map<String, Integer>> map, File dir) {
        this.start = start;
        this.end = end;
        this.files = files;
        this.dir = dir;
        this.map = map;
    }

    private void readFromFiles(Map<String,Integer> map) throws IOException {

        for (int i = start; i < end; i++) {
            String line;
            //Opens a file in read mode
            FileReader fileReader = new FileReader(files[i]);
            BufferedReader br = new BufferedReader(fileReader);

            //Gets each line till end of file is reached
            while((line = br.readLine()) != null) {
                //Splits each line into words
                String words[] = line.split(" ");
                //Counts each word
                for (String word: words){
                    if(Main.keywords.contains(word)){
                        if(map.containsKey(word)){
                            map.put(word,map.get(word) + 1);
                        }else {
                            map.put(word,1);
                        }
                    }
                }
            }
            br.close();
        }
    }

    private void countKeyWords(byte[] bytes, int offset, Map<String,Integer> map){
        Runnable runnable = () -> {
            byte[] bytesForScan = new byte[Main.file_scanning_size_limit];
            for (int i = 0; i < Main.file_scanning_size_limit; i++) {
                if(offset*Main.file_scanning_size_limit + i < bytes.length)
                    bytesForScan[i] = bytes[offset*Main.file_scanning_size_limit + i];
            }
            String s = new String(bytesForScan, StandardCharsets.UTF_8);
            for (String word: s.split(" ")){
                if(Main.keywords.contains(word)){
                    if(map.containsKey(word)){
                        map.put(word,map.get(word) + 1);
                    }else {
                        map.put(word,1);
                    }
                }
            }
        };
        runnable.run();
    }

    @Override
    protected Object compute() {

        Long filesSize = 0L;
        boolean can = true;
        if(end - start > 1){
            for (int i = start; i < end; i++) {
                filesSize += files[i].length();
                if (filesSize > MAX){
                    can = false;
                    break;
                }
            }
            if(can){
                try {
                    readFromFiles(map.get(dir.getName()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                int mid = ((end - start) / 2) + start;

                FileTaskWorker left = new FileTaskWorker(files, start, mid, map, dir);
                FileTaskWorker right = new FileTaskWorker(files, mid, end, map, dir);

                left.fork();

                Map<String, Map<String, Integer>> rightResult = ( Map<String, Map<String, Integer>>) right.compute();
                Map<String, Map<String, Integer>>leftResult = ( Map<String, Map<String, Integer>>) left.join();
            }
        }else { // delimo jedan fajl na manje delove
            try {
                byte[] bytes = readFile(files[start]);
                int brojNitiZaFajl = bytes.length / Main.file_scanning_size_limit;
                if(bytes.length % Main.file_scanning_size_limit != 0) brojNitiZaFajl++;

                for (int i = 0; i < brojNitiZaFajl; i++) {
                    countKeyWords(bytes, i, map.get(dir.getName()));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return map;
    }

    public byte[] readFile(File f) throws IOException {

        byte[] buffer = new byte[(int)f.length()];

        FileInputStream is = new FileInputStream(f);

        is.read(buffer);

        is.close();

        return  buffer;
    }
}
