/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package marketHistory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author VVP
 */
public class Configuration {

    private int updateTime;
    private String DBPath;
    private JsonObject properties;


    public Configuration() {
        readProperties();
    }

    private void readProperties() {
        FileReader fr;
        try {
            fr = new FileReader( "conf.json" );

            properties = new JsonParser().parse( fr ).getAsJsonObject();

            DBPath = properties.get( "DBPath" ).getAsString();
            updateTime = properties.get( "updatePeriodMS" ).getAsInt();

            fr.close();
        } catch ( FileNotFoundException ex ) {
            Logger.getLogger(Configuration.class.getName() ).log( Level.SEVERE, null, ex );
        } catch ( IOException ex ) {
            Logger.getLogger(Configuration.class.getName() ).log( Level.SEVERE, null, ex );
        } 
    }

    public String getDBPath() {
        return DBPath;
    }
    
    public int getUpdateTime(){
        return updateTime;
    }
}
