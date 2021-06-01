import matplotlib.pyplot as plt
import pandas as pd
import os
import glob
import math
import numpy as np
import json


def pull_data() -> None:
    datasets = []
    timepoints = set()
    duration = json.load(open("meta.json"))["duration"]

    for file in glob.glob("./raw/C*_Usage.csv"):
        name = file.rsplit("_", 1)[0][6:]
        dataLoad = pd.read_csv(file, sep=";", usecols=[0, 1])
        dataActive = pd.read_csv(
            "./raw/" + name + "_ActiveProcesses.csv", sep=";", usecols=[0, 1])
        dataTotal = pd.read_csv(
            "./raw/" + name + "_TotalProcesses.csv", sep=";", usecols=[0, 1])

        timepoints.update(dataLoad["Simulation Time"])
        timepoints.update(dataActive["Simulation Time"])
        timepoints.update(dataTotal["Simulation Time"])

        datasets.append(
            (name, dataLoad, dataActive, dataTotal))

    if (len(datasets) == 0):
        return

    fig, axs = plt.subplots(len(datasets), 1)
    plt.tight_layout()

    maxTime = duration
    step = int(maxTime/10)
    step = round(step/10)*10
    step = step if step > 1 else 1
    xtickz = list(range(0, int(maxTime+step), step))

    loc = 0
    for dataset in datasets:
        ax = axs[loc]
        # ax.scatter(x=dataset[1]["Simulation Time"],y=dataset[1]["Value"], color="blue", label="Load")
        # ax.scatter(x=dataset[2]["Simulation Time"], y=dataset[2]
        #            ["Value"], color="green", label="Successful")
        # ax.scatter(x=dataset[3]["Simulation Time"], y=dataset[3]
        #            ["Value"], color="red", label="Failed")
        # ax.fill_between(dataset[1]["Simulation Time"], 0,
        #                 dataset[1]["Value"], facecolor="blue",
        #                 label="Load")
        ax.fill_between(dataset[3]["Simulation Time"], 0, dataset[3]
                        ["Value"], facecolor="red", label="Total Processes")
        ax.fill_between(dataset[2]["Simulation Time"], 0,
                        dataset[2]["Value"], facecolor="green",
                        label="ActiveProcesses")
        ax.set_title(dataset[0])
        ax.set_ylim(ymin=0)
        ax.set_xticks(xtickz)
        ax.legend()
        loc = loc+1


while(True):
    pull_data()
    plt.show()
    plt.close()
