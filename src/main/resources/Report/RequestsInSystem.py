import glob

import pandas as pd
from matplotlib.axes import Axes
from pandas import DataFrame

import util

datasets = []

for file in glob.glob("./raw/*_InSystem.csv"):
    name = (file.split("_")[0] + "_" + file.split("_")[1])[6:]
    dataInSystem = pd.read_csv(file, sep=";", usecols=[0, 1])
    dataNotComputed = pd.read_csv(file.replace(
        "_InSystem", "_NotComputed"), sep=";", usecols=[0, 1])
    dataWaitingForDep = pd.read_csv(file.replace(
        "_InSystem", "_WaitingForDependencies"), sep=";", usecols=[0, 1])
    waitingValues = dataWaitingForDep["Value"].add(
        dataNotComputed["Value"])

    joined = dataInSystem.merge(
        dataNotComputed, how="outer", on="SimulationTime", suffixes=("_InSystem", "_NotComputed"))
    joined = joined.merge(
        dataWaitingForDep, how="outer", on="SimulationTime")
    joined.rename(
        columns={"Value": "Value_WaitingForDependencies"}, inplace=True)

    datasets.append((name, joined))


def write_dataset(ax: Axes, dataset: DataFrame):
    ax.fill_between(dataset["SimulationTime"], 0,
                    dataset["Value_NotComputed"],
                    label="Not Computed",
                    facecolor="yellow")
    ax.fill_between(dataset["SimulationTime"], 0,
                    dataset["Value_WaitingForDependencies"],
                    label="Waiting f. Dep.",
                    facecolor="pink")
    ax.set_ylim(bottom=0)
    ax.legend()


util.plot(datasets, write_dataset)
