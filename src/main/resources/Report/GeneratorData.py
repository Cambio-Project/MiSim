import glob
import json
import os

import numpy as np
import pandas as pd
from matplotlib.axes import Axes
from pandas.core.frame import DataFrame

import util

datasets = []
timepoints = set()
duration = json.load(open("metadata.json"))["duration"]

for file in glob.glob(os.path.join(".", "raw", "G*_Load.csv")):
    dataLoad = pd.read_csv(file, sep=";", usecols=[0, 1])
    dataSuccessful = pd.read_csv(
        file.replace("_Load.csv", "_SuccessfulRequests.csv"), sep=";", usecols=[0, 1])
    dataFailed = pd.read_csv(
        file.replace("_Load.csv", "_FailedRequests.csv"), sep=";", usecols=[0, 1])

    sucessrate = pd.DataFrame()
    sucessrate["SimulationTime"] = dataSuccessful["SimulationTime"]
    sucessrate["Value"] = dataSuccessful["Value"].div(
        dataSuccessful["Value"] + dataFailed["Value"])

    joined = dataLoad.merge(dataSuccessful,
                            how="outer", on="SimulationTime", suffixes=("_Load", "_Successful"))
    joined = joined.merge(dataFailed, how="outer", on="SimulationTime")
    joined.rename(columns={"Value_Load": "Load",
                           "Value_Successful": "Successful", "Value": "Failed"}, inplace=True)

    joined["Successrate"] = joined["Successful"].div(
        joined["Successful"] + joined["Failed"])

    joined.to_csv(file.replace("_Load", "_processed"), sep=";")

    name = file[file.rindex("[") + 1:file.rindex("]")]
    datasets.append((name, joined))


def write_dataset(ax: Axes, ax2: Axes, dataset: DataFrame):
    ax.plot(dataset["SimulationTime"],
            dataset["Load"],
            color="yellow",
            label="Load")
    ax.scatter(x=dataset["SimulationTime"],
               y=dataset["Successful"],
               color="green",
               label="Successful")
    ax.scatter(x=dataset["SimulationTime"],
               y=dataset["Failed"],
               color="blue",
               label="Failed")

    mask = np.logical_and(np.isfinite(dataset["Successrate"]), np.logical_not(np.isnan(dataset["Successrate"])))
    succ_data = dataset[mask].sort_values(by=["SimulationTime"]).reset_index(drop=True)
    ax2.plot(succ_data["SimulationTime"],
             succ_data["Successrate"],
             color="black",
             linewidth=2,
             label="Successrate")
    ax2.set_ylim(-0.01, 1.01)
    ax.legend()
    ax2.legend()


util.plot_two_column(datasets, write_dataset)
