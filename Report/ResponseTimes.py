import matplotlib.pyplot as plt
import pandas as pd
import os
import glob
import math
import numpy as np
import json


def pull_data() -> None:
    datasets = []
    loadsets = []
    duration = json.load(open("meta.json"))["duration"]

    for file in os.listdir("./raw/"):
        if (file.endswith("_ResponseTimes.csv")):
            data = pd.read_csv("./raw/" + file, sep=";", usecols=[0, 1])
            datasets.append((file.strip(), data))

            loadfile = file.replace("_ResponseTimes.csv", "")
            loadfile = loadfile[2:loadfile.rindex("]"):]
            loadfile = glob.glob("./raw/*" + loadfile+"*Load.csv")[0]
            load_data = pd.read_csv(loadfile, sep=";", usecols=[0, 1])

            

            # binning to seconds because pandas refuses to do it itself
            load_data["Time"] = np.int32(load_data["Time"])
            grouped = load_data.groupby("Time")
            grouped = grouped.apply(lambda x: x["Value"].sum())
            grouped = grouped.reset_index()
            grouped["Value"] = grouped[0]

            loadsets.append((loadfile.strip(), grouped))

    if (len(datasets) == 0):
        return

    fig, axs = plt.subplots(len(datasets), 2)
    plt.tight_layout()

    maxTime = duration
    step = int(maxTime/10)
    step = round(step/10)*10
    step = step if step > 1 else 1
    xtickz = list(range(0, int(maxTime+step), step))

    loc = 0
    for dataset in datasets:
        ax = axs[loc][0] if len(datasets) > 1 else axs[0]
        ax.scatter(x=dataset[1]["Time"], y=dataset[1]["Value"])
        ax.set_title(dataset[0])
        ax.set_ylim(ymin=0)
        ax.set_xticks(xtickz)
        loc = loc+1

    loc = 0
    for dataset in loadsets:
        ax = axs[loc][1] if len(datasets) > 1 else axs[1]
        ax.scatter(x=dataset[1]["Time"], y=dataset[1]["Value"])
        ax.plot(dataset[1]["Time"], dataset[1]["Value"])
        ax.set_title(dataset[0])
        ax.set_ylim(ymin=0)
        ax.set_xticks(xtickz)
        loc = loc+1


while(True):
    pull_data()
    plt.show()
    plt.close()
