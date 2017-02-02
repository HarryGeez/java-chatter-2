# java-chatter-2
Reworked version of java-chatter. Restarted almost from scratch.

This is a one-to-many messaging system that uses a centralized server to pair up users. Currently the system has the following features
* One-to-one messaging
* Message broadcasting using multicast
* Real-time voice chatting
* Queueing system

Currently the system is designed in such a way that it is more suitable for a customer support system because the system is designed for that, but that can be changed.

## Building
This program is written in `IntelliJ` and no 3rd party libraries or dependencies are required as the code is vanilla `Java 8`. You might have problems building with other IDEs because the GUI part is designed using IntelliJ's Form Designer. Earlier versions of Java might not work as some features such as *lambda expressions* are used.
