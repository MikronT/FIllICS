# FIllICS

*Read: fill ICS*

A Java program that fetches IFNTUOG schedule and converts it into an ICS file (iCal file format)

## Dependencies

Runtime

- Java 23\
  Language version

Project

- Jsoup 1.18.1\
  HTML format parser\
  Maven: `org.jsoup:jsoup`

- JSON 20240303\
  JSON format parser\
  Maven: `org.json:json`

- JComboBox AutoCompletion by [Thomas Bierhance](mailto:thomas@orbital-computer.de)\
  [Source](http://www.orbital-computer.de/JComboBox) | [Download](http://www.orbital-computer.de/JComboBox/source/AutoCompletion.java)

- Log4J API 3.0.0-beta2\
  Logging framework API\
  Maven: `org.apache.logging.log4j:log4j-api`

Dev

- JetBrains Annotations 24.1.0\
  Most common annotations\
  Maven: `org.jetbrains:annotations`

- JUnit Jupiter 5.11.0\
  Unit testing framework\
  Maven: `org.junit.jupiter:junit-jupiter`

- Log4J Core 3.0.0-beta2\
  Logging framework core\
  Maven: `org.apache.logging.log4j:log4j-core`

---

## Run

To use the program just open it

1. Get the schedule
    - Set your teacher or group and request the schedule from the server
    - Set your group name and import from a previously downloaded webpage
2. Filter results by
    1. Time period
    2. Session types
    3. Subjects
    4. Groups
3. Export your schedule
4. Import a generated ICS file into your calendar app

You can also set up some rename patterns by adding `map_subjects.txt` and `map_types.txt` near the JAR. Use the
following syntax

```txt
Original subject name;New subject name
```

Here are some examples

- `map_subjects.txt`

    ```txt
    Іноземна мова (анг) (за професійним спрямуванням);English
    Німецька мова (за професійним спрямуванням);Deutsch
    ```

- `map_types.txt`

    ```txt
    Лаб;Lab work
    Пр;Practice
    Л;Lecture
    ```
