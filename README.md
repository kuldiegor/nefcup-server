# NEFCUP server
NEFCUP это иснструмент CI/CD для web проектов.\
NEFCUP позволяет с минимальными правами заливать проекты только в нужный каталог.\
`Сервер nefcup` работает в свзяке с [nefcup client](https://github.com/kuldiegor/nefcup-client). 
Перейдите по ссылке [nefcup client](https://github.com/kuldiegor/nefcup-client) после настройки сервера.

## Настроки
Все параметры настраиваются через переменные среды. Ниже приведён список переменных среды, используемые в приложении.

`NEFCUP_TOKEN` =
Токен для аутентификации и авторизации клиентов `nefcup client`.\
Обязательный параметр

`NEFCUP_ROOT_DIRECTORY` =
Директория где распологаются web проекты\
Обязательный параметр

`NEFCUP_FILE_PERMISSIONS` =
POSIX разрешения после создания файла в директории.\
Стандартно имеет значение `rwxr-xr-x`

`NEFCUP_DIRECTORY_PERMISSIONS` =
POSIX разрешения после создания директории.\
Стандартно имеет значение `rwxr-xr-x`

`NEFCUP_MAX_FILE_SIZE` =
Максимальный размер загружаемых файлов (Настройка для apache tomcat container)\
Стандартно имеет значение `128MB`

`NEFCUP_MAX_REQUEST_SIZE` =
Максимальный размер запроса (Настройка для apache tomcat container)\
Стандартно имеет значение `128MB`

`NEFCUP_SERVER_PORT` =
Порт на котором слушает сервер (Настройка для apache tomcat container)\
Стандартно имеет значение `8080`

`NEFCUP_LOG_PATH` =
Путь для сохранения логов\
Обязательный параметр

## Настройка NGINX
```nginx
location /nefcup-server {
            proxy_pass http://localhost:8080;
            proxy_set_header Host $host;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Real-IP $remote_addr;
            rewrite ^/nefcup-server(.*)?$ $1 break;
    }
```


## Запуск
Для запуска необходимо заполнить обязательные переменные среды в файле `nefcup.service`.\
Скопировать файл `nefcup.service` в `/etc/systemd/system`

Применить изменения
```shell
sudo systemctl daemon-reload
```
      
Запустить сервис
```shell
sudo systemctl restart nefcup
```