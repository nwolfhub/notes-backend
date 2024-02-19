package org.nwolfhub.notes.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/server")
public class ServerInfo {
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    String issuer;
    @Value("${server.name}")
    String serverName;
    @GetMapping("/info")
    public String getServerInfo() {
        return JsonBuilder.serverInfo(serverName);
    }
    @GetMapping("/login")
    public String getLoginInfo() {
        return JsonBuilder.buildIndirectLogin(issuer + "/protocol/openid-connect/auth");
    }
}
