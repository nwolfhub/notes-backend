package org.nwolfhub.notes.api.legacy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.nwolfhub.notes.NotesApplication;
import org.nwolfhub.notes.database.legacy.TokenController;
import org.nwolfhub.notes.database.legacy.UserDao;
import org.nwolfhub.notes.database.legacy.model.User;
import org.nwolfhub.utils.Configurator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/limits")
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

    @GetMapping("/getPrivileges")
    public static ResponseEntity<String> getPrivileges(@RequestHeader(name = "token") String token) {
        if(!configurator.getValue("active").equals("true")) return ResponseEntity.status(400).body(JsonBuilder.buildFailOutput("Payments are disabled on this server"));
        Integer userId = TokenController.getUserId(token);
        if(userId==null) {
            return ResponseEntity.status(401).body(JsonBuilder.buildFailOutput("Token verification failed"));
        }
        List<String> privileges = configurator.getAllKeys("privilege_*");
        privileges = privileges.stream().map(e -> {
            e = e.replace("privilege_", "");
            return e;
        }).collect(Collectors.toList());
        return ResponseEntity.status(200).body(JsonBuilder.buildPrivilegesList(privileges));
    }

    @GetMapping("/buyPrivilege")
    public static ResponseEntity<String> buyPrivilege(@RequestHeader(name = "token") String token) {
        if(!configurator.getValue("active").equals("true")) return ResponseEntity.status(400).body(JsonBuilder.buildFailOutput("Payments are disabled on this server"));
        User user = TokenController.getUser(token);
        if(user==null) return ResponseEntity.status(401).body(JsonBuilder.buildFailOutput("Token verification failed"));
        return ResponseEntity.status(200).body(JsonBuilder.buildDonationServerUrlResponse(configurator.getValue("host") + "/public/buy?user=" + user.getId()));
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
