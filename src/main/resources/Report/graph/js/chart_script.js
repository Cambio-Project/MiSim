function showLines(id)
{
    var chart = $("#" + id).highcharts();
    for(var series in chart.series)
    {
        chart.series[series].visible = true;
    }
}

function hideLines(id)
{
    var chart = $("#" + id).highcharts();
    for(var series in chart.series)
    {
        chart.series[series].visible = false;
    }
}

function showAll(classname)
{
    var chart_containers = document.getElementsByClassName(classname);
    for(var cc in chart_containers)
    {
        showLines(chart_containers[cc].id);
    }
}

function hideAll(classname)
{
    var chart_containers = document.getElementsByClassName(classname);
    for(var cc in chart_containers)
    {
        hideLines(chart_containers[cc].id);
    }
}

function toggleLines(id)
{
    var chart = $("#" + id).highcharts();
    var isVisible = chart.series[0].visible;
    for(var series in chart.series)
    {
        if(isVisible)
        {
            chart.series[series].hide();
        }
        else
        {
            chart.series[series].show();
        }
    }
}

function unsmoothYAxis(id)
{
    var chart = $("#" + id).highcharts();
    chart.yAxis[0].setExtremes(chart.yAxis[0].min, chart.yAxis[0].max/2);
}

function smoothYAxis(id)
{
    var chart = $("#" + id).highcharts();
    chart.yAxis[0].setExtremes(chart.yAxis[0].min, chart.yAxis[0].max*2);
}