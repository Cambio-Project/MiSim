import glob
import json
from matplotlib.axes import Axes
import matplotlib.pyplot as plt
import pandas as pd
from pandas.core.frame import DataFrame

import numpy as np
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
    joined = joined.rename(columns={"Value": "Value_Total"},)

    name = file[file.index("[")+1:file.index("]")]
    datasets.append((name, joined))


def write_data(ax: Axes, dataset: DataFrame):
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


util.plot(datasets, write_data)

