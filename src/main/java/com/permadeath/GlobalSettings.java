package com.permadeath;

/** Reglas globales de dificultad (pestanas "Dificultad" y "Oleada"). Todo apagado por defecto. */
public class GlobalSettings {
    /** Zombificacion: activar o no. */
    public boolean infectionEnabled = false;
    /** Golpes directos de zombies necesarios para infectarse. */
    public int infectionHits = 10;
    /** Si pasan estos segundos sin recibir un golpe de zombie, el contador vuelve a 0. */
    public int infectionWindowSeconds = 180;
    /** Duracion de la Zombificacion (segundos). Al llegar a 0 mata al jugador. */
    public int infectionDurationSeconds = 300;

    /** Oleada: cantidad de cada tipo de mob (0 = no aparece). */
    public int waveZombieCount = 10;
    public int waveSkeletonCount = 12;
    public int waveSpiderCount = 5;
    public int waveCreeperCount = 5;
    public int waveWitherCount = 2;

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    public void fix() {
        infectionHits = clamp(infectionHits, 1, 30);
        infectionWindowSeconds = clamp(infectionWindowSeconds, 30, 600);
        infectionDurationSeconds = clamp(infectionDurationSeconds, 60, 900);
        waveZombieCount = clamp(waveZombieCount, 0, 60);
        waveSkeletonCount = clamp(waveSkeletonCount, 0, 60);
        waveSpiderCount = clamp(waveSpiderCount, 0, 30);
        waveCreeperCount = clamp(waveCreeperCount, 0, 30);
        waveWitherCount = clamp(waveWitherCount, 0, 10);
    }
}
