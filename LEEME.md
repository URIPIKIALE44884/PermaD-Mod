# Permadeath SMP - mod de Fabric (Minecraft 1.20.6) - Etapa 1

Incluye: Corazon Permanente, Esencia Vital, Totem de Memoria (items reales, stack 16),
pestana "Hardcore" en el Creativo (con su icono de Corazon Hardcore) visible tambien en la
busqueda, corazones permanentes por jugador (5 a 20), keepInventory por jugador con el
Totem de Memoria, loot en cofres de estructuras y pociones apilables hasta 32.

## Como obtener el .jar (sin instalar nada)

1. Crea un repositorio en https://github.com (cuenta gratis) y subi TODO el contenido de esta carpeta.
   - Si la carpeta oculta `.github` no se sube, en GitHub usa "Add file > Create new file",
     escribi como nombre `.github/workflows/build.yml` y pega el contenido de `build.yml`.
2. Entra a la pestana **Actions**. El build corre solo (tarda unos minutos).
3. Cuando termine con tilde verde, abri esa ejecucion y baja **permadeath-jar** (abajo, en "Artifacts").
4. Descomprimi y usa el archivo `permadeath-1.0.0.jar` (NO el que dice `-sources`).
5. Si el build falla (X roja): abri el paso "Compilar", copia el error y pegamelo.

## Instalar

Poner el .jar en la carpeta `mods` del servidor y de cada jugador (hace falta Fabric API).
Si tenias el jar anterior de solo datos, sacalo para que no se mezclen los items viejos.

## Comandos

/give @s permadeath:corazon_permanente
/give @s permadeath:esencia_vital
/give @s permadeath:totem_memoria
