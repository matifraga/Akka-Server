# AKKA Server

Special assignment for Advanced Techniques of videogames course at [ITBA](https://www.itba.edu.ar) 

## Run

To try this out, follow the steps below..

You need to use maven and run inside the folder the following command

```
$ mvn compile exec:exec
```

That will run the server (by default on localhost and port 8081).
You will be able to see the command logs on the console.

You can see below what this server can do!

## Endpoints

Remember that all the endpoints have the following structure `HOST:PORT/endpoint`

### Create a game room

You can create a game room like this

```
localhost:8081/create/gameRoom?userId=2
```

where `userId` is the ID of the owner of the room 

You will always be able to create a room, the id is an autoincremental number starting from 1

### Join a game room

You can join a game room like this

```
localhost:8081/join/gameRoom?id=1&userId=2
```

where `userId` is the ID of the user that is joining the game 
and `id` is the id of the game room the user is joining

In the event that a user is already on the game room, the user won't be able to join again (logically) 
and you will be notified as expected.

If the user tries to join a game room that hasn't been created, he won't be able to and again,
you will be notified as expected.

### Delete a game room

You can delete a game room like this

```
localhost:8081/delete/gameRoom?id=1
```

where `id` is the ID of the game room you want to delete

If you try to delete a game room that doesn't exist, no action will be performed, and you will be notified as expected

### Leave a game room

You can leave a game room like this

```
localhost:8081/leave/gameRoom?id=2&userId=5
```

where `userId` is the ID of the user that is leaving the game 
and `id` is the id of the game room the user is leaving

If a user tries to leave a room that doesn't exist, no action will be performed and you will be notified as expected

If a user tries to leave a room where he doesn't belong, no action will be performed and you will be notified as expected

### Show a game room

You can see the users of a game room like this

```
localhost:8081/show/gameRoom?id=2
```

Where `id` is the id of the game room that you want to inspect

## Credits

* [Fraga, Matias](https://github.com/matifraga)

</br>