package ar.edu.itba.main;

import static akka.pattern.PatternsCS.ask;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import ar.edu.itba.actors.GameRoom;
import ar.edu.itba.actors.GameRoomManager;
import java.io.IOException;
import java.util.concurrent.CompletionStage;

public class Main extends AllDirectives {

    private static final String HOST = "localhost";
    private static final int PORT = 8081;
    private static final int TIME_OUT = 5000;

    private static final String GAME_ROOM_PATH = "gameRoom";
    private static final String USER_ID_PARAM = "userId";
    private static final String ID_PARAM = "id";

    private static final String USER = "User";
    private static final String UNKNOWN_MESSAGE = "FAIL - Unknown message received.";
    private static final String EXCEPTION_MESSAGE = "ERROR - Exception received.";

    private static ActorRef gameRoomManager;

    public static void main(final String[] args) throws IOException {
        final ActorSystem actorSystem = ActorSystem.create("TAVJ-AKKA");
        gameRoomManager = actorSystem.actorOf(GameRoomManager.props(), "GameRoomManager");

        final ActorSystem system = ActorSystem.create("Main");
        final Http http = Http.get(system);
        final Main main = new Main();
        final ActorMaterializer materializer = ActorMaterializer.create(system);
        http.bind(ConnectHttp.toHost(HOST, PORT), materializer);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = main.createRoutes().flow(system, materializer);
        final CompletionStage<ServerBinding> binding = 
            http.bindAndHandle(routeFlow, ConnectHttp.toHost(HOST, PORT), materializer);

        System.out.println(String.format("Server online at http://%s:%d/\nPress RETURN to stop...", HOST, PORT));
        System.in.read();
        binding.thenCompose(ServerBinding::unbind).thenAccept(unbound -> system.terminate()); 
    }

    private Route createRoutes() {
        return route(
            pathSingleSlash(() -> complete("Welcome!")),
            createGameRoomRoute(),
            joinGameRoomRoute(),
            leaveGameRoomRoute(),
            deleteGameRoomRoute(),
            showGameRoomRoute()
        );
    }

    private Route createGameRoomRoute() {
        return path(PathMatchers.segment("create").slash(GAME_ROOM_PATH), () -> parameter(USER_ID_PARAM, userId -> {
            System.out.println();
            try {
                final Object message = ask(gameRoomManager, 
                    new GameRoomManager.CreateGameRoom(userId), TIME_OUT).toCompletableFuture().get();
                if (message instanceof GameRoomManager.GameRoomCreated) {
                    final GameRoomManager.GameRoomCreated gameRoomCreate = (GameRoomManager.GameRoomCreated) message;
                    final String gameRoomId = gameRoomCreate.getGameRoomId();
                    return complete(StatusCodes.OK, GameRoomManager.CREATE_GAME_ROOM 
                        + GameRoomManager.ROOM + gameRoomId + GameRoomManager.OWNER + userId);
                } else {
                    return complete(StatusCodes.CONFLICT, UNKNOWN_MESSAGE);
                }
            } catch (final Exception e) {
                e.printStackTrace();
                return complete(StatusCodes.CONFLICT, EXCEPTION_MESSAGE);
            }
        }));
    }

    private Route deleteGameRoomRoute() {
        return path(PathMatchers.segment("delete").slash(GAME_ROOM_PATH), () -> parameter(ID_PARAM, gameRoomId -> {
            System.out.println();
            try {
                final Object message = ask(gameRoomManager, 
                    new GameRoomManager.DeleteGameRoom(gameRoomId), TIME_OUT).toCompletableFuture().get();
                if (message instanceof GameRoomManager.GameRoomDeleted) {
                    return complete(StatusCodes.OK, 
                        GameRoomManager.DELETE_GAME_ROOM + GameRoomManager.ROOM + gameRoomId);
                } else if (message instanceof GameRoomManager.UnknownGameRoom) {
                    return complete(StatusCodes.CONFLICT, GameRoomManager.DELETE_GAME_ROOM + GameRoomManager.FAIL);
                } else {
                    return complete(StatusCodes.CONFLICT, UNKNOWN_MESSAGE);
                }
            } catch (final Exception e) {
                e.printStackTrace();
                return complete(StatusCodes.CONFLICT, EXCEPTION_MESSAGE);
            }
        }));
    }

    private Route joinGameRoomRoute() {
        return path(PathMatchers.segment("join").slash(GAME_ROOM_PATH), 
                () -> parameter(ID_PARAM, gameRoomId -> parameter(USER_ID_PARAM, userId -> {
            System.out.println();
            try {
                final Object message = ask(gameRoomManager, 
                    new GameRoomManager.JoinGameRoom(gameRoomId, userId), TIME_OUT).toCompletableFuture().get();
                if (message instanceof GameRoom.JoinGameRoomSuccessfully) {
                    return complete(StatusCodes.OK, GameRoomManager.JOIN_GAME_ROOM 
                        + GameRoomManager.ROOM + gameRoomId + GameRoomManager.USER + userId);
                } else if (message instanceof GameRoom.UserAlreadyJoinToGameRoom) {
                    return complete(StatusCodes.CONFLICT, USER + userId + " already joined.");
                } else if (message instanceof GameRoomManager.UnknownGameRoom) {
                    return complete(StatusCodes.CONFLICT, GameRoomManager.JOIN_GAME_ROOM + GameRoomManager.FAIL);
                } else {
                    return complete(StatusCodes.CONFLICT, UNKNOWN_MESSAGE);
                }
            } catch(final Exception e) {
                e.printStackTrace();
                return complete(StatusCodes.CONFLICT, EXCEPTION_MESSAGE);
            }
        })));
    }

    private Route leaveGameRoomRoute() {
        return path(PathMatchers.segment("leave").slash(GAME_ROOM_PATH), 
                () -> parameter(ID_PARAM, gameRoomId -> parameter(USER_ID_PARAM, userId -> {
            System.out.println();
            try {
                final Object message = ask(gameRoomManager, 
                    new GameRoomManager.LeaveGameRoom(gameRoomId, userId), TIME_OUT).toCompletableFuture().get();
                if (message instanceof GameRoom.LeaveGameRoomSuccessfully) {
                    return complete(StatusCodes.OK, GameRoomManager.LEAVE_GAME_ROOM 
                        + GameRoomManager.USER + userId + GameRoomManager.ROOM + gameRoomId);
                } else if (message instanceof GameRoom.UserIsNotInGameRoom) {
                    return complete(StatusCodes.CONFLICT, USER + userId + " not found.");
                } else if (message instanceof GameRoomManager.UnknownGameRoom) {
                    return complete(StatusCodes.CONFLICT, GameRoomManager.LEAVE_GAME_ROOM + GameRoomManager.FAIL);
                } else {
                    return complete(StatusCodes.CONFLICT, UNKNOWN_MESSAGE);
                }
            } catch (final Exception e) {
                e.printStackTrace();
                return complete(StatusCodes.CONFLICT, EXCEPTION_MESSAGE);
            }
        })));
    }

    private Route showGameRoomRoute() {
        return path(PathMatchers.segment("show").slash(GAME_ROOM_PATH), () -> parameter(ID_PARAM, gameRoomId -> {
            System.out.println();
            try {
                final Object message = ask(gameRoomManager, 
                    new GameRoomManager.ShowGameRoom(gameRoomId), TIME_OUT).toCompletableFuture().get();
                if (message instanceof GameRoom.ShowGameRoomSuccessfully) {
                    final GameRoom.ShowGameRoomSuccessfully msg = (GameRoom.ShowGameRoomSuccessfully) message;
                    return complete(StatusCodes.OK, GameRoomManager.SHOW_GAME_ROOM 
                        + GameRoomManager.ROOM + gameRoomId + "\n" + msg.sUsers); 
                } else {
                    return complete(StatusCodes.CONFLICT, GameRoomManager.LEAVE_GAME_ROOM + GameRoomManager.FAIL);
                }
            } catch (final Exception e) {
                e.printStackTrace();
                return complete(StatusCodes.CONFLICT, EXCEPTION_MESSAGE);
            }
        }));
    }
}