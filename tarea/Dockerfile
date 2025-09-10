FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/tarea-1.0-SNAPSHOT.jar app.jar

EXPOSE 35000

CMD ["java", "-jar", "app.jar", "eci.edu.arep.microspringboot.examples.GreetingController"]
