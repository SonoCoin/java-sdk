package io.sonocoin.sdk.Core;

import android.content.Context;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.json.JSONArray;
import io.sonocoin.sdk.Libs.FileUtils;
import io.sonocoin.sdk.Storage.ConnectionRepository;
import io.sonocoin.sdk.Types.Connection;

import java.io.IOException;
import java.util.ArrayList;

public class JsonStorage {
    /**
     * Retrieve nodes list from the Net
     *
     * @param url URL API for retrieving node list
     * @param ctx
     * @return String
     * @throws IOException
     */
    @SuppressWarnings("UnusedReturnValue")
    public static boolean requestNodes(String url, final Context ctx) throws IOException {
        Config.ConnectionsIsRead = false;
        final ConnectionRepository repocon = new ConnectionRepository(ctx);
        //noinspection unused
        AndroidNetworking.get(url)
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONArray(new JSONArrayRequestListener() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        ArrayList<Connection> new_con_list = new ArrayList<Connection>();
                        Config.CONNECTIONS = new ArrayList<Connection>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                JsonParser parser = new JsonParser();
                                JsonElement mJson = parser.parse(jsonArray.getString(i));
                                JsonObject m = mJson.getAsJsonObject();
                                JsonObject s = m.get("api").getAsJsonObject();
                                String addr = s.get("addr").getAsString();
                                String[] addrs = addr.split("/");
                                boolean ssl = s.get("ssl").getAsBoolean();
                                int port = s.get("port").getAsInt();
                                String protocol = ssl ? "https://" : "http://";
                                String url = protocol + addrs[0];
                                Connection con = new Connection(1, addr, port, ssl, url);
                                if (repocon.get(con.getFullUrl(), con.getPort()) == null) {
                                    new_con_list.add(con);
                                }
                                Config.CONNECTIONS.add(con);
                            } catch (Exception e) {
                                FileUtils.dump("JsonCheckHash.requestNodes, try error: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        if (new_con_list.size() > 0) {
                            repocon.insert(new_con_list);
                        }
                        Config.ConnectionsIsRead = true;
                        // Log.d("response", "success");
                    }

                    @Override
                    public void onError(ANError error) {
                        Config.ConnectionsIsRead = true;
                        FileUtils.dump("JsonCheckHash.requestNodes, error: " + error.getMessage());
                        // Log.d("response", "there was an error: " + error.getMessage());
                    }
                });
        return (Config.CONNECTIONS.size() > 0);
    }
}
