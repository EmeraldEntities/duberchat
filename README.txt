# duberchat
Duber's brand new open-source chat service!
 
In duberchat, we prioritize making new friends and new conversations before anything else.
That's why we allow you to make channels with any existing user, and we set it so that friends
events are automatically accepted. We think these changes will help new users get to know more people.

## Creators
Paula Yuan - Server-side code, involving handling IO, events, and server handlers.
Joseph Wang - Client-side code, involving GUI, events, and client handlers.

## The Pyjama protocol
The PJ (Pyjama) protocol is the primary protocol used to communicate between server and client
sockets. The protocol was named after duberchat's two main creators, Paula Yuan (P) and Joseph Wang (J).
The rules of the protocol are fairly simplistic:

1. Every thing that is sent over must be a predefined `Event` object.
2. Follow the first rule.

The protocol also defines some ground rules for certain events, which are listed below.

#### Login
The login sequence proceeds as following:
1. The client initiates a connection with the server, who handles the connection.
2. The client sends a `ClientLoginEvent` with login details.
3. The server receives the event, checks authentication details, and either:
    - accepts the login request and returns an `AuthSucceedEvent` with necessary information.
    - denies the login request, in which an `AuthFailedEvent` is sent and the client restarts step 3.

The client is now logged in.

#### Message Sending
Message sending works as follows:
1. The client types up a message and hits send, which sends a `MessageSentEvent`.
2. The server reciprocates, and sends back the event **back to the client** and to every person in the channel.
3. The client receives the event, and updates the display.

This change, while confusing to some, is designed to ensure synchronization happens with messages
no matter what. Everyone will get the messages in the same order, which is something we desired.

#### Other Events
Generally, all other events follow this similar protocol:
1. The client sends some sort of request.
2. The server reciprocates, and sends back the event and forwards it to everyone who needs the
event (eg. people in the same channel, friends, etc.)
3. The client receives the event, and makes the changes.

If a specific event is not listed here, it is safe to assume that it follows this similar protocol.
