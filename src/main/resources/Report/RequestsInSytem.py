import glob
import json
import matplotlib.pyplot as plt
import pandas as pd


def pull_data() -> None:
    datasets = []
    duration = json.load(open("meta.json"))["duration"]

    for file in glob.glob("./raw/*_InSystem.csv"):
        if (file.endswith("_InSystem.csv")):
            name = (file.split("_")[0] + "_" + file.split("_")[1])[6:]
            dataInSystem = pd.read_csv(file, sep=";", usecols=[0, 1])
            dataNotComputed = pd.read_csv(
                "./raw/" + name + "_Requests_NotComputed.csv", sep=";", usecols=[0, 1])
            dataWaitingForDep = pd.read_csv(
                "./raw/" + name + "_Requests_WaitingForDependencies.csv", sep=";", usecols=[0, 1])
            waitingValues = dataWaitingForDep["Value"].add(
                dataNotComputed["Value"])

            datasets.append(
                (name, dataInSystem, dataNotComputed, dataWaitingForDep))

    if (len(datasets) == 0):
        return

    fig, axs = plt.subplots(len(datasets), 1)
    plt.tight_layout()

    maxTime = duration
    step = int(maxTime / 10)
    step = round(step / 10) * 10
    step = step if step > 1 else 1
    xtickz = list(range(0, int(maxTime + step), step))

    loc = 0
    for dataset in datasets:
        ax = axs[loc]
        # ax.plot(dataset[2]["Simulation Time"], dataset[2]["Value"], "red",label="Not Computed")
        # ax.plot(dataset[3]["Simulation Time"], dataset[3]["Value"], "green", label="Waiting")
        ax.fill_between(dataset[1]["Simulation Time"], 0,
                        dataset[1]["Value"], label="Not Computed", facecolor="yellow")
        ax.fill_between(dataset[3]["Simulation Time"], 0, dataset[3]
        ["Value"], label="Waiting f. Dep.", facecolor="pink")
        ax.plot(dataset[1]["Simulation Time"], dataset[1]["Value"], "blue", label="Total")
        # ax.scatter(x=dataset[1]["Simulation Time"], y=dataset[1]["Value"], color="blue")

        ax.plot
        ax.set_title(dataset[0])
        ax.set_ylim(ymin=0)
        ax.set_xticks(xtickz)
        ax.legend()
        loc = loc + 1


while (True):
    pull_data()
    plt.show()
    plt.close()
