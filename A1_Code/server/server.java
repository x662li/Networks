package server;

public class server {

    public static void main(String[] args) {

        int reqCode;
        try {
            reqCode = Integer.parseInt(args[0]); // check for input
        } catch (Exception e) {
            System.out.println("Error: please specify req_code correctly");
            return;
        }

        ServerUtil server = new ServerUtil(reqCode);
        server.run();
    }
}