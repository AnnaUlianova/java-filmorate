# java-filmorate
Backend на java для сервиса по работе с фильмами и оценками пользователей, возможностью получения списка фильмов с наивысшим рейтингом, рекомендованных к просмотру комьюнити.

### Entity-relationship diagram ###
![ER-diagram for filmorate](FimorateDB.png)

Выше представлена ER-диаграмма с указанием первичных ключей и видов связи.
Для хранения нескольких жанров фильма, а также с целью обеспечения уникальности лайков (1 user = 1 like) была организована связь многие-ко-многим с помощью соединительных таблиц.

Поскольку (с целью специфики приложения) планируется частое обращение к количеству лайков для каждого фильма и сортировки общего списка (составление рейтинга), то с целью увеличения производительности в таблицу film был также добавлен film_count, значение которого будет увеличиваться на единицу при каждом добавлении лайка фильму.

Примеры запросов для основных операций при работе с приложением: 
- получение топ N наиболее популярных фильмов (сортировка по рейтингу) с дополнительной информацией по каждому из них,
- изменение рейтинга фильма (добавление и удаление лайка), 
- отправка запроса на добавление в друзья (возможность отправлять и принимать предложения о дружбе), 
- получение списка общих друзей с другим пользователем.
