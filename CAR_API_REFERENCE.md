# Car API Properties Reference
## Geely Galaxy Starship 7 (P145) - FlymeAuto

## Климат-контроль (HVAC)

### HVAC_AC_ON
- **Property ID:** `354419973` (`0x15200505`)
- **Тип:** `BOOLEAN`
- **Доступ:** `READ_WRITE`
- **Описание:** Включение/выключение кондиционера
- **Зона:** `117` (комбинированная зона)
- **Текущее значение:** `true`
- **Использование:**
  ```kotlin
  vehiclePropertyHelper.setBoolProperty(VehiclePropertyIds.HVAC_AC_ON, 117, true)
  ```

### HVAC_AUTO_ON
- **Property ID:** `354419978` (`0x1520050A`)
- **Тип:** `BOOLEAN`
- **Доступ:** `READ_WRITE`
- **Описание:** Автоматический режим климат-контроля
- **Зона:** `1` (водитель)
- **Текущее значение:** `false`

### HVAC_POWER_ON
- **Property ID:** `354419984` (`0x15200510`)
- **Тип:** `BOOLEAN`
- **Доступ:** `READ_WRITE`
- **Описание:** Питание системы климат-контроля
- **Зона:** `5`
- **Текущее значение:** `true`

### HVAC_TEMPERATURE_SET
- **Property ID:** `358614275` (`0x15600503`)
- **Тип:** `FLOAT`
- **Доступ:** `READ_WRITE`
- **Описание:** Установка температуры
- **Диапазон:** `15.5°C - 28.5°C`
- **Шаг:** `0.5°C`
- **Зона:** `1` (водитель)
- **Текущее значение:** `23.0°C`
- **Использование:**
  ```kotlin
  vehiclePropertyHelper.setFloatProperty(
      VehiclePropertyIds.HVAC_TEMPERATURE_SET,
      1,  // area ID (водитель)
      22.5f
  )
  ```

### HVAC_FAN_SPEED
- **Property ID:** `356517120` (`0x15400500`)
- **Тип:** `INT`
- **Доступ:** `READ_WRITE`
- **Описание:** Скорость вентилятора
- **Диапазон:** `1 - 9`
- **Зона:** `5`
- **Текущее значение:** `1`

### HVAC_FAN_DIRECTION
- **Property ID:** `356517121` (`0x15400501`)
- **Тип:** `INT`
- **Доступ:** `READ_WRITE`
- **Описание:** Направление потока воздуха
- **Зона:** `1`
- **Текущее значение:** `7`
- **Значения:**
  - `1` = В лицо (салон/внутрь)
  - `2` = На ноги (вниз)
  - `4` = На стекло (вверх/лобовое)
  - `3` = В лицо + на ноги (1+2)
  - `5` = В лицо + на стекло (1+4)
  - `6` = На ноги + на стекло (2+4)
  - `7` = Везде (1+2+4)

### HVAC_DEFROSTER
- **Property ID:** `320865540` (`0x13200504`)
- **Тип:** `BOOLEAN`
- **Доступ:** `READ_WRITE`
- **Описание:** Обдув стекол
- **Зоны:** `1` (переднее), `2` (заднее)
- **Текущее значение:** `false`

---

## Окна и люк

### WINDOW_POS (WINDOW_CONTROL)
- **Property ID:** `322964416` (`0x13400BC0`)
- **Тип:** `INT`
- **Доступ:** `READ_WRITE`
- **Описание:** Управление позицией окон и люка
- **Диапазон значений:** `0-100`
  - `0` = полностью закрыто
  - `100` = полностью открыто
- **Шаг:** `4` (рекомендуемый)
- **Операционный диапазон:** `12-88` (физические ограничения моторов)
- **Зоны:**
  - `16` = Переднее левое окно
  - `64` = Переднее правое окно
  - `256` = Заднее левое окно
  - `1024` = Заднее правое окно
  - `65536` = Люк
  - `131072` = Шторка люка
- **Текущие значения:** `16=0, 64=0, 256=12`
- **Особенности:**
  - При открытии люка автоматически открывается шторка (люк + 4 шага)
  - При закрытии люка шторка не трогается
- **Handler:** `WindowControlHandler`
- **Использование:**
  ```kotlin
  // Открыть переднее левое окно на 50%
  vehiclePropertyHelper.setIntProperty(322964416, 16, 50)

  // Полностью закрыть люк
  vehiclePropertyHelper.setIntProperty(322964416, 65536, 0)

  // Приоткрыть окно (минимальная позиция)
  vehiclePropertyHelper.setIntProperty(322964416, 16, 12)
  ```

### WINDOW_MOVE
- **Property ID:** `322964417` (`0x13400BC1`)
- **Тип:** `INT`
- **Доступ:** `READ_WRITE`
- **Описание:** Движение окон (команды)
- **Зоны:** `65536` (люк), `131072` (шторка)

---

## Двери и багажник

### DOOR_POS
- **Property ID:** `373293824` (`0x16400B00`)
- **Тип:** `INT`
- **Доступ:** `READ_WRITE`
- **Описание:** Позиция дверей
- **Диапазон:** `0-1`
- **Зоны:**
  - `1` = Передняя левая
  - `4` = Передняя правая
  - `16` = Задняя левая
  - `64` = Задняя правая
- **Текущие значения:** Все = `2` (закрыто)

### DOOR_LOCK
- **Property ID:** `373031682` (`0x16200B02`)
- **Тип:** `BOOLEAN`
- **Доступ:** `READ`
- **Описание:** Состояние замков дверей
- **Зоны:** `1` (водитель), `4` (пассажир)
- **Текущие значения:** Все = `true` (заперто)

### DOOR_MOVE (TRUNK_CONTROL)
- **Property ID:** `373295873` (`0x16400B01`)
- **Тип:** `INT`
- **Доступ:** `READ_WRITE`
- **Описание:** Управление багажником
- **Зона:** `536870912` (багажник)
- **Значения:**
  - `0` = Закрыть
  - `1` = Открыть
- **Текущее значение:** `0`
- **⚠️ Безопасность:** Блокируется при скорости > 5 км/ч
- **Handler:** `TrunkControlHandler`
- **Использование:**
  ```kotlin
  // Проверка скорости перед открытием
  val speed = vehiclePropertyHelper.getFloatProperty(VehiclePropertyIds.PERF_VEHICLE_SPEED, 0)
  if (speed < 5.0f) {
      vehiclePropertyHelper.setIntProperty(373295873, 536870912, 1)  // открыть
  }
  ```

### FUEL_DOOR_OPEN
- **Property ID:** `287310600` (`0x11200308`)
- **Тип:** `BOOLEAN`
- **Доступ:** `READ_WRITE`
- **Описание:** Крышка топливного бака
- **Зона:** `0`
- **Текущее значение:** `false`
- **Handler:** `FuelDoorHandler`
- **Использование:**
  ```kotlin
  vehiclePropertyHelper.setBoolProperty(287310600, 0, true)  // открыть лючок
  ```

---

## Сиденья

### HVAC_SEAT_TEMPERATURE (SEAT_HEATING)
- **Property ID:** `356517131` (`0x1540050B`)
- **Тип:** `INT`
- **Доступ:** `READ_WRITE`
- **Описание:** Обогрев сидений
- **Диапазон:** `0-3`
  - `0` = Выключено
  - `1` = LOW
  - `2` = MEDIUM
  - `3` = HIGH
- **Зоны:**
  - `1` = Водитель
  - `4` = Пассажир
- **Текущее значение:** `0`
- **Handler:** `SeatClimateHandler` / `SeatHeatingController`
- **Использование:**
  ```kotlin
  // Включить обогрев сиденья водителя на максимум
  vehiclePropertyHelper.setIntProperty(356517131, 1, 3)

  // Выключить обогрев
  vehiclePropertyHelper.setIntProperty(356517131, 1, 0)
  ```

### HVAC_SEAT_VENTILATION (SEAT_VENTILATION)
- **Property ID:** `356517139` (`0x15400513`)
- **Тип:** `INT`
- **Доступ:** `READ_WRITE`
- **Описание:** Вентиляция сидений
- **Диапазон:** `0-3`
  - `0` = Выключено
  - `1` = LOW
  - `2` = MEDIUM
  - `3` = HIGH
- **Зоны:**
  - `1` = Водитель
  - `4` = Пассажир
- **Текущее значение:** `0`
- **Handler:** `SeatClimateHandler`

### SEAT_MASSAGE_SWITCH (STARSHIP)
- **Property ID:** `622883040` (`0x252070E0`)
- **Vendor Name:** `VENDOR_622883040`
- **Тип:** `BOOLEAN`
- **Доступ:** `READ_WRITE`
- **Описание:** Включение/выключение массажа сидений
- **Зоны:**
  - `1` = Водитель
  - `4` = Пассажир
- **Текущее значение:** `false`
- **Handler:** `SeatMassageController`
- **Использование:**
  ```kotlin
  // Включить массаж сиденья водителя
  vehiclePropertyHelper.setBoolProperty(622883040, 1, true)

  // Выключить массаж
  vehiclePropertyHelper.setBoolProperty(622883040, 1, false)
  ```

### SEAT_MASSAGE_POWER (STARSHIP)
- **Property ID:** `624980189` (`0x254070DD`)
- **Vendor Name:** `VENDOR_624980189`
- **Тип:** `INT`
- **Доступ:** `READ_WRITE`
- **Описание:** Мощность массажа сидений
- **Диапазон:** `0-3`
  - `0` = OFF
  - `1` = LOW
  - `2` = MEDIUM
  - `3` = HIGH
- **Зоны:**
  - `1` = Водитель
  - `4` = Пассажир
- **Текущее значение:** `area 1=3, area 4=3`
- **Handler:** `SeatMassageController`
- **Использование:**
  ```kotlin
  // Установить среднюю мощность массажа
  vehiclePropertyHelper.setIntProperty(624980189, 1, 2)
  ```

### SEAT_MASSAGE_TYPE (STARSHIP)
- **Property ID:** `624980193` (`0x254070E1`)
- **Vendor Name:** `VENDOR_624980193`
- **Тип:** `INT`
- **Доступ:** `READ_WRITE`
- **Описание:** Тип/режим массажа сидений
- **Диапазон:** `0-8` (разные режимы массажа)
  - Каждое значение соответствует определённому режиму массажа
  - Требует экспериментального определения каждого режима
- **Зоны:**
  - `1` = Водитель
  - `4` = Пассажир
- **Текущее значение:** `area 1=1, area 4=4`
- **Handler:** `SeatMassageController`
- **Использование:**
  ```kotlin
  // Установить режим массажа №3
  vehiclePropertyHelper.setIntProperty(624980193, 1, 3)
  ```
- **⚠️ Примечание:** Разные значения активируют разные режимы массажа (например, волновой, точечный, комбинированный). Точное назначение каждого режима требует тестирования.

### Полный пример: Включить массаж с настройками
```kotlin
// Включить массаж сиденья водителя с высокой мощностью и режимом №3
val driverSeat = 1

// 1. Включить переключатель массажа
vehiclePropertyHelper.setBoolProperty(622883040, driverSeat, true)

// 2. Установить высокую мощность
vehiclePropertyHelper.setIntProperty(624980189, driverSeat, 3)

// 3. Выбрать режим массажа
vehiclePropertyHelper.setIntProperty(624980193, driverSeat, 3)

// Или использовать контроллер:
val massageController = SeatMassageController(vehiclePropertyHelper)
massageController.enableDriverMassage(power = SeatMassage.POWER_HIGH)
```

---

## Освещение

### INTERIOR_LIGHTS (STARSHIP)
- **Property ID:** `356544592` (`0x15407050`)
- **Тип:** `INT` (предполагается)
- **Доступ:** `READ_WRITE` (предполагается)
- **Описание:** Освещение салона для STARSHIP
- **Значения:** `0` = выкл, `1` = вкл
- **Зоны:**
  - `1` = Зона 1
  - `2` = Зона 2
  - `4` = Зона 4
  - `16` = Зона 16
  - `64` = Зона 64
- **Handler:** `LightControlHandler`
- **Использование:**
  ```kotlin
  // Включить все зоны освещения
  listOf(1, 2, 4, 16, 64).forEach { areaId ->
      vehiclePropertyHelper.setIntProperty(356544592, areaId, 1)
  }
  ```

---

## Внешнее освещение

### EXTERIOR_LIGHT_CONTROL
- **Property ID:** `557871126` (`0x21407016`)
- **Vendor Name:** `VENDOR_557871126`
- **Тип:** `INT`
- **Доступ:** `READ_WRITE`
- **Описание:** Управление внешним освещением (фары, габариты)
- **Зона:** `0`
- **Значения:**
  - `0` = Выключено
  - `1` = Габаритные огни (parking lights)
  - `3` = Фары (headlights / ближний свет)
- **Текущее значение:** (зависит от текущего состояния)
- **Handler:** `ExteriorLightControlHandler`
- **Использование:**
  ```kotlin
  // Включить фары
  vehiclePropertyHelper.setIntProperty(557871126, 0, 3)

  // Включить габариты
  vehiclePropertyHelper.setIntProperty(557871126, 0, 1)

  // Выключить свет
  vehiclePropertyHelper.setIntProperty(557871126, 0, 0)
  ```

### EXTERIOR_FOG_CONTROL
- **Property ID:** `289410578` (`0x11400E12`)
- **Тип:** `INT` (предполагается)
- **Доступ:** `READ_WRITE` (предполагается)
- **Описание:** Управление противотуманными фарами
- **Зона:** `0`
- **Значения:**
  - `0` = Выключено
  - `1` = Включено
- **⚠️ Отсутствует в скане** - требует дополнительной проверки доступности
- **Handler:** `ExteriorLightControlHandler`

---

## Режим вождения

### DRIVE_MODE
- **Property ID:** `557871372` (`0x2140710C`)
- **Vendor Name:** `VENDOR_557871372`
- **Тип:** `INT`
- **Доступ:** `READ_WRITE`
- **Описание:** Режим вождения / Drive Mode
- **Зона:** `0`
- **Значения:**
  - `0` = Гибридный режим (Hybrid / HEV)
  - `2` = Спортивный режим (Sport)
  - `16` = Электрический режим (Electric / EV)
  - `24` = Экономичный режим (Eco / Intelligent / Adaptive)
- **Текущее значение:** (зависит от текущего режима)
- **Handler:** `DriveModeHandler`
- **Использование:**
  ```kotlin
  // Установить экономичный режим
  vehiclePropertyHelper.setIntProperty(557871372, 0, 24)

  // Установить спортивный режим
  vehiclePropertyHelper.setIntProperty(557871372, 0, 2)

  // Установить электрический режим
  vehiclePropertyHelper.setIntProperty(557871372, 0, 16)

  // Установить гибридный режим
  vehiclePropertyHelper.setIntProperty(557871372, 0, 0)
  ```
- **⚠️ Примечание:** Значения могут отличаться в зависимости от комплектации и типа гибридной системы.

---


## Дополнительно

### PERF_VEHICLE_SPEED
- **Property ID:** `291504647` (`0x11600207`)
- **Тип:** `FLOAT`
- **Доступ:** `READ`
- **Описание:** Текущая скорость автомобиля (км/ч)
- **Зона:** `0`
- **Использование:** Проверка перед выполнением операций (например, открытие багажника)


### Интересные неизвестные properties с диапазонами


#### VENDOR_559968667
- **Property ID:** `0x2160719B`
- **Тип:** `FLOAT`
- **Диапазон:** `30.0` - `85.0`
- **Предположение:** Поддержание уровня АКБ от ДВС

---

## Примеры использования

### Включить кондиционер и установить температуру
```kotlin
// Включить питание климат-контроля
vehiclePropertyHelper.setBoolProperty(VehiclePropertyIds.HVAC_POWER_ON, 5, true)

// Включить AC
vehiclePropertyHelper.setBoolProperty(VehiclePropertyIds.HVAC_AC_ON, 117, true)

// Включить авто-режим
vehiclePropertyHelper.setBoolProperty(VehiclePropertyIds.HVAC_AUTO_ON, 1, true)

// Установить температуру 22°C
vehiclePropertyHelper.setFloatProperty(VehiclePropertyIds.HVAC_TEMPERATURE_SET, 1, 22.0f)
```

### Открыть окна на 50%
```kotlin
val windows = listOf(16, 64, 256, 1024)  // все 4 окна
windows.forEach { areaId ->
    vehiclePropertyHelper.setIntProperty(322964416, areaId, 50)
}
```

### Включить обогрев сидений и руля
```kotlin
// Обогрев сиденья водителя на максимум
vehiclePropertyHelper.setIntProperty(356517131, 1, 3)

// Обогрев руля
vehiclePropertyHelper.setIntProperty(289408269, 0, 2)
```

### Открыть багажник (с проверкой скорости)
```kotlin
val speed = vehiclePropertyHelper.getFloatProperty(VehiclePropertyIds.PERF_VEHICLE_SPEED, 0)
if (speed < 5.0f) {
    vehiclePropertyHelper.setIntProperty(373295873, 536870912, 1)
} else {
    Log.w(TAG, "Cannot open trunk while moving")
}
```

---

## Структура Property ID

### Формат
```
Property ID (32 бита):
- Биты 28-31: Тип данных (1=INT, 2=FLOAT, 3=BOOLEAN, и т.д.)
- Биты 24-27: Группа (1=глобальный, 2=по зонам, и т.д.)
- Биты 16-23: Область (0=система, 1=HVAC, 2=двери, и т.д.)
- Биты 0-15: Уникальный идентификатор свойства
```

### Примеры
- `0x15200505` = HVAC_AC_ON
  - `1` = BOOLEAN
  - `5` = Системный
  - `20` = HVAC
  - `0505` = AC_ON

- `0x15600503` = HVAC_TEMPERATURE_SET
  - `1` = BOOLEAN (на самом деле FLOAT, это стандартный Android ID)
  - `5` = Системный
  - `60` = HVAC Temperature
  - `0503` = TEMPERATURE_SET

---

## Источники данных

1. **Сканирование Car API:** Property Scanner из GGSCC (2025-12-11)
2. **Android Car API:** Стандартные VehiclePropertyIds

## Дополнительные ссылки

- [Android Car API Documentation](https://developer.android.com/reference/android/car/VehiclePropertyIds)
- [GGSCC Project](README.md)

---

**Последнее обновление:** 2025-12-15
