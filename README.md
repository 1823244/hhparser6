# hhparser
hh.ru vacancies parser

## Анализатор вакансий на hh.ru  

Позволяет собирать вакансии в разрезе "Проектов", которые включают несколько "Строк поиска", например:  
Проект "Java" может содержать такие строки:  
- java developer  
- java architector  
- java teamlead  

Проекты и строки поиска настраиваются интерактивно.  

UI реализован на Thymeleaf.  
Само приложение - на Spring Boot.  

# RUN in Ubuntu

```

  Остановка приложения:
 
 использовать команду ps -ef | grep ваше_приложение.jar
  (или ps -ef | grep java и проверить PID) для получения PID вашего приложения

Выполните команду kill <PID>, где <PID> - это PID вашего приложения.

  Запуск
  
nohup java -jar hhparser5.jar > output.log 2>&1 &
```
