# FIllICS

*Read: fill ICS*

A Java program that fetches IFNTUOG schedule and converts it into an ICS file (iCal file format)

Version: **2.2.3**

## Dependencies

- Java 18.0.2.1

- Jsoup 1.15.3\
    HTML format parser\
    Maven: `org.jsoup:jsoup`

- JSON 20220924\
    JSON format parser\
    Maven: `org.json:json`

- JComboBox AutoCompletion by [Thomas Bierhance](mailto:thomas@orbital-computer.de)\
    [Source](http://www.orbital-computer.de/JComboBox) | [Download](http://www.orbital-computer.de/JComboBox/source/AutoCompletion.java)

---

## Run

To use the program just open it

1. Set your teacher or group and time limits
2. Request schedule from the server
3. Filter results
4. Export schedule
5. Import a generated ICS file into your calendar app

You can also set up some exclude patterns adding `map_subjects.txt` and `map_types.txt` near the JAR. Use the following syntax

```txt
Text to replace;what to replace it with
```

Here are some examples

- `map_subjects.txt`

    ```txt
    Паралельне програмування;Intimate Dev
    Іноземна мова (анг) (за професійним спрямуванням);English
    ```

- `map_types.txt`

    ```txt
    Лаб;Lab work
    Пр;Practice
    Л;Lecture
    ```