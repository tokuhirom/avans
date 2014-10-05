# <%= @project %>

## debug run

    mvn exec:java -Dexec.mainClass=<%= @pkg %>.Main

## make jar

    mvn package

## run program

    java -jar target/<%= @project %>-0.0.1-SNAPSHOT.jar

