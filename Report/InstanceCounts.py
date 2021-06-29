import matplotlib.pyplot as plt
import pandas
import os
import glob
import time


def pull_data() -> None:
    datasets = []
    loadsets = []

    for file in os.listdir("./raw/"):
        if (file.endswith("_InstanceCount.csv")):
            data = pandas.read_csv("./raw/" + file, sep=";", usecols=[0, 1])
            datasets.append((file.strip(), data))

    if (len(datasets) == 0):
        return

    fig, axs = plt.subplots(len(datasets), 1)
    plt.tight_layout()

    loc = 0
    for dataset in datasets:
        ax = axs[loc]
        ax.plot(dataset[1]["Simulation Time"], dataset[1]["Value"])
        ax.set_title(dataset[0])
        loc = loc+1

    loc = 0
    for dataset in loadsets:
        ax = axs[loc]
        ax.scatter(x=dataset[1]["Simulation Time"], y=dataset[1]["Value"])
        ax.set_title(dataset[0])
        loc = loc+1


pull_data()
plt.show()
