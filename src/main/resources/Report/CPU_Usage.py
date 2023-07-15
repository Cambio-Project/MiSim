import glob

import numpy as np
import pandas as pd
from matplotlib.axes import Axes
from pandas.core.frame import DataFrame

import util

datasets = []

for file in glob.glob("./raw/C*_Utilization.csv"):
    dataUsage = pd.read_csv(file, sep=";", usecols=[0, 1])
    dataActive = pd.read_csv(file.replace(
        "_Utilization", "_QueueState"), sep=";", usecols=[0, 1, 2])

    joined = dataUsage.merge(
        dataActive, on="SimulationTime")
    joined = joined.rename(columns={"Value": "Utilization"})

    name = file[file.index("[") + 1:file.index("]")]
    datasets.append((name, joined))


def write_data(ax: Axes, ax2: Axes, dataset: DataFrame):
    mask = np.isfinite((dataset["Utilization"]))

    ax.fill_between(dataset["SimulationTime"][mask],
                    dataset["TotalProcesses"][mask],
                    facecolor="red",
                    linestyle="-",
                    label="Total Processes")

    ax.fill_between(dataset["SimulationTime"],
                    dataset["ActiveProcesses"],
                    facecolor="green",
                    label="ActiveProcesses")
    ax.legend()

    ax2.step(dataset["SimulationTime"],
             dataset["Utilization"],
             where="post",
             label="Usage (%)",
             linewidth=0.25)
    ax2.scatter(x=dataset["SimulationTime"],
                y=dataset["Utilization"],
                c="black",
                marker="2")
    ax2.set_ylim(-0.01, 1.01)
    ax2.legend()


util.plot_two_column(datasets, write_data)
