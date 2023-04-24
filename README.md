# FIllICS

*Read: fill ICS*

A Java program that fetches IFNTUOG schedule and converts it into an ICS file (iCal file format)

Version: **2.4**

## Dependencies

- Java 20\
  Language version

- Jsoup 1.15.4\
  HTML format parser\
  Maven: `org.jsoup:jsoup`

- JSON 20230227\
  JSON format parser\
  Maven: `org.json:json`

- JComboBox AutoCompletion by [Thomas Bierhance](mailto:thomas@orbital-computer.de)\
  [Source](http://www.orbital-computer.de/JComboBox) | [Download](http://www.orbital-computer.de/JComboBox/source/AutoCompletion.java)

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