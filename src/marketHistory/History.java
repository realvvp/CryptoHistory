/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package marketHistory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author VVP
 */
public class History {
    
    public static void main(String[] args) {
        HistoryUpdater historyUpdater = new HistoryUpdater();
        Thread updater = new Thread( historyUpdater );
        updater.setDaemon( true );

        updater.start();
        while(true){
            BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
            try {
                String command = br.readLine();
                
                if(command.equals( "/stop" )){
                    break;
                } else {
                    System.out.println("type '\"'/stop'\"' to stop programm!");
                }
                
            } catch ( IOException ex ) {
                Logger.getLogger( History.class.getName() ).log( Level.SEVERE, null, ex );
            }
        }
    }
    
}
