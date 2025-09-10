package eci.edu.arep.microspringboot;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import eci.edu.arep.httpserver.HttpServer;

public class MicroSpringBoot {

    public static void main(String[] args) {
        System.out.println("Starting MicroSpringBoot!");
        
        try{
            HttpServer.runServer(args);
        }catch(IOException  | URISyntaxException ex){
            Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
