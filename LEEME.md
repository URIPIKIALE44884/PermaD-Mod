# Permadeath SMP - mod de Fabric (Minecraft 1.20.6)

## Que incluye
**Items (pestana "Hardcore" del Creativo, tambien en la busqueda; /give permadeath:...)**
- corazon_permanente: +1 corazon permanente (maximo 20). Stack 16.
- esencia_vital: gasta 1 corazon permanente, cura todo y da efectos. Stack 16. No sirve con Zombificacion.
- totem_memoria: conserva inventario y experiencia al morir (por jugador). NO apilable.
  Solo funciona si lo tenes EN LA MANO (principal o secundaria). Al reaparecer ves su animacion propia.
  Receta: fragmento de eco / totem de la inmortalidad / fragmento de eco (en columna).
- Pocion de Antidoto (normal, arrojadiza y persistente), stack 32.
- Pociones a 32; estofados y sopas en tazon a 16.
- Loot en cofres de estructuras: corazon 1/16, esencia 1/24.

**Corazones permanentes:** de 5 a 20 corazones; cada muerte resta 1. Minimo real: 3 (solo por Zombificacion).

**Zombificacion (infeccion zombi)** - se activa en la pantalla (Dificultad > Zombies > Activar infeccion)
- Cada golpe directo de zombie suma al contador (10 por defecto). Si pasan 3 minutos sin otro golpe
  de zombie, el contador vuelve a 0. Al llegar al limite => Zombificacion de 5 minutos (editable).
- Solo cuentan los golpes DIRECTOS: lo que bloquea el escudo no cuenta.
- No deja regenerar vida (ni natural, ni pociones, ni comida). Al llegar a 0 el jugador muere
  (en creativo y espectador no mata).
- Morir con el efecto: -2 corazones permanentes EN TOTAL (en vez de -1), hasta un minimo de 3.
- La leche NO lo borra. Cura: Pocion de Antidoto = soporte para pociones con una pocion de
  curacion o de regeneracion (cualquier nivel) + carne podrida.
- El jugador ve el temporizador (y los golpes acumulados, ej. 7/10) arriba a la izquierda.

**Pantalla de administracion** (solo operadores): `/function permadeath:menu`  o  `/permadeath menu`
- Pestana Mobs: elegir mob con los huevos de spawn, modelo 3D girando, y opciones: probabilidad,
  ARMADURA POR PIEZA (casco, peto, pantalones, botas) con 6 materiales (cuero, malla, oro, hierro,
  diamante, netherita): clic derecho activa/desactiva (X), clic central escribe la probabilidad, rueda +/-5%.
  Las probabilidades de una pieza se suman y no pasan de 100%; lo que falta es la chance de salir SIN esa pieza.
  Ej.: Casco -> 20 | X | X | 40 | 20 | 5. Chance de salir encantada (Proteccion I-IV: 50/30/15/5% por nivel).
  Efectos con nivel y %, dimensiones, equipo especial (zombies con arco, esqueletos con escudo y espada
  de piedra), radio del creeper. Botones Default (el mob elegido) y Reset (todos).
- Pestana Dificultad: infeccion zombi y sus numeros.
- Todo arranca en vanilla y solo afecta a mobs que aparecen DESPUES de configurar.
- Tambien hay comandos: /permadeath list | show <mob> | set ... | default <mob> | reset | infection <true|false>

**Zombie infectado:** si un jugador muere con Zombificacion (por cualquier causa), aparece en ese lugar un
zombie con SU skin, nombre en rojo con un icono de zombie, +2 filas de corazones, Fuerza I y Resistencia al fuego.

**Oleada** (pestana "Oleada" de la pantalla, o /permadeath wave start | cancel; solo operadores)
- Se genera para TODOS los jugadores conectados. Cuenta regresiva de 3 minutos (rojo y en negrita sobre la barra de inventario).
- Cantidad configurable de cada mob (por defecto 10 zombies, 12 esqueletos, 5 aranas invisibles y rapidas,
  5 creepers que no rompen bloques y solo danan a jugadores, 2 esqueletos Wither). 0 = no aparece.
- Si el jugador sobrevive 5 minutos de oleada activa, los mobs desaparecen y la oleada termina.
- Aparecen en tandas, a 10-15 bloques del jugador. Persiguen SOLO a su jugador aunque los ataquen.
- Ponen andamios para subir y rompen bloques (velocidad de herramienta de diamante).
- Durante la oleada la Zombificacion necesita 25 golpes. Si el jugador muere o se desconecta, la oleada se cancela.

**Drops:** las armaduras puestas por el sistema que un zombie no podria tener de forma natural (netherita)
nunca se sueltan, tampoco si el zombie se convierte en ahogado. El resto solo se suelta con "Drop del equipo".

## Iceologer e Ilusioner (integrados en PermaDeath)
Adaptado de "Iceologer Mod" (NeoForge 1.21.4, MCreator, licencia Ms-RL, autores CoverWeb/MCreator): se tomo su
funcionamiento (stats, melee, hielo al tocar/caer, reglas de juego) y se ESCRIBIO DE NUEVO para Fabric 1.20.6;
el MODELO, las ANIMACIONES (reposo y caminata) y la TEXTURA son los ORIGINALES del mod, portados a Fabric
y distribuidos bajo su licencia Ms-RL (ver LICENSE-MS-RL.txt y NOTICE.md).
- Iceologer: illager cuerpo a cuerpo (10 de vida, dano 3). Suelta hielo compacto y el libro Ice Aspect I.
  Reglas de juego: dropIceChunks (por defecto true) e iceologerTurnsBlocksTouchedWhenFallingIntoIce (false).
  Huevo de aparicion en la pestana Hardcore. Aparece en iglus pequenos de laderas nevadas y picos helados.
- Ice Aspect I (espadas): ralentiza (Lentitud I) + congelamiento tipo nieve en polvo 5 s. Ice Aspect II (dos
  libros I en un yunque): Lentitud II + congelamiento 10 s. No se combina con Aspecto de Fuego.
- Ilusioner: mantiene su comportamiento. Suelta un arco con Multishot (3-5 flechas, gasta una sola flecha,
  incompatible con Infinity y Mending). Puede aparecer solo en el bosque oscuro.
- Raids: Iceologer e Ilusioner se suman a las oleadas de raid con probabilidad independiente.
- Menu (pestana "Illagers"): % de iglus, % de Ilusioner natural, % en raids de cada uno.
  Tambien estan en la pestana Mobs (efectos y dimensiones) como el resto.
- La generacion natural solo afecta a chunks NUEVOS.

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
