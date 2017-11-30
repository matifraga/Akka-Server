package ar.edu.itba.actors;

import akka.actor.*;

import java.util.HashMap;
import java.util.Map;

public class GameRoomManager extends AbstractActor {

    public static final String ROOM = " - ROOM: ";
    public static final String OWNER = " - OWNER: ";
    public static final String FAIL = " - FAIL: ROOM does not exist.";
    public static final String USER = "- USER: "; 
    public static final String CREATE_GAME_ROOM = "|CreateGameRoom|";
    public static final String DELETE_GAME_ROOM = "|DeleteGameRoom|";
    public static final String JOIN_GAME_ROOM = "|JoinGameRoom|";
    public static final String LEAVE_GAME_ROOM = "|LeaveGameRoom|";
    public static final String SHOW_GAME_ROOM = "|ShowGameRoom|";

    private long currentId = 0;
    private Map<String, ActorRef> gameRooms = new HashMap<>();
   
    public static Props props() {
        return Props.create(GameRoomManager.class, () -> new GameRoomManager());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateGameRoom.class, message -> createGameRoom(message))
                .match(DeleteGameRoom.class, message -> deleteGameRoom(message))
                .match(JoinGameRoom.class, message -> joinGameRoom(message))
                .match(LeaveGameRoom.class, message -> leaveGameRoom(message))
                .match(ShowGameRoom.class, message -> showGameRoom(message))
                .build();
    }

    private void createGameRoom(final CreateGameRoom message) {
        final String gameRoomId = String.valueOf(++currentId);
        final String ownerId = message.ownerId;
        final ActorRef gameRoom = getContext().
            actorOf(GameRoom.props(gameRoomId, ownerId), "GameRoom" + gameRoomId + "_Owner" + ownerId);
        System.out.println(CREATE_GAME_ROOM + ROOM + gameRoomId + OWNER + ownerId);
        gameRooms.put(gameRoomId, gameRoom);
        getSender().tell(new GameRoomCreated(gameRoomId), getSelf());
    }

    private void deleteGameRoom(final DeleteGameRoom message) {
        final String gameRoomId = message.gameRoomId;
        if (gameRooms.containsKey(gameRoomId)) {
            final ActorRef gameRoom = gameRooms.get(gameRoomId);
            gameRooms.remove(gameRoomId);
            gameRoom.tell(PoisonPill.getInstance(), getSelf());
            System.out.println(DELETE_GAME_ROOM + ROOM + gameRoomId);
            getSender().tell(new GameRoomDeleted(), getSelf());
        } else {
            System.out.println(DELETE_GAME_ROOM + FAIL);
            getSender().tell(new UnknownGameRoom(), getSelf());
        }
    }

    private void joinGameRoom(final JoinGameRoom message) {
        final String gameRoomId = message.gameRoomId;
        final String userId = message.userId;
        if (gameRooms.containsKey(gameRoomId)) {
            final ActorRef gameRoom = gameRooms.get(gameRoomId);
            System.out.println(JOIN_GAME_ROOM + ROOM + gameRoomId + USER + userId);
            gameRoom.tell(new GameRoom.JoinGameRoom(userId), getSender());
        } else {
            System.out.println(JOIN_GAME_ROOM + FAIL);
            getSender().tell(new UnknownGameRoom(), getSelf());
        }
    }

    private void leaveGameRoom(final LeaveGameRoom message) {
        final String gameRoomId = message.gameRoomId;
        final String userId = message.userId;
        if (gameRooms.containsKey(gameRoomId)) {
            final ActorRef gameRoom = gameRooms.get(gameRoomId);
            System.out.println(LEAVE_GAME_ROOM + USER + userId + ROOM + gameRoomId);
            gameRoom.tell(new GameRoom.LeaveGameRoom(userId), getSender());
        } else {
            System.out.println(LEAVE_GAME_ROOM + FAIL);
            getSender().tell(new UnknownGameRoom(), getSelf());
        }
    }

    private void showGameRoom(final ShowGameRoom message) {
        final String gameRoomId = message.gameRoomId;
        if (gameRooms.containsKey(gameRoomId)) {
            final ActorRef gameRoom = gameRooms.get(gameRoomId);
            System.out.println(SHOW_GAME_ROOM + ROOM + gameRoomId);
            gameRoom.tell(new GameRoom.ShowGameRoom(), getSender());
        } else {
            System.out.println(SHOW_GAME_ROOM + FAIL);
            getSender().tell(new UnknownGameRoom(), getSelf());
        }
    }

    public static class CreateGameRoom {

        private final String ownerId;

        public CreateGameRoom(final String userId) {
            this.ownerId = userId;
        }
    }

    public static class DeleteGameRoom {

        private final String gameRoomId;

        public DeleteGameRoom(final String gameRoomId) {
            this.gameRoomId = gameRoomId;
        }
    }

    public static class GameRoomCreated {

        private final String gameRoomId;

        public GameRoomCreated(final String gameRoomId) {
            this.gameRoomId = gameRoomId;
        }

        public String getGameRoomId() {
            return gameRoomId;
        }
    }

    public static class JoinGameRoom {

        private final String gameRoomId;
        private final String userId;

        public JoinGameRoom(final String gameRoomId, final String userId) {
            this.gameRoomId = gameRoomId;
            this.userId = userId;
        }
    }

    public static class LeaveGameRoom {

        private final String gameRoomId;
        private final String userId;

        public LeaveGameRoom(final String gameRoomId, final String userId) {
            this.gameRoomId = gameRoomId;
            this.userId = userId;
        }
    }

    public static class ShowGameRoom {

        private final String gameRoomId;

        public ShowGameRoom(final String gameRoomId) {
            this.gameRoomId = gameRoomId;
        }
    }

    public static class GameRoomDeleted { }

    public static class UnknownGameRoom { }
}
