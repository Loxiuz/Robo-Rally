package com.example.httpclientweb;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class ServerController {

    @Autowired
    private ClientWebInterface webSituation;


    @PostMapping(value = "/game")
    public ResponseEntity<String> createGame(@RequestBody String s) {
        String newServerID = webSituation.hostServerGame(s);
        if (newServerID == null) //something went wrong
            return ResponseEntity.internalServerError().body("Server couldn't start");
        return ResponseEntity.ok().body(newServerID);
    }


    @GetMapping(value = "/game")
    public ResponseEntity<String> listOfServerGame() {
        return ResponseEntity.ok().body(webSituation.listOfServerGames());
    }


    @PutMapping(value = "/game/{id}")
    public ResponseEntity<String> joinToAGame(@PathVariable String id) {
        String response = webSituation.joinToGame(id);
        if (response.equals("Server doesn't exist"))
            return ResponseEntity.status(404).body(response);
        if (response.equals("Server is full"))
            return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok().body(response);
    }


    @PostMapping(value = "/game/{id}/{robot}")
    public void leaveGame(@PathVariable String id, @PathVariable String robot) {
        webSituation.leaveTheGame(id, Integer.parseInt(robot));
    }


    @GetMapping(value = "/gameState/{id}")
    public ResponseEntity<String> getGameSituation(@PathVariable String id) {
        return ResponseEntity.ok().body(webSituation.getGameSituation(id));
    }


    @PutMapping(value = "/gameState/{id}")
    public ResponseEntity<String> setGameSituation(@PathVariable String id, @RequestBody String game) {
        webSituation.updateGame(id, game);
        return ResponseEntity.ok().body("ok");
    }
}
