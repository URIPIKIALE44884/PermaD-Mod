package com.permadeath;

/** Reglas globales de dificultad (pestana "Dificultad"). Todo apagado por defecto. */
public class GlobalSettings {
    /** Zombificacion: activar o no. */
    public boolean infectionEnabled = false;
    /** Golpes de zombies necesarios para infectarse. */
    public int infectionHits = 10;
    /** Ventana de tiempo para contar los golpes (segundos). */
    public int infectionWindowSeconds = 180;
    /** Duracion de la Zombificacion (segundos). Al llegar a 0 mata al jugador. */
    public int infectionDurationSeconds = 300;

    public void fix() {
        infectionHits = Math.max(1, Math.min(30, infectionHits));
        infectionWindowSeconds = Math.max(30, Math.min(600, infectionWindowSeconds));
        infectionDurationSeconds = Math.max(60, Math.min(900, infectionDurationSeconds));
    }
}
