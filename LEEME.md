# Permadeath SMP - mod de Fabric (Minecraft 1.20.6)

## Que incluye
**Items (pestana "Hardcore" del Creativo, tambien en la busqueda; /give permadeath:...)**
- corazon_permanente: +1 corazon permanente (maximo 20). Stack 16.
- esencia_vital: gasta 1 corazon permanente, cura todo y da efectos. Stack 16. No sirve con Zombificacion.
- totem_memoria: conserva inventario y experiencia al morir (por jugador). NO apilable.
  Receta: fragmento de eco / totem de la inmortalidad / fragmento de eco (en columna).
- Pocion de Antidoto (normal, arrojadiza y persistente), stack 32.
- Pociones a 32; estofados y sopas en tazon a 16.
- Loot en cofres de estructuras: corazon 1/16, esencia 1/24.

**Corazones permanentes:** de 5 a 20 corazones; cada muerte resta 1. Minimo real: 3 (solo por Zombificacion).

**Zombificacion (infeccion zombi)** - se activa en la pantalla (Dificultad > Zombies > Activar infeccion)
- 10 golpes de zombies en 3 minutos (editable) => efecto Zombificacion de 5 minutos (editable).
- No deja regenerar vida (ni natural, ni pociones, ni comida). Al llegar a 0 el jugador muere.
- Morir con el efecto: -2 corazones permanentes EN TOTAL (en vez de -1), hasta un minimo de 3.
- La leche NO lo borra. Cura: Pocion de Antidoto = soporte para pociones con una pocion de
  curacion o de regeneracion (cualquier nivel) + carne podrida.
- El jugador ve el temporizador (y los golpes acumulados, ej. 7/10) arriba a la izquierda.

**Pantalla de administracion** (solo operadores): `/function permadeath:menu`  o  `/permadeath menu`
- Pestana Mobs: elegir mob con los huevos de spawn, modelo 3D girando, y opciones: probabilidad,
  armadura por niveles (hierro/diamante/netherita con su %), efectos con nivel y %, dimensiones,
  equipo especial (zombies con arco, esqueletos con escudo y espada de piedra), radio del creeper.
  Botones Default (el mob elegido) y Reset (todos).
- Pestana Dificultad: infeccion zombi y sus numeros.
- Todo arranca en vanilla y solo afecta a mobs que aparecen DESPUES de configurar.
- Tambien hay comandos: /permadeath list | show <mob> | set ... | default <mob> | reset | infection <true|false>

## Todavia no hace (etapa 4)
- Zombies con arco disparan, esqueletos levantan el escudo, daño de explosion del creeper e ignorar escudo,
  mejoras de IA, pacificos que se defienden.
- Spawns NUEVOS en otras dimensiones requieren reiniciar el servidor.

## Como obtener el .jar (sin instalar nada)
1. Subi TODO el contenido de esta carpeta a tu repositorio de GitHub (reemplazando archivos).
2. Pestana Actions: el build corre solo. Con tilde verde, baja el artifact "permadeath-jar".
3. Usa permadeath-1.0.0.jar (no el -sources).
4. Si falla (X roja): abri el paso "Compilar", copia el error y pegalo en el chat.

## Instalar
.jar en la carpeta mods del servidor y de cada jugador (hace falta Fabric API).
Si tenias el jar viejo de solo datos, sacalo.
