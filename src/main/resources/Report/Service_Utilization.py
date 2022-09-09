import glob
import os

import matplotlib.pyplot as plt
import pandas

import util

datasets = []

for file in glob.glob(os.path.join(".", "raw", "S*_CPUUtilization.csv")):
    data = pandas.read_csv(file, sep=";", usecols=[0, 1])

    service_name = file[file.index("[") + 1:file.index("]")]
    datasets.append((service_name, data))

util.plot(datasets, lambda ax, dataset: ax.step(dataset["Simulation Time"], dataset["Value"], where="post"))

plt.show()
