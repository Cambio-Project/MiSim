'use strict';

function pad(s, size)
{
    while (s.length < size) s = "0" + s;
    return s;
}

function colors(steps)
{
    var cols = [];
    var stepFactor = 1 / (steps - 1);
    var color1 = [255, 0, 50], color2 = [0, 150, 255];

    for(var i = 0; i < steps; i++)
    {
        var result = '#';
        for (var j = 0; j < 3; j++)
        {
            result += pad(Math.round(color1[j] + (stepFactor * i) * (color2[j] - color1[j])).toString(16), 2);
        }
        cols.push(result)
    }
    return cols;
}

function timeFormat(seconds)
{
    var date = new Date(1970, 0, 1);
    date.setSeconds(seconds);

    var h = pad(date.getHours());
    var m = pad(date.getMinutes());
    var s = pad(date.getSeconds());

    var format = ((h != "00") ? h + "h" : "") + m + "m" + s + "s";

    return format;
}

(function(factory) {
    if (typeof module === 'object' && module.exports) {
        module.exports = factory;
    } else {
        factory(Highcharts);
    }
}(function(Highcharts) {
    (function(Highcharts) {

        Highcharts.theme = {
            chart: {
                type: 'spline',
                zoomType: 'x',
                backgroundColor: null,
                style: {
                    fontFamily: 'Verdana, sans-serif'
                }
            },
            title: {
                style: {
                    fontSize: '16px',
                    fontWeight: 'bold',
                    color: '#000000',
                    textDecoration: 'underline'
                }
            },
            rangeSelector: {
                selected: 4,
                inputEnabled: false,
                buttonTheme: {
                    visibility: 'hidden'
                },
                labelStyle: {
                    visibility: 'hidden'
                }
            },
            legend: {
                itemStyle: {
                    fontWeight: 'bold',
                    fontSize: '13px',
                    color: 'rgba(0,0,0,0.8)'
                },
                itemHoverStyle: {
                    color: 'rgba(0,150,0,0.8)'
                },
                enabled: true,
                layout: 'vertical',
                align: 'right',
                verticalAlign: 'top'
            },
            xAxis: {
                gridLineWidth: 1,
                ordinal: false,
                /*alternateGridColor: 'rgba(50,50,50,0.8)',*/
                minorGridLineColor: 'rgba(50,50,50,0.2)',
                gridLineColor: 'rgba(50,50,50,0.8)',
                labels: {
                    style: {
                        fontSize: '12px',
                        color: 'rgba(50,50,50,0.8)'
                    },
                    formatter: function () {
                        return timeFormat(this.value) + "<br>" + this.value + "s";
                    },
                }
            },
            yAxis: {
                min: 0,
                opposite: false,
                minorTickInterval: 'auto',
                gridLineWidth: 1,
                /*alternateGridColor: 'rgba(50,50,50,0.8)',*/
                minorGridLineColor: 'rgba(50,50,50,0.2)',
                gridLineColor: 'rgba(50,50,50,0.8)',
                title: {
                    style: {
                        textTransform: 'uppercase'
                    }
                },
                labels: {
                    style: {
                        fontSize: '12px',
                        color: 'rgba(50,50,50,0.8)'
                    }
                }
            },
            tooltip: {
                shared: true,
                borderWidth: 1,
                shadow: false,
                backgroundColor: 'rgba(50,50,50,0.8)',
                style : {
                    border: '1px solid white',
                    color: '#FFFFFF'
                }
            },
            plotOptions: {
                spline: {
                    marker: {
                        enabled: false
                    },
                    tooltip: {
                        headerFormat: 'Time: <b>{point.x} s</b><br/>',
                        pointFormat: '<br/><span style="color:{series.color};">\u25CF</span> {series.name}: <b>{point.y}</b>'
                    }
                },
                scatter: {
                    marker: {
                        radius: 2
                    },
                    tooltip: {
                        headerFormat: 'Time: <b>{point.x} s</b><br/>',
                        pointFormat: '<br/><span style="color:{series.color};">\u25CF</span> {series.name}: <b>{point.y}</b>'
                    }
                },
                candlestick: {
                    lineColor: 'rgba(50,50,50,0.8)'
                }
            },
            credits: {
                enabled: false
            }
        };
        Highcharts.setOptions(Highcharts.theme);

    }(Highcharts));
}));
