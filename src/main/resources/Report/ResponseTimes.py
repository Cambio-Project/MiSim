import glob
import os

import numpy as np
import pandas as pd
from matplotlib.axes import Axes
from pandas import DataFrame

import util

datasets = []

for file in glob.glob(os.path.join(".", "raw", "*_ResponseTimes.csv")):
    data = pd.read_csv(file, sep=";",
                       usecols=[0, 1], dtype=np.float32)

    # bin data over Simulation Time with 1s bins
    data["simulation_time_rounded_down"] = data["Simulation Time"].apply(
        lambda x: np.int64(x))

    # group by bins and calculates means and std over bins
    groups = data.groupby("simulation_time_rounded_down")
    mean = groups.mean().reset_index()
    std_mean = groups.sem().reset_index()

    # calculate the mean of the std over bins
    mean["error"] = std_mean["Value"]
    mean["mean_of_bin"] = mean["Value"]

    # drop columns "Value" and "Simulation Time" for readability
    mean = mean.drop(columns=["Value", "Simulation Time"])

    # output processed data to an additional csv file
    output_file = file.replace(".csv", "_processed.csv")
    mean.to_csv(output_file, sep=";", index=False)

    endpoint_name = file[file.index("[")+1:file.index("]")]
    datasets.append((endpoint_name, mean))


def write_dataset(ax: Axes, dataset: DataFrame):
    ax.errorbar(dataset["simulation_time_rounded_down"],
                dataset["mean_of_bin"],
                dataset["error"],
                linestyle='None',
                marker='o')
    ax.plot(dataset["simulation_time_rounded_down"],
            dataset["mean_of_bin"],
            linewidth=1)


util.plot(datasets, write_dataset)
