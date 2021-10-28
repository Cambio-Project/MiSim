import json
from typing import List, Tuple, Callable

import matplotlib.pyplot as plt
import sys
from matplotlib.axes import Axes
from matplotlib.gridspec import GridSpec
from pandas import DataFrame


def _setup(datasets):
    duration = json.load(open("metadata.json"))["duration"]

    if len(datasets) == 0:
        print("No dataset found!")
        exit(1)

    # use --no-plot for headless mode
    if len(sys.argv) > 1 and (sys.argv[1] == "--no-plot" or sys.argv[1] == "--headless"):
        exit(0)

    plt.tight_layout()

    step = max(int(duration / 10), 1)
    x_ticks = range(0, int(duration + 1), step)
    fig = plt.figure(1)

    return fig, x_ticks


def plot(datasets: List[Tuple[str, DataFrame]],
         consumer: Callable[[Axes, DataFrame], None]):
    fig, x_ticks = _setup(datasets)

    for index, (title, dataset) in enumerate(datasets, start=1):
        ax = fig.add_subplot(1, 1, index)
        ax.set_title(title)
        ax.set_ylim(ymin=0, auto=True)
        ax.set_xlim(xmin=0.1, xmax=x_ticks.stop)
        ax.set_xticks(x_ticks)
        consumer(ax, dataset)

    plt.show()


def plot_two_column(datasets: List[Tuple[str, DataFrame]],
                    consumer: Callable[[Axes, Axes, DataFrame], None]):
    fig, x_ticks = _setup(datasets)

    gs = GridSpec(len(datasets), 2, figure=fig)

    for index, (title, dataset) in enumerate(datasets, start=0):
        axes = [fig.add_subplot(gs[index, 0]),
                fig.add_subplot(gs[index, 1])]
        for ax in axes:
            ax.set_title(title)
            ax.set_ylim(ymin=0, auto=True)
            ax.set_xlim(xmin=0.1, xmax=x_ticks.stop)
            ax.set_xticks(x_ticks)
        consumer(axes[0], axes[1], dataset)

    plt.show()
