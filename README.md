# External-Sort

Java implement of 2PMMS that sorts a file of 3 million records.

## Compile

```
javac *.java
```

## Run

1. Generate input data (3000000 records)

```
java GenerateData
```

2. Sort

```
java SortPost
```

3. Compare with sample solution

```
java SortUnix
```
```
md5 taxpayers_3M_sorted.txt taxpayers_3M_sort_answer.txt
```