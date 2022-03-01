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
            if not key in dataMap:
                dataMap[key] = 0
            dataMap[key] += 1

    datasets.append([file, dataMap])


def plot(ax, dataset):
    keys = list(dataset.keys())
    y_pos = np.arange(len(keys))
    ax.bar(y_pos, dataset.values(), color="blue")
    ax.tick_params('x', rotation=90)
    ax.set_xticks(y_pos)
    ax.set_xticklabels(keys)
    ax.relim()
    ax.set_xlim(xmin=0.1, xmax=max(y_pos))


util.plot(datasets, plot)

plt.show()
