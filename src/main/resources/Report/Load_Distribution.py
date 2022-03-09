import glob
import os

import matplotlib.pyplot as plt
from matplotlib.ticker import AutoLocator
import pandas
import numpy as np

import util
import json

datasets = []

for file in glob.glob(os.path.join(".", "raw", "S*_Load_Distribution.csv")):
    data = pandas.read_csv(file, sep=";", usecols=[0, 1])

    dataMap = dict()

    alldata = list(data["Value"])

    for dat in alldata:
        # remove [ ] from dat and split content on ,
        arr = dat[1:-1].split(", ")

        for key in arr:
            key = key[key.rindex("_")+1:]
            if not key in dataMap:
                dataMap[key] = 0
            dataMap[key] += 1

    dataPairs = sorted(dataMap.items(), key=lambda x: int(x[0][1:]))
    datasets.append([file, dataPairs])


def plot(ax, dataset):
    labels = list(map(lambda x: x[0], dataset))
    values = list(map(lambda x: x[1], dataset))
    y_pos = np.arange(len(dataset))
    ax.bar(y_pos, values, color="blue")
    ax.tick_params('x', rotation=90)
    ax.set_xticks(y_pos)
    ax.set_xticklabels(labels)
    ax.relim()
    ax.set_xlim(xmin=0.1, xmax=max(y_pos))


util.plot(datasets, plot)

plt.show()
