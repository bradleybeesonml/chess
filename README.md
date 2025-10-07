# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## Server Architecture Diagram

[Server Diagram](https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2AMQALADMABwATG4gMP7I9gAWYDoIPoYASij2SKoWckgQaJiIqKQAtAB85JQ0UABcMADaAAoA8mQAKgC6MAD0PgZQADpoAN4ARP2UaMAAtihjtWMwYwA0y7jqAO7QHAtLq8soM8BICHvLAL6YwjUwFazsXJT145NQ03PnB2MbqttQu0WyzWYyOJzOQLGVzYnG4sHuN1E9SgmWyYEoAAoMlkcpQMgBHVI5ACUpkRKjulXJKHqVGAyC06Ixbw+KDWv3+HDWYNOMDQEGYyXYAGtSToUcBhTAALLZVRIBw9IVoKUs2YoHocnY9HkIa7VUR3BHVZ4wABCwA4BKJYAAogAPFTYAgFfXUShGyrFcz1QJOJzDcZzdTAezzRa2qDeOowPQcGAowlqMBQsycUxuqiG+6yeRKFTqephsAAVQGzIGrNJucUyjUqkpRh0tQAYgr42XKDXY5YYGq5phxShJTBO7BNkgwPE+5X1TB6RKOL2UPa0Roa-n656qiIVPUxzXM9nje6YwoEIvLO1JegHU6XYUbh7SvdvRhfU5goGxsHVKHw8skbRvU-aGAuw5LjA8jCugqboBwmAbnW6jbtS9RoD4CB6tSjZIQWqi1CAEromOFZdto1baJuKH3MYtQKBwHYDIeOE5lRyEEURw7ogoPhThiwB8fENaUXmHGNnRDHxrxU4sQaFIvpUMJPDG2JoniaiYVgylwqhJoxq8s6fDA9T7CCglTu0EAwWgSymZcmbPq+yA+jA4T+t+oF2cswLLBZ8RWTZ3n7Fc8GmF4vgBNA7BhjELZwLa0hwAoMAADIQFkhRvswJ41PUzRtF0vQGOo+RoIGoHsvofw7FcT7wkpjxwi8lUwFqALQk1z5UvJNIwAgGUKhi6WZdayakkeCk9bufV0gycgoGR7zqlVWw7HyAqxggIpihKUqyqVirKqqRkau1CGsblpoWlaKBJjkd4oM6ZWOfCzklGAH4BqMP71v+exAdA9Rxgmd02nB6Zkr1uHsfhRYoKW5agaJtb4RJzYwG2nCjsx2iIbDW6KTuWZ7jj5HyDA0B8pheqDntZOwPyzAAGa+Oml3TXlo7TEJ0BIAAXigHCPc9rr1du2UfgAjN+v7-RGUZA32PNTnzgu7KFkOsZUeH1vUXELTJ8QCUJIn42JaO0RjUkwEbckzduOmmiNCoZKoWmYE7b2c6ahlTOqXzmUJgWwZCdX6RLLnvm5Hk-V5kJB5Z1mhyFaYIZ43h+P4XgoOgMRxIkOd5yNvhYNljb1fl0i2qltrtLa3Q9CV8oFMM-kh4+Ef3F7Lzt8naCdbC3XE0i-UZaXw3j3xY0kpNhhsRbetGCg3A8UJJtJzZKPUQ2Vu1BkMwQDQtum3jHMPEPMYl9PmkINpXXe8Tvvh6ekcffU7nfSMmvpxFWcovGfw2AFRSlSmiGAABxdUGhy5XRjA0CBddG72HVG3YO-dXqNh7jAEYfcbKDxUhXXq9RkA5CgcGYaaJyFqBnmAUk2sZAE0LDAGoSBmaWCNhvAK-dt7iT3ljaSp95Dm1RoTXKo87Zn2hgvURzDSFgGoaoDEvDLaVDohQbg4BIHqhrA0ToIid56RmvUahuj9Hn2wWAsh0C3Yey9kYrm4wUHBgWA0Fw7jOgv1uETSWMcv5jGcWoVx7iXCeLTuFTOAQOAAHY3BOBQE4GItpghwASgANngMRQw1CYBFCjjlH28DWgdGQaglW3CbKBkCQAOXVF44e2DcHoJsmsGpdTPYPyIcYmABt0TUIxHALJ1DaETQYWhFh9IkCMkWm0uYG1BTbRVLtYc+05QKnsEqRZUpZliHPpXc0lpaEiwfJgnx+Svqyz+mGAGisYwg0TODH+UMHYyJ3rSSgbCOHrzwegFRhM1HNgESfWSUiXk6yYQRHpQz1QYlMRRAxfCAX0UYtouYh5dY0XEaTQJABJaQc9HYP3qIM7iKBhm33vpfBxvsAnqjxQsGAUtwjBHqY-Xxn9vy4ukAyplwRAhPIzpFfwlgV4DU2PnJACQwAiqwhAcVAApCACpUWGH8EKEAwo8kfS6VzZoJYio9ECWgze6BAzYG2iKqAcAIADSgK0ul0hWVYKJTgn5aB7VzDxWsEANroAEN0livqAArJVaB+mKoVOS+6dDnkk3npzUec0pkLQxOa4AlrrW2raiiAwGAPUoDxTAbIRaMCZEoMskcB15QODahaygmboCGodZTcc3ga3SuwGoAlRN9k3SOY6J6JzxZnPfjAP0-i5bXIVsBWMlpQbRohunbt4LF7MNYewzhbq-k0SRZjdswLhJ4wxbvQN9RJEUyphhLCdMVkwBxWgJN8YqYzDWe2jBez9L7nKWrIWxyXrDveq5P0MsfqTvDGMQGMYfDfqgALIWi7Y3ZgTaTR9C0G1QAxD6rNyqRgAHUWA4vrmsM0qUFBwAANIXArVKe9j6em+tgHNVI3a4H1D7WDZMf6xZd0A9HcdlyQxTog7c4Gc6HnJgQ8uxhq7IXFn6Vy7du9d1ArhcI49DjR6qeAC26m16hwjhyUzGArNoMXWkaxg5t1o1cc7q-EdQHY5BiueByDon4ziZyJJsZxCenxBQBqigcgFB0lOLobgGJAneoY9R+ADH5wXggr2aC6AWOFLPQly0V4bxoBs6c3jn0x2fgE3+ITrn4uXigtlrz0iV2yMhX4aZ8mHXsjrVahjaxkYItUU2fc2BpkqvtnGwll96gRrDeqWxd8OlUp7Z+nBTr7PRw5aMAVgqs5eHTRKqVG3eyIGHLAYA2BzWEDyAUXJsC0uNGrrXeujdjCnMaiN+b03CGnp6dwPAyipPHv1h9qA54EBffU3vGQNc662kxmkFo0oZyUFUAAblBzdiHLYocw8CQjpH4PIfQ-nEJeHqWL4qWJX9yblLXsWe-nlr05y-GBieUAA)



## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
