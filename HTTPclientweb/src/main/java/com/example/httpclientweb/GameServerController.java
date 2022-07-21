package com.example.httpclientweb;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class GameServerController {

    @Autowired
    private ClientWebInterface webSituation;


    @PostMapping(value = "/game")
    public ResponseEntity<String> createServerGame(@RequestBody String createServerGame) {
        String ServerID = webSituation.hostServerGame(createServerGame);
        if (ServerID == null) //something went wrong
            return ResponseEntity.internalServerError().body("Server couldn't start");
        return ResponseEntity.ok().body(ServerID);
    }


    @GetMapping(value = "/game")
    public ResponseEntity<String> listOfServerGame() {
        return ResponseEntity.ok().body(webSituation.listOfServerGames());
    }


    @PutMapping(value = "/game/{id}")
    public ResponseEntity<String> joinToServerGame(@PathVariable String id) {
        String serverResponse = webSituation.joinToServerGame(id);
        if (serverResponse.equals("Server doesn't exist"))
            return ResponseEntity.status(404).body(serverResponse);
        if (serverResponse.equals("Server is full"))
            return ResponseEntity.badRequest().body(serverResponse);
        return ResponseEntity.ok().body(serverResponse);
    }


    @PostMapping(value = "/game/{id}/{robot}")
    public void leaveServerGame(@PathVariable String id, @PathVariable String robot) {
        webSituation.leaveServerGame(id, Integer.parseInt(robot));
    }


    @GetMapping(value = "/gameState/{id}")
    public ResponseEntity<String> getGameSituation(@PathVariable String id) {
        return ResponseEntity.ok().body(webSituation.getGameSituation(id));
    }


    @PutMapping(value = "/gameState/{id}")
    public ResponseEntity<String> setGameSituation(@PathVariable String id, @RequestBody String game) {
        webSituation.updateServerGame(id, game);
        return ResponseEntity.ok().body("ok");
    }
}
