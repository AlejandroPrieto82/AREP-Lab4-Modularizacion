# AREP-Lab4-Modularizacion

java -cp "target/classes;target/dependency/*" eci.edu.arep.RestServiceApplication

docker build --tag dockersparkprimer . 
docker run -d -p 34000:6000 --name firstdockercontainer dockersparkprimer