import json
from typing import List, Tuple, Callable

import matplotlib.pyplot as plt
import sys
from matplotlib.axes import Axes
from pandas import DataFrame


def plot(datasets: List[Tuple[str, DataFrame]],
         consumer: Callable[[Axes, DataFrame], None]):
    duration = json.load(open("metadata.json"))["duration"]

    if len(datasets) == 0:
        print("No dataset found!")
        exit(1)

    # use --no-plot for headless mode
    if len(sys.argv) > 1 and (sys.argv[1] == "--no-plot" or sys.argv[1] == "--headless"):
        exit(0)

    fig = plt.figure(1)
    plt.tight_layout()

    step = max(int(duration / 10), 1)
    x_ticks = range(0, int(duration + step), step)

    for index, (title, dataset) in enumerate(datasets, start=1):
        ax = fig.add_subplot(len(datasets), 1, index)
        ax.set_title(title)
        ax.set_ylim(ymin=0, auto=True)
        ax.set_xlim(xmin=0.1, xmax=duration)
        ax.set_xticks(x_ticks)
        consumer(ax, dataset)

    plt.show()
