# AREP-Lab4-Modularizacion
# Hecho por Alejandro Prieto
--- 

![Prueba contenedor](/img/image.png)
Pruebas de que el contenedor en Docker funciona en EC2.

![Prueba de funcionamiento de contenedor](/img/image-2.png)

Repositorio: [GitHub](https://github.com/AlejandroPrieto82/AREP-Lab4-Modularizacion/tree/develop)

---

## Descripción del proyecto

Este proyecto consiste en un **microframework web en Java** (tipo Spring Boot) sin utilizar Spring, que permite definir controladores y endpoints mediante anotaciones personalizadas.
La aplicación se puede ejecutar en Docker y desplegar en una instancia de AWS EC2, con manejo de rutas estáticas y dinámicas.

---

## Getting Started

Estas instrucciones permitirán obtener una copia del proyecto en tu máquina local para desarrollo y pruebas.

### Prerequisitos

* Java 17
* Maven
* Docker
* Acceso a AWS EC2 (opcional, para despliegue remoto)

---

### Instalación local

1. Clona el repositorio:

```bash
git clone https://github.com/AlejandroPrieto82/AREP-Lab4-Modularizacion.git
cd AREP-Lab4-Modularizacion/tarea
```

2. Compila el proyecto con Maven:

```bash
mvn clean package
```

3. Ejecuta la aplicación localmente:

```bash
java -cp target/classes:target/dependency/* eci.edu.arep.microspringboot.MicroSpringBoot eci.edu.arep.microspringboot.examples.GreetingController
```

4. Prueba el endpoint `greeting`:

```bash
curl http://localhost:35000/greeting
# Debe responder: Hola World
```

---

### Uso de las imágenes Docker públicas

Ya existen imágenes disponibles en Docker Hub, por lo que puedes ejecutar directamente sin compilar:

1. **Imagen `tarea`:**

[https://hub.docker.com/r/samuelprietor/tarea](https://hub.docker.com/r/samuelprietor/tarea)

```bash
docker pull samuelprietor/tarea:latest
docker run -d -p 35000:35000 samuelprietor/tarea:latest
```

2. **Imagen `arep-lab4-modularizacion`:**

[https://hub.docker.com/repository/docker/samuelprietor/arep-lab4-modularizacion/general](https://hub.docker.com/repository/docker/samuelprietor/arep-lab4-modularizacion/general)

```bash
docker pull samuelprietor/arep-lab4-modularizacion:latest
docker run -d -p 35000:35000 samuelprietor/arep-lab4-modularizacion:latest
```

Después, prueba el endpoint desde tu navegador o `curl`:

```
http://localhost:35000/greeting
```

---

### Despliegue en AWS EC2

1. Lanza una instancia EC2 con Linux.
2. Configura tu Security Group para permitir el puerto TCP **35000** desde cualquier IP (`0.0.0.0/0`).
3. Conéctate a la instancia mediante SSH:

```bash
ssh -i "AWSLab4.pem" ec2-user@ec2-54-172-241-48.compute-1.amazonaws.com
```

4. Instala Docker si no está presente:

```bash
sudo yum update -y
sudo amazon-linux-extras install docker
sudo systemctl start docker
sudo usermod -aG docker ec2-user
```

5. Ejecuta tu contenedor usando las imágenes públicas:

```bash
docker pull samuelprietor/tarea:latest
docker run -d -p 35000:35000 samuelprietor/tarea:latest
```

6. Accede desde tu navegador al endpoint público:

```
http://ec2-54-172-241-48.compute-1.amazonaws.com:35000/greeting
```

---

## Pruebas

* Local: `curl http://localhost:35000/greeting` → `"Hola World"`
* Remoto (EC2): `http://ec2-54-172-241-48.compute-1.amazonaws.com:35000/greeting`

---

## Built With

* [Maven](https://maven.apache.org/) - Gestión de dependencias y compilación
* [Docker](https://www.docker.com/) - Contenerización de la aplicación
* [AWS EC2](https://aws.amazon.com/ec2/) - Despliegue en la nube

---

## Authors

*  [**Alejandro Prieto**](https://github.com/AlejandroPrieto82) - Desarrollo del microframework y despliegue en AWS 



---

## License

Este proyecto está bajo licencia MIT - ver [LICENSE.md](LICENSE.md) para más detalles.

---

## Acknowledgments

* Inspiración en frameworks como Spring Boot para microservicios.
* Tutoriales de Docker y AWS EC2 para despliegue en la nube.
