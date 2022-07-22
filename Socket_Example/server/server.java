package server;

public class server {
    public static void main(String[] args) {

        int reqCode;
        try {
            reqCode = Integer.parseInt(args[0]); // obtain req_code from commandline
        } catch (Exception e) {
            System.out.println("Error: please specify req_code correctly");
            return;
        }

        ServerUtil server = new ServerUtil(reqCode); // create ServerUtil object
        server.run(); // run server
    }
}