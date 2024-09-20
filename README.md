# Share It

---

## Описание


Share It - сервис, позволяющий осуществлять обмен вещами (книгами, инструментами и др.) на опредлённый срок.

Пользователи приложения могут выкладывать свои вещи для их дальнейшего бронирования другими пользователями.
Также имеется возможность оставлять заявки на вещи, которые ещё никто не публиковал для бронирования.
В приложении также предусмотрен поиск доступных к бронированию вещей по названию и описанию вещи.
Пользователь-вледелец вещи при просмотре своих вещей видит ближайшие их бронирования (предыдущее и следующее).

После бронирования и использования вещи, у пользователей есть возможность оставить комментарий об этом предмете.

## Технологии:

* Java 11
* Spring Boot 2.7.18
* Hibernate
* PostgreSQL
* REST API

# Endpoints

---

## User

---

User DTO:
```json
{
  "id": 1,
  "name": "Name",
  "email": "user@mail.com"
}
```

```POST /users```
Добавление пользователя

```PATCH /users/{userId}```
Обновление информации о существующем пользователе

```GET /users```
Получение списка всех пользователей

```GET /users/{userId}```
Получение информации об одном пользователе

```DELETE /users/{userId}```
Удаление пользователя

## Item

---

Item DTO
```json
{
  "id": 1,
  "name": "Item",
  "description": "This is cool item",
  "available": true,
  "lastBooking": {
    "id": 1,
    "start": "2024-09-18T16:00:00",
    "end": "2024-09-18T17:30:00",
    "status": "CANCELED",
    "bookerId": 2
  },
  "nextBooking": {
    "id": 2,
    "start": "2024-09-20T11:00:00",
    "end": "2024-09-21T15:30:00",
    "status": "APPROVED",
    "bookerId": 2
  },
  "comments": [
    {
      "id": 1,
      "text": "comment to item",
      "authorName": "Name",
      "created": "2024-09-19T15:30:00"
    }
  ],
  "requestId": 1
}
```

Comment DTO

```json
{
  "id": 1,
  "text": "comment to item",
  "authorName": "Name",
  "created": "2024-09-19T15:30:00"
}
```

```POST /items```
Добавление вещи

```PATCH /items/{itemId}```
Обновление информации о существующей вещи владельцем этой вещи

```GET /items```
Получение списка всех вещей пользователем-владельцем вещей

```GET /items/{itemId}```
Получение информации об одной вещи

```GET /search```
Поиск вещей по названию и описанию

```DELETE /items/{itemId}```
Удаление вещи владельцем этой вещи

```POST /items/{itemId}/comment```
Добавление отзыва о вещи

## Booking - Бронирование вещей

---

Booking DTO
```json
{
  "id": 1,
  "start": "2024-09-18T16:00:00",
  "end": "2024-09-18T17:30:00",
  "status": "CANCELED",
  "booker": {
    "id": 2,
    "name": "Name",
    "email": "user@mail.com"
  },
  "item": {
    "id": 1,
    "name": "Item",
    "description": "This is cool item",
    "available": true,
    "lastBooking": null,
    "nextBooking": null,
    "comments": [],
    "requestId": null
  }
}
```

```POST /bookings```
Создание бронирования вещи.

```PATCH /bookings/{bookingId}```
Обновление статуса бронирования вещи владельцем этой вещи.

```GET /bookings```
Получение списка всех бронирований пользователя.

```GET /bookings/owner```
Получение списка всех бронирований вещей пользователем-владельцем этих вещей.

```GET /bookings/{bookingId} ```
Получение информации о бронировании владельцем забронированной вещи или пользователем, создавшим это бронирование.

```DELETE /bookings/{bookingId}```
Удаление бронирования вещи пользователем, создавшим это бронирование.

## ItemRequest - Запросы вещей

---

ItemRequest DTO
```json
{
  "id": 1,
  "description": "need some item",
  "created": "2024-09-18T16:00:00",
  "items": [
    {
      "id": 1,
      "name": "Item",
      "description": "This is cool item",
      "available": true,
      "lastBooking": null,
      "nextBooking": null,
      "comments": [],
      "requestId": 1
    }
  ]
}
```

```POST /requests```
Создание запроса вещи.

```GET /requests```
Получение списка всех запросов пользователя.

```GET /requests/all```
Получение списка всех запросов.

```GET /requests/{requestId} ```
Получение информации о запросе вещи.