package org.nwolfhub.notes.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/server")
public class ServerInfo {
    @GetMapping("/info")
    public String getServerInfo() {
        return JsonBuilder.serverInfo;
    }
}
