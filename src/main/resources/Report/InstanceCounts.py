import glob
import os

import matplotlib.pyplot as plt
import pandas

import util

datasets = []

for file in glob.glob(os.path.join(".", "raw", "_InstanceCount.csv")):
    data = pandas.read_csv(file, sep=";", usecols=[0, 1])

    endpoint_name = file[file.index("["):file.index("]")]
    datasets.append((endpoint_name, data))

util.plot(datasets, lambda ax, dataset: ax.plot(dataset["Simulation Time"], dataset["Value"]))

plt.show()
