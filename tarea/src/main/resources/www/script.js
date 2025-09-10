const temas = ["fiesta", "navidad", "halloween"];
let index = 0;

document.getElementById("btnCambiar").addEventListener("click", async () => {
    let tema = temas[index];
    document.body.className = tema;

    // Llamar al backend REST
    try {
        let response = await fetch("/api/festividad?name=" + tema);
        let data = await response.json();

        if (data.diasRestantes !== undefined) {
            document.getElementById("respuesta").innerText =
                "Faltan " + data.diasRestantes + " días para " + tema + " 🎉";
        } else {
            document.getElementById("respuesta").innerText = "Festividad no encontrada 😢";
        }
    } catch (error) {
        document.getElementById("respuesta").innerText = "Error al consultar API";
    }

    index = (index + 1) % temas.length;
});
