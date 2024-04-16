import sys

if __name__ == "__main__":
    ts = 0.0
    tj = 0.0
    row_count = 0
    with open(sys.argv[1]) as file:
        lines = file.readlines()
        for line in lines:
            row = line.split(",")
            ts += int(row[0].split(":")[1])
            tj += int(row[1].split(":")[1])
            row_count += 1
    ts_average = ts / row_count
    tj_average = tj / row_count
    print("printing in milliseconds")
    print("TS average:", ts_average / 10**6, ", TJ average:", tj_average / 10**6)