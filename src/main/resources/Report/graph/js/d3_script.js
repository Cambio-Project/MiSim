// Setup
setTimeout(function(){
    if(graphMinimalistic == "minimalistic")
    {
        graphMinimalistic = true;
        document.getElementById('warning').style.display = "block";
    }
    else
    {
        graphMinimalistic = false;
    }
    document.getElementById('info').style.display = "none";

    var color = d3.scale.category20();
    var width = window.innerWidth;
    var height = window.innerHeight;
    var power = ((graph.nodes.length > 100) ? 50 : graph.nodes.length) * -10;
    var distance = 25 + ((graph.nodes.length > 100) ? 50 : graph.nodes.length) * 3;
    var svg = d3.select("#svg");//d3.select("body").append("svg").attr("width", '100%').attr("height", height - 1);
    var force = d3.layout.force().charge(power).linkDistance(distance).size([width, height]);

    graph = renameLinks(graph);
    graph = groupLinks(graph);
    force.nodes(graph.nodes).links(graph.links).on("tick", tick).start();

    //// Tooltip
    var tooltip = d3.select("body").append("div")
        .attr("class", "tooltip")
        .style("opacity", 0);

    // Links
    var link = svg.selectAll(".link")
        .data(graph.links)
        .enter()
        .append("line")
        .attr("class", "link")
        .style("stroke-width", function (d) { return 2})
        //.style("stroke-width", function (d) { return Math.sqrt(d.value)})
        .on("mouseover", function(d,i) {
                tooltip.transition().duration(50).style("opacity", .9);
                tooltip.html(d.label)
                .style("left", (d3.event.pageX - d.label.length*3) + "px")
                .style("top", (d3.event.pageY - 25) + "px");
        })
        .on("mouseout", function(d) {
            tooltip.transition().duration(50).style("opacity", 0);
        });

    // Nodes
    var node = svg.selectAll(".node")
        .data(graph.nodes)
        .enter()
        .append("g")
        .attr("class", "node")
        .attr("id", function(d){ return "group-" + d.group})
        .call(force.drag)
        .append("circle")
        .attr("r", 8)
        .style("fill", function (d) {return color(d.group); })
        .on("mouseover", function(d,i) {
            tooltip.transition().duration(200).style("opacity", .9);
            tooltip.html(d.name)
            .style("left", (d3.event.pageX - d.name.length*3) + "px")
            .style("top", (d3.event.pageY - 35) + "px");
        })
        .on("mouseout", function(d) {
            tooltip.transition().duration(500).style("opacity", 0);
        });
        /*
        .on("click", function(d) {
                svg.selectAll("#group-" + d.group).style("opacity", 0);
            });*/

    var text = d3.selectAll(".text");
    var label = d3.selectAll("text.label")


    // Update
    function tick(e)
    {
        var k = 6 * e.alpha;
        node.forEach(function(o, i) {

            if(o.group == 0) {
                o.x += i;
                o.y += i;
            }
        });

        link.attr("x1", function (d) {return d.source.x;})
            .attr("y1", function (d) {return d.source.y;})
            .attr("x2", function (d) {return d.target.x;})
            .attr("y2", function (d) {return d.target.y;});

        node.attr("cx", function (d) {return d.x;})
            .attr("cy", function (d) {return d.y;});

        text.attr("x", function (d) {return d.x;})
            .attr("y", function (d) {return d.y;});

        label.attr('x', function(d) {return d.x;})
             .attr('y', function(d) {return d.y;});
    }
}, 2000);
// Map links from indexes to ids
function renameLinks(graph)
{
    var links = [];
    var i = 0;
    if(graph.hasOwnProperty('links'))
    {
        graph.links.forEach(function(l)
        {
            var sourceNode = graph.nodes.filter(function(n) { return n.group === l.source; })[0];
            var targetNode = graph.nodes.filter(function(n) { return n.group === l.target; })[0];

            links.push({source: sourceNode.group, target: targetNode.group, value: l.value, label : l.label});
        });
    }
    graph.links = links;
    return graph;
}

// Create links between groups
function groupLinks(graph)
{
    var links = [];
    var groups = {};

    graph.links.forEach(function(l)
    {
        if(!groups[l.source])
            groups[l.source] = [];
        if(groups[l.source].indexOf(l.target) == -1)
            groups[l.source].push(l.target);
    });

    for(var g in groups)
    {
        var sourceNodes = graph.nodes.filter(function(n) { return n.group == g});
        var targetNodes = graph.nodes.filter(function(n) { return groups[g].indexOf(n.group) >= 0});

        sourceNodes.forEach(function(s)
        {
            var linkCounter = targetNodes.length;
            var index = 0;
            var label_name = "";
            targetNodes.forEach(function(t)
            {
                if(s.labels[index]) {
                    label_name = s.labels[index++];
                } else {
                    index = 0
                    label_name = s.labels[index++];
                }
                links.push({source : s, target : t, label : label_name});
            });
        });
    }

    graph.links = links;
    return graph;
}

function toggleGroup()
{

}
