package eci.edu.arep.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class FestividadApi {

    public static String getFestividad(String festividad) {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaFestividad;

        switch (festividad.toLowerCase()) {
            case "fiesta":
                fechaFestividad = LocalDate.of(hoy.getYear(), 2, 25);
                break;
            case "navidad":
                fechaFestividad = LocalDate.of(hoy.getYear(), 12, 25);
                break;
            case "halloween":
                fechaFestividad = LocalDate.of(hoy.getYear(), 10, 31);
                break;
            default:
                return "{\"error\": \"Festividad no encontrada\"}";
        }

        if (fechaFestividad.isBefore(hoy)) {
            fechaFestividad = fechaFestividad.plusYears(1);
        }

        long diasRestantes = ChronoUnit.DAYS.between(hoy, fechaFestividad);

        return "{ \"festividad\": \"" + festividad + "\", \"diasRestantes\": " + diasRestantes + " }";
    }
}
