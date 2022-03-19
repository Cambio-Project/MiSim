import glob

import numpy as np
import pandas as pd
from matplotlib.axes import Axes
from pandas.core.frame import DataFrame

import util

datasets = []

for file in glob.glob("./raw/C*_Usage.csv"):
    dataUsage = pd.read_csv(file, sep=";", usecols=[0, 1])
    dataActive = pd.read_csv(file.replace(
        "_Usage", "_ActiveProcesses"), sep=";", usecols=[0, 1])
    dataTotal = pd.read_csv(file.replace(
        "_Usage", "_TotalProcesses"), sep=";", usecols=[0, 1])

    joined = dataUsage.merge(
        dataActive, on="Simulation Time", suffixes=("_Usage", "_Active"))
    joined = joined.merge(dataTotal, on="Simulation Time")
    joined = joined.rename(columns={"Value": "Value_Total"})

    name = file[file.index("[") + 1:file.index("]")]
    datasets.append((name, joined))


def write_data(ax: Axes, ax2: Axes, dataset: DataFrame):
    mask = np.isfinite((dataset["Value_Total"]))

    ax.fill_between(dataset["Simulation Time"][mask],
                    dataset["Value_Total"][mask],
                    facecolor="red",
                    linestyle="-",
                    label="Total Processes")

    ax.fill_between(dataset["Simulation Time"],
                    dataset["Value_Active"],
                    facecolor="green",
                    label="ActiveProcesses")
    ax.legend()

    ax2.step(dataset["Simulation Time"],
             dataset["Value_Usage"],
             where="post",
             label="Usage (%)",
             linewidth=0.25)
    ax2.scatter(x=dataset["Simulation Time"],
                y=dataset["Value_Usage"],
                c="black",
                marker="2")
    ax2.set_ylim(-0.01, 1.01)
    ax2.legend()


util.plot_two_column(datasets, write_data)
