# NoCurtItem — мод Forge 1.16.5

Игрок не может подбирать предметы, добавленные в список в меню мода.

## Как пользоваться

- **Правый Ctrl** — открыть меню.
- В строке поиска ввести ID предмета (например `minecraft:diamond` или `diamond`) и нажать **Добавить** или Enter.
- В таблице отображаются заблокированные предметы. Кнопка **Удалить** убирает предмет из списка.
- Список сохраняется в `config/nocurtitem_blocked.txt`.

## Сборка в JAR

1. Установите [Gradle](https://gradle.org/install/) (6.8+ или 7.x) или скопируйте папку `gradle` и файлы `gradlew`, `gradlew.bat` из [Forge MDK 1.16.5](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.16.5.html).

2. Если Gradle установлен, один раз выполните в папке проекта:
   ```
   gradle wrapper
   ```

3. Сборка мода:
   ```
   python build_mod.py
   ```
   или:
   ```
   gradlew.bat build
   ```
   (на Linux/Mac: `./gradlew build`)

Готовый JAR будет в `build/libs/` и скопирован в `build/output/`. Скопируйте его в `.minecraft/mods/`.

## Требования

- Minecraft 1.16.5
- Forge 36.2.34 (или совместимая версия)
