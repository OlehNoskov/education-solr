Solr Education (Nix 2023 Noskov Oleh)

Поля в БД:
id (long)
author (text)
title (text)
description (string)
tags (array string)
publication_date (date)

Поля в индексе:
id (indexed=true, stored=true)
author (indexed=true, stored=false) ^2.0 boost
title (indexed=true, stored=false) ^1.5 boost
description (indexed=true, stored=false, docValues=true)
tags (indexed=true, stored=false, docValues=true)
publication_date (indexed=true, stored=false, docValues=true)

Поля для фасетов:
title
author
description
tags
publication_date (range facet)

Пример документа:
{
"id": 1,
"author": "Robert Martin",
"title": "Clean Code: A Handbook of Agile Software Craftsmanship",
"description": "Clean Code is divided into three parts. The first describes the principles, patterns, and practices of writing clean code.
The second part consists of several case studies of increasing complexity.
Each case study is an exercise in cleaning up code—of transforming a code base that has some problems into one that is sound and efficient.
The third part is the payoff: a single chapter containing a list of heuristics and “smells” gathered while creating the case studies.
The result is a knowledge base that describes the way we think when we write, read, and clean code.",
"tags": [
"Programming",
"Science"
],
"publication_date": "2012-12-01T21:59:59Z"
}

Эндпоинты:
-book/add - добавление новых документов
-book/search (принимает query) - возвращает отсортированные по релевантности док-ты. Шаг пагинации - 10
-book/delete/{id} - удаление существующих документов
-book - просмотр всех записей в PostgreSQL

Реализация поиска:
-Solr

База Данных - PostgreSQL
Start PostgreSQL in Docker:
1 option: docker run --name some-postgres -p 5432:5432 -e POSTGRES_PASSWORD=mysecretpassword -d postgres
2 option: docker run -itd -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD= mysecretpassword -p 5432:5432 -v /data:/var/lib/postgresql/data --name postgresql postgres
