package dk.dtu.compute.se.pisd.httpclient;


import dk.dtu.compute.se.pisd.roborally.fileaccess.SaveAndLoad;

import dk.dtu.compute.se.pisd.roborally.model.Board;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Create a http client that can interact with the RoboRally game server.
 */
public class Client implements Client_interface {

    private static final HttpClient HTTPclient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private String domain = "http://localhost:8080";    //server URL, can later be changed to get this data from a DNS request or pointing directly to a server IP.
    private String domains = "http://localhost:8081";
    private String domainID = "";                       //will be used after creating the connection, to inform the server what game we are in.
    private boolean connectedToServer = false;          //used to easily check if we already connected to a server, so that we can disconnect from that one first.
    private int robotNumber;                            //Is only used to free up our given robot in case we want to leave a game that has not yet concluded.

    public boolean isClientOnServer() {
        return connectedToServer;
    }

    /**
     * Uses Json and update the game state on the server and server can store this state.
     * @param gameSituation The state to be stored.
     */
    @Override
    public void updateGameSituation(String gameSituation) {
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(gameSituation))   //Http .put
                .uri(URI.create(domain + "/gameSituation/" + domainID))
                .setHeader("User-Agent", "RoboRally Client")
                .setHeader("Content-Type", "application/json")
                .build();
        CompletableFuture<HttpResponse<String>> response =
                HTTPclient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        try {
            String state = response.thenApply(HttpResponse::body).get(5, HOURS);
            // Result ignorer for now
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Gets the current game state from Json and deserialized.
     * @return Returns the response.
     */
    @Override
    public String getGameSituation() {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()                                               // http .Get request for game state
                .uri(URI.create(domain + "/gameSituation/" + domainID))
                .setHeader("User-Agent", "RoboRally Client")
                .header("Content-Type", "application/json")
                .build();
        CompletableFuture<HttpResponse<String>> response =
                HTTPclient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        String state;
        try {
            state = response.thenApply(HttpResponse::body).get(5, SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        return state;
    }

    /**
     * Hosts a new game on the server and sets the server id.
     * @param ServerName Title of the game to be hosted.
     * @return Returns "success" if running correctly.
     */
    @Override
    public String hostServerGame(String ServerName) {
        if (!Objects.equals(domainID, ""))
            leaveServer();
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(ServerName))
                .uri(URI.create(domain + "/game"))
                .setHeader("User-Agent", "RoboRally Client")
                .header("Content-Type", "text/plain")
                .build();
        CompletableFuture<HttpResponse<String>> response =
                HTTPclient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        try {
            domainID = response.thenApply(HttpResponse::body).get(5, SECONDS);
            if (response.get().statusCode() == 500)
                return response.get().body();
            connectedToServer = true;
            robotNumber = 0;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            domainID = "";
            return "Service timeout";
        }

        return "success";
    }

    /**
     * Lists all games available on the server.
     * @return Returns response.
     */
    @Override
    public String listGamesOnServer() {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(domain + "/game"))
                .setHeader("User-Agent", "RoboRally Client")
                .header("Content-Type", "application/json")
                .build();
        CompletableFuture<HttpResponse<String>> response =
                HTTPclient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        String state;
        try {
            state = response.thenApply(HttpResponse::body).get(5, SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return "server timeout";
        }
        return state;

    }

    /**
     * Select an id and joins a game and get the current game state
     * @param serverToJoin An ID of a server.
     * @return Returns "ok" as confirmation.
     */
    @Override
    public String joinToAGame(String serverToJoin) {
        if (!Objects.equals(serverToJoin, ""))
            leaveServer();
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(""))
                .uri(URI.create(domain + "/game/" + serverToJoin))
                .header("User-Agent", "RoboRally Client")
                .header("Content-Type", "text/plain")
                .build();

        CompletableFuture<HttpResponse<String>> response =
                HTTPclient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        try {
            HttpResponse<String> responseMessage = response.get(5, SECONDS); //gets the message back from the server
            if (responseMessage.statusCode() == 404)
                return responseMessage.body();
            robotNumber = Integer.parseInt(responseMessage.body());
            domainID = serverToJoin;

        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return "service timeout";
        }
        return "ok";
    }



    /**
     * leave our current game from server.
     */
    @Override
    public void leaveServer() {
        if (Objects.equals(domainID, ""))
            return;
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .uri(URI.create(domain + "/game/" + domainID + "/" + robotNumber))
                .header("User-Agent", "RoboRally Client")
                .header("Content-Type", "text/plain")
                .build();
        new Thread(() -> {
            int tries = 0;
            CompletableFuture<HttpResponse<String>> response =
                    HTTPclient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            do {
                try {
                    response.get(5, SECONDS);
                    break;
                } catch (ExecutionException | InterruptedException e) {
                    break;
                } catch (TimeoutException e) {
                    tries++;
                }
            } while (tries != 10);
        }).start();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        domainID = "";
    }

    @Override
    public String loadGame() {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(domain + "/loadgame"))
                .setHeader("User-Agent", "Roborally Client")
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = null;
        try {
            response =  HTTPclient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return response.body();
    }


    // the methode goes throw the ip exception. if the ip is valid, set the ip of the server in the process
    public void setServer(String server)  {
        // Simple regex pattern to check for string contains ip
        Pattern pattern = Pattern.compile("");
        Matcher matcher = pattern.matcher(server);
        if (matcher.find())
            this.domain = "http://" + server + ":8080";

    }


    //Return robot number given by the server
    public int getRobotNumber() {
        return robotNumber;
    }


    @Override
    public void saveBoard(Board board) {
        System.out.println(board.getPhase());
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(SaveAndLoad.serialize(board)))
                .uri(URI.create(domain +"/savegame"))
                .setHeader("User-Agent", "Roborally Client")
                .header("Content-Type", "application/json")
                .build();

        CompletableFuture<HttpResponse<String>> response =
                HTTPclient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

    }
}

