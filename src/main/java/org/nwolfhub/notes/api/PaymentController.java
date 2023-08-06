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
import org.nwolfhub.notes.database.UserDao;
import org.nwolfhub.notes.model.User;
import org.nwolfhub.utils.Configurator;

import java.io.IOException;

public class PaymentController {
    private static Configurator configurator;
    private static UserDao dao;
    public static void init(Configurator donationConfigurator, UserDao dao) {
        PaymentController.configurator = donationConfigurator;
        PaymentController.dao = dao;
        String active = configurator.getValue("active");
        if(active.equals("true")) {
            new Thread(PaymentController::startListening).start();
        }
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
                        String type = update.getAsJsonObject().get("type").getAsString();
                        switch (type) {
                            case "purchase" -> {
                                String user = update.getAsJsonObject().get("username").getAsString();
                                String privilege = update.getAsJsonObject().get("privilege").getAsString();
                                User fullUser = dao.getUser(user);
                                try {
                                    if (!(Integer.parseInt(configurator.getValue("privilege_" + fullUser.getPrivilege())) > Integer.parseInt(configurator.getValue("privilege_" + privilege)))) {
                                        dao.setObject(fullUser.setPrivilege(privilege));
                                    }
                                } catch (NumberFormatException | NullPointerException e) {
                                    NotesApplication.cli.print("Strange request from payment server! " + update);
                                }
                            }
                            case "revoke" -> {
                                String user = update.getAsJsonObject().get("username").getAsString();
                                User fullUser = dao.getUser(user);
                                dao.setObject(fullUser.setPrivilege("default"));
                            }
                            case "question" -> {
                                String user = update.getAsJsonObject().get("username").getAsString();
                                String question = update.getAsJsonObject().get("question").getAsString();
                                int id = update.getAsJsonObject().get("id").getAsInt();
                                client.newCall(new Request.Builder().url(configurator.getValue("host") + "/answerQuestion?id=" + id + "&response=" + processQuestion(question, user)).get().build()).execute().close();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                NotesApplication.cli.print("Failed to contact payments server: " + e);
            }
        }
    }

    private static String processQuestion(String question, String username) {
        User user = dao.getUser(username);
        switch (question) {
            case "privilege" -> {
                return user.privilege;
            }
        }
        return "unknown";
    }
}
