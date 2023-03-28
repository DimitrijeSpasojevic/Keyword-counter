import java.io.*;
import java.util.Map;
import java.util.concurrent.RecursiveTask;

public class FileTaskWorker extends RecursiveTask {

    private int start; //pocetak opsega
    private int end; //kraj opsega
    private File[] files;
    private static final int MAX = 2; //max posla koliko smemo da imamo
    private Map<String, Integer> map;


    private  Map<String, Integer> readFromFile(File file, Map<String,Integer> map) throws IOException {
        String line;
        //Opens a file in read mode
        FileReader fileReader = new FileReader(file);
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
        return map;
    }

    public FileTaskWorker(File[] files, int start, int end, Map<String, Integer> map) {
        this.start = start;
        this.end = end;
        this.files = files;
        //ispis da pratimo koliko se dodatnih niti napravilo, u rekurziji deljenja posla
        System.out.println("New Job " + start + " " + end);
        this.map = map;
    }

    @Override
    protected Object compute() {

        if(end - start < MAX){
            for(int i = start; i < end; i++){
                try {
                    readFromFile(files[i], map);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            //ako nije, trazimo polovinu opsega
            int mid = ((end - start) / 2) + start;

            //delimo posao da sva dela, levi i desni, levi ide od starta do pola(mid), a desni od mid do end-a, tj kraja opsega
            FileTaskWorker left = new FileTaskWorker(files, start, mid, map);
            FileTaskWorker right = new FileTaskWorker(files, mid, end, map);

            //ovim pravimo novu nit koja ce da se bavi levim poslom
            left.fork();

            //ova nit koja je i podelila posao racuna svoj, desni deo posla
            Map<String, Integer> rightResult = (Map<String, Integer>) right.compute();
            //dohvatamo sta je rezultat leve, nove, niti
            Map<String, Integer> leftResult = (Map<String, Integer>) left.join();


            //upisujemo i jedno i drugo u glavnu listu
//            primeNumbers.addAll(leftResult);
//            primeNumbers.addAll(rightResult);

        }

        return map;
    }
}
