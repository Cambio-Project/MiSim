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

    for file in glob.glob("./raw/G*_Load.csv"):
        name = file.rsplit("_", 1)[0][6:]
        dataLoad = pd.read_csv(file, sep=";", usecols=[0, 1])
        dataSuccessful = pd.read_csv(
            "./raw/" + name + "_SuccessfulRequests.csv", sep=";", usecols=[0, 1])
        dataFailed = pd.read_csv(
            "./raw/" + name + "_FailedRequests.csv", sep=";", usecols=[0, 1])

        timepoints.update(dataLoad["Time"])
        timepoints.update(dataSuccessful["Time"])
        timepoints.update(dataSuccessful["Time"])

        # combined = dataSuccessful["Value"].add(
        #     dataFailed["Value"], fill_value=0)

        # dataFailed["Value"] = combined
        sucessrate = pd.DataFrame()
        sucessrate["Time"] = dataSuccessful["Time"]
        sucessrate["Value"] = dataSuccessful["Value"].div(
            dataSuccessful["Value"]+dataFailed["Value"])

        datasets.append(
            (name, sucessrate, dataSuccessful, dataFailed))

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
        ax = axs[loc] if len(datasets) > 1 else axs
        # ax.scatter(x=dataset[1]["Time"],y=dataset[1]["Value"], color="blue", label="Load")
        ax.scatter(x=dataset[2]["Time"], y=dataset[2]["Value"],
                   color="green", label="Successful")
        ax.scatter(x=dataset[3]["Time"], y=dataset[3]
                   ["Value"], color="blue", label="Failed")
        ax.plot(dataset[1]["Time"], dataset[1]
                   ["Value"], color="yellow", label="SucessRate")

        # ax.fill_between(dataset[1]["Time"], 0,
        #                 dataset[1]["Value"], facecolor="blue",
        #                 label="Load")
        # ax.fill_between(dataset[3]["Time"], 0, dataset[3]
        #                 ["Value"], facecolor="red", label="Failed")
        # ax.fill_between(dataset[2]["Time"], 0,
        #                 dataset[2]["Value"], facecolor="green",
        #                 label="Successful")

        ax.set_title(dataset[0])
        ax.set_ylim(ymin=0)
        ax.set_xlim(xmin=0)
        ax.set_xticks(xtickz)
        ax.legend()
        loc = loc+1


while(True):
    pull_data()
    plt.show()
    plt.close()
