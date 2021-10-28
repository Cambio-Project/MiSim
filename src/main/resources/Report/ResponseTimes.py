import glob
import json
import os
import sys

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

datasets = []
duration = np.float32(json.load(open("metadata.json"))["duration"])

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

    endpoint_name = file[file.index("["):file.index("]")]
    datasets.append((endpoint_name, mean))

if len(datasets) == 0:
    print("No dataset found!")
    exit(1)

# use --no-plot for headless mode
if len(sys.argv) > 1 and (sys.argv[1] == "--no-plot" or sys.argv[1] == "--headless"):
    exit(0)

fig = plt.figure(1)
plt.tight_layout()

maxTime = duration
step = max(int(maxTime / 10), 1)
x_ticks = range(0, int(duration + step), step)

for index, (title, dataset) in enumerate(datasets, start=1):
    ax = fig.add_subplot(len(datasets), 1, index)
    ax.errorbar(dataset["simulation_time_rounded_down"], dataset
    ["mean_of_bin"], dataset["error"], linestyle='None', marker='o')
    ax.plot(dataset["simulation_time_rounded_down"],
            dataset["mean_of_bin"], linewidth=1)
    ax.set_title(title)
    ax.set_ylim(ymin=0)
    ax.set_xlim(xmin=0.1)
    ax.set_xticks(x_ticks)

plt.show()
