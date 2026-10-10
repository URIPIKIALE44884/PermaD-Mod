# Avisos y creditos

## Iceologer Mod (parte derivada)

El modelo, las animaciones y la textura del Iceologer provienen de **"Iceologer Mod"**
(NeoForge 1.21.4), de **CoverWeb** y **MCreator**, publicado bajo la
**Microsoft Reciprocal License (Ms-RL)**. Creditos del mod original: "Credits Belong To Mojang and
using MCreator for creating the mod" (el Iceologer es un personaje de Minecraft Dungeons).

Archivos de PermaDeath que contienen datos derivados de ese mod (siguen bajo la Ms-RL; ver LICENSE-MS-RL.txt):

- `src/main/java/com/permadeath/client/IceologerModel.java` - geometria del modelo, portada a Fabric 1.20.6 (nombres Yarn).
- `src/main/java/com/permadeath/client/IceologerAnimations.java` - animaciones "walk" e "idle", portadas.
- `src/main/resources/assets/permadeath/textures/entity/iceologer.png` - textura original, sin modificar.

El resto del codigo de PermaDeath (incluida la entidad IceologerEntity, que se escribio de nuevo
siguiendo el comportamiento del mod original) es trabajo propio y no esta cubierto por la Ms-RL.

Nota: el texto de LICENSE-MS-RL.txt es el texto estandar de la licencia, escrito de memoria por el
asistente. Conviene compararlo con el oficial (https://opensource.org/license/ms-rl-php) antes de redistribuir.

## Wildfire assets adapted from ebs_wildfire
The original Wildfire texture, geometry JSON, and animation JSON were extracted from `ebs_wildfire-2.0.1-neoforge-1.21.1hotfix.jar` at the user's request. The upstream mod declares GPL-3.0. These files retain that license; do not redistribute this combined project publicly without reviewing the implications for the project's licensing. The gameplay entity implementation in this project is a separate Fabric reimplementation.
