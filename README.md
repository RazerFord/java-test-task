## Описание

В данном репозитории лежит приложение, которое будет позволять тестировать производительность той или иной архитектуры сервера.

Артефакты работы приложения можно найти в следующих папках:
- Графики находятся в папке [image](/images)
- Файлы с результатами находятся в папке [results](/results). В данной папке находятся результаты тестирования при изменении того или иного параметра. Каждому опыту соответствуют 4 файла: 3 файла с результатом тестирования и 1 файл (`*_description.txt`) с описанием

## Графики при изменении длины массива

### Среднее время одного запроса на клиенте

![Array length. Average request time on client](images/array_length_average_request_time_on_client.png)

Данные результаты вполне справедливы. Так как при увеличении длины массива возрастает объем пересылаемых данных между клиентом и сервером. Кроме того при увеличении длины массива увеличивается время сортировки, и как следствие увеличивается среднее время запроса.

### Время обработки клиента на сервере

![Array length. Processing client on server](images/array_length_processing_client_on_server.png)

Эти результаты также вполне справедливы, так как при увеличении длины массива увеличивается время сортировки, и как следствие увеличивается время обработки запроса уже на сервере.

### Время обработки запроса на сервере

![Array length. Processing request on server](images/array_length_processing_request_on_server.png)

Эти замеры учитывают лишь время затрачиваемое на обработку запроса. При увеличении длины массива объем запроса увеличивается, и как следствие увеличивается затрачиваемое время. Обе архитектуры справляются с этой задачей приблизительно одинаково.

## Графики при изменении количества одновременно работающих клиентов

### Среднее время одного запроса на клиенте

![Number of clients. Average request time on client](images/number_of_clients_average_request_time_on_client.png)

### Время обработки клиента на сервере

![Number of clients. Processing client on server](images/number_of_clients_processing_client_on_server.png)

### Время обработки запроса на сервере

![Number of clients. Processing request on server](images/number_of_clients_processing_request_on_server.png)

## Графики при изменении временного промежутка от получения сообщения до отправки следующего

### Среднее время одного запроса на клиенте

![Time period from receiving to sending message. Average request time on client](images/time_period_from_receiving_to_sending_message_average_request_time_on_client.png)

### Время обработки клиента на сервере

![Time period from receiving to sending message. Processing client on server](images/time_period_from_receiving_to_sending_message_processing_client_on_server.png)

### Время обработки запроса на сервере

![Time period from receiving to sending message. Processing request on server](images/time_period_from_receiving_to_sending_message_processing_request_on_server.png)
