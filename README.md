# Marce Client

Независимая открытая основа клиентского мода для **Minecraft 1.21.11 / Fabric / Java 21**.
Свой интерфейс и ресурсы; код Lunar Client не используется. Название рабочее:
проверка товарных знаков и сходных названий не проводилась.

## Запуск

Установить JDK 21. Из папки проекта в PowerShell:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
```

В игре нажать **Right Shift**, чтобы открыть ClickGUI; **Escape** закрывает.
Клавишу можно изменить в настройках управления Minecraft. Для установки взять
`build/libs/bunar-client-0.2.0.jar`, установить Fabric Loader 0.18.4+ и Fabric API
для 1.21.11, затем поместить мод в `mods`. Sources JAR устанавливать не нужно.

## Структура

```text
src/main/java/dev/bunar/client/
  BunarClient.java          # регистрация, клавиши, жизненный цикл
  ui/                      # Screen, категории, адаптивная геометрия, painter
  render/                  # неизменяемый render state, PIP, пакетные прямоугольники
    blur/                  # Gaussian kernel и двухпроходный GPU blur
    font/                  # TTF/NanoVG, один atlas/context
    gl/                    # shader lifecycle и сохранение состояния OpenGL
src/main/resources/
  fabric.mod.json
  assets/bunar/
    fonts/                 # Inter TTF (SIL OFL)
    icons/                 # собственные SVG-иконки; GUI рисует эквиваленты пакетно
    shaders/               # GLSL 330 core
    lang/                  # RU/EN для клавиши
src/test/java/             # CPU-тесты и настоящие GPU-тесты
licenses/                  # полные лицензии внешних компонентов
docs/                      # архитектура и результаты проверок
```

## Рендер

Minecraft GUI extraction → неизменяемый `BunarRenderState` → PIP renderer →
blur мира → пакет прямоугольников → TTF → штатное GUI compositing Minecraft.
Прямоугольники сохраняют порядок слоёв; до 4096 штук в одном draw call.
GPU-буферы и targets переиспользуются, targets меняются только при resize.
Невидимые, пустые и полностью прозрачные прямоугольники отбрасываются.
Нет искусственного лимита в 144 FPS из исходного примера.

Blur — два прохода в половинном разрешении (13 выборок на проход).
Один CPU worker заранее готовит Gaussian kernel, OpenGL выполняется только
на render thread. Это **не два потока, одновременно вызывающих OpenGL**,
и вычисление kernel в worker само по себе не считается ускорением GPU blur.
На backend, отличном от OpenGL, этот renderer пока не поддерживается.

## Проверки

```powershell
.\gradlew.bat test
.\gradlew.bat gpuTest
```

`gpuTest` создаёт скрытое окно GLFW с настоящим OpenGL-контекстом.
Проверяет альфу/порядок слоёв, границу ёмкости batch, оба направления blur,
нормализацию, resize/reuse, компиляцию GLSL, TTF и отсутствие GL errors.
Сохраняет preview и замеры в `build/reports/bunar/`. Это синтетический benchmark,
не FPS Minecraft. Историю выполненных проверок см. `docs/VALIDATION.md`.

В меню можно настроить размытие, затемнение, масштаб, палитру и HUD с FPS и координатами.
Клавиши **1–3** переключают разделы. Настройки сохраняются в `config/marce-client.properties` внутри папки игры.
