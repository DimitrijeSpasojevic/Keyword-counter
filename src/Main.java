import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        readConfig();

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

            }else if(command.equals("get") && parts.length == 2){

            } else if(command.equals("query") && parts.length == 2){

            }else if(command.equals("cws")){

            }else if(command.equals("cfs")){

            }else{
                System.out.println("Komanda -> " + command + ", ne postoji ili nije zadat odgovarajuci broj parametara");
            }
            line = sc.nextLine();
        }
    }

    private static void addDirectory(String relativPathToDir) {
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

            //get the property value and print it out
            System.out.println(prop.getProperty("keywords"));
            System.out.println(prop.getProperty("url_refresh_time"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}