/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package marketHistory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

/**
 *
 * @author VVP
 */
public class HistoryUpdater implements Runnable {

    private String BITTREX_API_URL = "https://bittrex.com/api/v1.1/public/getmarkethistory?market=usdt-";
    private ArrayList<String> currencies;

    @Override
    public void run() {
        initCurrencies();

        while ( true ) {

            doUpdate();

            try {
                Thread.sleep( Property.getInstance().getUpdateTime() );
            } catch ( InterruptedException ex ) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void initCurrencies() {
        currencies = new ArrayList<String>();

        currencies.add( "btc" );
        currencies.add( "ltc" );
        currencies.add( "eth" );
        currencies.add( "dash" );
    }

    protected void doUpdate() {
        JsonObject history = null;

        for ( String curr : currencies ) {
            history = performBasicRequest( BITTREX_API_URL.concat( curr ) );

            if ( history != null ) {
                if ( history.get( "success" ).getAsBoolean() ) {
                    JsonArray result = history.get( "result" ).getAsJsonArray();
                    JsonObject item;
                    
                    for ( int i = 0; i < result.size(); i++ ) {
                        item = result.get( i ).getAsJsonObject();

                        if ( checkCanInsert( item.get( "Id" ).getAsInt(), curr ) ) {
                            insertRow( item, curr );
                        } else{
                            break;
                        }
                    }
                }
            }
        }
    }

    protected Boolean checkCanInsert( int id, String currency ) {
        Boolean result = false;

        ResultSet rs;
        synchronized ( DBHandler.class ) {
            rs = DBHandler.getInstance().executeQuery( "SELECT * FROM history WHERE Id = '" + id
                    + "' AND Currency = '" + currency + "'" );
        }

        if ( rs != null ) {
            try { 
                int count = 0;
                while(rs.next()){
                    count++;
                    if(count > 0){
                        break;
                    }
                }
                result = count == 0;
            } catch ( SQLException ex ) {
                Logger.getLogger( HistoryUpdater.class.getName() ).log( Level.SEVERE, null, ex );
            }
        }

        return result;
    }

    protected void insertRow( JsonObject data, String currency ) {
        String query = "INSERT INTO history (Id, TimeStamp, Quantity, Price, "
                + "Total, FillType, OrderType, Currency) VALUES ('";

        query += data.get( "Id" ).getAsString() + "', '" + data.get( "TimeStamp" ).getAsString() + "', '";
        query += data.get( "Quantity" ).getAsDouble() + "', '" + data.get( "Price" ).getAsDouble() + "', '";
        query += data.get( "Total" ).getAsDouble() + "', '" + data.get( "FillType" ).getAsString() + "', '";
        query += data.get( "OrderType" ).getAsString() + "', '" + currency + "')";

        synchronized ( DBHandler.class ) {
            DBHandler.getInstance().executeUpdate( query );
        }
    }

    protected JsonObject performBasicRequest( String api_string ) {
        JsonObject result = null;

        String URL = api_string;

        HttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet( URL );

        try {
            HttpResponse response = client.execute( get );
            HttpEntity entity = response.getEntity();

            if ( entity == null ) {
                throw new NullPointerException();
            }

            result = new JsonParser().parse( IOUtils.toString( entity.getContent(), "UTF-8" ) ).getAsJsonObject();
        } catch ( Exception e ) {
            e.printStackTrace();
            return result;
        }

        return result;
    }

}
