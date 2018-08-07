var graphMinimalistic = '';
var graph = {
    nodes: [{name: 'B', id: 2, labels: ['b1'], group: 0}, {
        name: 'B',
        id: 7,
        labels: ['b1'],
        group: 0
    }, {name: 'A', id: 3, labels: ['a2', 'a1'], group: 1}, {
        name: 'A',
        id: 8,
        labels: ['a2', 'a1'],
        group: 1
    }, {name: 'D', id: 3, labels: ['d1'], group: 2}, {name: 'E', id: 5, labels: ['e2', 'e1'], group: 3}, {
        name: 'E',
        id: 10,
        labels: ['e2', 'e1'],
        group: 3
    }, {name: 'C', id: 5, labels: ['c1', 'c2'], group: 4}],
    links: [{source: 0, target: 2, value: 2}, {source: 0, target: 3, value: 2}, {
        source: 1,
        target: 4,
        value: 2
    }, {source: 1, target: 0, value: 2}, {source: 1, target: 4, value: 2}, {source: 4, target: 3, value: 1}]
};