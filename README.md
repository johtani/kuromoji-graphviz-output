# Output GraphViz data from Lucene Kuromoji
This tool outputs GraphViz data and token stream that is toknized by Lucene Kuromoji  

## Version

* Lucene 8.5.2

## Build

```shell script
./gradlew build
```

## Usage

Run the command to show help.
```shell script
java -jar build/libs/lucene-kuromoji-graphviz-output-1.0-SNAPSHOT.jar -h
```
OR
```shell script
./kuromoji.sh -h
```


Run the below command on macOS.

```shell script
 java -jar build/libs/lucene-kuromoji-graphviz-output-1.0-SNAPSHOT.jar "関西国際空港" | dot -Tpng | open -f -a preview.app
```

**the command outputs DOT file to stdout and outputs tokens to stderr.**


## License

Apache License 2.0