package org.nwolfhub.notes.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.hibernate.sql.Update;
import org.nwolfhub.notes.NotesApplication;
import org.nwolfhub.utils.Configurator;

import java.io.IOException;

public class PaymentController {
    private static Configurator configurator;
    public static void init(Configurator donationConfigurator) {
        PaymentController.configurator = donationConfigurator;
    }

    private static void startListening() {
        OkHttpClient client = new OkHttpClient();
        while (true) {
            try {
                Response response = client.newCall(new Request.Builder().get().url(configurator.getValue("host") + "/getUpdates").build()).execute();
                int code = response.code();
                String body = "";
                if(code==200) {
                    body = response.body().string();
                }
                response.close();
                if(code==200) {
                    JsonObject master = JsonParser.parseString(body).getAsJsonObject();
                    JsonArray updates = master.get("updates").getAsJsonArray();
                    for(JsonElement update:updates) {

                    }
                }
            } catch (Exception e) {
                NotesApplication.cli.print("Failed to contact payments server: " + e);
            }
        }
    }
}
