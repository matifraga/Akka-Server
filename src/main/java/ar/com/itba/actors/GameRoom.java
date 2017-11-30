package ar.edu.itba.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;

import java.util.Collection;
import java.util.HashSet;
import java.lang.StringBuilder;

public class GameRoom extends AbstractActor {

    private final String gameRoomId;
    private final String ownerId;
    private final Collection<String> users = new HashSet<>();

    public static Props props(final String gameRoomId, final String ownerId) {
        return Props.create(GameRoom.class, () -> new GameRoom(gameRoomId, ownerId));
    }

    public GameRoom(final String gameRoomId, final String ownerId) {
        this.gameRoomId = gameRoomId;
        this.ownerId = ownerId;
        users.add(ownerId);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(JoinGameRoom.class, message -> joinGameRoom(message))
                .match(LeaveGameRoom.class, message -> leaveGameRoom(message))
                .match(ShowGameRoom.class, message -> showGameRoom())
                .build();
    }

    private void joinGameRoom(final JoinGameRoom message) {
        final String userId = message.userId;
        if (users.contains(userId)) {
            getSender().tell(new UserAlreadyJoinToGameRoom(), getSelf());
        } else {
            users.add(userId);
            getSender().tell(new JoinGameRoomSuccessfully(), getSelf());
        }
    }

    private void leaveGameRoom(final LeaveGameRoom message) {
        final String userId = message.userId;
        if (users.contains(userId)) {
            users.remove(userId);
            getSender().tell(new LeaveGameRoomSuccessfully(), getSelf());
        } else {
            getSender().tell(new UserIsNotInGameRoom(), getSelf());
        }
    }

    private void showGameRoom() {
        final StringBuilder sb = new StringBuilder();
        for (final String user: users ) {
            sb.append("User: " + user + "\n");
        }
        getSender().tell(new ShowGameRoomSuccessfully(sb.toString()), getSelf());       
    }

    public static class JoinGameRoom {

        private final String userId;

        public JoinGameRoom(final String userId) {
            this.userId = userId;
        }
    }

    public static class LeaveGameRoom {

        private final String userId;

        public LeaveGameRoom(final String userId) {
            this.userId = userId;
        }
    }

    public static class ShowGameRoom { }

    public static class ShowGameRoomSuccessfully {

        public final String sUsers;

        public ShowGameRoomSuccessfully(final String users) {
            sUsers = users;
        }
    }

    public static class JoinGameRoomSuccessfully { }

    public static class UserAlreadyJoinToGameRoom { }

    public static class LeaveGameRoomSuccessfully { }

    public static class UserIsNotInGameRoom { }
}
